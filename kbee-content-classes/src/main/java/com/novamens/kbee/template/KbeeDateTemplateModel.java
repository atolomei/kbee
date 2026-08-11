package com.novamens.kbee.template;

import java.time.OffsetDateTime;
import java.util.Date;

import freemarker.template.*;


public class KbeeDateTemplateModel extends KbeeCanonicalTemplateModel implements TemplateNodeModel, TemplateScalarModel, TemplateDateModel {
	
	OffsetDateTime value;
	
	public KbeeDateTemplateModel(String name, OffsetDateTime value, TemplateNodeModel parent) {
		super(name, String.valueOf(value));
		this.value=value;
	}
	
	
	public Date getAsDate() throws TemplateModelException {
		if (value==null) return null;
		long epochMilli = value.toInstant().toEpochMilli();
		Date date = new Date(epochMilli);
		return date;
	}
	
	public int getDateType() {
		return TemplateDateModel.DATETIME;
	}
}
