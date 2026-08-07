package com.novamens.kbee.vault;

import java.util.Properties;

import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.Service;
import com.novamens.service.SystemService;
import com.novamens.util.KbeeRuntimeException;

import kbee.util.PropertiesFactory;

public class VaultServiceFactory extends AbstractServiceFactory<SystemService> {

	private VaultService service;
	
	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.equals(VaultService.class);
	}
	
	@SuppressWarnings("unchecked")
	public <S extends SystemService> S getService() {
		if (service == null) {
	       	Properties properties = PropertiesFactory.getInstance("kbee").getProperties(); 
        	String url = properties.getProperty("vault.url");
        	if (url==null) {
        		throw new KbeeRuntimeException("Vault service cannot be initialized. Property 'vault.url' must point to the Vault's URL");
        	}
        	service = new VaultService();
        	service.setUrl(url);
		}
		return (S)service;
	}
	
}
