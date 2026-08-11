package com.novamens.content.social;

import java.io.IOException;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.dao.Dao;
import com.novamens.security.User;

public interface SocialDao  extends Dao  {
	
	public void save(Comment comment);
	
	public List<Comment> findCommentsByContent(Content content);
	public List<Comment> findCommentsResponses(Comment comment);
	
	
	public void save(Vote vote);
	public List<Vote> findVotesByUser(Content content, User user);
					
	public void save(Report report);
	public List<Report> findReportsByUser(Content content, User user);
	
	public List<Comment> findRecentCommentsBySiteOId(Long site_oid);

	int getTotalComments(Content content) throws IOException;
	
}
