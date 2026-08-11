package com.novamens.kbee.content.workflow;

import java.util.Arrays;

import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.ObjectService;
import com.novamens.service.Service;
import com.novamens.service.ServiceLocator;
import com.novamens.spring.service.SpringServiceLocator;

import kbee.util.PropertiesFactory;

public class KbeeWorkflowServiceFactory extends AbstractServiceFactory<ObjectService>{

	private static boolean workflow = Arrays.asList(PropertiesFactory.getInstance("kbee").getModules()).contains("workflow");

	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.equals(WorkflowService.class);
	}
	
	@SuppressWarnings("unchecked")
	public <S extends ObjectService> S getService(Object object) {
		Assert.isInstanceOf(Content.class, object);
		SpringServiceLocator serviceLocator = (SpringServiceLocator)ServiceLocator.getInstance();
		if (serviceLocator.getContext().containsBean(getBean((Content)object)))
			return (S)serviceLocator.getContext().getBean(getBean((Content)object), object);
		else
			if (workflow) {
				return (S)serviceLocator.getContext().getBean("content-workflow-service", object);
			}
			else 
				return null;
	}
	
	private String getBean(Content content) {
		return content.getDomain().getName()+"-content-workflow-service";
		
	}
}

