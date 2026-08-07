package com.novamens.kbee.content.version;

import com.novamens.beans.BeansService;
import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.Service;
import com.novamens.service.SystemService;

public class KbeeVersionServiceFactory extends AbstractServiceFactory<BeansService> {
	
	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.equals(BeansService.class);
	}
	
	public <S extends SystemService> S getService() {
		//if (service==null) service = new SpringBeansService();
		//return  (S)service;
		return null;
	}	
}