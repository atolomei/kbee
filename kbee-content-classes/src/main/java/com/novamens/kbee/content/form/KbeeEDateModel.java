package com.novamens.kbee.content.form;

import java.time.OffsetDateTime;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.datetime.DateTimeService;
import com.novamens.service.ServiceLocator;

@JsonTypeName("date")
public class  KbeeEDateModel extends KbeeEFormAttributeModel<OffsetDateTime> {
	private static final long serialVersionUID = 1L;
	
	protected String toString(Object datetime) {
		String value = datetime!=null ?
			datetime.toString() :
			null;
			return value;	
	}
	
	protected OffsetDateTime getValueOf(String stringvalue) {
		OffsetDateTime value = stringvalue!=null ? 
				ServiceLocator.getService(DateTimeService.class).parseStrDate(stringvalue) :
				null;
			return value;	
	}
	
	@Override
	@JsonIgnore 
	public String getDescription(Locale locale) {
		return "Date";
	}
	
	@JsonIgnore 
	@Override
	public String getModelObjectName(Locale locale) {
		return "Date";
	}
}