package com.novamens.logging;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.notes.Billboard;

@Entity
@DiscriminatorValue("WorkNoteUpdateEvent")
public class WorkNoteUpdateEvent extends WorkNoteEvent {

	public WorkNoteUpdateEvent() {
	}

	public WorkNoteUpdateEvent(Billboard note) {
		this(note, false);
	}
	
	public WorkNoteUpdateEvent(Billboard note, boolean is_silent) {
		super(note, is_silent);
	}
	
	@Override
	public String getAction() {
		return "Update";
	}
}
