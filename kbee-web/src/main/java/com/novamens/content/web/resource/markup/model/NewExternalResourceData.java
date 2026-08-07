package com.novamens.content.web.resource.markup.model;

import java.io.Serializable;

public class NewExternalResourceData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String title;
	private String description;
	private String url;
	private boolean in_portal = true;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	
	public boolean isInPortalVersion() {
		return this.in_portal;
	}
	
	public void setInPortalVersion(boolean b) {
		this.in_portal=b;
	}

}
