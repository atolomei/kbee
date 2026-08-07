package com.novamens.kbee.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class LocalAuthenticationToken extends UsernamePasswordAuthenticationToken {
	private static final long serialVersionUID = 1L;

	public LocalAuthenticationToken(Object principal) {
		super(principal, null);
	}
}