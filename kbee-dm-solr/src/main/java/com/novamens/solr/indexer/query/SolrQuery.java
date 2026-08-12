package com.novamens.solr.indexer.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.FacetOptions;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.QueryBuilder;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexProxy;
import com.novamens.solr.indexer.service.SolrIndex;

public abstract class SolrQuery implements Query  {
	private static final long serialVersionUID = 1L;
													
	private Index index;
	private boolean highlight = false;
	private boolean textQuery = false;
	private boolean includeFacets = false;
	private boolean ascending = true;
	private boolean cache = true;
	private int highlightMaxChars = 0;
	private int pageSize = 64;
	private Map<String, Object> parameters = new HashMap<String, Object>();
	private Map<String, Object> filterparameters = new HashMap<String, Object>();
	private String qf;
	private String df = "metainfo";
	private String sortField;

	public SolrQuery(Index index) {
		this.index = index;
	}	

	public abstract String getStatement();
	
	
	public abstract String getSolrStatement();
	
	public String getSolrFilterStatement() {
		return null;
	}
	
	public SolrIndex getIndex() {
		return index instanceof IndexProxy ? (SolrIndex)((IndexProxy) index).getIndex() : (SolrIndex) index ;
	}
	
	@Override
	public Map<String, Object> getParameters() {
		return this.parameters;
	}
	
	public void setParameters(Map<String, Object> parameters) {
		setTextQuery(false);
		this.parameters = parameters;
	}
	
	public void setParameter(String name, Object value) {
		setTextQuery(false);
		this.parameters.put(name, value);
	}
	
	public Map<String, Object> getFilterParameters() {
		return this.filterparameters;
	}
	
	public void setFilterParameters(Map<String, Object> parameters) {
		this.filterparameters = parameters;
	}
	
	public String getTitle() {
		return "";
	}
	
	public boolean includeScore() {
		return false;
	}
	
	public boolean includeSnippets() {
		return highlight;
	}
	
	public void setHighlight(boolean value) {
		this.highlight = value;
	}
	
	public void setTextQuery(boolean value) {
		this.textQuery = value;
	}
	
	public boolean isTextQuery() {
		return textQuery;
	}
	
	public void setHighlightMaxChars(int value) {
		highlightMaxChars = value;
	}
	
	public int getHighlightMaxChars() {
		return highlightMaxChars;
	}
	
	public String getHighlightField() {
		return "text";
	}
	
	public String getQueryFields() {
		return qf;
	}
	
	public void setQueryFields(String qf) {
		this.qf = qf;
	}
	
	public String getDefaultField() {
		return df;
	}
	
	public void setDefaultField(String df) {
		this.df = df;
	}
	
	public String getSortField() {
		return sortField;
	}
	
	public void setSortField(String sf) {
		this.sortField = sf;
	}
	
	public boolean isAscending() {
		return ascending;
	}
	
	public void setAscending(boolean value) {
		this.ascending = value;
	}
	
	public boolean getCache() {
		return cache;
	}
	
	public void setCache(boolean value) {
		this.cache = value;
	}
	
	public void setIncludeFacets(boolean value) {
		this.includeFacets = value;
	}
	
	public boolean includeFacets() {
		return includeFacets;
	}
	
	public String[] fields() {
		return null;
	}
	
	public int getPageSize() {
		return pageSize;
	}
	
	public void setPageSize(int size) {
		this.pageSize = size;
	}
	
	public void setOptions(Map<String, FacetOptions> options) {
	}
	
	public QueryBuilder getBuilder() {
		return new SolrQueryBuilder(getIndex());
	}
	
	public ResultSet execute() {
		return new SolrResultSetV1(this);
	}
	
	@Override
	public List<Facet> getFacets() {
		List<Facet> facets = new ArrayList<Facet>();
		facets.addAll(getIndex().getCube().getFacets());
		return facets;
	}
}
