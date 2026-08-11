package com.novamens.content.questionanswer;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

import com.novamens.content.base.KnowledgeSharing;
import com.novamens.content.social.Comment;

public interface Question extends KnowledgeSharing, Serializable {

	public static final int OPEN 		= 0;
	public static final int CLOSED  	= 1;
	public static final int DISABLED 	= 2; 
	
	public void setTitle(String title);
	public String getTitle();

	public void setText(String title);
	public String getText();
	
	public com.novamens.security.User getUser();
	public void setUser(com.novamens.security.User user);

	public void addVote();
	public void addVote(int n);
	public int getVotes();

	public List<Comment> getComments();
	public void addComment(Comment comment);
	public void removeComment(Comment comment);
	public void removeComments();

	public List<Answer> getAnswers();
	public void addAnswer(Answer answer);
	public void setAnswers(List<Answer> answers);
	
	public int getNumAnswers();
	public int decreaseNumAnswers();
	
	public OffsetDateTime  getDateSubmitted();	
	public void setDateSubmitted(OffsetDateTime  date);
	
	public int getQuestionState();	
	public void setQuestionState(int qstate);
	
	public boolean wasEdited();
	public OffsetDateTime  getDateEditedByAdmin();
	public void setDateEditedByAdmin(OffsetDateTime  date);
	
}
