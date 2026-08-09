package com.novamens.kbee.wicket.markup.html.console.grid;

import java.util.Locale;

import java.util.ResourceBundle;
/**
 *
 */
public enum GridDisplayMode {

	COMPACT 			(1, "compact","compact bkodd"), 
	COMFORTABLE			(2, "comfortable","comfortable bkodd"),
	COMFORTABLE_GRID	(3, "comfortablegrid","comfortable xgrid bkodd"),
	COMPACT_GRID		(4, "compactgrid","compact xgrid bkodd"),
							
	COMPACT_GRID_NO_BCK 	 (5, "compactgridnobck","compact xgrid bknone"), 
	COMFORTABLE_GRID_NO_BCK	 (6, "comfortablegridnobck","comfortable xgrid bknone"),
	COMPACT_NO_BCK 			 (7, "compactnobck","compact bknone"), 
	COMFORTABLE_NO_BCK		 (8, "comfortablenobck","comfortable bknone");
	
	private String rslabel;
	private int id;
	private String css;
	
	private GridDisplayMode(int code, String rslabel, String css) {
		this.rslabel = rslabel;
		this.css=css;
		this.id = code; 
	}
	
	public String toString() {
		return ("id: " + getId() + ". RsLabel: "+ getRsLabel() + " css: " + getCss());
	}
	
	public String getRsLabel() {
		return this.rslabel;
	}
	
	public String getCss() {
		return this.css;
	}
	
	public String getLabel() {
		return getLabel(Locale.getDefault());
	}
	
	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(GridDisplayMode.this.getClass().getName(), locale);
		return res.getString(this.rslabel);
	}
	
	public int getId() {
		return id;
	}
	
}
