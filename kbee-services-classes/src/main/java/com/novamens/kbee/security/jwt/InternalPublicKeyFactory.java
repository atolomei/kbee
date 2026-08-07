package com.novamens.kbee.security.jwt;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class InternalPublicKeyFactory {


    public RSAPublicKey createPublicKey() throws Exception {

    	try (InputStream is = getClass().getResourceAsStream("public.pem")) {

            if (is == null) {
                throw new IllegalArgumentException(
                        "No se encontró la clave");
            }

            String key = new String(
                    is.readAllBytes(),
                    StandardCharsets.UTF_8);

            key = key
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(key);

            X509EncodedKeySpec spec =
                    new X509EncodedKeySpec(decoded);

            return (RSAPublicKey) KeyFactory
                    .getInstance("RSA")
                    .generatePublic(spec);
        }
    }
}