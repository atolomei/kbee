package com.novamens.content.test.dao;

import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.test.model.Author;

public class AuthorDao {
	
	private SessionFactory sessionFactory;
	
	public AuthorDao() {
		//this.sessionFactory = sessionFactory;
	}
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
 
	@Transactional
	public void save(Author author){
		sessionFactory.getCurrentSession().save(author);
	} 
 
	@Transactional
	public void add(Author author){
		sessionFactory.getCurrentSession().save(author);
	}
	
	public void update(Author author){
		//getHibernateTemplate().update(message);
	}
 
	public Author findById(String id){
		return (Author) sessionFactory.
				getCurrentSession().
				get(Author.class, id);
	}
 
}