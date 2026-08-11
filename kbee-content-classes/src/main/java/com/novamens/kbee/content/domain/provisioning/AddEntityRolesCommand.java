package com.novamens.kbee.content.domain.provisioning;

import java.time.OffsetDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.command.AbstractCommand;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class AddEntityRolesCommand extends AbstractCommand {

	static Logger logger = LogManager.getLogger(AddEntityRolesCommand.class.getName());
	
	private int total_domains = 0;
	private int total_domains_processed = 0;
	
	public AddEntityRolesCommand() {
		setName("Add Entity Roles");
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
								domain.getService(DomainRolesEntityBuilderService.class).buildNoTrx();
								this.total_domains_processed++;
								setProgress(total_domains_processed/this.total_domains);
								if (str.length()>0)
									str.append(" | ");
								str.append(domain.getName());
								//list.forEach(item -> str.append((str.length()>0? (", "+item.getName()):item.getName())));
							}
						}
		}
		else {
						this.total_domains = 1;
						getDomain().getService(DomainRolesEntityBuilderService.class).buildNoTrx();
						this.total_domains_processed++;
						//str.append(domain.getName()+": ");
						//list.forEach(item -> str.append((str.length()>0? (", "+item.getName()):item.getName())));
		}
					
 		setProgress(100);
 		setResult("OK");
 		setState(CommandState.COMPLETED);
 		
		} catch (ContentMgmtException | ContentCreationException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + e.getMessage());
			throw(e);
 					
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
	

	
	//public void setDomain(Domain domain) {
	//	this.domain=domain;
	//}
	
	
	public Domain getDomain() {
		if (super.getDomain() == null) {
			if (getDomainId() == null) {
				if (getParameters().containsKey("domain")) {
					setDomain( getContentDao().findDomainByName((String) getParameters().get("domain")));
				}
			}
			else { 
				Domain d = getContentDao().findDomainById(getDomainId());
				if (d==null)
					d = getContentDao().findDomainByName(getDomainId().toString().trim().toLowerCase());
				setDomain(d);
			}
		}
		return super.getDomain();
	}


	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	
	


}
