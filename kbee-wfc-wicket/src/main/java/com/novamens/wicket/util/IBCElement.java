package com.novamens.wicket.util;


import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

public abstract interface IBCElement extends IDetachable {

	default public boolean isAjax() {return false;}
	
	public IModel<String> getLabel();
	public IModel<String> getHTMLTitleAttribute();
	public void onClick();
	public boolean isNewTab();
	
	
	 
}
