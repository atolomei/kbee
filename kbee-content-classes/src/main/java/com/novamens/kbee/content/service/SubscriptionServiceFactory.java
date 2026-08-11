package com.novamens.kbee.content.service;

import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.kbee.content.dao.SubscriptionDao;
import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.Service;

public class SubscriptionServiceFactory extends AbstractServiceFactory<KbeeSubscriptionService> {
	
	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.equals(SubscriptionDao.class);
	}
	
	@SuppressWarnings("unchecked")
	public KbeeSubscriptionService getService(Object object) {
		Assert.isInstanceOf(Content.class, object);
		return new KbeeSubscriptionService((Content) object);
	}

}
