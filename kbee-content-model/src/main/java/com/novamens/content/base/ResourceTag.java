package com.novamens.content.base;

import com.novamens.dom.DomainObject;

public interface ResourceTag extends DomainObject  {
	public String getName();
	public String getAlias(); 
	public String getDisplayName();
	public boolean isMultiple();
	public boolean isDefault();
	public boolean isInNewContentTemplates();
	
	public ResourceGroupType getType();
}