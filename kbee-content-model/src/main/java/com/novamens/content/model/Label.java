package com.novamens.content.model;

import java.io.Serializable;

public interface Label {
	
	public Serializable getId();
	
	public void setLabel(String label);
	public String getLabel();
	
	public void setScope(LabelScope scope);
	public LabelScope getScope();
	
	public void setCss(String css);
	public String getCss();
	
	public  String getClassName();
}
