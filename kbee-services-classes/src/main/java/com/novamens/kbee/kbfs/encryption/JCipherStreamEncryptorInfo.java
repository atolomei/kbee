package com.novamens.kbee.kbfs.encryption;

import com.novamens.kbee.kbfs.encryption.interfaces.KeyEncryptor;
import com.novamens.kbee.kbfs.encryption.interfaces.StreamEncryptor;
import com.novamens.kbee.kbfs.encryption.interfaces.StreamEncryptorInfo;


public class JCipherStreamEncryptorInfo implements StreamEncryptorInfo {
    private String encryptionAlgorithm;
    private String encryptedKey;
    private String keyAlgorithm;
    private KeyEncryptor keyEncryptor;

    public JCipherStreamEncryptorInfo() {
    }

    public JCipherStreamEncryptorInfo(JCipherStreamEncryptor jCipherStreamEncryption, String encryptedKey) {
        this.setEncryptionAlgorithm(jCipherStreamEncryption.getEncryptionAlgorithm());
        this.setKeyAlgorithm(jCipherStreamEncryption.getKeyAlgorithm());
        this.setKeyEncryptor(jCipherStreamEncryption.getKeyEncryptor());
        this.setEncryptedKey(encryptedKey);
    }

    @Override
    public StreamEncryptor getStreamEncryption() {
        return new JCipherStreamEncryptor(encryptionAlgorithm, keyAlgorithm, keyEncryptor);
    }

    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    @Override
    public String getEncryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    public KeyEncryptor getKeyEncryptor() {
        return keyEncryptor;
    }

    public void setKeyEncryptor(KeyEncryptor keyEncryptor) {
        this.keyEncryptor = keyEncryptor;
    }
}
