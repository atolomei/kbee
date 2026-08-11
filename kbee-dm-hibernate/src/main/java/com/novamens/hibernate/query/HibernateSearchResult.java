package com.novamens.hibernate.query;


import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.indexer.query.SearchResult;
import com.novamens.security.Identifiable;
import com.novamens.service.ServiceLocator;

public class HibernateSearchResult implements SearchResult {
			
	private static final long serialVersionUID = 941443843627269072L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(HibernateSearchResult.class.getName());
	
	private Object object;
	private Class<?> clazz;
	private Serializable id;
	private boolean detached = false;
	
	public HibernateSearchResult(Object object) {
		this.object = object;
	}
	
	/**
	 * IMPORTANT: This method depends on the Hibernate version
	 */
	@Override
	public void detach() {
		try {
			if (object!=null) {
				String classname = object.getClass().getName();
				int i = classname.indexOf("_");
				if (i>0) classname = classname.substring(0, i);
				i = classname.indexOf("$");
				if (i>0) {
					classname = classname.substring(0, i);
				}
				clazz = Class.forName(classname);
				id = ((Identifiable)object).getId();
				object = null;
				detached = true;
			}
		}
		catch (ClassNotFoundException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object getObject() {
		if (detached) {
			SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
			detached = false;
			object = sf.getCurrentSession().load(clazz, id); 
		}	
		return object;
	}

	@Override
	public String getText() {
		return null;
	}

	@Override
	public Map<String, Object> getParameters() {
		return null;
	}

	@Override
	public float getScore() {
		return (float)0;
	}

	@Override
	public List<String> getSnippets() {
		return null;
	}

}
