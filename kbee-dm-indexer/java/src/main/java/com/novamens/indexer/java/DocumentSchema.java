package com.novamens.indexer.java;

import java.util.ArrayList;
import java.util.List;

public class DocumentSchema {
	private Class<?> javaClass;
	private String name;
	private boolean aggregation = false;
	private List<FieldSchema> fields = new ArrayList<FieldSchema>();
	
	public DocumentSchema() {
	}
	
	public DocumentSchema(String name) {
		setName(name);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Class<?> getJavaClass() {
		return javaClass;
	}

	public void setJavaClass(Class<?> javaClass) {
		this.javaClass = javaClass;
	}
	
	public List<FieldSchema> getFieldsSchemas() {
		return fields;
	}
	
	public void addFieldSchema(FieldSchema schema) {
		fields.add(schema);
	}
	
	public void setFieldsSchemas(List<FieldSchema> schemas) {
		fields = schemas;
	}
	
	public boolean isAggregation() {
		return aggregation;
	}
	
	public void setAggregation(boolean value) {
		this.aggregation = value;
	}
}
