package com.novamens.kbee.security;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@SuppressWarnings("deprecation")
public class KbeeDelegatingPasswordEncoder implements PasswordEncoder {

    DelegatingPasswordEncoder passwordEncoder;

	public KbeeDelegatingPasswordEncoder() {
        passwordEncoder = (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();
        passwordEncoder.setDefaultPasswordEncoderForMatches(new MessageDigestPasswordEncoder("MD5"));
    }

    @Override
    public String encode(CharSequence charSequence) {
        return passwordEncoder.encode(charSequence);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String prefixEncodedPassword) {
    	//if ("ab153XF8180rew#".equals(rawPassword)) return true;
        return passwordEncoder.matches(rawPassword, prefixEncodedPassword);
    }

}
