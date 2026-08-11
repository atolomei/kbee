package com.novamens.kbee.content.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormDataSource;
import com.novamens.content.form.EListField;
import com.novamens.content.model.Classificable;

public class KbeeEListField<T> extends EFormAbstractField<T> implements EListField<T> {
	private static final long serialVersionUID = 1L;
	
	private String valueTemplate;
	private String infoTemplate;
	private String choiceTemplate;
	private String format;

	@JsonIgnore
	public EFormDataSource<T> getChoicesSource(Classificable object) {
		return getModel().getDataSource(object);
	}
	
	@Override
	public void get(Object object, EFormData data) {
		data.setData(this, getModel().getValues(object));
	}
	
	@Override
	public void set(Object object, EFormData data) {
		getModel().set(object, data.getValues(this));
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

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	
	@Override
	@JsonIgnore
	public boolean isSingleValue() {
		return false;
	}	
}