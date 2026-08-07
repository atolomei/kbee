package com.novamens.logging;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.base.Rule;
 
import com.novamens.content.model.ObjectId;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserService;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;


@Entity
@DiscriminatorValue("SecurityDeleteEvent")
public class SecurityDeleteEvent extends SecurityEvent {
		
	
	

	public SecurityDeleteEvent() {
		super();
	}

	

	public SecurityDeleteEvent(Principal principal, List<String> updatedParts) {
		super(principal, updatedParts);
		setObjectId((new ObjectId(principal)).toString());

		if (principal instanceof Group) {
			setTitle(((Group)principal).getDisplayName());
			setKbeeClass("Group");
		}
		else {
			setTitle(((User)principal).getFirstLastName());
			setKbeeClass("User");
		}
		
		setEventUser(ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser());
		
	}



	public SecurityDeleteEvent(Principal principal, String description) {
		super(principal, description);
		
		setObjectId((new ObjectId(principal)).toString());
		
		if (principal instanceof Group) {
			setTitle(((Group)principal).getDisplayName());
			setKbeeClass("Group");
		}
		else {
			setTitle(((User)principal).getDisplayName());
			setKbeeClass("User");
		}
		
		setEventUser(ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser());
	}
	

	public SecurityDeleteEvent(Rule rule, String description) {
		super(rule, description);
		setObjectId((new ObjectId(rule)).toString());
		setTitle(rule.getName());
		setKbeeClass("Rule");
		setEventUser(ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser());
	}
	
	
	public SecurityDeleteEvent(Role role, String description) {
		super(role, description);
		setObjectId((new ObjectId(role)).toString());
		setTitle(role.getName());
		setKbeeClass("Role");
		setEventUser(ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser());
	}

	
	
	public SecurityDeleteEvent(Rule rule, List<String> updatedParts) {
		super(rule, updatedParts);
		setObjectId((new ObjectId(rule)).toString());
		setTitle(((Rule) rule).getDisplayName());
		setKbeeClass("Rule");
		setEventUser(ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser());
	}

	
	@Deprecated
	@Override
	public String getEventType() {
		return "SecurityDelete";
	}

	@Override
	public String getType() {
		return "Security";
	}

	@Override
	public String getObjectClass() {
		return getKbeeClass();
	}
	
	@Override
	public String getAction() {
		return "Delete";
	}


	@Override
	public String toString() {
		return getAction()+ " | " + getTarget();
	}

}
