package com.novamens.encryption;

import com.novamens.service.SystemService;

public interface EncryptionSystemService extends SystemService{
    public String encrypt(String string);
    public String decrypt(String string);
}