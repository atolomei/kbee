package com.novamens.kbee.content.webapi.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import com.novamens.service.WebSessionService;

import kbee.api.model.ApiProxy;
import kbee.api.model.ITransaction;
import kbee.api.model.ApiUser;
import kbee.api.model.IUserRole;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.entity.Person;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.content.model.PersonMember;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.user.KbeeUserProfile;
import com.novamens.kbee.content.user.KbeeUserRole;
import com.novamens.kbee.content.webapi.type.UriHelper;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class UserUpdateHandler extends ClassificableUpdateHandler {
	
	static private Logger logger = LogManager.getLogger(UserUpdateHandler.class.getName());

	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Transactional
	public ITransaction update(ApiUser user) {
		try {
			Domain domain = getDomain(user);
			
			if (domain == null) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.DOMAIN_NOT_FOUND);
			}
			
			su(domain);
						
			// // System.out.println(getDomain().getName());
			
			Person person = getPerson(user);
			
			List<String> updates = new ArrayList<String>();
 			
			updates.addAll(update(person, user));
			
			updates.addAll(setRoles(person, user));
			
			updates.addAll(setAttributes((PersonMember)person, user));
						
			if (updates.isEmpty()) {
				throw new ApiException(HttpStatus.NOT_MODIFIED, ApiError.NOT_MODIFIED);
			}
			
			getContentDao().flush();
			
			ServiceLocator.getService(SecurityContentMgmtService.class).update(person, updates);
			
			ITransaction transaction  = getTransaction(getProxy(user));
			
			return transaction;
		}
		catch (ApiException e) {
			throw e;
		}
		catch (ContentMgmtException e) {
			if (logger.isDebugEnabled()) {
				logger.error("updating user", e);
			}
			logger.error(e);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
		catch (AuthenticationException e) {
			if (logger.isDebugEnabled()) {
				logger.error("updating user", e);
			}
			logger.error(e);
			throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED, e.getMessage());
		}
		catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.error("updating user", e);
			}
			logger.error(e);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}

	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Transactional
	public ITransaction enable(ApiUser iuser) {
		try {
			Domain domain = getDomain(iuser);
			
			if (domain == null) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.DOMAIN_NOT_FOUND);
			}
			
			su(domain);
						
			// // System.out.println(getDomain().getName());
			
			Person person = getPerson(iuser);
			
			ServiceLocator.getService(SecurityContentMgmtService.class).enable(getUser(person));
			
			ITransaction transaction = getTransaction(getProxy(iuser));
			
			return transaction;
		}
		catch (ApiException e) {
			throw e;
		}
		catch (ContentMgmtException e) {
			if (logger.isDebugEnabled()) {
				logger.error("updating user", e);
			}
			logger.error(e);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
		catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.error("updating user", e);
			}
			logger.error(e);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Transactional
	public ITransaction disable(ApiUser iuser) {
		try {
			Domain domain = getDomain(iuser);
			
			if (domain == null) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.DOMAIN_NOT_FOUND);
			}
			
			su(domain);
						
			// // System.out.println(getDomain().getName());
			
			Person person = getPerson(iuser);
			
			ServiceLocator.getService(SecurityContentMgmtService.class).disable(getUser(person));
			
			ITransaction transaction = getTransaction(getProxy(iuser));
			
			return transaction;
		}
		catch (ApiException e) {
			throw e;
		}
		catch (ContentMgmtException e) {
			if (logger.isDebugEnabled()) {
				logger.error("updating user", e);
			}
			e.printStackTrace();
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
		catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.error("updating user", e);
			}
			e.printStackTrace();
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
	}

	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Override
	protected List<Classifier> getClassifiers(Classificable classificable){
		return ((DataSetMember)classificable).getDataSet().getClassifiers();
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Override
	protected List<AttributeTemplate> getAttributes(Classificable classificable) {
		return ((DataSetMember)classificable).getDataSet().getAttributes();
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected ApiProxy getProxy(ApiUser user) {
		return new ApiProxy(user.getDisplayName(), UriHelper.getUri(user));
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected Person getPerson(ApiUser user) throws ContentMgmtException {
		Person person = null;
		if (user.getId()!=null) {
			try {
				person = (PersonMember)getContentDao().findMemberById(Long.valueOf(user.getId()));
				if (person==null) {
					throw new ApiException(HttpStatus.NOT_FOUND, ApiError.USER_NOT_FOUND);
				}
			}
			catch (NumberFormatException e) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.USER_NOT_FOUND, e.getMessage());
			}
		}
		else {
			throw new ApiException(HttpStatus.NOT_FOUND, ApiError.USER_NOT_FOUND, "no id");
		}
		return person;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected User getUser(Person person) throws ContentMgmtException {
		UserProfile profile = person.getProfile(UserProfile.class);
		if (profile!=null) {
			User user = profile.getUser();
			if (user!=null) {
				return user;
			}
			else {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.USER_NOT_FOUND, "no user");
			}
		}
		else {
			throw new ApiException(HttpStatus.NOT_FOUND, ApiError.USER_NOT_FOUND, "no user");
		}
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected String getValidUserName(ApiUser user) {
		String username = user.getName();
		if (username.contains("@")) {
			if (!username.endsWith("@"+user.getDomain())) {
				throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.USER_INVALID_NAME);
			}
			else {
				username = username.replace("@"+user.getDomain(), "");
			}
		}
		if (!ServiceLocator.getService(SecurityService.class).validateName(username)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.USER_INVALID_NAME);
		}
		String fullname = username + "@" + user.getDomain();
		if (ServiceLocator.getService(SecurityService.class).findUserByUsername(fullname)!=null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.USER_ALREADY_EXIST);
		}
		return fullname;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected String getUserName(ApiUser user) {
		String username = user.getName();
		if (username.contains("@")) {
			if (!username.endsWith("@"+user.getDomain())) {
				throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.USER_INVALID_NAME);
			}
			else {
				username = username.replace("@"+user.getDomain(), "");
			}
		}
		String fullname = username + "@" + user.getDomain();
		return fullname;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected List<String> update(Person person, ApiUser iuser) {
		List<String> updates = new ArrayList<String>();
		
		User user = person.getProfile(UserProfile.class).getUser();
		
		if (user == null) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, "no user");
		}
		
		if (!equals(user.getName(), getUserName(iuser))) {
			((KbeeUser)user).setUserName(getValidUserName(iuser));
			updates.add("user name");
		}
		
		if (!equals(person.getLastName(), iuser.getLastName())) {
			person.setLastName(iuser.getLastName());
			updates.add("last name");
		}
		
		if (!equals(person.getFirstName(), iuser.getFirstName())) {
			person.setFirstName(iuser.getFirstName());
			updates.add("first name");
		}
		
		if (!equals(person.getEmail(), iuser.getEmail())) {
			person.setEmail(iuser.getEmail());
			updates.add("email");
		}
		
		if (!equals(person.getPhone(), iuser.getPhone())) {
			person.setPhone(iuser.getPhone());
			updates.add("phone");
		}
		
		if (!equals(user.getTimeZone(), iuser.getTimeZone())) {
			if (!validTimeZone(iuser.getTimeZone())) {
				throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.USER_INVALID_TIMEZONE);
			}
			user.setTimeZone(iuser.getTimeZone());
			updates.add("time zone");
		}	
		
		if (iuser.isEnabled()!=user.isEnabled()) {
			
			if (iuser.isEnabled())
				user.setStateEnabled();
			else
				user.setStateArchived();
					
			updates.add("state");
		}
		
		if (person instanceof PersonMember) {
			PersonMember member = (PersonMember)person; 
			if (!equals(member.getExternalId(), iuser.getExternalId()) ||
					(member.getExternalId()!=null && !member.getExternalId().startsWith("u"))) {
				String externalId = iuser.getExternalId();
				if (externalId!=null) {
					if (!externalId.startsWith("u")) externalId = "u"+externalId;
					DataSetMember other = getContentDao().findMemberByExternalId(externalId);
					if (other!=null && !other.equals(member)) {
						throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.USER_ALREADY_EXIST, "external id");
					}
				}
				member.setExternalId(externalId);
				updates.add("external id");
			}
		}

		if(!user.isEnabled()){
			ServiceLocator.getService(WebSessionService.class).expireUserSessions(user.getUserName());
		}


		return updates;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected List<String> setGroups(Person person, ApiUser iuser) {
		List<String> updates = new ArrayList<String>();
		
		User user = person.getProfile(UserProfile.class).getUser();
		
		if (user == null) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, "no user");
		}
		
		boolean  update = false;
		
		Set<Group> groups = new HashSet<Group>();
		for (ApiProxy groupproxy : iuser.getGroups()) {
			Group group = getGroup(groupproxy);
			if (group == null) {
				throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.GROUP_NOT_FOUND);
			}
			else {
				groups.add(group);
			}
		}
		
		if (user.getGroups().size()!=groups.size()) {
			update = true;
		}
		else {
			for (Group group1 : groups) {
				boolean found = false;
				for (Group group2 : user.getGroups()) {
					if (group2.equals(group1)) {
						found = true;
						break;
					}
				}
				if (!found) {
					update = true;
					break;
				}
			}
		}
		
		if (!update) {
			return updates;
		}
		
		updates.add("groups");
		
		user.setGroups(groups);
	
		return updates;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected List<String> setRoles(Person person, ApiUser iuser) {
		List<String> updates = new ArrayList<String>();
		
		UserProfile userprofile = person.getProfile(UserProfile.class);
		User user = userprofile.getUser();
		
		if (user == null) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, "no user");
		}
		
		boolean update = false;
		
		List<UserRole> roles = new ArrayList<UserRole>();
		for (IUserRole userRole : iuser.getRoles()) {
			Role role = getRole(userRole.getRole());
			EntityMember entity = null;
			if (role == null) {
				throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.ROLE_NOT_FOUND);
			}
			if (userRole.getEntity()!=null) {
				entity = getEntity(userRole.getEntity());
				if (entity==null) {
					throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.ENTITY_NOT_FOUND);
				}
			}
			else {
				if (role instanceof  EntityRole) {
					throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.ENTITY_NOT_FOUND);
				}
			}
			roles.add(new KbeeUserRole(role, user, entity));
		}
		
		if ((userprofile.getRoles()==null && roles.size()>0) || 
				userprofile.getRoles()!=null && userprofile.getRoles().size()!=roles.size()) {
			update = true;
		}
		else {
			for (UserRole role1 : roles) {
				boolean found = false;
				for (UserRole role2 : userprofile.getRoles()) {
					if (role1.equals(role2)) {
						found = true;
						break;
					}
				}
				if (!found) {
					update = true;
					break;
				}
			}
		}
		
		if (!update) {
			return updates;
		}
		
		updates.add("roles");
		
		((KbeeUserProfile)userprofile).setEntity(person);
		
		userprofile.setRoles(roles);
	
		return updates;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected boolean validTimeZone(String timeZone) {
		return Arrays.asList(TimeZone.getAvailableIDs()).contains(timeZone);
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected Group getGroup(ApiProxy proxy) {
		if (proxy==null) return null;
		String id = proxy.getId();
		if (id == null) {
			if (proxy.getHRef()==null)
				return null;
			String urlfrags[] = proxy.getHRef().split("/");
			id = urlfrags[urlfrags.length-1];
		}
		try {
			Group group = getSecurityDao().findGroupById(Long.valueOf(id));
			return group;
		}
		catch (NumberFormatException e) {
			return null;
		}
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected Role getRole(ApiProxy proxy) {
		if (proxy==null) return null;
		String id = proxy.getId();
		if (id == null) {
			if (proxy.getHRef()==null)
				return null;
			String urlfrags[] = proxy.getHRef().split("/");
			id = urlfrags[urlfrags.length-1];
		}
		try {
			Role role = getContentSecurityDao().findRoleById(Long.valueOf(id));
			return role;
		}
		catch (NumberFormatException e) {
			return null;
		}
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected EntityMember getEntity(ApiProxy proxy) {
		if (proxy==null) return null;
		String id = proxy.getId();
		if (id == null) {
			if (proxy.getHRef()==null)
				return null;
			String urlfrags[] = proxy.getHRef().split("/");
			id = urlfrags[urlfrags.length-1];
		}
		try {
			DataSetMember entity = getContentDao().findMemberById(Long.valueOf(id));
			if (entity!=null && !(entity instanceof EntityMember)) {
				entity = null;
			}
			return (EntityMember)entity;
		}
		catch (NumberFormatException e) {
			return null;
		}
	}


}