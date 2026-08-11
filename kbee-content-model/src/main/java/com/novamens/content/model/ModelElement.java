package com.novamens.content.model;

public interface ModelElement extends ModelObject {
	
	static public int SYSTEM_FACETS_THRESHOLD = 64;
	
	public String getName();
	
	public Multiplicity getMultiplicity();

	public boolean isVisible(String context);
	public void setVisibility(String context, boolean value);
	
	public boolean isOrdered();
	
	public boolean isDefaultStructure();
	
	
	public void setAlias(String alias);
	
	
}
