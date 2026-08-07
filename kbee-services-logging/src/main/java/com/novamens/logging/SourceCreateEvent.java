package com.novamens.logging;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.base.Source;

@Entity
@DiscriminatorValue("SourceCreateEvent")
public class SourceCreateEvent extends SourceEvent {
			
	public SourceCreateEvent() {
		super();
	}

	public SourceCreateEvent(Source source, String description) {
		super();
		setSource(source);
		setParameters(description);
	}

	public SourceCreateEvent(Source source, List<String> updatedParts)  {
		super(source, updatedParts);
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
