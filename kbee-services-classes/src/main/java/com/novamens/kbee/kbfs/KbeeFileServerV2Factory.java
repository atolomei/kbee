package com.novamens.kbee.kbfs;


import com.novamens.kbfs.FileServerMinio;
import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.Service;
import com.novamens.service.SystemService;


/**
 * Depending on the Spring configuration it should create a:
 * 
 * {@link KbeeShardedMinioFileServer}
 * {@link KbeeMinioFileServer}
 *
 */
public class KbeeFileServerV2Factory extends AbstractServiceFactory<SystemService> {
	
	private FileServerMinio service;

	boolean started = false;
	
	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.isInstance(getService());
	}
	
	@SuppressWarnings("unchecked")
	public <S extends SystemService> S getService() {
		return (S)service;
	}
	
	public void setService(FileServerMinio service) {
		this.service = service;
	}
}
