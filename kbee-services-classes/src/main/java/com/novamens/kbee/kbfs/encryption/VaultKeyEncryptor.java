package com.novamens.kbee.kbfs.encryption;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.novamens.kbee.kbfs.encryption.interfaces.KeyEncryptor;
import com.novamens.kbee.vault.VaultService;
import com.novamens.service.ServiceLocator;
import org.springframework.util.Base64Utils;

import java.nio.charset.StandardCharsets;

public class VaultKeyEncryptor implements KeyEncryptor {
    private String keyID;

    @Override
    public byte[] encryptKey(byte[] key) {
        String keyStr = Base64Utils.encodeToString(key);
        return getVaultService().encrypt(this.getKeyID(), keyStr).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] decryptKey(byte[] key) {
        String keyStr = new String(key, StandardCharsets.UTF_8);
        return Base64Utils.decodeFromString(getVaultService().decrypt(this.getKeyID(), keyStr));
    }

    public String getKeyID() {
        return keyID;
    }

    public void setKeyID(String keyID) {
        this.keyID = keyID;
    }

    @JsonIgnore
    public VaultService getVaultService() {
        return ServiceLocator.getService(VaultService.class);
    }
}