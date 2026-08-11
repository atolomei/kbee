package com.novamens.content.test.dao;

import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.test.model.Message;

public class MessageDao {
	
	private SessionFactory sessionFactory;
	
	public MessageDao(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
 
	@Transactional
	public void save(Message message){
		sessionFactory.getCurrentSession().save(message);
	} 
 
	public void update(Message message){
		//getHibernateTemplate().update(message);
	}
 
	public void delete(Message message){
		//getHibernateTemplate().delete(message);
	}
 
	public Message findById(Long id){
		return (Message) sessionFactory.
				getCurrentSession().
				get(Message.class, id);
	}
 
}