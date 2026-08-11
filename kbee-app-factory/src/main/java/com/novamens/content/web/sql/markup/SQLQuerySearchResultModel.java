package com.novamens.content.web.sql.markup;

import org.apache.wicket.model.IModel;



public class SQLQuerySearchResultModel implements IModel<SQLQuerySearchResult> {

private static final long serialVersionUID = 1L;
	
	private SQLQuerySearchResult object;
	long index;
	
	public SQLQuerySearchResultModel(SQLQuerySearchResult result) {
		setObject(result);
		this.index=result.getIndex();
	}
	
	public SQLQuerySearchResult getObject() {
		return object;
	}
	
	public void setObject(SQLQuerySearchResult result) {
		object = result;
	}
	
	public void detach() {
		object=null;
	}
	

}
