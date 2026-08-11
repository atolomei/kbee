package com.novamens.kbee.content.webapi.type;

import com.novamens.content.email.EmailTemplate;

import kbee.api.model.IEmailTemplate;

public class IEmailTemplateAdapter implements Adapter<EmailTemplate, IEmailTemplate> {
	
	public IEmailTemplateAdapter() {
	}
	
	public IEmailTemplate adapt(EmailTemplate template) {
		
		IEmailTemplate itemplate = new IEmailTemplate();
	
		itemplate.setId(String.valueOf(template.getId()));
		itemplate.setTitle(template.getTitle());
		itemplate.setKey(template.getKey());
		itemplate.setFrom(template.getFrom());
		itemplate.setLanguage(template.getLanguage());
		itemplate.setSubject(template.getSubject());
		itemplate.setText(template.getStringTemplate());
		itemplate.setLastModifiedDate(template.getLastModifiedOffsetDateTime());

		return itemplate;	
	}
}
