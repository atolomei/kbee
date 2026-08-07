package com.novamens.kbee.encryption;

import com.novamens.encryption.EncryptionSystemService;
import com.novamens.service.AbstractServiceFactory;
import com.novamens.service.Service;
import com.novamens.service.SystemService;

import kbee.util.PropertiesFactory;

public class EncryptionSystemServiceFactory extends  AbstractServiceFactory<SystemService> {

	public final String vaultUrl = PropertiesFactory.getInstance("kbee").getProperties().getProperty("vault.url");
	
	private String vaultKey;
	private EncryptionSystemService service;
	
	public boolean isFactory(Class<? extends Service> serviceClass) {
		return serviceClass.isAssignableFrom(EncryptionSystemService.class);
	}
	
	@SuppressWarnings("unchecked")
	public <S extends SystemService> S getService() {
		if (service==null) {
			if (vaultUrl==null) {
				service = new KbeeEncryptionService();
			}
			else {
				service = new VaultEncryptionService(getVaultKey());
				((VaultEncryptionService)service).setKbeeEncryptionService(new KbeeEncryptionService());
			}
		}
		return (S)service;
	}

	public String getVaultKey() {
		return vaultKey;
	}

	public void setVaultKey(String vaultKey) {
		this.vaultKey = vaultKey;
	}
	
}
