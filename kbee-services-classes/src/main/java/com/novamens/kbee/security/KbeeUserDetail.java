package com.novamens.kbee.security;

import java.io.Serializable;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class KbeeUserDetail extends User {
	private static final long serialVersionUID = 1L;
	private Serializable id;
	private boolean is_su = false;
	
	
	public KbeeUserDetail(Serializable id, String username, String password, boolean enabled, Collection<? extends GrantedAuthority> authorities, boolean is_su) {
		super(username, password, enabled, true, true, true, authorities);
		this.is_su=is_su;
		this.id = id;
	}
	
	
	public KbeeUserDetail(Serializable id, String username, String password, boolean enabled, Collection<? extends GrantedAuthority> authorities) {
		super(username, password, enabled, true, true, true, authorities);
		this.id = id;
	}
	
	
	public Serializable getId() {
		return id;
	}
	
	public boolean isSu() {
		return this.is_su;
	}
}
