package com.novamens.kbee.content.domain.provisioning;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.EntityMember;
import com.novamens.content.model.ModelObject;
import com.novamens.content.model.PersonMember;
import com.novamens.content.model.PersonSet;
import com.novamens.content.model.UserSet;
import com.novamens.content.security.Role;
import com.novamens.content.security.RolesService;
import com.novamens.content.service.DomainService;
// import com.novamens.content.service.UserImagesService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.entity.KbeePerson;
import com.novamens.kbee.content.model.KbeePersonMember;
import com.novamens.kbee.content.model.KbeeUserSet;
import com.novamens.kbee.content.user.KbeeUserProfile;
import com.novamens.kbee.content.user.KbeeUserRole;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.logging.SecurityCreateEvent;
import com.novamens.security.ReservedUsername;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.BrandingService;
import com.novamens.service.ObjectService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.system.SystemParameter;

public class DomainUsersBuilderService extends BaseDomainBuilder  implements ObjectService {
			
	/** Logger that works synchronously in the TRX thread */
	static private Logger txlogger = LogManager.getLogger("TxLogger");
	static private Logger logger = LogManager.getLogger( DomainUsersBuilderService.class.getName());

	
	static final private String ICON_SET = "far";

	private Map<String, Object> parameters;
	
	
	 public DomainUsersBuilderService() {
	 }
	 
			 
	public DomainUsersBuilderService(Domain domain) {
		super(domain);
	}
			
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void build() throws ContentMgmtException, ContentCreationException {
		build(DomainType.EXPRESS.getAlias());
	}
	
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void build(String imodeltype) throws ContentMgmtException, ContentCreationException {
		
		StringBuilder sustr = new StringBuilder(); 
		
		List<Group> canonical_groups = getContentSecurityDao().getCanonicalGroups(getBuildingDomain());
		List<Role> canonical_roles = getContentSecurityDao().getCanonicalRoles(getBuildingDomain());		
		
		ResourceBundle resources = ResourceBundle.getBundle(getClass().getName(), getBuildingDomain().getLocale());
		
		KbeeUserSet userset = new KbeeUserSet();
		userset.setDomain(getBuildingDomain());
		userset.setName(resources.getString("user"));
		
		userset.setAlias(makeAlias(userset.getName()));
		userset.setDataSetType(DataSetType.USER);

		getContentDao().save(userset);

		createRootUser(userset, canonical_groups, canonical_roles, getBuildingDomain(), getStringValue("root_password", getParameters()), getStringValue("root_email", getParameters()));
		logger.debug("Create Root User with email " + getStringValue("root_email", getParameters()));
		
		createWorkflowUser(userset, canonical_groups, getBuildingDomain());
		logger.info("Create Workflow User");
		
		createPublicResourcesUser(userset, canonical_groups, getBuildingDomain());
		logger.info("Create Public Resources User");
		
		createAdminUser(userset,  getStringValue("admin_username", getParameters()), getStringValue("admin_firstname", getParameters()), getStringValue("admin_lastname", getParameters()), getStringValue("admin_email", getParameters()), canonical_groups, canonical_roles, getBuildingDomain());
		logger.info("Create Corporate Admin User");
		sustr.append(getStringValue("admin_username", getParameters()));
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void setExpressUsers() {
		
		List<UserSet> usersets =  getContentDao().getUserSets(getBuildingDomain());
		UserSet userset = !usersets.isEmpty() ? usersets.get(0) : null;
		
		if (userset==null) return;
		
		DataSet personset = null, organizationset = null;
		for (DataSet dataset : getContentDao().getDataSets(getBuildingDomain())) {
			if ("organizacion".equals(dataset.getAlias())) {
				organizationset = dataset;
			}
			if ("persona".equals(dataset.getAlias()) && dataset instanceof PersonSet) {
				personset = dataset;
			}
		}
		
		if (personset==null || organizationset==null) return;
		
		Classifier organizationclassifier = null;
		for (Classifier classifier : getContentDao().getClassifiers(getBuildingDomain())) {
			if ("organization".equals(classifier.getAlias())) {
				organizationclassifier = classifier;
				break;
			}
		}
		
		if (organizationclassifier==null) return;

		DataSetMember organization = organizationset.createMember();
		organization.setStrValue(getBuildingDomain().getOrganization());
		getContentDao().save(organization);
		
		List<UserRole> userroles = new ArrayList<UserRole>();
		for (Role role : getContentSecurityDao().getRoles(getBuildingDomain())) {
			if ("administrator".equals(role.getAlias())) {
				userroles.add(new KbeeUserRole(role, null, null));
			}
			if ("miembro_organizacion".equals(role.getAlias())) {
				userroles.add(new KbeeUserRole(role, null, (EntityMember)organization));
			}
		}
		
		if (userroles.isEmpty()) return;
		
		for (DataSetMember usermember : getContentDao().getMembers(userset, "strvalue")) {
			if (usermember instanceof PersonMember) {
				Person person = ((PersonMember)usermember).getPerson();
				UserProfile profile = person.getProfile(UserProfile.class);
				User user = profile!=null ? profile.getUser() : null;
				if (user!=null && 
					!user.getName().startsWith("root") && 
					ServiceLocator.getService(SecurityService.class).isMember(user, KbeeGlobalRole.DOMAIN_ADMIN.getId())) {
					KbeePersonMember newmember = (KbeePersonMember)personset.createMember();
					newmember.setPerson(person);
					newmember.setStrValue(person.getFirstLastName());
					newmember.setClassification(organizationclassifier, organization);
					getContentDao().save((DataSetMember)newmember);
					profile.setRoles(userroles);
				}
			}
		}
	}

	/**
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
		appuser.setTimeZone(domain.getTimeZone());
		appuser.setLocale(domain.getLocale());
		
		for (Group group: groups) {
			if (KbeeGlobalRole.USER.getId().equals(group.getName()))
				appuser.addGroup(group);
		}
		
		KbeeUserProfile userProfile = (KbeeUserProfile)member.getProfile(UserProfile.class);
		userProfile.setUser(appuser);
		userProfile.setClientProfile(false);
		userProfile.setDomain(domain);
		userProfile.setEmailNotifications(true);
		userProfile.setLastModifiedUser(callerProfile.getUser());
		userProfile.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		userProfile.setEditPersonEnabled(true);
		userProfile.setStartPage("home");

		
		KbeePerson person = (KbeePerson) member.getPerson(); 
		person.setLastModifiedUser(callerProfile.getUser());
		person.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		person.setLastName(application);
		person.setPhone("");
		person.setDomain(domain);
		person.setWorkPosition(getLanguageService().getString("analyst", domain.getLocale()));
		person.setEmail(email);
		person.addProfile(userProfile);

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

	public void setParameters( Map<String, Object> map) {
		this.parameters=map;
	}
	
	public Map<String, Object> getParameters() {
		return this.parameters;
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
		
		rootuser.setTimeZone(domain.getTimeZone());
		rootuser.setLocale(domain.getLocale());
		
		for (Group group : groups) {
			if 		(KbeeGlobalRole.USER.getId().equals(group.getName()))						rootuser.addGroup(group);
			else if (KbeeGlobalRole.WORKSPACE.getId().equals(group.getName()))					rootuser.addGroup(group);
			//else if (KbeeGlobalRole.WORKSPACE_BULK_ACTIONS.getId().equals(group.getName()))		rootuser.addGroup(group);
			else if (KbeeGlobalRole.SU.getId().equals(group.getName()))							rootuser.addGroup(group);
			else if (KbeeGlobalRole.DOMAIN_ADMIN.getId().equals(group.getName()))				rootuser.addGroup(group);
			else if (KbeeGlobalRole.MONITOR_AUDIT.getId().equals(group.getName()))				rootuser.addGroup(group);
			else if (KbeeGlobalRole.ARCHIVE.getId().equals(group.getName()))					rootuser.addGroup(group);
			else if (KbeeGlobalRole.MONITOR_AUDIT.getId().equals(group.getName()))				rootuser.addGroup(group);
			else if (KbeeGlobalRole.PENDING_TASKS.getId().equals(group.getName()))				rootuser.addGroup(group);
		}
		
		KbeeUserProfile userProfile = (KbeeUserProfile)member.getProfile(UserProfile.class);
		userProfile.setUser(rootuser);
		userProfile.setDomain(domain);
		userProfile.setIconSet(ICON_SET);
		userProfile.setUitheme(ServiceLocator.getService(BrandingService.class).getDefaultUITheme());
		userProfile.setEmailNotifications(true);
		userProfile.setLastModifiedUser(callerProfile.getUser());
		userProfile.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		userProfile.setEditPersonEnabled(true);
		userProfile.setStartPage("home");

		userProfile.setClientProfile(false);
		
		
		
		rootuser.setUitheme(userProfile.getUitheme());
		
		
		KbeePerson person = (KbeePerson) member.getPerson(); 
		person.setLastModifiedUser(callerProfile.getUser());
		person.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		person.setCreationOffsetDateTime(OffsetDateTime.now());
		person.setLastName("Root");
		person.setWorkPosition("SysAdmin");
		person.setPhone("");
		person.setDomain(domain);
		person.setEmail(root_email);
		person.addProfile(userProfile);
		person.setState(ObjectState.ENABLED);
		
		getContentDao().save((ModelObject)member);
		getContentDao().save(person);
		
		rootuser.setPassword(root_pwd);
		rootuser.setDomain(domain);
		
		logger.debug(" {} | {} | {} | before saving root user ", (getSessionUser()!=null?getSessionUser().getUserName():""), Thread.currentThread().getStackTrace()[1].getMethodName());
		
		getContentDao().save(rootuser);
		
		txlogger.info(new SecurityCreateEvent(rootuser, "create"));
		
		return rootuser;
	}
	
	/***
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
		adminuser.setLocale(domain.getLocale());
		adminuser.setTimeZone(domain.getTimeZone());
	 
		for (Group group : groups)
			if (KbeeGlobalRole.USER.getId().equals(group.getName())) adminuser.addGroup(group);
		
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
		userProfile.setStartPage("home");
		userProfile.setIconSet(ICON_SET);
		
		KbeePerson person = (KbeePerson)member.getPerson(); 
		person.setLastModifiedUser(callerProfile.getUser());
		person.setCreationOffsetDateTime(OffsetDateTime.now());
		person.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		person.setLastName(admin_lastname);
		person.setFirstName(admin_fisrtname);
		person.setWorkPosition(getLanguageService().getString("domain-admin", domain.getLocale()) );
		person.setEmail(admin_email);
		person.setPhone("");
		person.setDomain(domain);
		person.addProfile(userProfile);
		person.setState(ObjectState.ENABLED);
		
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
		
		// if domain is Express -> Person for Root User
		
	}
	

	/**
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
		wuser.setLastName(getLanguageService().getString("pending", domain.getLocale()));
		
 		wuser.setLastModifiedUser(callerProfile.getUser());
		wuser.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		wuser.setDomain(domain);
		wuser.setStateEnabled();
		
		wuser.setCanonical(true);
		wuser.setActive(true);
		wuser.setLocale(domain.getLocale());
		wuser.setTimeZone(domain.getTimeZone());

		
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
		userProfile.setClientProfile(false);
		userProfile.setUser(wuser);
		userProfile.setDomain(domain);
		userProfile.setStartPage("home");
		userProfile.setIconSet(ICON_SET);
		
		userProfile.setLastModifiedUser(callerProfile.getUser());
		userProfile.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		
		KbeePerson person = (KbeePerson)member.getPerson(); 
		person.setLastModifiedUser(callerProfile.getUser());
		person.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		person.setCreationOffsetDateTime(OffsetDateTime.now());
		person.setLastName( getLanguageService().getString("pending", domain.getLocale()));
		person.setDomain(domain);
		person.addProfile(userProfile);
		person.setPhone("");
		person.setState(ObjectState.ARCHIVED);


		//UserImagesService service = ServiceLocator.getService(UserImagesService.class);
		//person.setPhoto(service.getDefaultImage(wuser.getUserName()));
		
		getContentDao().save((ModelObject) member);
		getContentDao().save(person);
		
											
		wuser.setPassword( "w0rk"+ String.valueOf((Double.valueOf(Math.random() * 1000000.0)).intValue()) +"fl0w" + String.valueOf((Double.valueOf(Math.random() * 1000000.0)).intValue()));
		wuser.setDomain(domain);
		getContentDao().save(wuser);
		txlogger.info(new SecurityCreateEvent(wuser, "create"));
		
		
	}
	
	/**
	 * 
	 * @param userSet
	 * @param groups
	 * @param domain
	 * @throws ContentMgmtException
	 */
	private void createPublicResourcesUser(UserSet userSet, List<Group> groups, Domain domain) throws ContentMgmtException {

		UserProfile callerProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		
		PersonMember member  = (PersonMember)userSet.createMember();
		member.setDomain(domain);
		
		KbeeUser wuser = new KbeeUser();
		wuser.setUserName(ReservedUsername.PUBLICRESOURCES.getUserName() + "@" +domain.getName());
		wuser.setLastName( getLanguageService().getString("shared-resources", domain.getLocale()));
 		wuser.setLastModifiedUser(callerProfile.getUser());
		wuser.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		wuser.setDomain(domain);
		wuser.setStateEnabled();
		
		wuser.setCanonical(true);
		wuser.setActive(true);
		wuser.setLocale(domain.getLocale());
		wuser.setTimeZone(domain.getTimeZone());
		
		

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
		userProfile.setClientProfile(false);
		userProfile.setUser(wuser);
		userProfile.setDomain(domain);
		userProfile.setStartPage("home");
		userProfile.setIconSet(ICON_SET);
		
		userProfile.setLastModifiedUser(callerProfile.getUser());
		userProfile.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		
		KbeePerson person = (KbeePerson)member.getPerson(); 
		person.setLastModifiedUser(callerProfile.getUser());
		person.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		person.setCreationOffsetDateTime(OffsetDateTime.now());
		person.setLastName(getLanguageService().getString("shared-resources", domain.getLocale()));
		person.setDomain(domain);
		person.addProfile(userProfile);
		person.setPhone("");
		person.setState(ObjectState.ARCHIVED);

		getContentDao().save((ModelObject) member);
		getContentDao().save(person);
											
		wuser.setPassword( "w0rk"+ String.valueOf((Double.valueOf(Math.random() * 1000000.0)).intValue()) +"fl0w" + String.valueOf((Double.valueOf(Math.random() * 1000000.0)).intValue()));
		wuser.setDomain(domain);
		getContentDao().save(wuser);
		txlogger.info(new SecurityCreateEvent(wuser, "create"));
		
	}
	
	
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
	
		return s;
		
	}
}
