package kbee.email;

import java.util.Map;

import com.novamens.content.email.EmailTemplate;
import com.novamens.content.user.UserDevice;
import com.novamens.dom.Domain;
import com.novamens.email.EmailBuilder;
import com.novamens.email.EmailData;
import com.novamens.kbee.content.user.KbeeUserDevice;
import com.novamens.kbee.template.KbeeEMailTemplateModel;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.text.TextTemplate;

import freemarker.template.TemplateModel;

public class EmailBuilderRegisterDevice extends EmailBuilderBase implements EmailBuilder {
	
	private UserDevice device;
	
	public EmailBuilderRegisterDevice () {
	}
	
	public EmailBuilderRegisterDevice (UserDevice device) {
		setDevice(device);
		if (device instanceof KbeeUserDevice)
			setLanguage(((KbeeUserDevice) device).getDomain().getLocale().getLanguage());
	}
	
	
	public EmailBuilderRegisterDevice(Map<String, Object> parameters) {
		super();
		setParameters(parameters);
	}

	@Override
	public void setParameters(Map<String, Object> map) {
		super.setParameters(map);
	}
	
	public UserDevice getDevice() {
		return device;
	}

	public void setDevice(UserDevice device) {
		this.device = device;
	}

	@Override
	public EmailData build() {
		
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

	@Override
	public Domain getDomain() {
		return getDevice()!=null?((KbeeUserDevice)getDevice()).getDomain():null;
	}
	
	@Override
	public String getArea() {
		return DOMAIN;
	}

	@Override
	public String getKey() {
		return EmailTemplate.REGISTER_DEVICE;
	}
	
	@Override
	public Map<String, Object> getBuilderObjects() {
		return null;
	}
	
	public TemplateModel getTemplateModel() {
		KbeeEMailTemplateModel model = new KbeeEMailTemplateModel();
		model.setModel("device", getDevice());
		return model;
	}

}