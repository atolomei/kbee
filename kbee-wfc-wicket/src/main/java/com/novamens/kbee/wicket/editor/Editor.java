package com.novamens.kbee.wicket.editor;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;

import com.novamens.content.form.UpdatedField;

public interface Editor<T> extends Serializable {
	
	public void update(AjaxRequestTarget target);
	public void edit(AjaxRequestTarget target);
	public Form<?> getForm();
	public IModel<T> getModel();
	public T getModelObject();
	
	public void update(T object);
	
	public boolean isEditionEnabled();   // si la edicion esta habilitada
	public boolean isReadOnly();		 // si el editor es readonly
	
	public void setIsNew(boolean isnew);			
	public boolean isNew();				// si T es nuevo
	
	public boolean isFullWidth();
	
	public List<UpdatedField> getUpdatedFields();
	public void setUpdatedField(UpdatedField updatedField);
	
	public List<String> getUpdatedParts();
	public void setUpdatedPart(String updatedPart);
}