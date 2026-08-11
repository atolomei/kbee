package kbee.web.security.role;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.wicket.util.BCElement;



public class RoleBC extends BCElement {
			
	private static final long serialVersionUID = 1L;
	
	IModel<Role> model;
	
	public RoleBC(IModel<Role> model) {
		super();
		this.model=model;
	}
	
	@Override
	public IModel<String> getLabel() {
		
	 if (model.getObject() instanceof EntityRole && ((EntityRole) model.getObject()).getClassifier()!=null)
			return new Model<String>(model.getObject().getName() + " <span class=\"ago\">(" + ((EntityRole) model.getObject()).getClassifier().getDisplayName() +")</span>");
		else
			return new Model<String>(model.getObject().getName());
	}
	
	@Override
	public void onDetach() {
		this.model.detach();
		super.onDetach();
	}
	
	@Override
	public void onClick() {
		setResponsePage(new RolePage(this.model));
	}
}
