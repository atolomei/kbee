package com.novamens.dao;

import java.io.Serializable;
import java.util.Set;

import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.Group;

public interface SecurityDao extends Dao {
	
	public void save(Group group);
	public void save(User user);
	public void save(Acl acl);
	
	public User findUserById(Long id);
	public User findUserByName(String name);
	public Acl findAclById(Serializable id);
	public Group findGroupById(Serializable id);
	public Principal findPrincipalById(Serializable id);
	public Set<Principal> getDomainAdminUsers(String domain_id);
	public Set<Principal> getDomainSupportUsers(String domain_id);
}