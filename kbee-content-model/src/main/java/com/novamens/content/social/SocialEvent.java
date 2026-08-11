package com.novamens.content.social;

import com.novamens.content.base.Social;
import com.novamens.event.Event;
import com.novamens.security.User;

public interface SocialEvent extends Event {
	
	public final int VOTE 	 = 2;
	public final int REPORT  = 3;
	
	public User getUser();
	public Social getSocialObject();
	public String getSocialObjectUrl();
	
}

