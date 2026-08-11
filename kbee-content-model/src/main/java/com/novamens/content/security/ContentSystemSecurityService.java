package com.novamens.content.security;

import java.util.List;
import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.SecurityRule;
import com.novamens.content.model.EntityMember;
import com.novamens.content.model.SecuredMember;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.Permission;
import com.novamens.service.SystemService;

public interface ContentSystemSecurityService extends SystemService {
	public boolean isReadable(Content content);
	public boolean isReadable(Content content, Resource resource);
	public boolean isWriteable(Content content);
	public boolean isPublishable(Content content);
	public boolean isDeleteable(Content content);
	public boolean isTakeable(Content content);
	public boolean isMonitorable(Content content);
	public boolean isTerminable(Content content);
	public boolean isPrivateEnabled(Content content);
	
	public boolean isAuditTrailReadable(Content content);
	
	public Acl getAcl(Content content);
	public List<SecurityRule> getRules(Content content);
	public List<Principal> getEnabledPrincipals(Content content, Permission permission);
	public List<Principal> getTakers(Content content);
	public List<Principal> getMonitors(Content content);
	public List<Principal> getReaders(Content content);
	
	public void onUpdate(IQLRule rule);
	public void onUpdate(User user);
	
	public Acl getAcl(SecuredMember member);
	public boolean isWriteable(SecuredMember member, User user);
	public boolean isWriteable(SecuredMember member);
	public boolean hasPermission(SecuredMember member, Permission permission);
	
	public boolean isReadable(Content content, User user);
	public boolean isWriteable(Content content, User user);
	public boolean isTakeable(Content content, User user);
	public boolean isPrivateEnabled(Content content, User user);
	
	public Group getGroup(EntityMember entity, Role role); 
}