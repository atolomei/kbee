package com.novamens.content.properties;

import java.io.Serializable;
import java.time.OffsetDateTime;
 

import com.novamens.content.base.Content;
import com.novamens.security.User;

/**
 * Strings properties associated to {@code Content} objects.
 * Each property has a key that is a String and value that is also a String.
  
 */
public interface ContentProperties extends Serializable {
		
	public Content getContent();
	public void setContent(Content content);
	
	public void setProperty(String key, String value);
	public String getProperty(String key);
	public void removeProperty(String key);
	

	public OffsetDateTime getLastmodifiedOffsetDateTime();
	public void setLastModifiedOffsetDateTime(OffsetDateTime date);
	
	public User getLastModifiedUser();
	public void setLastModifiedUser(User user);
	
}
