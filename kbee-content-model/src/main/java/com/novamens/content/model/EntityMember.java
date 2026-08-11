package com.novamens.content.model;

import com.novamens.content.entity.Person;
import com.novamens.content.security.Role;
import com.novamens.security.acl.Group;

public interface EntityMember extends DataSetMember {
	public void setRole(Role role, Person person);
	public void removeRole(Role role, Person person);
	public Group getGroup();
}
