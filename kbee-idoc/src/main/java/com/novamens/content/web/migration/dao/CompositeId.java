package com.novamens.content.web.migration.dao;

import java.io.Serializable;

public class CompositeId implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private String url;
	
	public CompositeId(String id, String url) {
		super();
		this.id = id;
		this.url = url;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
