package com.novamens.kbee.content.command;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.scheduler.AbstractCronJobRequest;
			
public class RecycleBinCleanUpServiceRequest extends AbstractCronJobRequest {

	private static final long serialVersionUID = 1L;

 	static Logger logger = LogManager.getLogger(RecycleBinCleanUpServiceRequest.class.getClass().getName());

 	/**
 	 * 
 	 *  /kbee-idoc/src/main/webapp/WEB-INF/META-INF/kbee/spring/content-datamanagement-context.xml
 	 *   
 	 * 	<bean id="clean-up-recycle-bin" class="com.novamens.kbee.content.command.RecycleBinCleanUpServiceRequest">
	 *		<property name="cronExpression" value="0 46 4 * * *"/>
	 *	</bean>
 	 * 
 	 */
 	public RecycleBinCleanUpServiceRequest() {
			setName("Clean up old files from Recyle Bin ");
			setDescription("Deletes Contents from the Recycle Bin that are older than System Property: recycle-bin-retention-months (12 months).");
	}

 	/**
 	 * @see com.novamens.scheduler.ServiceRequest#execute()
 	 * 
 	 */
	@Override
	public void execute() {
		RecycleBinCleanUpCommand command = new RecycleBinCleanUpCommand();
		command.execute();		
	}
}
