package com.novamens.kbee.content.multidimensional;

import com.novamens.beans.BeansService;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.Role;
import com.novamens.service.ServiceLocator;

public class RoleFacet extends HierarchicalFacet {
	private static final long serialVersionUID = 1L;

	public RoleFacet() {
	}
	
	protected String getDisplayName(String id) {
		Role role = (Role)getSecurityDao().findRoleById(Long.valueOf(id));
		if (role== null) return "name err";
		return role.getDisplayName();
	}
	
	protected ContentSecurityDao  getSecurityDao() {
		return	(ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
}
