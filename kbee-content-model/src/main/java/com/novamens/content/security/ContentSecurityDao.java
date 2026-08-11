package com.novamens.content.security;


import java.io.Serializable;
import java.util.List;

import com.novamens.content.base.SecurityRule;
import com.novamens.content.base.SiteIQLRule;
import com.novamens.content.model.EntityMember;
import com.novamens.content.model.EntitySet;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.dao.Dao;
import com.novamens.dom.Domain;

import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.Group;

public interface ContentSecurityDao  extends Dao {
	public Acl findAclById(Serializable id);
	public void save(Acl acl);

	public void save(User user);
	public void save(UserProfile user);

	public IQLRule findRuleById(Serializable id);

	public List<SiteIQLRule> findRuleByRelatedObjectId(Serializable id);

	public List<IQLRule> getRules(Domain domain);
	public void save(IQLRule rule);

	public void delete(IQLRule rule);
	
	public Group findGroupById(Long id);
	public Group findGroupByName(String name);
	public Group findGroupByName(String name, Domain domain);
	
	public void save(Group group);
	public void delete(Group group);
	
	public List<Group> getGroups();

	public List<Group> getCanonicalGroups(Domain domain);
	public List<String> canonicalGroupsMissing(Domain domain);
	public List<Group> getGroups(Domain domain);
	
	public void save(User user, User sessionUser);
	public List<SecurityRule> getRules(User user);
	
	/**
	 * Role
	 * 
	 * DomainRole
	 * EntityRole
	 */
	public void save(Role role);
	public void delete(Role role);
	
	// get Roles
	
	public List<Role> getRoles(Domain domain);
	public List<Role> getCanonicalRoles(Domain domain);
	public List<Role> getRolesByEntitySet(EntitySet dataset);
	public boolean isDeletable(Role role);
	public Role findRoleById(Long id);
	
	
	// get List UserRole
	/**
	 * 
	 * @param role
	 * @param order
	 * values can be: null, "name"
	 * 
	 */
	public List<UserRole> findUserRolesByRole(Role role, String order);
	public List<UserRole> findUserRolesByRole(Role role);
	
	public List<UserRole> findUserRolesByEntityMember(EntityMember member);
	
		
	public List<UserRole> findUserRolesByEntityMember(Role role, EntityMember member);
	public List<Role> getAPIAssignableRoles(Domain domain);
	
	// returns all (enabled and other states)
	public List<Role> getDomainRoles(Domain domain);
	
	public EntityRole findEntityRoleById(Long id);
	public DomainRole findGeneralRoleById(Long id);
	public DomainRole findRoleByName(String name, Serializable domainid);
	public long getTotalMembers(Role role);
	
	// Property Manager (Creek) [GS]
	// Property Auditor (Creek) [GS, AT]
	
	// public List<UserRole> findUserRolesByEntitySet(EntitySet dataset);
	
	
	
	
	
	
	
	
}
