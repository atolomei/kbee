package com.novamens.content.model;

public interface AttributeTemplate extends ModelElementTemplate {
	public Attribute getAttribute();
	//public String getSubsection();
	public boolean isMetadataSubtitle();
	public boolean isPortalSubtitle();
	public boolean isReadOnly();
	public boolean isVisible();
	public Multiplicity getMultiplicity();
	public AttributeSource getSource();
	public String getCalculationScript();
}