package com.novamens.kbee.content.notification;

import com.novamens.content.notification.NotificationService;
import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.Service;
import com.novamens.service.SystemService;

public class NotificationServiceFactory extends	AbstractServiceFactory<SystemService> {
				
	private NotificationService service;

	boolean started = false;
	
	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.isInstance(getService());
	}
	
	@SuppressWarnings("unchecked")
	public <S extends SystemService> S getService() {
		return (S)service;
	}
	
	public void setService(NotificationService service) {
		this.service = service;
	}
}
