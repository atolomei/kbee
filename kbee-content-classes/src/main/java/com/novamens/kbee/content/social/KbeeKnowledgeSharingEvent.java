package com.novamens.kbee.content.social;

import com.novamens.content.base.KnowledgeSharing;
import com.novamens.content.social.KnowledgeSharingEvent;
import com.novamens.event.AbstractEvent;
import com.novamens.security.User;

/**
 * Comment, Answer 
 *
 */
public class KbeeKnowledgeSharingEvent extends AbstractEvent implements KnowledgeSharingEvent {

	private KnowledgeSharing ks_object;
	
	public KbeeKnowledgeSharingEvent(Object object, KnowledgeSharing ks) {
		super(object);
		this.ks_object=ks;  
	}
	
	@Override
	public KnowledgeSharing getSocialObject() {
		return ks_object;
	}
	
	
	@Override
	public User getUser() {
		return ks_object.getLastModifiedUser();
	}
	
}
