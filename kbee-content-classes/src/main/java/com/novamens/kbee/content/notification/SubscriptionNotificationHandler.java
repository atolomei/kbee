package com.novamens.kbee.content.notification;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.notification.NotificationTask;
import com.novamens.content.service.ContentSubscriptionService;
import com.novamens.content.subscription.ContentSubscription;
import com.novamens.event.LogEvent;
import com.novamens.logging.ContentEvent;
import com.novamens.security.User;

import kbee.util.logging.Logger;

public class SubscriptionNotificationHandler extends AbstractLogEventNotificationHandler {
	
	private static Logger logger = Logger.getLogger(SubscriptionNotificationHandler.class.getName());
	
	public List<NotificationTask> getNotifications(LogEvent event) {
		List<NotificationTask> notifications = new ArrayList<NotificationTask>();
		try {
			if (event.isSilentMode() || !(event instanceof ContentEvent))
				return notifications;
			
			Content content = (Content) ((ContentEvent)event).getContent();
			
			if (content!=null) {
				
				List<ContentSubscription> list = content.getService(ContentSubscriptionService.class).getSubscriptions();
				
				if (list!=null) {
					for (ContentSubscription subscription : list) {
						User user = getUser(subscription.getPerson());
						if (user!=null) {
							notifications.add(getNotification(event, user, true, true));
						}
					}
				}
				else {
					logger.warn(" content.getService(ContentSubscriptionService.class).getSubscriptions() -> is null");
				}
			} 
		}
	
		catch (Exception e) {
			logger.error(e);
		}

		return notifications;
	}
}