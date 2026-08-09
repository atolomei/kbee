package com.novamens.wicket.markup.html.actions;

import org.apache.wicket.model.ResourceModel;

public abstract class WorkingAjaxMenuItem extends AjaxMenuItem  {

	private static final long serialVersionUID = 714429793590416363L;

	private String indicatingLabel = new ResourceModel("working").getObject();
	
	public void setIndicatingLabel(String str) {
		indicatingLabel=str;
	}
	
	public String getIndicatingLabel() {
		return indicatingLabel;
	}
	
}
