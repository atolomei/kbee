package com.novamens.kbee.security.oauth2;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class KbeeAuthenticationDetails extends WebAuthenticationDetails {
	private static final long serialVersionUID = 1L;
	
	private String domain;

	public KbeeAuthenticationDetails(HttpServletRequest request, String domain) {
		super(request);
		setDomain(domain);
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
}
