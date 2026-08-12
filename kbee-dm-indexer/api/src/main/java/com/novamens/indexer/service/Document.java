package com.novamens.indexer.service;

import java.io.Serializable;
import java.time.OffsetDateTime;

import java.util.Set;

public interface Document extends Serializable {
	public String getId();
	
	public OffsetDateTime getLastModifiedOffsetDateTime();
	
	public Object getFieldValue(String name);
	public void addField(String name, Object value);
	public Set<String> getFieldNames();
}
