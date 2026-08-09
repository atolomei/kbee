package com.novamens.wicket.util;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public class DraftPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public DraftPanel(String id, String style) {
		super(id);
		
		WebMarkupContainer d=new WebMarkupContainer("draft-panel");
		add(d);
				
		d.add(new AttributeModifier("style", style));
		d.add(new Label("label", id));
		
	}
	
	public DraftPanel(String id) {
		super(id);
		add(new Label("label", id));
	}
	
	

}
