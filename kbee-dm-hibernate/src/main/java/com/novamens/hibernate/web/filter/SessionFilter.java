package com.novamens.hibernate.web.filter;

import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.hibernate5.support.OpenSessionInViewFilter;

import com.novamens.beans.BeansService;
import com.novamens.service.ServiceLocator;

public class SessionFilter extends OpenSessionInViewFilter {
	
	@Override
	protected SessionFactory lookupSessionFactory() {
		BeansService beans = ServiceLocator.getService(BeansService.class);
		SessionFactory sf = (SessionFactory)beans.getBean("sessionFactory");
		return sf;
	}
	
	protected Session openSession(SessionFactory sessionFactory) throws DataAccessResourceFailureException {
		try {
			Session session = sessionFactory.openSession();
			session.setFlushMode(FlushMode.COMMIT);
			return session;
		}
		catch (HibernateException ex) {
			throw new DataAccessResourceFailureException("Could not open Hibernate Session", ex);
		}
	}
}
