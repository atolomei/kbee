package com.novamens.content.web.solr.markup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.novamens.indexer.query.SearchResult;

public class SolrGatewayResult implements SearchResult {
	private static final long serialVersionUID = 1L;
	private Serializable object;
	private float score;
	private List<String> snippets;

	public SolrGatewayResult(Object object) {
		this.object = (Serializable)object;
	}

	public void detach() {
	}

	public Object getObject() {
		return object;
	}

	public String getText() {
		return null;
	}

	public Map<String, Object> getParameters() {
		return null;
	}

	public float getScore() {
		return score;
	}
	
	public void setScore(float score) {
		this.score = score;
	}
	
	public void setSnippets(List<String> snippets) {
		this.snippets = snippets; 
	}
	
	public List<String> getSnippets() {
		return snippets==null ? new ArrayList<String>(1) : snippets;
	}
}