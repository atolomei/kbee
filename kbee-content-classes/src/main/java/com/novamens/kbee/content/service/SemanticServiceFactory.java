package com.novamens.kbee.content.service;

import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.Service;

public class SemanticServiceFactory extends AbstractServiceFactory<SemanticService> {

	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.equals(SemanticService.class);
	}
	
	@SuppressWarnings("unchecked")
	public SemanticService getService(Object object) {
		Assert.isInstanceOf(Content.class, object);
		return new KbeeSemanticService((Content)object);
	}	
}
