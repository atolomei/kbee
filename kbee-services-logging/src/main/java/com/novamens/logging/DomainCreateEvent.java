package com.novamens.logging;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.dom.Domain;


@Entity
@DiscriminatorValue("DomainCreateEvent")
public class DomainCreateEvent extends DomainEvent {

 

	public DomainCreateEvent() {
		super();
	}
	
	
	public DomainCreateEvent(Domain domain) {
		super();
		setDomain(domain);
	}
	
	
	public DomainCreateEvent(Domain domain, String description){
		super(domain, description);
	}
	
	
	public DomainCreateEvent(Domain domain, List<String> updatedParts)  {
		setDomain(domain);
		setParameters(getDescription(updatedParts));
	}
	
	
	@Override
	public String toString() {
		return getAction()+ " | " + getTarget();
	}
	
	
	@Override
	public String getAction() {
		return "Create";
	}
}
