package com.novamens.kbee.content.form;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.base.Content;
import com.novamens.content.service.ContentService;

@JsonTypeName("html attribute")
public class  KbeeEHtmlModel extends KbeeEAttributeFieldModel<String> {
	private static final long serialVersionUID = 1L;
	
	protected String toString(Object value) {
		return value!=null ? value.toString() : null;	
	}
	
	protected String getValueOf(String stringvalue) {
		return stringvalue;
	}
	
	
	@Override
	public void set(Object object, Object data) {
		super.set(object, data);
		Content content =  (Content)object;
		((Content)object).setLinks(content.getService(ContentService.class).getLinks());
	}
	
	@Override
	@JsonIgnore 
	public String getDescription(Locale locale) {
		return "Html";
	}
	
	@JsonIgnore 
	@Override
	public String getModelObjectName(Locale locale) {
		return "Html";
	}
}