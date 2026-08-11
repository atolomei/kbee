package com.novamens.kbee.content.multidimensional;

public class ValueFacet extends HierarchicalFacet {
	private static final long serialVersionUID = 1L;

	public ValueFacet() {
	}
	
	protected String getDisplayName(String id) {
		return id;
	}
}
