package kbee.web.security.role;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.security.Role;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.nav.RolesBC;
import kbee.web.nav.SecurityDropDownMenuBC;
import kbee.web.object.TitleHeaderPanel;

public class RoleHeaderPanel extends TitleHeaderPanel<Role> {
	private static final long serialVersionUID = 1L;

	IModel<String> icon = new Model<String>("fal fa-address-card");

	public RoleHeaderPanel(IModel<Role> model) {
		super("role-panel", model);
		MenuBreadCrumbPanel<Void>  bc = new MenuBreadCrumbPanel<Void>();
		bc.addElement(new SecurityDropDownMenuBC());
		bc.addElement(new RolesBC());
		bc.addElement(new BCElement(new Model<String>(model.getObject().getDisplayName())));
		setBreadCrumbPanel(bc);
	}
	
	protected IModel<String> getGlyphicon() {
		return icon; 
	}
} 
