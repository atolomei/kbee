package com.novamens.kbee.content.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceNode;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormDataSource;
import com.novamens.content.form.EResourceSystemField;
import com.novamens.content.model.Classificable;

@JsonTypeName("resourcesystemv2")
public class KbeeEResourceSystemV2 extends EFormAbstractField<ResourceNode> implements EResourceSystemField {
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
		return "Resource System v2";
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
	public boolean isCalculable() {
		return false;
	}
	
	@Override
	@JsonIgnore
	public boolean isSingleValue() {
		return false;
	}	
}	
