package com.novamens.kbee.kbfs;

import com.novamens.kbfs.FileServerOdilon;
import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.Service;
import com.novamens.service.SystemService;

/*
* <p>Depending on the Spring configuration it should create a:
* 
* {@link KbeeShardedOdilonFileServer}
* {@link KbeeOdilonFileServer}
* 
*</p>
*/
public class KbeeFileServerOdilonFactory extends AbstractServiceFactory<SystemService> {

	private FileServerOdilon service;
	
	//private boolean started = false;
	
	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.isInstance(getService());
	}
	
	@SuppressWarnings("unchecked")
	public <S extends SystemService> S getService() {
		return (S)service;
	}
	
	public void setService(FileServerOdilon service) {
		this.service = service;
	}
	
}
