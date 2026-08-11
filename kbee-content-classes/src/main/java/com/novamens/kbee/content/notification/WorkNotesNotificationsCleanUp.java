package com.novamens.kbee.content.notification;

import java.time.OffsetDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.notification.NotificationService;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

/**
 * com.novamens.kbee.content.notification.WorkNotesNotificationsCleanUp
 *
 */
public class WorkNotesNotificationsCleanUp extends AsyncCommand {
			
	
	static private Logger logger = LogManager.getLogger(WorkNotesNotificationsCleanUp.class.getName());
				
	public WorkNotesNotificationsCleanUp() {
		setName(this.getClass().getName());
	}
	
	@Override
	protected void executeAsync() {
		try {
			com.novamens.hibernate.session.Session.open();
			ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
			String months = getContentDao().findSystemParameterValueByKey( "work-notes-notification-retention-months", "1");
			Long mn;
			try {
				mn = Long.valueOf(months);
			} catch (Exception e) {
				mn = Long.valueOf(1);
			}

			OffsetDateTime date = OffsetDateTime.now().minusMonths(mn.longValue());
			ServiceLocator.getService(NotificationService.class).deleteWorkNoteNotification(date);
			ServiceLocator.getService(NotificationService.class).evict();
			
		} catch (Exception e) {
			
			logger.error(e.getClass().getName());
			
		} finally {
			com.novamens.hibernate.session.Session.close();	
			setStatusInfo("DB Session closed.");
		}
	}

	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

}
