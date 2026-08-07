package com.novamens.logging;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.base.Rule;
import com.novamens.content.model.ObjectId;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;

@Entity
@DiscriminatorValue("SecurityCreateEvent")
public class SecurityCreateEvent extends SecurityEvent {

	public SecurityCreateEvent() {
		super();
	}
	
	public SecurityCreateEvent(Principal principal, List<String> updatedParts) {
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

		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		if (profile!=null)
			setEventUser(ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser());
		
	}

	
	public SecurityCreateEvent(Principal principal, String description) {
		this(principal, description, null);
	}
	
	
	/**
	 * Used by batch actions that may not have a Session User
	 * 
	 * @param principal
	 * @param description
	 * @param caller
	 */
	public SecurityCreateEvent(Principal principal, String description, User caller) {
		super(principal, description);
		
		setObjectId((new ObjectId(principal)).toString());
		
		if (principal instanceof Group) {
			setTitle(((Group)principal).getDisplayName());
			setKbeeClass("Group");
		}
		else {
			setTitle(((User)principal).getFirstLastName());
			setKbeeClass("User");
		}
	
		if (caller!=null)  
			setEventUser(caller);
		else { 
			UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
			if (profile!=null)
				setEventUser(profile.getUser());
		}
	}
	
	
	
	
	public SecurityCreateEvent(Rule rule, String description) {
		super(rule, description);
		setObjectId((new ObjectId(rule)).toString());
		setTitle(rule.getName());
		setKbeeClass("Rule");
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		if (profile!=null)
			setEventUser(profile.getUser());

	}
	
										
	public SecurityCreateEvent(Role role, String description) {
		super(role, description);
		setObjectId((new ObjectId(role)).toString());
		setTitle(role.getName());
		setKbeeClass("Role");
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		if (profile!=null)
			setEventUser(profile.getUser());

	}
	
							
	public SecurityCreateEvent(Rule rule, List<String> updatedParts) {
		super(rule, updatedParts);
		setObjectId((new ObjectId(rule)).toString());
		setTitle(((Rule) rule).getDisplayName());
		setKbeeClass("Rule");
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		if (profile!=null)
			setEventUser(profile.getUser());

	}


	@Override
	public String toString() {
		return getClass().getSimpleName() +  " [" + getKbeeClass() + " - " + getObjectId() + "]";
	}
	
	@Deprecated
	@Override
	public String getEventType() {
		return "SecurityCreate";
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
		return "Create";
	}

	
	
}
