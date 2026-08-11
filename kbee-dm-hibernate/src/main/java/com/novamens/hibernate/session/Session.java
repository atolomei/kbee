package com.novamens.hibernate.session;

import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.novamens.beans.BeansService;
import com.novamens.service.ServiceLocator;

public class Session {
	
	private static ThreadLocal<Boolean> api = new ThreadLocal<Boolean>();
	
	public static SessionFactory open() {
		SessionFactory sessionFactory = getSessionFactory();
		if(!TransactionSynchronizationManager.hasResource(sessionFactory)) {
			org.hibernate.Session session = sessionFactory.openSession();
			session.setHibernateFlushMode(FlushMode.COMMIT);
			TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
		}	
		return sessionFactory;
	}
	
	public static void setApi(Boolean value) {
		api.set(value);
	}
	
	public static boolean isApi() {
		return api.get()!=null && api.get();
	}	
		
	public static org.hibernate.Session get() {
		SessionFactory sessionFactory = getSessionFactory();
		if(TransactionSynchronizationManager.hasResource(sessionFactory)) {
			SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
			return sessionHolder.getSession();
		}
		return null;
	}
	
	public static void close() {
		SessionFactory sessionFactory = getSessionFactory();
		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);
		SessionFactoryUtils.closeSession(sessionHolder.getSession());
	}
	
	private static SessionFactory getSessionFactory() {
		BeansService beans = ServiceLocator.getService(BeansService.class);
		SessionFactory sessionFactory = (SessionFactory)beans.getBean("sessionFactory");
		return sessionFactory;
	}
}