package com.novamens.kbee.content.questionanswer;


import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classification;
import com.novamens.content.questionanswer.Answer;
import com.novamens.content.questionanswer.Question;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.social.Comment;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.event.EventService;
import com.novamens.kbee.content.social.KbeeKnowledgeSharingEvent;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class KbeeQuestionAnswerService implements QuestionAnswerService  {
										
	static Map<QuestionAnswerPermission, Double> access_threshold  = new HashMap<QuestionAnswerPermission, Double>();
	
	static private Logger logger = LogManager.getLogger(KbeeQuestionAnswerService.class.getName());

	private static final String ROLE_DOMAIN_ADMIN  = KbeeGlobalRole.DOMAIN_ADMIN.getId();

	static {
		
		// junior 
		//
		access_threshold.put(QuestionAnswerPermission.CREATE_QUESTION	, Double.valueOf(10));
		access_threshold.put(QuestionAnswerPermission.CREATE_COMMENT 	, Double.valueOf(10));
		access_threshold.put(QuestionAnswerPermission.CREATE_ANSWER		, Double.valueOf(10));
		access_threshold.put(QuestionAnswerPermission.VOTE				, Double.valueOf(10));
		
		// semi-senior
		//
		access_threshold.put(QuestionAnswerPermission.REPORT			, Double.valueOf(30));
		
		// senior
		//
		access_threshold.put(QuestionAnswerPermission.DELETE_ANSWER		, Double.valueOf(60));
		access_threshold.put(QuestionAnswerPermission.DELETE_COMMENT	, Double.valueOf(60));
		access_threshold.put(QuestionAnswerPermission.DELETE_QUESTION	, Double.valueOf(60));
		
		access_threshold.put(QuestionAnswerPermission.EDIT_ANSWER		, Double.valueOf(60));
		access_threshold.put(QuestionAnswerPermission.EDIT_COMMENT		, Double.valueOf(60));
		access_threshold.put(QuestionAnswerPermission.EDIT_QUESTION		, Double.valueOf(60));
		
		// Admin or Authority
		//
		access_threshold.put(QuestionAnswerPermission.LOCK_QUESTION		, Double.valueOf(80));
		access_threshold.put(QuestionAnswerPermission.UNLOCK_QUESTION	, Double.valueOf(80));

		access_threshold.put(QuestionAnswerPermission.CREATE_USER		, Double.valueOf(80));
		access_threshold.put(QuestionAnswerPermission.DELETE_USER 		, Double.valueOf(80));
		access_threshold.put(QuestionAnswerPermission.EDIT_USER			, Double.valueOf(80));
	}
	
	private ContentDao dao = null;
	private SecurityService secu;

	public KbeeQuestionAnswerService() {

	}
	
	@Override
	@Transactional
	public Question sendQuestion(String title, List<Classification> classification, String text)  {
		try {
	
			Question question = (Question)ServiceLocator.getService(ContentFactoryService.class).create("Question", false, true);
			User user = ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
			question.setTitle(title);
			question.setText(text);
			if (classification!=null)
				question.setClassification(classification);
			question.setDateSubmitted(OffsetDateTime.now());
			UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
			question.setDomain(userProfile.getDomain());
			question.setUser(user);
			getContentDao().save(question);
			
			// EVENT
			//
			ServiceLocator.getService(EventService.class).fire(new com.novamens.kbee.content.social.KbeeKnowledgeSharingEvent(question, question));
			
			return question;
		}
		catch (ContentCreationException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			throw new RuntimeException(e);
		}
		catch (Exception e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean hasPermission(UserProfile up, QuestionAnswerPermission action) {
		return up.getConfidenceLevel() >= access_threshold.get(action).doubleValue() || getSecurityService().isMember(ROLE_DOMAIN_ADMIN); 
	}

	/**
	 * @param question
	 * @param text
	 * @return
	 */
	@Override
	@Transactional
	public Answer addAnswer(Question question, String text)  {
		try {

			Answer answer = (Answer)ServiceLocator.getService(ContentFactoryService.class).create("Answer", false, true);
			UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
			answer.setUser(userProfile.getUser());
			answer.setDateSubmitted(OffsetDateTime.now());
			answer.setText(text);

			answer.setTitle(question.getTitle());
			answer.setDomain(userProfile.getDomain());
			question.addAnswer(answer);
			getContentDao().save(question);
			
			// EVENT
			//
			ServiceLocator.getService(EventService.class).fire(new KbeeKnowledgeSharingEvent(question, answer));

			return answer;
		}
		catch (ContentCreationException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			throw new RuntimeException(e);
		}
		catch (Exception e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Actualizamos la Question ?
	 * 
	 * @param answer
	 */
	@Override
	@Transactional
	public void update(Answer answer)  {
		try {
			UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
			answer.setDomain(userProfile.getDomain());
			answer.setUser(userProfile.getUser());
			getContentDao().save(answer);
		}	
		catch (Exception e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			throw new RuntimeException(e);
		}
	}
	
	@Override
	@Transactional
	public void update(Question question)  {
		try {
			UserProfile sessionUser=ServiceLocator.getService(UserService.class).getSessionUserProfile();
			question.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			question.setLastModifiedUser(sessionUser.getUser());
			getContentDao().save(question);
		}	
		catch (Exception e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			throw new RuntimeException(e);
		}
	}
	
	@Override
	@Transactional
	public void delete(Answer answer)  {
		try {
			 Question question = answer.getQuestion();
			 question.getAnswers().remove(answer);
			 question.decreaseNumAnswers();
			 getContentDao().delete(answer);
		}	
		catch (Exception e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());	
			throw new RuntimeException(e);
		}
	}
	
	@Override
	@Transactional
	public void delete(Comment comment)  {
		try {
			getContentDao().delete(comment);
		}	
		catch (Exception e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());	
			throw new RuntimeException(e);
		}
	}
	
	@Override
	@Transactional
	public void delete(Question question)  {
		try {
			getContentDao().delete(question);
		}	
		catch (Exception e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());	
			throw new RuntimeException(e);
		}
	}

	@Override
	@Transactional
	public void update(Comment comment)  {
		try {
			getContentDao().save(comment);
		}	
		catch (Exception e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			throw new RuntimeException(e);
		}
	}

	

	@Override
	public long wipeOut(Domain domain)  {
		long total=0;
		try {
			UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
			if (userProfile.getUser().getUserName().startsWith("root@")) {
				@SuppressWarnings("unchecked")
				List<Question> list = (List<Question>) getContentDao().getContent(Question.class, domain.getId().toString());
				total+=wipe(list,domain);
			}
		}
		catch (RuntimeException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return total;
	}

	@Transactional
	private long wipe(List<Question> list, Domain domain)  {
		long total=0;
		for (Question q: list) {
			try {
				getContentDao().delete(q);
				total++;
			} catch (Exception e) {
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			}
		}
		return total;
	}

	private ContentDao getContentDao() {
		if (dao==null)	 {
			 BeansService beans = ServiceLocator.getService(BeansService.class);
			 dao = (ContentDao) beans.getBean("contentDao");
		 }
		return dao;
	}
	
	private SecurityService getSecurityService() {
		if (secu!=null)
			return secu;
	secu = ServiceLocator.getService(SecurityService.class);
	return secu;
	}
	
}
