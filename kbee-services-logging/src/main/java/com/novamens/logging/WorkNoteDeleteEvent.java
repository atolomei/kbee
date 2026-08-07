package com.novamens.logging;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.notes.Billboard;

@Entity
@DiscriminatorValue("WorkNoteDeleteEvent")
public class WorkNoteDeleteEvent extends WorkNoteEvent {

	
	public WorkNoteDeleteEvent() {
	}
	
	public WorkNoteDeleteEvent(Billboard note) {
			super(note, true);
	}
	
	@Override
	public String getAction() {
		return "Delete";
	}
}
