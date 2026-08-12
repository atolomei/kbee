package com.novamens.indexer.java;

import com.novamens.indexer.service.IndexerException;

public class ConstantFieldSchema extends FieldSchema {
	private String value;
	
	private class ValueExtractor implements Extractor { 
		public Object extract(Object object) throws IndexerException {
			return getValue();
		}
	}

	public void setValue(String value) {
		this.value = value;
		setExtractor(new ValueExtractor());
	}
	
	public String getValue() {
		return value;
	}
}
