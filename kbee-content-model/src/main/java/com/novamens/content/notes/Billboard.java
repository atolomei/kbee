package com.novamens.content.notes;

import java.time.OffsetDateTime;
import java.util.List;

import com.novamens.content.resource.KBFile;
import com.novamens.content.security.Role;
import com.novamens.dom.DomainObject;
import com.novamens.dom.Indexable;
import com.novamens.scheduler.CronExpressionJ8;
import com.novamens.security.Auditable;
import com.novamens.security.Identifiable;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;

/**
 * Nota 
 * 
 * Alerta
 * Billboard
 * 
 * email
 *
 */
public interface Billboard extends Identifiable, Auditable, DomainObject, Indexable {

	public void setId(Long id);
	public Long getId();

	public User getUser();
	
	public CronExpressionJ8 getCronExpression();
	public void setCronExpression(CronExpressionJ8 cronExpression);
	
		
	public List<Principal> getReceivers();
	public void setReceivers(List<Principal> receivers);
	public List<Role> getRoleReceivers();
	
	public String getTitle();
	public void setTitle(String title);

	public String getText();
	public void setText(String text);
	
	public String getPriority();
	public void setPriority(String priority);
	
	public OffsetDateTime getCreationOffsetDateTime();
	public void setCreationOffsetDateTime(OffsetDateTime date);
						
	public OffsetDateTime getModifiedOffsetDateTime();
	public void setLastModifiedOffsetDateTime(OffsetDateTime date);
	
	public void setLastModifiedUser(User user);
	public User getLastModifiedUser();
	
	public String getCreationOffsetDateTimeColloquial();
	public String getLastModifiedOffsetDateTimeColloquial();
	public String getLastModifiedOffsetDateTimeColloquialAgo();
	public String getCreationOffsetDateTimeColloquialAgo();
	
	boolean isSendNotification();
	boolean isEmail();
	boolean isAlert();
	boolean isBillboard();
	String getGlyphicon();
	
	OffsetDateTime getStartpub();
	OffsetDateTime getEndpub();
	
	public void setTimeZone(String timeZone);
	public String getTimeZone();
	
	public void setFile(KBFile kfile);
	public KBFile getFile();
	
	public void setSideImage(KBFile kfile);
	public KBFile getSideImage();
	
	public default AuditSet getAuditSet() {
		return AuditSet.SYSTEM;
	}
}