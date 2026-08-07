package com.novamens.logging;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.model.ObjectId;
import com.novamens.content.user.UserService;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;


@Deprecated
@Entity
@DiscriminatorValue("UserUpdateEvent")
public class UserUpdateEvent extends SecurityEvent {
	 

	public UserUpdateEvent() {
		super();
	}
	
	public UserUpdateEvent(Principal principal, List<String> updatedParts) {
		super(principal, updatedParts);
		setObjectId((new ObjectId(principal)).toString());
		setTitle(((User)principal).getFirstLastName());
		setKbeeClass("User");
		setEventUser(ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser());
	}
	
	public UserUpdateEvent(Principal principal, String description) {
		super(principal, description);
		setObjectId((new ObjectId(principal)).toString());
		setTitle(((User)principal).getFirstLastName());
		setKbeeClass("User");
		setEventUser(ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser());
	}
	
	
	@Deprecated
	@Override
	public String getEventType() {
		return "UserUpdate";
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
		return "Update";
	}
	
	
	@Override
	public String toString() {
		return getAction()+ " | " + getTarget();
	}

}
