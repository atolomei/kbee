package com.novamens.kbee.security;

import org.springframework.security.authentication.AuthenticationManager;

public class WebSecurityService extends KbeeSecurityService {
			
	public WebSecurityService() {
	}
	
	public AuthenticationManager getAuthenticationManager() {
		return null;
	};
}
