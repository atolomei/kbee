package com.novamens.wicket.markup.html.form;

import java.io.Serializable;

public class WebSuggestion implements com.novamens.indexer.query.Suggestion {
	private static final long serialVersionUID = -1L;
	private Serializable object;
	private String text;
	private String facet;
	private String cssclass;
	private float score;
	private boolean outstanding;
	
	public WebSuggestion(Serializable object, String text, String facet, float score, boolean outstanding) {
		this.object = object;
		this.text = text;
		this.score = score;
		this.facet = facet;
		this.outstanding = outstanding;
	}
	
	public WebSuggestion(Serializable object, String text, float score, boolean outstanding) {
		this.object = object;
		this.text = text;
		this.score = score;
		this.outstanding = outstanding;
	}
	
	public Object getObject() {
		return this.object;
	}
	
	public String getText() {
		return this.text;
	}
	
	public float getScore() {
		return score;
	}
	
	public String getCssClass() {
		return cssclass;
	}
	
	public void setCssClass(String cssclass) {
		this.cssclass = cssclass;;
	}
	
	public void setOutstanding(boolean value) {
		this.outstanding = value;
	}
	
	public boolean isOutstanding() {
		return outstanding;
	}

	public String getFacet() {
		return facet;
	}

	public void setFacet(String facet) {
		this.facet = facet;
	}
}
