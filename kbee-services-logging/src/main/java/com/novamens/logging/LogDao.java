package com.novamens.logging;

import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.dao.Dao;
import com.novamens.event.LogEvent;

public class LogDao  implements Dao {
	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Transactional
	public void update(LogEvent event) {
		sessionFactory.getCurrentSession().save(event);
	}
}
