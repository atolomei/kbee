package com.novamens.kbee.kbfs.encryption;

import com.novamens.kbee.kbfs.encryption.interfaces.KeyEncryptor;

import kbee.util.PropertiesFactory;

public class KbeeKeyEncryptorWrapper implements KeyEncryptor {

	public final String vaultUrl = PropertiesFactory.getInstance("kbee").getProperties().getProperty("vault.url"); 

	private VaultKeyEncryptor vaultEncryptor;
	private KbeeKeyEncryptor kbeeEncryptor;

    @Override
    public byte[] encryptKey(byte[] key) {
    	return vault() ? (getVaultEncryptor()).encryptKey(key) : (getKbeeEncryptor()).encryptKey(key);
    }

    @Override
    public byte[] decryptKey(byte[] key) {
    	return (new String(key)).startsWith("vault:") ? (getVaultEncryptor()).decryptKey(key) : getKbeeEncryptor().decryptKey(key);
    }

	public VaultKeyEncryptor getVaultEncryptor() {
		return vaultEncryptor;
	}

	public void setVaultEncryptor(VaultKeyEncryptor vaultEncryptor) {
		this.vaultEncryptor = vaultEncryptor;
	}

	public KbeeKeyEncryptor getKbeeEncryptor() {
		return kbeeEncryptor;
	}

	public void setKbeeEncryptor(KbeeKeyEncryptor kbeeEncryptor) {
		this.kbeeEncryptor = kbeeEncryptor;
	}
	
	public boolean vault() {
		return vaultUrl!=null;
	}
}