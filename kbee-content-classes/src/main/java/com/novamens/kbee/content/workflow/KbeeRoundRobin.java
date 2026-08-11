package com.novamens.kbee.content.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.DomainService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.Permission;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.workflow.UserAssignationStrategy;
import com.novamens.workflow.WorkflowContext;

public class KbeeRoundRobin implements UserAssignationStrategy {
	private Permission permission, backupPermission;
	
	public User getUser(WorkflowContext context) {
		Assert.isInstanceOf(KbeeContext.class, context);
		Content content = ((KbeeContext)context).getContent();
		List<Principal> principals = getEnabledPrincipals(content);
		if (principals.isEmpty()) principals = getBackupPrincipals(content);
		User user = getUser(principals);
		return user;
	}
	
	public List<Principal> getEnabledPrincipals(WorkflowContext context) {
		Assert.isInstanceOf(KbeeContext.class, context);
		Content content = ((KbeeContext)context).getContent();
		List<Principal> principals = getEnabledPrincipals(content);
		return principals;
	}
	
	@Override
	public List<Permission> getPermissions() {
		List<Permission> permissions = new ArrayList<Permission>();
		permissions.add(getPermission());
		permissions.add(getBackupPermission());
		return permissions;
	}
	
	public Permission getPermission() {
		return permission;
	}
	
	public void setPermission(Permission permission) {
		this.permission = permission;
	}
	
	public Permission getBackupPermission() {
		return backupPermission;
	}
	
	public void setBackupPermission(Permission permission) {
		this.backupPermission = permission;
	}
	
	private List<Principal> getEnabledPrincipals(Content content) {
		return getEnabledPrincipals(content, getPermission());
	}
	
	private List<Principal> getBackupPrincipals(Content content) {
		if (getBackupPermission()==null) {
			return new ArrayList<Principal>();
		}
		else {
			return getEnabledPrincipals(content, getBackupPermission());
		}
	}
	
	private List<Principal> getEnabledPrincipals(Content content, Permission permission) {
		List<Principal> principals = ServiceLocator.getService(ContentSystemSecurityService.class).getEnabledPrincipals(content, permission);
		Collections.sort(principals, new Comparator<Principal>() {
			@Override
			public int compare(Principal a, Principal b) {
				return a.getName().compareTo(b.getName());
			}
		});
		return principals;
	}
	
	private User getUser(List<Principal> enabledPrincipals) {
		if (enabledPrincipals.isEmpty())
			return null;
		
		List<User> enabledUsers = new ArrayList<User>();
		for (Principal principal : enabledPrincipals) {
			if (principal instanceof Group) {
				Enumeration<? extends Principal> members = ((Group)principal).members();
				while (members.hasMoreElements()) {
					Principal member = members.nextElement();
					if (member instanceof User && ((User)member).isActive() && ((User)member).isEnabled()) {
						enabledUsers.add((User)member);
					}
				}
			}
			else {
				if (principal instanceof User) {
					enabledUsers.add((User)principal);
				}
			}
		}
		
		Collections.sort(enabledUsers, new Comparator<User>() {
			@Override
			public int compare(User a, User b) {
				try {
				return a.getDisplayName().compareTo(b.getDisplayName());
				} catch (Exception e) {
					return 0;
				}
			}
		});
		
		User nextuser = null;
		String id = getLastUserId(enabledPrincipals);
		if (id!=null) {
			boolean last = false;
			for (User user : enabledUsers) {
				if (id.equals(String.valueOf(user.getId()))) {
					last = true;
				}
				else {
					if (last) {
						nextuser = user;
						break;
					}
				}
			}
		}
		
		if (nextuser==null && !enabledUsers.isEmpty()) {
			nextuser = enabledUsers.get(0);
			setLastUserId(enabledPrincipals, String.valueOf(nextuser.getId()));
		}
		
		return nextuser;
	}
	
	private String getLastUserId(List<Principal> principals) {
		Domain domain = getDomain(principals);
		if (domain == null) return null;
		User wkuser = ServiceLocator.getService(SecurityService.class).findUserByUsername(DomainService.WORKFLOW_USER+"@"+domain.getName());
		String id = ((KbeeUser)wkuser).getService(PreferencesService.class).getValue("roundrobin", getKey(principals));
		return id;
	}
	
	private void setLastUserId(List<Principal> principals, String userid) {
		Domain domain = getDomain(principals);
		if (domain == null) return;
		User wkuser = ServiceLocator.getService(SecurityService.class).findUserByUsername(DomainService.WORKFLOW_USER+"@"+domain.getName());
		((KbeeUser)wkuser).getService(PreferencesService.class).setValue("roundrobin", getKey(principals), userid);
	}
	
	private String getKey(List<Principal> principals) {
		String key = "rr-"+getPermission().toString();
		for (Principal principal : principals) {
			if (principal instanceof Group) {
				key += "-" + ((Group) principal).getId();
			}
		}
		return key;
	}
	
	private Domain getDomain(List<Principal> principals) {
		for (Principal principal : principals) {
			if (principal instanceof Group) {
				if (principal instanceof DomainObject) {
					return ((DomainObject)principal).getDomain();
				}	
			}
		}
		return null;
	}
	
}
