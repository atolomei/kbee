package com.novamens.logging;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.model.ObjectId;
import com.novamens.kbee.security.KbeePrincipal;
import com.novamens.security.Principal;
import com.novamens.security.User;

@Entity
@DiscriminatorValue("LogoutEvent")
public class LogoutEvent extends SecurityEvent {
	 

	public LogoutEvent() {
		super();
	}
	
	public LogoutEvent(Principal principal) {
		super();
		setDomain(((KbeePrincipal)principal).getDomain());
		setObjectId((new ObjectId(principal)).toString());
		setTitle(((User)principal).getFirstLastName());
		setKbeeClass("User");
		setEventUser((User)principal);
	}
	
	@Override
	public String getEventType() {
		return "Logout";
	}
	
	@Override
	public String getType() {
		return "Security";
	}

	@Override
	public String getObjectClass() {
		return "User";
	}


	@Override
	public String getAction() {
		return getEventType();
	}

	
	@Override
	public String toString() {
		return getType() + " - " + getAction() + ". " + getTarget();
	}
}
