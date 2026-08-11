package com.novamens.content.base;

import java.time.OffsetDateTime;

import com.novamens.security.User;

/**
 *  <p>Social elements: {@link Vote} and {@link Report}.  
 *  They are not subclasses of {@link Content} and therefore are not versioned.</p> 
 *
 */
public interface Social {

	public Content getContent();
	public void setContent(Content content);
	
	public OffsetDateTime getOffsetDateTime();
	public User getUser();
	public void setOffsetDateTime(OffsetDateTime date);
	public void setUser(User user);


}
