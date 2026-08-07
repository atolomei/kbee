package kbee.lang;

import java.util.Locale;

import com.novamens.service.LanguageService;
import com.novamens.service.ServiceLocator;

public class LocalizedString {

	String key;
	
	public LocalizedString( String key) {
		this.key=key;
	}
	
	public String getString(Locale locale) {
		return ServiceLocator.getService(LanguageService.class).getString(key, locale);
	}
	
}
