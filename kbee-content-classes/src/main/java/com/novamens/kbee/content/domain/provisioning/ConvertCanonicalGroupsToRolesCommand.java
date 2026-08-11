package com.novamens.kbee.content.domain.provisioning;

import java.time.OffsetDateTime;

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.command.AbstractCommand;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class ConvertCanonicalGroupsToRolesCommand extends AbstractCommand {
			
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ConvertCanonicalGroupsToRolesCommand.class.getName());
	
	private Domain domain;
	
	private int total_domains = 0;
	private int total_domains_processed = 0;
	
	
	public  ConvertCanonicalGroupsToRolesCommand() {
		setDescription("Convert Domain Admin and Support to Role ");
	}
	
	
	@Override
	public void execute() {
		
	setDateStarted(OffsetDateTime.now());
	setProgress(0);
	
	logger.debug("Domain: " + getDomain()!=null?getDomain():"all");
	
	StringBuilder str = new StringBuilder();
	
	try {
					
		ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
				
		if (getDomain()==null) {

						this.total_domains = getContentDao().getDomains().size();
						this.total_domains_processed = 0;
						if (this.total_domains>0) {
							for (Domain domain: getContentDao().getDomains()) {
								// TRX is Scheduler
								//List<Role> list = ServiceLocator.getService(DomainLifeCycleService.class).addEntityRolesIfNotExistsNoTrx(domain);
								processDomain(domain);
								this.total_domains_processed++;
								setProgress(total_domains_processed/this.total_domains);
								break;
							}
						}
		}
		else {
						this.total_domains = 1;
						// List<Role> list = ServiceLocator.getService(DomainLifeCycleService.class).createCanonicalRolesIfNotExistsNoTrx(getDomain());
						this.total_domains_processed++;
						processDomain(getDomain());
		}
					
 		setProgress(100);
 		setResult("OK");
 		setState(CommandState.COMPLETED);
 					
 					
	 	} catch (Exception e) {
	 				setState(CommandState.ERROR);
	 				setResult(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName()+ " | " + e.getMessage());
					logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName()+ " | " + e.getMessage());
		}
		finally {
			logger.debug("done " + String.valueOf(this.total_domains_processed));
			setDateTerminated(OffsetDateTime.now());
			setResult("Domains " + String.valueOf(this.total_domains_processed));
			setResultDetails(str.toString());
		}
	}
	

	
	public Domain getDomain() {
		if (domain == null) {
			if (getDomainId() != null) { 
				domain = getContentDao().findDomainById(getDomainId());
				if (domain==null)
					domain = getContentDao().findDomainByName(getDomainId().toString().trim().toLowerCase());
			}
		}
		return domain;
	}

	
	private void processDomain(Domain domain) {
		
	}
	
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	
	


}
