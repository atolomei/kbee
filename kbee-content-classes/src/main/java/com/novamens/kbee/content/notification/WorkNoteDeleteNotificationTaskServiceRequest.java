package com.novamens.kbee.content.notification;

import org.apache.logging.log4j.LogManager;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.model.ObjectId;
import com.novamens.content.notes.Billboard;
import com.novamens.dao.SecurityDao;
import com.novamens.dom.Domain;
import com.novamens.event.LogEvent;
import com.novamens.logging.WorkNoteDeleteEvent;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@Deprecated
public class WorkNoteDeleteNotificationTaskServiceRequest extends NotificationTaskServiceRequest {
			
	private static final long serialVersionUID = 1L;

	static protected org.apache.logging.log4j.Logger logger = LogManager.getLogger(WorkNoteCreateNotificationTaskServiceRequest.class.getName());

	SecurityDao securityDao;
	SecurityService sec;
	
	Domain domain;
			
	public WorkNoteDeleteNotificationTaskServiceRequest(LogEvent event) {
		super(event);
		String title;
		if (! (event instanceof WorkNoteDeleteEvent)) {
			logger.error("Event incorrect class " + event.getClass().getName());
			title="Event incorrect class " + event.getClass().getName();
			return;
		}
		else
			title = "Delete existing Notifications for Work Note " +((WorkNoteDeleteEvent) event).getTitle();
		super.setName(title);
	}
	
	
	
	@Override
	protected void notify(LogEvent event) {
		
		if (event.isSilentMode())
			return;

		
		if (! (event instanceof WorkNoteDeleteEvent)) {
			logger.error("Event incorrect class " + event.getClass().getName());
			return;
		}
		
		WorkNoteDeleteEvent ev = (WorkNoteDeleteEvent) event;
		
		Billboard note;
		
		try {
		
			note = (Billboard) getContentDao().findObjectById(new ObjectId(ev.getObjectId()));
			
		} catch (ContentMgmtException e1) {
			logger.error(e1);
			return;
		}
		
		if (note==null) 
			return;
		
		Domain domain = getContentDao().findDomainById(ev.getDomainId());
		
		if (domain==null) 
			return;
	}



	public SecurityService getSecurityService() {
		if (this.sec==null) 
			sec=ServiceLocator.getService(com.novamens.service.SecurityService.class);			
		return sec;
	}



	@Override
	protected void sendNotification(User user, Content content, ENotiRule rule, LogEvent event) {
		// TODO Auto-generated method stub
		
	}

}
