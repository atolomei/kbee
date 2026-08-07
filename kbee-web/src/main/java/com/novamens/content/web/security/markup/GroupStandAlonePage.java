package com.novamens.content.web.security.markup;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.Group;

import kbee.web.nav.TabNavigationBar;
import kbee.web.page.AbstractApplicationPage;

@SuppressWarnings("serial")
public class GroupStandAlonePage extends AbstractApplicationPage<Group> {
	private static final long serialVersionUID = 1L;
	
	
	public GroupStandAlonePage(IModel<Group> model) {
		super(model);
		
		//add(new TabNavigationBar<Group>("navigation"));
		setMenu(new InvisiblePanel("menu"));
		
		getPageParameters().set("id", model.getObject().getId().toString());
		
		
		setPageTitle( new Model<String>("Group. " + model.getObject().getName()));
		
		GroupMainPanel editor = new GroupMainPanel(model, true, false) {
			@Override
			protected void onClose(AjaxRequestTarget target) {
				((TabNavigationBar<?>)GroupStandAlonePage.this.get("navigation")).onReturn(target);
			}
		};
		editor.setEditionEnabled(false);
		add(editor);

	}
	
	 
}
