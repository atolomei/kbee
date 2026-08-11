package com.novamens.kbee.content.form;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormDataSource;
import com.novamens.content.form.EListField;
import com.novamens.content.form.ERelationField;
import com.novamens.content.model.Classificable;

@JsonTypeName("relation")
public class KbeeERelation extends EFormAbstractField<Content> implements EListField<Content>, ERelationField<Content> {
	private static final long serialVersionUID = 1L;
	
	@JsonIgnore
	public EFormDataSource<Resource> getChoicesSource(Classificable object) {
		return null;
	}
	@Override
	public void get(Object object, EFormData data) {
		data.setData(this, getModel().getValues(object));
	}
	
	@Override
	public void set(Object object, EFormData data) {
		getModel().set(object, data.getValues(this));
	}
	
	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return "Relation";
	}
	
	public String getValueTemplate() {
		return null;
	}

	public String getInfoTemplate() {
		return null;
	}

	public String getChoiceTemplate() {
		return null;
	}
	
	@Override
	@JsonIgnore
	public boolean isSingleValue() {
		return false;
	}	
}
