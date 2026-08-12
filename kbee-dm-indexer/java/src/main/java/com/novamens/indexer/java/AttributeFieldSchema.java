package com.novamens.indexer.java;

public class AttributeFieldSchema extends FieldSchema {
	private boolean isId;
	
	public void setAsId(boolean value) {
		this.isId = value;
	}
	
	public boolean isId() {
		return isId;
	}
	
	public void setPath(String path) {
		setExtractor(new JPathExtractor(path));
	}
}
