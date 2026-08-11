package kbee.translation;

import java.util.Locale;

import com.novamens.service.ObjectService;

public interface TranslationService extends ObjectService {
	String transalte(String key, Locale locale);
}
