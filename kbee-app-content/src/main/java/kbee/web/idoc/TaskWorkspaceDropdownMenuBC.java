package kbee.web.idoc;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.security.User;
import com.novamens.wicket.util.BCElement;

import kbee.web.nav.DropDownMenuBC;

public class TaskWorkspaceDropdownMenuBC extends DropDownMenuBC<User> {

	private static final long serialVersionUID = 1L;
	
	IModel<User> model;
	
	public TaskWorkspaceDropdownMenuBC(IModel<User> model) {
			this("bc-menu-item", model);
	}

	public TaskWorkspaceDropdownMenuBC(String id, IModel<User> model) {
		super(id, model);
	
		BCElement owner = new BCElement( new Model<String>(getModel().getObject().getFirstLastName()));
		addElement(owner);
		
	}
	
	protected void addItems() {
		
		
		
	}
	
}
