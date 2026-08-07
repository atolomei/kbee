package com.novamens.security;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Set;


import com.novamens.security.acl.Group;

public interface Principal extends Identifiable, Auditable {
	
	public String getDisplayName();
		
	public Serializable getId();	
	public Set<Group> getGroups();
	
	public OffsetDateTime getCreationOffsetDateTime();
	public void setCreationOffsetDateTime(OffsetDateTime lastModifiedOffsetDate);
	
	public OffsetDateTime getLastModifiedOffsetDateTime();
	public void setLastModifiedOffsetDateTime(OffsetDateTime lastModifiedOffsetDate);
	
	public User getLastModifiedUser();
	public void setLastModifiedUser(User lastModifiedUser);
	
	   public boolean equals(Object another);
	    public String toString();
	    public int hashCode();
	    public String getName();
//	    public default boolean implies(Subject subject) {
//	        if (subject == null)
//	            return false;
//	        return subject.getPrincipals().contains(this);
//	    }
	
}
