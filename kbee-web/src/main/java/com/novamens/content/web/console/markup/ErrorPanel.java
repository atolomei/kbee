package com.novamens.content.web.console.markup;


import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;


public class ErrorPanel extends Panel {  
	private static final long serialVersionUID = 1L;

	boolean is_full_width = false;
	
	public ErrorPanel(String id, IModel<String> title, IModel<String> message) {
		super(id);
		add((new Label("title", title)).setVisible(title!=null));
		add( (new Label("text", message).setEscapeModelStrings(false)));
	}
}
