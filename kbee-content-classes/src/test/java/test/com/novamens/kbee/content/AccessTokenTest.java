package test.com.novamens.kbee.content;


import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

import com.novamens.kbee.content.document.KbeeIDoc;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.kbee.text.KbeeTextTemplate;

public class AccessTokenTest  {
	
	
    // Algorithm used
    private final static String ALGORITHM = "DES";

    /**
     * Encrypt data
     * @param secretKey -   a secret key used for encryption
     * @param data      -   data to encrypt
     * @return  Encrypted data
     * @throws Exception
     */
    public static String cipher(String secretKey, String data) throws Exception {
        // Key has to be of length 8
        if (secretKey == null || secretKey.length() != 8)
            throw new Exception("Invalid key length - 8 bytes key needed!");

        SecretKey key = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        return toHex(cipher.doFinal(data.getBytes()));
    }

    /**
     * Decrypt data
     * @param secretKey -   a secret key used for decryption
     * @param data      -   data to decrypt
     * @return  Decrypted data
     * @throws Exception
     */
    public static String decipher(String secretKey, String data) throws Exception {
        // Key has to be of length 8
        if (secretKey == null || secretKey.length() != 8)
            throw new Exception("Invalid key length - 8 bytes key needed!");

        SecretKey key = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);

        return new String(cipher.doFinal(toByte(data)));
    }

    // Helper methods

    private static byte[] toByte(String hexString) {
        int len = hexString.length()/2;

        byte[] result = new byte[len];

        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2*i, 2*i+2), 16).byteValue();
        return result;
    }

    public static String toHex(byte[] stringBytes) {
        StringBuffer result = new StringBuffer(2*stringBytes.length);

        for (int i = 0; i < stringBytes.length; i++) {
            result.append(HEX.charAt((stringBytes[i]>>4)&0x0f)).append(HEX.charAt(stringBytes[i]&0x0f));
        }

        return result.toString();
    }

    private final static String HEX = "0123456789ABCDEF";
	@Test
	public void test01() {
	        try {

	        	KbeeJson json = new KbeeJson();
	        	
	            String secretKey  = "01234567";
	            String data0="version=01";
	            
	            json.put("id", "1212121");
	            json.put("date", "12/12/1969:15:15:32:345");
	            json.put("domain", "11212");
	            
	            
	            //String data="id=1253636;date=12/12/1969:15:15:32:345;domain=12345";
	            String data=json.toString();
	            String encryptedData = cipher(secretKey, data0);

	            // System.out.println("encryptedData: " + encryptedData);

	            String decryptedData = decipher(secretKey, encryptedData);

	            // System.out.println("decryptedData: " + decryptedData);
	            
	            
	            
	            String keyString = "averylongkeytextipsofacto#13789";
	            String input = data;

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
	            byte[] encrypted = cipher.doFinal(input.getBytes("UTF-8"));
	            String e = new String(toHex(encrypted));
	            String p = new String(toHex(iv));
	            // System.out.println("encrypted: " + e+";"+p+";"+encryptedData);
	            // System.out.println("encrypted: " + p);

	            // include the IV with the encrypted bytes for transport, you'll
	            // need the same IV when decrypting (it's safe to send unencrypted)

	            // decrypt
	            IvParameterSpec ivSpec2 = new IvParameterSpec(toByte(p));
	            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec2);
	            byte[] decrypted = cipher.doFinal(toByte(e));
	            // System.out.println("decrypted: " + new String(decrypted, "UTF-8"));
	            KbeeJson d = new KbeeJson(new String(decrypted, "UTF-8"));
	            // System.out.println("decrypted: " + d.toString());
	            // System.out.println("decrypted: " + d.get("id"));

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	 
	}
}
