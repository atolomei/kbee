package com.novamens.wicket.util;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class DummyBlockPanel extends Panel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String style;
	
	public DummyBlockPanel() {
			this("panel"); 
	}
	
	public DummyBlockPanel(String id) {
			this(id, null);
	}
	
	
	public DummyBlockPanel(String id, IModel<String> label_left) {
			this(id, label_left, null, null);
	}
	
	public DummyBlockPanel(String id, IModel<String> label_left, IModel<String> label_right) {
		this(id,  label_left, label_right, null);
	}
	
	
	public DummyBlockPanel(String id, IModel<String> label_left, IModel<String> label_right, String css) {
		super(id);
		this.label_left=label_left;
		this.label_right=label_right;
		this.css=css;
	}


	IModel<String> label_left;
	IModel<String> label_right;
	String css;
	
	public void onInitialize() {
		super.onInitialize();

		setOutputMarkupId(true);
		
		WebMarkupContainer r=new WebMarkupContainer("rect");
		
		if (css!=null)
			r.add(new AttributeModifier("class", css));
	
		if  (style!=null)
			r.add(new AttributeModifier("style", style));
				
		Label la=new Label("label-left", label_left);
		la.setEscapeModelStrings(false);
		la.setVisible(la!=null);
		r.add(la);
		
		
		Label lar=new Label("label-right", label_right);
		lar.setEscapeModelStrings(false);
		lar.setVisible(lar!=null);
		r.add(lar);
		
		add (r);

	}
	public void setStyle( String s) {
		style=s;
	}
}
