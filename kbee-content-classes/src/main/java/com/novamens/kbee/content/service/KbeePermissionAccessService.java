package com.novamens.kbee.content.service;

import java.util.Map;

import com.novamens.content.model.Classificable;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.security.acl.Permission;

import kbee.query.QueryHelpher;
import kbee.util.logging.Logger;

public class KbeePermissionAccessService extends KbeeAccessService {
	
	private static Logger logger = Logger.getLogger(KbeePermissionAccessService.class.getName());
	
	private Permission permission;
	
	public KbeePermissionAccessService(ClassifierTemplate template, Permission permission) {
		super(template);
		this.permission = permission;
	}
	
	public boolean isReadable(DataSetMember value) {
		return true;
	}
	
	@Override
	protected String getStatement(String pattern, Classificable object, Map<String, Object> parameters) {
		
		String statement = super.getStatement(pattern, object, parameters);
		
		try {
			String securityTerm = QueryHelpher.buildSecurityTerm(permission);
			if (securityTerm!=null && !"".equals(securityTerm)) {
				statement += " AND ("+securityTerm +")";
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
		
		return statement;
	}
}