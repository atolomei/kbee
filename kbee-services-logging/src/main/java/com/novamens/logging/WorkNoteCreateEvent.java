package com.novamens.logging;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.notes.Billboard;


@Entity
@DiscriminatorValue("WorkNoteCreateEvent")
public class WorkNoteCreateEvent extends WorkNoteEvent {
	
	public WorkNoteCreateEvent() {
	}
	
	public WorkNoteCreateEvent(Billboard note) {
			this(note, false);
	}
	
	public WorkNoteCreateEvent(Billboard note, boolean is_silent) {
		super(note, is_silent);
	}

	@Override
	public String getAction() {
		return "Create";
	}
	

}
