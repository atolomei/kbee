package com.novamens.kbee.content.domain.provisioning;



import java.time.OffsetDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.security.ContentSecurityDao;

import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.dao.SecurityDao;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.command.AbstractCommand;
import com.novamens.kbee.security.KbeeSecurityDao;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

/**
 * Creates {@code Domain Admin} and {@code SuperUser} Roles
 * 
 * Encapsulated by a CommandRequest
 * 
 * com.novamens.kbee.content.command.AddGeneralRolesCommand
 * 
 * {@link SQLException} that prevent the Trx from committing must be propagated to the Scheduler
 * 
 */
public class AddGeneralRolesCommand extends AbstractCommand {
			
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AddGeneralRolesCommand.class.getName());
	
	private Domain domain;
	
	private int total_domains = 0;
	private int total_domains_processed = 0;
	
	private StringBuilder result;
	
	public AddGeneralRolesCommand() {
		setName("Add Canonical Groups and Roles");
		setDescription ("Add Canonical Groups from KbeeGeneralRole (Workspace, Reports, etc.) and Roles (Domain Admin, Superuser, etc.). If domain is null then all domains are processed.");
	}
	
	
	
	/**
	 * 
	 * 
	 * @see  DomainRolesCanonicalBuilderService
	 * 
	 */
	@Override
	public void execute() {
		
	setDateStarted(OffsetDateTime.now());
	setProgress(0);
	
	result = new StringBuilder();
	
	logger.debug("Domain: " + getDomain()!=null?getDomain():"all");
	
	// StringBuilder str = new StringBuilder();
	
	try {
					
		ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
		
		if (getDomain()==null) {
			
			this.total_domains = getContentDao().getDomains().size();
			this.total_domains_processed = 0;
			
			if (this.total_domains>0) {
				
							for (Domain domain: getContentDao().getDomains()) {
								
								try {
									// 	TRX is Scheduler
									createCanonicalGroupsIfNotExists(domain);
									
								} catch (Exception e) {
									logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName()+ " | " + e.getMessage());
								}

								// TRX is Scheduler
								domain.getService(DomainRolesCanonicalBuilderService.class).buildNoTrx();
								
								this.total_domains_processed++;
								
								setProgress(total_domains_processed/this.total_domains);
								
								//if (str.length()>0)
								//	str.append(" | ");
								
								// str.append(domain.getName());
							}
			}
		}
		else {
						this.total_domains = 1;
						createCanonicalGroupsIfNotExists(getDomain());
						getDomain().getService(DomainRolesCanonicalBuilderService.class).buildNoTrx();
						this.total_domains_processed++;
		}
					
 		setProgress(100.0);
 		setResult("OK");
 		setState(CommandState.COMPLETED);
 					
	} catch (ContentMgmtException | ContentCreationException e) {
		logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + e.getMessage());
		setState(CommandState.ERROR);
		setResult(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName()+ " | " + e.getMessage());
		/** {@link SQLException} that prevent the Trx from committing must be propagated to the Scheduler */
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
			setResultDetails(result.toString());
		}
	}
	

	private void createCanonicalGroupsIfNotExists(Domain domain) {
	
		List<String> names = getContentSecurityDao().canonicalGroupsMissing(domain);
		
		boolean ex = false;
		int n = 0;
		
		if (result.length()>0)
			result.append(" | ");
		
		result.append(domain.getName());
		
		for (String name : names) {
			User domain_root = getRootUser(domain);
			SecurityContentMgmtService service = ServiceLocator.getService(SecurityContentMgmtService.class);
			KbeeGlobalRole globalrole = KbeeGlobalRole.getGlobalRoleByKey(name);
			
			if (domain_root==null)
				domain_root = getRootUser(getContentDao().findDomainByName("kbee"));
			
			KbeeGroup group = (KbeeGroup) service.createGroup(name, domain, domain_root, true, globalrole.getAreaCode());
			logger.debug("Creating Group -> " + group.getName() + "  | " + domain.getName());
			
			if (!ex) {
				result.append( " -> " ); 
				ex = true;
			}
			
			if (n>0)
				result.append(" - ");
			
			result.append(group.getName());
			n++;
		}
		
	}

	@Override
	public void setDomain(Domain domain) {
		this.domain=domain;
	}

	@Override
	public Domain getDomain() {
		if (domain == null) {
			if (getDomainId() == null) {
				if (getParameters().containsKey("domain")) {
					this.domain = getContentDao().findDomainByName((String) getParameters().get("domain"));
				}
			}
			else { 
				domain = getContentDao().findDomainById(getDomainId());
				if (domain==null)
					domain = getContentDao().findDomainByName(getDomainId().toString().trim().toLowerCase());
			}
		}
		return this.domain;
	}
	
	private ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	

	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private User getRootUser(Domain domain) {
		User rootuser = ((KbeeSecurityDao) getSecurityDao()).findUserByName("root@"+ domain.getName());
		return rootuser;
	}
	
	private SecurityDao  getSecurityDao() {
		return	(SecurityDao) ServiceLocator.getService(BeansService.class).getBean("securityDao");
	}

}
