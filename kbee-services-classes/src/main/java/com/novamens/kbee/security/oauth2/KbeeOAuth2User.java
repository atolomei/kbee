package com.novamens.kbee.security.oauth2;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class KbeeOAuth2User extends User implements OAuth2User {
	private static final long serialVersionUID = 1L;
	
	Map<String, Object> attributes;
	
	public KbeeOAuth2User(Serializable id, String username, String password, boolean enabled, Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes) {
		super(username, password, enabled, true, true, true, authorities);
		this.attributes = attributes;
	}
	
	public String getName() {
		return getUsername();
	}
	
	public Map<String, Object> getAttributes() {
		return attributes;
	}
}
