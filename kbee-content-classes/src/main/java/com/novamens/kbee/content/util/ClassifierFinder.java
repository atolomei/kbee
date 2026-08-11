package com.novamens.kbee.content.util;

import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classifier;

public class ClassifierFinder {
	private ContentDao dao;
	SessionFactory sessionFactory;
	
	public ClassifierFinder() {
	}
	
	public void setContentDao(ContentDao dao) {
		this.dao = dao;
	}
	
	public void setSessionFactory(SessionFactory factory) {
		sessionFactory = factory;
	}
	
	public Classifier find(String classifiername, String domainid) {
		TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(sessionFactory.openSession()));
		Classifier classifier = (Classifier)dao.findModelObjectByName(Classifier.class, classifiername, Long.valueOf(domainid));
		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);
		SessionFactoryUtils.closeSession(sessionHolder.getSession());
		
		return classifier;
	}
}
