package com.novamens.kbee.content.notification;


import com.novamens.scheduler.AbstractCronJobRequest;

public class WorkNotesNotificationsCleanUpServiceRequest extends AbstractCronJobRequest {
			
	private static final long serialVersionUID = 1L;

	/**
 	   /kbee-idoc/src/main/webapp/WEB-INF/META-INF/kbee/spring/content-datamanagement-context.xml
 	    
		<bean id="clean-up-work-notes-notifications" class="com.novamens.kbee.content.notification.WorkNotesNotificationsCleanUpServiceRequest">
			<property name="cronExpression" value=  "0 15 4 ? * SUN"/>
		</bean>
	 
	   see {@link WorkNotesNotificationsCleanUp}
 	  
 	 */
 	public WorkNotesNotificationsCleanUpServiceRequest() {
			setName("WorkNotes Notifications CleanUp");
			setDescription("Deletes Work Note Notifications sent X months ago. By default X= 1 (work-notes-notification-retention-months)");
	}

 	/**
 	 * @see com.novamens.scheduler.ServiceRequest#execute()
 	 *  Async
 	 */
	@Override
	public void execute() {
		WorkNotesNotificationsCleanUp command = new  WorkNotesNotificationsCleanUp();
		command.execute();		
	}

}
