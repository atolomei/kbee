package com.novamens.kbee.system;


import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.service.ServiceLocator;
import com.novamens.system.SystemParameter;

/**
 * 
 *
 */
public class SystemParameters {
			
	static private Logger logger = LogManager.getLogger(SystemParameters.class.getName());
	
	static public String get(String parametername, String defaultvalue) {
		String value = null;
		SystemParameter parameter = getContentDao().findSystemParameterByKey(parametername);
		value = parameter==null || parameter.getValue()==null ? defaultvalue : parameter.getValue();
		return value;
	}
	
	static public String get(String parameter) {
		try {
			return getContentDao().findSystemParameterByKey(parameter).getValue();
		} catch (Exception e) {
			logger.error(e.getClass().getName() + " | parameter: " + parameter);
			return null;
		}
	}
	
	static List<String> getParameterNames() {
		List<String> names = new ArrayList<String>();
		for (SystemParameter parameter : getContentDao().getSystemParameters()) {
			names.add(parameter.getKey());
		}
		return names;
	}
	
	static private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
