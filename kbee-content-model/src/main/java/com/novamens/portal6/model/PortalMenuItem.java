package com.novamens.portal6.model;

import java.io.Serializable;

import com.novamens.dom.ObjectState;
import com.novamens.security.Identifiable;

public interface PortalMenuItem extends Identifiable, Serializable {

	public void setLabel(String s);
	public String getLabel();
	
	public void setDisplayName(String s);
	public String getDisplayName();
	
	public void setId(String s);
	public String getId();
	
	public void setDisplayPanelKey(String s);
	public String displayPanelKey();
	
	public void setHRef(String s);
	public String getHRef();
	
	public ObjectState getState();
	void setState(ObjectState s);
	
	public String getMenuString();
	public Object getMenuString(int level);
	
	public String getTitle();
	public void setTitle(String s);
	
	
	String treeString();
	
	
	
}
