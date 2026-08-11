package com.novamens.kbee.content.command;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.novamens.indexer.query.SearchResult;


public class ListSearchResult<T extends Serializable> implements SearchResult {

	private T object;	
	
	private static final long serialVersionUID = 1L;

	public ListSearchResult(T object) {
		this.object = object;
	}
	
	@Override
	public void detach() {
	}

	@Override
	public Object getObject() {
		return object;
	}
	
	@Override
	public String getText() {
		return null;
	}

	@Override
	public Map<String, Object> getParameters() {
		return null;
	}

	@Override
	public float getScore() {
		return 0;
	}

	@Override
	public List<String> getSnippets() {
		return null;
	}



	

}
