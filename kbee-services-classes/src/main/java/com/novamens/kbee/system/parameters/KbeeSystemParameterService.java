package com.novamens.kbee.system.parameters;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.service.ServiceLocator;
import com.novamens.system.parameters.SystemParameterService;

public class KbeeSystemParameterService implements SystemParameterService {
	
	public String getParameter(String key, String defaultvalue) {
		if (key==null)
			return  defaultvalue;
		
		return getContentDao().findSystemParameterValueByKey(key.toLowerCase().trim(), defaultvalue);
	}
	
	public int getIntegerParameter(String key, int default_value) {
		if (key==null)
			return  default_value;
		String str=getContentDao().findSystemParameterValueByKey(key.toLowerCase().trim(), String.valueOf(default_value));
		try {
			Integer in = Integer.valueOf(str);
			return in.intValue();
		} catch (Exception e) {
			return default_value;
		}
	}
	
	private ContentDao getContentDao() {
		BeansService beans = ServiceLocator.getService(BeansService.class);
		return (ContentDao) beans.getBean("contentDao");
	}
}