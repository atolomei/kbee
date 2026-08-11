package com.novamens.content.model;

import com.novamens.dom.DomainObject;

public interface ModelElementTemplate extends DomainObject {
	public ModelElement getElement();
	public int getOrder();
	public String getName();
	public String getDisplayName();
	//public ModelSection getSection();
	//public String getSubsection();
	//public void setSection(ModelSection section);
	public ModelElement getParent();
	public boolean isMandatory();
	public boolean isReverse();
	public boolean isCanonical();
	public Multiplicity getMultiplicity();
	public default void setDefaultValues() {}
}