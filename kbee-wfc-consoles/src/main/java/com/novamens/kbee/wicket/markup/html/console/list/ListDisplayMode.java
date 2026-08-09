package com.novamens.kbee.wicket.markup.html.console.list;

import java.util.Locale;
import java.util.ResourceBundle;


/**
 * 
 * Border   | no bkrder
 *
 * Bck      | no bck
 * 
 * Compact  | Comfortable
 * 
 *
 */
public enum ListDisplayMode {
					
	COMPACT_LIST_NOBORDER_BCK				(1, "compact","compact bkodd"),  		// compact NoBorder BCK 
	COMPACT_LIST_BORDER_BCK					(2, "compactlist","compact xgrid bkodd"), // compact Border BCK
	COMPACT_LIST_BORDER_NOBCK				(3, "compactgridnobck","compact xgrid bknone"), // compact Border NOBCK
	COMPACT_LIST_NOBORDER_NOBCK				(4, "compactnobck","compact bknone"), // compact NOBorder NOBCK

	
	COMFORTABLE_LIST_NOBORDER_BCK		(5, "comfortable","comfortable bkodd"),
	COMFORTABLE_LIST_BORDER_BCK			(6, "comfortablelist","comfortable xgrid bkodd"),
	COMFORTABLE_LIST_BORDER_NOBCK		(7, "comfortablelistnobck","comfortable xgrid bknone"),
	COMFORTABLE_LIST_NOBORDER_NOBCK			(8, "comfortablenobck","comfortable bknone");
	
	private String rslabel;
	private int id;
	private String css;
	
	private ListDisplayMode(int code, String rslabel, String css) {
		this.rslabel = rslabel;
		this.css=css;
		this.id = code; 
	}
	
	public String toString() {
		return ("id: " + getId() + ". RsLabel: "+ getRsLabel() + " css: " + getCss());
	}
	
	
	public boolean isComfortable() {
		return !isCompact();
	}
	
	public boolean isCompact() {
		return (this==ListDisplayMode.COMPACT_LIST_NOBORDER_BCK ||
				this==ListDisplayMode.COMPACT_LIST_BORDER_BCK ||
				this==ListDisplayMode.COMPACT_LIST_NOBORDER_NOBCK ||
				this==ListDisplayMode.COMPACT_LIST_BORDER_NOBCK );
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
		ResourceBundle res = ResourceBundle.getBundle(ListDisplayMode.this.getClass().getName(), locale);
		return res.getString(this.rslabel);
	}
	
	public int getId() {
		return id;
	}
	
}
