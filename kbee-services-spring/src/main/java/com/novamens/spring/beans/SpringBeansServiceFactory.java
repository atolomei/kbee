package com.novamens.spring.beans;

import com.novamens.beans.BeansService;
import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.Service;
import com.novamens.service.SystemService;

public class SpringBeansServiceFactory extends AbstractServiceFactory<BeansService> {
	private SpringBeansService service;
	
	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.equals(BeansService.class);
	}
	
	@SuppressWarnings("unchecked")
	public <S extends SystemService> S getService() {
		if (service==null) service = new SpringBeansService();
		return  (S)service;
	}	
}