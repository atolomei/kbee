package com.novamens.kbee.content.script;

import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class KbeeUserScriptWrapper {
	
	private User user;

	public KbeeUserScriptWrapper(User user) {
		this.user = user;
	}
	
	public boolean isMember(String groupname) {
		return ServiceLocator.getService(SecurityService.class).isMember(user, groupname);
	}
}