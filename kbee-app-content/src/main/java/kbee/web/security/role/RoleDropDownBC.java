package kbee.web.security.role;

import com.novamens.beans.BeansService;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.RolesBC;
import kbee.web.nav.SeparatorBC;

public class RoleDropDownBC extends DropDownMenuBC<Void> {

	private static final long serialVersionUID = 1L;
	
	public RoleDropDownBC() {
		
		addElement(new RolesBC(), true);
		addElement(new RolesBC());
		addElement(new SeparatorBC());
		for (Role t: getContentSecurityDao().getRoles(getDomain())) {
			addElement( new RoleBC(new ObjectModel<Role> (t)));
		}
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	protected ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao) ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}

}
