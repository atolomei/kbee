package com.novamens.kbee.wicket.markup.html.console.grid;


import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;


public class GridColumnPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public GridColumnPanel(String id, IModel<String> model) {
		super(id);

		Label lab=new Label("label", model);
		
		lab.setEscapeModelStrings(isEscapeModelString());
		
		if (getCellContainerCss()!=null) 
			lab.add(new AttributeModifier("class", "cell-label "+ getCellContainerCss()));

		WebMarkupContainer lc = new WebMarkupContainer("label-container");
		add(lc);
		
		if (getLabelCss(model)!=null) 
			lc.add(new AttributeModifier("class", "label-container " + getLabelCss(model)));
		else if (getLabelCss()!=null) 
			lc.add(new AttributeModifier("class", "label-container " + getLabelCss()));
		
		lc.add(lab);
		
	}
	
	
	protected String getLabelCss(IModel<String> str) {
		return null;
	}
	
	protected String getLabelCss() {
		return null;
	}
	
	
	protected String getCellContainerCss() {
		return null;
	}
	
	protected boolean isEscapeModelString() {
		return false;
	}
	
}

