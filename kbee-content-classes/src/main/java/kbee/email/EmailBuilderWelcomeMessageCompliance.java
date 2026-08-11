package kbee.email;

import java.util.Map;

import com.novamens.content.email.EmailTemplate;
import com.novamens.content.entity.Person;
import com.novamens.dom.DomainType;

public class EmailBuilderWelcomeMessageCompliance extends EmailBuilderWelcomeMessage  implements com.novamens.email.EmailBuilder {

	
public EmailBuilderWelcomeMessageCompliance() {
	super();
}

public EmailBuilderWelcomeMessageCompliance(Person person, String to, String displayname) {
	super(person, to, displayname);
}

public EmailBuilderWelcomeMessageCompliance(Map<String, Object> parameters) {
	super(parameters);

}


	@Override
	public String getKey() {
		return EmailTemplate.WELCOME;
		//return "welcome_compliance";
	}

}
