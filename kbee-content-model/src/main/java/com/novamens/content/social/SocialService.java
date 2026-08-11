package com.novamens.content.social;

import java.util.List;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.security.User;
import com.novamens.service.ObjectService;

/**
 * <p>Social Services include {@link Vote}, {@link Comment} and {@link Report} contents</p>
 * 
 * Checked Exceptions {@link ContentMgmtException}  are propagated to the calling object. 
 *  
 */
public interface SocialService extends ObjectService {
	
	public int getVotes();
	public void addVote();
	public void report();
	public int getReports();

	public boolean hasVotedSessionUser();
	
	public void update(Comment comment) throws ContentMgmtException;
	public void delete(Comment comment) throws ContentMgmtException;

	public List<Comment> getComments();

	public List<Comment> getCommentsResponses(Comment comment);
	
	public Comment addComment(String xt, Long site_id, Comment parent) 		throws ContentMgmtException;
	public Comment addComment(String text, Comment parent) 					throws ContentMgmtException;
	
	public int getTotalComments();
	public void notifyCommentResponded(Comment parent, Comment response, User user_response);
	public void notifyReportComment(Comment comment, String to_email);
	
}
