package kbee.email;

import java.util.Map;

import com.novamens.content.email.EmailTemplate;
import com.novamens.content.entity.Person;
import com.novamens.dom.DomainType;

public class EmailBuilderWelcomeMessageBasic extends EmailBuilderWelcomeMessage  implements com.novamens.email.EmailBuilder {
	

	public EmailBuilderWelcomeMessageBasic() {
				super();
	}
	
	public EmailBuilderWelcomeMessageBasic(Person person, String to, String displayname) {
		super(person, to, displayname);
	}
	
	public EmailBuilderWelcomeMessageBasic(Map<String, Object> parameters) {
		super(parameters);
		
	}
	

		@Override
	public String getKey() {
			return EmailTemplate.WELCOME;
		 // return "welcome_" + DomainType.EXPRESS.getLabel().trim().toLowerCase();
	}

	
}
