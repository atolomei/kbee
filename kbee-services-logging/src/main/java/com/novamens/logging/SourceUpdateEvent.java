package com.novamens.logging;

import com.novamens.content.base.Source;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.List;

@Entity
@DiscriminatorValue("SourceUpdateEvent")
public class SourceUpdateEvent extends SourceEvent {

	public SourceUpdateEvent() {
		super();
	}

	public SourceUpdateEvent(Source source, String description) {
		super();
		setSource(source);
		setParameters(description);
	}

	public SourceUpdateEvent(Source source, List<String> updatedParts)  {
		super(source, updatedParts);
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
