package com.novamens.kbee.content.security;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.novamens.service.WebSessionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ConstraintException;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.SiteIQLRule;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.content.model.UserSet;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.DomainRole;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.IQLRule;
import com.novamens.content.security.Role;
import com.novamens.content.service.DomainService;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.hibernate.session.Session;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.user.KbeeUserProfile;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.kbee.security.acl.KbeeAclEntry;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.logging.ApplicationStartEvent;
import com.novamens.logging.SecurityCreateEvent;
import com.novamens.logging.SecurityDeleteEvent;

import com.novamens.logging.SecurityUpdateEvent;
import com.novamens.security.ReservedUsername;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeArea;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.security.acl.Permission;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

/**  
 *  Services are responsible for saving Audit Event. LogEvent. This includes updating 
 *  the object last modified date and user.
 *  
 *  All methods must be called inside a Spring Authenticated Session  (getSessionUser() not null)
 */			
public class KbeeSecurityContentMgmtService implements SecurityContentMgmtService, EventListener {
	
	static private Logger logger = LogManager.getLogger(KbeeSecurityContentMgmtService.class.getName());
	static private Logger trx_logger = LogManager.getLogger("TxLogger");	

	// TODO HA									
	private Map<String, Group> group_Librarys = new HashMap<String, Group>();
	private Map<String, List<Group>> default_groups = new HashMap<String,  List<Group>>();
	
	private ContentSecurityDao securityDao;
	private String default_time_zone = null;

	private final Set<String> reserverdNames =Stream.of("suroot", "root", "pending", "workflow", "suworkflow", "support", ReservedUsername.PUBLICRESOURCES.getUserName() ).collect(Collectors.toSet());

	public KbeeSecurityContentMgmtService() {
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void startApplicationServer() {
		startApplicationServer(0);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void startApplicationServer(long miliseconds) {
		try {
			ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
			trx_logger.info(new ApplicationStartEvent( miliseconds));
			
		} catch (Exception e) {
			logger.error(e);
		}
		finally {
			
		}
	}
	
	@Override
	public List<Group> getRootsGroups() {
		List<Group> roots = new ArrayList<Group>();
		for (Group group : getSecurityDao().getGroups()) {
			if (group.getGroups().isEmpty()) {
				roots.add(group);
			}
		}
		return roots;
	}

	
	@Transactional(propagation = Propagation.MANDATORY)
	public void addDefaultGroups(Person person) throws ContentCreationException {
		User user = getUser(person);
		Set<Group> ug = user.getGroups();
		StringBuilder str = new StringBuilder();
		for (Group group: getDefaultGroups(person.getDomain())) {
			if (!ug.contains(group)) {
				user.addGroup(group);
				if (str.length()==0)
					str.append("Add Group -> ");
				else
					str.append(", ");
				str.append(group.getName());
			}
		}
		if (str.length()>0)
			trx_logger.info(new SecurityUpdateEvent(user, str.toString()));
	}

	/** 
	 *
	 *  <p>Spring Transactional methods must throw RuntimeException because Spring 
	 *  needs RuntimeException to rollback in case the transaction fails</p>
	 *   
	 * @throws {@link ContentMgmtException}
	 * 
	 * Logs: SecurityEvent
 	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Person createUser(UserSet dataSet) throws ContentCreationException, ContentMgmtException {
		try {
			
			Person member = (Person) dataSet.createMember();

			member.setCreationOffsetDateTime(OffsetDateTime.now());
			member.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			member.setLastModifiedUser(getSessionUser());
			ResourceBundle res = ResourceBundle.getBundle(KbeeSecurityContentMgmtService.this.getClass().getName(),  getSessionUser().getLocale());
			member.setLastName(res.getString("new-user"));
			member.setState(ObjectState.ENABLED);
		
			getContentDao().save(((PersonMember)member).getPerson());
			getContentDao().save(member);
			
			// no se puede aca porque el user es null 
			// usamos el Session User
			//
			User user = getSessionUser();
			trx_logger.info(new SecurityUpdateEvent(user, "Create User for Person: " + String.valueOf(member.getId())));
			
			return member;
			
		} catch (RuntimeException e)  {
			logger.error(e);
			throw new ContentCreationException(e);
		}		
	}
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Group createGroup(String name, Domain domain, User caller, boolean isCanonical, String areacode) throws ContentCreationException {
		return  createGroup(name,  domain,  caller, isCanonical, false, areacode);
	}
	/** 
	* @param name
	* @param domain
	* @param caller
	* @throws ContentCreationException
	*/
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Group createGroup(String name, Domain domain, User caller, boolean isCanonical, boolean isPortalOnly, String areacode) throws ContentCreationException {
	
	    if (name==null)
	    	throw new IllegalArgumentException("name is null");
	    
	    if (domain == null)
	    	throw new IllegalArgumentException("domain is null");
	    
	    if (caller == null)
	    	throw new IllegalArgumentException("caller is null");
	    	
	    //if (areacode == null)
	    //	throw new IllegalArgumentException("areacode is null");
	    	
	    		
	    
		KbeeGroup group = new KbeeGroup();  
 		group.setName(name);
		group.setDomain(domain);
		group.setCanonical(isCanonical);
		group.setOnlyInternalUse(false);
		
		if (isCanonical) {
			if (areacode!=null) {
				KbeeArea area = KbeeArea.getAreaByCode(areacode);
				if (area!=null)
					group.setArea(area);
			}
		}	
		
		group.setOnlyPortal(isPortalOnly);
		group.setLastModifiedUser(caller);
		group.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		getSecurityDao().save(group);
		trx_logger.info(new SecurityCreateEvent(group, "Create", caller));
		return group;
	}
	

	@Transactional(propagation = Propagation.REQUIRED)
	public Group createGroup() throws ContentCreationException {
		User caller = ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();
		return createGroup("New Group", getDomain(), caller, false, null);
	}
	
	/** 
	*/
	@Transactional
	public Acl createAcl() throws ContentCreationException {
		User caller = ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();
		KbeeAcl acl = new KbeeAcl(); 
		acl.setLastModifiedUser(caller);
		acl.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		acl.setCreationOffsetDateTime(OffsetDateTime.now());
		getSecurityDao().save(acl);
		// LOG EVENTO
		return acl;
	}

	/** 
	* 
	* @param type: IQL Sentence, Wizard created Sentence
	* @throws ContentCreationException
	* 
	*/
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public IQLRule createRule(int type) throws ContentCreationException {
		return createRule(type, getDomain());
	}
	
	/** 
	* 
	* @param type: IQL Sentence, Wizard created Sentence
	* @return
	* @throws ContentCreationException
	*/
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public IQLRule createRule(int type, Domain domain) throws ContentCreationException {
		User caller = ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();
		KbeeSecurityRule rule = new KbeeSecurityRule();
		rule.setName("New Rule");
		rule.setType(type);
		rule.setLastModifiedUser(caller);
		rule.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		rule.setCreationOffsetDateTime(OffsetDateTime.now());
		rule.setState(ObjectState.ENABLED);
		
		if (domain!=null)
			rule.setDomain(getDomain());
		else
			rule.setDomain(domain);
		KbeeAcl acl = new KbeeAcl(); 
		acl.setLastModifiedUser(caller);
		acl.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		rule.setAcl(acl);
		getSecurityDao().save(rule);
		trx_logger.info(new SecurityUpdateEvent(rule, "Create Rule " + String.valueOf(rule.getId())));
		return rule;
	}

	/** 
	* 
	* @param type: IQL Sentence, Wizard created Sentence
	* @return
	* @throws ContentCreationException
	*/
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public IQLRule createRule(int type, Domain domain, User basicUser) throws ContentCreationException, ContentMgmtException {
		User caller = ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();
		KbeeSecurityRule rule = new KbeeSecurityRule();
		rule.setName(basicUser.getDisplayName());
		rule.setType(type);
		rule.setLastModifiedUser(caller);
		rule.setCreationOffsetDateTime(OffsetDateTime.now());
		rule.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		if (domain!=null)
			rule.setDomain(getDomain());
		else
			rule.setDomain(domain);
		KbeeAcl acl = new KbeeAcl(); 
		acl.setLastModifiedUser(caller);
		acl.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		acl.setCreationOffsetDateTime(OffsetDateTime.now());

		AclEntry fentry = new KbeeAclEntry(acl, basicUser, false);
		List<Permission> fpermissions = new ArrayList<Permission>();
		fpermissions.add(KbeePermission.READ);
		fentry.setPermissions(fpermissions);
		acl.addEntry(getSessionUser(), fentry);
		getContentDao().save(acl);
		rule.setAcl(acl);
		getSecurityDao().save(rule);
		trx_logger.info(new SecurityUpdateEvent(rule, "Create Rule " + String.valueOf(rule.getId())));
		return rule;
	}
	
	
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public Role createRole(int type, Domain domain) throws ContentCreationException {
		return createRole(type, domain, null);
	}

	@Override
	public Role createRoleNoTrx(int type, Domain domain) throws ContentCreationException {
		return createRoleNoTrx(type, domain, null);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Role createRole(int type) throws ContentCreationException {
		return createRole(type, getDomain(), null);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Role createRole(int type, Classifier clasi) throws ContentCreationException {
		return createRole(type, getDomain(), clasi);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Role createRole(int type, Domain domain, Classifier clasi) throws ContentCreationException {
		return createRoleNoTrx(type, domain, clasi);
	}
	
	@Override
	public Role createRoleNoTrx(int type, Domain domain, Classifier clasi) throws ContentCreationException {
		
		KbeeAbstractRole role = null;
		String name = null;
		Locale locale = getSessionUser()!=null?getSessionUser().getLocale():Locale.getDefault();
				
		switch (type) {
			case EntityRole.TYPE: {
				role = new KbeeEntityRole();
				if (clasi!=null)
					((KbeeEntityRole) role).setClassifier(clasi);
				ResourceBundle res = ResourceBundle.getBundle(EntityRole.class.getName(), locale);
				name=res.getString("new-name");
				break;
			}
			case DomainRole.TYPE: {
				role = new KbeeDomainRole();
				ResourceBundle res = ResourceBundle.getBundle(DomainRole.class.getName(), locale);
				name=res.getString("new-name");
				break;
			}
			default:	
				throw new ContentCreationException("invalid type " + String.valueOf(type) + " Domain: " + domain.getName());
		}
		
		List<Permission> list = new ArrayList<Permission>();
		list.add(KbeePermission.READ);
		role.setPermissions(list);
		
		role.setName(name!=null?name: "New " + role.getRoleType() +" Role");
		role.setLastModifiedUser(getSessionUser());
		role.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		role.setCreationOffsetDateTime(OffsetDateTime.now());
		role.setState(ObjectState.ENABLED);
		role.setDomain(domain);
		
 		Group roleGroup = createGroup();
		roleGroup.setName(role.getName());
		((KbeeAbstractRole)role).setGroup(roleGroup);
		
		/**
		 * add all workflow rights
		 * add all content rights
		 **/
		for (Group group: getContentSecurityDao().getCanonicalGroups(getDomain())) {
			if ( group.isEnabled() && group.getAreaCode()!=null && (group.getAreaCode().equals(KbeeArea.CONTENT.getCode()) || group.getAreaCode().equals(KbeeArea.WORKFLOW.getCode()))) {
				if (group.getName()!=null && group.getName().equals(KbeeGlobalRole.BILLBOARDS.getId())) {
					logger.debug(group.getDisplayName() + " Skipped ");
				}
				else {
					role.addGroup(group);
					logger.debug("add canonical group -> " + group.getDisplayName());
				}
			}
		}
		
		getSecurityDao().save(role);
		trx_logger.info(new SecurityCreateEvent(role, "Create " + role.getName()));
		return role;
	}
	

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(Role role) throws ContentMgmtException {
		getSecurityDao().delete(role);
		trx_logger.info(new SecurityDeleteEvent(role, "Role"));
	}


	/**
	 * User -> Person
	 */
	@Override
	@Transactional
	public void enable(User user) {
		
		if (user.isEnabled()) 
			return;
		
		user.setStateEnabled();
		User caller = ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();
		user.setLastModifiedUser(caller);
		user.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		getSecurityDao().save(user);
		
		// person state must be Enabled
		this.propagateUserToPerson(user);
		
		trx_logger.info(new SecurityUpdateEvent(user, "Enable"));
		
	}


	/**
	 * User -> Person
	 */
	@Override
	@Transactional
	public void disable(User user) {
		
		if (!user.isEnabled()) 
			return;
		
		user.setStateArchived();
		User caller = ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();
		
		user.setLastModifiedUser(caller);
		user.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		getSecurityDao().save(user);
		
		// person state must be disabled
		this.propagateUserToPerson(user);
		
		trx_logger.info(new SecurityUpdateEvent(user, "Disable"));
		ServiceLocator.getService(WebSessionService.class).expireUserSessions(user.getUserName());

	}
	
	
	
	

	@Override
	@Transactional
	public void disable(Person person) {
		
		if (person.getState()==ObjectState.ARCHIVED)
			return;

		person.setState(ObjectState.ARCHIVED);
		User caller = ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();
		person.setLastModifiedUser(caller);
		person.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		getContentDao().save(person);
		
		// user state must be archived
		User user = person.getProfile(UserProfile.class).getUser();
		user.setStateArchived();
		user.setLastModifiedUser(caller);
		user.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		getSecurityDao().save(user);
		
		trx_logger.info(new SecurityUpdateEvent(user, "Disable"));
		ServiceLocator.getService(WebSessionService.class).expireUserSessions(user.getUserName());
		
	}

	
	@Override
	@Transactional
	public void enable(Person person) {
		
		
		if (person.getState()==ObjectState.ENABLED)
				return;
		
		person.setState(ObjectState.ENABLED);
		User caller = ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();
		person.setLastModifiedUser(caller);
		person.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		getContentDao().save(person);
		
		
		// user state must be enabled
		User user = person.getProfile(UserProfile.class).getUser();
		user.setStateEnabled();
		user.setLastModifiedUser(caller);
		user.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		getSecurityDao().save(user);
		
		trx_logger.info(new SecurityUpdateEvent(user, "Enable"));

	}


	/**
	* @param person
	* @param updatedParts
	* @throws IOException
	*/
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update(Person person, List<String> updatedParts)throws ContentMgmtException {
		person.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		person.setLastModifiedUser(getSessionUser());
		getContentDao().save(person);
		
		// user state must be enabled
		User user = person.getProfile(UserProfile.class).getUser();
		
		if (user==null)
			return;
			
		//if (person.getState()==ObjectState.ENABLED)			user.setStateEnabled();
		//else if (person.getState()==ObjectState.ARCHIVED)   user.setStateArchived();
		//else if (person.getState()==ObjectState.DELETED)	user.setStateDeleted();
		//user.setLastModifiedUser(getSessionUser());
		//user.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		//getSecurityDao().save(user);
		  
		trx_logger.info(new SecurityUpdateEvent(getUser(person), updatedParts));
		
	}

	/** 
	* @param person
	* @param updatedParts
	* @throws IOException
	*/
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update(Person person, String desc) throws ContentMgmtException {
		
		if (!(person instanceof PersonMember)) {
			List<DataSetMember> members = getContentDao().findMembersByEntity(person);
			if (members.size()==1) {
				if (members.get(0) instanceof PersonMember) {
					person = (PersonMember)members.get(0);
				}
			}
		}
		person.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		person.setLastModifiedUser(getSessionUser());
		getContentDao().save(person);
		
		User user = person.getProfile(UserProfile.class).getUser();
		
		if (user==null)
			return;
		/**
		if 		(person.getState()==ObjectState.ENABLED)		user.setStateEnabled();
		else if (person.getState()==ObjectState.ARCHIVED)		user.setStateArchived();
		else if (person.getState()==ObjectState.DELETED)		user.setStateDeleted();
		user.setLastModifiedUser(getSessionUser());
		user.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		getSecurityDao().save(user);
		**/
		
		trx_logger.info(new SecurityUpdateEvent(user, desc!=null?desc:""));
		
	}
	

	/** 
	* @param profile
	* @param updatedParts
	* @throws IOException
	*/
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update(UserProfile profile, List<String> updatedParts)throws ContentMgmtException {
		
		profile.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		profile.setLastModifiedUser(getSessionUser());
		
		try {
		
			Person person = profile.getPerson();
			update(person, "User Profile");
			
			if (profile.getUser() instanceof KbeeUser) {
				if (((KbeeUser) profile.getUser()).getUitheme()==null || profile.getUitheme()==null || (!((KbeeUser) profile.getUser()).getUitheme().equals(profile.getUitheme()))) {
					((KbeeUser) profile.getUser()).setUitheme(profile.getUitheme());
					getSecurityDao().save(profile.getUser());
				}
			}
		} 
		catch (Exception e) {
			logger.error("LastModified of a Person when updating the UserProfile | " + e.getClass().getName());
		}
		
		getSecurityDao().save(profile);
		
		if (profile.getUser()!=null)  
			trx_logger.info(new SecurityUpdateEvent(profile.getUser(), updatedParts));
	}
	
	
	/**
	* @param profile
	* @param updatedParts
	* @throws IOException
	*/
	@Override
	@Transactional
	public void update(User user, List<String> updatedParts)throws ContentMgmtException {
		User sessionUser = getSessionUser();
		
		if (user.getTimeZone()==null)
			user.setTimeZone( getDefaultTimeZone());
		
		
		//UserProfile profile = getContentDao().findUserProfileByUser(user);
		this.propagateUserToPerson(user);

		 
		
		user.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		user.setLastModifiedUser(getSessionUser());
		getSecurityDao().save(user, sessionUser);
		trx_logger.info(new SecurityUpdateEvent(user, sessionUser, updatedParts));
	}
	
	
	/**
	*  
	* @param user
	* @throws IOException
	*/
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update(User user) throws ContentMgmtException {
		
		if (user.getTimeZone()==null)
			user.setTimeZone(getDefaultTimeZone());
		
		user.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		user.setLastModifiedUser(getSessionUser());
		
		getSecurityDao().save(user);
		
		this.propagateUserToPerson(user);
		
		trx_logger.info(new SecurityUpdateEvent(user, "Update"));
	}

	
	
	/**
	 * 
	 * {Person} and {KbeeUser}
	 * Person is PersonMember 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void markAsDeleted(Person person)  throws ContentMgmtException {
		
		User user =person.getProfile(UserProfile.class).getUser();
		
		if (user.getUserName().startsWith("root@") || 
				user.getUserName().startsWith(DomainService.WORKFLOW_USER+"@") ||
				user.getUserName().startsWith("pendingresources@"))
				throw new ContentMgmtException(user.getUserName()+"  is a reserved user and can not be deleted"); 
			
		if (person instanceof PersonMember)
			((PersonMember) person).getPerson().setState(ObjectState.DELETED);

		person.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		person.setLastModifiedUser(getSessionUser());
		person.setState(ObjectState.DELETED);
		
		// this is propagated to UserProfile -> User
		person.setLastName(person.getLastName()+ " [DELETED]");
		getContentDao().save(person);
		
		if (user!=null) {
			user.setStateDeleted();
			user.setActive(false);
			user.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			user.setLastModifiedUser(getSessionUser());
			getContentDao().save(user);
		}
		
		
		if (user!=null) {
			trx_logger.info(new SecurityDeleteEvent(user, "Mark as Deleted"));
			ServiceLocator.getService(WebSessionService.class).expireUserSessions(user.getUserName());
		}
	}
	
	/**
	 * 
	 * {Person} and {KbeeUser}
	 * Person is PersonMember 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void restore(Person person)  throws ContentMgmtException {
		
		UserProfile profile = person.getProfile(UserProfile.class);
		User user =profile.getUser();
		
		if (person instanceof PersonMember) {
			((PersonMember) person).getPerson().setState(ObjectState.ENABLED);
			person.setLastName(person.getLastName().replace(" [DELETED]", ""));
		}

		person.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		person.setLastModifiedUser(getSessionUser());
		person.setState(ObjectState.ENABLED);
		getContentDao().save(person);
		
		if (user!=null) {
			user.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			user.setLastModifiedUser(getSessionUser());
			user.setStateEnabled();
			user.setActive(true);
			getContentDao().save(user);
			trx_logger.info(new SecurityUpdateEvent(user, "Restored"));
		}
	}
	
	/**
	 * if the group has members, we try to remove the group from the members.
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(Group group) throws ContentMgmtException {
		getSecurityDao().delete(group);
		trx_logger.info(new SecurityDeleteEvent(group, "Delete"));
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(IQLRule rule)throws ContentMgmtException {
		getSecurityDao().delete(rule);
		trx_logger.info(new SecurityDeleteEvent(rule, "Delete"));
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteRule(IQLRule rule)throws ContentMgmtException {
		getSecurityDao().delete(rule);
		trx_logger.info(new SecurityDeleteEvent(rule, "Delete"));
	}

	@Override
	public List<Group> getDefaultGroups(Domain domain) {
	
		if (default_groups.containsKey(domain.getId().toString())) 
				return default_groups.get(domain.getId().toString());
		
		synchronized (this) {
			List<Group> list = new ArrayList<Group>();
			list.add(getSecurityDao().findGroupByName(KbeeGlobalRole.USER.getId(), domain));
			list.add(getSecurityDao().findGroupByName(KbeeGlobalRole.WORKSPACE.getId(), domain));
			list.add(getSecurityDao().findGroupByName(KbeeGlobalRole.MONITOR_AUDIT.getId(), domain));
			//list.add(getSecurityDao().findGroupByName(KbeeGlobalRole.CABINET_ENTERPRISE.getId(), domain));
			default_groups.put(domain.getId().toString(), list);
		}
		
		return default_groups.get(domain.getId().toString()); 
	}

	public List<SiteIQLRule> findRuleByRelatedObjectId(String id) {
		return getSecurityDao().findRuleByRelatedObjectId(id);
	}

	/**
	 * 
	 * 
	 */
	@Override
	@Transactional
	public void update(Group group)throws ContentMgmtException {
		group.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		group.setLastModifiedUser(getSessionUser());
		getSecurityDao().save(group);
		trx_logger.info(new SecurityUpdateEvent(group, "Update"));
	}
	
	
	/**
	 * 
	 * 
	 */
	@Override
	@Transactional
	public void update(Acl  acl)throws ContentMgmtException {
		((KbeeAcl) acl).setLastModifiedOffsetDateTime(OffsetDateTime.now());
		((KbeeAcl) acl).setLastModifiedUser(getSessionUser());
		getSecurityDao().save(acl);
	}
	
	
	
	@Transactional
	public void save(Acl acl)throws ContentMgmtException {
		((KbeeAcl) acl).setLastModifiedOffsetDateTime(OffsetDateTime.now());
		((KbeeAcl) acl).setLastModifiedUser(getSessionUser());
		getSecurityDao().save(acl);
		// LOG
		// TODO SAVE ACL
		//
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update(IQLRule rule, List<String> updatedParts) throws ContentMgmtException {
		rule.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		if (rule instanceof KbeeSecurityRule) 
			((KbeeSecurityRule) rule).setLastModifiedUser(getSessionUser());
		else if (rule instanceof KbeeSiteSecurityRule)
			((KbeeSiteSecurityRule) rule).setLastModifiedUser(getSessionUser());
		getSecurityDao().save(rule);
		trx_logger.info(new SecurityUpdateEvent(rule, "Update"));
	}

	
	@Override
	public void updateNoTrx(Role role, String part) throws ContentMgmtException {
		List<String> updatedParts = new ArrayList<String>();
		updatedParts.add(part);
		update(role, updatedParts);
	}	


	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update(Role role, String part) throws ContentMgmtException {
		List<String> updatedParts = new ArrayList<String>();
		updatedParts.add(part);
		update(role, updatedParts);
	}	
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update(Role role, List<String> updatedParts) throws ContentMgmtException {
		KbeeAbstractRole kbeerole = (KbeeAbstractRole)role;
		kbeerole.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		kbeerole.setLastModifiedUser(getSessionUser());
		getSecurityDao().save(kbeerole);
		StringBuilder str = new StringBuilder();
			updatedParts.forEach(item ->  str.append( (str.length()>0?", ":"")+item));
		trx_logger.info(new SecurityUpdateEvent(role, str.toString()));
	}
	

	@Transactional
	public SiteIQLRule createSiteRule() throws ContentCreationException {
		User caller = ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();
		KbeeSiteSecurityRule rule = new KbeeSiteSecurityRule();
		rule.setType(IQLRule.RULE_WIZARD_IQL);
		rule.setLastModifiedUser(caller);

		rule.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		rule.setCreationOffsetDateTime(OffsetDateTime.now());

		rule.setDomain(getDomain());
		KbeeAcl acl = new KbeeAcl(); 
		acl.setLastModifiedUser(caller);
		
		acl.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		acl.setCreationOffsetDateTime(OffsetDateTime.now());
		
		rule.setAcl(acl);
		getSecurityDao().save(rule);
		trx_logger.info(new SecurityCreateEvent(rule, "Create"));
		
		return rule;
	}

	
	/**
	 *  Person -> deletes entity
	 * TODO VER CUAND SE BORRA PERSON !!!
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(Person person)throws ContentMgmtException, ConstraintException {
		
		{
			UserProfile profile = person.getProfile(UserProfile.class);
	
			if (profile!=null && profile.getUser()!=null && (profile.getUser().getUserName().startsWith("root@") || 
				profile.getUser().getUserName().startsWith("workflow@") ||
				profile.getUser().getUserName().startsWith("pendingresources@")))
				throw new ContentMgmtException(profile.getUser().getUserName()+"  is a reserved user and can not be deleted"); 
			
			User user = null;
			
			if (profile!=null && profile.getUser()!=null) {
				user = profile.getUser();
				user = (User)getContentDao().reload(user);
				List<Group> groups = new ArrayList<Group>();
				groups.addAll(user.getGroups());
				for (Group group : groups) {
					user.removeGroup(group);
				}
				SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");	
				for (UserRole userrole : profile.getRoles()) {
					Role role = userrole.getRole();
//					role = sf.getCurrentSession().load(KbeeAbstractRole.class, role.getId());
					role = (Role)getContentDao().reload(role);
					role.removeRole(person, userrole.getEntity());
				}
				((KbeeUserProfile)profile).setRoles(new ArrayList<UserRole>());
				
				sf.getCurrentSession().flush();
				sf.getCurrentSession().evict(profile);
				//sf.getCurrentSession().clear();
//				try {
//				sf.getCurrentSession().delete(profile);
//				person.set
//				sf.getCurrentSession().flush();
//				}
//				catch (Exception e) {
//					
//					throw e;
//				}
			}
		}
		
		// -----
		// VER DELETE USER
		// ----
		getContentDao().delete(person);
	
//		if (user!=null) {
//			user.setStateDeleted();
//			user.setActive(false);
//			user.setLastModifiedOffsetDateTime(OffsetDateTime.now());
//			user.setLastModifiedUser(getSessionUser());
//			getContentDao().save(user);
//			trx_logger.info(new SecurityDeleteEvent(user, "Delete (Person)"));
//		}
	}

	
	public void setSecurityDao(ContentSecurityDao securityDao) {
		this.securityDao = securityDao;
	}
	
	public ContentSecurityDao getSecurityDao() {
		return this.securityDao;
	}
	
	
	@Override
	public boolean listen(Event event) {
		if (event instanceof EvictCacheServiceEvent)
			return true;
		return false;
	}

	@Override
	public void onEvent(Event event) {
		if (event instanceof EvictCacheServiceEvent) {
			default_groups.clear();
			group_Librarys.clear();
		}
	}

	@Override
	public Set<String> getReservedUserNames() {
		return reserverdNames;
	}

	
	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(com.novamens.service.SecurityService.class).getSessionUser();
	}
	
	protected User getUser(Person person) {
		return (person.getProfile(UserProfile.class)).getUser(); 
	}

	protected Index getIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
	}
	
	protected void propagateUserToPerson(User user) {

		UserProfile profile = getContentDao().findUserProfileByUser(user);
		if (profile!=null) {
			Person person = ((com.novamens.kbee.content.user.KbeeUserProfile) profile).getPerson();
			if (user.isEnabled())				person.setState(ObjectState.ENABLED);
			else if (user.isArchived())			person.setState(ObjectState.ARCHIVED);
			else if (user.isDeleted())			person.setState(ObjectState.DELETED);

			getContentDao().save(person);
			touch(person);
		}
	}

	private void touch(Person person) throws ContentMgmtException {
		long s=System.currentTimeMillis();
		if (!(person instanceof PersonMember)) {
			List<DataSetMember> members = getContentDao().findMembersByEntity(person);
			if (members.size()==1) {
				if (members.get(0) instanceof PersonMember) {
					ObjectState state = person.getState();
					person = (PersonMember) members.get(0);
					person.setState(state);
				}
			}
		}
		person.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		person.setLastModifiedUser(getSessionUser());
		getContentDao().save(person);
		logger.error(System.currentTimeMillis()-s + " ms");
		
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private String getDefaultTimeZone() {
		if (default_time_zone==null) {
			synchronized(this) {
				default_time_zone = getContentDao().findSystemParameterValueByKey("timezone.default", "US/Central");
			}
		}
		return default_time_zone;
	}

	private ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	


	
}