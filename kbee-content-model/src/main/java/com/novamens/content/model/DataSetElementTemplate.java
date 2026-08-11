package com.novamens.content.model;

public interface DataSetElementTemplate extends ModelElementTemplate {
	public ModelElement getElement();
	public Multiplicity getMultiplicity();
	public boolean isReadOnly();
	public boolean isMandatory();
	public boolean isAggregation();
	public boolean isAggregation(DataSet dataset);
}
