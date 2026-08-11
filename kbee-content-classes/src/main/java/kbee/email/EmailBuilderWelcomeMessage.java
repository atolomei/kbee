package kbee.email;

import java.util.HashMap;
import java.util.Map;

import com.novamens.content.email.EmailTemplate;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.email.EmailBuilder;
import com.novamens.email.EmailData;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public abstract class EmailBuilderWelcomeMessage extends EmailBuilderBase   {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailBuilderDBExport.class.getName());
	private static kbee.util.logging.Logger emaillogger = kbee.util.logging.Logger.getLogger("email");

	static private final int ONE_DAY_IN_MINUTES = 24 * 60;
	
	Person person;
	String to;
	String displayname;
	
	private String areas [] = { EmailBuilder.GENERAL, EmailBuilder.CONTEXT };
	
	
	public EmailBuilderWelcomeMessage() {
		setMacroAreas(areas);
	}
	
	
	/**
	 * 
	 * @param person
	 * @param to
	 * @param displayname
	 */
	public EmailBuilderWelcomeMessage(Person person, String to, String displayname) {
		setMacroAreas(areas);
		this.person=person;
		this.to=to;
		this.displayname=displayname;
		if (person!=null)
			setLanguage(person.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
	}
	
	public EmailBuilderWelcomeMessage(Map<String, Object> parameters) {
		
		super();
		setParameters(parameters);
		setMacroAreas(areas);
	}

	@Override
	public void setParameters(Map<String, Object> map) {
		super.setParameters(map);
		try {
			this.person= map.containsKey("person") ? (getContentDao().findPersonById( Long.valueOf((String) map.get("person")))) :null;
			this.to=map.containsKey("to") ? ((String) map.get("to")) : null;
			this.displayname=map.containsKey("displayname") ? ((String) map.get("displayname")) : null;
			
			if (person!=null)
				setLanguage(person.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
			
		} catch (Exception e) {
			logger.error(e);
		}
	}

	
	
	public EmailData build() {
		
		if (this.person==null)
			throw new IllegalArgumentException("person is null");
		
		if (this.to==null)
			throw new IllegalArgumentException("to is null");
		
		
		Domain domain = person.getDomain();
		
		EmailTemplate template = getEmailTemplate(domain, getLanguage(), getKey());
		
		if (template==null)
			getEmailTemplate(domain, getLanguage(), "welcome");
		
		
		SecurityService service = ServiceLocator.getService(SecurityService.class);
		String token = service.nextSecureToken();
		service.addToken(person.getProfile(UserProfile.class).getUser(), token, ONE_DAY_IN_MINUTES * 3);
		String purl = getServerUrl(domain) +"/passwordrecovery?key=" + token;
		
		Map<String, Object> map = new HashMap<String, Object>();
		addGeneralMacros(domain, map);
		addAppContextMacros( person, person, "action", map);
		
		map.put("${password-url}", purl);
		
		
		return parse(template, to, map, "welcome-message-"+person.getProfile(UserProfile.class).getUser().getUserName());

	}

		

	public Domain getDomain() {
		return person.getDomain();
	}


	public String getArea() {
		return EmailBuilder.GENERAL;
	}
	

	public Map<String, Object> getBuilderObjects() {
		Map<String, Object> r=new HashMap<String, Object> ();
		r.put("person", person);
		r.put("to",  to);
		r.put("displayname",  displayname);
		return r;
	}


}
