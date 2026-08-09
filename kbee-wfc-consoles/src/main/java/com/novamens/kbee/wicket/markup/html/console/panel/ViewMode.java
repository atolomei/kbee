package com.novamens.kbee.wicket.markup.html.console.panel;

import java.util.Locale;
import java.util.ResourceBundle;



public enum ViewMode {

	NOIMAGE(100, "noimage"),
	ICON(10, "icon"),
	THUMBNAIL(20, "thumbnail", "media-list col-lg-12 col-md-12 col-xs-12 ", "media col-lg-4 col-md-6 col-xs-12 ", "media-left", "320", "240"),
	THUMBNAIL_LARGE(30, "thumbnail_large", "media-list col-lg-12 col-md-12 col-xs-12 ", "media col-lg-4 col-md-6 col-xs-12 ", "thumbnail", "320", "240"),
	THUMBNAIL_JUMBO(40, "thumbnail_jumbo", "media-list col-lg-12 col-md-12 col-xs-12 ", "media col-lg-4 col-md-6 col-xs-12 ", "thumbnail_jumbo", "900", "768");
	
	private String list_css;
	private String element_css;
	private String image_w;
	private String image_h;
	private String image_container_css;
	private String label;
	private int id; 
	
	
	public static ViewMode of( int id) {
		if (id==NOIMAGE.getId()) return NOIMAGE;
		if (id==THUMBNAIL.getId()) return THUMBNAIL;
		if (id==THUMBNAIL_LARGE.getId()) return THUMBNAIL_LARGE;
		if (id==THUMBNAIL_JUMBO.getId()) return THUMBNAIL_JUMBO;
		return ICON;
	}
	
	public int getId() {
		return id;
		
	}
	public String getLabel() {
		return label;
	}
	
	public String getImageContainerCss() {
		return this.image_container_css;
	}
	
	public String getImageH() {
		return this.image_h;
	}

	public String getImageW() {
		return this.image_w;
	}
	
	public String getElementCss() {
		return this.element_css;
	}
	
	public String getListCss() {
		return this.list_css;
	}
	
	private ViewMode(int id, String label) {
		this.id=id;
		this.list_css="media-list col-lg-12 col-md-12 col-xs-12 "+ this.name().toLowerCase();
		this.element_css="media col-lg-12 col-md-12 col-xs-12 ";		
		this.image_h="80";
		this.image_w="80";
		this.image_container_css="media-left ";
		this.label=label;
	}
	
	private ViewMode(int id, String label, String list_css, String element_css, String image_container_css, String image_w, String image_h) {
		
		this.id=id;
		this.label=label;
		this.element_css=element_css; 
		this.list_css=list_css + " " + this.name().toLowerCase();
		this.image_h=image_h;
		this.image_w=image_w;
		this.image_container_css=image_container_css;
	}

	
	public String getDisplayName() {
		Locale locale = Locale.getDefault();
		return getDisplayName(locale); 
	}

	
	public String getDisplayName(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(ViewMode.this.getClass().getName(), locale);
		return res.getString(this.label);
	}
}

