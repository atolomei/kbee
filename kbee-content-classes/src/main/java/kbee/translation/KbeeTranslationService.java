package kbee.translation;

import java.util.Locale;

import com.novamens.dom.Domain;

public class KbeeTranslationService  implements TranslationService {
	
	private Domain domain;
	
	public KbeeTranslationService(Domain domain) {
	}
	
	public String transalte(String key, Locale locale) {
		return null;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
}
