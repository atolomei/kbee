package com.novamens.content.web.security.markup;


import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.security.acl.Group;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.nav.GroupsBC;
import kbee.web.nav.SecurityDropDownMenuBC;
import kbee.web.object.TitleHeaderPanel;

public class GroupHeaderPanel extends TitleHeaderPanel<Group> {
	private static final long serialVersionUID = 1L;

	IModel<String> icon = new Model<String>("far fa-users");

	public GroupHeaderPanel(IModel<Group> model) {
		super("group-panel", model);
		MenuBreadCrumbPanel  bc = new MenuBreadCrumbPanel();
		bc.addElement(new SecurityDropDownMenuBC());
		bc.addElement(new GroupsBC());
		bc.addElement(new BCElement(new Model<String>(model.getObject().getDisplayName())));
		setBreadCrumbPanel(bc);
	}
	
	protected IModel<String> getGlyphicon() {
		return icon; 
	}
}
