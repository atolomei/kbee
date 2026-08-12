package com.novamens.indexer.query;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 
 * 
 *
 */
public interface Query extends Serializable {
	public QueryBuilder getBuilder();
	public ResultSet execute();
	public Map<String, Object> getParameters();
	public void setParameters(Map<String, Object> parameters);
	public void setParameter(String name, Object value);
	public void setOptions(Map<String, FacetOptions> options); 
	public String getTitle();
	public List<Facet> getFacets();
}