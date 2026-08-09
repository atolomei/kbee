package com.novamens.kbee.wicket.markup.html.console.panel;

import org.apache.wicket.model.IModel;
import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListItem;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;

public class KbeeUserListMenuItemFactory<T> implements MenuItemFactory<T> {
												
	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeUserListMenuItemFactory.class.getName());

	private static final long serialVersionUID = 1L;
	
	private IModel<T> model;
	private IModel<UserList> list_model;
	
	private int version_match = UserListItem.NEWEST;

	
	public KbeeUserListMenuItemFactory(IModel<T> model,IModel<UserList> list_model, int versionMatch) {
		this.model=model;
		this.list_model=list_model;
		this.version_match=versionMatch;
	}

	
	public 	IModel<T> getModel() {
		return model;
	}
	
	public IModel<UserList> getUserListModel() {
		return this.list_model;
	}
	
	public int getVersionMatch() {
		return this.version_match;
	}
	
	
	@Override
	public AbstractMenuItemPanelV5<T> getItem(String id) {
		return new ObjectUserListMenuItem<T>(id, getUserListModel(), getModel(), getVersionMatch());
	}


	public void detach() {
		if (this.model!=null)
			this.model.detach();
		
		if (this.list_model!=null)
			this.list_model.detach();
	}
	
}
