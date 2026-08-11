package com.novamens.kbee.content.service;

import java.time.OffsetDateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentClass;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.ResourceGroupType;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.entity.ProfileFactory;
import com.novamens.content.form.EForm;
import com.novamens.content.library.Library;
import com.novamens.content.model.AccessStrategy;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.EntityMember;
import com.novamens.content.model.LauncherGroup;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.model.PersonMember;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.model.UserSet;
import com.novamens.content.model.UserSubset;
import com.novamens.content.properties.PropertyDao;
import com.novamens.content.rule.ActionRule;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.DomainRole;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.security.RolesService;
import com.novamens.content.service.DomainService;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserProfileType;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserSelfService;
import com.novamens.content.user.UserService;
import com.novamens.content.user.externalLogin.ExternalPlatformId;
import com.novamens.content.user.externalLogin.UserExternalPlatformIdType;
import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListService;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.base.KbeeResourceTag;
import com.novamens.kbee.content.entity.KbeePerson;
import com.novamens.kbee.content.form.KbeeDefaultForm;
import com.novamens.kbee.content.form.KbeeEForm;
import com.novamens.kbee.content.model.KbeeAttribute;
import com.novamens.kbee.content.model.KbeeAttributeTemplate;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeClassifierTemplate;
import com.novamens.kbee.content.model.KbeeContentTemplate;
import com.novamens.kbee.content.model.KbeeDataSet;
import com.novamens.kbee.content.model.KbeeEntitySet;
import com.novamens.kbee.content.model.KbeeExternalSet;
import com.novamens.kbee.content.model.KbeeExtractionMacro;
import com.novamens.kbee.content.model.KbeeLabelSet;
import com.novamens.kbee.content.model.KbeeLauncherGroup;
import com.novamens.kbee.content.model.KbeePersonMember;
import com.novamens.kbee.content.model.KbeePersonSet;
import com.novamens.kbee.content.model.KbeeRelationTemplate;
import com.novamens.kbee.content.model.KbeeSecuredSet;
import com.novamens.kbee.content.model.KbeeUserSubset;
import com.novamens.kbee.content.model.KbeeValueSet;
import com.novamens.kbee.content.query.KbeeSavedQuery;
import com.novamens.kbee.content.rule.KbeeActionRule;
import com.novamens.kbee.content.rule.KbeeEntityRule;
import com.novamens.kbee.content.user.KbeeUserProfile;
import com.novamens.kbee.content.user.KbeeUserRole;
import com.novamens.kbee.content.userlist.KbeeUserList;
import com.novamens.kbee.content.workflow.KbeeProcessLauncher;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.kbee.security.acl.KbeeAclEntry;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.logging.DataSetValueCreateEvent;
import com.novamens.logging.ModelCreateEvent;
import com.novamens.logging.ObjectUpdateEvent;
import com.novamens.logging.SecurityCreateEvent;
import com.novamens.portal6.model.Site;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.security.User;
import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.security.acl.Permission;
import com.novamens.service.BrandingService;
import com.novamens.service.LanguageService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.content.support.KbeeSupportTicket;
import kbee.content.support.SupportTicket;

/**
 * <p>This Services creates instances of {@link ModelObjet} like {@link DataSetMember} and also {@link Person} and {@link User}</p>
 * 
 * 
 * IMPORTANT 
 * 
 * SolR:
 * 
 * \kbee-webapp\src\main\resources\META-INF\solr-schemas\content-core\schema.json
 * 
 */
public class KbeeObjectFactoryService implements ObjectFactoryService {
																						
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeObjectFactoryService.class.getName());

	/** Logger synchronous with the TRX	*/
	static private Logger txlogger = LogManager.getLogger("TxLogger");
	
	private ContentDao contentDao;
	private ContentSecurityDao contentSecurityDao;
	private PropertyDao propertyDao;

	
	/** this array must be in sync with this file \kbee-webapp\src\main\resources\META-INF\solr-schemas\content-core\schema.json */
	static final public String solr_schema_attributes[] = { 
			"01",			"02",			"03",			"04",			"05",			"06",			"07",			"08",			"09",			"10",	"11",			"12",			"13",			"14",			"15",			"16",			"17",			"18",			"19",			"20",
			"21", 			"22" ,			"23" ,			"24" ,			"25" ,			"26" ,			"27" ,			"28" ,			"29" ,			"30" ,	"31" ,			"32" ,			"33" ,			"34" ,			"35" ,			"36" ,			"37" ,			"38" ,			"39" ,			"40" ,
			"41" , 			"42" ,			"43" ,			"44" ,			"45" ,			"46" ,			"47" ,			"48" ,			"49" ,			"50" ,	"51" ,			"52" ,			"53" ,			"54" ,			"55" ,			"56" ,			"57" ,			"58" ,			"59" ,			"60" ,
			"61" ,			"62" ,			"63" ,			"64"	
	};

	public KbeeObjectFactoryService() {
	}
	
	@Override
	public Person createUser(String username) throws ContentMgmtException {
		return createUser("", "",  "", username,  ObjectState.ENABLED, true,  new HashSet<Group>(), null, null);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Person createUser(String firstname, 
								String lastname, 
								String email, 
								String username, 
								ObjectState state) {
		return createUser(firstname, 
							lastname, 
							email, 
							username, 
							state,
							true, 
							new HashSet<Group>(), 
							null, 
							null);
	}
	
	/***
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Person createUser(	String firstname, 
								String lastname, 
								String email, 
								String username, 
								ObjectState state,
								boolean isemail, 
								Set<Group> groups, 
								List<KbeeGlobalRole> global_permissions, 
								List<Role> roles)
			throws ContentMgmtException {

		long start = System.currentTimeMillis();
		
		// Creates PersonMember -----------------------------------------------------------
		
		Person member = (Person) getUserSet().createMember();
		member.setLastName(lastname);
		member.setFirstName(firstname);
		member.setEmail(email);
		member.setState(state);
		member.setCreationOffsetDateTime(OffsetDateTime.now());
		getContentDao().save(((PersonMember)member).getPerson());
		getContentDao().save(member);
		
		logger.debug( member.toString());
		
		// Creates User -----------------------------------------------------------------
		
		KbeeUser user = new KbeeUser();
		user.setUserName(username);
		user.setFirstName(firstname);
		user.setLastName(lastname);
		user.setLastModifiedUser(getSessionUser());
		user.setCreationOffsetDateTime(OffsetDateTime.now());
		user.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		user.setEmail(email);
		user.setDomain(getDomain());

		if (state==ObjectState.ENABLED) {
			user.setStateEnabled();
			user.setActive(true);
		}
		if (state==ObjectState.DELETED) {
			user.setStateDeleted();
			user.setActive(false);
		}
		if (state==ObjectState.ARCHIVED) {
			user.setStateArchived();
			user.setActive(false);
		}
		
		boolean iscanonical = false;
		for (String s:ServiceLocator.getService(SecurityContentMgmtService.class).getReservedUserNames()) {
				if (user.getUserName().startsWith(s+"@")) {
					iscanonical=true;
					break;
				}
		}
		user.setCanonical(iscanonical);
		user.setLocale(getDomain().getLocale());
		user.setTimeZone(getDomain().getTimeZone());
		user.setUitheme(ServiceLocator.getService(BrandingService.class).getDefaultUITheme());
		if (getDomain().getDefaultPassword()!=null)
			user.setPassword(getDomain().getDefaultPassword());

		final Set<String> reservedUserNames = ServiceLocator.getService(SecurityContentMgmtService.class).getReservedUserNames();
		String usernameNoDomain = username.contains("@") ? username.substring(0, username.indexOf('@')) : username;
		user.setBillable(!reservedUserNames.contains(usernameNoDomain));
		user.setCanonical(reservedUserNames.contains(usernameNoDomain));

		
		// Canonical Groups
		//
		user.addGroup(getGroup(KbeeGlobalRole.USER));
		
		if (global_permissions!=null) {
			for (KbeeGlobalRole grole: global_permissions) {
				user.addGroup(getGroup(grole));
			}
		}
		
		for (Group group : groups) 
			user.addGroup(group);
		
		// Completes UserProfile -----------------------------------------------------------------
		
		KbeeUserProfile userProfile = (KbeeUserProfile)member.getProfile(UserProfile.class);
		userProfile.setLastModifiedUser(getSessionUser());
		userProfile.setDomain(getDomain());
		userProfile.setCreationOffsetDateTime(OffsetDateTime.now());
		userProfile.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		userProfile.setEditPersonEnabled(true);
		userProfile.setUser(user);
		userProfile.setStartPage("home");
		userProfile.setEmailNotifications(true);
		userProfile.setTipOfTheDay(false);
		userProfile.setEditPersonEnabled(true);
		userProfile.setType(UserProfileType.EMPLOYEE);
		ProfileFactory profileFactory = null;
		
		

		
		// Other Profiles via Spring -----------------------------------------------------------------

		try {
			profileFactory = (ProfileFactory)ServiceLocator.getService(BeansService.class).getBean("profilesFactory");
		} catch ( org.springframework.beans.factory.NoSuchBeanDefinitionException e) {
			logger.warn(e.getClass().getName() + " | no profile factory exists");	
		}
		
		if (profileFactory!=null) {
			profileFactory.createProfiles(((PersonMember)member).getPerson());
			logger.debug("profiles created");
		}
		
		
		// Person  -----------------------------------------------------------------
		
		((KbeePerson)((KbeePersonMember)member).getPerson()).addProfile(userProfile);
		getContentDao().save(((PersonMember)member).getPerson());
		getContentDao().save(member);
		getContentDao().save(user);
		getContentDao().flush();
		logger.debug("Person saved -> " + ((PersonMember)member).getPerson().getDisplayName());
		

		// Default Roles  ------------------------------------------------------------
		
		List<Role> list_roles = getContentSecurityDao().getRoles(getDomain());
		List<UserRole> user_roles = new ArrayList<UserRole>();
		for (Role role: list_roles) {
			
			if (role.isDefault()) {

				logger.debug("role default -> " + role.getName());
				
				if (role.getType()==EntityRole.TYPE) {
						
					
					
					// El unico Role Entity que puede tener al crear el usuario es un Entity Role sobre Person 
					// porque la Person no existe aun por lo tanto no puede tener 
					//
					EntityRole e_role = getContentSecurityDao().findEntityRoleById((Long) role.getId());
					
					if (e_role!= null) {	
						
						Classifier cla = e_role.getClassifier();
						
						if (cla!=null) {
							
							EntityMember entity_member = null;
							if (cla.getDataSet().getDataSetType()==DataSetType.PEOPLE) {
								entity_member = (PersonMember)member;
							}

							 /**
								EntityMember entity_member = null;
								List<Classification> list = ((PersonMember)member).getClassification();
								for (Classification clasification: list) {
									if (clasification.getClassifier()!=null && clasification.getClassifier().equals( cla )) {
										DataSetMember dm = clasification.getDataSetMember();
										if (dm instanceof EntityMember)
											entity_member = (EntityMember) dm;
									}
								}
								*/
							
							if (entity_member!=null) {
									KbeeUserRole k_ur = new KbeeUserRole(e_role, user, entity_member);
									user_roles.add(k_ur);
								}
								else {
									logger.error("can not create UserRole for Entity -> '" + e_role.getName()  + "' because entity_member is null");
								}
						}
						else {
							logger.error("Classifier is null EntityRole -> " + e_role.getName());
						}
					}
					else {
						logger.error( "No EntityRole for Role id -> " + role.getId());
					}
				}
				else if (role.getType()==DomainRole.TYPE)  {
						KbeeUserRole k_ur = new KbeeUserRole(getContentSecurityDao().findGeneralRoleById((Long) role.getId()),	user, null);
						user_roles.add(k_ur);
				}
				else {
					logger.error("Role must be Entity or Domain");
				}
			}
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("adding user roles ");
			user_roles.forEach(item -> logger.debug(item.getDisplayName()));
		}
		
		if (user_roles !=null && user_roles.size()>0)
			member.getService(RolesService.class).update(user_roles);
		
		txlogger.info(new SecurityCreateEvent(user, "create"));

		
		// Create site list
		//
		//ResourceBundle resources = ResourceBundle.getBundle(getClass().getName(), getDomain().getLocale());
		//ServiceLocator.getService(ObjectFactoryService.class).createUserList(user, Site.KEY, resources.getString("favorites"));
		logger.debug("user creation: " + String.valueOf(System.currentTimeMillis()-start)+" ms");
		
		return member;
	}
	
	/***
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Person createUser(String firstname,	
			String lastname, 
			String email,
			List<ExternalPlatformId> platforms,
			List<Role> roles,
			Map<ModelElement, List<Object>> classification) throws ContentMgmtException {
		
		long start = System.currentTimeMillis();
		
		// Creates PersonMember -----------------------------------------------------------
		Person member = (Person) getUserSet().createMember();
		member.setLastName(lastname);
		member.setFirstName(firstname);
		member.setEmail(email);
		member.setState(ObjectState.ENABLED);
		member.setCreationOffsetDateTime(OffsetDateTime.now());
		getContentDao().save(((PersonMember)member).getPerson());
		getContentDao().save(member);
		
		for (ModelElement element : classification.keySet()) {
			if (element instanceof Classifier) {
				List<DataSetMember> values = new ArrayList<DataSetMember>();
				for (Object value : classification.get(element)) {
					if (value instanceof DataSetMember) {
						values.add((DataSetMember)value);
					}
				}
				((PersonMember)member).setClassification((Classifier)element, values);
			}
			if (element instanceof Attribute) {
				List<String> values = new ArrayList<String>();
				for (Object value : classification.get(element)) {
					values.add(value.toString());
				}
				((PersonMember)member).setAttributeValues((Attribute)element, values);
			}
		}
				
		// Creates User -----------------------------------------------------------------
		
		KbeeUser user = new KbeeUser();
		user.setUserName(getUserName(firstname, lastname));
		user.setFirstName(firstname);
		user.setLastName(lastname);
		user.setLastModifiedUser(getSessionUser());
		user.setCreationOffsetDateTime(OffsetDateTime.now());
		user.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		user.setEmail(email);
		user.setDomain(getDomain());
		user.setActive(true);
		
		user.addGroup(getGroup(KbeeGlobalRole.USER));
		
		user.setLocale(getDomain().getLocale());
		user.setTimeZone(getDomain().getTimeZone());
		user.setUitheme(ServiceLocator.getService(BrandingService.class).getDefaultUITheme());
		if (getDomain().getDefaultPassword()!=null)
			user.setPassword(getDomain().getDefaultPassword());
		
		// Completes UserProfile -----------------------------------------------------------------
		
		KbeeUserProfile userProfile = (KbeeUserProfile)member.getProfile(UserProfile.class);
		userProfile.setLastModifiedUser(getSessionUser());
		userProfile.setDomain(getDomain());
		userProfile.setCreationOffsetDateTime(OffsetDateTime.now());
		userProfile.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		userProfile.setEditPersonEnabled(true);
		userProfile.setUser(user);
		userProfile.setStartPage("home");
		userProfile.setEmailNotifications(true);
		userProfile.setTipOfTheDay(false);
		userProfile.setEditPersonEnabled(true);
		userProfile.setType(UserProfileType.EMPLOYEE);
		ProfileFactory profileFactory = null;

		
		// Other Profiles via Spring -----------------------------------------------------------------
		try {
			profileFactory = (ProfileFactory)ServiceLocator.getService(BeansService.class).getBean("profilesFactory");
		} 
		catch (NoSuchBeanDefinitionException e) {
			logger.warn("no profile factory exists");	
		}
		if (profileFactory!=null) {
			profileFactory.createProfiles(((PersonMember)member).getPerson());
		}
		
		((KbeePerson)((KbeePersonMember)member).getPerson()).addProfile(userProfile);
		getContentDao().save(((PersonMember)member).getPerson());
		getContentDao().save(member);
		getContentDao().save(user);
		getContentDao().flush();
		
		//
		// assign default roles (NOTE: it must be a DomainRole (not a EntityRole)
		//
		List<UserRole> userRoles = new ArrayList<UserRole>();
		for (Role role: getContentSecurityDao().getRoles(getDomain())) {
			if (role.isDefault() && !role.isEntity()) {
				KbeeUserRole k_ur = new KbeeUserRole(role, user, null);
				userRoles.add(k_ur);
			}
		}
		for (Role role: roles) {
			if (!role.isEntity()) {
				KbeeUserRole k_ur = new KbeeUserRole(role, user, null);
				userRoles.add(k_ur);
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("adding user roles ");
			userRoles.forEach(item -> logger.debug(item.getDisplayName()));
		}
		
		if (userRoles !=null && userRoles.size()>0)
			member.getService(RolesService.class).update(userRoles);
		
		if (platforms!=null) {
			for (ExternalPlatformId platformId : platforms) {
				user.getService(UserSelfService.class).addLinkLoginPlatform(platformId, UserExternalPlatformIdType.EMAIL, email);
			}
		}
		
		txlogger.info(new SecurityCreateEvent(user, "create"));

		// Create site list
		//
		//ResourceBundle resources = ResourceBundle.getBundle(getClass().getName(), getDomain().getLocale());
		//ServiceLocator.getService(ObjectFactoryService.class).createUserList(user, Site.KEY, resources.getString("favorites"));
		
		logger.debug("user creation: " + String.valueOf(System.currentTimeMillis()-start)+" ms");
		return member;
	}
	
	/***
	 * 
	 */
	@Override
	public UserSet getUserSet() {
		UserSet userset= null;
		for (DataSet dataset : getDataSets()) {
			if (dataset.getDataSetType().equals(DataSetType.USER) && !(dataset instanceof UserSubset)) {
				userset = (UserSet)getContentDao().reload(dataset);
				break;
			}
		}

		return userset;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public com.novamens.dom.Object createUserList(User user, String console) throws ContentCreationException {
		return createUserList(user, console, null);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public com.novamens.dom.Object createUserList(User user, String console, String title) throws ContentCreationException {
		try {

			UserList u=new KbeeUserList(user, console);
			int total= ((KbeeUser)user).getService(UserListService.class).getUserLists(console).size();
			u.setTitle( title==null? (console +  " " + String.format("%2d",total+1)) : title);
			u.setCreationOffsetDateTime(OffsetDateTime.now());
			u.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			u.setLastModifiedUser(getSessionUser());
			u.setState(ObjectState.ENABLED);
			u.setDomain(getContentDao().findUserProfileByUser(user).getDomain());
			getPropertyDao().save(u);
			
			// txlogger.info(new DataSetValueCreateEvent(member, "create"));
			
			return u;
		}
		catch (Exception e)  {
			logger.error(e);
			throw new ContentCreationException(e);
		}
	}
	
	
	@Transactional(propagation = Propagation.REQUIRED)
	public com.novamens.dom.Object createUserList(User user, Site site) throws ContentCreationException {
		return createUserList(user, site, null);
	}
	
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public com.novamens.dom.Object createUserList(User user, Site site,  String title) throws ContentCreationException {
		try {

			UserList u=new KbeeUserList(user, site.getOId().toString(), site);
			int total= ((KbeeUser)user).getService(UserListService.class).getUserLists(site).size();
			u.setTitle( title==null? (site.getTitle() +  " " + String.format("%2d",total+1)) : title);
			u.setCreationOffsetDateTime(OffsetDateTime.now());
			u.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			u.setLastModifiedUser(getSessionUser());
			u.setState(ObjectState.ENABLED);
			u.setSite(site);
			u.setDomain(getContentDao().findUserProfileByUser(user).getDomain());
			getPropertyDao().save(u);
			
			// txlogger.info(new DataSetValueCreateEvent(member, "create"));
			
			return u;
		}
		catch (Exception e)  {
			logger.error(e);
			throw new ContentCreationException(e);
		}
	}
	

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public SupportTicket createSupportTicket(User user, String subject, String text) throws ContentCreationException {
		try {

			KbeeSupportTicket u=new KbeeSupportTicket(user, subject, text);
			u.setCreationOffsetDateTime(OffsetDateTime.now());
			u.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			u.setLastModifiedUser(getSessionUser());
			u.setState(ObjectState.DRAFT); // it can not be enabled 
			u.setDeliveryStatus(SupportTicket.DELIVERY_STATUS_DRAFT);
			u.setDomain(getContentDao().findUserProfileByUser(user).getDomain());
			getContentDao().save(u);
			return u;
		}
		catch (Exception e)  {
			logger.error(e);
			throw new ContentCreationException(e);
		}
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public ResourceTag createResourceTag() throws ContentCreationException {
		try {
			KbeeResourceTag tag = new KbeeResourceTag();
			tag.setName("new tag");
			tag.setCreationOffsetDateTime(OffsetDateTime.now());
			tag.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			tag.setLastModifiedUser(getSessionUser());
			tag.setState(ObjectState.ENABLED);
			tag.setType(ResourceGroupType.DEFAULT);
			tag.setDomain(getDomain());
			getRepository(ResourceTag.class).save(tag);
			return tag;
		}
		catch (Exception e)  {
			logger.error(e);
			throw new ContentCreationException(e);
		}
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public EForm createEForm(ContentTemplate template) throws ContentCreationException {
		try {
			KbeeEForm eform = new KbeeEForm();
			eform.setCreationOffsetDateTime(OffsetDateTime.now());
			eform.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			eform.setLastModifiedUser(getSessionUser());
			eform.setState(ObjectState.ENABLED); 
			eform.setDomain(getDomain());
						
			ResourceBundle resources = ResourceBundle.getBundle(getClass().getName(), getDomain().getLocale());
			if (template!=null) {
				eform.setDisplayName(resources.getString("new-form") + " " + template.getDisplayName());
				eform.setName(resources.getString("new-form") + " " + template.getDisplayName());
			}
			
			getRepository(EForm.class).save(eform);
			if (template!=null) {
				((KbeeContentTemplate)template).getForms().add(eform);
				getContentDao().save(template);
			}
			return eform;
		}
		catch (Exception e)  {
			logger.error(e);
			throw new ContentCreationException(e);
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public EForm createDefaultEForm(ContentTemplate template) throws ContentCreationException {
		try {
			
			KbeeEForm eform = new KbeeEForm();
			eform.setCreationOffsetDateTime(OffsetDateTime.now());
			eform.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			eform.setLastModifiedUser(getSessionUser());
			eform.setState(ObjectState.ENABLED); 
			eform.setDomain(getDomain());
			eform.setComponents((new KbeeDefaultForm(template)).getComponents());
						
			ResourceBundle resources = ResourceBundle.getBundle(getClass().getName(), getDomain().getLocale());
			eform.setDisplayName(resources.getString("new-form") + " " + template.getDisplayName());
			eform.setName(resources.getString("new-form") + " " + template.getDisplayName());
			
			getRepository(EForm.class).save(eform);
			((KbeeContentTemplate)template).getForms().add(eform);
			getContentDao().save(template);
			return eform;
		}
		catch (Exception e)  {
			logger.error(e);
			throw new ContentCreationException(e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public com.novamens.dom.Object createSavedQuery(User user, String console) throws ContentCreationException {
		return createSavedQuery(user, null, console, null, null, new HashMap<String,java.lang.Object>());
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public com.novamens.dom.Object createSavedQuery(User user, String title, String console, String browser, Site site, Map<String, java.lang.Object> parameters) throws ContentCreationException {
		try {
			
			KbeeSavedQuery u = new KbeeSavedQuery(user, title, console, site, parameters);
			u.setBrowser(browser);
			u.setCreationOffsetDateTime(OffsetDateTime.now());
			u.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			u.setState(ObjectState.ENABLED);
			u.setDomain(getContentDao().findUserProfileByUser(user).getDomain());
			getPropertyDao().save(u);
			return u;
		}
		catch (Exception e)  {
			logger.error(e);
			throw new ContentCreationException(e);
		}
	}


	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public com.novamens.dom.Object createSavedQueryDashboard(User user, String title, String console, Site site, Map<String, java.lang.Object> parameters) throws ContentCreationException {
		try {
		
			
			// workflow user (should be portal user ?)
			// 
			User w_user = getDomain().getService(DomainService.class).getWorkflowUser();
			
			KbeeSavedQuery u=new KbeeSavedQuery(w_user, title, console, site, parameters);
			u.setCreationOffsetDateTime(OffsetDateTime.now());
			u.setHome(true);
			u.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			u.setState(ObjectState.ENABLED);
			u.setLastModifiedUser(user);
			u.setDomain(getContentDao().findUserProfileByUser(user).getDomain());
			getPropertyDao().save(u);
			return u;
		}
		catch (Exception e)  {
			logger.error(e);
			throw new ContentCreationException(e);
		}
	}

	
	
	
	
	
	
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public com.novamens.dom.Object createClassifier() throws ContentCreationException {
			return createClassifier(null);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public com.novamens.dom.Object createClassifier(DataSet dataset) throws ContentCreationException {

		List<String> names = new ArrayList<String>(Arrays.asList(solr_schema_attributes));

		String dataset_name = dataset!=null ? dataset.getName().toLowerCase() : null;

		boolean is_dataset_name = false;
				
		List<Classifier> list = getContentDao().getClassifiers(getDomain());
		
		for (Classifier classifer : list) {
			String name = classifer.getUniqueName();
			if (name!=null && name.startsWith("clsf") && name.length()>5) {
				names.remove(name.substring(4, 6));
			}
			if (!is_dataset_name && (classifer.getName()!=null && classifer.getName().toLowerCase().equals(dataset_name))) {
				is_dataset_name = true;			
			}
		}
		
		if (names.isEmpty() || list.size()==solr_schema_attributes.length)
			throw new ContentCreationException("Can not obtain an Attribute from the Search Platform index. You may have reached the maximun number of Classifiers per server ("+String.valueOf(solr_schema_attributes.length)+") <br /> file -> \\src\\main\\resources\\META-INF\\solr-schemas\\content-core\\schema.json");


		String name = "clsf"+names.get(0);
		KbeeClassifier classifier = new KbeeClassifier();
		UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		
		if (userProfile!=null)
			classifier.setLastModifiedUser(userProfile.getUser());
		
		
		classifier.setMultiplicity(Multiplicity.M0N);
		classifier.setUniqueName(name);
		classifier.setCreationOffsetDateTime(OffsetDateTime.now());
		classifier.setName(name);
		classifier.setAPIClassifier(false);
		classifier.setPortal(true);
		classifier.setPortalSubtitle(false);classifier.setPortal(true);
		classifier.setPredicate(name);
		
		
		if (dataset!=null) {
			classifier.setDataSet(dataset);
			String c_name = getClassifierName(dataset.getName());
			classifier.setAlias(parsePredicate(c_name).toLowerCase().trim());
			classifier.setPredicate(parsePredicate(c_name));
			classifier.setName(c_name);
			classifier.setMetadataSubtitle(dataset.isCanonical());
			classifier.setOrdered(!dataset.isAggregation());
			
			
			
			
			boolean default_column = !(dataset.getDataSetType()==DataSetType.EXTERNAL ||dataset.getDataSetType()==DataSetType.LABEL || dataset.isAggregation());
			classifier.setDefaultGridColumn(default_column);
			
			boolean is_column = (dataset.getDataSetType()==DataSetType.ENTITY || dataset.getDataSetType()==DataSetType.PEOPLE || dataset.getDataSetType()==DataSetType.STRING || dataset.getDataSetType()==DataSetType.USER || dataset.getDataSetType()==DataSetType.DATE);
			
			classifier.setVisibility("workspace", is_column); 
			classifier.setVisibility("monitor",   is_column);
			classifier.setVisibility("pending",   is_column);
			classifier.setVisibility("portals",   is_column);
			
			for (Library lb: getLibraries(getDomain())) 
				classifier.setVisibility(lb.getKey(), is_column);
		}
		
		try {
			getContentDao().save(classifier);
			txlogger.info(new ModelCreateEvent(classifier, "create"));
		}
		catch (Exception e) {
			logger.error(e);
			throw new ContentCreationException(e);
		}
		
		return classifier;
	}
	
	/**
	 * 
	 * <p>We can not create more attributes than the supported by the SolR schema in file: <br/>
	 * {@code \kbee-webapp\src\main\resources\META-INF\solr-schemas\content-core\schema.json}
	 * </p>
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public com.novamens.dom.Object createAttribute() throws ContentCreationException {
		
		
		List<String> names = new ArrayList<String>(Arrays.asList(solr_schema_attributes));
		
		List<Attribute> list= getContentDao().getAttributes(getDomain());
		
		for (Attribute attribute : list) {
			String name = attribute.getUniqueName();
			if (name!=null && name.startsWith("attr") && name.length()==6) {
				names.remove(name.substring(4, 6));
			}
		}

		if (names.isEmpty() || list.size()==solr_schema_attributes.length)
			throw new ContentCreationException("Can not obtain an Attribute from the Search Platform index. You may have reached the maximun number of attributes per server ("+String.valueOf(solr_schema_attributes.length)+") <br /> file -> \\src\\main\\resources\\META-INF\\solr-schemas\\content-core\\schema.json");
		
		String name = "attr"+names.get(0);
		
		KbeeAttribute attribute = new KbeeAttribute();
		
		attribute.setMetadataSubtitle(false);
		attribute.setType(AttributeType.STRING);
		attribute.setFilterable(false);
		attribute.setUniqueName(name);
		attribute.setAlias(null);
		attribute.setCreationOffsetDateTime(OffsetDateTime.now());
		attribute.setLastModifiedUser(getSessionUser());
		attribute.setPortal(true);
		attribute.setPortalSubtitle(false);
		attribute.setPredicate(name);
		
		try {
			getContentDao().save(attribute);
			txlogger.info(new ModelCreateEvent(attribute, "create"));
		}
		catch (Exception e) {
			logger.error(e);
			throw new ContentCreationException(e);
		}
		return attribute;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public com.novamens.dom.Object createLauncher(ContentTemplate template) throws ContentCreationException {
		return createLauncher(template, getDomain());
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public com.novamens.dom.Object createLauncher(ContentTemplate template, Domain domain) throws ContentCreationException {
	
		//;
		
		
		try {
			
			KbeeProcessLauncher launcher = new KbeeProcessLauncher();
			if (template!=null)
			launcher.setLabel(template.getName()+" " + String.valueOf(template.getProcessLaunchers().size()+1));
			
			launcher.setCreationOffsetDateTime(OffsetDateTime.now());
			launcher.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			launcher.setLastModifiedUser(getSessionUser());
			
			launcher.setState(ObjectState.ENABLED);
			launcher.setDomain(template!=null ? template.getDomain() : domain);
			launcher.setContentTemplate(template);
			
			List<LauncherGroup> list=  getRepository(LauncherGroup.class).findAll(getDomain());
			if(list!=null && list.size()>0) {
				launcher.setLauncherGroup(list.get(0));
			}
			
			launcher.setLibrary(true);
			launcher.setEnabled(true);

			KbeeAcl acl = new KbeeAcl(); 
			acl.setLastModifiedUser(getSessionUser());
			acl.setLastModifiedOffsetDateTime(OffsetDateTime.now());

			List<Group> fgroups = null;
			fgroups = getSecurityDao().getCanonicalGroups(getDomain());
			Group fusers = null;
			for (Group group: fgroups) {
				if (group.getName().equals(KbeeGlobalRole.USER.getId())) {
					fusers = group;
					break;
				}
			}
			if (fusers!=null) {
				AclEntry docuentry = new KbeeAclEntry(acl, fusers, false);
				List<Permission> docupermissions= new ArrayList<Permission>();
				docupermissions.add(KbeePermission.CREATE);
				docuentry.setPermissions(docupermissions);
				acl.addEntry(getSessionUser(), docuentry);
				launcher.setAcl(acl);
			} else {
				logger.error("can not find group USERS");
				launcher.setAcl(new KbeeAcl());
			}
			
			getContentDao().save(launcher);
			
			return launcher;
		}
		catch (ContentMgmtException e) {
			logger.error(e);
			return null;
		}
	}
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public com.novamens.dom.Object createLauncherGroup(String na) throws ContentCreationException {
		return createLauncherGroup(na, getDomain());
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public com.novamens.dom.Object createLauncherGroup(String na, Domain domain) throws ContentCreationException {
		
		KbeeLauncherGroup lg = new KbeeLauncherGroup();
		
		String name=na!=null?na:"launcher_group";
		
		lg.setName(name);
		lg.setAlias(makeAlias(name));
		
		lg.setDomain(domain);
		lg.setCreationOffsetDateTime(OffsetDateTime.now());
		lg.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		lg.setLastModifiedUser(getSessionUser());
		lg.setState(ObjectState.ENABLED);
		lg.setVisible(true);
		
		lg.setLastModifiedUser(getSessionUser());
		lg.setOrder(getRepository(LauncherGroup.class).findAll(domain).size()+1);
		getRepository(LauncherGroup.class).save(lg);
		
		return lg;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public com.novamens.dom.Object createDataSet(DataSetType type) throws ContentCreationException {

		if (type==null)
			throw new IllegalArgumentException("type is null");

		try {
			
			DataSet dataSet = null;
			
			if (type==DataSetType.STRING) {
				dataSet = new KbeeValueSet();
				((KbeeValueSet) dataSet).setSuggester(false);
				((KbeeValueSet) dataSet).setAccessStrategy(AccessStrategy.All);
			}
			else if (type==DataSetType.USERSUBSET) {
				dataSet = new KbeeUserSubset();
			}
//			else if (type==DataSetType.DATE) {
//				dataSet = new KbeeDateSet();
//				((KbeeDateSet) dataSet).setSuggester(false);
//				((KbeeDateSet) dataSet).setAccessStrategy(AccessStrategy.All);
//			}
			else if (type==DataSetType.LABEL) {
				dataSet = new KbeeLabelSet();
				((KbeeLabelSet) dataSet).setSuggester(false);
				((KbeeLabelSet) dataSet).setAccessStrategy(AccessStrategy.All);
			}
			else if (type==DataSetType.ENTITY) {
	 			dataSet = new KbeeEntitySet();
				((KbeeEntitySet) dataSet).setSuggester(false);
				((KbeeEntitySet) dataSet).setAccessStrategy(AccessStrategy.All);
			}
			else if (type==DataSetType.EXTERNAL) {
	 			dataSet = new KbeeExternalSet();
				((KbeeExternalSet) dataSet).setSuggester(false);
				((KbeeExternalSet) dataSet).setAccessStrategy(AccessStrategy.All);
			}
			else if (type==DataSetType.SECURED) {
	 			dataSet = new KbeeSecuredSet();
				((KbeeSecuredSet) dataSet).setSuggester(false);
				((KbeeSecuredSet) dataSet).setAccessStrategy(AccessStrategy.All);
			}
			else if (type==DataSetType.PEOPLE) {
	 			dataSet = new KbeePersonSet();
				((KbeePersonSet) dataSet).setSuggester(false);
				((KbeePersonSet) dataSet).setAccessStrategy(AccessStrategy.All);

				KbeeExtractionMacro rule = new KbeeExtractionMacro();
				rule.setMarco(KbeePersonSet.defaultTitleRule);
				((KbeePersonSet) dataSet).setDisplayNameRule(rule);
			}

			
			UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
			
			if (userProfile!=null)
				dataSet.setLastModifiedUser(userProfile.getUser());

			dataSet.setCreationOffsetDateTime(OffsetDateTime.now());
			dataSet.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			((KbeeDataSet)dataSet).setCanonical(false);
			dataSet.setName (type.getLabel(getSessionUser().getLocale())  + "_" + String.valueOf(OffsetDateTime.now().getSecond()));
			((KbeeDataSet)dataSet).setAlias(null);
			
			getContentDao().save(dataSet);
			txlogger.info(new ModelCreateEvent(dataSet, "create"));

			return dataSet;
		}
		catch (Exception e)  {
			logger.error(e);
			throw new ContentCreationException(e);
		}
	}

	//@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public com.novamens.dom.Object cloneTemplate(ContentTemplate source) throws ContentCreationException {

		KbeeContentTemplate template = new KbeeContentTemplate();
		
		setBaseValues(template);

		//UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		
		template.setName(source.getName()+" clone");
		// readable is not cloned
		
		template.setContentClass(source.getContentClass());
		template.setInstantiable(source.isInstantiable());
		template.setDefault(source.isDefault());
		template.setAbstract(source.isAbstract());
		template.setOrder(source.getOrder());
		template.setHasDetailPage(source.hasDetailPage());
		template.setAbstract_label(source.getAbstract_label());
		template.setTitleRule(source.getTitleRuleTemplate());

		template.setLinkResources(source.isLinkResources());
		
		template.setDocument(source.isDocument());
		template.setPrivateNotes(source.isPrivateNotes());
		template.setCustomAttributes( source.isCustomAttributes());
		
		template.setKnowledgeBaseCabinet(source.isKnowledgeBaseCabinet());
		template.setComplianceCabinet(source.isComplianceCabinet());
		template.setTemplate(source.isTemplate());
		
		template.setActivity(source.isActivity());
		template.setAd(source.isAd());
		
		template.setMultimedia(source.isMultimedia());
		template.setVideo(source.isVideo());
		template.setAudio(source.isAudio());
		template.setText(source.isText());
		template.setImage(source.isImage());
		template.setTool(source.isTool());
		
		template.setAbstract_label(source.getAbstract_label());
		template.setPrivate_notes_label(source.getPrivate_notes_label());
		
		template.setText_label(source.getText_label());		
		template.setCustomattributes_label(source.getCustomattributes_label());
		template.setAPIContentClass(source.isAPIContentClass());
		template.setContentClassCode(source.getContentClassCode());
		template.setTreeFileLabel(source.getTreeFileLabel());
		template.setTreeFile(source.isTreeFile());
		template.setResources(source.isResources());
		template.setResourcesLabel(source.getResourcesLabel());
		template.setExternalReference(source.isExternalReference());
		template.setConsoleSubtitleRule(source.getConsoleSubtitleRule());
		template.setPortalsSubtitleRule(source.getPortalsSubtitleRule());
		
		// Clone Structure
		cloneStructure(source, template);

		// Clone Relationships
		cloneRelationships(source, template);
		
		// Clone Launchers
		cloneLaunchers(source, template);
		
		getContentDao().save(template);
		txlogger.info(new ModelCreateEvent(template, "create (clone:"+source.getDisplayName()+")"));
		
		return template;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public com.novamens.dom.Object createTemplate() throws ContentCreationException {
		return createTemplate(true);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public com.novamens.dom.Object createTemplate(boolean defaultWorkflow) throws ContentCreationException {
		try {
			
			KbeeContentTemplate template = new KbeeContentTemplate();
			
			UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
			
			setBaseValues(template);

			getContentDao().save(template);
			List<ModelElementTemplate> m_list = new ArrayList<ModelElementTemplate>();
			
			int n=0;
			String st_type_alias=null;
			
			
			
			/** add default resource groups --------------*/
			
			List<ResourceTag> list = new ArrayList<ResourceTag>();
			for ( ResourceTag rt: getRepository(ResourceTag.class).findAll(userProfile.getDomain()) ) {
				if (rt.isInNewContentTemplates()) {
					list.add(rt);
				}
			}
			template.setResourceTags(list);

			/** -----------------------------------------*/
			
			
			
			
			/**
			 * Document Type
			 * Status
			 * "User List" (invisible)
			 * 
			 */
			for (Classifier cl: getContentDao().getClassifiers(userProfile.getDomain().getId(), ObjectState.ENABLED)) {

				if (st_type_alias==null && cl.isContentType()) {
					//st_type=cl.getName();
					st_type_alias=cl.getAlias();
				}
				
				if (cl.isDefaultStructure()) {
					ClassifierTemplate ct = new KbeeClassifierTemplate(cl, n++);
					ct.setDefaultValues();
					ct.setMetadataSubtitle(true);
					template.addClassifier(ct);
					m_list.add(ct);
				}
			}
			
			//if (st_type_alias==null) {
				//st_type=get	ContentDao().findSystemParameterValueByKey("dataset_type.name","Document Type");
				st_type_alias="documenttype";
			//}
					
			
			for (Attribute at: getContentDao().getAttributes(userProfile.getDomain())) {
				
				if (at.isDefaultStructure() && at.getState()==ObjectState.ENABLED) {
						AttributeTemplate ct = new KbeeAttributeTemplate();
						((KbeeAttributeTemplate) ct).setAttribute(at);
						ct.setDefaultValues();
						n++;
						template.addAttribute(ct);
						m_list.add(ct);
				}
			}
			//section.setStructure(m_list);
			// ----
			//Modelo<#if tipodocumento??> ${tipodocumento}</#if><#if fideicomiso??> ${fideicomiso}</#if><#if unidadfuncional??> ${unidadfuncional}</#if><#if adherente??> 
			// ${adherente}</#if><#if fechareferencia??> - ${fechareferencia?string["dd/MM/yy"]}</#if>
			 

			/** -----------------------------------------*/
			
			
			template.setPrivateNotes(getContentDao().findSystemParameterValueByKey("content_class.new.private_notes", "yes").toLowerCase().trim().equals("yes"));
			template.setCustomAttributes(getContentDao().findSystemParameterValueByKey("content_class.new.custom-attributes", "no").toLowerCase().trim().equals("yes"));
			
			
			String tr = getContentDao().findSystemParameterValueByKey("content_class.new.consoletitlerule", st_type_alias!=null ? ("$classifier:"+ st_type_alias + "$") :"");
			
			template.setTitleRule(tr);
			
			template.setConsoleSubtitleRule(getContentDao().findSystemParameterValueByKey("content_class.new.consolesubtitlerule", st_type_alias!=null ? ("$classifier:"+ st_type_alias + "$") : "" ));
			template.setPortalsSubtitleRule(getContentDao().findSystemParameterValueByKey("content_class.new.portalsubtitlerule", st_type_alias!=null ? ("$classifier:"+ st_type_alias + "$") : "" ));
			
			//
			// Default Workflow launcher Assign
			// 
			KbeeAcl facl = new KbeeAcl(); 
			facl.setCreationOffsetDateTime(OffsetDateTime.now());
			facl.setLastModifiedUser(getSessionUser());
			facl.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			List<Group> fgroups = null;
			fgroups = ServiceLocator.getService(SecurityContentMgmtService.class).getDefaultGroups( userProfile.getDomain());
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
			

//			if (defaultWorkflow) {
//				//
//				// Assign Workflow by Default
//				//
//				List<Procedure> procs = userProfile.getDomain().getService(WorkflowDomainService.class).getProceduresLibrary();
//				
//				List<ProcessLauncher> launchers = new ArrayList<ProcessLauncher>();
//				
//				for (Procedure pr: procs) {
//					if (pr.getName()!=null && pr.getName().toLowerCase().equals("assign")) {
//						KbeeProcessLauncher launcher = new KbeeProcessLauncher();
//						launcher.setDomain(userProfile.getDomain());
//						launcher.setLabel(template.getName());
//						launcher.setAcl(facl);
//						launcher.setContentTemplate(template);
//						launcher.setEnabledContext(true);
//						launcher.setEnabled(true);
//						launcher.setProcedure(pr);
//						launchers.add(launcher);
//						logger.info("Setting Assign launcher");
//					}
//				}
//	
//				if (!launchers.isEmpty())
//					template.setProcessLaunchers(launchers);
//			}
			
			getContentDao().save(template);
			txlogger.info(new ModelCreateEvent(template, "create"));
			return template;
			
		}
		catch(Exception e) {
			logger.error(e);
			throw new ContentCreationException(e);
		}
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public com.novamens.dom.Object createMember(DataSet dataSet) throws ContentCreationException {
		try {
			UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
			DataSetMember member = dataSet.createMember();
			if (userProfile!=null)
				member.setLastModifiedUser(userProfile.getUser());
			member.setCreationOffsetDateTime(OffsetDateTime.now());
			member.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			getContentDao().save(member);
			txlogger.info(new DataSetValueCreateEvent(member, "create"));
			return member;
		}
		catch (Exception e)  {
			logger.error(e);
			throw new ContentCreationException(e);
		}
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public com.novamens.dom.Object createMember(DataSet dataSet, String name, Map<ModelElement, List<Object>> classification) throws ContentCreationException {
		try {
			UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
			DataSetMember member = dataSet.createMember();
			
			if (userProfile!=null)
				member.setLastModifiedUser(userProfile.getUser());
			member.setCreationOffsetDateTime(OffsetDateTime.now());
			member.setStrValue(name);
			member.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			
			for (ModelElement element : classification.keySet()) {
				if (element instanceof Classifier) {
					List<DataSetMember> values = new ArrayList<DataSetMember>();
					for (Object value : classification.get(element)) {
						if (value instanceof DataSetMember) {
							values.add((DataSetMember)value);
						}
					}
					member.setClassification((Classifier)element, values);
				}
				if (element instanceof Attribute) {
					List<String> values = new ArrayList<String>();
					for (Object value : classification.get(element)) {
						values.add(value.toString());
					}
					member.setAttributeValues((Attribute)element, values);
				}
			}
			
			getContentDao().save(member);
			txlogger.info(new DataSetValueCreateEvent(member, "create"));
			return member;
		}
		catch (Exception e)  {
			logger.error(e);
			throw new ContentCreationException(e);
		}
	}

	@Override
	public com.novamens.dom.Object createMemberNoTx(DataSet dataSet) throws ContentCreationException {

		if (dataSet==null)
			throw new IllegalArgumentException("dataSet is null");

		try {
			UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
			
			DataSetMember member = dataSet.createMember();

			if (userProfile!=null)
				member.setLastModifiedUser(userProfile.getUser());
			
			member.setCreationOffsetDateTime(OffsetDateTime.now());
			member.setLastModifiedOffsetDateTime(OffsetDateTime.now());

			getContentDao().save(member);
			txlogger.info(new DataSetValueCreateEvent(member, "create"));
			return member;
		}
		catch (Exception e)  {
			logger.error(e);
			throw new ContentCreationException(e);
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public ActionRule createRule(EntityMember entity, String label) {
		
		KbeeEntityRule rule = new KbeeEntityRule();
		rule.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		rule.setCreationOffsetDateTime(OffsetDateTime.now());
		rule.setLastModifiedUser(getSessionUser());
		rule.setState(ObjectState.ENABLED);
		rule.setEntity(entity);
		rule.setDomain(entity.getDomain());
		rule.setName(label);
		
		getRepository(ActionRule.class).save(rule);
		
		txlogger.info(new ObjectUpdateEvent<KbeeActionRule>(rule, "Create"));
		
		return rule;
	}

	
	// Spring 
	//
	public ContentDao getContentDao() 	{
		return contentDao;
	}
	
	public void setContentDao(ContentDao dao) {
		contentDao=dao;
	}
	
	public PropertyDao getPropertyDao()	{
		return propertyDao;
	}
	
	public void setPropertyDao(PropertyDao dao)	{
		propertyDao=dao;
	}
	
	public ContentSecurityDao getContentSecurityDao() {	
		return contentSecurityDao;
	}
	
	public void setContentSecurityDao(ContentSecurityDao dao) {
		contentSecurityDao=dao;
	}

	private String getClassifierName(String suggestedName) {
		if (suggestedName==null)
			return "classifier 123";
		boolean exists  = false;
		for (Classifier c : getContentDao().getClassifiers(getDomain())) {
			if (c.getName()!=null && c.getName().trim().equals(suggestedName)) {
				exists = true;
				break;
			}
		}
		if (!exists)
			return suggestedName.trim();
		return suggestedName + "_" + String.valueOf(System.currentTimeMillis() % 10000);
	}
	
	private List<Library> getLibraries(Domain domain) {
		return getRepository(Library.class).findAll(domain);
	}
	
	private List<DataSet> getDataSets() {
		return getContentDao().getDataSets(getDomain());
	}

	private Group getGroup(KbeeGlobalRole role) {
		return getContentSecurityDao().findGroupByName(role.getId(), getDomain());
	}
	
	private String parsePredicate(String s) {
		if (s==null)
			return null;
		String a1=s.toLowerCase().trim().replace(" de ", " ").replace(" a ", " ").replace(" por ", " ");
		a1=a1.replace("á", "a")
			 .replace("é", "e")
			 .replace("í", "i")
			 .replace("ó", "o")
			 .replace("ú", "u")
			 .replace("ñ", "nn");
		String a2 = WordUtils.capitalizeFully(a1).replaceAll("[ |\\t|\\s|(|)]", "").trim();
		return a2;
	}
	
	private String cleanName(String s) {
		if (s==null)
			return null;
		String a1=s.toLowerCase().trim().replace(" de ", " ").replace(" a ", " ").replace(" por ", " ");
		a1=a1.replace("á", "a")
			 .replace("é", "e")
			 .replace("í", "i")
			 .replace("ó", "o")
			 .replace("ú", "u")
			 .replace("ñ", "nn");
		return a1;
	}
	
	private void cloneRelationships(ContentTemplate source, KbeeContentTemplate template) {
		List<RelationTemplate> relations = new ArrayList<RelationTemplate>(); 
		for ( RelationTemplate rc: source.getRelations()) {
			KbeeRelationTemplate nrel = new KbeeRelationTemplate((KbeeRelationTemplate) rc);
			relations.add(nrel);
		}
		template.setRelations(relations);
	}
	
	private void cloneStructure(ContentTemplate source, KbeeContentTemplate template) {
		for (ClassifierTemplate ct: source.getClassifiers()) {
			KbeeClassifierTemplate t= new KbeeClassifierTemplate(ct);
			template.addClassifier(t);
		}
	}
	
	private String getUserName(String firstName, String lastName) {
		int iteration=0;
		String name = null;
		boolean exist = true;
		while (exist) {
			name = firstName!=null && firstName.trim().length()>0 ? firstName.substring(0,1) : "";
			name += lastName.trim();
			name = cleanName(name);
			if (iteration>0) name += String.valueOf(iteration);
			name += "@" + getDomain().getName();
			User user = ServiceLocator.getService(com.novamens.service.SecurityService.class)
				.findUserByUsername(name);
			if (user!=null) {
				iteration++;
			}
			else {
				exist = false;
			}
		}
		return name;
	}
	
	private LanguageService getLanguageService() {
		return  ServiceLocator.getService(LanguageService.class);
	}
	
	
	private void setBaseValues(KbeeContentTemplate ct) {
		
		UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		ct.setDomain(userProfile.getDomain()); 
		Locale locale = userProfile.getDomain().getLocale();
		
		
		ct.setCreationOffsetDateTime(OffsetDateTime.now());
		ct.setLastModifiedUser(userProfile.getUser());
		ct.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		ct.setPrivateNotes(getContentDao().findSystemParameterValueByKey("content_class.new.private_notes", "no").toLowerCase().trim().equals("yes"));
		ct.setCustomAttributes(getContentDao().findSystemParameterValueByKey("content_class.new.custom-attributes", "no").toLowerCase().trim().equals("yes"));
		

		String st_type = null, st_type_alias = null;
		
		for (Classifier cl: getContentDao().getClassifiers(userProfile.getDomain().getId(), ObjectState.ENABLED)) {
			if (st_type==null && cl.isContentType()) {
				st_type=cl.getName();
				st_type_alias=cl.getAlias();
			}
		}
		
		if (st_type_alias!=null) {
			ct.setTitleRule(getContentDao().findSystemParameterValueByKey("content_class.new.consoletitlerule", "$"+st_type_alias+"$"));
			ct.setConsoleSubtitleRule(getContentDao().findSystemParameterValueByKey("content_class.new.consolesubtitlerule",  "$"+st_type_alias+"$"));
			ct.setPortalsSubtitleRule(getContentDao().findSystemParameterValueByKey("content_class.new.portalsubtitlerule",  "$"+st_type_alias+"$"));
		}
		
		ct.setResources(true);
		ct.setResourcesLabel(getContentDao().findSystemParameterValueByKey("tab.resources", getLanguageService().getString("resources", locale)));

		ct.setAbstract(true);
		ct.setAbstract_label(getContentDao().findSystemParameterValueByKey("tab.notes",  getLanguageService().getString("notes", locale)));
		
		ct.setPrivate_notes_label(getContentDao().findSystemParameterValueByKey("tab.internal-information",  getLanguageService().getString("internal-use", locale)));
		ct.setPrivateNotes(true);
		
		ct.setLinkResources(true);
		ct.setDocument(true);
		
		ct.setCustomAttributes(false);
		
		for (ContentClass claz: getContentDao().getClasses()) {
			if (claz.getName().toLowerCase().startsWith("idoc")) {
				ct.setContentClass(claz);
				break;
			}
		}
		
		if (ct.getContentClass()==null)
			ct.setContentClass(getContentDao().getClasses().get(0));
		
		
		ct.setName(ct.getContentClass().getDisplayName());
	}
	
	private void cloneLaunchers(ContentTemplate source, KbeeContentTemplate template) {
		List<ProcessLauncher> launchers = new ArrayList<ProcessLauncher>();
		for (ProcessLauncher p: source.getProcessLaunchers()) {
			ProcessLauncher np = new KbeeProcessLauncher((KbeeProcessLauncher) p);
			KbeeAcl new_acl = new KbeeAcl();
			new_acl.setCreationOffsetDateTime(OffsetDateTime.now());
			new_acl.setLastModifiedUser(getSessionUser());
			new_acl.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			for(AclEntry src_e: p.getAcl().getEntries()) {
				AclEntry new_entry = new KbeeAclEntry((KbeeAclEntry) src_e);
				((KbeeAclEntry) new_entry).setPermissionsSerialized( ((KbeeAclEntry) src_e).getPermissionsSerialized());
				new_acl.addEntry(getSessionUser(), new_entry);
			}
			((KbeeProcessLauncher)np).setAcl(new_acl);
			getContentDao().save(new_acl);
			launchers.add(np);
		}
	}
	
	private Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	private ContentSecurityDao getSecurityDao() {
		return	(ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}	
	
	private <R> DomRepository<R> getRepository(Class<R> objectclass) {
		DomRepository<R> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
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