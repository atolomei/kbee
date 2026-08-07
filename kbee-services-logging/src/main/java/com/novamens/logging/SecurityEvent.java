package com.novamens.logging;

import java.util.List;

import javax.persistence.Entity;

import com.novamens.content.base.Rule;
import com.novamens.content.model.ObjectId;
import com.novamens.content.security.Role;
import com.novamens.kbee.security.KbeePrincipal;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


/** ------------------------------------------------------------ 
 * Create User 
 * Delete User
 * Update User (change password, suspend)
 * 
 * Create Group
 * Delete Group
 * Update Group
 * 
 * Create Rule
 * Delete Rule
 * Update Rule
 * 
 */

@Entity
public class SecurityEvent extends AbstractObjectEvent {
	
	public SecurityEvent() {
		super();
		setAuditSet(AuditSet.SECURITY);
	}
	
	public SecurityEvent(Principal principal, String description) {
		setAuditSet(AuditSet.SECURITY);
		setPrincipal(principal);
		if (principal instanceof User)
			setKbeeClass(User.class.getName());
		else if (principal instanceof Group)
			setKbeeClass(Group.class.getName());
		setParameters(description);
	}
	
	public SecurityEvent(Principal principal, List<String> updatedParts) {
		setAuditSet(AuditSet.SECURITY);
		setPrincipal(principal);
		if (principal instanceof User)
			setKbeeClass(User.class.getName());
		else if (principal instanceof Group)
			setKbeeClass(Group.class.getName());
		setParameters(getDescription(updatedParts));		
	}

	
	public SecurityEvent(Rule rule) {
		this(rule, (String)null);
	}

	public SecurityEvent(Rule rule, List<String> updatedParts) {
		setAuditSet(AuditSet.SECURITY);
		setDomain(rule.getDomain());
		setObjectId((new ObjectId(rule)).toString());
		setKbeeClass(Rule.class.getName());
		setTitle(rule.getName());
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
		setParameters(getDescription(updatedParts));		
	}
	
	public SecurityEvent(Rule rule, String description) {
		setAuditSet(AuditSet.SECURITY);
		setDomain(rule.getDomain());
		setObjectId((new ObjectId(rule)).toString());
		setKbeeClass(Rule.class.getName());
		setTitle(rule.getName());
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
		setParameters(description);		
	}

	
	public SecurityEvent(Role role, List<String> updatedParts) {
		setAuditSet(AuditSet.SECURITY);
		setDomain(role.getDomain());
		setObjectId((new ObjectId(role)).toString());
		setKbeeClass(Role.class.getName());
		setTitle(role.getName());
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
		setParameters(getDescription(updatedParts));		
	}

	public SecurityEvent(Role role, String description) {
		setAuditSet(AuditSet.SECURITY);
		setDomain(role.getDomain());
		setObjectId((new ObjectId(role)).toString());
		setKbeeClass(Role.class.getName());
		setTitle(role.getName());
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
		setParameters(description);		
	}
	
	public SecurityEvent(Principal principal) {
		setAuditSet(AuditSet.SECURITY);
		setPrincipal(principal);
		if (principal instanceof User)
			setKbeeClass(User.class.getName());
		else if (principal instanceof Group)
			setKbeeClass(Group.class.getName());
	}
	
	public void setPrincipal(Principal principal) {
		setDomain(((KbeePrincipal)principal).getDomain());
		setObjectId((new ObjectId(principal)).toString());
		setTitle(principal.getName());
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	
	
	// Deprecated
	//
	@Override
	public String getEventType() {
		return "Security";
	}

	@Override
	public String getType() {
		return "Security";
	}
	
	public String getContentId() {
		return null;
	}

	/**
	 * User, Group, Rule
	 */
	@Override
	public String getObjectClass() {
		return "Security"; // User, Group, Rule 
	}
	
	@Override
	public String toString() {
		return getAction()+ " | " + getTarget();
	}
	
	@Override
	public String getTarget() {
		try {
			ObjectId oid = (new ObjectId(getObjectId()));
			String id = oid.getId();
			return getKbeeClass() + " - "  + id;
		} 
		catch( Throwable e) {
			return getClass().getSimpleName();
		}
	}

}
