package com.novamens.kbee.content.notification;

import com.novamens.kbee.content.command.ContentPublishNotificationsCleanUpCommand;
import com.novamens.scheduler.AbstractCronJobRequest;

public class ContentPublishNotificationsCleanUpServiceRequest extends AbstractCronJobRequest {
			
	private static final long serialVersionUID = 1L;

	/**
 	   /kbee-idoc/src/main/webapp/WEB-INF/META-INF/kbee/spring/content-datamanagement-context.xml
 		<bean id="clean-up-work-notes-notifications" class="com.novamens.kbee.content.notification.WorkNotesNotificationsCleanUpServiceRequest">
			<property name="cronExpression" value=  "0 15 4 ? * SUN"/>
		</bean>
	   see {@link WorkNotesNotificationsCleanUp}
 	 */
	
 	public ContentPublishNotificationsCleanUpServiceRequest() {
			setName("Content Publish Notifications CleanUp");
			setDescription("Deletes Content Publish Notifications sent X months ago. By default X= 3 (content-publish-notification-retention-months)");
	}

 	/**
 	 * @see com.novamens.scheduler.ServiceRequest#execute()
 	 *  Async
 	 */
	@Override
	public void execute() {
		 ContentPublishNotificationsCleanUpCommand command = new  ContentPublishNotificationsCleanUpCommand();
		command.execute();		
	}
}
