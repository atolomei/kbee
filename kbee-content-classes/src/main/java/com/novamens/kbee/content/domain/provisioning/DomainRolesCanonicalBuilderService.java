package com.novamens.kbee.content.domain.provisioning;



import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.library.Library;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.DomainRole;
import com.novamens.content.security.Role;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.kbee.content.library.KbeeLibrary;
import com.novamens.kbee.content.security.KbeeDomainRole;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceLocator;


/**
 * 
 *
 */
public class DomainRolesCanonicalBuilderService extends BaseDomainBuilder implements ObjectService {
				
	/** Logger that works synchronously in the TRX thread */
	// static private Logger txlogger = LogManager.getLogger("TxLogger");

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DomainRolesCanonicalBuilderService.class.getName());
	
	private boolean trx = true;

	
	public DomainRolesCanonicalBuilderService() {
		
	}
	
	
	public DomainRolesCanonicalBuilderService(Domain domain) {
		super(domain);
	}
	
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void build() throws ContentMgmtException, ContentCreationException {
		if (getBuildingDomain().getDomainType()==null || getBuildingDomain().getDomainType()==DomainType.EXPRESS)
			build(DomainType.EXPRESS.getAlias());
		else
			build("premium");
	}
	
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void build(String imodeltype) throws ContentMgmtException, ContentCreationException {
			xbuild(imodeltype);
	}
	
	
	public void buildNoTrx() throws ContentMgmtException, ContentCreationException {
		this.setTrx(false);
		if (     getBuildingDomain().getDomainType()==null || getBuildingDomain().getDomainType()==DomainType.EXPRESS)    		xbuild(DomainType.EXPRESS.getAlias());
		else if (getBuildingDomain().getDomainType()==null || getBuildingDomain().getDomainType()==DomainType.PREMIUM)  		xbuild("premium");
		else if (getBuildingDomain().getDomainType()==null || getBuildingDomain().getDomainType()==DomainType.COMPLIANCE)   	xbuild("compliance");
		else if (getBuildingDomain().getDomainType()==null || getBuildingDomain().getDomainType()==DomainType.SYSTEM)  			xbuild("system");
		else
			xbuild("premium");
			

	}
	
					
	public void buildNoTrx(String imodeltype) throws ContentMgmtException, ContentCreationException {
		this.setTrx(false);
		xbuild(imodeltype);
	}
	
	/**
	 * Premium-empty  (assign)
	 */
	
	private void xbuild(String ximodeltype) throws ContentMgmtException, ContentCreationException {
							
		logger.debug("starting to build Canonical Roles for domain: " + getBuildingDomain().getName() + "  Type:" + ximodeltype);
		
		StringBuilder str = new StringBuilder();
				
		List<Group> canonical_groups = getContentSecurityDao().getCanonicalGroups(getBuildingDomain());
		
		List<Role> list = getContentSecurityDao().getRoles(getBuildingDomain());
		List<Role> canonical_roles = new ArrayList<Role>();

		boolean is_domain = false;
		boolean is_super_user = false;
		boolean is_support = false;
		boolean is_reports = false;
		
		boolean is_operations = false;
		// boolean is_support_agent = false;
		boolean is_general_user  = false;
		
		for (Role role: list) {
			try {
				if (role instanceof DomainRole) {
						if (role.getAlias()!=null) {
							if (role.getAlias().toLowerCase().trim().equals("domain-admin")) { 
								is_domain = true;
								canonical_roles.add(role);
							}
							else if (role.getAlias().toLowerCase().trim().equals("super-user")) { 
								is_super_user  = true;
								canonical_roles.add(role);
							}
							else if (role.getAlias().toLowerCase().trim().equals("support")) { 
								is_support  = true;
								canonical_roles.add(role);
							}
							else if (role.getAlias().toLowerCase().trim().equals("reports")) { 
								is_reports  = true;
								canonical_roles.add(role);
							}
							else if (role.getAlias().toLowerCase().trim().equals("operations-engineer")) { 
								is_operations  = true;
								canonical_roles.add(role);
							}
							else if (role.getAlias().toLowerCase().trim().equals("general-user")) { 
								is_general_user  = true;
								canonical_roles.add(role);
							}
						}
						else {
							if (role.getName()!=null && role.getName().toLowerCase().trim().equals("domain admin")) {
								is_domain = true;
								canonical_roles.add(role);
							}
							else if (role.getName()!=null && role.getName().toLowerCase().trim().equals("super user")) {
								is_super_user  = true;
								canonical_roles.add(role);
							}
							else if (role.getName()!=null && role.getName().toLowerCase().trim().equals("support")) {
								is_support  = true;
								canonical_roles.add(role);
							}
							else if (role.getName()!=null && role.getName().toLowerCase().trim().startsWith("operations")) {
								is_operations  = true;
								canonical_roles.add(role);
							}
							else if (role.getName()!=null && role.getName().toLowerCase().trim().startsWith("reports")) { 
								is_reports  = true;
								canonical_roles.add(role);
							}
							else if (role.getName()!=null && role.getName().toLowerCase().trim().startsWith("general user")) { 
								is_general_user  = true;
								canonical_roles.add(role);
							}


						}
				}
				
			} catch (ContentMgmtException | ContentCreationException e) {
				logger.error(e);
				throw(e);
				
			} catch (Exception e) {
				logger.error(e);
				return;
			}
		}

		if (getBuildingDomain().getName().equals("kbee") && !is_operations) {
			
			SecurityContentMgmtService service = ServiceLocator.getService(SecurityContentMgmtService.class);
			
			try {
				KbeeDomainRole role;
				
				if (this.isTrx())		
					role = (KbeeDomainRole) service.createRole(DomainRole.TYPE, getBuildingDomain());
				else					
					role = (KbeeDomainRole) service.createRoleNoTrx(DomainRole.TYPE, getBuildingDomain());
			
				is_operations = true;
				
				role.setAlias("operations-engineer");
				role.setCanonical(true);
				role.setApiEnabled(true);
				role.setDomain(getBuildingDomain());
				
				
				role.setName( getLanguageService().getString("operations-engineer", getBuildingDomain().getLocale()));
				List<Group> groups = new ArrayList<Group>();
											
				for (Group g: canonical_groups) {
						if (g.getName().equals(KbeeGlobalRole.OPERATIONS_ENGINEER.getId())) groups.add(g);
				}	
				
				if (!groups.isEmpty()) {
					role.setGroups(groups);
					logger.debug("Creating Role Operations Engineer");
					if (this.isTrx()) 			
						ServiceLocator.getService(SecurityContentMgmtService.class).update(role, "save Operations Engineer");
					else						
						ServiceLocator.getService(SecurityContentMgmtService.class).updateNoTrx(role, "save Operations Engineer");
					logger.debug("Created " + getBuildingDomain().getName() + " Role:" + role.getName()+ " (" + role.getAlias()+")");
					canonical_roles.add(role);
				}
				else {
					logger.error(role.getName() + " -> Can not add Canonical Group");
				}
				
					
			} catch (ContentMgmtException | ContentCreationException e) {
				logger.error(e);
				throw(e);
				
			} catch (Exception e) {
				logger.error(e);
			}
		}
		
		// ---------------------------------------------
		//
		//
		if (!is_domain) {

			SecurityContentMgmtService service = ServiceLocator.getService(SecurityContentMgmtService.class);
			
			try {
				KbeeDomainRole role;
				if (this.isTrx())		
					role = (KbeeDomainRole) service.createRole(DomainRole.TYPE, getBuildingDomain());
				else					
					role = (KbeeDomainRole) service.createRoleNoTrx(DomainRole.TYPE, getBuildingDomain());
			
				is_domain = true;
				
				role.setAlias("domain-admin");
				role.setCanonical(true);
				role.setApiEnabled(true);
				role.setDomain(getBuildingDomain());
				
				
				role.setName(getLanguageService().getString("domain-admin", getBuildingDomain().getLocale()) );
				List<Group> groups = new ArrayList<Group>();
											
				for (Group g: canonical_groups) {
					if 		(g.getName().equals(KbeeGlobalRole.DOMAIN_ADMIN.getId())) groups.add(g);
					
					
					else if (g.getName().equals(KbeeGlobalRole.WORKSPACE.getId())) groups.add(g);
					
					//else if (g.getName().equals(KbeeGlobalRole.WORKSPACE_BULK_ACTIONS.getId())) groups.add(g);
					
					else if (g.getName().equals(KbeeGlobalRole.PENDING_TASKS.getId())) groups.add(g);
														
					//else if (g.getName().equals(KbeeGlobalRole.WORKSPACE_MY_RESOURCES.getId())) groups.add(g);
					else if (g.getName().equals(KbeeGlobalRole.MONITOR_AUDIT.getId())) groups.add(g);
					else if (g.getName().equals(KbeeGlobalRole.INFORMATION_MODEL.getId())) groups.add(g);
					
					else if (g.getName().equals(KbeeGlobalRole.DATASET_VALUES_WRITE.getId())) groups.add(g);
					else if (g.getName().equals(KbeeGlobalRole.SETTINGS.getId())) groups.add(g);
					
					else if (g.getName().equals(KbeeGlobalRole.SECURITY.getId())) groups.add(g);
					else if (g.getName().equals(KbeeGlobalRole.PORTAL_ADMIN.getId())) groups.add(g);
					else if (g.getName().equals(KbeeGlobalRole.REPORTS.getId())) groups.add(g);

					
					
					
					if (getBuildingDomain().getName().equals("kbee")) {
						if (g.getName().equals(KbeeGlobalRole.SERVICE_ADMIN.getId())) groups.add(g);
						if (g.getName().equals(KbeeGlobalRole.API_DEVELOPER.getId())) groups.add(g);
						if (g.getName().equals(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId())) groups.add(g);
						if (g.getName().equals(KbeeGlobalRole.OPERATIONS_ENGINEER.getId())) groups.add(g);
						if (g.getName().equals(KbeeGlobalRole.SUPPORT_AGENT.getId())) groups.add(g);
					}
				}	
				
				if (!groups.isEmpty()) {
					role.setGroups(groups);
					logger.debug("Creating Role Domain Admin");
					if (this.isTrx()) 			
						ServiceLocator.getService(SecurityContentMgmtService.class).update(role, "save Domain Admin");
					else						
						ServiceLocator.getService(SecurityContentMgmtService.class).updateNoTrx(role, "save Domain Admin");
						
					
					str.append(role.getAlias()+"  ");
					
					logger.debug("Created " + getBuildingDomain().getName() + " Role:" + role.getName()+ " (" + role.getAlias()+")");
					canonical_roles.add(role);
				}
				else {					
					logger.error(role.getName() + " -> is not adding Canonical Groups");
				}
					
			} catch (ContentMgmtException | ContentCreationException e) {
				logger.error(e);
				throw(e);
				
			} catch (Exception e) {
				logger.error(e);
			}
		}
		
		
		// -----------------------------
		// 
		// 
		if (!is_general_user) {

			SecurityContentMgmtService service = ServiceLocator.getService(SecurityContentMgmtService.class);
			
			try {
				KbeeDomainRole role;
				if (this.isTrx())	role = (KbeeDomainRole) service.createRole(DomainRole.TYPE, getBuildingDomain());
				else				role = (KbeeDomainRole) service.createRoleNoTrx(DomainRole.TYPE, getBuildingDomain());
			
				is_general_user = true;
				
				role.setAlias("general-user");
				role.setCanonical(true);
				role.setApiEnabled(true);
				role.setDomain(getBuildingDomain());
				
				// operations-engineer, "Operations Engineer"
				// domain-admin "Domain Admin"
				// "Super User"
				// "General user"

				
				role.setName(getLanguageService().getString("general-user", getBuildingDomain().getLocale())  );
				List<Group> groups = new ArrayList<Group>();
											
				int order=-1;
				
				for (Library lib: getRepository(Library.class).findAll(getBuildingDomain())) {
					if ( (order==-1) || (lib.getOrder()<order)) {
						if (((KbeeLibrary)lib).getReaders()!=null) {
							groups.add(((KbeeLibrary)lib).getReaders());
							order=lib.getOrder();
							logger.debug("Adding library group " + ((KbeeLibrary)lib).getReaders().getName());
						}
					}
				}
				
				if (!groups.isEmpty()) {
					role.setGroups(groups);
					role.setDefault(true);
					logger.debug("Creating Role General User (default role)");
					if (this.isTrx())		ServiceLocator.getService(SecurityContentMgmtService.class).update(role, "save General User");
					else					ServiceLocator.getService(SecurityContentMgmtService.class).updateNoTrx(role, "save General User");
					
					str.append(role.getAlias()+"  ");
					
					logger.debug("Created " + getBuildingDomain().getName() + " Role:" + role.getName()+ " (" + role.getAlias()+")");
					canonical_roles.add(role);
				}
				else {
					logger.error(role.getName() + " Can not add Library Group (list is empty)");
				}
					
			} catch (ContentMgmtException | ContentCreationException e) {
				logger.error(e);
				throw(e);
				
			} catch (Exception e) {
				logger.error(e);
			}
		}
		
		
		
		
		
		/**
		 * 
		 */
		if (!is_super_user) {
			
			KbeeDomainRole role;
			if (this.isTrx())
				role = (KbeeDomainRole) ServiceLocator.getService(SecurityContentMgmtService.class).createRole(DomainRole.TYPE, getBuildingDomain());
			else
				role = (KbeeDomainRole) ServiceLocator.getService(SecurityContentMgmtService.class).createRoleNoTrx(DomainRole.TYPE,  getBuildingDomain());
			role.setAlias("super-user");
			role.setCanonical(true);
			role.setApiEnabled(false);
			role.setDomain( getBuildingDomain());
			role.setName(getLanguageService().getString("super-user", getBuildingDomain().getLocale())  );
			
			
			is_super_user = true;
			
			List<Group> groups =  new ArrayList<Group>();
			
			for (Group g: canonical_groups) {
				if (g.getName().equals(KbeeGlobalRole.SU.getId()))  {
					groups.add(g);
					break;
				}
			}
			
			if (!groups.isEmpty()) {
				role.setGroups(groups);
				logger.debug("Creating Role Super User");
				if (this.isTrx()) 
					ServiceLocator.getService(SecurityContentMgmtService.class).update(role, "save Super user");
				else 	 
					ServiceLocator.getService(SecurityContentMgmtService.class).updateNoTrx(role, "save Super user");
				canonical_roles.add(role);
				str.append(role.getAlias()+"  ");
			}
			else {
				logger.error(role.getName() + " Can not add Canonical Group");
			}
			logger.debug("Created " + getBuildingDomain().getName() + " Role:" + role.getName()+ " (" + role.getAlias()+")");
			
		}
		
		
		/**
		 * 
		 */
		if (!is_support) {
			
			KbeeDomainRole role;
			
			if (this.isTrx())	
				role = (KbeeDomainRole) ServiceLocator.getService(SecurityContentMgmtService.class).createRole(DomainRole.TYPE, getBuildingDomain());
			else		
				role = (KbeeDomainRole) ServiceLocator.getService(SecurityContentMgmtService.class).createRoleNoTrx(DomainRole.TYPE, getBuildingDomain());
			
			is_support = true;
			
			role.setAlias("support");
			role.setCanonical(true);
			role.setApiEnabled(false);

			role.setDomain(getBuildingDomain());
			role.setName(getLanguageService().getString("support", getBuildingDomain().getLocale())  );
			
			List<Group> groups =  new ArrayList<Group>();
			for (Group g: canonical_groups) {
				if 		(g.getName().equals(KbeeGlobalRole.SUPPORT.getId())) 					groups.add(g);
				else if (g.getName().equals(KbeeGlobalRole.WORKSPACE.getId())) 					groups.add(g);
				//else if (g.getName().equals(KbeeGlobalRole.WORKSPACE_BULK_ACTIONS.getId())) 	groups.add(g);
				else if (g.getName().equals(KbeeGlobalRole.ARCHIVE.getId())) 					groups.add(g);
				else if (g.getName().equals(KbeeGlobalRole.MONITOR_AUDIT.getId())) 				groups.add(g);
				else if (g.getName().equals(KbeeGlobalRole.PENDING_TASKS.getId())) 				groups.add(g);
			}
			
			if (!groups.isEmpty()) {
				role.setGroups(groups);
				logger.debug("Creating Role Support");
				if (this.isTrx())
					ServiceLocator.getService(SecurityContentMgmtService.class).update(role, "save");
				else
					ServiceLocator.getService(SecurityContentMgmtService.class).updateNoTrx(role, "save");
				canonical_roles.add(role);
				
				str.append(role.getAlias()+"  ");
				logger.debug("Created " + getBuildingDomain().getName() + " Role:" + role.getName()+ " (" + role.getAlias()+")");
			}
			else {
				logger.error(role.getName() + " Can not add Role Support");
			}
		}

		

		
		
		/**
		 * 
		 */
		if (!is_reports) {
			
			KbeeDomainRole role;
			
			if (this.isTrx())	
				role = (KbeeDomainRole) ServiceLocator.getService(SecurityContentMgmtService.class).createRole(DomainRole.TYPE, getBuildingDomain());
			else		
				role = (KbeeDomainRole) ServiceLocator.getService(SecurityContentMgmtService.class).createRoleNoTrx(DomainRole.TYPE, getBuildingDomain());
			
			is_reports = true;
			
			role.setAlias("reports");
			role.setCanonical(true);
			role.setApiEnabled(false);

			
			// operations-engineer, "Operations Engineer"
			// domain-admin "Domain Admin"
			// "Super User"
			// "General user"
			// "Support"
			// "Reports"
			role.setName(getLanguageService().getString("reports", getBuildingDomain().getLocale())  );

			
			role.setDomain(getBuildingDomain());
			List<Group> groups =  new ArrayList<Group>();
			for (Group g: canonical_groups) {
				if 		(g.getName().equals(KbeeGlobalRole.REPORTS.getId())) 					groups.add(g);
			}
			
			if (!groups.isEmpty()) {
				role.setGroups(groups);
				logger.debug("Creating Role Reports");
				if (this.isTrx())
					ServiceLocator.getService(SecurityContentMgmtService.class).update(role, "save");
				else
					ServiceLocator.getService(SecurityContentMgmtService.class).updateNoTrx(role, "save");
				canonical_roles.add(role);
				
				str.append(role.getAlias()+"  ");
				logger.debug("Created " + getBuildingDomain().getName() + " Role:" + role.getName()+ " (" + role.getAlias()+")");
			}
			else {
				logger.error(role.getName() + " Can not add Role Reports");
			}
		}

		
		if (logger.isDebugEnabled() && str.length()>0)
			logger.debug("Roles created: " + str.toString());
		
	}
	
	public void setTrx(boolean b) {
		this.trx=b;
	}
	
	public boolean isTrx() {
		return this.trx;
	}
	


	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected Domain getDomainKbee() {
		return getContentDao().findDomainByName ("kbee");
	}

	protected ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	 
}
