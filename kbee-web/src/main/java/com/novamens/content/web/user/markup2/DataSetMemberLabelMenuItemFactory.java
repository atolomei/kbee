package com.novamens.content.web.user.markup2;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.LabelMember;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;


import kbee.web.event.wicket.ErrorEvent;

import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;

public class DataSetMemberLabelMenuItemFactory implements MenuItemFactory<DataSetMember>, IDetachable {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DataSetMemberLabelMenuItemFactory.class.getName());
	
	private static final long serialVersionUID = 1L;

	private IModel<LabelMember> model;
	private IModel<DataSetMember> object_model;
	
	public DataSetMemberLabelMenuItemFactory(IModel<LabelMember> model, IModel<DataSetMember> object_model) {
		this.model = model;
		this.object_model = object_model;
		model.detach();
	}
	@Override
	public AbstractMenuItemPanelV5<DataSetMember> getItem(String id) {
		return new DataSetMemberLabelMenuItem(id, model, object_model) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				try {
				DataSetMemberLabelMenuItemFactory.this.onUpdate(target);
				} catch (Exception e) {
					logger.error(e);
					fire( new ErrorEvent<>(target, e));
				}
			}
		};	
	}

	public void onUpdate(AjaxRequestTarget target) {
	}

	public void detach() {
		this.model.detach();
		this.object_model.detach();
	}
	
}
