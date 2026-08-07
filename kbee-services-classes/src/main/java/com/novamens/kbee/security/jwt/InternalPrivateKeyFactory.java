package com.novamens.kbee.security.jwt;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class InternalPrivateKeyFactory {

    public RSAPrivateKey createPrivateKey() throws Exception {
    	
    	try (InputStream is = getClass().getResourceAsStream("private.pem")) {
    
            if (is == null) {
                throw new IllegalArgumentException(
                        "No se encontró la clave");
            }

            String key = new String(
                    is.readAllBytes(),
                    StandardCharsets.UTF_8);

            key = key
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(key);

            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);

            return (RSAPrivateKey) KeyFactory
                    .getInstance("RSA")
                    .generatePrivate(spec);
        }
    }
}