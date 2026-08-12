package kbee.aerolineas.web.security;

import com.novamens.content.user.UserRole;

public class UserRolesEditor extends kbee.web.security.user.UserRolesEditor {
	private static final long serialVersionUID = 1L;

	public UserRolesEditor(String id) {
		super(id);
	}
	
	protected String getLabel(UserRole userRole) {
		StringBuilder label = new StringBuilder();
		
		if (userRole.getEntity()==null || 
			"area".equals(userRole.getEntity().getDataSet().getAlias())) {
			label.append(userRole.getRole().getName());
		}

		if (userRole.getEntity()!=null) 
			label.append("  <span class=\"iql-group-start\"> <span class=\"iql-value\">" + 
				getDisplayName(userRole.getEntity())+
				"</span><span class=\"iql-group-end\">  </span>");
		
		return label.toString();
	}
}
