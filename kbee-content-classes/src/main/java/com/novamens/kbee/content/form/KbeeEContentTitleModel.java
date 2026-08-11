package com.novamens.kbee.content.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.novamens.content.base.Content;
import com.novamens.content.form.EContentTitleModel;
import com.novamens.content.form.EFormDataSource;
import com.novamens.content.form.EPropertyModel;
import com.novamens.content.model.Classificable;
import com.novamens.event.Event;

public class  KbeeEContentTitleModel implements EContentTitleModel {
	private static final long serialVersionUID = 1L;
	
	
	@Override
	@JsonIgnore
	public boolean isReadOnly() {
		return false;
	}
	
	@Override
	public boolean handle(Event event) {
		return false;
	}
	
	@Override
	public List<String> onEvent(Event event) {
		return new ArrayList<String>();
	}
	
	@Override
	@JsonIgnore
	public String getMetainfoMessage() {
		return null;
	}
	
	@Override
	public EFormDataSource<String> getDataSource(Classificable object) {
		return null;
	}
	
	@Override
	public void set(Object object, List<String> data) {
	}
	
	@Override
	public void set(Object object, Object data) {
		if (object instanceof Content) {
			((Content)object).setTitle((String)data);
		}
	}
	
	@Override
	public String get(Object object) {
		return object instanceof Content ? ((Content)object).getTitle() : null;
	}
	
	public List<String> getValues(Object object) {
		return null;
	}
	
	@Override
	public String serialize(Classificable formobject, String value) {
		return value!=null ? value.toString() : null;
	}
	
	@Override
	public String deserialize(Classificable formobject, String token) {
		return token!=null ? getValueOf(token) : null;
	}
	
	@Override
	public String getErrorMessage(Object object) {
		return null;
	}
	
	@Override
	@JsonIgnore
	public String getDescription(Locale locale) {
		String description = getModelObjectName(locale);
		return description;
	}
	
	@JsonIgnore
	public String getModelObjectName(Locale locale) {
		return "Content Title";
	}
	
	
	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return EPropertyModel.GetTypeLabel();
	}

	protected String getValueOf(String value) {
		return value;
	}
}