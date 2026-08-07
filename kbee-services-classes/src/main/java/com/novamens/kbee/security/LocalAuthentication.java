package com.novamens.kbee.security;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class LocalAuthentication implements Authentication {
	private static final long serialVersionUID = 1L;
	
	private Authentication authentication;
	
	public LocalAuthentication(Authentication authentication) {
		this.authentication = authentication;
	}
	
	public Authentication getAuthentication() {
		return this.authentication;
	}
	
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return getAuthentication().getAuthorities();
	}
	
	public Object getCredentials() {
		return getAuthentication().getCredentials();
	}

	public Object getDetails() {
		return getAuthentication().getDetails();
	}
	
	public Object getPrincipal() {
		return getAuthentication().getPrincipal();
	}
	
	public String getName() {
		return getAuthentication().getName();
	}

	public boolean isAuthenticated() {
		return getAuthentication().isAuthenticated();
	}

	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		getAuthentication().setAuthenticated(isAuthenticated);
	}
}
