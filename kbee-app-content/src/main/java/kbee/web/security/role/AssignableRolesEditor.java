package kbee.web.security.role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.service.ServiceLocator;

import kbee.web.form.RelationEditor;

@SuppressWarnings("serial")
public class AssignableRolesEditor extends RelationEditor<Role, Role> {
	private static final long serialVersionUID = 1L;
	
	public AssignableRolesEditor() {
		super("assignableRoles");
	}
		
	public AssignableRolesEditor(String id) {
		super(id, "assignableRoles");
	}
	
	public List<Role> getRoles() {
		
		List<Role> roles = new ArrayList<Role>(); 
				
		for (Role role : getSecurityDao().getRoles(getDomain())) {
			if (role instanceof EntityRole) {
				roles.add(role);
			}
		}
		
		Collections.sort(roles, new Comparator<Role>() {
			@Override
			public int compare(Role a, Role b) {
				try {
					String na = a.getDisplayName().trim().toLowerCase();
					String nb = b.getDisplayName().trim().toLowerCase();
					return na.compareTo(nb);
				} 
				catch (Exception e) {
					return 0;
				}
			}
		}); 
		
		return roles;
	}
	
	@Override
	protected Property<?> getKey() {
		return new Property<Role>() {
			public String getName() {
 				return "role";
			}
			public List<Role> getChoices() {
				return getRoles();
			}
		};
	}
	
	@Override	
	protected String getStringValue(Object value) {
		return ((EntityRole)value).getDisplayName() + "@"+ ((EntityRole)value).getClassifier().getDisplayName();
	}
	
//	private Domain getDomain() {
//		return ServiceLocator.getService(UserService.class).getDomain();
//	}
	
	private ContentSecurityDao getSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
}
