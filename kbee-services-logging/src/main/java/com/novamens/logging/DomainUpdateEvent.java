package com.novamens.logging;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.dom.Domain;

@Entity
@DiscriminatorValue("DomainUpdateEvent")
public class DomainUpdateEvent extends DomainEvent {

	public DomainUpdateEvent() {
		super();
	}
	
	public DomainUpdateEvent(Domain domain, String description) {
		super();
		setDomain(domain);
		setParameters(description);
	}
	
	public DomainUpdateEvent(Domain domain, List<String> updatedParts)  {
		super();
		setDomain(domain);
		setParameters(getDescription(updatedParts));
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