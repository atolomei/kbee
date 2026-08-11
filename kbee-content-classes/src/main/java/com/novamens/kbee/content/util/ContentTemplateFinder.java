package com.novamens.kbee.content.util;

import org.apache.logging.log4j.LogManager;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ContentTemplate;
import com.novamens.dom.Domain;

public class ContentTemplateFinder {
	private ContentDao dao;
	SessionFactory sessionFactory;
	
	private org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getName());

	public ContentTemplateFinder() {
	}
	
	public void setContentDao(ContentDao dao) {
		this.dao = dao;
	}
	
	public void setSessionFactory(SessionFactory factory) {
		sessionFactory = factory;
	}
	
	public ContentTemplate find(String templatename, String domainid) {
		TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(sessionFactory.openSession()));
		ContentTemplate template = (ContentTemplate)dao.findContentTemplateByName(templatename, domainid);
		if (template==null) logger.warn("Template "+templatename +" not found in domain " + domainid);
		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);
		SessionFactoryUtils.closeSession(sessionHolder.getSession());
		return template;
	}
	
	public ContentTemplate findByDomainName(String templatename, String domainname) {
		TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(sessionFactory.openSession()));
		Domain domain = null;
		ContentTemplate template = null;
		for (Domain d : dao.getDomains()) {
			if (d.getName().equals(domainname)) {
				domain = d;
				break;
			}
		}
		if (domain !=null) {
			template = (ContentTemplate)dao.findContentTemplateByName(templatename, domain.getId());
		}
		if (template==null) logger.warn("Template "+templatename +" not found in domain " + domainname);
		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);
		SessionFactoryUtils.closeSession(sessionHolder.getSession());
		return template;
	}

}
