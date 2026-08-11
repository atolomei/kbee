package kbee.email;

import java.util.HashMap;
import java.util.Map;

import com.novamens.content.email.EmailTemplate;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.Domain;
import com.novamens.email.EmailBuilder;
import com.novamens.email.EmailData;
import com.novamens.security.User;

public class EmailBuilderGetMyUserName extends EmailBuilderBase implements EmailBuilder {

	
	//private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailBuilderSendContent.class.getName());
	//private static kbee.util.logging.Logger emaillogger = kbee.util.logging.Logger.getLogger("email");

	private Person person;
	private String to;
	private String areas [] = { GENERAL,  CONTEXT };
	 

	public EmailBuilderGetMyUserName () {
		setMacroAreas(areas);
	}
			
	/**
	 * domain
	 * language
	 * map ???
	 * 
	 * @param domain
	 * @param language
	 * @param to
	 * @param map
	 */
	public EmailBuilderGetMyUserName (Person person, String to) {
		setMacroAreas(areas);
		this.person=person;
		String language = person.getProfile(UserProfile.class).getUser().getLocale().getLanguage();
		setLanguage(language);
		this.to  = to;
	}
	
	
	public EmailBuilderGetMyUserName(Map<String, Object> parameters) {
		super();
		setParameters(parameters);
		setMacroAreas(areas);
	}
	
	
	@Override
	public void setParameters(Map<String, Object> map) {
		super.setParameters(map);
		this.person = map.containsKey("person") ? (getContentDao().findPersonById( Long.valueOf((String) map.get("person")))) :null;
		this.to=map.containsKey("to") ? ((String) map.get("to")) : null;
		if (person!=null)
			setLanguage(person.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
	}

	/**
	 */
	@Override
	public EmailData build() {
		
		if (this.person==null)
			throw new IllegalArgumentException("person is null");
		
		
		Map<String, Object> map= new HashMap<String, Object>();
		
		
		if (person.getPhone()!=null && person.getPhone().length()>3) { 
			String fourdigits= person.getPhone().substring(person.getPhone().length()-4, person.getPhone().length());
			map.put("${person-phone-last-four-digits}", fourdigits);
		}
		
		EmailTemplate template = getEmailTemplate(person.getDomain(), getLanguage(), "forgot-username");
		
		addGeneralMacros(person.getDomain(), map);
		addAppContextMacros(person, person, getKey(),  map);
		
		
		return parse(template, to, map, "forgot-username-"+to);
	}

	@Override
	public Map<String, Object> getBuilderObjects() {
		Map<String, Object> r=new HashMap<String, Object> ();
		r.put("person", person);
		r.put("to", to);
		return r;
	}



	@Override
	public boolean isSendEnabled() {
		return true;
	}

	@Override
	public User getSender() {
		return null;
	}
	
	
	@Override
	public Domain getDomain() {
		return person!=null?person.getDomain():null;
	}
	
	@Override
	public String getArea() {
		return GENERAL;
	}

	@Override
	public String getKey() {
		return "forgot-username";
	}
}
