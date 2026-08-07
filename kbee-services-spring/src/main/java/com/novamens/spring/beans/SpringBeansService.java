package com.novamens.spring.beans;


import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.novamens.beans.BeansService;
import com.novamens.service.ServiceLocator;
import com.novamens.spring.service.SpringServiceLocator;


public class SpringBeansService implements BeansService {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SpringBeansService.class.getName());
	
	private ApplicationContext context;
	
	public SpringBeansService() {
		SpringServiceLocator serviceLocator = (SpringServiceLocator)ServiceLocator.getInstance();
		this.context = serviceLocator.getContext();
	}
	
	public Object getBean(String name) {
		return context.getBean(name);
	}
	
	public boolean containsBean(String name) {
		return context.containsBean(name);
	}
	
	public Object getBean(String name, Object... args) {
		Object object = context.getBean(name, args);
		logger.debug(name +" -> " +  (object!=null?object.getClass().getName(): "null") );
		return object;
	}
	
	public <T> Map<String, T> getBeansOfType(Class<T> type) {
		return context.getBeansOfType(type);
	}
}
