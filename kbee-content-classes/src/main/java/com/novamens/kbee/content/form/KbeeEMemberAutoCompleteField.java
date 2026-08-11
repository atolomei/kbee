package com.novamens.kbee.content.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.form.EAutoCompleteField;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormDataSource;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.DataSetMember;

@JsonTypeName("member autocomplete")
public class KbeeEMemberAutoCompleteField extends EFormAbstractField<DataSetMember> implements EAutoCompleteField<DataSetMember> {
	private static final long serialVersionUID = 1L;
	
	private String valueTemplate;
	private String infoTemplate;
	private String choiceTemplate;

	@JsonIgnore
	public EFormDataSource<DataSetMember> getChoicesSource(Classificable object) {
		return getModel().getDataSource(object);
	}
	
	@Override
	public void get(Object object, EFormData data) {
		data.setData(this, getModel().get(object));
	}
	
	@Override
	public void set(Object object, EFormData data) {
		getModel().set(object, data.getData(this));
	}

	public String getValueTemplate() {
		return valueTemplate;
	}

	public void setValueTemplate(String valueTemplate) {
		this.valueTemplate = valueTemplate;
	}

	public String getInfoTemplate() {
		return infoTemplate;
	}

	public void setInfoTemplate(String infoTemplate) {
		this.infoTemplate = infoTemplate;
	}

	public String getChoiceTemplate() {
		return choiceTemplate;
	}

	public void setChoiceTemplate(String choiceTemplate) {
		this.choiceTemplate = choiceTemplate;
	}

	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return "AutoComplete";
	}
}