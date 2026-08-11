package com.novamens.content.tools;

import com.novamens.content.base.Content;

public interface Tool extends Content {

	public static final String TOOL		 		= "tool";
	
	public static final String APPLICATION		= "app";
	public static final String SIMULATOR 		= "simu";
	public static final String FORM				= "form";
	public static final String SERVICE			= "serv";
	
	
	public String getUrl();
	public void setUrl(String url);
	
	public String getSubtitle();
	public void setSubtitle(String subtitle);
	
	
}
