package com.novamens.logging;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.model.ObjectId;
import com.novamens.dom.DomainObject;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;

@Entity
@DiscriminatorValue("EmptyRecycleBinEvent")
public class EmptyRecycleBinEvent extends AbstractObjectEvent implements DomainObject {


	static public String getClassEventType() {
		return "Empty Recycle Bin";
	}
	 
	public EmptyRecycleBinEvent() {
		setTitle("Empty Recycle bin");
		setAuditSet(AuditSet.DOMAIN_ADMIN);
	}
	
	public EmptyRecycleBinEvent(User user) {
		setAuditSet(AuditSet.DOMAIN_ADMIN);
		setEventUser(user);
		setObjectId((new ObjectId(getEventUser())).toString());
		setKbeeClass("User");
		setTitle("Empty Recycle bin");
	}
	
	// Deprecated
	@Override
	public String getEventType() {
		return  getClassEventType();
	}
	
	@Override
	public String getType() {
		return "System";
	}
	
	@Override
	public String getObjectClass() {
		return "Recycle Bin";
	}
	
	@Override
	public String getAction() {
		return "Empty Bin";
	}
	
	@Override
	public String toString() {
		return getAction()+ " | user: " + getEventUser().getDisplayName() + " (" + getEventUser().getUserName() + ") ";
	}
	
	


}
