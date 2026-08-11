package com.novamens.content.security;

import java.util.List;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.EntityMember;
import com.novamens.content.user.UserRole;
import com.novamens.service.ObjectService;

/** 
 * Roles of a Person
 */
public interface RolesService extends ObjectService {
	public void update(List<UserRole> roles);
	public void add(UserRole role);
	public void remove(UserRole role);
	public boolean isAdministrator(EntityMember entity);
	public boolean isAdministrator(DataSet dataset);
}