package com.novamens.kbee.repository;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.Object;
import com.novamens.dom.ObjectState;
import com.novamens.repository.DomRepository;
import com.novamens.service.ServiceLocator;

public abstract class AbstractDomRepository<T extends Object, I> implements DomRepository<I> {
	
	public void save(I object) {
		getSessionFactory().getCurrentSession().save(object);
	}
	
	public void delete(I object) {
		getSessionFactory().getCurrentSession().delete(object);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public I findById(Serializable id) {
		return (I) getSessionFactory().getCurrentSession().get(getTypeOfT(), id);
	}
	
	@Override
	public I findByExternalId(String id) {
		return null;
	}
	
	@Override
	public List<I> findAll() {
		return findAll(getDomain(), ObjectState.ENABLED);
	}
	
	@Override
	public List<I> findAll(ObjectState state) {
		return findAll(getDomain(), state);
	}
	
	@Override
	public List<I> findAll(Domain domain) {
		return findAll(domain, ObjectState.ENABLED);
	}
	
	@Override
	public List<I> findAll(Domain domain, ObjectState state) {
		return findAll(domain, state, null);
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<I> findAll(Domain domain, ObjectState state, String order) {
		if (domain == null) 
			return new ArrayList<I>();
		String st= (state!=null ?  (" and T.state="+String.valueOf(state.getId())+" "):"");
		String od= (order!=null ?  (" order by T."+order) : "");
		
		String classname = getTypeOfT().getName();
		String hql = "FROM "+classname+" T WHERE T.domain.id=" + domain.getId().toString() +" " + st + od; // " order by T.listOrder";// order by T.orden";
		org.hibernate.query.Query<?> query = getSessionFactory().getCurrentSession().createQuery(hql);
		query.setCacheable(true);
		//query.setCacheRegion("entity");
		List results = query.list();
		if (results.isEmpty())
			return new ArrayList<I>();
		return results;	
	}
	
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<I> findAll(Domain domain, String order) {
		if (domain == null) 
			return new ArrayList<I>();
		//String st= (state!=null ?  (" and T.state="+String.valueOf(state.getId())+" "):"");
		String od= (order!=null ?  (" order by T."+order) : "");
		
		String classname = getTypeOfT().getName();
		String hql = "FROM "+classname+" T WHERE T.domain.id=" + domain.getId().toString() +" " +  od; // " order by T.listOrder";// order by T.orden";
		org.hibernate.query.Query<?> query = getSessionFactory().getCurrentSession().createQuery(hql);
		query.setCacheable(true);
		//query.setCacheRegion("entity");
		List results = query.list();
		if (results.isEmpty())
			return new ArrayList<I>();
		return results;	
	}
	
	
	@Override
	public long getTotal() {
		return getTotal(getDomain());
	}
	
	@Override
	public long getTotal(Domain domain) {
		if (domain == null)
			return 0;
		String classname = getTypeOfT().getName();
		String hql = "SELECT count(*) FROM "+classname+" T WHERE T.domain.id=" + domain.getId().toString();
		org.hibernate.query.Query<?> query = getSessionFactory().getCurrentSession().createQuery(hql);
		query.setCacheable(true);
		query.setCacheRegion("metrics");
		return (Long) query.uniqueResult();
	}
	
	public boolean accept(Class<?> objectclass) {
		if (objectclass.isAssignableFrom(getTypeOfT())) {
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public Class<T> getTypeOfT() {
		Class<T> typeOfT = (Class<T>)
				((ParameterizedType)getClass()
				.getGenericSuperclass())
				.getActualTypeArguments()[0];
		return typeOfT;
	}
	
	protected SessionFactory getSessionFactory() {
		return (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
	}
	
	private Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}