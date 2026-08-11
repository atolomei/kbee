package com.novamens.kbee.content.model;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.novamens.content.model.AttributeValidator;
import com.novamens.content.model.ValidatorParser;
import com.novamens.dom.Json;
import com.novamens.kbee.json.KbeeJson;

public class KbeeValidatorParser extends ValidatorParser {
	
	public Json getJson(AttributeValidator validator) {
		if (validator instanceof DateRangeValidator) {
			OffsetDateTime from = ((DateRangeValidator)validator).getFrom();
			OffsetDateTime to = ((DateRangeValidator)validator).getTo();
			if (from!=null || to!=null) {
				KbeeJson json = new KbeeJson();
				json.put("type", "date-range");
				if (from!=null)
				json.put("from", getString(from));
				if (to!=null)
				json.put("to", getString(to));
				return json;
			}
		}
		if (validator instanceof NumericRangeValidator) {
			long from = ((NumericRangeValidator)validator).getFrom();
			long to = ((NumericRangeValidator)validator).getTo();
			KbeeJson json = new KbeeJson();
			json.put("type", "numeric-range");
			json.put("from", String.valueOf(from));
			json.put("to", String.valueOf(to));
			return json;
		}
		return null;
	}
	
	public AttributeValidator getValidator(Json json) {
		if (json==null) return null;
		if ("date-range".equals(json.get("type"))) {
			DateRangeValidator validator = new DateRangeValidator();
			String tostring = (String)json.get("to");
			if (tostring!=null) {
				OffsetDateTime to = getTime(tostring);
				if (to!=null) {
					validator.setTo(to);
				}
			}
			String fromstring = (String)json.get("from");
			if (fromstring!=null) {
				OffsetDateTime from = getTime(fromstring);
				if (from!=null) {
					validator.setFrom(from);
				}
			}
			return validator;
		}
		if ("numeric-range".equals(json.get("type"))) {
			NumericRangeValidator validator = new NumericRangeValidator();
			String tostring = (String)json.get("to");
			if (tostring!=null) {
				try {
					long to = Long.valueOf(tostring);
					validator.setTo(to);
				}
				catch (Exception e) {
				}
			}
			String fromstring = (String)json.get("from");
			if (fromstring!=null) {
				try {
					long from = Long.valueOf(fromstring);
					validator.setFrom(from);
				}
				catch (Exception e) {
				}
			}
			return validator;
		}
		return null;
	}
	
	private String getString(OffsetDateTime time) {
		if (time==null) return null;
		String string = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(time);
		return string;
	}
	
	private OffsetDateTime getTime(String value) {
		try {
			LocalDate local  = LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			LocalDateTime ldt = local.atStartOfDay();
			return OffsetDateTime.of(ldt, OffsetDateTime.now().getOffset());
		}
		catch (Exception e) {
		}
		return null;
	}

}
