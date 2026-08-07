package com.novamens.kbee.encryption;

import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.novamens.encryption.EncryptionSystemService;

public class KbeeEncryptionService implements EncryptionSystemService {
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeEncryptionService.class.getName());

	private final static String ALGORITHM = "DES";
	private final static String HEX = "0123456789ABCDEF";

	private String secretKey = "72132671";
	private int version = 1;
	private String[] keys = { "averylongkeytextipsofacto#13789" };
    
	public String encrypt(String string) {
		try {
			String encryptedversion = cipher(String.valueOf(getVersion()));
			
			String keyString = keys[getVersion()-1];
	
			// setup AES cipher in CBC mode with PKCS #5 padding
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	
			// setup an IV (initialization vector) that should be
			// randomly generated for each input that's encrypted
			byte[] iv = new byte[cipher.getBlockSize()];
			new SecureRandom().nextBytes(iv);
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
	
			// hash keyString with SHA-256 and crop the output to 128-bit for key
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(keyString.getBytes());
			byte[] key = new byte[16];
			System.arraycopy(digest.digest(), 0, key, 0, key.length);
			SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
	
			// encrypt
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
			byte[] encryptedbytes = cipher.doFinal(string.getBytes("UTF-8"));
			
			String encryptedstring = new String(toHex(encryptedbytes))+"&"+new String(toHex(iv))+"&"+encryptedversion;
			
			return encryptedstring;
		}
		catch (Exception e) {
			logger.error(e);
		}
		return null;
    }
	
    public String decrypt(String encrypted) {
		try {
			
			String tokens[] = encrypted.split("&");
			
			if (tokens.length<3) { 
				
				logger.error("------------------------------------------------------");
				logger.error("there must be 3 tokens in -> " + encrypted);
				if (encrypted.startsWith("vault")) {
					logger.error("This String was encrypted using Vault and can not be decrypted by the internal service.\n check if the Vault is Enabled and online");
				}
				logger.error("------------------------------------------------------");
				return null;
			}
			
			String version = decipher(tokens[2]);
			
			String keyString = keys[Integer.valueOf(version)-1];
			
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			
			// hash keyString with SHA-256 and crop the output to 128-bit for key
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(keyString.getBytes());
			byte[] key = new byte[16];
			System.arraycopy(digest.digest(), 0, key, 0, key.length);
			SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
	
			IvParameterSpec ivSpec2 = new IvParameterSpec(toByte(tokens[1]));
			
			cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec2);
			byte[] decryptedbytes = cipher.doFinal(toByte(tokens[0]));
			
			String descrypted = new String(decryptedbytes, "UTF-8");
			
			return descrypted;
		}
		catch (Exception e) {
			logger.error(e);
		}
		return null;
	}
    
	private int getVersion() {
		return version;
	}
    
	private  String cipher(String data) throws Exception {
		if (secretKey == null || secretKey.length() != 8)
			throw new Exception("Invalid key length - 8 bytes key needed!");
		SecretKey key = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return toHex(cipher.doFinal(data.getBytes()));
	}
	
	public String decipher(String data) throws Exception {
		// Key has to be of length 8
		if (secretKey == null || secretKey.length() != 8)
			throw new Exception("Invalid key length - 8 bytes key needed!");
		SecretKey key = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, key);
		return new String(cipher.doFinal(toByte(data)));
	}
	
	private byte[] toByte(String hexString) {
		int len = hexString.length()/2;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++)
			result[i] = Integer.valueOf(hexString.substring(2*i, 2*i+2), 16).byteValue();
		return result;
	}

	public String toHex(byte[] stringBytes) {
		StringBuffer result = new StringBuffer(2*stringBytes.length);
		for (int i = 0; i < stringBytes.length; i++) {
			result.append(HEX.charAt((stringBytes[i]>>4)&0x0f)).append(HEX.charAt(stringBytes[i]&0x0f));
		}
		return result.toString();
	}
}