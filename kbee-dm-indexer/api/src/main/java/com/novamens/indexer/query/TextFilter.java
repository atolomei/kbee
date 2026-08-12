package com.novamens.indexer.query;

public class TextFilter extends AbstractFilter {
	private static final long serialVersionUID = 1L;
	
	public TextFilter(String value) {
		super("text", value);
	}
	
	public String getClause() {
		return "("+ (String)getValue() + ")";
	}
};
