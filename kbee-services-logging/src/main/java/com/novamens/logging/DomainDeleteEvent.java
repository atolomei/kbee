package com.novamens.logging;



import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.dom.Domain;

@Entity
@DiscriminatorValue("DomainDeleteEvent")
public class DomainDeleteEvent extends DomainEvent {
	 

	public DomainDeleteEvent() {
		super();
	}
	
	public DomainDeleteEvent(Domain domain)  {
		super();
		setDomain(domain);
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
