package com.novamens.hibernate.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.CacheMode;
import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.FacetOptions;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.QueryBuilder;
import com.novamens.indexer.query.ResultSet;
import com.novamens.service.ServiceLocator;
 

/**
 * <p>Hibernate Query does not support search filters (Solr is required for this).
 * Also, Iterators don't support Cursors (as of v6.5)
 * </p> 
 *
 */
public class HibernateQuery implements Query {
	
	private static final long serialVersionUID = 4904959153557865423L;
	
	private String title;
	private String statement;
	private String sizeQuery;
	
	Map<String, Object> parameters = new HashMap<String, Object>();
	
	private int startResult = 0;
	private int maxResults = HibernateResultSet.DEFAULT_BUFFER_SIZE;

	public QueryBuilder getBuilder() {
		return null;
	};
	
	public void setStatement (String statement) {
		this.statement = statement;
	}
	
	public String getStatement () {
		return statement;
	}
	
	public void setSizeQuery (String statement) {
		this.sizeQuery = statement;
	}
	
	public String getSizeQuery () {
		return this.sizeQuery;
	}
	
	public ResultSet execute() {
		return new HibernateResultSet(this);
	}
	
	public Map<String, Object> getParameters()  {
		return parameters;
	}
	
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
	
	public void setParameter(String name, Object value) {
		
	}

	
	/** Facets are not supported in this type of Queries
	*/
	public void setOptions(Map<String, FacetOptions> options) {
	}
	
	public int getMaxResults() {
		return maxResults;
	}
	
	public void setMaxResults(int pageSize) {
		maxResults = pageSize;
	}
	
	public int getFirstResult() {
		return this.startResult;
	}
		
	public void setFirstResult(int startResult) {
		this.startResult = startResult;
	}
	
	public void setTitle(String title)  {
		this.title= title;
	}
	
	public String getTitle()  {
		return title;
	}
	
	public SessionFactory getSessionFactory() {
		return (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
	}
	
	@Override
	public List<Facet> getFacets() {
		return new ArrayList<Facet>();
	}
	
	public CacheMode getCacheMode() {
		return null;
	}
}
