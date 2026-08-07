package com.novamens.kbee.kbfs;


import com.novamens.kbfs.LocalFileServerCache;
import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.Service;
import com.novamens.service.SystemService;

public class KbeeLocalFileServerCacheFactory extends AbstractServiceFactory<SystemService> {
				
	private LocalFileServerCache  service;

	boolean started = false;
	
	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.isInstance(getService());
	}
	
	@SuppressWarnings("unchecked")
	public <S extends SystemService> S getService() {
		return (S)service;
	}
	
	public void setService(LocalFileServerCache service) {
		this.service = service;
	}
}
