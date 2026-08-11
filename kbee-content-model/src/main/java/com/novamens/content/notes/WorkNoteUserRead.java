package com.novamens.content.notes;

import java.time.OffsetDateTime;

import com.novamens.security.User;

public interface WorkNoteUserRead {

	public User getUser();
	public Billboard getWorkNote();
	
	public OffsetDateTime getOffsetDateTimeRead();
	public void setOffsetDateTimeRead(OffsetDateTime  date);
}
