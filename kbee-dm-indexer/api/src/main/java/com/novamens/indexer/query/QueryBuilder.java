package com.novamens.indexer.query;

import java.io.Serializable;
import java.util.Map;


public abstract class QueryBuilder implements Serializable {
	private static final long serialVersionUID = 1L;
	private static QueryBuilder Instance;
	
	public static QueryBuilder getInstance() {
		return Instance;
	}
	
	public static void setInstance(QueryBuilder builder) {
		Instance = builder;
	}
	
	public abstract Query build(String text);
	public abstract Query build(Map<String, Object> parameters);
}
