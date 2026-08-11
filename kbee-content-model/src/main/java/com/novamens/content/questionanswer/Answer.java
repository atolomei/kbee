package com.novamens.content.questionanswer;

import java.io.Serializable;
import java.time.OffsetDateTime;
 
import java.util.List;

import com.novamens.content.base.KnowledgeSharing;
import com.novamens.content.social.Comment;
import com.novamens.dom.Domain;

public interface Answer extends KnowledgeSharing, Serializable {
	
	public void setQuestion (Question content);
	public Question getQuestion();
	
	public void setTitle(String title);
	public String getTitle();
					
	public void setText(String text);
	public String getText();
	
	public com.novamens.security.User getUser();
	public void setUser(com.novamens.security.User user);
	 
	public void setAccepted(boolean b);
	public boolean getAccepted();
	
	public OffsetDateTime getDateAccepted();
	public void setDateAccepted(OffsetDateTime date);
	
	public void addVote();
	public void addVote(int n);
	public int getVotes();
	
	public List<Comment> getComments();
	public void addComment(Comment comment);
	public void removeComment(Comment comment);
	
	public Domain getDomain();
	public void setDomain(Domain domain);
	
	public OffsetDateTime getDateSubmitted();	
	public void setDateSubmitted(OffsetDateTime date);
	
	public boolean wasEdited();
	public OffsetDateTime getDateEditedByAdmin();
	public void setDateEditedByAdmin(OffsetDateTime date);
	
}
