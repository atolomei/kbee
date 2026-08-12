package com.novamens.indexer.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TextQuery implements Query {
	private static final long serialVersionUID = 1L;
	
	private String statement;
	private String sortField;
	private String defaultField = "metainfo";
	private boolean ascending = true;
	private int offset = 0;
	private int pageSize = 25;
	private boolean faceted = false;
	private boolean highlight = false;
	private int highlightMaxChars = 0;
	private boolean cache=true;
	
	public TextQuery(String statement) {
		setStatement(statement);
	}
	
	public String getStatement() {
		return statement;
	}
	
	public void setStatement(String statement) {
		this.statement = statement; 
	}
	
	public boolean isFaceted() {
		return faceted;
	}
	
	public void setFaceted(boolean value) {
		this.faceted = value;
	}
	
	public boolean isHighlight() {
		return highlight;
	}
	
	public void setHighlight(boolean value) {
		this.highlight = value;
	}
	
	public void setHighlightMaxChars(int value) {
		highlightMaxChars = value;
	}
	
	public int getHighlightMaxChars() {
		return highlightMaxChars;
	}

	public int getOffset() {
		return offset;
	}
	
	public void setOffset(int value) {
		this.offset = value;
	}
	
	public int getPageSize() {
		return pageSize;
	}
	
	public void setPageSize(int value) {
		this.pageSize = value;
	}
	
	public String getSortField() {
		return sortField;
	}
	
	public void setSortField(String name) {
		this.sortField = name;
	}
	
	public String getDefaultField() {
		return defaultField;
	}
	
	public void setDefaultField(String name) {
		this.defaultField = name;
	}
	
	public boolean getCache() {
		return cache;
	}
	
	public void setCache(boolean value) {
		this.cache = value;
	}
	
	public boolean isAscending() {
		return ascending;
	}
	
	public void setAscending(boolean value) {
		this.ascending = value;
	}
	
	public QueryBuilder getBuilder() {
		return null;
	}
	
	public ResultSet execute() {
		return null;
	}
	
	public Map<String, Object> getParameters() {
		return null;
	}
	
	public void setParameters(Map<String, Object> parameters) {
	}
	
	public void setParameter(String name, Object value) {
	}
	
	public void setOptions(Map<String, FacetOptions> options) {
	}
	
	public String getTitle() {
		return null;
	}
	
	public List<Facet> getFacets() {
		return new ArrayList<Facet>();
	}
	
	public String[] fields() {
		return null;
	}
}
