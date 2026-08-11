package com.novamens.content.notes;

import java.time.OffsetDateTime;

import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.security.Identifiable;
import com.novamens.security.User;

public interface UserNote extends DomainObject, Identifiable {

	public void setId(Long id);
	public Long getId();

	public User getUser();
	
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

	public Domain getDomain();
	
	public void setLastModifiedUser(User user);
	public User getLastModifiedUser();
	
	public String getCreationOffsetDateTimeColloquial();
	public String getLastModifiedOffsetDateTimeColloquial();
	public String getLastModifiedOffsetDateTimeColloquialAgo();
	public String getCreationOffsetDateTimeColloquialAgo();
	
	
}
