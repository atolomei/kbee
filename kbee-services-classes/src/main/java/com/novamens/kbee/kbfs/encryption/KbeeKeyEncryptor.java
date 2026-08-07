package com.novamens.kbee.kbfs.encryption;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.novamens.kbee.kbfs.encryption.interfaces.KeyEncryptor;
import com.novamens.util.KbeeRuntimeException;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class KbeeKeyEncryptor implements KeyEncryptor {

    @JsonIgnore
    private byte[] encryptorKey= "Yq3t6v9y$B&E)H@M".getBytes();

    @JsonIgnore
    private String encryptionAlgorithm = "AES/ECB/PKCS5Padding";

    @JsonIgnore
    private String keyAlgorithm = "AES";

    private byte[] processBytes(byte[] bytes, int encryptMode, byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, keyAlgorithm);
        Cipher c = Cipher.getInstance(encryptionAlgorithm);
        c.init(encryptMode, secretKeySpec);
        return c.doFinal(bytes);
    }

    @Override
    public byte[] encryptKey(byte[] key) {
        try {
            return processBytes(key,Cipher.ENCRYPT_MODE, encryptorKey);
        } 
        catch (Exception e){
            throw new KbeeRuntimeException(e);
        }
    }

    @Override
    public byte[] decryptKey(byte[] key) {
        try {
            return processBytes(key,Cipher.DECRYPT_MODE, encryptorKey);
        } 
        catch (Exception e){
            throw new KbeeRuntimeException(e);
        }
    }
}
