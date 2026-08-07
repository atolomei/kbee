package com.novamens.spring.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.novamens.service.ObjectService;
import com.novamens.service.Service;
import com.novamens.service.ServiceFactory;
import com.novamens.service.ServiceLocator;
import com.novamens.service.SystemService;
import com.novamens.spring.ApplicationContextFactory;

public class SpringServiceLocator extends ServiceLocator {
		
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SpringServiceLocator.class.getName());

	
	private List<ServiceFactory<?>> factories;
	private ApplicationContext context;
	private Map<Class<?>, ServiceFactory<?>> cache = Collections.synchronizedMap(new HashMap<Class<?>, ServiceFactory<?>>());

	public SpringServiceLocator(String context) {
		try {
			this.context = ApplicationContextFactory.getInstance(context);
		}
 		catch (Throwable e) {
			logger.error(e);
			throw(e);
		}
	}
	
	@Override
	protected <T extends ObjectService> T _getService(Object object, Class<T> service) {
		
		try {
		ServiceFactory<? extends Service> factory = cache.get(service);
		if (factory!=null) {
			return factory.getService(object);
		};
		for (ServiceFactory<? extends Service> f : getFactories()) {
			if (f.isFactory(service)) {
				cache.put(service, f);
				return f.getService(object);
			}
		}
		return null;
		} catch (Exception e) {
			logger.error(e);
			throw(e);
		}
	}
	
	public ApplicationContext getContext() {
		return context;
	}
	
	@Override
	protected <T extends SystemService> T _getService(Class<T> service) {
		ServiceFactory<? extends Service> factory = cache.get(service);
		if (factory!=null) {
			return factory.getService();
		};
		for (ServiceFactory<? extends Service> f : getFactories()) {
			if (f.isFactory(service)) {
				cache.put(service, f);
				return f.getService();
			}
		}
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	protected List<ServiceFactory<?>> getFactories() {
		if (factories == null) {
			factories = new ArrayList<ServiceFactory<?>>();
			Map<String, ServiceFactory> beans = getContext().getBeansOfType(ServiceFactory.class);
			for (String bean : beans.keySet()) {
				factories.add((ServiceFactory<?>)getContext().getBean(bean));
			}
		}
		return factories;
	}
}
