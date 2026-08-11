package kbee.email;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novamens.content.base.Content;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.Domain;
import com.novamens.email.EmailBuilder;
import com.novamens.email.EmailData;
import com.novamens.kbee.template.KbeeEFormModel;
import com.novamens.kbee.template.KbeeEMailTemplateModel;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.text.TextTemplate;

import freemarker.template.TemplateModel;


public class EmailBuilderSendContent extends EmailBuilderBase implements EmailBuilder {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EmailBuilderSendContent.class.getName());
	
	private Content content;
	private List<Person> receivers = new ArrayList<Person>();
	private String to;
	private String  text;

	public EmailBuilderSendContent () {
	}
	
	public EmailBuilderSendContent (Content content,  String to, String text) {
		setContent(content);
		setTo(to);
		setText(text);
		setLanguage(content.getDomain().getLocale().getLanguage());
	}
	
	public EmailBuilderSendContent (Content content, Person sender, String to, String text, String[] attachments) {
		this.content = content;
		this.to=to;
		this.text = text;
		if (sender!=null)
			setLanguage(sender.getProfile(UserProfile.class).getUser().getLocale().getLanguage());
	}
	
	public EmailBuilderSendContent(Map<String, Object> parameters) {
		super();
		setParameters(parameters);
	}

	@Override
	public void setParameters(Map<String, Object> map) {
		super.setParameters(map);
		try {
			this.to=map.containsKey("to") ? ((String) map.get("to")) : null;
			this.text=map.containsKey("text") ? ((String) map.get("text")) : null;
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}

	@Override
	public String getKey() {
		return EmailTemplate.SEND_EMAIL;
	}
	 
	@Override
	public Map<String, Object> getBuilderObjects() {
		Map<String, Object> r=new HashMap<String, Object> ();
		r.put("content",  content);
		r.put("to",  to);
		r.put("text",  text);
		return r;
	}
	 
	@Override
	public EmailData build() {
		try{ 
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
	
	public Content getContent() {
		return content;
	}

	public void setContent(Content content) {
		this.content = content;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public List<Person> getPersonsReceivers() {
		return receivers;
	}

	public void setReceiver(Person receiver) {
		this.receivers.add(receiver);
	}
	
	public void setReceivers(List<Person> receivers) {
		this.receivers = receivers;
	}

	@Override
	public Domain getDomain() {
		if (content==null)
			return null;
		return content.getDomain();
	}

	@Override
	public String getArea() {
		return GRID;
	}
	
	public String getEmailTo() {
		String to = super.getEmailTo();
		for (Person receiver : getPersonsReceivers()) {
			if (!"".equals(to)) to+= "; ";
			to += receiver.getEmail();
		}
		if (getTo()!=null && !"".equals(getTo())) {
			if (!"".equals(to)) to+= "; ";
			to += getTo();	
		}	
		return to;
	}
	
	public TemplateModel getTemplateModel() {
		KbeeEMailTemplateModel model = new KbeeEMailTemplateModel();
		Content content = getContent();
		if (content==null) 	throw new IllegalArgumentException("content is null");
		model.setModel("sender", getSender());
		model.setModel("comment", getText());
		model.setContent(content);
		model.setModel("eform", new KbeeEFormModel());
		return model;
	}

}