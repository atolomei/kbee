package com.novamens.kbee.encryption;

import com.novamens.encryption.EncryptionSystemService;
import com.novamens.kbee.vault.VaultService;
import com.novamens.service.ServiceLocator;

public class VaultEncryptionService implements EncryptionSystemService {
	
    private String keyID;
    EncryptionSystemService kbeeEncryptionService;
    
    public VaultEncryptionService(String key) {
    	setKeyID(key);
    }
    
    public String encrypt(String string) {
    	 return ServiceLocator.getService(VaultService.class).encrypt(keyID, string);
    }
    
    public String decrypt(String string) {
    	if (string.startsWith("vault:"))
    		return ServiceLocator.getService(VaultService.class).decrypt(keyID, string);
    	else
       		return getKbeeEncryptionService().decrypt(string);
    }

	public String getKeyID() {
		return keyID;
	}

	public void setKeyID(String keyID) {
		this.keyID = keyID;
	}

	public EncryptionSystemService getKbeeEncryptionService() {
		return kbeeEncryptionService;
	}

	public void setKbeeEncryptionService(EncryptionSystemService kbeeEncryptionService) {
		this.kbeeEncryptionService = kbeeEncryptionService;
	}
}