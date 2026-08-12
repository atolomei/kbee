package com.novamens.indexer.query;

public class PhoneticTextFilter extends AbstractFilter {
	private static final long serialVersionUID = 1L;
	
	public PhoneticTextFilter(String value) {
		super("text", value);
	}
	
	public String getClause() {
		return "(titlephonetic:" + getValue() + " OR " + getName() + ":" + getValue() + ")";
	}
};
