package com.novamens.indexer.query;

public class WordsFilter extends AbstractFilter {
	private static final long serialVersionUID = 1L;
	
	public WordsFilter(String name, String value, String displayValue) {
		super(name, value, displayValue);
	}
	
	public WordsFilter(String name, String value) {
		super(name, value);
	}
	
	public String getClause() {
		return "(" + getName() + ":\"" + getValue() + "\" OR " +  getName() + ":" + getAndWords((String)getValue()) +  ")";
	}
};
