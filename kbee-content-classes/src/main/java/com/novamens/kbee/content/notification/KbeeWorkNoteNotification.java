package com.novamens.kbee.content.notification;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.notes.Billboard;
import com.novamens.content.notification.NotificationType;
import com.novamens.content.notification.WorkNoteNotification;
import com.novamens.content.resource.KBFile;
import com.novamens.kbee.content.notes.KbeeBillboard;


@Entity
@DiscriminatorValue("20") // NotificationType.WORK_NOTE 
public class KbeeWorkNoteNotification extends KbeeNotification implements WorkNoteNotification {

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeBillboard.class)
	@Fetch(FetchMode.JOIN)
	@JoinColumn(name ="work_note_id", nullable=true)
	private Billboard billboard = null;
	
	@Column(name = "isbillboard")
	private boolean isbillboard = false;
	
	@Column(name = "isalert")
	private boolean isalert = true;
	
	public KbeeWorkNoteNotification() {
	}
	
	public KbeeWorkNoteNotification(Billboard  billboard) {
		this. billboard= billboard;
	}
	
	@Override
	public Billboard getWorkNote() {
		return  billboard;
	}

	@Override
	public void setWorkNote(Billboard note) {
		this. billboard=note;
	}

	@Override
	public NotificationType getNotificationType() {
		return NotificationType.WORK_NOTE;
	}
	
	@Override
	public boolean isBillboard() {
		return isbillboard;
	}

	public void setBillboard(boolean isbillboard) {
		this.isbillboard = isbillboard;
	}

	@Override
	public boolean isAlert() {
		return isalert;
	}

	public void setAlert(boolean isalert) {
		this.isalert = isalert;
	}
	
	@Override
	public String getTypeStr() {
		return NotificationType.WORK_NOTE_BILLBOARD.getLabel(getSessionUser().getLocale());
	}

	@Override
	public String getIcon() {
		return isBillboard() ? "fal fa-bullhorn" : "fal fa-fw fa-mailbox";
	}
	
	/**
	 * These values are reduntant, for efficiency,
	 * they take their value from the WorkNote
	 */
	@Override
	public KBFile getFile() {
		if (billboard!=null)
			return billboard.getFile();	
		 return null;
	 }
	
	@Override
	public String getUrl() {
		return null;
	}
	
	@Override
	public KBFile getSideImage() {
		if (billboard!=null)
			 return billboard.getSideImage();	
		 return null;
	 }
}
