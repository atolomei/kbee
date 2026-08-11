package com.novamens.kbee.content.webapi.type;

import com.novamens.security.User;

import kbee.api.model.ApiProxy;

public class ApiUserProxy extends ApiProxy {
	private static final long serialVersionUID = 1L;
	
	public ApiUserProxy(User user) {
		setHRef(UriHelper.getUri(user));
		setId(String.valueOf(user.getId()));
		setName(user.getDisplayName());
		setRel("user");
	}
}
