package com.novamens.kbee.content.service;

import java.time.OffsetDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.dao.DomainWipeDao;

import com.novamens.content.entity.Person;
import com.novamens.content.library.Library;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.ModelObject;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.model.PersonMember;
import com.novamens.content.model.UserSet;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.DomainRole;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.IQLRule;
import com.novamens.content.security.Role;
import com.novamens.content.security.RolesService;
import com.novamens.content.service.DataManagementException;
import com.novamens.content.service.DomainLifeCycleService;
import com.novamens.content.service.DomainService;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.service.UserImagesService;
import com.novamens.content.service.domain.DomainSettingsService;

import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserService;
import com.novamens.content.workflow.ProcessLauncher;

import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.dao.SecurityDao;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.Json;
import com.novamens.dom.KBFSStorageType;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.domain.provisioning.DomainSettingsBuilderService;
import com.novamens.kbee.content.entity.KbeePerson;
import com.novamens.kbee.content.library.KbeeLibrary;
import com.novamens.kbee.content.model.KbeeAttribute;
import com.novamens.kbee.content.model.KbeeAttributeTemplate;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeClassifierTemplate;
import com.novamens.kbee.content.model.KbeeContentTemplate;
import com.novamens.kbee.content.model.KbeeEntitySet;
import com.novamens.kbee.content.model.KbeeLabelSet;
import com.novamens.kbee.content.model.KbeeRelationTemplate;
import com.novamens.kbee.content.model.KbeeUserSet;

import com.novamens.kbee.content.model.KbeeValueSet;
import com.novamens.kbee.content.security.KbeeDomainRole;
import com.novamens.kbee.content.security.KbeeEntityRole;
import com.novamens.kbee.content.security.KbeeSecurityRule;
import com.novamens.kbee.content.user.KbeeUserProfile;
import com.novamens.kbee.content.user.KbeeUserRole;
import com.novamens.kbee.content.workflow.KbeeProcessLauncher;
import com.novamens.kbee.domain.KbeeDomain;

import com.novamens.kbee.json.KbeeJson;
import com.novamens.kbee.security.KbeeSecurityDao;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.kbee.security.acl.KbeeAclEntry;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.kbee.security.acl.KbeePermission;

import com.novamens.logging.DataSetValueCreateEvent;
import com.novamens.logging.DomainCreateEvent;
import com.novamens.logging.DomainUpdateEvent;
import com.novamens.logging.ModelCreateEvent;
import com.novamens.logging.ModelUpdateEvent;
import com.novamens.logging.SecurityCreateEvent;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.security.User;

import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.security.acl.Permission;
import com.novamens.service.BrandingService;
import com.novamens.service.ServiceLocator;
import com.novamens.system.SystemParameter;
import com.novamens.system.properties.SystemPropertiesService;
import com.novamens.workflow.Procedure;

import kbee.util.PropertiesFactory;

/** --------------------------------------------------------------
//
// Relaciones de los DataSet y DataSet Members
// DataSet Security Handlers
//
//  Usuarios y grupos no se crean (excepto los que tiene security handler ?)
//

/**
 * DomainBuilder:
 * 
 * DomainKbeeBuilder
 * DomainSimpleBuilder
 * DomainBasicBuilder
 * DomainComplianceBuilde
 * DomainEnterpriseBuilder
 * 
 */

/**
 *    
 *  The steps to create a Domain are:
 *  
 * 1. CreateDomain
 * 2. SetUpDomainFromTemplate  
 * 
 * Default Domain Template:
 * 
 * 1. Security: Canonical Users, Groups, Rules
 * 2. DataSet: Type
 * 2. Classifier: Type
 * 3. ContentTemplate: File(Type) Launcher: [Assign Workflow]
 * 4. Labels: Draft, Delete, Duplicate, Follow up
 * ---------------------------------------------------------------------------
 *  External API
 *  
 *  [External Users] -> System Parameters ?
 *  
 *  - OneSite user
 *  - TIBCO user
 *  - SSO user
 *  
 **/

public class KbeeDomainLifeCycleService implements DomainLifeCycleService {
			
	static String EMAIL_SUPPORT_1 	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("support1.email", 	 "support@kbee.io");
	static String EMAIL_SUPPORT_2 	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("support2.email", 	 "support@kbee.io");
	static String EMAIL_NOREPLY 	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("noreply.email", 	 "noreply@kbee.io");
	static String EXTERNAL_Library 	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("Library.external",  "LIB");
					
	static String DEFAULT_LABELS 	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("labels.default","Draft;Delete;Follow up;Duplicate;Review");
	static String DEFAULT_TYPES 	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("dataset_type.default","Training;Tenant Selection Plan;Contract;EOM Financials;Mortgage Statement;Lease Agreement;Rent Schedule;Management Agreements;Non-Disclosure Agreement;Shareholder Meetings;Lawsuits;Acquisitions;Due Diligence;Territory Assignments;Sales Incentives;Compensation Plan;Hardware;Software;System Logs;Benefits;Organizational Chart;Annual Reviews;Offer Letters;Signage;Brochures;Flyers");
	static String DEFAULT_STATUS 	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("dataset_status.default","Draft;Under Review;Approved;Final;Cancelled");
						
	static String DEFAULT_SECURED_ACCESS 	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("dataset_secured_access.default","Public;Secured");
	static String DEFAULT_DEPARTMENT= PropertiesFactory.getInstance("kbee").getProperties().getProperty("dataset_department.default","Marketing;HR;IT;Sales;Legal;Finance;Compliance;Property Management;Facilities;Training");
	
	static public final String DEFAULT_DOMAIN_NAME 	= "base";
	
	static private final int UNLIMITED_HD 		= -1;
	static private final int UNLIMITED_USERS 	=  0;

	static private final int NO_RENEW = -1;
	
	/** Logger that works synchronously in the TRX thread */
	static private Logger txlogger = LogManager.getLogger("TxLogger");
															
	static private Logger logger = LogManager.getLogger(KbeeDomainLifeCycleService.class.getName());
						
	static private Logger startlogger = LogManager.getLogger("StartupLogger");
	

	public static final int DEFAULT_HARD_DISK 	= 100;
	public static final int DEFAULT_USERS 		= -1;

	private String default_time_zone;
	
	private ContentDao dao=null;

	/** 
 	 *  KBEE
	 *  
	 * <b>IMPORTANT</b>: 
	 * Domain <b>kbee</b> must exist
	 * User <b>root</b> from Domain kbee must exists
	 *
	 * UserSet for Domain @kbee ?
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public boolean kbeeDomainStartUp() throws ContentMgmtException {
		Domain kbee = getDomainKbee();
		if (kbee==null) {
			startlogger.error("Domain kbee does not exists ");
			throw new ContentMgmtException ("Domain kbee does not exists !");
		}
		
		return false;
	
	}
	

	/***
	 *  KBEE 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void kbeeDomainInitRoot() throws ContentMgmtException {
		try {
			Person person = getContentDao().findUserProfileByUser(getDomainKbee().getService(DomainService.class).getRootUser()).getPerson();
			
			//if (person!=null && person.getPhoto()==null) {
			//	UserImagesService service = ServiceLocator.getService(UserImagesService.class);
			//	person.setPhoto(service.getDefaultImage(getDomainKbee().getService(DomainService.class).getRootUser().getUserName()));
			//	getContentDao().save(person);
			//}
			//else {
				// --------------------
				// UserProfile Person
				// --------------------
			//}
			
		} catch (Exception e) {
			startlogger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + e.getMessage());
	  		     logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + e.getMessage());
		}
	}

	
	/***
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Domain createCanonicalGroupsIfNotExists(Domain domain) throws ContentMgmtException, ContentCreationException {
		List<String> names = getContentSecurityDao().canonicalGroupsMissing(domain);
		for (String name : names) {
			User domain_root = getRootUser(domain);
			SecurityContentMgmtService service = ServiceLocator.getService(SecurityContentMgmtService.class);
			KbeeGlobalRole globalrole = KbeeGlobalRole.getGlobalRoleByKey(name);
			KbeeGroup group = (KbeeGroup) service.createGroup(name, domain, domain_root, true, globalrole.getAreaCode());
			logger.info("Creating Group " + group.getName() + " for Domain " + domain.getName());
		}
		return domain;
	}
	
	/***
	 * Domain Admin 
	 * Super User
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Role> createCanonicalRolesIfNotExists(Domain domain, List<Group> canonical_groups) throws ContentMgmtException, ContentCreationException  {
		 return addCanonicalRoles(domain, canonical_groups, true);
	}
	
	/**
	 * 
	 * TRX is managed by Scheduler.
	 * Canonical Groups must exist before the transaction that executes this.
	 * 
	 */
	@Override		
	public List<Role> createCanonicalRolesIfNotExistsNoTrx(Domain domain) throws ContentMgmtException, ContentCreationException {
		List<Group> canonical_groups = getContentSecurityDao().getCanonicalGroups(domain);
		return addCanonicalRoles(domain, canonical_groups, false);
	}
	

	@Override
	public List<Role> addEntityRolesIfNotExistsNoTrx(Domain domain)	throws ContentMgmtException, ContentCreationException {
		List<Group> canonical_groups = getContentSecurityDao().getCanonicalGroups(domain);
		return createEntityRoles(domain, canonical_groups, false);
	}
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Role> addEntityRolesIfNotExists(Domain domain, List<Group> canonical_groups) throws ContentMgmtException, ContentCreationException  {
		return createEntityRoles(domain, canonical_groups, true);
	}
	
	/***
	 * Domain Admin 
	 * Super User
	 */
	private List<Role> createEntityRoles(Domain domain, List<Group> canonical_groups, boolean trx) throws ContentMgmtException, ContentCreationException  {

		List<Role> list = getContentSecurityDao().getRoles(domain);
		List<Role> default_roles = new ArrayList<Role>();
		
		boolean is_pmanager = false;
		boolean is_dmanager = false;
		
		for (Role role: list) {
			try {
				if (role instanceof EntityRole && role.getAlias()!=null) {
						if (role.getAlias().toLowerCase().trim().equals("property-manager")) { 
							is_pmanager = true;
							default_roles.add(role);
						}
						else if (role.getAlias().toLowerCase().trim().equals("department-manager")) { 
							is_dmanager  = true;
							default_roles.add(role);
						}
				}
				
			} catch (Exception e ) {
				is_dmanager = true;
				is_pmanager = true;
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + e.getMessage());
				return default_roles;
			}
		}
		
		// Property Manager -------
		//
		if (!is_pmanager) {
			
			KbeeEntityRole  role = null;
			if (trx)		role = (KbeeEntityRole) ServiceLocator.getService(SecurityContentMgmtService.class).createRole(EntityRole.TYPE, domain);
			else			role = (KbeeEntityRole) ServiceLocator.getService(SecurityContentMgmtService.class).createRoleNoTrx(EntityRole.TYPE, domain);
			
			role.setAlias("property-manager");
			role.setCanonical(true);
			role.setDomain(domain);
			role.setName("property-manager");
								
			List<Group> groups 	=  new ArrayList<Group>();			
			
			for (Group g: canonical_groups) {
				if (g.getName().equals(KbeeGlobalRole.WORKSPACE.getId())) groups.add(g);
				//else if (g.getName().equals(KbeeGlobalRole.WORKSPACE_BULK_ACTIONS.getId())) groups.add(g);
				//else if (g.getName().equals(KbeeGlobalRole.CABINET_ENTERPRISE.getId())) groups.add(g);
				else if (g.getName().equals(KbeeGlobalRole.MONITOR_AUDIT.getId())) groups.add(g);
			}

			boolean bsave=false;
			
			if (!groups.isEmpty()) {
				role.setGroups(groups);
				logger.debug("Creating Role property-manager");
				bsave=true;
				default_roles.add(role);
			}
			
			for (Classifier cl:getContentDao().getClassifiers(domain)) {
				if (cl.getName()!=null && (cl.getName().toLowerCase().trim().equals("site name") || cl.getName().toLowerCase().trim().equals("property"))) {
					role.setClassifier(cl);
					bsave=true;
					break;
				}
			}
			
			List<Permission> ps = new ArrayList<Permission>();
			ps.add(KbeePermission.READ);
			role.setPermissions(ps);
			
			
			String arr[]= {"securedaccess", "Public"};
			
			if (arr.length>1) {
				String pred=arr[0].trim().toLowerCase(); // predicate. securedaccess
				String dms=arr[1].trim();  // value. Secured
				DataSet dataset = null;
				List<Classifier> lc = getContentDao().getClassifiers(domain);
				for (Classifier c: lc) {
						if (c.getPredicate().toLowerCase().equals(pred)) {
							dataset = c.getDataSet();
							break;
						}
				}
				if (dataset!=null) {
					DataSetMember dm = getContentDao().findMemberByValue(dataset, dms);
					if (dm!=null) {
						String con= pred + "("+dm.getId().toString()+ ")";
						role.setCondition(con);
						bsave=true;
					}
				}
			}

			
			if (bsave) {
				if (trx)			ServiceLocator.getService(SecurityContentMgmtService.class).update(role, "save property-manager");
				else				ServiceLocator.getService(SecurityContentMgmtService.class).updateNoTrx(role, "save property-manager");
			}
		}
		
		
		
		// Department Manager -------
		//
		if (!is_dmanager) {

			KbeeEntityRole role = null;
			
			if (trx)	 role = (KbeeEntityRole) ServiceLocator.getService(SecurityContentMgmtService.class).createRole(DomainRole.TYPE, domain);
			else		 role = (KbeeEntityRole) ServiceLocator.getService(SecurityContentMgmtService.class).createRoleNoTrx(DomainRole.TYPE, domain);
				
			role.setAlias("department-manager");
			role.setCanonical(true);
			role.setDomain(domain);
			role.setName("department-manager");
			List<Group> groups = new ArrayList<Group>();
			
			for (Group g: canonical_groups) {
				if (g.getName().equals(KbeeGlobalRole.WORKSPACE.getId())) groups.add(g);
				//if (g.getName().equals(KbeeGlobalRole.CABINET_ENTERPRISE.getId())) groups.add(g);
				if (g.getName().equals(KbeeGlobalRole.ARCHIVE.getId())) groups.add(g);
				if (g.getName().equals(KbeeGlobalRole.DATASET_VALUES_READ.getId())) groups.add(g);
				if (g.getName().equals(KbeeGlobalRole.MONITOR_AUDIT.getId())) groups.add(g);
				if (g.getName().equals(KbeeGlobalRole.WORKSPACE_BULK_ACTIONS.getId())) groups.add(g);
			}
			
			boolean bsave=false;
			
			if (!groups.isEmpty()) {
				role.setGroups(groups);
				bsave=true;
				default_roles.add(role);
			}
			
			for (Classifier cl:getContentDao().getClassifiers(domain)) {
				if (cl.getName()!=null && (cl.getName().toLowerCase().trim().equals("department"))) {
					role.setClassifier(cl);
					bsave=true;
					break;
				}
			}

			List<Permission> ps = new ArrayList<Permission>();
			
			ps.add(KbeePermission.READ);
			ps.add(KbeePermission.WRITE);
			ps.add(KbeePermission.DELETE);
			role.setPermissions(ps);
								
			String arr[]= {"securedaccess", "Public"};
			
			if (arr.length>1) {
				String pred=arr[0].trim().toLowerCase(); // predicate. securedaccess
				String dms=arr[1].trim();  // value. Secured
				DataSet dataset = null;
				List<Classifier> lc = getContentDao().getClassifiers(domain);
				for (Classifier c: lc) {
						if (c.getPredicate().toLowerCase().equals(pred)) {
							dataset = c.getDataSet();
							break;
						}
				}
				if (dataset!=null) {
					DataSetMember dm = getContentDao().findMemberByValue(dataset, dms);
					if (dm!=null) {
						String con= pred + "("+dm.getId().toString()+ ")";
						role.setCondition(con);
						bsave=true;
					}
				}
			}

			
			logger.debug("Saving Role department-manager");
			if (bsave) {
				if (trx)			ServiceLocator.getService(SecurityContentMgmtService.class).update(role, "save department-manager");
				else				ServiceLocator.getService(SecurityContentMgmtService.class).updateNoTrx(role, "save department-manager");
			}
		}
		return default_roles;
	}
	

	/***
	 * 
	 * Domain Admin
	 * Support
	 * Super User
	 * ---
	 * Property Manager (no ve secured)
	 * Department Manager (no ve secured)
	 * Secured 
	 */														
	private List<Role> addCanonicalRoles(Domain domain,  List<Group> canonical_groups, boolean trx) throws ContentMgmtException, ContentCreationException {
												
		List<Role> list = getContentSecurityDao().getRoles(domain);
		List<Role> canonical_roles = new ArrayList<Role>();

		boolean is_domain = false;
		boolean is_super_user = false;
		boolean is_support = false;
		boolean is_secured = false;
		
		for (Role role: list) {
			try {
				if (role instanceof DomainRole && role.getAlias()!=null) {
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
						else if (role.getAlias().toLowerCase().trim().equals("secured")) { 
							is_secured  = true;
							canonical_roles.add(role);
						}
				}
				
			} catch (ContentMgmtException | ContentCreationException e) {
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + e.getMessage());
				throw(e);
				
			} catch (Exception e ) {
				is_domain = true;
				is_super_user  = true;
				is_support=true;
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + e.getMessage());
				return canonical_roles;
			}
		}
		
		
		if (!is_domain) {

			SecurityContentMgmtService service = ServiceLocator.getService(SecurityContentMgmtService.class);
			
			try {
				KbeeDomainRole role;
				if (trx)
					role = (KbeeDomainRole) service.createRole(DomainRole.TYPE, domain);
				else
					role = (KbeeDomainRole) service.createRoleNoTrx(DomainRole.TYPE, domain);
					
				role.setAlias("domain-admin");
				role.setCanonical(true);
				role.setDomain(domain);
				role.setName("Domain Admin");
				List<Group> groups 			 =  new ArrayList<Group>();
				
				for (Group g: canonical_groups) {
					if (g.getName().equals(KbeeGlobalRole.DOMAIN_ADMIN.getId())) groups.add(g);
					if (g.getName().equals(KbeeGlobalRole.WORKSPACE.getId())) groups.add(g);
					//if (g.getName().equals(KbeeGlobalRole.WORKSPACE_BULK_ACTIONS.getId())) groups.add(g);
					if (g.getName().equals(KbeeGlobalRole.MONITOR_AUDIT.getId())) groups.add(g);
					if (g.getName().equals(KbeeGlobalRole.INFORMATION_MODEL.getId())) groups.add(g);
					if (g.getName().equals(KbeeGlobalRole.MODEL_READ.getId())) groups.add(g);
					if (g.getName().equals(KbeeGlobalRole.SETTINGS.getId())) groups.add(g);
					if (g.getName().equals(KbeeGlobalRole.DATASET_VALUES_WRITE.getId())) groups.add(g);
					if (g.getName().equals(KbeeGlobalRole.SECURITY.getId())) groups.add(g);
					if (g.getName().equals(KbeeGlobalRole.PORTAL_ADMIN.getId())) groups.add(g);
					if (g.getName().equals(KbeeGlobalRole.AUDITOR.getId())) groups.add(g);
				}	
				
				if (!groups.isEmpty()) {
					role.setGroups(groups);
					logger.debug("Creating Role Domain Admin");
					if (trx)
						ServiceLocator.getService(SecurityContentMgmtService.class).update(role, "save Domain Admin");
					else
						ServiceLocator.getService(SecurityContentMgmtService.class).updateNoTrx(role, "save Domain Admin");
						
					canonical_roles.add(role);
				}
				else {
					logger.error(role.getName() + " Can not add Canonical Group");
				}
					
			} catch (ContentMgmtException | ContentCreationException e) {
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + e.getMessage());
				throw(e);
				
			} catch (Exception e) {
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + e.getMessage());
			}
		}
		
		
		/**
		 * 
		 */
		if (!is_super_user) {
			
			KbeeDomainRole role;
			if (trx)
				role = (KbeeDomainRole) ServiceLocator.getService(SecurityContentMgmtService.class).createRole(DomainRole.TYPE, domain);
			else
				role = (KbeeDomainRole) ServiceLocator.getService(SecurityContentMgmtService.class).createRoleNoTrx(DomainRole.TYPE, domain);
			role.setAlias("super-user");
			role.setCanonical(true);
			role.setDomain(domain);
			role.setName("Super User");
			List<Group> groups 			 =  new ArrayList<Group>();
			for (Group g: canonical_groups) {
				if (g.getName().equals(KbeeGlobalRole.SU.getId()))  {
					groups.add(g);
					break;
				}
			}
			
			if (!groups.isEmpty()) {
				role.setGroups(groups);
				logger.debug("Creating Role Super User");
				if (trx) ServiceLocator.getService(SecurityContentMgmtService.class).update(role, "save Super user");
				else 	 ServiceLocator.getService(SecurityContentMgmtService.class).updateNoTrx(role, "save Super user");
				canonical_roles.add(role);
			}
			else {
				logger.error(role.getName() + " Can not add Canonical Group");
			}
		}
		
		/**
		 */
		if (!is_support) {
			KbeeDomainRole role;
			if (trx)	role = (KbeeDomainRole) ServiceLocator.getService(SecurityContentMgmtService.class).createRole(DomainRole.TYPE, domain);
			else		role = (KbeeDomainRole) ServiceLocator.getService(SecurityContentMgmtService.class).createRoleNoTrx(DomainRole.TYPE, domain);
			role.setAlias("support");
			role.setCanonical(true);
			role.setDomain(domain);
			role.setName("Support");
			List<Group> groups =  new ArrayList<Group>();
			for (Group g: canonical_groups) {
				if 		(g.getName().equals(KbeeGlobalRole.SUPPORT.getId())) 					groups.add(g);
				else if (g.getName().equals(KbeeGlobalRole.WORKSPACE.getId())) 					groups.add(g);
				//else if (g.getName().equals(KbeeGlobalRole.WORKSPACE_BULK_ACTIONS.getId())) 	groups.add(g);
				else if (g.getName().equals(KbeeGlobalRole.ARCHIVE.getId())) 					groups.add(g);
				else if (g.getName().equals(KbeeGlobalRole.MONITOR_AUDIT.getId())) 					groups.add(g);
				else if (g.getName().equals(KbeeGlobalRole.PENDING_TASKS.getId())) 				groups.add(g);
//				else if (g.getName().equals(KbeeGlobalRole.CABINET_ENTERPRISE.getId())) 		groups.add(g);
//				else if (g.getName().equals(KbeeGlobalRole.CABINET_EXTERNAL.getId())) 			groups.add(g);
//				else if (g.getName().equals(KbeeGlobalRole.CABINET_KNOWLEDGE_BASE.getId())) 	groups.add(g);
//				else if (g.getName().equals(KbeeGlobalRole.CABINET_TEMPLATES.getId())) 			groups.add(g);
			}
			
			if (!groups.isEmpty()) {
				role.setGroups(groups);
				logger.debug("Creating Role Support");
				if (trx)
					ServiceLocator.getService(SecurityContentMgmtService.class).update(role, "save");
				else
					ServiceLocator.getService(SecurityContentMgmtService.class).updateNoTrx(role, "save");
				canonical_roles.add(role);
			}
			else {
				logger.error(role.getName() + " Can not add Canonical Group Support");
			}
		}


		// ----------- Role: SECURED ACCESS ---------------------
		//
		if (!is_secured) {
			logger.debug("Creating Role Secured Access");
			KbeeDomainRole role;
			if (trx)	role = (KbeeDomainRole) ServiceLocator.getService(SecurityContentMgmtService.class).createRole(DomainRole.TYPE, domain);
			else		role = (KbeeDomainRole) ServiceLocator.getService(SecurityContentMgmtService.class).createRoleNoTrx(DomainRole.TYPE, domain);
			
			role.setAlias("secured");
			role.setCanonical(true);
			role.setDomain(domain);
 			role.setName("Secured Access");
 								
			List<Group> groups =  new ArrayList<Group>();
//			for (Group g: canonical_groups) {
////				 if (g.getName().equals(KbeeGlobalRole.CABINET_ENTERPRISE.getId())) 			groups.add(g);
////				else if (g.getName().equals(KbeeGlobalRole.CABINET_EXTERNAL.getId())) 			groups.add(g);
////				else if (g.getName().equals(KbeeGlobalRole.CABINET_KNOWLEDGE_BASE.getId())) 	groups.add(g);
////				else if (g.getName().equals(KbeeGlobalRole.CABINET_TEMPLATES.getId())) 			groups.add(g);
//			}

			role.setGroups(groups);
			
			if (trx) ServiceLocator.getService(SecurityContentMgmtService.class).update(role, "save");
			else	 ServiceLocator.getService(SecurityContentMgmtService.class).updateNoTrx(role, "save");
				
			canonical_roles.add(role);
		
		}

		return canonical_roles;
	}
	
	
	
	/**
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Domain createCabinetsIfNotExists(Domain domain)  throws ContentMgmtException, ContentCreationException  {
		
		List<Library> Librarys = getRepository(Library.class).findAll(domain);
		
		boolean is_std =false;
		boolean is_external=false; 
		boolean is_kbase=false; 
		boolean is_templates=false; 
		boolean is_all=false;
		
		
		for (Library ca: Librarys) {
			if 		(ca.getKey().equals(Library.STANDARD))		is_std=true;
			else if (ca.getKey().equals(Library.EXTERNAL))		is_external=true;
			else if (ca.getKey().equals(Library.KBASE))			is_kbase=true;
			else if (ca.getKey().equals(Library.TEMPLATES))		is_templates=true;
			else if (ca.getKey().equals(Library.ALL))			is_all=true;
		}
			
		if (!is_std) {
			KbeeLibrary std_cab = new KbeeLibrary();
			std_cab.setDomain(domain);
			std_cab.setLastModifiedUser(getSessionUser());
			std_cab.setKey(Library.STANDARD);
			std_cab.setDisplayName(getContentDao().findSystemParameterValueByKey("Library_standard", "Enterprise"));
			std_cab.setCriteria(getContentDao().findSystemParameterValueByKey("Library_standard.criteria", "type=[text, idoc];head=true;state=1;-istemplate=true;-isexternal=true;-isknowledgebase=true"));
			std_cab.setState(ObjectState.ENABLED);
			std_cab.setReadOnly(false);
			std_cab.setOrder(0);
			logger.info("Creating Library " + std_cab.getName());
			startlogger.info("Creating Library " + std_cab.getName());
			getContentDao().save((Library) std_cab);
		}

		
		if (!is_external) {
			KbeeLibrary one_cab = new KbeeLibrary();
			one_cab.setDomain(domain);
			one_cab.setLastModifiedUser(getSessionUser());
			one_cab.setDisplayName(getContentDao().findSystemParameterValueByKey("Library_external", EXTERNAL_Library));
			one_cab.setKey(Library.EXTERNAL);
			one_cab.setState(domain.isCabinetExternal()?ObjectState.ENABLED:ObjectState.ARCHIVED);
			one_cab.setCriteria(getContentDao().findSystemParameterValueByKey("Library_external.criteria", "type=[text, idoc];head=true;state=1;isexternal=true"));
			one_cab.setReadOnly(true);
			one_cab.setOrder(1);
			logger.info("Creating Library " + one_cab.getName());
			startlogger.info("Creating Library " + one_cab.getName());
			
			getContentDao().save((Library)  one_cab);
		}
		

		if (!is_templates) {
			KbeeLibrary tem_cab = new KbeeLibrary();
			tem_cab.setDomain(domain);
			tem_cab.setLastModifiedUser(getSessionUser());
			tem_cab.setKey(Library.TEMPLATES);
			tem_cab.setState(domain.isCabinetTemplate()?ObjectState.ENABLED:ObjectState.ARCHIVED);
			tem_cab.setDisplayName(getContentDao().findSystemParameterValueByKey("Library_templates", "Letter Templates"));
			tem_cab.setCriteria(getContentDao().findSystemParameterValueByKey("Library_templates.criteria","type=[text, idoc];head=true;state=1;istemplate=true;"));
			tem_cab.setReadOnly(false);
			tem_cab.setOrder(2);
			logger.info("Creating Library " + tem_cab.getName());
			startlogger.info("Creating Library " + tem_cab.getName());
			getContentDao().save((Library)  tem_cab);
		}


		if (!is_kbase) {
			KbeeLibrary kba_cab = new KbeeLibrary();
			kba_cab.setDomain(domain);
			kba_cab.setLastModifiedUser(getSessionUser());
			kba_cab.setKey(Library.KBASE);
			kba_cab.setDisplayName(getContentDao().findSystemParameterValueByKey("Library_kbase", "Knowledge Base"));
			kba_cab.setCriteria(getContentDao().findSystemParameterValueByKey("Library_kbase.criteria", "type=[text, idoc];head=true;state=1;isknowledgebase=true")); 
			kba_cab.setReadOnly(false);
			kba_cab.setState(domain.isCabinetKnowledgeBase()?ObjectState.ENABLED:ObjectState.ARCHIVED);
			kba_cab.setOrder(3);
			logger.info("Creating Library " + kba_cab.getName());
			startlogger.info("Creating Library " + kba_cab.getName());
			getContentDao().save((Library)  kba_cab);
		}
				
		 
		if (!is_all) {
			boolean is_all_enabled = domain.isCabinetKnowledgeBase() || domain.isCabinetTemplate() || domain.isCabinetExternal();
			KbeeLibrary all_cab = new KbeeLibrary();
			all_cab.setDomain(domain);
			all_cab.setLastModifiedUser(getSessionUser());
			all_cab.setKey(Library.ALL);
			all_cab.setState(is_all_enabled ?ObjectState.ENABLED:ObjectState.ARCHIVED);
			all_cab.setDisplayName(getContentDao().findSystemParameterValueByKey("Library_all", "All"));
			all_cab.setCriteria(getContentDao().findSystemParameterValueByKey("Library_all.criteria", "type=[text, idoc];head=true;state=1;"));
			all_cab.setReadOnly(false);
			all_cab.setOrder(10);
			
			logger.info("Creating Library " + all_cab.getName());
			startlogger.info("Creating Library " + all_cab.getName());
			getContentDao().save((Library)  all_cab);
		}
		
		return domain;
	}

	
	/**
	 * 
	 * 
	 * This is the first step to create a Domain. 
	 * It creates a new Domain with the base infrastructure needed to be operational.
	 * 
	 * 
	 * name
	 * organization
	 * type
	 * 
	 * root email
	 * root password
	 * 
	 * lang -> el del user que lo crea
	 */
	
	@Deprecated
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Domain createDomain( String name, 
								String organization, 
								DomainType type,
								boolean isAPI, 
								String root_password, 
								String root_email, 
								String noreply_email,
								String admin_username,
								String admin_fisrtname,
								String admin_lastname,
								String admin_email) throws ContentMgmtException, ContentCreationException {

		Domain newdomain = new KbeeDomain();

		newdomain.setEncryptFiles(true);
		newdomain.setName(name);
		newdomain.setDomainType(type);
		
		// where to store binary objects 
		// the main purpose of this field was
		// to allow beta testing during the transition to KBFS2 and distributed KBFS2
		//
		KBFSStorageType defaultStorageType = KBFSStorageType.getByKey(getContentDao().findSystemParameterValueByKey("kbfs.storage.default", ServiceLocator.getService(SystemPropertiesService.class).getDefaultKBFSService()));
		newdomain.setStorageType(defaultStorageType);

		
		// API enabled
		//
		newdomain.setCabinetExternal(isAPI);
		newdomain.setAPIEnabled(isAPI);
		

		// Only Enterprise has Templates & KBase enabled
		//
		newdomain.setCabinetTemplate(false);
		newdomain.setCabinetKnowledgeBase(false);

		newdomain.setCreationOffsetDateTime(OffsetDateTime.now());
		newdomain.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		newdomain.setOrganization(organization);
		newdomain.setPasswordRenewMonths(NO_RENEW);
		newdomain.setState(ObjectState.ENABLED);
		newdomain.setTemplate(false);
		getContentDao().save(newdomain);
		logger.info(new DomainCreateEvent(newdomain));

		// Default Values
		//
		if (type==DomainType.EXPRESS) {
			String s= getContentDao().findSystemParameterValueByKey("default.hard.disk", String.valueOf(DEFAULT_HARD_DISK)); 
			int defaulthd = DEFAULT_HARD_DISK;
			try {
				Integer n=Integer.valueOf(s);
				defaulthd=n.intValue();
			} catch (Exception e) {
				logger.error(e.getClass().getName(), e);
			}
			newdomain.setQuota(defaulthd);
			
			String su= getContentDao().findSystemParameterValueByKey("default.hard.users", String.valueOf(DEFAULT_USERS));
			int defaultusers = DEFAULT_USERS;
			try {
				Integer n=Integer.valueOf(su);
				defaultusers=n.intValue();
			} catch (Exception e) {
				
			}
			newdomain.setMaxUsers(defaultusers);
		}
		else {
			newdomain.setQuota(	UNLIMITED_HD);
			newdomain.setMaxUsers(UNLIMITED_USERS);
		}
		
		Json json;
		json = new KbeeJson();
		json.put(DomainSettingsService.CONSOLES_PERSISTS_LABELS, "no");
		json.put(DomainSettingsService.TIP_OF_THE_DAY, "no");
		json.put(DomainSettingsService.EMAIL_SERVICE_STATUS, "enabled");
		json.put(DomainSettingsService.EMAIL_SERVICE_NO_REPLY, noreply_email==null?EMAIL_NOREPLY:noreply_email);
		
		setDefaultSettings(newdomain, json);
		
		DomainSettingsBuilderService domain_builder = new DomainSettingsBuilderService(newdomain);
		domain_builder.build(new HashMap<String, Object>());
		
		txlogger.info(new DomainCreateEvent(newdomain, "create"));
		return newdomain;
	}
	

	/**---
	 * BASIC
	 * 
	 * Basic Domain Template, includes the model for the API
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void setUpBasicDomainTemplate(Domain domain, boolean createmembers) throws ContentMgmtException {
		setUpDomainTemplate(domain, createmembers, false, false, false);
	}

	/**---
	 * 
	 * ENTERPRISE
	 * Enterprise, includes the model for the API
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void setUpEnterpriseDomainTemplate(Domain domain) throws ContentMgmtException {
		setUpDomainTemplate(domain, true, true, false, false); 
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void setUpAssignDomainTemplate(Domain domain) throws ContentMgmtException {
		setUpDomainTemplate(domain, true, true, false, true); 
	}
	
	/**---
	 * Compliance, includes the model for the API
	 */
	@Override
	public void setUpComplianceDomainTemplate(Domain domain) throws ContentMgmtException {
		setUpDomainTemplate(domain, true, false, true, false);
	}

	/** 
	 * 
	 *  Default Domain Template for RPDD Basic 
	 *  
	 *  Labels
	 *  DataSets
	 *  Classifiers
	 *  Attributes
	 *  
	 *  ContentTemplate: File + Launcher Assign
	 *  
	 *  
	 *  
	 *  MUST exist before the Trx that executes with this method:
	 *  
	 *  Canonical Groups
	 *  Canonical Roles 
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void setUpDomainTemplate(Domain domain,        boolean createmembers, 
													final boolean isStandard, 
													final boolean isCompliance, 
													final boolean isAssign) throws ContentMgmtException {
		

		// Effective Date --------------------------------------------------------
		//
		KbeeAttribute at_effective_date = new KbeeAttribute();
		at_effective_date.setType(AttributeType.DATE);
		at_effective_date.setMultiplicity(Multiplicity.M01);
		at_effective_date.setName(getContentDao().findSystemParameterValueByKey("attribute_date.name", "Effective Date"));
		at_effective_date.setUniqueName("attr01");  // must be on of the solr predefined (schema.xml) attr01 - attr15
		at_effective_date.setDomain(domain);
		at_effective_date.setMetadataSubtitle(false);
		at_effective_date.setLastModifiedUser(getSessionUser());
		getContentDao().save(at_effective_date);
		
		
		// DataSet: Type ---------------------------------------------------------
		//				
		KbeeValueSet d_type = new KbeeValueSet();
		d_type.setDomain(domain);
		d_type.setCanonical(true);
		d_type.setReadonly(getContentDao().findSystemParameterValueByKey("dataset_type.readonly", "no").toLowerCase().trim().equals("yes"));
		d_type.setName(getContentDao().findSystemParameterValueByKey("dataset_type.name", "Document Type"));
		d_type.setAlias(makeAlias(d_type.getName()));
		d_type.setLastModifiedUser(getSessionUser());
		getContentDao().save(d_type);
		txlogger.info(new ModelCreateEvent(d_type, "create"));

		if (createmembers) {
			String vals=getContentDao().findSystemParameterValueByKey("dataset_type.values", DEFAULT_TYPES);
			String va[] = vals.split(";");
			for (String str: va ){
				addDataSetMember(d_type, str);
			}
		}
		
		// Classifier: Type ---------------------------------------------------------
		//
		KbeeClassifier c_type = new KbeeClassifier();
		c_type.setDomain(domain);
		c_type.setContentType(true);
		c_type.setName(d_type.getName());
		c_type.setPredicate("Type");
		c_type.setAPIClassifier(true);
		c_type.setMultiplicity(Multiplicity.M1N);
		c_type.setUniqueName("type");
		c_type.setRuleCondition(true);
		c_type.addDataSet(d_type);
		c_type.setMetadataSubtitle(true);
		c_type.setLastModifiedUser(getSessionUser());
		getContentDao().save(c_type);
		txlogger.info(new ModelCreateEvent(c_type, "create"));

		
		// DataSet: Status ---------------------------------------------------------
		//
		KbeeValueSet d_status = new KbeeValueSet();
		d_status.setDomain(domain);
		d_status.setCanonical(true);
		d_status.setReadonly(getContentDao().findSystemParameterValueByKey("dataset_status.readonly", "no").toLowerCase().trim().equals("yes"));
		d_status.setName(getContentDao().findSystemParameterValueByKey("dataset_status.name", "Status"));
		d_status.setLastModifiedUser(getSessionUser());
		d_status.setAlias(makeAlias(d_status.getName()));
		getContentDao().save(d_status);
		txlogger.info(new ModelCreateEvent(d_status, "create"));
		
		if (createmembers) {
			String vals=getContentDao().findSystemParameterValueByKey("dataset_status.values", DEFAULT_STATUS);
			String vs[] = vals.split(";");
			for (String str: vs )
				addDataSetMember(d_status, str);
		}
		
		// Classifier: Status
		//
		KbeeClassifier c_status = new KbeeClassifier();
		c_status.setDomain(domain);
		c_status.setName(d_status.getName());
		c_status.setAPIClassifier(true);
		c_status.setUniqueName("status");
		c_status.setPredicate("Status");
		c_status.setRuleCondition(false);
		c_status.setContentType(false);
		c_status.setMetadataSubtitle(false);
		c_status.setMultiplicity(Multiplicity.M11);
		c_status.addDataSet(d_status);
		c_status.setLastModifiedUser(getSessionUser());
		getContentDao().save(c_status);

		txlogger.info(new ModelCreateEvent(c_status, "create"));
		

		// DataSet: Secured Accesss ---------------------------------------------------------
		//
		
		KbeeEntitySet d_secured_access = new  KbeeEntitySet();
		d_secured_access.setDomain(domain);
		d_secured_access.setCanonical(true);

		d_secured_access.setReadonly(getContentDao().findSystemParameterValueByKey("dataset_secured_access.readonly", "no").toLowerCase().trim().equals("yes"));
		d_secured_access.setName(getContentDao().findSystemParameterValueByKey("dataset_secured_access.name", "Secured Access"));
		
		d_secured_access.setAlias(makeAlias(d_secured_access.getName()));
		d_secured_access.setLastModifiedUser(getSessionUser());
		getContentDao().save(d_secured_access);
		txlogger.info(new ModelCreateEvent(d_secured_access, "create"));
		
		DataSetMember member_secured_public = null; 
		DataSetMember member_secured_private = null;
		DataSetMember dm = null;
		
		if (createmembers) {
			String vals=getContentDao().findSystemParameterValueByKey("dataset_secured_access.values", DEFAULT_SECURED_ACCESS);
			String vs[] = vals.split(";");
			for (String str: vs ) {
				dm = addDataSetMember(d_secured_access, str);
				if (str.toLowerCase().trim().equals("Public"))
					member_secured_public=dm;
				if (str.toLowerCase().trim().equals("Secured"))
					member_secured_private=dm;
			}
		}
		
		// Classifier: 
		//
		KbeeClassifier c_secured_access = new KbeeClassifier();
		c_secured_access.setDomain(domain);
		c_secured_access.setName(d_secured_access.getName());
		c_secured_access.setAPIClassifier(true);

		c_secured_access.setUniqueName("secureaccess");
		c_secured_access.setPredicate(getContentDao().findSystemParameterValueByKey("dataset_secured_access.predicate", "securedaccess").toLowerCase().trim());
		c_secured_access.setRuleCondition(false);
		c_secured_access.setContentType(false);
		c_secured_access.setMetadataSubtitle(false);
		c_secured_access.setRuleCondition(true);
		c_secured_access.setMultiplicity(Multiplicity.M11);
		c_secured_access.addDataSet(d_secured_access);
		c_secured_access.setLastModifiedUser(getSessionUser());
		getContentDao().save(c_secured_access);
		txlogger.info(new ModelCreateEvent(c_secured_access, "create"));
		
		
		// DataSet: Property o Site Name  ---------------------------------------------------------
		//
		KbeeEntitySet d_property = new KbeeEntitySet();
		d_property.setDomain(domain);
		d_property.setCanonical(true);
		d_property.setReadonly(getContentDao().findSystemParameterValueByKey("dataset_property.readonly","no").toLowerCase().trim().equals("yes"));
		d_property.setName(getContentDao().findSystemParameterValueByKey("dataset_property.name", "Site Name"));
		d_property.setAlias("sitename");
		d_property.setLastModifiedUser(getSessionUser());
		getContentDao().save(d_property);
		txlogger.info(new ModelCreateEvent(d_property, "create"));

		if (createmembers) {
			String vals=getContentDao().findSystemParameterValueByKey("dataset_property.values", "");
			String vs[] = vals.split(";");
			for (String str: vs )
				addDataSetMember(d_property, str);
		}

		// Classifier: Property
		//
		KbeeClassifier c_property = new KbeeClassifier();
		c_property.setDomain(domain);
		c_property.setName(d_property.getName());
		c_property.setAPIClassifier(true);
		c_property.setUniqueName("property"); // tiene que ser consistente con el esquema solr fijo en schema.xml
		c_property.setPredicate("SiteName");
		c_property.setMultiplicity(Multiplicity.M0N);
		c_property.setContentType(false);
		c_property.setMetadataSubtitle(false);
		c_property.setRuleCondition(true);
		c_property.addDataSet(d_property);
		c_property.setLastModifiedUser(getSessionUser());
		getContentDao().save(c_property);
		txlogger.info(new ModelCreateEvent(c_property, "create"));
		

		// DataSet: Label  ---------------------------------------------------------
		//
		KbeeLabelSet d_tag = new KbeeLabelSet();
		d_tag.setDomain(domain);
		d_tag.setCanonical(true);
		d_tag.setReadonly(false);
		d_tag.setName(getContentDao().findSystemParameterValueByKey("dataset_label.name", "Label"));
		d_tag.setLastModifiedUser(getSessionUser());
		d_tag.setAlias(makeAlias(d_tag.getName()));
		getContentDao().save(d_tag);
		txlogger.info(new ModelCreateEvent(d_tag, "create"));
		if (createmembers) {
			String vals=getContentDao().findSystemParameterValueByKey("dataset_label.values", "follow up; duplicate; delete; draft");
			String vs[] = vals.split(";");
			for (String str: vs )
				addDataSetMember(d_tag, str);
		}

		// Classifier: Label
		//
		KbeeClassifier c_tag = new KbeeClassifier();
		c_tag.setDomain(domain);
		c_tag.setName(d_tag.getName());
		c_tag.setAPIClassifier(false);
		c_tag.setUniqueName("tag"); // tiene que ser consistente con el esquema solr fijo en schema.xml
		c_tag.setPredicate("tag");
		c_tag.setMultiplicity(Multiplicity.M0N);
		c_tag.setContentType(false);
		c_tag.setMetadataSubtitle(false);
		c_tag.setRuleCondition(false);
		c_tag.addDataSet(d_tag);
		c_tag.setLastModifiedUser(getSessionUser());
		getContentDao().save(c_tag);
		txlogger.info(new ModelCreateEvent(c_tag, "create"));

		
		// DataSet: Department ---------------------------------------------------------
		//
		KbeeEntitySet d_department = new KbeeEntitySet();
		d_department.setDomain(domain);
		d_department.setAlias("department");
		d_department.setCanonical(true);
		d_department.setReadonly(getContentDao().findSystemParameterValueByKey("dataset_department.readonly", "no").toLowerCase().trim().equals("yes"));
		d_department.setName(getContentDao().findSystemParameterValueByKey("dataset_department.name", "Department"));
		d_department.setLastModifiedUser(getSessionUser());
		getContentDao().save(d_department);
		txlogger.info(new ModelCreateEvent(d_department, "create"));

		if (createmembers) {
			String vals=getContentDao().findSystemParameterValueByKey("dataset_department.values", DEFAULT_DEPARTMENT);
			String vs[] = vals.split(";");
			for (String str: vs )
				addDataSetMember(d_department, str);
		}
		
		// Classifier: Department
		//
		//
		KbeeClassifier c_department = new KbeeClassifier();
		c_department.setDomain(domain);
		c_department.setAPIClassifier(true);
		c_department.setName(d_department.getName());
		c_department.setUniqueName("department");
		c_department.setPredicate("Department");
		c_department.setMultiplicity(Multiplicity.M0N);
		c_department.addDataSet(d_department);
		c_department.setContentType(false);
		c_department.setMetadataSubtitle(false);
		c_department.setLastModifiedUser(getSessionUser());
		c_department.setRuleCondition(false);
		getContentDao().save(c_department);
		txlogger.info(new ModelCreateEvent(c_department, "create"));
						

		// ContentTemplate: File ---------------------------------------------------------------
		//
		// type, property, status, tag, department
		//
		ClassifierTemplate ctf_type   		= new KbeeClassifierTemplate(c_type, 0);					ctf_type.setMetadataSubtitle(true);
		ClassifierTemplate ctf_property 	= new KbeeClassifierTemplate(c_property, 1); 				ctf_property.setMetadataSubtitle(true);
		ClassifierTemplate ctf_status 		= new KbeeClassifierTemplate(c_status, 2);					ctf_status.setMetadataSubtitle(false);
		ClassifierTemplate ctf_tag 			= new KbeeClassifierTemplate(c_tag, 3);						ctf_tag.setMetadataSubtitle(false);
		ClassifierTemplate ctf_department 	= new KbeeClassifierTemplate(c_department, 4);				ctf_department.setMetadataSubtitle(false);
		ClassifierTemplate ctf_secured_access = new KbeeClassifierTemplate(c_secured_access, 5);		ctf_secured_access.setMetadataSubtitle(false);
												
		
		KbeeAcl facl = new KbeeAcl(); 
		facl.setCreationOffsetDateTime(OffsetDateTime.now());
		facl.setLastModifiedUser(getSessionUser());
		facl.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		List<Group> fgroups = null;
		fgroups = ServiceLocator.getService(SecurityContentMgmtService.class).getDefaultGroups(domain);
		Group fusers = null;
		for (Group group: fgroups) {
			if (group.getName().equals(KbeeGlobalRole.USER.getId())) {
				fusers = group;
				break;
			}
		}
		
		AclEntry fentry = new KbeeAclEntry(facl, fusers, false);
		List<Permission> fpermissions = new ArrayList<Permission>();
		fpermissions.add(KbeePermission.CREATE);
		fentry.setPermissions(fpermissions);
		facl.addEntry(getSessionUser(), fentry);
		getContentDao().save(facl);
		
		// ----------------
		// File
		//
		ContentTemplate content_template_file = new KbeeContentTemplate();
		content_template_file.setName(getContentDao().findSystemParameterValueByKey("content_class.file.name", "File"));
		content_template_file.setContentClass(getContentDao().findContentClassByName("IDoc"));
		content_template_file.setContentClassCode("FILE");
		content_template_file.setPrivateNotes(domain.getDomainType()!=DomainType.EXPRESS);
		content_template_file.setCustomAttributes(getContentDao().findSystemParameterValueByKey("content_class.file.custom-attributes", "no").toLowerCase().trim().equals("yes"));
				
		content_template_file.addClassifier(ctf_type);
		content_template_file.addClassifier(ctf_status);
		content_template_file.addClassifier(ctf_property);
		content_template_file.addClassifier(ctf_department);
		content_template_file.addClassifier(ctf_tag);
		content_template_file.addClassifier(ctf_secured_access);

		content_template_file.setResources(true);
		content_template_file.setResourcesLabel("Resources");
		
		KbeeAttributeTemplate ftemplate_date = new com.novamens.kbee.content.model.KbeeAttributeTemplate();
		ftemplate_date.setAttribute(at_effective_date);
		
		List<AttributeTemplate> fattributes = new ArrayList<AttributeTemplate>();
		fattributes.add(ftemplate_date);
		content_template_file.setAttributes(fattributes);
		
		content_template_file.setDomain(domain);
		content_template_file.setLastModifiedUser(getSessionUser());
		content_template_file.setInstantiable(true);
		content_template_file.setMultimedia(false);
		content_template_file.setTemplate(false);
		((KbeeContentTemplate) content_template_file).setAcl(facl);
		content_template_file.setAbstract(true);
		content_template_file.setLinkResources(true);
		content_template_file.setDocument(true);
		getContentDao().save(content_template_file);
		txlogger.info(new ModelCreateEvent(content_template_file, "create"));
																	
			// File: Launcher for procedure Assign / Standard / Compliance ---------------------------------------------
			//
			if (fusers!=null) {
					
					List<Procedure> procs = domain.getService(WorkflowDomainService.class).getProceduresLibrary();

					List<ProcessLauncher> launchers = new ArrayList<ProcessLauncher>();
					
					for (Procedure pr: procs) {
						if (pr.getName()!=null && pr.getName().toLowerCase().equals("assign")) {
							KbeeProcessLauncher launcher = new KbeeProcessLauncher();
							launcher.setDomain(domain);
							launcher.setLabel(content_template_file.getName());
							launcher.setAcl(facl);
							launcher.setContentTemplate(content_template_file);
							launcher.setLibrary(true);
							launcher.setEnabled(true);
							launcher.setProcedure(pr);
							launchers.add(launcher);
							logger.info("Setting Assign launcher");
						}
					}
					
					if (isStandard) {
						KbeeProcessLauncher launcher_standard = new KbeeProcessLauncher();
						launcher_standard.setDomain(domain);
						launcher_standard.setLabel(content_template_file.getName() + " Standard");
						launcher_standard.setAcl(facl);
						launcher_standard.setContentTemplate(content_template_file);
						launcher_standard.setLibrary(true);
						launcher_standard.setEnabled(true);
						for (Procedure pr: procs) {
							if (pr.getName()!=null && pr.getName().toLowerCase().equals("standard")) {
								launcher_standard.setProcedure(pr);
								launchers.add(launcher_standard);
								logger.info("Setting Standard launcher");
								break;
							}
						}
					}
					
					if (isCompliance) {
						KbeeProcessLauncher launcher_compliance = new KbeeProcessLauncher();
						 launcher_compliance.setDomain(domain);
						 launcher_compliance.setLabel(content_template_file.getName() + " Compliance");
						 launcher_compliance.setAcl(facl);
						 launcher_compliance.setContentTemplate(content_template_file);
						 launcher_compliance.setLibrary(true);
						 launcher_compliance.setEnabled(true);
						
						for (Procedure pr: procs) {
							if (pr.getName()!=null && pr.getName().toLowerCase().startsWith("compliance")) {
								 launcher_compliance.setProcedure(pr);
								launchers.add( launcher_compliance);
								logger.info("Setting Compliance launcher");
								break;
							}
						}
					}
					
//					if (!launchers.isEmpty())
//						content_template_file.setProcessLaunchers(launchers);
//					
					getContentDao().save(content_template_file);
					txlogger.info(new ModelUpdateEvent(content_template_file, "launchers"));
					
				}
				else
					logger.error("Group Users does not exists");
				
				if (!domain.isAPIEnabled())
					return;
				
				
				// ---------
				// API Users 
				// ---------
				// Tibco
				// OneSite
				// SSO
				//
				List<Group> appgroups= getContentSecurityDao().getCanonicalGroups(domain);
				List<Role> canonical_roles = getContentSecurityDao().getCanonicalRoles(domain);
		 		List<UserSet> list_userset = getContentDao().getUserSets(domain);
		 		
				UserSet kbee_userset = null;
				
				if (list_userset!=null && !list_userset.isEmpty()) {
					for (UserSet us: list_userset) {
						 if (us instanceof KbeeUserSet) {
							 kbee_userset = us;
							 break;
						 }
					}

					String application = getContentDao().findSystemParameterValueByKey("external_application.name", "onesitedm"); 
							
					if (kbee_userset!=null) {
						User user = ServiceLocator.getService(com.novamens.service.SecurityService.class).findUserByUsername(application + "@" + domain.getName());
						String pwd = null;
						String email = null;
						String f_name = null;
						String l_name = null;
						if (user==null) {
								pwd = getContentDao().findSystemParameterValueByKey("onesitedm.password", "1Aqqqqqq");
								email = getContentDao().findSystemParameterValueByKey("onesitedm.email", "onesite-noreply@realpage.com");
								f_name = getContentDao().findSystemParameterValueByKey("onesitedm.firstname", null);
								l_name = getContentDao().findSystemParameterValueByKey("onesitedm.lastname", "OneSite");
								
								logger.info("Adding Application User " + application + "@"+domain.getName());
								createApplicationUser(application, kbee_userset, f_name, l_name, appgroups, canonical_roles, domain,  pwd, email);
						}
						
						String sso = getContentDao().findSystemParameterValueByKey("sso.username", "sso");
						user = ServiceLocator.getService(com.novamens.service.SecurityService.class).findUserByUsername( sso + "@"+domain.getName());
						
						if (user==null) {
							pwd = getContentDao().findSystemParameterValueByKey("sso.password", "1Aqqqqqq");
						 	email = getContentDao().findSystemParameterValueByKey("sso.email", "sso-noreply@realpage.com");
							f_name = getContentDao().findSystemParameterValueByKey("sso.firstname", null);
							l_name = getContentDao().findSystemParameterValueByKey("sso.lastname", "SSO");

							logger.info("Adding SSO User " + sso + "@"+domain.getName());
							createApplicationUser(sso, kbee_userset, f_name, l_name, appgroups, canonical_roles, domain,  pwd, email);
						}
							
						String tibco = getContentDao().findSystemParameterValueByKey("tibco.username", "tibco");
						user = ServiceLocator.getService(com.novamens.service.SecurityService.class).findUserByUsername( tibco + "@"+domain.getName());
						
						if (user==null) {
								pwd = getContentDao().findSystemParameterValueByKey("tibco.password", "1Aqqqqqq");
							 	email = getContentDao().findSystemParameterValueByKey("tibco.email", "tibco-noreply@realpage.com");
								f_name = getContentDao().findSystemParameterValueByKey("tibco.firstname", null);
								l_name = getContentDao().findSystemParameterValueByKey("tibco.lastname", "Tibco");

								logger.info("Adding Tibco User " + tibco + "@"+domain.getName());
								createApplicationUser(tibco, kbee_userset, f_name, l_name, appgroups, canonical_roles, domain,  pwd, email);
						}
						
						
						
								
						String us1 = getContentDao().findSystemParameterValueByKey("admin1.username", null);
						if (us1!=null) {
							User user_us1 = ServiceLocator.getService(com.novamens.service.SecurityService.class).findUserByUsername( us1  + "@"+domain.getName());
							if (user_us1==null && us1.length()>0) {
									pwd = getContentDao().findSystemParameterValueByKey("admin1.password", "1Aqqqqqq");
								 	email = getContentDao().findSystemParameterValueByKey("admin1.email", "noreply@realpage.com");
									f_name = getContentDao().findSystemParameterValueByKey("admin1.firstname", null);
									l_name = getContentDao().findSystemParameterValueByKey("admin1.lastname", us1);

									logger.info("Adding User " + user_us1 + "@"+domain.getName());
									createApplicationUser(us1, kbee_userset, f_name, l_name, appgroups, canonical_roles, domain,  pwd, email);
							}
						}
						

						String us2 = getContentDao().findSystemParameterValueByKey("admin2.username", null);
						if (us2!=null && us2.length()>0) {
							User user_us2 = ServiceLocator.getService(com.novamens.service.SecurityService.class).findUserByUsername( us2  + "@"+domain.getName());
							if (user_us2==null) {
									pwd = getContentDao().findSystemParameterValueByKey("admin2.password", "1Aqqqqqq");
								 	email = getContentDao().findSystemParameterValueByKey("admin2.email", "noreply@realpage.com");
									f_name = getContentDao().findSystemParameterValueByKey("admin2.firstname", null);
									l_name = getContentDao().findSystemParameterValueByKey("admin2.lastname", us1);

									logger.info("Adding User " + user_us2 + "@"+domain.getName());
									createApplicationUser(us2, kbee_userset, f_name, l_name, appgroups, canonical_roles, domain,  pwd, email);
							}
						}


						String us3 = getContentDao().findSystemParameterValueByKey("admin3.username", null);
						if (us3!=null && us3.length()>0) {
							User user_us3 = ServiceLocator.getService(com.novamens.service.SecurityService.class).findUserByUsername( us3  + "@"+domain.getName());
							if (user_us3==null) {
									pwd = getContentDao().findSystemParameterValueByKey("admin3.password", "1Aqqqqqq");
								 	email = getContentDao().findSystemParameterValueByKey("admin3.email", "noreply@realpage.com");
									f_name = getContentDao().findSystemParameterValueByKey("admin3.firstname", null);
									l_name = getContentDao().findSystemParameterValueByKey("admin3.lastname", us1);

									logger.info("Adding User " + user_us3 + "@"+domain.getName());
									createApplicationUser(us3, kbee_userset, f_name, l_name, appgroups,canonical_roles, domain,  pwd, email);
							}
						}


						String us4 = getContentDao().findSystemParameterValueByKey("admin4.username", null);
						if (us4!=null && us4.length()>0) {
							User user_us4 = ServiceLocator.getService(com.novamens.service.SecurityService.class).findUserByUsername( us4  + "@"+domain.getName());
							if (user_us4==null) {
									pwd = getContentDao().findSystemParameterValueByKey("admin4.password", "1Aqqqqqq");
								 	email = getContentDao().findSystemParameterValueByKey("admin4.email", "noreply@realpage.com");
									f_name = getContentDao().findSystemParameterValueByKey("admin4.firstname", null);
									l_name = getContentDao().findSystemParameterValueByKey("admin4.lastname", us1);

									logger.info("Adding User " + user_us4 + "@"+domain.getName());
									createApplicationUser(us4, kbee_userset, f_name, l_name, appgroups, canonical_roles, domain,  pwd, email);
							}
						}
						
									
						String us5 = getContentDao().findSystemParameterValueByKey("admin5.username", null);
						if (us5!=null && us5.length()>0) {
							User user_us5 = ServiceLocator.getService(com.novamens.service.SecurityService.class).findUserByUsername( us5  + "@"+domain.getName());
							if (user_us5==null) {
									pwd = getContentDao().findSystemParameterValueByKey("admin5.password", "1Aqqqqqq");
								 	email = getContentDao().findSystemParameterValueByKey("admin5.email", "noreply@realpage.com");
									f_name = getContentDao().findSystemParameterValueByKey("admin5.firstname", null);
									l_name = getContentDao().findSystemParameterValueByKey("admin5.lastname", us1);

									logger.info("Adding User " + user_us5 + "@"+domain.getName());
									createApplicationUser(us5, kbee_userset, f_name, l_name, appgroups, canonical_roles, domain,  pwd, email);
							}
						}
						
					}
					else {
							logger.error("No KbeeUserSet found for Domain @" + domain.getName());
					}
					
			 	} else {
					logger.error("No List of UserSet found for Domain @" + domain.getName());
			 	}

				// DataSet: dataset_1 to 10 ---------------------------------------------------------
				//
				/*	
			    Secured Access
			    Site Id
			    Library
			    Pmc Id
			    Document Entity
			    Packet Type
				*/
				
				KbeeValueSet ds;
				KbeeClassifier cs;
				List<Classifier> system_properties_classifiers = new ArrayList<Classifier>();
				
				for (int cn=0;cn<10;cn++) {
							String dn = "dataset" +String.valueOf(cn);
							String dname=getContentDao().findSystemParameterValueByKey(dn, "null");
							if (!dname.equals("null")) {
								
								ds = new KbeeValueSet();
								ds.setDomain(domain);
								ds.setCanonical(true);
								ds.setReadonly(getContentDao().findSystemParameterValueByKey(dn+".readonly","yes").trim().toLowerCase().equals("yes"));
								ds.setName(dname);
								ds.setAlias(makeAlias(ds.getName()));
								ds.setLastModifiedUser(getSessionUser());
								ds.setCreationOffsetDateTime(OffsetDateTime.now());
								getContentDao().save(ds);
								txlogger.info(new ModelCreateEvent(ds, "create"));
							
								// add DataSet Members
								//
								String vals=getContentDao().findSystemParameterValueByKey("dataset_"+dn+".values", "");
								String vs[] = vals.split(";");
								for (String str: vs ) {
									if (str!=null && str.length()>0)
										addDataSetMember(ds, str);
								}
								
								cs = new KbeeClassifier();
								cs.setAPIClassifier(true);
								cs.setDomain(domain);
								cs.setName(dname);
								cs.setDefaultGridColumn(false);
								cs.setSemantic(false);
								cs.setRuleCondition(false);
								cs.setDisplayable(true);
								cs.setMetadataSubtitle(false);
								cs.setUniqueName("clsf"+ (cn<10?"0":"")+String.valueOf(cn));
								cs.setPredicate(dname.replace(" ", "").toLowerCase().trim());
								cs.setMultiplicity(Multiplicity.M0N);
								cs.addDataSet(ds);
								cs.setContentType(false);
								cs.setLastModifiedUser(getSessionUser());
								getContentDao().save(cs);
								txlogger.info(new ModelCreateEvent(cs, "create"));
								system_properties_classifiers.add(cs);
							}
				}



				// ONESITE FILE ----------------------------------------------------------------------------------------
				//
				//
				ClassifierTemplate ct_type   = new KbeeClassifierTemplate(c_type, 0);					ct_type.setMetadataSubtitle(true);
				ClassifierTemplate ct_status = new KbeeClassifierTemplate(c_status, 1);					ct_status.setMetadataSubtitle(false);
				ClassifierTemplate ct_property = new KbeeClassifierTemplate(c_property, 2);				ct_property.setMetadataSubtitle(true);
				ClassifierTemplate ct_department = new KbeeClassifierTemplate(c_department, 3);			ct_department.setMetadataSubtitle(false);
				ClassifierTemplate ct_secured_access = new KbeeClassifierTemplate(c_secured_access,4);	ct_secured_access.setMetadataSubtitle(false);
				
				int n=5;
				List<ClassifierTemplate> lct= new ArrayList<ClassifierTemplate>();
				for (Classifier c: system_properties_classifiers) 
					lct.add(new KbeeClassifierTemplate(c, n++));

				KbeeAcl acl = new KbeeAcl(); 
				acl.setCreationOffsetDateTime(OffsetDateTime.now());
				acl.setLastModifiedUser(getSessionUser());
				acl.setLastModifiedOffsetDateTime(OffsetDateTime.now());
				List<Group> groups = null;
				groups = ServiceLocator.getService(SecurityContentMgmtService.class).getDefaultGroups(domain);
				
				Group users = null;
				for (Group group: groups) {
								if (group.getName().equals(KbeeGlobalRole.USER.getId())) {
								users = group;
							break;
					}
				}

				AclEntry entry = new KbeeAclEntry(acl, users, false);
				List<Permission> permissions = new ArrayList<Permission>();
				permissions.add(KbeePermission.CREATE);
				entry.setPermissions(permissions);
				acl.addEntry(getSessionUser(), entry);
				getContentDao().save(acl);
								
				ContentTemplate content_template_onesite_file = new KbeeContentTemplate();
				content_template_onesite_file.setName(getContentDao().findSystemParameterValueByKey("content_class.api.file.name", "OneSite File"));
				content_template_onesite_file.setContentClass(getContentDao().findContentClassByName("IDoc"));
				content_template_onesite_file.setContentClassCode("OSFILE");

				content_template_onesite_file.setResources(true);
				content_template_onesite_file.setResourcesLabel("Resources");

				
				content_template_onesite_file.setPrivateNotes(false);
				content_template_onesite_file.setCustomAttributes(true);
				
				content_template_onesite_file.addClassifier(ct_type);
				content_template_onesite_file.addClassifier(ct_status);
				content_template_onesite_file.addClassifier(ct_property);
				content_template_onesite_file.addClassifier(ct_department);
				content_template_onesite_file.addClassifier(ct_secured_access);

				for (ClassifierTemplate cter: lct) 
					content_template_onesite_file.addClassifier(cter);

				KbeeAttributeTemplate template_date = new com.novamens.kbee.content.model.KbeeAttributeTemplate();
				template_date.setAttribute(at_effective_date);

				
				// Create Date ---------------------------------------------------------------
				//
				//
				KbeeAttribute at_create_date = new KbeeAttribute();
				at_create_date.setType(AttributeType.DATE);
				at_create_date.setMultiplicity(Multiplicity.M01);
				at_create_date.setName(getContentDao().findSystemParameterValueByKey("attribute_create_date.name", "Create Date"));
				at_create_date.setUniqueName("attr02");  // must be on of the solr predefined (schema.xml) attr01 - attr15
				at_create_date.setDomain(domain);
				at_create_date.setLastModifiedUser(getSessionUser());
				at_create_date.setAPIClassifier(true);
				at_create_date.setMetadataSubtitle(false);
				getContentDao().save(at_create_date);
				
				KbeeAttributeTemplate template_cdate = new com.novamens.kbee.content.model.KbeeAttributeTemplate();
				template_cdate.setAttribute(at_create_date);
				
						
				// FileID  ---------------------------------------------------------------
				//
				//
				KbeeAttribute at_fileid = new KbeeAttribute();
				at_fileid.setType(AttributeType.DATE);
				at_fileid.setMultiplicity(Multiplicity.M01);
				at_fileid.setName(getContentDao().findSystemParameterValueByKey("attribute_fileid.name", "FileID"));
				at_fileid.setUniqueName("attr03");  // must be on of the solr predefined (schema.xml) attr01 - attr15
				at_fileid.setDomain(domain);
				at_fileid.setLastModifiedUser(getSessionUser());
				at_fileid.setAPIClassifier(true);
				at_fileid.setMetadataSubtitle(false);
				getContentDao().save(at_fileid);
				
				KbeeAttributeTemplate template_fileid = new com.novamens.kbee.content.model.KbeeAttributeTemplate();
				template_fileid.setAttribute(at_fileid);

				
				
				// Add attributes
				//
				//
				List<AttributeTemplate> attributes = new ArrayList<AttributeTemplate>();
				
				attributes.add(template_date);  // Effective Date
				attributes.add(template_cdate); // Create Date
				attributes.add(template_fileid); // File Id
				content_template_onesite_file.setAttributes(attributes);

				content_template_onesite_file.setDomain(domain);
				content_template_onesite_file.setLastModifiedUser(getSessionUser());
				content_template_onesite_file.setInstantiable(true);
				content_template_onesite_file.setMultimedia(false);
				content_template_onesite_file.setAPIContentClass(true);
				content_template_onesite_file.setTemplate(false);
				((KbeeContentTemplate) content_template_onesite_file).setAcl(acl);
				content_template_onesite_file.setAbstract(true);
				content_template_onesite_file.setLinkResources(true);
				content_template_onesite_file.setDocument(true);
				getContentDao().save(content_template_onesite_file);
				txlogger.info(new ModelCreateEvent(content_template_onesite_file, "create"));
				
				
				// ------------------------------------------------------------------------------------------------
				// DOCUSIGN FILE
				//
							
				KbeeAcl docuacl = new KbeeAcl(); 
				docuacl.setCreationOffsetDateTime(OffsetDateTime.now());
				docuacl.setLastModifiedUser(getSessionUser());
				docuacl.setLastModifiedOffsetDateTime(OffsetDateTime.now());
				List<Group> docugroups = null;
				docugroups = ServiceLocator.getService(SecurityContentMgmtService.class).getDefaultGroups(domain);
				Group docuusers = null;
				for (Group group: docugroups) {
					if (group.getName().equals(KbeeGlobalRole.USER.getId())) {
						docuusers = group;
						break;
					}
				}
				AclEntry docuentry = new KbeeAclEntry(docuacl, docuusers, false);
				List<Permission> docupermissions= new ArrayList<Permission>();
				docupermissions.add(KbeePermission.CREATE);
				docuentry.setPermissions(docupermissions);
				docuacl.addEntry(getSessionUser(), docuentry);
				getContentDao().save(docuacl);
				

				ContentTemplate content_template_docu_file = new KbeeContentTemplate();
				content_template_docu_file.setContentClassCode("DOCUSIGN");
				content_template_docu_file.setName(getContentDao().findSystemParameterValueByKey("content_class.api.docusign.name", "DocuSign Certificate"));
				content_template_docu_file.setContentClass(getContentDao().findContentClassByName("IDoc"));
				content_template_docu_file.setDomain(domain);
				content_template_docu_file.setPrivateNotes(false);
				content_template_docu_file.setCustomAttributes(true);
				content_template_docu_file.setDomain(domain);
				content_template_docu_file.setLastModifiedUser(getSessionUser());
				content_template_docu_file.setInstantiable(false);
				content_template_docu_file.setMultimedia(false);
				content_template_docu_file.setTemplate(false);
				content_template_docu_file.setAbstract(false);
				content_template_docu_file.setLinkResources(false);
				content_template_docu_file.setDocument(true);
				
				content_template_docu_file.setResources(true);
				content_template_docu_file.setResourcesLabel("Resources");
				
								
				ClassifierTemplate docu_type   		= new KbeeClassifierTemplate(c_type,0);			docu_type.setMetadataSubtitle(true);
				ClassifierTemplate docu_status  	= new KbeeClassifierTemplate(c_status,1);			docu_status.setMetadataSubtitle(true);
				ClassifierTemplate docu_property  	= new KbeeClassifierTemplate(c_property,2);		docu_property.setMetadataSubtitle(true);
				ClassifierTemplate docu_department  = new KbeeClassifierTemplate(c_department,3);		docu_department.setMetadataSubtitle(false);
				ClassifierTemplate docu_secured_access = new KbeeClassifierTemplate(c_secured_access,4);	docu_secured_access.setMetadataSubtitle(false);

				content_template_docu_file.addClassifier(docu_type);
				content_template_docu_file.addClassifier(docu_status);
				content_template_docu_file.addClassifier(docu_property);
				content_template_docu_file.addClassifier(docu_department);
				content_template_docu_file.addClassifier(docu_secured_access);


				// DataSet: dataset_1 to DocuSign  ---------------------------------------------------------
				//
				/*	
				    Secured Access
				    Site Id
				    Library
				    Pmc Id
				    Document Entity
				    Packet Type
 				*/
				
				n=5;
				List<ClassifierTemplate> docu_lct= new ArrayList<ClassifierTemplate>();
				for (Classifier c: system_properties_classifiers) 
					docu_lct.add(new KbeeClassifierTemplate(c,n++));
				
				for (ClassifierTemplate cter: docu_lct) 
					content_template_docu_file.addClassifier(cter);

	  			
				((KbeeContentTemplate) content_template_docu_file).setAcl(docuacl);

				
				// Add attribute Create Date to DOCUSIGN ---------------------------------------------------------------
				
				List<AttributeTemplate> docu_attributes = new ArrayList<AttributeTemplate>();
				KbeeAttributeTemplate docu_template_date = new com.novamens.kbee.content.model.KbeeAttributeTemplate();
				docu_template_date.setAttribute(at_create_date);
				docu_attributes.add(docu_template_date);
				 
				// Add attribute FileId to DOCUSIGN ---------------------------------------------------------------
				
	 			KbeeAttributeTemplate docu_fileid = new com.novamens.kbee.content.model.KbeeAttributeTemplate();
	 			docu_fileid.setAttribute(at_fileid);
	 			docu_attributes.add(docu_fileid);
	 			content_template_docu_file.setAttributes(docu_attributes);


	 			
				// Relationship from DocuSign -> OneSite File ----------------------------------------------------------
				//
				KbeeRelationTemplate signs = new KbeeRelationTemplate();  
				signs.setName(getContentDao().findSystemParameterValueByKey("content_relationship.sign.name", "signs"));
				signs.setTargetLabel(getContentDao().findSystemParameterValueByKey("content_relationship.sign.targetlabel", "Signed Documents"));
				signs.setReverseLabel(getContentDao().findSystemParameterValueByKey("content_relationship.sign.reverselabel", "Signing Certificate"));
				content_template_docu_file.getRelations().add(signs);
				signs.setTargetTemplate(content_template_onesite_file);
				signs.setMultiplicity(Multiplicity.M0N);
				signs.setState(ObjectState.ENABLED);
				

				getContentDao().save(content_template_docu_file);
				txlogger.info(new ModelCreateEvent(content_template_docu_file, "Create"));

				getContentDao().save(content_template_onesite_file);
				txlogger.info(new ModelUpdateEvent(content_template_onesite_file, "Add Relationship:" + getContentDao().findSystemParameterValueByKey("content_relationship.sign.name", "signs")));
				
				// complete Role Secured Access
				//
				for (Role role: canonical_roles) {
					if (role instanceof KbeeDomainRole && role.getAlias().equals("secured")) {
						if (member_secured_private!=null && c_secured_access!=null) {
							String predicate=c_secured_access.getPredicate();
							String did= member_secured_private.getId().toString();
							String condition = predicate+"("+did+")";
							List<Permission> ps = new ArrayList<Permission>();
							ps.add(KbeePermission.READ);
							 ((KbeeDomainRole) role).setPermissions(ps);
							 ((KbeeDomainRole) role).setCondition(condition);
							ServiceLocator.getService(SecurityContentMgmtService.class).update(role, "condition");
							break;
						}
					}
				}
	}

	
	
	/**
	 * 
	 * 
	 */
	private String makeAlias(String name) {
		if (name == null)
			return null;
		String s=name.toLowerCase().replaceAll("[°,¡!?¿:\\/\"-().\\s]", "")
				.replace("á", "a")
				.replace("é", "e")
				.replace("í", "i")
				.replace("ó", "o")
				.replace("ú", "o")
				.replace("ñ", "n")
				.trim();
		// logger.debug(name + " -> " + s);
		return s;
		
	}


	@Transactional
	@Override
	public Domain setDefaultSettings(Domain domain,	Json json) throws ContentMgmtException, ContentCreationException {
		domain.getService(DomainSettingsService.class).SetValues(json);		
		getContentDao().save(domain);
		txlogger.info(new DomainUpdateEvent(domain, "Settings"));
		return domain;
	}
	

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void saveSystemParameter(SystemParameter pa) throws ContentMgmtException {
	
		if (pa.getKey()!=null)
			pa.setKey(pa.getKey().trim());
		
		if (pa.getValue()!=null)
			pa.setValue(pa.getValue().trim());
		
		getContentDao().save(pa);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteSystemParameter(SystemParameter pa) throws ContentMgmtException {
		getContentDao().delete(pa);
	}

	
	// Spring
	public ContentDao getContentDao() 			{return dao;	}
	public void setContentDao(ContentDao dao) 	{this.dao=dao;	}
	
	/***
	 *  Root
	 *  Workflow
	 *  SSO
	 *  
	 *  Admin
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)																		
	public Domain setDefaultSecurityModel(Domain domain, String root_password, String root_email, String admin_username, String admin_fisrtname, String admin_lastname, String admin_email) throws ContentCreationException, ContentMgmtException {

			
		KbeeUserSet userset = new KbeeUserSet();
		userset.setDomain(domain);
		userset.setName("User");
		userset.setAlias(makeAlias(userset.getName()));
		userset.setDataSetType(DataSetType.USER);
		getContentDao().save(userset);		

		// Canonical Groups -------------------------------------------------------
	 	List<Group> groups = createCanonicalGroups(domain);

		
		// Canonical Roles ------------------------------------------------------- 
		List<Role> canonical_roles = createCanonicalRolesIfNotExists(domain, groups);
		logger.info("Canonical Roles");

		
		// Root 
		//
		createRootUser(userset, groups, canonical_roles, domain, root_password, root_email);
		logger.info("Create Root User with email " + root_email);
		
		
		// Workflow 
		//
		createWorkflowUser(userset, groups, domain);
		logger.info("Create Workflow User");
		

		// Corporate Admin User
		//
		createAdminUser(userset, admin_username, admin_fisrtname, admin_lastname, admin_email, groups, canonical_roles, domain);
		logger.info("Create Corporate Admin User");
		
		
		// Support 1 and 2
		//		
		String email1 = EMAIL_SUPPORT_1;
		String phone1 = "";
		String pwd1   = "supp0rtus3r" + String.valueOf((Double.valueOf(Math.random() * 10000000.0)).intValue()) + email1 + String.valueOf((Double.valueOf(Math.random() * 10000000.0)).intValue());
		
		String email2 = EMAIL_SUPPORT_2;
		String phone2 = "";
		String pwd2   = "supp0rtus3r" + String.valueOf((Double.valueOf(Math.random() * 10000000.0)).intValue()) + email2 + String.valueOf((Double.valueOf(Math.random() * 10000000.0)).intValue());
		
		List<SystemParameter> list = getContentDao().getSystemParameters();
												
		for (SystemParameter pa: list) {						
			if      (pa.getKey().equals("support1.email"))		email1 = pa.getValue();
			else if (pa.getKey().equals("support1.phone"))		phone1 = pa.getValue();
			else if (pa.getKey().equals("support1.pwd"))		pwd1 = pa.getValue();
			else if (pa.getKey().equals("support2.email"))		email2 = pa.getValue();
			else if (pa.getKey().equals("support2.phone"))		phone2 = pa.getValue();
			else if (pa.getKey().equals("support2.pwd"))		pwd2 = pa.getValue();
		}
		
		createSupportUser("support1", email1, phone1, pwd1, userset, groups, canonical_roles, domain);
		createSupportUser("support2", email2, phone2, pwd2, userset, groups, canonical_roles, domain);
		
		logger.info("Create Support users");
		
		createDefaultSecurityRule(groups, domain);
		logger.info("Create Default Domain Rule");
		
		return domain;
	}
	
	/***
	 * @param domain
	 * @param createmembers
	 * @param isStandard
	 * @return
	 * @throws ContentMgmtException
	 */
 	
	
		
	
	private SecurityDao  getSecurityDao() {
		return	(SecurityDao) ServiceLocator.getService(BeansService.class).getBean("securityDao");
	}

	
	private User getRootUser(Domain domain) {
		User rootuser = ((KbeeSecurityDao) getSecurityDao()).findUserByName("root@"+ domain.getName());
		return rootuser;
	}
	
	
	/**
	 * 
	 *  
	 */
	private void createDefaultSecurityRule(List<Group> groups, Domain domain) throws ContentMgmtException {

		String secured_rule_condition=getContentDao().findSystemParameterValueByKey("rule.secured.public.condition", null ); // "not securedaccess(Secured)"
		
		if (secured_rule_condition!=null) {
			
	 		User caller = getSessionUser();
			
			KbeeSecurityRule sec_rule = new KbeeSecurityRule();
			sec_rule.setType(IQLRule.RULE_COLLOQUIAL_IQL);
			sec_rule.setName("Secured Access Public");
			
			sec_rule.setDescription("Secured Access Public");
			sec_rule.setLastModifiedUser(caller);
			sec_rule.setCreationOffsetDateTime(OffsetDateTime.now());
			sec_rule.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			sec_rule.setDomain(domain);
			
			sec_rule.setCondition(secured_rule_condition);
			sec_rule.setDisplayCondition(secured_rule_condition);
			
			KbeeAcl sacl = new KbeeAcl();
			
			sacl.setLastModifiedUser(caller);
			sacl.setCreationOffsetDateTime(OffsetDateTime.now());
			sacl.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		
			Group users = null;
			
			for (Group group : groups) {
				if (group.getName().equals(KbeeGlobalRole.USER.getId())) {
					users = group;
					break;
				}
			}
			
			if (users!=null) {
				AclEntry sentry  = new KbeeAclEntry(sacl, users, false);
				List<Permission> spermissions = new ArrayList<Permission>();
				spermissions.add(KbeePermission.READ);
				sentry.setPermissions(spermissions);
				sacl.addEntry(caller, sentry);
				getContentDao().save(sacl);
				sec_rule.setAcl(sacl);
				List<String> slist = new ArrayList<String>();
				slist.add("Rule Secured Access");
				ServiceLocator.getService(SecurityContentMgmtService.class).update(sec_rule, slist);
			}
		} 
 	}

	/** 
	 * 
	 * 
	 */
	@Override
	public void deleteAllResources(Domain domain) throws DataManagementException {
		
		if (domain.getState()!=ObjectState.DELETED)
			throw new DataManagementException("Domain " + domain.getName() +" must be in status DELETED to wipe off");

		if (domain.getName().equals("kbee"))
			throw new DataManagementException("Domain kbee can not be deleted");
	}
	
	/** 
	 * @throws ContentMgmtException 
	 */
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void wipe(Domain domain) throws ContentMgmtException {
		if (domain.getName().equals("kbee"))
			throw new ContentMgmtException("Domain kbee can not be deleted");
		getDomainWipeDao().deleteDomain(domain);
	}
	
	
	/**
	 * 
	 * 
	 * @param domain
	 * @return
	 * @throws ContentCreationException
	 * @throws ContentMgmtException
	 */
	private List<Group> createCanonicalGroups(Domain domain) throws ContentCreationException, ContentMgmtException   {
		
		List<Group> groups = new ArrayList<Group>();
		
		// 1
		//
		KbeeGroup group = (KbeeGroup)ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
		group.setDomain(domain);
		group.setName(KbeeGlobalRole.USER.getId());
		group.setCanonical(true);
		group.setOnlyInternalUse(true);
		ServiceLocator.getService(SecurityContentMgmtService.class).update(group);
		groups.add(group);

		// 2
		//
		group = (KbeeGroup)ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
		group.setDomain(domain);
		group.setName(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		group.setCanonical(true);
		group.setOnlyInternalUse(false);
		ServiceLocator.getService(SecurityContentMgmtService.class).update(group);
		groups.add(group);

		
		// 3
		//
		group = (KbeeGroup)ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
		group.setDomain(domain);
		group.setName(KbeeGlobalRole.SUPPORT.getId());
		group.setCanonical(true);
		group.setOnlyInternalUse(false);
		ServiceLocator.getService(SecurityContentMgmtService.class).update(group);
		groups.add(group);


		// 4
		//
		group = (KbeeGroup)ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
		group.setDomain(domain);
		group.setName(KbeeGlobalRole.SECURITY.getId());
		group.setCanonical(true);
		group.setOnlyInternalUse(false);
		ServiceLocator.getService(SecurityContentMgmtService.class).update(group);
		groups.add(group);

		
		// 5
		//
		group = (KbeeGroup)ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
		group.setDomain(domain);
		group.setName(KbeeGlobalRole.INFORMATION_MODEL.getId());
		group.setCanonical(true);
		group.setOnlyInternalUse(false);
		ServiceLocator.getService(SecurityContentMgmtService.class).update(group);
		groups.add(group);
		
		
		// 15
		//
		group = (KbeeGroup)ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
		group.setDomain(domain);
		group.setName(KbeeGlobalRole.MODEL_READ.getId());
		group.setCanonical(true);
		group.setOnlyInternalUse(false);
		ServiceLocator.getService(SecurityContentMgmtService.class).update(group);
		groups.add(group);
		
		// 16
		//
		group = (KbeeGroup)ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
		group.setDomain(domain);
		group.setName(KbeeGlobalRole.SETTINGS.getId());
		group.setCanonical(true);
		group.setOnlyInternalUse(false);
		ServiceLocator.getService(SecurityContentMgmtService.class).update(group);
		groups.add(group);


		// 6
		//
		group = (KbeeGroup)ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
		group.setDomain(domain);
		group.setName(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
		group.setCanonical(true);
		group.setOnlyInternalUse(false);
		ServiceLocator.getService(SecurityContentMgmtService.class).update(group);
		groups.add(group);
		
		// 7
		//
		group = (KbeeGroup)ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
		group.setDomain(domain);
		group.setName(KbeeGlobalRole.DATASET_VALUES_READ.getId());
		group.setCanonical(true);
		group.setOnlyInternalUse(false);
		ServiceLocator.getService(SecurityContentMgmtService.class).update(group);
		groups.add(group);

		
		// 8
		//
		group = (KbeeGroup)ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
		group.setDomain(domain);
		group.setName(KbeeGlobalRole.WORKFLOW.getId());
		group.setCanonical(true);
		group.setOnlyInternalUse(true);
		ServiceLocator.getService(SecurityContentMgmtService.class).update(group);
		groups.add(group);
		
		
		// 9
		//
		group = (KbeeGroup)ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
		group.setDomain(domain);
		group.setName(KbeeGlobalRole.MONITOR_AUDIT.getId());
		group.setCanonical(true);
		group.setOnlyInternalUse(false);
		ServiceLocator.getService(SecurityContentMgmtService.class).update(group);
		groups.add(group);

		// 10
		//
		group = (KbeeGroup)ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
		group.setDomain(domain);
		group.setName(KbeeGlobalRole.ARCHIVE.getId());
		group.setCanonical(true);
		ServiceLocator.getService(SecurityContentMgmtService.class).update(group);
		groups.add(group);
		
		// 11
		//
		group = (KbeeGroup)ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
		group.setDomain(domain);
		group.setName(KbeeGlobalRole.WORKSPACE.getId());
		group.setCanonical(true);
		ServiceLocator.getService(SecurityContentMgmtService.class).update(group);
		groups.add(group);
		
		// 12
		//
		//group = (KbeeGroup)ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
		//group.setDomain(domain);
		//group.setName(KbeeGlobalRole.WORKSPACE_BULK_ACTIONS.getId());
		//group.setCanonical(true);
		//ServiceLocator.getService(SecurityContentMgmtService.class).update(group);
		//groups.add(group);
		
		// 13
		//
//		group = (KbeeGroup)ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
//		group.setDomain(domain);
//		group.setName(KbeeGlobalRole.CABINET_TEMPLATES.getId());
//		group.setCanonical(true);
//		ServiceLocator.getService(SecurityContentMgmtService.class).update(group);
//		groups.add(group);

		
		// 14
		//
		group = (KbeeGroup)ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
		group.setDomain(domain);
		group.setName(KbeeGlobalRole.PENDING_TASKS.getId());
		group.setCanonical(true);
		ServiceLocator.getService(SecurityContentMgmtService.class).update(group);
		groups.add(group);
		
		// 18
		//
		group = (KbeeGroup)ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
		group.setDomain(domain);
		group.setName(KbeeGlobalRole.PORTAL_ADMIN.getId());
		group.setCanonical(true);
		group.setOnlyPortal(true);
		ServiceLocator.getService(SecurityContentMgmtService.class).update(group);
		groups.add(group);

		// 19
		//
		group = (KbeeGroup)ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
		group.setDomain(domain);
		group.setName(KbeeGlobalRole.BILLBOARDS.getId());
		group.setCanonical(true);
		ServiceLocator.getService(SecurityContentMgmtService.class).update(group);
		groups.add(group);

		// 20
		//
		group = (KbeeGroup)ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
		group.setDomain(domain);
		group.setName(KbeeGlobalRole.SU.getId());
		group.setCanonical(true);
		ServiceLocator.getService(SecurityContentMgmtService.class).update(group);
		groups.add(group);
		

		// 21
		//
		group = (KbeeGroup)ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
		group.setDomain(domain);
		group.setName(KbeeGlobalRole.REPORTS.getId());
		group.setCanonical(true);
		ServiceLocator.getService(SecurityContentMgmtService.class).update(group);
		groups.add(group);

		// 27
		//
		group = (KbeeGroup)ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
		group.setDomain(domain);
		group.setName(KbeeGlobalRole.AUDITOR.getId());
		group.setCanonical(true);
		ServiceLocator.getService(SecurityContentMgmtService.class).update(group);
		groups.add(group);
		
		return groups;
		
	}
	
	/**
	 * 
	 * @param userSet
	 * @param groups
	 * @param domain
	 * @throws ContentMgmtException
	 */
	private void createWorkflowUser(UserSet userSet, List<Group> groups, Domain domain) throws ContentMgmtException {

		UserProfile callerProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		
		PersonMember member  = (PersonMember)userSet.createMember();
		member.setDomain(domain);
		
		KbeeUser wuser = new KbeeUser();
		wuser.setUserName(DomainService.WORKFLOW_USER+"@"+domain.getName());
		wuser.setLastName("Pending");
 		wuser.setLastModifiedUser(callerProfile.getUser());
		wuser.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		wuser.setDomain(domain);
		wuser.setStateEnabled();
		wuser.setCanonical(true);
		wuser.setActive(true);
		
		SystemParameter default_time_zone= getContentDao().findSystemParameterByKey("default_timezone");
		
		if (default_time_zone==null) 
			wuser.setTimeZone(getDefaultTimeZone());	
		else
			wuser.setTimeZone(default_time_zone.getValue());
			
		
		wuser.setLocale(java.util.Locale.getDefault());
		
		for (Group group : groups) {
			if (KbeeGlobalRole.USER.getId().equals(group.getName()))
				wuser.addGroup(group);
		}
		
		KbeeUserProfile userProfile = (KbeeUserProfile)member.getProfile(UserProfile.class);
		userProfile.setUser(wuser);
		userProfile.setDomain(domain);
		
		
		
		userProfile.setStartPage(domain.getDomainType()==DomainType.EXPRESS?"library":"mytasks");
		
		userProfile.setLastModifiedUser(callerProfile.getUser());
		userProfile.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		
		KbeePerson person = (KbeePerson)member.getPerson(); 
		person.setLastModifiedUser(callerProfile.getUser());
		person.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		person.setCreationOffsetDateTime(OffsetDateTime.now());
		person.setLastName("Pending");
		person.setDomain(domain);
		person.addProfile(userProfile);
		person.setPhone("");

		UserImagesService service = ServiceLocator.getService(UserImagesService.class);
		person.setPhoto(service.getDefaultImage(wuser.getUserName()));
		
		getContentDao().save((ModelObject) member);
		getContentDao().save(person);
		
		
		wuser.setPassword( "w0rk"+ String.valueOf((Double.valueOf(Math.random() * 1000000.0)).intValue()) +"fl0w" + String.valueOf((Double.valueOf(Math.random() * 1000000.0)).intValue()));
		wuser.setDomain(domain);
		getContentDao().save(wuser);
		txlogger.info(new SecurityCreateEvent(wuser, "create"));
		
		
	}
	
	
	
	/***
	 * 
	 * @param username
	 * @param email
	 * @param phone
	 * @param pwd
	 * @param userSet
	 * @param groups
	 * @param domain
	 * @throws ContentMgmtException
	 */
	private void createSupportUser(String username, String email, String phone, String pwd, UserSet userSet, List<Group> groups, List<Role> canonical_roles, Domain domain) throws ContentMgmtException {
								
		UserProfile callerProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		
		PersonMember member = (PersonMember)userSet.createMember();
		member.setDomain(domain);
		
		KbeeUser wuser = new KbeeUser();
		wuser.setUserName(username+"@"+domain.getName());
		wuser.setLastName(username);
 		wuser.setLastModifiedUser(callerProfile.getUser());
		wuser.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		wuser.setDomain(domain);
		wuser.setStateEnabled();
		wuser.setCanonical(true);
		wuser.setTimeZone(getDefaultTimeZone());
		wuser.setLocale(java.util.Locale.getDefault());
		
		for (Group group : groups) {
			if (KbeeGlobalRole.USER.getId().equals(group.getName())) wuser.addGroup(group);
		}

		KbeeUserProfile userProfile = (KbeeUserProfile)member.getProfile(UserProfile.class);
		userProfile.setUser(wuser);
		userProfile.setDomain(domain);
		userProfile.setLastModifiedUser(callerProfile.getUser());
		userProfile.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		userProfile.setEditPersonEnabled(true);
		userProfile.setStartPage(domain.getDomainType()==DomainType.EXPRESS?"library":"mytasks");

		
		KbeePerson person = (KbeePerson)member.getPerson(); 
		person.setLastModifiedUser(callerProfile.getUser());
		person.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		person.setCreationOffsetDateTime(OffsetDateTime.now());
		person.setLastName(username);
	
		person.setEmail(email);
		person.setPhone(phone);
		person.setDomain(domain);
		person.addProfile(userProfile);

		UserImagesService service = ServiceLocator.getService(UserImagesService.class);
		person.setPhoto(service.getDefaultImage(wuser.getUserName()));

		getContentDao().save((ModelObject) member);
		getContentDao().save(person);

		List<Role> list = getContentSecurityDao().getRoles(domain);
		
		for (Role role:list) {
			if (role.getAlias()!=null && role.getAlias().equals("support")) {
				List<UserRole> user_roles = new ArrayList<UserRole>();
				KbeeUserRole ur = new KbeeUserRole();
				ur.setRole(role); 
				ur.setUser(wuser);
				user_roles.add(ur);
				person.getService(RolesService.class).update(user_roles);
				break;
			}
		}

		wuser.setPassword(pwd);
		wuser.setDomain(domain);
		getContentDao().save(wuser);
		
		txlogger.info(new SecurityCreateEvent(wuser, "create"));
	}
	
	/**
	 * email
	 * phone
	 * password
	 */															
	private User createRootUser(UserSet userSet, List<Group> groups, List<Role> canonical_roles, Domain domain, String root_pwd, String root_email) throws ContentMgmtException {
		
		UserProfile callerProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		
		PersonMember member  = (PersonMember)userSet.createMember();
		member.setDomain(domain);
		
		KbeeUser rootuser = new KbeeUser();
		rootuser.setUserName("root@"+domain.getName());
		rootuser.setLastName("Root");
 		rootuser.setLastModifiedUser(callerProfile.getUser());
		rootuser.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		rootuser.setDomain(domain);
		rootuser.setStateEnabled();
		rootuser.setActive(true);
		rootuser.setLocale(java.util.Locale.getDefault());
		rootuser.setTimeZone(getDefaultTimeZone());
		rootuser.setLocale(java.util.Locale.getDefault());
		
		for (Group group : groups) {
			if 		(KbeeGlobalRole.USER.getId().equals(group.getName()))						rootuser.addGroup(group);
			else if (KbeeGlobalRole.WORKSPACE.getId().equals(group.getName()))					rootuser.addGroup(group);
			//else if (KbeeGlobalRole.WORKSPACE_BULK_ACTIONS.getId().equals(group.getName()))		rootuser.addGroup(group);
			else if (KbeeGlobalRole.SU.getId().equals(group.getName()))							rootuser.addGroup(group);
			else if (KbeeGlobalRole.DOMAIN_ADMIN.getId().equals(group.getName()))				rootuser.addGroup(group);
			else if (KbeeGlobalRole.MONITOR_AUDIT.getId().equals(group.getName()))					rootuser.addGroup(group);
			else if (KbeeGlobalRole.ARCHIVE.getId().equals(group.getName()))					rootuser.addGroup(group);
			else if (KbeeGlobalRole.MONITOR_AUDIT.getId().equals(group.getName()))					rootuser.addGroup(group);
			else if (KbeeGlobalRole.PENDING_TASKS.getId().equals(group.getName()))				rootuser.addGroup(group);
//			else if (KbeeGlobalRole.CABINET_ENTERPRISE.getId().equals(group.getName()))			rootuser.addGroup(group);
//			else if (KbeeGlobalRole.CABINET_EXTERNAL.getId().equals(group.getName()))			rootuser.addGroup(group);
		}
		
		KbeeUserProfile userProfile = (KbeeUserProfile)member.getProfile(UserProfile.class);
		userProfile.setUser(rootuser);
		userProfile.setDomain(domain);
		
		userProfile.setUitheme(ServiceLocator.getService(BrandingService.class).getDefaultUITheme());
		userProfile.setEmailNotifications(true);
		userProfile.setLastModifiedUser(callerProfile.getUser());
		userProfile.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		userProfile.setEditPersonEnabled(true);
		userProfile.setStartPage(domain.getDomainType()==DomainType.EXPRESS?"library":"mytasks");

		
		
		rootuser.setUitheme(userProfile.getUitheme());
		
		KbeePerson person = (KbeePerson) member.getPerson(); 
		person.setLastModifiedUser(callerProfile.getUser());
		person.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		person.setCreationOffsetDateTime(OffsetDateTime.now());
		person.setLastName("Root");
		person.setWorkPosition("RPDD SysAdmin");
		person.setPhone("");
		person.setDomain(domain);
		person.setEmail(root_email);
		person.addProfile(userProfile);
								
		UserImagesService service = ServiceLocator.getService(UserImagesService.class);
		person.setPhoto(service.getDefaultImage(rootuser.getUserName()));
		
		getContentDao().save((ModelObject)member);
		getContentDao().save(person);
		
		rootuser.setPassword(root_pwd);
		rootuser.setDomain(domain);
		
		getContentDao().save(rootuser);
		
		txlogger.info(new SecurityCreateEvent(rootuser, "create"));
		return rootuser;
	}
	
	
	/**
	 * 
	 * 
	 * @param application
	 * @param userSet
	 * @param groups
	 * @param domain
	 * @param pwd
	 * @param email
	 * @return
	 * @throws ContentMgmtException
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	private User createApplicationUser(String application, UserSet userSet, String admin_fisrtname, String admin_lastname, List<Group> groups, List<Role> canonical_roles, Domain domain, String pwd, String email) throws ContentMgmtException {
						
		UserProfile callerProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		
		PersonMember member  = (PersonMember)userSet.createMember();
		member.setDomain(domain);
		
		KbeeUser appuser = new KbeeUser();
		appuser.setUserName( application + "@"+domain.getName());
		appuser.setLastName(application);

		appuser.setLastModifiedUser(callerProfile.getUser());
		appuser.setCreationOffsetDateTime(OffsetDateTime.now());
		appuser.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		appuser.setDomain(domain);
		appuser.setStateEnabled();
		appuser.setActive(true);
		appuser.setLocale(Locale.ENGLISH);
		appuser.setTimeZone(getDefaultTimeZone());
		appuser.setLocale(java.util.Locale.getDefault());

		
		for (Group group: groups) {
			if 		(KbeeGlobalRole.USER.getId().equals(group.getName()))						appuser.addGroup(group);
		}
		
		KbeeUserProfile userProfile = (KbeeUserProfile)member.getProfile(UserProfile.class);
		userProfile.setUser(appuser);
		userProfile.setDomain(domain);
		userProfile.setEmailNotifications(true);
		userProfile.setLastModifiedUser(callerProfile.getUser());
		userProfile.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		userProfile.setEditPersonEnabled(true);
		userProfile.setStartPage(domain.getDomainType()==DomainType.EXPRESS?"library":"mytasks");

		
		
		KbeePerson person = (KbeePerson)member.getPerson(); 
		person.setLastModifiedUser(callerProfile.getUser());
		person.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		person.setLastName(application);
		person.setPhone("");
		person.setDomain(domain);
		person.setWorkPosition("Analyst");
		person.setEmail(email);
		person.addProfile(userProfile);

		UserImagesService service = ServiceLocator.getService(UserImagesService.class);
		person.setPhoto(service.getDefaultImage(appuser.getUserName()));
		
		getContentDao().save((ModelObject)member);
		getContentDao().save(person);
		
		appuser.setPassword(pwd);
		appuser.setDomain(domain);
		
		getContentDao().save(appuser);
		
		for (Role role:canonical_roles) {
			if (role.getAlias()!=null && role.getAlias().equals("domain-admin")) {
				List<UserRole> user_roles = new ArrayList<UserRole>();
				KbeeUserRole ur = new KbeeUserRole();
				ur.setRole(role); 
				ur.setUser(appuser);
				user_roles.add(ur);
				person.getService(RolesService.class).update(user_roles);
				break;
			}
		}

		txlogger.info(new SecurityCreateEvent(appuser, "create"));
		
		return appuser;
	}

	
	/***
	 * 
	 * 
	 * @param userSet
	 * @param username
	 * @param admin_fisrtname
	 * @param admin_lastname
	 * @param admin_email
	 * @param groups
	 * @param domain
	 * @throws ContentMgmtException
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	private void createAdminUser(UserSet userSet, String username, String admin_fisrtname, String admin_lastname, String admin_email, List<Group> groups, List<Role> canonical_roles, Domain domain) throws ContentMgmtException {
		UserProfile callerProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		
		PersonMember member  = (PersonMember)userSet.createMember();
		member.setDomain(domain);
		
		KbeeUser adminuser = new KbeeUser();
		adminuser.setUserName(username + "@"+domain.getName());
		adminuser.setFirstName(admin_fisrtname);
		adminuser.setLastName(admin_lastname);
 		adminuser.setLastModifiedUser(callerProfile.getUser());
		adminuser.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		adminuser.setDomain(domain);
		adminuser.setEmail(admin_email);
		adminuser.setStateEnabled();
		adminuser.setActive(true);
		adminuser.setLocale(Locale.ENGLISH);
		adminuser.setTimeZone(getDefaultTimeZone());

	 
		for (Group group : groups) {
			if 		(KbeeGlobalRole.USER.getId().equals(group.getName())) adminuser.addGroup(group);
		}
		
		KbeeUserProfile userProfile = (KbeeUserProfile)member.getProfile(UserProfile.class);
		userProfile.setUser(adminuser);
		userProfile.setDomain(domain);
		userProfile.setUitheme(ServiceLocator.getService(BrandingService.class).getDefaultUITheme());
		userProfile.setEmailNotifications(true);
		userProfile.setEmailRuleNotifications(true);
		userProfile.setSendFilesEmail(true);
		userProfile.setLastModifiedUser(callerProfile.getUser());
		userProfile.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		userProfile.setEditPersonEnabled(true);

		if (domain.getDomainType()==DomainType.EXPRESS)
			userProfile.setStartPage("library");
		else
			userProfile.setStartPage("mytasks");
		
		KbeePerson person = (KbeePerson)member.getPerson(); 
		person.setLastModifiedUser(callerProfile.getUser());
		person.setCreationOffsetDateTime(OffsetDateTime.now());
		person.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		person.setLastName(admin_lastname);
		person.setFirstName(admin_fisrtname);
		person.setWorkPosition("Domain Admin");
		person.setEmail(admin_email);
		person.setPhone("");
		person.setDomain(domain);
		person.addProfile(userProfile);

		UserImagesService service = ServiceLocator.getService(UserImagesService.class);
		person.setPhoto(service.getDefaultImage(adminuser.getUserName()));
		
		getContentDao().save((ModelObject) member);
		getContentDao().save(person);
		
		adminuser.setPassword( username+"user"+ String.valueOf((Double.valueOf(Math.random() * 1000000.0)).intValue()) + String.valueOf((Double.valueOf(Math.random() * 1000000.0)).intValue()));
		adminuser.setDomain(domain);
		
		
		List<UserRole> user_roles = new ArrayList<UserRole>();
		for (Role role:canonical_roles) {
			if (role.getAlias()!=null && role.getAlias().equals("domain-admin") || role.getAlias().equals("super-user")) {
				KbeeUserRole ur = new KbeeUserRole();
				ur.setRole(role); 
				ur.setUser(adminuser);
				user_roles.add(ur);
			}
		}

		if (!user_roles.isEmpty())
			person.getService(RolesService.class).update(user_roles);
		
		txlogger.info(new SecurityCreateEvent(adminuser, "create"));
		
		getContentDao().save(adminuser);
	}
	
		
	

	private DomainWipeDao getDomainWipeDao() {
		return (DomainWipeDao) ServiceLocator.getService(BeansService.class).getBean("domainWipeDao");
	}

	private Domain getDomainKbee() {
		return getContentDao().findDomainByName ("kbee");
	}
	
	private <T> DomRepository<T> getRepository(Class<T> objectclass) {
		DomRepository<T> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
	}

	private ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	private DataSetMember addDataSetMember(DataSet dataset, String value) throws ContentMgmtException {
		User domain_root = getRootUser(dataset.getDomain());
		DataSetMember mt_1 = dataset.createMember();
		mt_1.setDomain(dataset.getDomain());
		mt_1.setCreationOffsetDateTime(OffsetDateTime.now());
		mt_1.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		mt_1.setLastModifiedUser(domain_root);
		mt_1.setState(ObjectState.ENABLED);
		mt_1.setStrValue(value);
		getContentDao().save(mt_1);
		txlogger.info(new DataSetValueCreateEvent(mt_1, "create"));
		return mt_1;
	}

	
	private String getDefaultTimeZone() {
		if (default_time_zone==null) {
			synchronized(this) {
				
				logger.info(TimeZone.getDefault().getID());
				
				default_time_zone = getContentDao().findSystemParameterValueByKey("timezone.default", "US/Central");
			}
		}
		return default_time_zone;
	}
	
	private User getSessionUser() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();
	}
}










