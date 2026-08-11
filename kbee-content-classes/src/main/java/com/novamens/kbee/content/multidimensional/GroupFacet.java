package com.novamens.kbee.content.multidimensional;

import com.novamens.beans.BeansService;
import com.novamens.dao.SecurityDao;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;

public class GroupFacet extends HierarchicalFacet {
	private static final long serialVersionUID = 1L;

	public GroupFacet() {
	}
	
	protected String getDisplayName(String id) {
		Group group = (Group)getSecurityDao().findGroupById(Long.valueOf(id));
		if (group== null) return "name err";
		return group.getName();
	}
	
	protected SecurityDao  getSecurityDao() {
		return	(SecurityDao)ServiceLocator.getService(BeansService.class).getBean("securityDao");
	}
}
