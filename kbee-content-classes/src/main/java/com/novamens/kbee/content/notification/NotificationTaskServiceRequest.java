package com.novamens.kbee.content.notification;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.model.ObjectId;
import com.novamens.dom.ObjectID;
import com.novamens.event.LogEvent;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;

import kbee.util.logging.Logger;

@Deprecated
public abstract class NotificationTaskServiceRequest extends AbstractServiceRequest {
	private static final long serialVersionUID = 1L;
	
	private ObjectId objectId;
	
	private static Logger logger = Logger.getLogger(NotificationTaskServiceRequest.class.getName());

	public NotificationTaskServiceRequest(LogEvent event) {
		setEvent(event);
		try {
			setObjectID(new ObjectID(event).toString());
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	@Override
	public void execute() {
		notify(getEvent());
	}
	
	protected void notify(LogEvent event) {	}

	public void setEvent(LogEvent event) {
		objectId = new ObjectId(event);
	}
	
	public LogEvent getEvent() {
		LogEvent event = null;
		try {
			event = (LogEvent)getContentDao().findObjectById(objectId);
		} 
		catch (ContentMgmtException e) {
			logger.error(e);
		}	
		return event;
	}

	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected void sendNotification(Group group, Content content, ENotiRule rule, LogEvent event) {
		if (group.isEnabled() && (group instanceof KbeeGroup)) {
			for (Principal pi: ((KbeeGroup) group).getMembers()) {
				if 		(pi instanceof User)			sendNotification( (User) pi, content, rule, event);
				else if (pi instanceof Group)			sendNotification((Group) pi, content, rule, event);
			}
		}
	}

	protected abstract void sendNotification(User user, Content content, ENotiRule rule, LogEvent event);
}