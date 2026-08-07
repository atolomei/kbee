package com.novamens.kbee.kbfs.encryption.interfaces;

import com.novamens.kbee.kbfs.encryption.KbeeEncryptedInputStream;

import java.io.InputStream;

public interface StreamEncryptor {
    KbeeEncryptedInputStream encrypt(InputStream inputStream, String key);
    InputStream decrypt(InputStream inputStream, String encryptedKey);

    String genNewKey();
    StreamEncryptorInfo getStreamEncryptionInfo(String key);
}
