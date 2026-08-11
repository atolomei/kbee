package com.novamens.content.social;

import java.time.OffsetDateTime;
import com.novamens.content.base.Content;
import com.novamens.content.base.KnowledgeSharing;
import com.novamens.security.User;



public interface Comment extends KnowledgeSharing {

	public void setReferencedContent(Content content);
	
 	public void setTitle(String title);
	public String getTitle();
				
	public void setText(String text);
	public String getText();

	/** User that edited the comment for the last time 
	*/
	public void setUser(User user);

	
	/** User that edited the comment for the last time 
	*/
	public User getUser();
	
	public OffsetDateTime getDateSubmitted();	
	public void setDateSubmitted(OffsetDateTime date);
	
	public boolean isEditable();
	
	public void setSiteOId(Long site_oid);
	public Long getSiteOId();
	
	public Comment getParent();
	
	public void setParent(Comment parent);
	public boolean isFirstLevel();
	public int getLevel();
	
	public Content getReferencedContent();
	
}
