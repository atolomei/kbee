package com.novamens.kbee.content.util;

import org.apache.logging.log4j.LogManager;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;

public class ContentFinder {
	private ContentDao dao;
	SessionFactory sessionFactory;
	
	private org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getName());

	public ContentFinder() {
	}
	
	public void setContentDao(ContentDao dao) {
		this.dao = dao;
	}
	
	public void setSessionFactory(SessionFactory factory) {
		sessionFactory = factory;
	}
	
	public Content find(String contentId) {
		TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(sessionFactory.openSession()));
		Content content = dao.findContentByOId(Long.valueOf(contentId));
		if (content==null) logger.warn("Content "+contentId +" not found");
		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);
		SessionFactoryUtils.closeSession(sessionHolder.getSession());
		return content;
	}
}
