package kbee.email;

import java.util.HashMap;
import java.util.Map;

import com.novamens.content.base.Content;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.ObjectState;
import com.novamens.email.EmailBuilder;
import com.novamens.email.EmailData;
import com.novamens.email.EmailService;
import com.novamens.kbee.content.entity.KbeePerson;
import com.novamens.kbee.content.user.KbeeUserDevice;
import com.novamens.kbee.template.KbeeEMailTemplateModel;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.text.TextTemplate;

import freemarker.template.TemplateModel;

public class EmailBuilderRegistrationMessage extends EmailBuilderBase implements EmailBuilder {
	
	private Person receiver;
	
	private String areas [] = { GENERAL, CONTENT, WORKFLOW, CONTEXT };

	public EmailBuilderRegistrationMessage () {
		setMacroAreas(areas);
	}
	
	public EmailBuilderRegistrationMessage (Person receiver) {
		setReceiver(receiver);
		if (receiver!=null) {
			UserProfile profile = ((KbeePerson) receiver).getProfile(UserProfile.class);
			if (profile!=null)
				setLanguage( profile.getUser().getLocale().getLanguage());
		}
	}

	@Override	
	public String getKey() {
		return EmailTemplate.WELCOME;
	}

	@Override
	public Map<String, Object> getBuilderObjects() {
		Map<String, Object> map = new HashMap<String, Object> ();
		return map;
	}
	 
	@Override
	public EmailData build() {
		if (getReceiver()==null)
			throw new IllegalArgumentException("receiver is null");

		EmailTemplate template = ServiceLocator.getService(EmailService.class).getEmailTemplate(getDomain(), getLanguage(), getKey());
		
		String key = "registration"+ (getReceiver()!=null?  ("-"+getReceiver().getId().toString()):"");
		
		Map<String, Object> parameters = getParameters();
		
		parameters.put("text", template.getTextHTML());
		
		String to = getReceiver().getEmail();
	
		KbeeEMailTemplateModel emailmodel = new KbeeEMailTemplateModel(null, to, parameters, null);
		emailmodel.setReceiver(getReceiver());
		
		TextTemplate fromtemplate = new KbeeTextTemplate(template.getFrom());
		String from = fromtemplate.process(emailmodel);
		
		TextTemplate subjecttemplate = new KbeeTextTemplate(template.getSubject());
		String subject = subjecttemplate.process(emailmodel);
		
		String templatesource = template.getStringTemplate();
		templatesource = templatesource.replace("\r\n", "");
		templatesource = templatesource.replace("\t", "");
		TextTemplate texttemplate = new KbeeTextTemplate(templatesource);
		String text = texttemplate.process(emailmodel);
		
		EmailData data = new EmailData(from, to, subject, text, null, key, null);
		
		return data; 
	}
	
	@Override
	public boolean isSendEnabled() {
		return true  ;
	}

	public Person getReceiver() {
		return receiver;
	}

	public void setReceiver(Person receiver) {
		this.receiver = receiver;
	}

	@Override
	public Domain getDomain() {
		if (getReceiver()==null)
			return null;
		return getReceiver().getDomain();
	}

	@Override
	public String getArea() {
		return GRID;
	}
}