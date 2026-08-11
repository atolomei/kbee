package com.novamens.content.test.dao;



import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.test.model.Article;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class ArticleDao {
	
	private SessionFactory sessionFactory;
	
	public ArticleDao() {
	}
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
 
	@Transactional
	public void save(Article article) {
		article.setLastModifiedUser(getSessionUser());
		sessionFactory.getCurrentSession().save(article);
	} 
 
	public void update(Article article){
		sessionFactory.getCurrentSession().save(article);
	}
 
	public void delete(Article article){
	}
 
	@SuppressWarnings("rawtypes")
	public Article findByArticleId(String id){
		Query query = sessionFactory.getCurrentSession().createQuery("from Article where articleid=:articleid");
		query.setParameter("articleid", id);
		List list = query.list();
		if (list.isEmpty()) return null;
		return (Article)list.get(0);
	}
	
	public Article findById(Long id){
		return (Article) sessionFactory.
				getCurrentSession().
				load(Article.class, id);
	}
	
	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
 
}