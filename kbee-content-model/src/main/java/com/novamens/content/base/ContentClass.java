package com.novamens.content.base;

import java.util.List;

import com.novamens.content.model.Classifier;

import com.novamens.dom.ObjectState;
import com.novamens.security.Identifiable;

public interface ContentClass  extends Identifiable {
	
	public String getId(); 			
	
	
	public String getDisplayName();
	public String getName();
	public void setName(String name);
	
	public boolean isEnabled();
	public void setEnabled(boolean enabled);

	public boolean isSelectable();
	public void setSelectable(boolean enabled);

	
	public boolean isIndexable();
	
	public void setId(String id);

	public List<Classifier> getClassifiers();
	
	public String getJavaClass();
	public void setJavaClass(String classname);

	public ObjectState getState();
	
	
}
