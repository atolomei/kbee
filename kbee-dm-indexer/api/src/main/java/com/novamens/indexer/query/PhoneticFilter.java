package com.novamens.indexer.query;

public class PhoneticFilter extends AbstractFilter {
	
	private static final long serialVersionUID = 1L;
	
	public PhoneticFilter(String name, String value, String displayValue) {
		super(name, value, displayValue);
	}
	
	public PhoneticFilter(String name, String value) {
		super(name, value);
	}
	
	public String getClause() {
		return "(" + getName() + ":\"" + getValue() + "\" OR " + getName() + "phonetic:\"" + getValue() + "\" OR " + getName() + "phonetic:" + getAndWords((String)getValue()) +  ")";
	}
} 
