package com.novamens.kbee.thumbnail;

import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.Service;
import com.novamens.service.SystemService;
import com.novamens.thumbnail.ThumbnailService;

public class KbeeThumbnailServiceFactory extends  AbstractServiceFactory<SystemService> {

	private ThumbnailService service;

	boolean started = false;
	
	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.isInstance(getService());
	}
	
	@SuppressWarnings("unchecked")
	public <S extends SystemService> S getService() {
		return (S)service;
	}
	
	public void setService(ThumbnailService service) {
		this.service = service;
	}
	
}
