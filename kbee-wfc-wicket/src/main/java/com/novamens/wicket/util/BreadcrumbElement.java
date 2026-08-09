package com.novamens.wicket.util;

import java.io.Serializable;

public class BreadcrumbElement implements Serializable {
	private static final long serialVersionUID = -8978146096633843467L;
	private String label;
	private String url;
	public String getLabel() { return label;}
	public String getUrl() { return url; }
	public BreadcrumbElement(String label, String url) {
		this.label=label;
		this.url=url;
	}
}
