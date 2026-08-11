package com.novamens.kbee.content.command;

import java.time.OffsetDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.novamens.beans.BeansService;
import com.novamens.content.base.SecurityRule;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.IQLRule;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.security.KbeeSecurityRule;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.scheduler.SchedulerService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class CreateDomainRuleCommand extends AbstractCommand implements Runnable  {
			
	
																
	private static Logger logger = LogManager.getLogger(CreateDomainRuleCommand.class.getName());

	@SuppressWarnings("unused")
	static private Logger trx_logger = LogManager.getLogger("TxLogger");
	
	
	private int errors = 0;
	private int converted = 0;
	private int processed= 0;
	private int total =0;
	
	private Thread thread;
	private boolean running;

	private SessionFactory sf;

	
	public CreateDomainRuleCommand() {
		setName("Create Domain Rule");
		setPriority(SchedulerService.HIGH_PRIORITY);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(getClass().getSimpleName());
		return str.toString();	
	}

	
	@Override
	public void run() {
		setState(CommandState.RUNNING);
		executeTask();
	}
	/** --------------------------------------------------------------------
	 */
	public boolean isRunning() {
	    	return this.running;
	}
	
	/** --------------------------------------------------------------------
	 */
	protected void setRunning(boolean value) {
    	this.running = value;
	}
	
	/** --------------------------------------------------------------------
	 */
	@Override
	public void execute() {

		this.thread = new Thread(this);
    	this.thread.setDaemon(false);
    	this.thread.setName(getName());
    	this.thread.setPriority(Thread.NORM_PRIORITY);
    	this.thread.start();
	}

	
	/** ---------------------------------------------------------------------------------------
	 * 
	 */
	private void executeTask() {
		
		logger.info("Starting Command execution " + getName());

		setDateStarted(OffsetDateTime.now());
		setProgress(0);
		
		
		try {
				// open Hibernate Session
				//
				this.sf = com.novamens.hibernate.session.Session.open();
		
				errors 		= 0;
				converted 	= 0;
				processed	= 0;
				total 		= 0;
		
				List<Domain> list = getContentDao().getDomains();
				
				total = list.size();
				
				ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
				
				User caller = ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();
				
				logger.info("Starting to process " + String.valueOf(total));
				
				for(Domain domain: list) {
					
					if (this.isStopped()) {
						break;
					}
				
					try {
				
						List<SecurityRule> rules = getContentDao().getSecurityRules(domain);
						
						boolean found = false;
						
						for (SecurityRule rule: rules) {
							if (rule.getCondition() !=null) {
								if (rule.getCondition().toLowerCase().equals(("domain("+domain.getId().toString()+")"))) {
									found = true;
									break;
								}
							}
						}
						
						if (!found) {
							
							Transaction transaction = null;
							boolean is_ok = false;
							
							try {
								
								transaction = sf.getCurrentSession().beginTransaction();

								KbeeSecurityRule drule = new KbeeSecurityRule();
								drule.setName("Domain " + domain.getName());
								drule.setCondition("domain("+domain.getId().toString()+")");
								drule.setType(IQLRule.RULE_COLLOQUIAL_IQL);
								
								drule.setLastModifiedUser(caller);
								drule.setLastModifiedOffsetDateTime(OffsetDateTime.now());
								drule.setDomain(domain);
								
								KbeeAcl acl = new KbeeAcl(); 
								acl.setLastModifiedUser(caller);
								acl.setLastModifiedOffsetDateTime(OffsetDateTime.now());
								
								drule.setAcl(acl);
								
								logger.info("Creating Rule " + drule.toString());
								
								getContentSecurityDao().save(drule);
								
								converted++;
								
								logger.info("Rule Added to Domain: " + domain.getName());
								is_ok = true;
							
							} catch (Exception e) {
							
								is_ok = false;
								errors++;
								logger.error(e.getStackTrace());
							
								if (errors > 30) {
									this.stop();
									break;
							}
							} finally {
								if (transaction!=null) {
									if (is_ok)
										transaction.commit();
									else
										transaction.rollback();
								}
							}
						}
						
						
					} catch (Exception e) {
						logger.error(e.getClass().getName());
					}  
					finally {
						processed++;
						setProgress((int)(100*processed/total));
						logger.info("progress: " + getProgress());
					}
				}
					
				setDateTerminated(OffsetDateTime.now());
		
				StringBuilder str = new StringBuilder();
				str.append("Total: " + String.valueOf(total));
				str.append("  |  Created: " + String.valueOf(converted));
				str.append("  |  Errors: " + String.valueOf(errors));
				
				setResultComments(str.toString());
				
				if (!isStopped()) {
					setProgress(100);
					setResult("OK");
					setState(CommandState.COMPLETED);
				}
				else {
					setResult("Cancelled by User");
					setState(CommandState.CANCELED);
				}
		
				logger.info("Ending Command execution " + getName());
		
		} finally {
			
				logger.info("Closing Session ");
				com.novamens.hibernate.session.Session.close();	
				setStatusInfo("DB Session closed.");
		}
}
	
 
	public String getStatement() {
		return (String)getParameter("statement");
	}


 	private ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
 
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}



}
