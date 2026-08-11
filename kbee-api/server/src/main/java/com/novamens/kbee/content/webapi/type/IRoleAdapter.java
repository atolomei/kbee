package com.novamens.kbee.content.webapi.type;

import com.novamens.content.model.Classifier;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.indexer.iql.Expression;
import com.novamens.indexer.iql.IqlService;
import com.novamens.kbee.content.security.KbeeAbstractRole;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.Permission;

import kbee.api.model.ApiProxy;
import kbee.api.model.IRole;

public class IRoleAdapter implements Adapter<Role, IRole> {
	
	public IRoleAdapter() {
	}
	
	public IRole adapt(Role role) {
		IRole irole = new IRole();
		
		irole.setId(String.valueOf(((KbeeAbstractRole)role).getId()));
		irole.setName(role.getName());
		irole.setAlias(role.getAlias());
		irole.setDisplayName(role.getDisplayName());
		irole.setDescription(role.getDescription());
		
		if (role.getCondition()!=null && !"".equals(role.getCondition().trim())) {
			IqlService iqlservice = role.getDomain().getService(IqlService.class);
			Expression expression = iqlservice.getExpression(role.getCondition());
			irole.setCondition(expression.toString());
		}		
		
		if (((KbeeAbstractRole)role).getGroup()!=null) {
			irole.setGroup(new IGroupProxy(((KbeeAbstractRole)role).getGroup()));
		}
		
		irole.setState(role.getState().name());
		irole.setDomain(((KbeeAbstractRole)role).getDomain().getName());
		irole.setCanonical(role.isCanonical());
		irole.setLastModifiedUser(new ApiUserProxy(((KbeeAbstractRole)role).getLastModifiedUser()));
		
		for (Permission permission : ((KbeeAbstractRole)role).getPermissions()) {
			irole.addPermission(permission.toString());
		}
		
		for (Group group : ((KbeeAbstractRole)role).getGroups()) {
			irole.addGroup(new IGroupProxy(group));
		}

		if (role instanceof EntityRole) {
			irole.setType("entity");
			if(((EntityRole)role).getClassifier()!=null) {
				Classifier scope = ((EntityRole)role).getClassifier();
				irole.setScope(new ApiProxy(String.valueOf(scope.getId()), scope.getName(), UriHelper.getUri(scope), "classifier"));
			}
		}
		else {
			irole.setType("domain");
		}
		
		irole.setLastModifiedDate(((KbeeAbstractRole)role).getLastModifiedOffsetDateTime());
		
		return irole;	
	}
}