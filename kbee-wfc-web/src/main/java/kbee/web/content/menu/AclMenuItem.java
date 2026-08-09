package kbee.web.content.menu;


import com.novamens.content.base.Content;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.security.AclPage;

public class AclMenuItem<T extends Content> extends MenuItemPanelV5<T> {
	private static final long serialVersionUID = 1L;
	
	public AclMenuItem(String id) {
		super(id);
	}
	
	public void onClick() {
		ObjectModel<Content> model = new ObjectModel<Content>((Content)getModelObject());
		setResponsePage(new AclPage(model));
	}
	
	@Override 
	public String getLabel() {
		return getItemLabelString("contextmenu.acl");
	}
	
	@Override 
	public String getTarget() {
		return "_blank";
	}
	
	@Override 
	public boolean isEnabled() {
		return isWriteable();
	}
	
	@Override 
	public boolean isVisible() {
		return isWriteable();
	}
	
	public Content getContent() {
		return (Content)getModel().getObject();
	}
	
	protected boolean isWriteable() {
		return ServiceLocator
			.getService(ContentSystemSecurityService.class)
			.isDeleteable(getContent());
	}
};