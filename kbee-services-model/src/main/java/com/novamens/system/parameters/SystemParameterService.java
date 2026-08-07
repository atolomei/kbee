package com.novamens.system.parameters;

import com.novamens.service.SystemService;

public interface SystemParameterService extends SystemService {
	
	public String getParameter(String key, String defaultvalue);
	public int getIntegerParameter(String key, int default_value);
	
}