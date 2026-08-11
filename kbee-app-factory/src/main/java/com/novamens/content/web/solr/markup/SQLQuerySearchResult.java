package com.novamens.content.web.solr.markup;

import java.util.List;

public class SQLQuerySearchResult {
				
	private List<Object> objects;
	private long index;

	public SQLQuerySearchResult(List<Object> objects, int index) {
		super();
		this.objects = objects;
		this.index=index;
	}


	public List<Object> getObjects() {
		return objects;
	}


	public void setObjects(List<Object> objects) {
		this.objects = objects;
	}


	public long getIndex() {
		return index;
	}
	
	public void detach() {
		
	}
	

}
