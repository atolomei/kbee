package com.novamens.portal6.model;



public interface ViewBKIQL extends ViewBK {
	
	static public final String IQL_TYPE = "iql";
	static public final String PARAMETERS_QUERY_TYPE = "parameters_query";
	
			
			
	public String getStatement();
	public void setStatement( String iql);
	
}
