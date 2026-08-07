package com.novamens.kbee.security.jwt;

import java.security.interfaces.RSAPublicKey;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

public class InternalJwtDecoderFactory {

    public JwtDecoder createDecoder(RSAPublicKey publicKey) {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }
}