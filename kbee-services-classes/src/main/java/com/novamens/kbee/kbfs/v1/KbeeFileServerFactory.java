package com.novamens.kbee.kbfs.v1;

import com.novamens.kbfs.v1.FileServerV1;
import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.Service;
import com.novamens.service.SystemService;

public class KbeeFileServerFactory extends AbstractServiceFactory<SystemService> {
	
	private FileServerV1 service;

	boolean started = false;
	
	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.isInstance(getService());
	}
	
	@SuppressWarnings("unchecked")
	public <S extends SystemService> S getService() {
		return (S)service;
	}
	
	public void setService(FileServerV1 service) {
		this.service = service;
	}
}
