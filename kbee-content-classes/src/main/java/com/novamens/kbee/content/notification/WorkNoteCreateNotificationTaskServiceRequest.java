package com.novamens.kbee.content.notification;


import java.util.List;
import java.util.Set;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.model.ObjectId;
import com.novamens.content.notes.Billboard;
import com.novamens.content.notification.NotificationService;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;

import com.novamens.event.LogEvent;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.logging.WorkNoteUpdateEvent;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;

import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

/**
 *  {@link EventNotifier} calls {@link NotificationService}, who adds a this ServiceRequest to the Scheduler
 */				
@Deprecated
public class WorkNoteCreateNotificationTaskServiceRequest extends NotificationTaskServiceRequest {
		
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(WorkNoteCreateNotificationTaskServiceRequest.class.getName());
																								
	private static final long serialVersionUID = 1L;

	private  SecurityService sec;
	
	
	public WorkNoteCreateNotificationTaskServiceRequest(LogEvent event) {
		super(event);
		super.setName("Create Notification for Alert " + event!=null ?event.getDisplayName():"");
	}
	
	
	@Override
	protected void notify(LogEvent event) {
		
		if (event.isSilentMode())
			return;

		
		if (! (event instanceof WorkNoteUpdateEvent)) {
			logger.error("Event incorrect class " + event.getClass().getName());
			return;
		}
		
		WorkNoteUpdateEvent ev = (WorkNoteUpdateEvent) event;
		Billboard billboard;
		
		if (ev.isSilent())
			return;
		
		try {
				billboard = (Billboard) getContentDao().findObjectById(new ObjectId(ev.getObjectId()));
				
		} catch (ContentMgmtException e1) {
				logger.error(e1);
				return;
		}
		
		if (billboard==null) 
			return;
		
		if (!billboard.isSendNotification())
			return;
		
		Domain domain = getContentDao().findDomainById(ev.getDomainId());
		
		if (domain==null) 
			return;
		
		List<com.novamens.security.Principal> list = billboard.getReceivers();
		
		for (Principal pr: list) {
			 if 		(pr instanceof User)		sendUser(billboard, (com.novamens.security.User) pr);
			 else if 	(pr instanceof KbeeGroup) 	sendGroup(billboard, (Group) pr);
		}
		
		// this is necessary because of the on delete cascade in the Database
		//
		ServiceLocator.getService(NotificationService.class).evict();
		
	}


	/**
	 * @param note
	 * @param user
	 */
	private void sendUser(Billboard note, com.novamens.security.User user) {
		try {
			
			if (!user.isEnabled())
				return;
			
			if (((KbeeUser) user).getState()!=ObjectState.ENABLED)
				return;

			
			if (note.isBillboard()) {
				ContentFactoryService factory = ServiceLocator.getService(ContentFactoryService.class);
				factory.createWorkNoteNotification(note, user);
			}
			
			else if (note.isAlert()) {
				ContentFactoryService factory = ServiceLocator.getService(ContentFactoryService.class);
				factory.createWorkNoteNotification(note, user);
			}
		
			// --
			// Billboards do not support email because by the moment because they are "timed"
			// they can be created for a future time
			// unless we send the email right away ?
			// --
			
			//if (note.isEmail()) 
			//	ServiceLocator.getService(EmailService.class).sendBillboard(note, user);
			
			
		} catch (ContentCreationException e) {
				logger.error(e);
						
		} catch (ContentMgmtException e) {
			logger.error(e);
		}
	}
	
	private void sendGroup(Billboard note, Group group) {
		Set<Principal> mem= ((KbeeGroup) group).getMembers();
		for (Principal p: mem) {
			if (p instanceof KbeeUser && ((KbeeUser) p).getState()==ObjectState.ENABLED)
				sendUser(note, (com.novamens.security.User) p);
			else if (p instanceof KbeeGroup) {
				if (!((KbeeGroup) p).getId().equals(group.getId()))
					sendGroup(note, (Group) p);
			}
		}

	}
	
	public SecurityService getSecurityService() {
		if (this.sec==null) 
			sec=ServiceLocator.getService(com.novamens.service.SecurityService.class);			
		return sec;
	}

	@Override
	protected void sendNotification(User user, Content content, ENotiRule rule, LogEvent event) {
		logger.error("should never be here !!");
	}
	
}
