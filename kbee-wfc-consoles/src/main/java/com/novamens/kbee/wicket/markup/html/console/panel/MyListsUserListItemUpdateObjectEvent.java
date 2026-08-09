package com.novamens.kbee.wicket.markup.html.console.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.content.userlist.UserList;
import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class MyListsUserListItemUpdateObjectEvent<T> extends AbstractWicketAjaxEvent {

	IModel<T> model;
	IModel<UserList> listmodel;
	
	public MyListsUserListItemUpdateObjectEvent(AjaxRequestTarget requestTarget, IModel<T> model, IModel<UserList> listmodel) {
		super(requestTarget);
		this.model=model;
		this.listmodel=listmodel;
	}

	public IModel<T> getModel() {
		return this.model;
	}

	public IModel<UserList> getListModel() {
		return this.listmodel;
	}

	public void detach() {
		this.model.detach();
		
		if (listmodel!=null)
			listmodel.detach();
		
	}
}
