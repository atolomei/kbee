package com.novamens.content.notification;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Locale;

import com.novamens.content.resource.KBFile;
import com.novamens.dom.DomainObject;
import com.novamens.dom.Indexable;
import com.novamens.dom.Object;
import com.novamens.security.Identifiable;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;

/**
 * Internal Notifications that are displayed by the Notifications Panel in the application.
 * The System keeps record of when they are accepted.
 * 
 * Task
 * Content
 * Work Note
 * System 
 *
 */
public interface Notification extends Object, Identifiable, Indexable, DomainObject {
	
	public Long getId();

	public String getTitle();
	
	public String getSubject(Locale locale);
	
	public String getText();
	
	public User getSender();

	public User getReceiver();
	
	public OffsetDateTime getOffsetDateTimeSent();
	
	public OffsetDateTime getDateRead();
	public void setOffsetDateTimeRead(OffsetDateTime read);
	
	public NotificationState getNotificationState();
	public void setNotificationState(NotificationState state);
	
	public String getUrl();
						
	default public KBFile getFile() {return null;}
	
	public NotificationType getNotificationType();
	public String getIcon();
	
	public boolean deleteOnAccept();
	public boolean isBillboard();
	public boolean isAlert();
	
	public OffsetDateTime getStartpub();
	public OffsetDateTime getEndpub();
	public String getTypeStr();

	public void setGeneratingENotiRule(Serializable eid);
	public Serializable getGeneratingENotiRule();
	
	public void setGeneratingActionRule(Serializable eid);
	public Serializable getGeneratingActionRule();

	public default AuditSet getAuditSet() {
		return AuditSet.SYSTEM;
	}
}