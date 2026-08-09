package com.novamens.wicket.markup.html.form;

import java.util.Locale;

import org.apache.wicket.util.convert.IConverter;

public class SugestionConverter implements IConverter<WebSuggestion> {
	private static final long serialVersionUID = 1L;
	
	public WebSuggestion convertToObject(String value, Locale locale) {
		return new WebSuggestion(value, value, 0, false);
	}
	
	public String convertToString(WebSuggestion value, Locale locale) {
		return value.getText();
	}
}	