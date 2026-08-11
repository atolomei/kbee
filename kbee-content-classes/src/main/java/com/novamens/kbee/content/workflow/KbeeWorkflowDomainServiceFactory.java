package com.novamens.kbee.content.workflow;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.dom.Domain;
import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.ObjectService;
import com.novamens.service.Service;
import com.novamens.service.ServiceLocator;
import com.novamens.spring.service.SpringServiceLocator;

import kbee.util.PropertiesFactory;

public class KbeeWorkflowDomainServiceFactory extends AbstractServiceFactory<ObjectService>{

	private static boolean workflow = Arrays.asList(PropertiesFactory.getInstance("kbee").getModules()).contains("workflow");
	private Map<Serializable, WorkflowDomainService> services = Collections.synchronizedMap(new HashMap<Serializable, WorkflowDomainService>());
	
	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.equals(WorkflowDomainService.class);
	}
	
	@SuppressWarnings("unchecked")
	public <S extends ObjectService> S getService(Object object) {
		Assert.isInstanceOf(Domain.class, object);
		SpringServiceLocator serviceLocator = (SpringServiceLocator)ServiceLocator.getInstance();
		if (serviceLocator.getContext().containsBean(getBean((Domain)object)))
			return (S)serviceLocator.getContext().getBean(getBean((Domain)object), object);
		else
			if (workflow) {
				WorkflowDomainService service = services.get(((Domain)object).getId());
				if (service == null) {
					synchronized (this) {
						service = services.get(((Domain)object).getId());
						if (service == null) {
							service = (WorkflowDomainService)serviceLocator.getContext().getBean("domain-workflow-service", object);
							services.put(((Domain)object).getId(), service);
						}
					}
				}
				return (S)service;
			}
			else 
				return null;
	}
	
	private String getBean(Domain domain) {
		return domain.getName()+"-domain-workflow-service";
	}
}
