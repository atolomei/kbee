package com.novamens.logging;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.email.EmailTemplate;

@Entity
@DiscriminatorValue("EmailTemplateUpdateEvent")
public class EmailTemplateUpdateEvent extends EmailTemplateEvent {
			
	public EmailTemplateUpdateEvent() {
		super();
	}
	
	public EmailTemplateUpdateEvent(EmailTemplate cabinet, String description) {
		super(cabinet, description);
	}
	
	public EmailTemplateUpdateEvent(EmailTemplate cabinet, List<String> updatedParts)  {
		super(cabinet, updatedParts);
	}
	
	@Override
	public String getAction() {
		return "Update";
	}

	@Override
	public String toString() {
		return getAction()+ " | " + getTarget();
	}
}
