package com.novamens.kbee.content.notification;

import java.io.Serializable;
import java.time.OffsetDateTime;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ObjectId;
import com.novamens.content.notification.NotificationService;
import com.novamens.content.notification.NotificationType;
import com.novamens.dao.SecurityDao;
import com.novamens.kbee.content.workflow.KbeeWorkflowActivity;
import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.ActivityProgressNote;

import kbee.util.logging.Logger;

public class TaskProgressNoteNotificationSender extends AbstractServiceRequest {
	private static final long serialVersionUID = 1L;
	
	private ObjectId noteId;
	private Serializable receiverId;
	
	private static Logger logger = Logger.getLogger(TaskProgressNoteNotificationSender.class.getName());

	public TaskProgressNoteNotificationSender(ActivityProgressNote note, User receiver) {
		setNote(note);
		setReceiver(receiver);
		setExecuteAfter(OffsetDateTime.now().plusMinutes(5));
	}
	
	@Override
	public void execute() {
		try {
			ActivityProgressNote note = getNote();
			User receiver = getReceiver();
			if (note!=null && receiver!=null) {
				Content content = ((KbeeWorkflowActivity)note.getActivity()).getContent(); 
				ServiceLocator.getService(NotificationService.class).sendNotification(NotificationType.PROGRESS_NOTE, content, note.getText(), receiver);
			}
		} 
		catch (Exception e) {
			logger.error(e);
			throw(e);
		}
	}

	public void setNote(ActivityProgressNote note) {
		noteId = new ObjectId(note);
	}
	
	public void setReceiver(User receiver) {
		receiverId = receiver.getId();
	}
	
	public ActivityProgressNote getNote() {
		ActivityProgressNote note = null;
		try {
			note = (ActivityProgressNote)getContentDao().findObjectById(noteId);
		} 
		catch (ContentMgmtException e) {
			logger.error(e);
		}	
		return note;
	}
	
	public User getReceiver() {
		User receiver = null;
		try {
			receiver = (User)getSecurityDao().findUserById((Long)receiverId);
		} 
		catch (ContentMgmtException e) {
			logger.error(e);
		}	
		return receiver;
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected SecurityDao getSecurityDao() {
		return (SecurityDao)ServiceLocator.getService(BeansService.class).getBean("securityDao");
	}
}