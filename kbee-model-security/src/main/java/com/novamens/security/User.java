package com.novamens.security;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.novamens.security.acl.Group;

public interface User extends Principal {
	
	public Serializable getId();
	
	public String getDisplayName();
	public String getLastFirstName();
	public String getFirstLastName();
	public String getFirstName();
	public String getLasName();
	
	
	public String getUserName();
	public String getPassword();
	
	
	public boolean isEnabled();             	// ObjectState=ENABLED 
	public void setStateEnabled();
	
	public boolean isArchived();             	// ObjectState=ENABLED
	public void setStateArchived();
	
	public void setStateDeleted();
	public boolean isDeleted();             	// ObjectState=DELETED
	
	
	
	// ObjectState=ENABLED
	public boolean isActive();
	public void setActive(boolean enabled);

	
	public void setGroups(Set<Group> groups);
	public Set<Group> getGroups();
	public void addGroup(Group group);
	public void removeGroup(Group group);
	public boolean isMember(Group group);
	
	public void setLocale(String locale_str);
	public void setLocale(Locale locale);
	public Locale getLocale();

	public String getTimeZone();
	public void  setTimeZone(String tz);
	public OffsetDateTime getValidityAccessDate();

	
	/**
	 * For root, workflow and other users that can not be deleted 
	 */
	public boolean isCanonical();

	List<Group> getStandardGroups();
	
	
	public ZoneId getZoneId();

	default OffsetDateTime getPasswordLastModifiedDate() { return null;}

	

	 

	
}
