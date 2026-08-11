package com.novamens.kbee.content.notification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.model.ObjectId;
import com.novamens.content.notes.Billboard;
import com.novamens.content.notification.NotificationTask;
import com.novamens.content.security.Role;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.event.LogEvent;
import com.novamens.kbee.content.security.KbeeAbstractRole;
import com.novamens.logging.WorkNoteUpdateEvent;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;

import kbee.util.logging.Logger;

public class WorkNoteHandler extends AbstractLogEventNotificationHandler {
	
	private static Logger logger = Logger.getLogger(RulesNotificationHandler.class.getName());
	
	public List<NotificationTask> getNotifications(LogEvent event) {
		List<NotificationTask> notifications = new ArrayList<NotificationTask>();
		try {
			if (event.isSilentMode() || !(event instanceof WorkNoteUpdateEvent))
				return notifications;
			
			
			WorkNoteUpdateEvent ev = (WorkNoteUpdateEvent) event;

			Billboard billboard = getBillboard(ev);
			
			if (billboard==null || !billboard.isSendNotification()) 
				return notifications;
			
			
			Set<User> receivers = new HashSet<User>();
			receivers.addAll(getUsers(billboard.getReceivers()));
			receivers.addAll(getRolesUsers(billboard.getRoleReceivers()));
			
			for (User receiver : receivers) {
				notifications.add(getNotification(event, receiver, true, false));
			}
		} 
		catch (Exception e) {
			logger.error(e);
		}

		return notifications;
	}
	
	@Override
	protected void execute(NotificationTask notification) {
		
		LogEvent event = notification.getEvent();
		
		if (!(event instanceof WorkNoteUpdateEvent)) {
			return;
		}
		
		User receiver = notification.getReceiver();
		
		if (!isEnabled(receiver)) 
			return;
		
		Billboard billboard = getBillboard((WorkNoteUpdateEvent) event);
		
		ContentFactoryService factory = ServiceLocator.getService(ContentFactoryService.class);
		
		if (billboard.isBillboard()) {
			factory.createWorkNoteNotification(billboard, receiver);
		}
		
		else if (billboard.isAlert()) {
			factory.createWorkNoteNotification(billboard, receiver);
		}
	}	
	
	private Billboard  getBillboard(WorkNoteUpdateEvent event) {
		Billboard billboard = null;
		try {
			billboard = (Billboard) getContentDao().findObjectById(new ObjectId(event.getObjectId()));
		} 
		catch (ContentMgmtException e1) {
			logger.error(e1);
		}
		return billboard;
	}
	
	private Set<User> getRolesUsers(List<Role> roles) {
		Set<User> users = new HashSet<User>();
		for (Role role : roles) {
			users.addAll(getUsers(role));
		}
		return users;
	}
	
	private Set<User> getUsers(Role role) {
		return getMembers(((KbeeAbstractRole)role).getGroup());
	}
}