package com.novamens.indexer.query;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface SearchResult extends Serializable{
	public void detach();
	public Object getObject();
	public String getText();
	public Map<String, Object> getParameters();
	public float getScore();
	public List<String> getSnippets();
}
