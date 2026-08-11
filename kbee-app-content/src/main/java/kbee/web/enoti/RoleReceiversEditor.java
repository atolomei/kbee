package kbee.web.enoti;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.model.IModel;

import com.novamens.beans.BeansService;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;

import kbee.web.form.RelationEditor;

@SuppressWarnings("serial")
public class RoleReceiversEditor<T> extends RelationEditor<T, Role> {	
	private static final long serialVersionUID = 1L;
	
	static Logger logger = LogManager.getLogger(ReceiversEditor.class.getName());

	public RoleReceiversEditor() {
		super("roleReceivers");
	}

	public RoleReceiversEditor(String id) {
		super(id);
	}

	public List<Role> getRoles() {
		List<Role> allroles =  getSecurityDao().getRoles(getDomain());
		List<Role> roles = new ArrayList<Role>();

		for (Role role : allroles) {
			boolean found = false;
			for (IModel<Role> model : getValues()) {
				if (role.equals(model.getObject())) {
					found = true;
					break;
				}
			}
			if (!found) roles.add(role);
		}
		
		Collections.sort(roles, new Comparator<Role>() {
			@Override
			public int compare(Role a, Role b) {
				try {
					if (a.getName()!=null && b.getName()!=null)
						return a.getName().trim().toLowerCase().compareTo(b.getName().trim().toLowerCase());
					return 0;
				} 
				catch (Exception e)  {
					logger.error(e);
					return 0;
				}
			}
		}); 
		
		return roles;
	}
	
	@Override
	public boolean ordered() {
		return true;
	}

	@Override
	protected Property<?> getKey() {
		return new Property<Role>() {
			public String getName() {
				return "roles";
			}
			public boolean isSelectable() {
				return true;
			}
			public List<Role> getChoices() {
				return getRoles();
			}
		};
	}
	
//	private Domain getDomain() {
//		return (Domain)ServiceLocator.getService(UserService.class).getDomain();
//	}
	
	private ContentSecurityDao getSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
}
