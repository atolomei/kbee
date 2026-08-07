package com.novamens.service;

public class AbstractServiceFactory<T extends Service> implements ServiceFactory<T> {

	public boolean isFactory(Class<? extends Service> serviceClass) {
		return false;
	}
	
	public <S extends ObjectService> S getService(Object object) {
		return null;
	}	
	
	public <S extends SystemService> S getService() {
		return null;
	}
}