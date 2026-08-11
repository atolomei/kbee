package com.novamens.kbee.content.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.content.model.ObjectId;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserService;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class KbeeManagedAccessService extends KbeeAccessService {
	
	final boolean is_domain_admin = 
		ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_security_admin = 
		ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
	final boolean is_dataset_values_read = 
		ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_READ.getId());

	public KbeeManagedAccessService(DataSet dataset) {
		super(dataset);
	}
	
	public KbeeManagedAccessService(ClassifierTemplate template) {
		super(template);
	}
	
	public boolean isReadable(DataSetMember value) {
		
		if (ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_READ.getId())) {
			return true;
		}
		
		for (UserRole role : ServiceLocator.getService(UserService.class).getSessionUserProfile().getRoles()) {
			if (role.getRole().isEntity()) {
				if (value.equals(role.getEntity())) {
					return true;
				}
			}
		}
		
		for (UserRole role : ServiceLocator.getService(UserService.class).getSessionUserProfile().getRoles()) {
			if (role.getRole().isEntity()) {
				for (EntityMember refered : getReferences(role.getEntity())) {
					if (value.equals(refered)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	protected String getStatement(String pattern, Classificable object, Map<String, Object> parameters) {
		String statement = super.getStatement(pattern, object, parameters);
		
		if (!is_domain_admin && !is_security_admin && !is_dataset_values_read) {
			String entitiesstatement = "";
			if (ServiceLocator.getService(UserService.class).getSessionUserProfile()!=null)
			for (UserRole role : ServiceLocator.getService(UserService.class).getSessionUserProfile().getRoles()) {
				if (role.getRole().isEntity()) {
					EntityRole entityRole = (EntityRole)reload(role.getRole());
					EntityMember entity = role.getEntity();
					// role manage area
					if (entityRole.manage(getDataSets().get(0))) {
						entitiesstatement +=  "".equals(entitiesstatement) ? "(" : " OR ";
						String id = (new ObjectId(entity)).toString();
						if (!entitiesstatement.contains(id))
							entitiesstatement += "id:"+id;
					}
					else {
						if (entityRole.manage(entity.getDataSet())) {
							for (EntityMember refered : getReferences(entity)) {
								String id = (new ObjectId(refered)).toString();
								if (!entitiesstatement.contains(id)) {
									entitiesstatement +=  "".equals(entitiesstatement) ? "(" : " OR ";
									entitiesstatement += "id:"+id;
								}	
							}
						}
					}
				}
			}
			if (!"".equals(entitiesstatement)) {
				statement += " AND " + entitiesstatement + ")";
			}
			else {
				// si no tiene roles no tiene acceso
				statement += " AND id:X";
			}
		}
		
		return statement;
	}
	
	private List<EntityMember> getReferences(EntityMember entity) {
		List<EntityMember> references = new ArrayList<>();
		for (Classification classification : entity.getClassification()) {
			if (classification!=null) {
				for (DataSet dataset : getDataSets()) {
					if (dataset.equals(classification.getClassifier().getDataSet())) {
						references.add((EntityMember)classification.getDataSetMember());
					}
				}
			}
		}
		return references;
		
	}
	
	private Role reload(Role role) {
		return (Role)getContentDao().reload(role);
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
}