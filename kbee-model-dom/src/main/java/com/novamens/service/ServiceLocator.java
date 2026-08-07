package com.novamens.service;

public abstract class ServiceLocator {

	private static ServiceLocator Instance;
	
	public static ServiceLocator getInstance() {
		return Instance;
	}
	
	public static void setInstance(ServiceLocator instance) {
		Instance = instance;
	}

	public static <T extends ObjectService> T getService(Object object, Class<T> service) {
		return getInstance()._getService(object, service);
	}
	
	public static <T extends SystemService> T getService(Class<T> service) {
    		return getInstance()._getService(service);
	}
	
	abstract protected <T extends ObjectService> T _getService(Object object, Class<T> service);
	
	abstract protected <T extends SystemService> T _getService(Class<T> service);
}