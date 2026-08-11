package com.novamens.kbee.template;

import freemarker.template.*;

import com.novamens.content.model.Attribute;
import com.novamens.datetime.DateTimeService;
import com.novamens.service.ServiceLocator;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

public class KbeeDateAttributeTemplateModel extends KbeeAttributeTemplateModel implements TemplateDateModel {
	
	public KbeeDateAttributeTemplateModel(String value, Attribute attribute, TemplateNodeModel parent) {
		super(value, attribute, parent);
	}
	
	public Date getAsDate() throws TemplateModelException {
	    if (getValue() == null || "".equals(getValue())) {
	        return null;
	    }

	    OffsetDateTime odate = ServiceLocator
	            .getService(DateTimeService.class)
	            .parseStrDate(getValue());

	    LocalDate localDate = odate.toLocalDate();

	    return Date.from(
	            localDate
	                    .atStartOfDay(ZoneId.systemDefault())
	                    .toInstant()
	    );
	}

	public int getDateType() {
		return TemplateDateModel.DATE;
		
	}
}
