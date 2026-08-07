package com.novamens.kbee.kbfs.encryption;

import com.novamens.kbee.kbfs.encryption.interfaces.KeyEncryptor;
import com.novamens.kbee.kbfs.encryption.interfaces.StreamEncryptor;
import com.novamens.kbee.kbfs.encryption.interfaces.StreamEncryptorInfo;
import com.novamens.util.KbeeRuntimeException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 
 * <p>Enctypts the stream of data with a key provided by the app or by the Vault</p>
 *
 */
public class JCipherStreamEncryptor implements StreamEncryptor {
    private String encryptionAlgorithm;
    private String keyAlgorithm;
    private KeyEncryptor keyEncryptor;

    public JCipherStreamEncryptor(String encryptionAlgorithm, String keyAlgorithm, KeyEncryptor keyEncryptor) {
        this.encryptionAlgorithm = encryptionAlgorithm;
        this.keyAlgorithm = keyAlgorithm;
        this.keyEncryptor = keyEncryptor;
    }

    public JCipherStreamEncryptor() {
    }

    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
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

    public String genNewKey(){
        try {
            //Creating a KeyGenerator object
            KeyGenerator keyGen = KeyGenerator.getInstance(keyAlgorithm);
            //Creating a SecureRandom object
            SecureRandom secRandom = new SecureRandom();
            //Initializing the KeyGenerator
            keyGen.init(secRandom);
            //Creating/Generating a key
            Key key = keyGen.generateKey();

            return Base64.getEncoder().encodeToString(key.getEncoded());
        }catch (NoSuchAlgorithmException e){
            throw new KbeeRuntimeException(e);
        }
    }

    @Override
    public StreamEncryptorInfo getStreamEncryptionInfo(String key) {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        String encryptKey = Base64.getEncoder().encodeToString(keyEncryptor.encryptKey(decodedKey));
        return new JCipherStreamEncryptorInfo(this, encryptKey);
    }

    @Override
    public KbeeEncryptedInputStream encrypt(InputStream inputStream, String key) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(key);
            InputStream encryptedStream = processStream(inputStream, Cipher.ENCRYPT_MODE, decodedKey);
            StreamEncryptorInfo streamEncryptionInfo = this.getStreamEncryptionInfo(key);

            return new KbeeEncryptedInputStream(encryptedStream, streamEncryptionInfo);
        } 
        catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            throw new KbeeRuntimeException(e);
        }
    }

    @Override
    public InputStream decrypt(InputStream inputStream, String encryptedKey) {
        try {
            byte[] decodedEncryptedkey = Base64.getDecoder().decode(encryptedKey);
            byte[] key = keyEncryptor.decryptKey(decodedEncryptedkey);
            return processStream(inputStream, Cipher.DECRYPT_MODE, key);
        } 
        catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            throw new KbeeRuntimeException(e);
        }
    }

    private InputStream processStream(InputStream inputStream, int encryptMode, byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, getKeyAlgorithm());
        Cipher c = Cipher.getInstance(getEncryptionAlgorithm());
        c.init(encryptMode, secretKeySpec);
        return new CipherInputStream(inputStream, c);
    }
}
