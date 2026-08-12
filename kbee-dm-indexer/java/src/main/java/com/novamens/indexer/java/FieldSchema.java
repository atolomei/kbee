package com.novamens.indexer.java;

public class FieldSchema {
	private String fieldName;
	private Extractor extractor;
	
	public Extractor getExtractor() {
		return extractor;
	}
	
	public void setExtractor(Extractor extractor) {
		this.extractor = extractor;
	}
	
	public String getFieldName() {
		return fieldName;
	}
	
	public void setFieldName(String name) {
		this.fieldName = name;
	}
	
	public boolean isId() {
		return false;
	}
	
	public boolean isMetainfo() {
		return true;
	}
}
