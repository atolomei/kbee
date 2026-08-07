package com.novamens.logging;

import java.util.List;

import javax.persistence.Entity;

import com.novamens.content.email.EmailTemplate;
import com.novamens.content.model.ObjectId;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


@Entity
public class EmailTemplateEvent extends AbstractObjectEvent {

				
	public EmailTemplateEvent() {
		super();
		setAuditSet(AuditSet.EMAIL);
	}
	
	public EmailTemplateEvent(EmailTemplate emailtemplate, String description) {
		super();				
		setAuditSet(AuditSet.EMAIL);
		setEmailTemplate(emailtemplate);
		setParameters(description);
	}
	
	public EmailTemplateEvent(EmailTemplate emailtemplate, List<String> updatedParts) {
		super();
		setAuditSet(AuditSet.EMAIL);
		setEmailTemplate(emailtemplate);
		setParameters(getDescription(updatedParts));
	}
	
				
	public void setEmailTemplate(EmailTemplate dm) {
		setObjectId((new ObjectId(dm)).toString());
		setDomainId((Long) dm.getDomain().getId());
		String title = dm.getDisplayName(); 
		setTitle(title);
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
		setKbeeClass(EmailTemplate.class.getName());
	}
	
	
	@Deprecated
	@Override
	public String getEventType() {
		return "EmailTemplate";
	}
	
	// 
	// Action:  para Create, Update, Delete
	//
	@Override
	public String getAction() {
		return getEventType();
	}
	
	@Override
	public String getType() {
		return "EmailTemplate";
	}
}
