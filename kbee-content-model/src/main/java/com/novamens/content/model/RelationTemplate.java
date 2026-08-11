package com.novamens.content.model;

import java.util.List;

import com.novamens.dom.DomainObject;

public interface RelationTemplate extends com.novamens.dom.Object, DomainObject {
	
	public static int LinkDispalyMode = 0; 
	public static int ResourceDispalyMode = 1; 
	public static int EmbeddedDispalyMode = 2;
	
	public String getName();
	public String getTargetLabel();
	public String getReverseLabel();
	public int getTargetDisplayMode();
	public int getReverseDisplayMode();
	public int getTargetOrder();
	public int getReverseOrder();
	@Deprecated
	public ContentTemplate getTargetTemplate();
	public ContentTemplate getSourceTemplate();
	public List<ContentTemplate> getTargetTemplates();
	public Multiplicity getMultiplicity();
	public boolean keepVersion();
	public boolean isAggregation();
	public boolean isMandatory();
}
