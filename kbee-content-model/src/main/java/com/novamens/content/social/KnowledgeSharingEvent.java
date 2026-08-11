package com.novamens.content.social;

import com.novamens.content.base.KnowledgeSharing;
import com.novamens.event.Event;
import com.novamens.security.User;

public interface KnowledgeSharingEvent extends Event {
	
	public final int COMMENT  = 10;
	public final int QUESTION = 11;
	public final int ANSWER   = 12;
	
	public KnowledgeSharing getSocialObject();
	public User getUser();
	
}
