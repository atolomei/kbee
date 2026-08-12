package com.novamens.content.web.migration.dao;


import java.io.IOException;
import java.util.List;

import org.hibernate.query.Query;
import org.hibernate.SessionFactory;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.web.migration.model.EntityMatching;

public class EntityMatchingDao implements EntityDao{

	private SessionFactory sessionFactory;
	
	public EntityMatchingDao() {
		super();
	}

	@Override
	public void save(EntityMatching entity) throws IOException {
		sessionFactory.getCurrentSession().saveOrUpdate(entity);
	}

	@Override
	public EntityMatching findEntityById(String id, String url)  throws ContentMgmtException {
		return (EntityMatching) sessionFactory.getCurrentSession().get(EntityMatching.class, new EntityMatching.EntityMatchingId(id, url));
	}
		
	public EntityMatching findEntityByKbeeId(String id)  throws ContentMgmtException {
		@SuppressWarnings("unchecked")
		List<EntityMatching> list = (List<EntityMatching>) getResultSet("FROM EntityMatching K WHERE K.compositeid.koId = '" + id +"'");
		if(list!=null&&!list.isEmpty()){
			if(list.size()==1)
				return (EntityMatching) list.get(0);
			else 
				throw new ContentMgmtException("There is more than one entity with the same kbee_id");
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	public List<EntityMatching> findEntitiesByClass(String className){
		return (List<EntityMatching>) getResultSet("FROM EntityMatching K WHERE K.className = '" + className +"' order by K.lastModifiedDate");
	}
	
	@SuppressWarnings("unchecked")
	public List<EntityMatching> findEntitiesByKbeeClass(String className){
		return (List<EntityMatching>) getResultSet("FROM EntityMatching K WHERE K.kbeeClassName = '" + className +"' order by K.lastModifiedDate");
	}
	
	private List<? extends Object> getResultSet(String hql) {
		Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
		List<?> results = query.list();
		return results;
	}
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

}
