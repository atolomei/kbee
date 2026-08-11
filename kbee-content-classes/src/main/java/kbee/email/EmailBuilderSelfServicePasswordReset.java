package kbee.email;

import java.util.HashMap;
import java.util.Map;

import com.novamens.content.email.EmailTemplate;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.email.EmailBuilder;
import com.novamens.email.EmailData;
import com.novamens.email.EmailService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class EmailBuilderSelfServicePasswordReset extends EmailBuilderBase implements EmailBuilder {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailBuilderSelfServicePasswordReset.class.getName());
	private static kbee.util.logging.Logger emaillogger = kbee.util.logging.Logger.getLogger("email");

	private Person person;
	
	private String areas [] = { GENERAL,   CONTEXT };
	
	
	 public EmailBuilderSelfServicePasswordReset() {
			setMacroAreas(areas);
	 }
	 
			 
	public EmailBuilderSelfServicePasswordReset(Person person) {
		setMacroAreas(areas); 
		this.person=person;
		setSender(this.person.getProfile(UserProfile.class).getUser());
		setLanguage(person.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
	 }
	 
		
	 public EmailBuilderSelfServicePasswordReset(Map<String, Object> parameters) {
			super();
			setParameters(parameters);
			setMacroAreas(areas);
	 }
	 
	 
	@Override
	public void setParameters(Map<String, Object> map) {
			super.setParameters(map);
			try {
				this.person= map.containsKey("person") ? (getContentDao().findPersonById( Long.valueOf((String) map.get("person")))) :null;
				
				if (this.person!=null)
					setLanguage(person.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
				
			} catch (Exception e) {
				logger.error(e);
			}
	}
		
	
	@Override
	public EmailData build() {
		
		if (this.person==null)
			throw new IllegalArgumentException("person is null");

		UserProfile profile = person.getProfile(UserProfile.class);
		
		EmailTemplate template = ServiceLocator.getService(EmailService.class).getEmailTemplate(this.person.getDomain(),  getLanguage(), getKey());
		String key="user-password-reset-" + profile.getUser().getUserName();
		Map<String, Object> map = new HashMap<String, Object>();
		
		
		 
		
		addGeneralMacros(this.person.getDomain(), map);
		addAppContextMacros(this.person,  this.person, key, map);

		SecurityService service = ServiceLocator.getService(SecurityService.class);
		String token = service.nextSecureToken();
		service.addToken(person.getProfile(UserProfile.class).getUser(), token);
		String url = getServerUrl(person.getDomain()) +"/passwordrecovery?key=" + token;
		
		logger.debug(url);
		emaillogger.debug(url);

		
		map.put("${password-reset-url}", url);
		map.put("${password-url}", url);

		
		//Domain kd= getContentDao().findDomainByName("kbee");
		//if (kd!=null) {
			// User ruser = ServiceLocator.getService(UserService.class).findRootUser(kd);
			//UserProfile rp = getContentDao().findUserProfileByUser(ruser);
			// rp.getPerson().getEmail()
			EmailData data = parse(template, person.getEmail(), map, key);
			return data;
		//}
		//return null;
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
		return EmailTemplate.FORGOT_PASSWORD;
	}
	
	@Override
	public Map<String, Object> getBuilderObjects() {
		Map<String, Object> r=new HashMap<String, Object> ();
		r.put("person", person);
		return r;
	}
	



}

