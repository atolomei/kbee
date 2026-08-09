package com.novamens.wicket.markup.html.repeater.util;

import org.apache.wicket.model.IModel;

import com.novamens.indexer.query.SearchResult;

public class SearchResultModel implements IModel<SearchResult> {

	private static final long serialVersionUID = 1L;
	private com.novamens.indexer.query.SearchResult object;
	
	public SearchResultModel(SearchResult result) {
		setObject(result);
	}
	
	public SearchResult getObject() {
		return object;
	}
	
	public void setObject(SearchResult result) {
		object = result;
	}
	
	public void detach() {
		object.detach();
	}
}