package com.novamens.kbee.content.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.hibernate.SessionFactory;
import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.properties.ObjectPropertyService;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.dom.ObjectID;
import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;

public class UpdateRoleGroupsCommandServiceRequest extends AbstractServiceRequest {
				
	private static final long serialVersionUID = 1L;
	
	private Long roleId;
	private transient KbeeAbstractRole role = null;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UpdateRoleGroupsCommandServiceRequest.class.getName());
	
	public UpdateRoleGroupsCommandServiceRequest(Role role) {
		try {
			roleId = ((KbeeAbstractRole)role).getId();
			setObjectID(new ObjectID(role).toString());
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	
	public void execute() {
		Assert.isTrue(getRole()!=null, "no role");
		for (UserRole userrole : getUsers()) {
			updateGroups(userrole.getUser(), getRole());
		}
		setCurrentGroups(getRole());
	}
	
	private List<UserRole> getUsers() {
		return getSecurityDao().findUserRolesByRole(getRole());
	}
	
	public KbeeAbstractRole getRole() {
		if (role==null) {
			role = (KbeeAbstractRole)getSecurityDao().findRoleById(roleId);
			((SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory")).getCurrentSession().evict(role);
			role = (KbeeAbstractRole)getSecurityDao().findRoleById(roleId);
		}
		return role;
	}
	
	protected void updateGroups(User user, KbeeAbstractRole role) {
		Set<Group> groups = new HashSet<Group>();  
		groups.addAll(user.getGroups());
		groups.removeAll(getGroupsRemoved(role));
		UserProfile userprofile = getUserProfile(user);
		for (UserRole actualrole : userprofile.getRoles()) {
			groups.addAll(((KbeeAbstractRole)actualrole.getRole()).getGroups());
		}
		user.setGroups(groups);
		getSecurityDao().save(user);
	}
	
	private UserProfile getUserProfile(User user) {
		UserProfile profile = getContentDao().findUserProfileByUser(user);
		return profile;
	}
	
	private List<Group> getGroupsRemoved(KbeeAbstractRole role) {
		List<Group> groups = new ArrayList<Group>();
		for (Long groupid : getPreviuosGroups(role)) {
			boolean found = false;
			for (Group group : role.getGroups()) {
				if (group.getId().equals(groupid)) {
					found = true;
					break;
				}
			}
			if (!found) {
				Group group = getSecurityDao().findGroupById(groupid);
				if (group!=null) {
					groups.add(group);
				}
			}
		}
		return groups;
	}
	
	private List<Long> getPreviuosGroups(KbeeAbstractRole role) {
		List<Long> groups = new ArrayList<Long>();
		String rolesstring = (String)role.getService(ObjectPropertyService.class).getProperty("groups");
		if (rolesstring==null) return groups;
		StringTokenizer tokenizer = new StringTokenizer(rolesstring, ",");
		while (tokenizer.hasMoreTokens()) {
			String idstr = tokenizer.nextToken();
			try {
				groups.add(Long.valueOf(idstr.trim()));
			}
			catch (NumberFormatException e) {
			}
		}
		return groups;
	}
	
	private void setCurrentGroups(KbeeAbstractRole role) {
		String value = "";
		for (Group  group : role.getGroups()) {
			if (!"".equals(value)) value +=", ";
			value += String.valueOf(group.getId());
		}
		role.getService(ObjectPropertyService.class).setProperty("groups", value);
	}
	
	private ContentSecurityDao getSecurityDao() {
		return	(ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	private ContentDao getContentDao() {
		return	(ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
