package com.novamens.logging;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.library.Library;

@Entity
@DiscriminatorValue("CabinetUpdateEvent")
public class LibraryUpdateEvent extends LibraryEvent {
	
	public LibraryUpdateEvent() {
		super();
	}
	
	public LibraryUpdateEvent(Library cabinet, String description) {
		super();
		setCabinet(cabinet);
		setParameters(description);
	}
	
	public LibraryUpdateEvent(Library cabinet, List<String> updatedParts)  {
		super(cabinet, updatedParts);
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
