package com.novamens.content.web.user.markup2;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.LabelMember;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;

import kbee.web.event.wicket.ErrorEvent;

import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;

public class PersonLabelMenuItemFactory implements MenuItemFactory<Person>, IDetachable {
			
	private static final long serialVersionUID = 1L;

	private IModel<LabelMember> model;
	private IModel<DataSetMember> object_model;
	private IModel<Person> person_model;
	
	public PersonLabelMenuItemFactory(IModel<LabelMember> model, IModel<Person> person_model, IModel<DataSetMember> object_model) {
		this.model = model;
		this.object_model = object_model;
		this.person_model=person_model;
		
		object_model.detach();
		person_model.detach();
		model.detach();
	}
	@Override
	public AbstractMenuItemPanelV5<Person> getItem(String id) {
		return new PersonLabelMenuItem(id, model, person_model, object_model) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				try {
				PersonLabelMenuItemFactory.this.onUpdate(target);
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
		this.person_model.detach();
	}
}
