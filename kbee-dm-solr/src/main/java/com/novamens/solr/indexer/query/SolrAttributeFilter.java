package com.novamens.solr.indexer.query;

import java.io.Serializable;

import com.novamens.content.model.Attribute;
import com.novamens.indexer.query.Filter;


public class SolrAttributeFilter implements Filter {
	private static final long serialVersionUID = 1L;
	
	private String name, display;
	private String value;
	
	public SolrAttributeFilter(Attribute attribute, String value) {
		this.display = value;
		this.name = attribute.getUniqueName() + "name";
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDisplayName() {
		return display;
	}
	
	public Serializable getValue() {
		return value;
	}
	
	public String getDisplayValue() {
		return display;
	}
	
	public String getClause() {
		return name + ":" + getValue();
	}
}
