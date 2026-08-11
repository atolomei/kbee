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
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class EmailBuilderDBExport extends EmailBuilderBase implements EmailBuilder {
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailBuilderDBExport.class.getName());
	private static kbee.util.logging.Logger emaillogger = kbee.util.logging.Logger.getLogger("email");

	private  Person person;
	private  String filename;
	
	private String areas [] = { GENERAL, CONTEXT };
	

	public EmailBuilderDBExport() {
		setMacroAreas(areas);
	}
	
	public EmailBuilderDBExport(Person person, String filename) {
		setMacroAreas(areas);
		this.person=person;
		this.filename=filename;
		if (person!=null)
			setLanguage(person.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
	}
	
	/**
	 * person
	 * filename
	 * 
	 * @param parameters
	 */
	public EmailBuilderDBExport(Map<String, Object> parameters) {
		super();
		setParameters(parameters);
		setMacroAreas(areas);
	}
	
	
	@Override
	public void setParameters(Map<String, Object> map) {
		super.setParameters(map);
		try {
			this.person= map.containsKey("person") ? (getContentDao().findPersonById( Long.valueOf((String) map.get("person")))) :null;
			this.filename=map.containsKey("filename") ? ((String) map.get("filename")) : null;
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	@Override
	public EmailData build() {
	
		if (this.person==null)
			throw new IllegalArgumentException("person is null");
		
		if (this.filename==null)
			throw new IllegalArgumentException("filename is null");

		
		UserProfile profile = person.getProfile(UserProfile.class);

		String language = getLanguage();
		Domain domain = profile.getDomain();
		
		EmailTemplate template = getEmailTemplate(domain, language, "db-export");

		SecurityService service = ServiceLocator.getService(SecurityService.class);
		
		String token = service.nextSecureToken();
		service.addToken(profile.getUser(), token, filename, 60 * 24 * 10);  // 10 days
		
		String url = getServerUrl(domain) +"/dbexport?key=" + token;
		
		logger.debug("DB Export link -> " + url);
		emaillogger.debug("DB Export link -> " + url);
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		addGeneralMacros(domain, map);
		addAppContextMacros(person, person, getKey(),  map);

		map.put("${database-export-url}", url);
		
		
		return parse(template, profile.getPerson().getEmail(), map, "db-export-" + filename);

	}

	
	@Override
	public boolean isSendEnabled() {
		return isEnabled(person);
	}

	@Override
	public User getSender() {
		return person.getProfile(UserProfile.class).getUser();
	}
	
	
	@Override
	public Domain getDomain() {
		return person.getDomain();
	}

	@Override
	public String getArea() {
		return GENERAL;
	}

	@Override
	public String getKey() {
		return "db-export";
	}
	
	@Override
	public Map<String, Object> getBuilderObjects() {
		Map<String, Object> r=new HashMap<String, Object> ();
		r.put("person", person);
		r.put("filename", filename);
		return r;
	}

	

}
