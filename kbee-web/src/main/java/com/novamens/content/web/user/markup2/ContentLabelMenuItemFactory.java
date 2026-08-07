package com.novamens.content.web.user.markup2;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.model.LabelMember;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;

import kbee.web.event.wicket.ErrorEvent;

import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;

public class ContentLabelMenuItemFactory implements MenuItemFactory<Content> {
				
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ContentLabelMenuItemFactory.class.getName());
	
	private IModel<LabelMember> model;
	private IModel<Content> object_model;
	
	public ContentLabelMenuItemFactory(IModel<LabelMember> model, IModel<Content> object_model) {
		this.model = model;
		this.object_model = object_model;
		model.detach();
	}
	@Override
	public AbstractMenuItemPanelV5<Content> getItem(String id) {
		return new ContentLabelMenuItem(id, model, object_model) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				try {
				ContentLabelMenuItemFactory.this.onUpdate(target);
				} catch (Exception e) {
					logger.error(e);
					fire( new ErrorEvent<>(target, e));
				}
			}
		};	
	}

	public void onUpdate(AjaxRequestTarget target) {
	}

}
