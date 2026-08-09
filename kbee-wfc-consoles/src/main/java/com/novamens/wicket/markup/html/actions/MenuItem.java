package com.novamens.wicket.markup.html.actions;

import java.io.Serializable;

public interface MenuItem extends Serializable {
	
	public void onClick() throws Exception;
	public String getLabel();
	public boolean isEnabled();
	public boolean isVisible();
	
	public String getTarget();
	public String getCssClass();
	public String getBeforeClick();
}
