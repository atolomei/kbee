package kbee.email;

import java.util.HashMap;
import java.util.Map;

import com.novamens.content.email.EmailTemplate;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.Domain;
import com.novamens.email.EmailBuilder;
import com.novamens.email.EmailData;
import com.novamens.kbee.template.KbeeEMailTemplateModel;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.text.TextTemplate;

import freemarker.template.TemplateModel;

public class EmailBuilderAdminSendPasswordReset extends EmailBuilderBase implements EmailBuilder {
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailBuilderSendContent.class.getName());
	
	private Person receiver;
	private Person sender;
	private String token;
	
	private String areas [] = { GENERAL, CONTEXT };
	
	public EmailBuilderAdminSendPasswordReset () {
		setMacroAreas(areas);
	}
	
	/**
	 * @param person
	 * @param to
	 * @param displayname
	 */
	public EmailBuilderAdminSendPasswordReset (Person receiver, Person sender) {
		this(receiver, sender, null);
	}
	
	public EmailBuilderAdminSendPasswordReset (Person receiver, Person sender, String token) {
		this.receiver=receiver;
		this.sender=sender;
		this.token=token;
		setLanguage(receiver.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
		setMacroAreas(areas);
	}
	
	public EmailBuilderAdminSendPasswordReset (Map<String, Object> parameters) {
		super();
		setParameters(parameters);
		setMacroAreas(areas);
	}
	
	public String getKey() {
		return "admin-sends-reset-password";
	}

	@Override
	public void setParameters(Map<String, Object> map) {
		super.setParameters(map);
		try {
			this.receiver= map.containsKey("receiver") ? (getContentDao().findPersonById( Long.valueOf((String) map.get("receiver")))) :null;
			this.sender= map.containsKey("sender") ? (getContentDao().findPersonById( Long.valueOf((String) map.get("sender")))) :null;
			if (this.receiver!=null)
					setLanguage(receiver.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
			
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}

	@Override
	public EmailData build() {
		try {
			if (getLanguage()==null)
				setLanguage(receiver.getDomain().getLocale().getLanguage());
				
			EmailTemplate template = getEmailTemplate(getDomain(), getLanguage(), getKey());
				
			TemplateModel emailmodel = getTemplateModel();
			
			TextTemplate fromtemplate = new KbeeTextTemplate(template.getFrom());
			String from = fromtemplate.process(emailmodel);
				
			TextTemplate subjecttemplate = new KbeeTextTemplate(template.getSubject());
			String subject = subjecttemplate.process(emailmodel);
				
			TextTemplate texttemplate = new KbeeTextTemplate(template.getStringTemplate());
			String text = texttemplate.process(emailmodel);
				
			EmailData data = new EmailData(from, getEmailTo(), subject, text, null, getKey(), null);
		
			return data;
				
		} 
		catch (Exception e) {
			logger.error(e);
			throw(e);
		}
	}
	
	@Override
	public String buildPlain() {
		try {
			if (getLanguage()==null)
				setLanguage(receiver.getDomain().getLocale().getLanguage());
				
			EmailTemplate template = getEmailTemplate(getDomain(), getLanguage(), getKey());
				
			TemplateModel emailmodel = getTemplateModel();
				
			TextTemplate texttemplate = new KbeeTextTemplate(template.getPlainTextTemplate());
			String text = texttemplate.process(emailmodel);
		
			return text;
		} 
		catch (Exception e) {
			logger.error(e);
			throw(e);
		}
	}

	@Override
	public Domain getDomain() {
		return receiver.getDomain();
	}

	@Override
	public String getEmailTo() {
		return receiver.getEmail();
	}

	@Override
	public String getArea() {
		return GENERAL;
	}
	
	@Override
	public Map<String, Object> getBuilderObjects() {
		Map<String, Object> r=new HashMap<String, Object> ();
		r.put("receiver", receiver);
		r.put("sender", sender);
		return r;
	}
	
	public TemplateModel getTemplateModel() {
		KbeeEMailTemplateModel model = new KbeeEMailTemplateModel();
		
		model.setModel("user", getUser(receiver));
		
		if (token==null) {
			SecurityService service = ServiceLocator.getService(SecurityService.class);
			token = service.nextSecureToken();
			service.addToken(receiver.getProfile(UserProfile.class).getUser(), token);
		}
		
		String url = getServerUrl(receiver.getDomain()) +"/passwordrecovery?key=" + token;
		
		model.setModel("ResetPasswordUrl", url);

		return model;
	}
}
