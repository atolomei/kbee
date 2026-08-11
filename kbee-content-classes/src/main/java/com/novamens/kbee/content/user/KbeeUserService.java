package com.novamens.kbee.content.user;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.document.IDoc;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.ContentId;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.EntityMember;
import com.novamens.content.model.PersonMember;
import com.novamens.content.model.SecuredMember;
import com.novamens.content.model.SecuredSet;
import com.novamens.content.query.SavedQuery;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.DataSetService;
import com.novamens.content.service.PersonService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.content.security.KbeeEntityRole;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.kbee.metrics.KbeeSystemMetricsService;
import com.novamens.kbee.security.KbeeAuthToken;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.logging.LoginEvent;
import com.novamens.security.AuthToken;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

/** 
 * System Service related with the Session {@link User}
 */
public class KbeeUserService implements UserService, EventListener  {

	static private Logger DBLogger = LogManager.getLogger("DBEventLogger");
						
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeUserService.class.getName());
	
	private Map<Serializable, Domain> domaincache = Collections.synchronizedMap(new HashMap<Serializable, Domain>());
	
	private Map<Serializable, AuthToken> tokenscache = Collections.synchronizedMap(new HashMap<Serializable, AuthToken>());
	
	private ContentDao contentDao;
	
	/** 
	 * @return Session User
	 */
	public UserProfile getSessionUserProfile() {
		User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
		if (user==null) 
			return null;
		UserProfile profile = getContentDao().findUserProfileByUser(user);
		return profile;
	}

	public Locale getSessionUserLocale() {
		User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
		Locale locale = user!=null ? user.getLocale() : Locale.getDefault();
		return locale;
	}
	
	public List<Principal> getSessionUserPrincipals() {
		List<Principal> principals = new ArrayList<>();
		SecurityService service = ServiceLocator.getService(SecurityService.class);
		User user = service.getSessionUser();
		principals.add(user);
		for (Group group : user.getGroups()) {
			principals = getWriters(group, principals);
		}
		return principals;
	}
	
	protected List<Principal> getWriters(Group group, List<Principal> principals) {
		
		if (principals.contains(group)) 
			return principals;
		
		principals.add(group);
	
		for (Group parent : ((Principal)group).getGroups()) {
			principals = getWriters(parent, principals);
		}
		
		return principals;
	}

	
	/** 
	 * @return Domain of Session User
	 */
	public Domain getDomain() {
	
		User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
		
		if (user==null) 
			return null;
		
		Domain domain = this.domaincache.get(user.getId());
		
		if (domain == null ) {
				domain =getSessionUser().getDomain();
				this.domaincache.put(user.getId(), domain);
		}	
		return domain;
	}
	
	@Override
	public boolean isUserAdmin() {
		return isAdmin(getContentDao().getUserSet());
	}
	
	@Override
	public boolean isUserAdmin(PersonMember person) {
		if (person.getLastModifiedUser().equals(getSessionUser()))
			return true;
		for (Classification classification : person.getClassification()) {
			if (classification!=null && isAdmin(classification.getDataSetMember())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean isUserAdmin(Person person) {
		PersonMember member = person instanceof PersonMember 
			? (PersonMember)person
			: person.getService(PersonService.class).getUserMember();		
		return isUserAdmin(member);
	}
	
	public boolean isReadable(DataSetMember member) {
		if (member instanceof SecuredMember) {
			return ServiceLocator
				.getService(ContentSystemSecurityService.class)
				.hasPermission((SecuredMember)member, KbeePermission.READ);
		}
		if (isAdmin(member)) {
			return true;
		}
		return false;
	}
	
	public boolean isWriteable(DataSet dataset) {
		return isAdmin(dataset) && dataset instanceof SecuredSet;
	}

	
	public boolean isWriteable(DataSetMember member) {
		if (member instanceof SecuredMember) {
			return ServiceLocator
				.getService(ContentSystemSecurityService.class)
				.hasPermission((SecuredMember)member, KbeePermission.WRITE);
		}
		if (isAdmin(member)) {
			//return true;
			return false; // va depender del tipo de administracion
		}
		if (member.getDataSet().isAggregation()) {
			DataSetMember aggregator = member.getDataSet()
				.getService(DataSetService.class)
				.getAggregator(member);
			if (isAdmin(aggregator)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isDeleteable(DataSetMember member) {
		if (member instanceof SecuredMember) {
			return ServiceLocator
				.getService(ContentSystemSecurityService.class)
				.hasPermission((SecuredMember)member, KbeePermission.DELETE);
		}
		if (isAdmin(member)) {
			return true;
		}
		if (member.getDataSet().isAggregation()) {
			DataSetMember aggregator = member.getDataSet()
				.getService(DataSetService.class)
				.getAggregator(member);
			if (isAdmin(aggregator)) {
				return true;
			}
		}
		return false;
	}

	
	@Override
	public boolean isAdmin(DataSet ds) {

		UserProfile usersessionprofile = getSessionUserProfile();
		for (UserRole userrole : usersessionprofile.getRoles()) {
			Role role = userrole.getRole();
			if (role.isEntity()) {
				EntityRole entityrole = (EntityRole)getContentDao().unproxy(role); 
				if (entityrole.getClassifier().getDataSet().equals(ds) && entityrole.isAdministrator()) {
					return true;
				}
				if (entityrole.manage(ds)) {
					return true;
				}
			}	
		}
		return false;
	}
	
	public AuthToken getAuthToken() {
		User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
		if (user==null) return null;
		AuthToken token = tokenscache.get(user.getId());
		if (token==null || !token.isValid()) {
			token = new KbeeAuthToken(60);
			tokenscache.put(user.getId(), token);
		}    
		return token;
	}
	
	@Override
	public User findRootUser(Domain domain) {
		String dm = domain.getName();
		return ServiceLocator.getService(SecurityService.class).findUserByUsername("root@"+dm);
	}
	
	@Transactional
	public void saveQuery(SavedQuery query) {
		try {
			getContentDao().save(query);
		}
		catch (Exception e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);
		}
	}
	
		
	@Transactional
	public void onLogin(User user) {
		UserProfile profile = getContentDao().findUserProfileByUser(user);
		profile.setLastLoginDate(OffsetDateTime.now());
	}
	
	@Transactional
	public void impersonate(User user) {
		try  {
			
			if (user==null) {
				logger.error("User is null");
				return;
			}
			
			// Impersonate ROOT@KBEE not allowed
			if (user.getUserName().startsWith("root@kbee"))
					throw new IllegalArgumentException ("Can not impersonate KBEE ROOT user");

			// Impersonating ROOT@domain is allowed only from domain KBEE
			if (getSessionUser()!=null && !getSessionUser().getDomain().getName().equals("kbee")) {
				if (user.getUserName().startsWith("root@kbee"))
					throw new IllegalArgumentException ("Can not impersonate ROOT user");	
			}
				
			ServiceLocator.getService(com.novamens.service.SecurityService.class).authenticate(user.getUserName());
			
			Serializable did=getContentDao().findUserProfileByUser(user).getDomain().getId();
			
			getMetricsServices().mark("login", did);
			
			if (!ServiceLocator.getService(SecurityService.class).isActive(user)) {
				ServiceLocator.getService(SecurityService.class).setActive(user);
				getMetricsServices().inc("users_logged", getContentDao().findUserProfileByUser(user).getDomain().getId());
			}
			DBLogger.info(new LoginEvent(user, "Super User"));
		}
		catch (Exception e) {
			
			logger.error(e);
			
			if (user==null)
				logger.error("user is null");
			
			else if (user.getUserName()==null)
				logger.error("username is null");
		}
		finally {
			
		}
	}

	@Override
	public IDoc getUploadAndCreateContainer() throws ContentMgmtException {
		UserPropertyService service =getSessionUser().getService(UserPropertyService.class);
		String cid = (String) service.getProperty("upload-and-create-container");
		if (cid!=null) {
			try {
				IDoc ret = (IDoc) getContentDao().findContentById(new ContentId(cid));
			if (ret!=null)
				return ret;
			}
			catch (Exception e) {
				throw new ContentMgmtException(e);
			}
		}
		IDoc idoc;
		try {
			idoc = createUploadAndCreateContainer();
			ContentId content_id = new ContentId(idoc);
			service.setProperty("upload-and-create-container", content_id.toString());
			return idoc;
		
		} 
		catch (ContentCreationException e) {
			throw new ContentMgmtException(e);
		}	
	}
		
	@Override
	@Transactional
	public void logout() {
		Domain domain = ServiceLocator.getService(UserService.class).getDomain();
		if (domain!=null) 
			getMetricsServices().dec("users_logged", domain.getId().toString());
		DBLogger.info(new com.novamens.logging.LogoutEvent(getSessionUser()));
	}
	
	@Override
	public boolean listen(Event event) {
		if (event instanceof EvictCacheServiceEvent)
			return true;
		return event.getObject() instanceof Domain;
	}

	@Override
	public void onEvent(Event event) {
		evict();
	}
	
	@Override
	public List<Classification> getClassification() {
		return ((Classificable)getMember()).getClassification();
	}
	
	public synchronized void evict() {
		this.domaincache.clear();
	}
	
	protected PersonMember getMember() {
		PersonMember member = null;
		User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
		user.getName();
		UserProfile profile = getContentDao().findUserProfileByUser(user);
		List<DataSetMember> members =  getContentDao().findMembersByEntity(profile.getEntity());
		for (DataSetMember m : members) {
			if (m.getDataSet().getDataSetType().equals(DataSetType.USER)) {
				member = (PersonMember)m;
				break;
			}
		}
		return member;
	}
	
	protected boolean isAdmin(DataSetMember member) {
		if (!(member instanceof EntityMember))
			return false;
		for (UserRole userrole : ServiceLocator.getService(UserService.class).getSessionUserProfile().getRoles()) {
			if (userrole.getRole().isEntity()) {
				if (userrole.getEntity().equals(member)) {
					Role role = (Role)getContentDao().reload(userrole.getRole());
 					if (((KbeeEntityRole)role).manage(member.getDataSet()))  
						return true;
				}
			}
		}
		return false;
	}
	
	protected KbeeUser getSessionUser() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile()!=null ? 
			(KbeeUser) ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser() :
			null;
	}

	private IDoc createUploadAndCreateContainer() throws  ContentMgmtException, ContentCreationException {
		ContentTemplate idoc_template;
		List<ContentTemplate> list = getContentDao().getTemplates(getDomain());
		for (ContentTemplate tem: list) {
			if (tem.getContentClass().getName().toLowerCase().equals("idoc")) {
				idoc_template = tem;
				IDoc content;
				content = (IDoc) ServiceLocator.getService(ContentFactoryService.class).create(idoc_template.getName(), true);
				content.setTitle("Upload and Create Container");
				content.setState(ObjectState.DRAFT);
				content.setAbstract("Container of KBFile uploaded in the Upload and Create Page");
				return content;
			}
		}
		logger.error("No ContentTemplate with KbeeClass iDOC");
		throw new ContentMgmtException("No ContentTemplate with KbeeClass iDOC");
	}
	
	private KbeeSystemMetricsService getMetricsServices() {
		return ServiceLocator.getService(KbeeSystemMetricsService.class);
	}
	
	private ContentDao getContentDao() {
		if (this.contentDao==null)
			this.contentDao = (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
		return this.contentDao;
	}
}
