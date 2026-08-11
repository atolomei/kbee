package com.novamens.content.document;

import java.util.List;

import com.novamens.content.base.Resource;

public interface IDocSection extends  com.novamens.dom.Object {

	// public List<Attribute> getAttributes();
	
	public List<Resource> getResources();
	
	public void setDescription(String description);
	public String getDescription();

	public void setOrder(int order);
	public int getOrder();
	
	public void setName(String name);
	public String getName();

	public void setResources(List<Resource> list);
	
	public void setAttributesJSON(String attributes);
	public String getAttributesJSON();
	
	
	
	
	
	
	
	
}
