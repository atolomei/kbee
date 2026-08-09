package com.novamens.kbee.wicket.markup.html.console.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.content.userlist.UserList;
import com.novamens.kbee.wicket.markup.html.event.AbstractWicketAjaxEvent;

public class MyListsApplyUserListEvent extends AbstractWicketAjaxEvent {
			
	IModel <UserList> mlist;
	boolean is_apply = true;
	
	public MyListsApplyUserListEvent(AjaxRequestTarget requestTarget, IModel<UserList> list, boolean isapply) {
		super(requestTarget);
		this.mlist=list;
		this.is_apply=isapply;
	}
	
	public boolean isApply() {
		return this.is_apply;
	}
	
	public IModel<UserList>  getUserList() {
		return this.mlist;
	}
	
	
	public void detach() {
		this.mlist.detach();
	}
}
