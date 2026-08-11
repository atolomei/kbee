package com.novamens.kbee.content.script;

import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.novamens.service.Service;
import com.novamens.service.ServiceFactory;
import com.novamens.service.ServiceLocator;
import com.novamens.spring.service.SpringServiceLocator;

import kbee.util.logging.Logger;

@SuppressWarnings("rawtypes")
public class KbeeServiceLocator {
	
	private static Logger logger = Logger.getLogger(SpringServiceLocator.class.getName());

	public Object getService(String serviceClassName) {
		return getService(null, serviceClassName);
	}
	
	@SuppressWarnings("unchecked")
	public Object getService(Object object, String serviceClassName) {
		try {
			Class<? extends Service> serviceClass = (Class<? extends Service>)Class.forName(serviceClassName);
			ApplicationContext context = ((SpringServiceLocator)ServiceLocator.getInstance()).getContext(); 
			Map<String, ServiceFactory> beans = context.getBeansOfType(ServiceFactory.class);
			for (String bean : beans.keySet()) {
				ServiceFactory<?> factory = ((ServiceFactory<?>)context.getBean(bean));
				if (factory.isFactory(serviceClass)) {
					return object==null ? factory.getService() : factory.getService(object);
				}
			}
		}
		catch (ClassNotFoundException e) {
			logger.error(e);
		}
		return null;
	}

}
