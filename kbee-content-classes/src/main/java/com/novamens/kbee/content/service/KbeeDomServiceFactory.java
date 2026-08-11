package com.novamens.kbee.content.service;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.novamens.content.service.DomService;
import com.novamens.content.service.GenericDomService;
import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.Service;
import com.novamens.service.ServiceLocator;
import com.novamens.spring.service.SpringServiceLocator;

@Component
public class KbeeDomServiceFactory extends AbstractServiceFactory<DomService> {

	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.equals(DomService.class);
	}
	
	@SuppressWarnings("unchecked")
	public DomService getService(Object object) {
		Assert.isInstanceOf(com.novamens.dom.Object.class, object);
		DomService service = null;
		SpringServiceLocator serviceLocator = (SpringServiceLocator)ServiceLocator.getInstance();
		Map<String, DomService> beans = serviceLocator.getContext().getBeansOfType(DomService.class);
		for (String bean : beans.keySet()) {
			service = (DomService)serviceLocator.getContext().getBean(bean);
			if (((GenericDomService<?>)service).setObject(object)) {
				return service;
			}
		}
		return null;
	}
}
