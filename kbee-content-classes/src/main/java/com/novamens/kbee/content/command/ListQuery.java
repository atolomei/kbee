package com.novamens.kbee.content.command;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.FacetOptions;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.QueryBuilder;
import com.novamens.indexer.query.ResultSet;

public class ListQuery<T extends Serializable> implements Query {

	private static final long serialVersionUID = 1L;

	private Map<String, Object> parameters;
	private List<T> list;
	
	
	public ListQuery() {
	}
	
	public ListQuery(List<T> list) {
		this.list=list;
	}
	
	protected void setList(List<T> list) {
		this.list=list;
	}
	
	@Override
	public QueryBuilder getBuilder() {
		return null;
	}

	@Override
	public ResultSet execute() {
		ListResultSet<T> resultset=new ListResultSet<T>(getList());
		return resultset;
	}

	@Override
	public Map<String, Object> getParameters() {
		if (this.parameters==null) 
			this.parameters = new HashMap<String, Object>();
		return this.parameters;
	}

	@Override
	public void setParameters(Map<String, Object> parameters) {
	}
	
	public void setParameter(String name, Object value) {
	}

	@Override
	public void setOptions(Map<String, FacetOptions> options) {
	}

	@Override
	public String getTitle() {
		return null;
	}

	protected List<T> getList() {
		return list;
	}
	
	@Override
	public List<Facet> getFacets() {
		return new ArrayList<Facet>();
	}
}
