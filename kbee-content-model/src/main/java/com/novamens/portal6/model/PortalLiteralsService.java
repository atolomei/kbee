package com.novamens.portal6.model;

import com.novamens.service.BusinessSystemService;

public interface PortalLiteralsService extends PortalService,  BusinessSystemService  {

	
										
	static final public String BLOCK_SITE_INFO				= "block-site-info";
	
	
	static final public String BLOCK_SEARCH 				= "block-search";
	static final public String BLOCK_MY_TASKS 				= "block-widget--my-tasks";
	static final public String BLOCK_PENDING_TASKS   		= "block-widget-my-tasks";
	
	static final public String BLOCK_MONITOR 				= "block-widget-monitor";
	static final public String BLOCK_LIBRARY 				= "block-widget-library";
	
	static final public String BLOCK_PORTAL_LIBRARY 		= "block-widget-portal-library";
	static final public String BLOCK_PORTAL_TEXT 			= "block-portal-text";

	static final public String BLOCK_PORTAL_CONTENT_LIST 	= "block-widget-content-list";
	
	static final public String BLOCK_PORTAL_LISTVIEW  		= "block-widget-listview";
	
	static final public String BLOCK_DASHBOARD_QUERIES  	= "block-widget-dashboard-queries";
	
	static final public String BLOCK_DASHBOARD_DATASETMEMBERS  	= "block-widget-dashboard-datasetmembers-query";
	
	
	public  String getViewer( String model);
	public  String getDataProvider( String model);
}

