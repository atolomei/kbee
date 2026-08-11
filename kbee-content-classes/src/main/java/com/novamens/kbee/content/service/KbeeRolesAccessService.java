package com.novamens.kbee.content.service;

import java.util.Map;

import com.novamens.content.model.Classificable;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.content.model.ObjectId;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserService;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class KbeeRolesAccessService extends KbeeAccessService {
	
	public KbeeRolesAccessService(DataSet dataset) {
		super(dataset);
	}
	
	public KbeeRolesAccessService(ClassifierTemplate template) {
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
		
		return false;
	}
	
	// filtra las entidades sobre las que el usuario cumple algun rol
	protected String getStatement(String pattern, Classificable object, Map<String, Object> parameters) {
		String statement = super.getStatement(pattern, object, parameters);
		
		if (!ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_READ.getId())) {
			String entitiesstatement = "";
			if (ServiceLocator.getService(UserService.class).getSessionUserProfile()!=null)
			for (UserRole role : ServiceLocator.getService(UserService.class).getSessionUserProfile().getRoles()) {
				if (role.getRole().isEntity()) {
					EntityMember entity = role.getEntity();
					if (isSource(entity.getDataSet())) {
					//if (entity.getDataSet().equals(getDataSet())) {
						entitiesstatement +=  "".equals(entitiesstatement) ? "(" : " OR ";
						String id = (new ObjectId(entity)).toString();
						if (!entitiesstatement.contains(id))
							entitiesstatement += "id:"+id;
					}
//					if (isManager(role.getRole())) {
//					//if (role.getRole().manage(getDataSet())) {
//						entitiesstatement +=  "".equals(entitiesstatement) ? "(" : " OR ";
//						String id = String.valueOf(entity.getId());
//						Classifier classifier = ((EntityRole)reload(role.getRole())).getClassifier();
//						entitiesstatement += classifier.getUniqueName() + "member:"+ id;
//					}
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
	
	private boolean isSource(DataSet dataSet) {
		for (DataSet sourceDataSet : getDataSets()) {
			if (dataSet.equals(sourceDataSet)) return true;
		}
		return false;
	}
	
//	private boolean isManager(Role role) {
//		for (DataSet dataSet : getDataSets()) {
//			if (role.manage(dataSet)) return true;
//		}
//		return false;
//	}
//	
//	private Role reload(Role role) {
//		return (Role)getContentDao().reload(role);
//	}
	
//	private ContentDao getContentDao() {
//		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
}
