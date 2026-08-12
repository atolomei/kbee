package com.novamens.solr.indexer.iql;

import com.novamens.beans.BeansService;
import com.novamens.content.entity.Person;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.iql.CalculatedPredicate;
import com.novamens.service.ServiceLocator;

public class SolrRolePredicate extends SolrAbstractPredicate implements CalculatedPredicate {
	
	@Override
	public boolean isInformatioModel() {
		return true;
	}
	
	public boolean isCanonical() {
		return false;
	}
	
	public String getCode(String argument) {
		
		Role role = getRole(argument);
		
		String code = "rolemember:" + (role!=null ? String.valueOf(role.getId()) : "x");
	
		return code;
	}
	
	public boolean evaluate(Object object, Object argument) {
		if (!(object instanceof Person)) return false;
		
		boolean evaluation = false;
		
		return evaluation;
	}
	
	private Role getRole(String rolename) {
		for (Role role : getSecurityDao().getRoles(getDomain())) {
			if (rolename.toLowerCase().equals(role.getAlias().toLowerCase())) {
				return role;
			}
		}
		return null;
	}
	
	private ContentSecurityDao getSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	private Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
