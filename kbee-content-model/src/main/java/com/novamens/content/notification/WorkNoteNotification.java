package com.novamens.content.notification;

import com.novamens.content.notes.Billboard;
import com.novamens.content.resource.KBFile;

public interface WorkNoteNotification extends Notification {

	public Billboard getWorkNote();
	public void setWorkNote(Billboard note);
	public KBFile getSideImage();

}
