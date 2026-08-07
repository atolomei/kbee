package com.novamens.content.web.user.markup2;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.user.UserLabel;

import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;

public class UserLabelMenuItemFactory2<T extends Content> implements MenuItemFactory<T> {

	private static final long serialVersionUID = 1L;
	private IModel<UserLabel> model;
	
	public UserLabelMenuItemFactory2(IModel<UserLabel> model) {
		this.model = model;
		model.detach();
	}
	
	public AbstractMenuItemPanelV5<T> getItem(String id) {
		return new UserLabelMenuItem2<T>(id, model) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				UserLabelMenuItemFactory2.this.onUpdate(target);
			}
		};
	}

	public void onUpdate(AjaxRequestTarget target) {
	}
}
