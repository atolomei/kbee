package com.novamens.logging;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.library.Library;

@Entity
@DiscriminatorValue("CabinetCreateEvent")
public class LibraryCreateEvent extends LibraryEvent {

	public LibraryCreateEvent() {
		super();
	}
	
	public LibraryCreateEvent(Library cabinet, String description) {
		super();
		setCabinet(cabinet);
		setParameters(description);
	}
	
	public LibraryCreateEvent(Library cabinet, List<String> updatedParts)  {
		super(cabinet, updatedParts);
	}
	
	@Override
	public String getAction() {
		return "Create";
	}

	@Override
	public String toString() {
		return getAction()+ " | " + getTarget();
	}
}
