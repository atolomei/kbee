package com.novamens.kbee.wicket.markup.html.console.grid;

import java.util.Locale;
import java.util.ResourceBundle;

public enum GridRowBackgroundMode {
			
	NONE	 	(1, "bknone"), 
	BK_ODD		(2, "bkodd");
	
	private String rslabel;
	private int id;
	
	private GridRowBackgroundMode(int code, String rslabel) {
		this.rslabel = rslabel;
		this.id = code; 
	}
	
	public String toString() {
		return ("id: " + getId() + ". RsLabel: "+ getRsLabel());
	}
	
	public String getRsLabel() {
		return this.rslabel;
	}
	
	public String getLabel() {
		return getLabel(Locale.getDefault());
	}
	
	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(GridRowBackgroundMode.this.getClass().getName(), locale);
		return res.getString(this.rslabel);
	}
	
	public int getId() {
		return id;
	}

}
