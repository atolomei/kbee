package com.novamens.solr.indexer.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.indexer.query.SearchResult;
import com.novamens.security.Identifiable;
import com.novamens.service.ServiceLocator;

public class SolrSearchResult implements SearchResult {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SolrSearchResult.class.getName());
	
	private static final long serialVersionUID = 1L;
	private Object object;
	private Class<?> clazz;
	private Serializable id;
	private float score;
	private List<String> snippets;
	private boolean detached = false;

	public SolrSearchResult(Object object) {
		this.object = object;
	}

	/**
	 * Depends on the Hibernate version
	 */
	public void detach() {
		try {
			if (object!=null) {
				String classname = object.getClass().getName();
				int i = classname.indexOf("_");
				if (i>0) classname = classname.substring(0, i);
				i = classname.indexOf("$");
				if (i>0) classname = classname.substring(0, i);
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

	public Object getObject() {
		if (detached) {
			SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
			detached = false;
			object = sf.getCurrentSession().load(clazz, id); 
		}	
		return object;
	}

	public String getText() {
		return null;
	}

	public Map<String, Object> getParameters() {
		return null;
	}

	public float getScore() {
		return score;
	}
	
	public void setScore(float score) {
		this.score = score;
	}
	
	public void setSnippets(List<String> snippets) {
		this.snippets = snippets; 
	}
	
	public List<String> getSnippets() {
		return snippets==null ? new ArrayList<String>(1) : snippets;
	}
	
	@Override
	public String toString() {
		
		if(detached && clazz!=null && id!=null) 
				return clazz.getSimpleName() + "@" + id.toString(); 
				
		if (object!=null) {
			try {
			return object.toString();
			} catch (Exception e) {
				logger.error(e);
				return "null";
			}
		}
		return "null";
	}
}
