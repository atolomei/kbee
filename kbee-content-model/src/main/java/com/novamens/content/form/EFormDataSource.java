package com.novamens.content.form;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.novamens.indexer.query.Suggestion;

public interface EFormDataSource<T> {
	
	public interface Url extends Serializable {
		public String getLabel();
		public com.novamens.dom.Url getUrl();
	};
	
	public List<T> getValues();
	public List<Suggestion> getValues(String pattern);
	public List<Suggestion> getValues(String pattern, Map<String, Object> parameters);
	public List<Url> getUrls();
	
	public boolean isReadable();
}