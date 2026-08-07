package com.novamens.system.properties;

import com.novamens.service.SystemService;

public interface SystemPropertiesService extends SystemService {

	
	public String getDefaultKBFSService();
	public String getServerId();
	
	
	public String getProperty(String key);
	// public void setProperty(String key, String value);
	String getServerIdPrefix();
	
	
}
