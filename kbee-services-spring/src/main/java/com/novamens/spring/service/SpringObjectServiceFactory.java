package com.novamens.spring.service;

import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.ObjectService;
import com.novamens.service.Service;
import com.novamens.service.ServiceLocator;

public class SpringObjectServiceFactory extends AbstractServiceFactory<ObjectService> {
	private String bean;
	private ObjectService prototype;
	
	public boolean isFactory(Class<? extends Service> serviceClass) {
		if (prototype == null) {
			SpringServiceLocator serviceLocator = (SpringServiceLocator)ServiceLocator.getInstance();
			prototype = (ObjectService)serviceLocator.getContext().getBean(getBean());
		}	
		return serviceClass.isInstance(prototype);
	}
	
	@SuppressWarnings("unchecked")
	public <S extends ObjectService> S getService(Object object) {
		SpringServiceLocator serviceLocator = (SpringServiceLocator)ServiceLocator.getInstance();
		return (S)serviceLocator.getContext().getBean(getBean(), object);
	}
	
	public void setBean(String beanname) {
		this.bean = beanname;
	}
	
	public String getBean() {
		return bean;
	}
}
