package test.com.novamens.kbee.content;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;

import com.novamens.kbee.content.document.KbeeIDoc;
import com.novamens.kbee.text.KbeeTextTemplate;

public class CipherTest  {
	
	
	@Test
	public void test01() {
		try {
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			
			String ciphertext = new String(Base64.decodeBase64("qKp7GDihfFbY/Vn1mnuZeBRKNvifu8T4w7jSKmJhNtG181TbXbo0OnpAtce0YEh3"));
			String keystring = new String(Base64.decodeBase64("9h5bCooSKBtOS0kLG4hYkV6ZF8dBur4OGuYxA7CTJRU="));

			String iv = ciphertext.substring(0,12);
			String actual_ciphertext = ciphertext.substring(12);
			
			
			
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(keystring.getBytes());
			byte[] key = new byte[16];
			System.arraycopy(digest.digest(), 0, key, 0, key.length);
			SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
	
			IvParameterSpec ivSpec2 = new IvParameterSpec(iv.getBytes());
			
			cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec2);
			byte[] decryptedbytes = cipher.doFinal(actual_ciphertext.getBytes());
			
			String descrypted = new String(decryptedbytes, "UTF-8");
			
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
}
