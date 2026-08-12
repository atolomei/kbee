package com.novamens.indexer.query;

public class ValueFilter extends AbstractFilter {
	private static final long serialVersionUID = 1L;
	
	public ValueFilter(String name, String value, String displayValue) {
		super(name, value, displayValue);
	}
	
	public ValueFilter(String name, String value) {
		super(name, value);
	}
	
	public String getClause() {
		return super.name + ":" + super.value;
	}
	
	public String toString() {
		return getClause();
	}
};
