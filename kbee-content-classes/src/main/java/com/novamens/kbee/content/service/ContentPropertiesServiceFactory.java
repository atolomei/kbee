package com.novamens.kbee.content.service;

import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.Service;

public class ContentPropertiesServiceFactory extends AbstractServiceFactory<KbeeContentPropertiesService> {

	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.equals(KbeeContentPropertiesService.class);
	}
	
	@SuppressWarnings("unchecked")
	public KbeeContentPropertiesService getService(Object content) {
		Assert.isInstanceOf(Content.class, content);
		return new KbeeContentPropertiesService((Content)content);
	}
	
}
