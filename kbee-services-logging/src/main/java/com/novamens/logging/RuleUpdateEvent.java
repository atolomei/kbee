package com.novamens.logging;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.base.Rule;
import com.novamens.content.model.ObjectId;
import com.novamens.content.user.UserService;
import com.novamens.service.ServiceLocator;


@Deprecated
@Entity
@DiscriminatorValue("RuleUpdateEvent")
public class RuleUpdateEvent extends SecurityEvent {
	 
	public RuleUpdateEvent() {
		super();
	}
	
	public RuleUpdateEvent(Rule rule, List<String> updatedParts) {
		super(rule, updatedParts);
		setObjectId((new ObjectId(rule)).toString());
		setTitle(rule.getName());
		setKbeeClass("Rule");
		setEventUser(ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser());
	}
	
	@Override
	public String getEventType() {
		return "RuleUpdate";
	}
	
	@Override
	public String getType() {
		return "Security";
	}

	@Override
	public String getAction() {
		return "Update";
	}
	
	@Override
	public String getObjectClass() {
		return "Rule";
	}
	
	
	
	

}
