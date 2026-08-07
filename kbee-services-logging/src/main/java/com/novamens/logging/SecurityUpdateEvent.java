package com.novamens.logging;


import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import com.novamens.content.base.Rule;
import com.novamens.content.model.ObjectId;
import com.novamens.content.security.Role;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

/** 
 *  User, Group, Rule
 */

@Entity
@DiscriminatorValue("SecurityUpdateEvent")
public class SecurityUpdateEvent extends SecurityEvent {
	
	@Transient  
	private static transient kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SecurityUpdateEvent.class.getName());
	
	public SecurityUpdateEvent() {
		super();
	}

	/**
	 * Sirve para Password Reset, donde no hay Session user
	 * 
	 * @param principal
	 * @param sessionUser
	 * @param updatedParts
	 */
	public SecurityUpdateEvent(Principal principal, User sessionUser, List<String> updatedParts) {
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
		setEventUser(sessionUser);
		if (sessionUser!=null)
			setEventUser(sessionUser);
	}
	
	
	public SecurityUpdateEvent(Principal principal, List<String> updatedParts) {
		super(principal, updatedParts);
		try {
			setObjectId((new ObjectId(principal)).toString());

			if (principal instanceof Group) {
				setTitle(((Group)principal).getDisplayName());
				setKbeeClass("Group");
			}
			else {
				setTitle(((User)principal).getFirstLastName());
				setKbeeClass("User");
			}
			
			// Session user
			//
			User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
			if (user!=null)
				setEventUser(user);
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	public SecurityUpdateEvent(Principal principal, String description) {
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

		// Session user
		//
		User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
		if (user!=null)
			setEventUser(user);
	}
									
	public SecurityUpdateEvent(Rule rule, String description) {
		super(rule, description);
		setObjectId((new ObjectId(rule)).toString());
		setTitle(rule.getName());
		setKbeeClass("Rule");

		// Session user
		//
		User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
		if (user!=null)
			setEventUser(user);
	}
							
	public SecurityUpdateEvent(Rule rule, List<String> updatedParts) {
		super(rule, updatedParts);
		setObjectId((new ObjectId(rule)).toString());
		setTitle(((Rule) rule).getDisplayName());
		setKbeeClass("Rule");

		// Session user
		//
		User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
		if (user!=null)
			setEventUser(user);
	}
	
	public SecurityUpdateEvent(Role role, List<String> updatedParts) {
		super(role, updatedParts);
		setKbeeClass("Role");
	}
	
	public SecurityUpdateEvent(Role role, String description) {
		super(role, description);
		setKbeeClass("Role");
	}
	
	@Deprecated
	@Override
	public String getEventType() {
		return "SecurityUpdate";
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
		return "Update";
	}

	@Override
	public String toString() {
		return getAction()+ " | " + getTarget();
	}
}
