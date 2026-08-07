package com.novamens.service;

import java.util.Locale;

public interface LanguageService extends SystemService {
	
	public String getString(String key);
	public String getString(String key, Locale locale);
	
	public String removeStopWords(String trim, Locale locale);
	String getString(String key, Locale locale, String defaultValue);
	
}
