package com.novamens.kbee.wicket.editor;

import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;

public abstract class EditorFragment<T> extends Fragment implements Editor<T>{
	private static final long serialVersionUID = 1L;
	IModel<T> model;
	
	public EditorFragment(String id, String markupid, MarkupContainer container) {
		super(id, markupid, container);
	}
	
	public void update(AjaxRequestTarget target) {
		
	}
	
	public void update(T object) {
		
	}
	
	public void edit(AjaxRequestTarget target) {
		
	}
	
	public Form<?> getForm() {
		return null;
	}
	
	public void setModel(IModel<T> model) {
		this.model = model;;
	}
	
	public IModel<T> getModel() {
		return model;
	}
	
	public T getModelObject() {
		return getModel().getObject();
	}
	
	public boolean isEditionEnabled() {
		return true;
	}
	
	public boolean isReadOnly() {
		return false;
	}
	
	public List<String> getUpdatedParts() {
		return null;
	}
	
	public void setUpdatedPart(String updatedPart) {
		
	}

	public boolean isFullWidth() {
		return false;
	}

	@Override
	public boolean isNew() {
		return false;
	}

	@Override
	public void setIsNew(boolean isnew) {
		
	}
}
