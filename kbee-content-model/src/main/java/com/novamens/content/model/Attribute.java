package com.novamens.content.model;

import com.novamens.dom.Indexable;

/**
 * Free form fields of text / Date / Number
 * see also
 * 
 * {@link Dataset}
 * {@link Classifier}
 * {@link ContentTemplate}
 * 
 */
public interface Attribute extends ModelElement, Indexable  {
	
	public AttributeType getType();
	public Multiplicity getMultiplicity();
	
	public String getAlias();
	public String getUniqueName();
	public boolean isDate();
	public boolean isMetadataSubtitle();
	public boolean isRequired();
	public boolean isFilterable();
	public boolean isSearchable();
	public boolean isDefaultGridColumn();
	public boolean isVisible(String context);
	public boolean isIdentityDocument();
	public String getPredicate();
	public int getBoostFactor();
	public Attribute clone();
	
	public AttributeValidator getValidator();
	
	public int getOrder();
	public void setOrder(int order);
	
	boolean isAPIClassifier();
	void setAPIClassifier(boolean value);
	boolean isPortalSubtitle();
	boolean isPortal();
	boolean isRuleCondition();
	void setPortalSubtitle(boolean isportalsubtitle);
	void setPortal(boolean inportal);
	
	default public String getModelObjectClassName() { return "Attribute";}
}
