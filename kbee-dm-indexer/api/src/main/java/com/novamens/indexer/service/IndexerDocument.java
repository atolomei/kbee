package com.novamens.indexer.service;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class IndexerDocument implements Document {
	private static final long serialVersionUID = 1L;
	private Map<String, Object> fields = new HashMap<String, Object>();

	private OffsetDateTime lastModifiedDate;
	private String id;
	
	public IndexerDocument() {
	}
	
	public IndexerDocument(String id) {
		setId(id);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public OffsetDateTime getLastModifiedOffsetDateTime() {
		return lastModifiedDate;
	}

	
	public void setLastModifiedOffsetDateTime(OffsetDateTime date) {
		this.lastModifiedDate = date;
	}
	
	
	public void addField(String name, Object value) {
		fields.put(name, value);
	}
	
	public Object getFieldValue(String name) {
		return fields.get(name);
	}
	
	public Set<String> getFieldNames() {
		return fields.keySet();
	}


}

