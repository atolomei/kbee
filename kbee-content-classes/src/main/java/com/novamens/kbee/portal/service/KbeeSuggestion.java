package com.novamens.kbee.portal.service;

public class KbeeSuggestion implements com.novamens.indexer.query.Suggestion {
	private static final long serialVersionUID = -1L;
	private Object object;
	private String text;
	private String facet;
	private String cssclass;
	private float score;
	private boolean outstanding;
	
	public KbeeSuggestion(Object object, String text, String facet, float score, boolean outstanding) {
		this.object = object;
		this.text = text;
		this.score = score;
		this.facet = facet;
		this.outstanding = outstanding;
	}
	
	public KbeeSuggestion(Object object, String text, float score, boolean outstanding) {
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