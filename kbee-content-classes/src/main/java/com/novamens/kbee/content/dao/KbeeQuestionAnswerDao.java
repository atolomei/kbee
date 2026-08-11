package com.novamens.kbee.content.dao;

import java.io.IOException;
import java.io.Serializable;
import java.time.OffsetDateTime;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.hibernate.query.Query;
import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.dao.QuestionAnswerDao;
import com.novamens.content.questionanswer.Answer;
import com.novamens.content.questionanswer.Question;
import com.novamens.content.questionanswer.QuestionStat;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.questionanswer.KBeeQuestionStat;
import com.novamens.kbee.content.questionanswer.KbeeAnswer;
import com.novamens.kbee.content.questionanswer.KbeeQuestion;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class KbeeQuestionAnswerDao implements QuestionAnswerDao {

	@SuppressWarnings("unused")
	final private org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getName());
	
	private SessionFactory sessionFactory;
	
	static private KbeeQuestionAnswerDao instance = null;
	
	static public QuestionAnswerDao getInstance() {
		if (instance==null) 
			instance = new KbeeQuestionAnswerDao();
		return instance;
	}
	
	private KbeeQuestionAnswerDao() {
	}
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	public  Question findQuestionByName(String name, Serializable domainid) {
		String hql = "FROM KbeeQuestion U WHERE lower(U.name) = '" + name.toLowerCase().trim() + "' AND U.domain.id= '" + domainid.toString() +"'";
		Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
		
		@SuppressWarnings("rawtypes")
		List results = query.list();
		
		if (results.isEmpty())
			return null;
		
		Question question = (Question) results.get(0); 
		return  question;
	}
	
	@Override
	public Question findQuestionById(Serializable id) {
		return (Question) sessionFactory.getCurrentSession().get(KbeeQuestion.class, id);
	}

	@Override
	public Answer findAnswerById(Serializable id) {
		return (Answer) sessionFactory.getCurrentSession().get(KbeeAnswer.class, id);
	}
			
	public  Answer findAnswerByName(String name, Serializable domainid) {
		String hql = "FROM KbeeAnswer U WHERE lower(U.name) = '" + name.toLowerCase().trim() + "' AND U.domain.id= '" + domainid.toString() +"'";
		Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
		
		@SuppressWarnings("rawtypes")
		List results = query.list();
		
		if (results.isEmpty())
			return null;
		
		Answer answer = (Answer) results.get(0); 
		return answer;
	}

	@Override
	// @Transactional
	public void save(Question question) throws IOException {
			setDefaults(question);
			sessionFactory.getCurrentSession().save(question);
	}

	@Override
	public void delete(Question question) throws IOException {
		sessionFactory.getCurrentSession().delete(question);
	}

	@Override
	public void save(Answer answer) throws IOException {
		setDefaults(answer);
		sessionFactory.getCurrentSession().save(answer);
		sessionFactory.getCurrentSession().flush();		
	}

	@Override
	public void delete(Answer answer) throws IOException {
		sessionFactory.getCurrentSession().delete(answer);
	}

	@Override
	public void save(QuestionStat stat) throws IOException {
		sessionFactory.getCurrentSession().save(stat);
	}

	@Override
	public void delete(QuestionStat stat) throws IOException {
		sessionFactory.getCurrentSession().delete(stat);
	}
	
	private void setDefaults(Content object) {

		if (object.getLastModifiedOffsetDateTime()==null)
			object.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		
		if (object.getLastModifiedUser()==null)
			object.setLastModifiedUser(getSessionUser());
		
		if (object.getState()==null)
			object.setState(ObjectState.ENABLED);
		
		 if (object.getDomain()==null) {
			 BeansService beans = ServiceLocator.getService(BeansService.class);
			 ContentDao dao = (ContentDao) beans.getBean("contentDao");
			 Domain domain = dao.getDomain();
			 object.setDomain(domain);
		 }
	}

	@Override
	public QuestionStat findQuestionStat(Question question) {
		return (QuestionStat) sessionFactory.getCurrentSession().get(KBeeQuestionStat.class, question.getId());
	}

	@SuppressWarnings("unchecked")
	public List<Question> getNews(Domain domain) {
		String hql = "FROM KbeeQuestion U WHERE U.domain.id= '" + domain.getId().toString() +"' order by U.date_submitted desc";
		Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
		@SuppressWarnings("rawtypes")
		List results = query.list();
		
		if (results.isEmpty())
			return null;
		
		if (results.size()>5)
			return (List<Question>) results.subList(0, 5);
		else
			return (List<Question>) results;
	}
	
	public List<Question> getFeatured(Domain domain) {
		return null;
	}

	
}
