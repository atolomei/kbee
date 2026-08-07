package com.novamens.service;

public interface ServiceFactory<T extends Service>{
	public boolean isFactory(Class<? extends Service> serviceClass);
	public <S extends ObjectService> S getService(Object object);
	public <S extends SystemService> S getService();
}