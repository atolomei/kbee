package com.novamens.kbee.content.notification;

import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;
import com.novamens.content.notification.NotificationService;
import com.novamens.content.notification.NotificationTask;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.ContentService;
import com.novamens.content.user.UserProfile;
import com.novamens.email.EmailService;
import com.novamens.event.LogEvent;
import com.novamens.logging.CheckinEvent;
import com.novamens.logging.ContentEvent;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;

import kbee.email.EmailBuilderPublishEventENotiRule;
import kbee.util.logging.Logger;

public class PublishNotificationHandler extends AbstractLogEventNotificationHandler  {

	private static Logger logger = Logger.getLogger(EmailBuilderPublishEventENotiRule.class.getName());
	
	@Override
	protected void execute(NotificationTask notification) {
		
		LogEvent event = notification.getEvent();
		
		if (!(event instanceof CheckinEvent)) {
			return;
		}
		
		User user = notification.getReceiver();
		
		if (!isEnabled(user))
			return;
		
		Content content = (Content)((ContentEvent)event).getContent();
		
		if (content==null) 
			return;
			
		UserProfile profile =  getContentDao().findUserProfileByUser(user);

		if (profile==null || !profile.isEmailRuleNotifications())
			return;
			
		/** Check that the principal has READ permission on the Content If you can't read it then no notification is sent.
		The validity of the document is also checked. if it is not valid, it is not sent. A document with a valid specification 
		is considered current */
		if (!ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(content, user) || !content.getService(ContentService.class).isValid()) 
			return;
				
		try {
			/** publisher */
			Person publisher = getContentDao().findUserProfileByUser(((CheckinEvent)event).getEventUser()).getPerson();

			/** subscriber */
			Person subscriber = getContentDao().findUserProfileByUser(user).getPerson();
						
			if (subscriber==null) 
				return;
			
			logger.debug("Publish_" + event.getId().toString() + " - to: " + (subscriber!=null?subscriber.getDisplayName():"null"));

			if (notification.isAlert()) {
				ServiceLocator.getService(NotificationService.class).sendPublishNotification(content, user);
			}
			
			if (notification.isEmail()) {
				logger.debug(" | content -> " + content.getDisplayName() + " |  publisher -> " +publisher.getDisplayName() + " | receiver -> " + subscriber.getDisplayName());
				EmailBuilderPublishEventENotiRule builder =  new EmailBuilderPublishEventENotiRule(content, publisher, subscriber);
				ServiceLocator.getService(EmailService.class).send(builder);
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
	}
}
