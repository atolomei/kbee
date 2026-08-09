package com.novamens.wicket.markup.html.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;


public enum FormLayout {
	EDITOR 				("editor"), 
	VIEWER 				("viewer"), 
	EDITOR_WITH_VIEWER 	("editor-with-viewer");
		
	private String id;
	
	static List<FormLayout> li = new ArrayList<FormLayout>();
	
	static {
		li.add(EDITOR);
		li.add(VIEWER);
		li.add(EDITOR_WITH_VIEWER);
	}
	
	private FormLayout(String id) {
		this.id=id;
	}
	
	public String toString() {
		return ("id: " + getId());
	}
	
	
	public String getDisplayName() {
		return getLabel();
	}
	
	public String getLabel() {
		return getLabel(Locale.getDefault());
	}
	
	
	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(FormLayout.this.getClass().getName(), locale);
		return res.getString(this.id);
	}
	
	public String getId() {
		return id;
	}
	
	
	
	static public List<FormLayout> getFormLayouts() {
		return li;
	}
}
