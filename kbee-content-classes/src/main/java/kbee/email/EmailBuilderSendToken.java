package kbee.email;

import java.util.Map;

import com.novamens.content.base.Content;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.Domain;
import com.novamens.email.EmailBuilder;
import com.novamens.email.EmailData;
import com.novamens.kbee.content.entity.KbeePerson;
import com.novamens.kbee.template.KbeeEMailTemplateModel;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.text.TextTemplate;

import freemarker.template.TemplateModel;

public class EmailBuilderSendToken extends EmailBuilderBase implements EmailBuilder {
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailBuilderSendToken.class.getName());
	
	private Content content;
	private Person person;
	private String token;
	
	//private String areas [] = { CONTEXT };
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public EmailBuilderSendToken () {
	}
	
	public EmailBuilderSendToken (Content content, Person person, String token) {
		setContent(content);
		setPerson(person);
		setToken(token);
		
		if (person!=null) {
			UserProfile profile = ((KbeePerson) getPerson()).getProfile(UserProfile.class);
			if (profile!=null)
				setLanguage( profile.getUser().getLocale().getLanguage());
			else
				setLanguage(getContent().getDomain().getLocale().getLanguage());
		}
		else
			setLanguage(getContent().getDomain().getLocale().getLanguage());
		
	}
	
	public EmailBuilderSendToken(Map<String, Object> parameters) {
		super();
		setParameters(parameters);
	}

	@Override
	public void setParameters(Map<String, Object> map) {
		super.setParameters(map);
	}
	

	public Content getContent() {
		return content;
	}

	public void setContent(Content content) {
		this.content = content;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}
	
	@Override
	public String getEmailTo() {
		return getPerson().getEmail();
	}

	@Override
	public EmailData build() {
		
		try {
			
				if (getLanguage()==null)
					setLanguage(getContent().getDomain().getLocale().getLanguage());
				
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
				
		} catch (Exception e) {
			logger.error(e);
			throw(e);
		}
	}

	@Override
	public Domain getDomain() {
		return getPerson()!=null?getPerson().getDomain():null;
	}
	
	@Override
	public String getArea() {
		return DOMAIN;
	}

	@Override
	public String getKey() {
		return EmailTemplate.SEND_TOKEN;
	}
	
	@Override
	public Map<String, Object> getBuilderObjects() {
		return null;
	}
	
	@Override
	public boolean isSendEnabled()  {
		return true;
	}
	
	public TemplateModel getTemplateModel() {
		KbeeEMailTemplateModel model = new KbeeEMailTemplateModel();
		model.setModel("content", getContent());
		model.setModel("person", getPerson());
		model.setModel("token", getToken());
		return model;
	}

}