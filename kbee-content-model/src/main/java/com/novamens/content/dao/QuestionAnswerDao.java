package com.novamens.content.dao;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import com.novamens.content.questionanswer.Answer;
import com.novamens.content.questionanswer.Question;
import com.novamens.content.questionanswer.QuestionStat;
import com.novamens.dao.Dao;
import com.novamens.dom.Domain;

public interface QuestionAnswerDao extends Dao {

 	public Question findQuestionById(Serializable id);
 	public Question findQuestionByName(String name, Serializable domainid);
 	
	public Answer findAnswerById(Serializable id);
	public Answer findAnswerByName(String name, Serializable domainid);
	
	public QuestionStat findQuestionStat(Question question);
	
	public void save(Question question) throws IOException;
	public void delete(Question question) throws IOException;
					
	public void save(Answer answer) throws IOException;
	public void delete(Answer answer) throws IOException;
					
	public void save(QuestionStat stat) throws IOException;
	public void delete(QuestionStat stat) throws IOException;
 	
	public List<Question> getFeatured(Domain domain);
	public List<Question> getNews(Domain domain);
}
