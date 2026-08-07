package com.novamens.logging;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.base.Content;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@Entity
@DiscriminatorValue("CheckoutEvent")
public class CheckoutEvent extends ContentEvent {

	public CheckoutEvent() {
	}
	
	public CheckoutEvent(Content content) {
		super();
		setContent(content);
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	@Override
	public String getEventType() {
		return "Checkout";
	}
	
	@Override
	public String getType() {
		return "Content";
	}
	
	@Override
	public String getObjectClass() {
		return "Content"; // o lo que sea !!!  VER
	}

	@Override
	public String getAction() {
		return "Checkout";
	}
}