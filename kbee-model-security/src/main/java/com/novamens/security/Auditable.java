package com.novamens.security;

import java.time.OffsetDateTime;

import com.novamens.security.audit.AuditSet;

public interface Auditable {

	public void setDefaultAudit();
	public AuditSet getAuditSet();

	public void setLastModifiedUser(User user);
	public User getLastModifiedUser();
	
	public void setCreationOffsetDateTime(OffsetDateTime date);
	public OffsetDateTime getCreationOffsetDateTime();
	
	public void setLastModifiedOffsetDateTime(OffsetDateTime date);
	public OffsetDateTime getLastModifiedOffsetDateTime();
	
	public String getCreationOffsetDateTimeColloquial();
	
	public default String getLastModifiedOffsetDateTimeColloquial() {return getLastModifiedOffsetDateTimeColloquial("ago"); }
	public String getLastModifiedOffsetDateTimeColloquial(String css);
	

	
	
	
}