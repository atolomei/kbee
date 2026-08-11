package com.novamens.content.service;

import java.util.List;

import com.novamens.content.model.EntityMember;
import com.novamens.content.user.UserRole;
import com.novamens.service.ObjectService;

public interface UserSecurityService extends ObjectService {
	public List<UserRole> getRoles();
	public boolean isMember(String roleName);
	public boolean hasRole(String roleName, EntityMember entity);
}