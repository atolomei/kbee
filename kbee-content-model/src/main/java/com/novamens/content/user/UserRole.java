package com.novamens.content.user;

import java.util.Set;

import com.novamens.content.entity.Person;
import com.novamens.content.model.EntityMember;
import com.novamens.content.security.Role;
import com.novamens.security.User;
import com.novamens.security.acl.Group;

public interface UserRole {
	public Role getRole();
	public User getUser();
	public Person getPerson();
	public EntityMember getEntity();
	public Set<Group> getGroups();
	public String getDisplayName();
}
