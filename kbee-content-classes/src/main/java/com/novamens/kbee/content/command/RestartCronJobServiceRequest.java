package com.novamens.kbee.content.command;

import com.novamens.scheduler.AbstractCronJobRequest;

/**
 * 
 * insert into  kb_cronjob (id, lastmodifieduser, name, description, cronexpression, clazz) 
 * values ((select nextval('objectid_sequence')), (select id from users where username='root@kbee'), 
 * 'RestartCronJobRequest', 'Restart the application at 3:35 on the 5th day of every month', 
 *    '0          35       3          5         *       ? ', 'com.novamens.kbee.content.command.RestartCronJobRequest');
 *    <second> <minute> <hour> <day-of-month> <month> <day-of-week> <year> 
 *
 *
 *
 *
 *
 *
 */
public class RestartCronJobServiceRequest extends AbstractCronJobRequest {
	private static final long serialVersionUID = 1L;

	public RestartCronJobServiceRequest() {
		setName("Restart Application");
		setDescription("Restart Application (works only on Linux)");
		
	}
	
	@Override
	public void execute() {
		RestartCommand command = new RestartCommand();
		command.setExactlyOneSemantics(true);
		command.execute();		
	}
	
	@Override
	public int getPriority() {
		return 0;
	}
}

/**

  insert into  kb_cronjob (id, lastmodifieduser, name, description, cronexpression, clazz) values ((select nextval('objectid_sequence')), (select id from users where username='root@kbee'), 'RestartCronJobRequest', 'Restart the application at 3:35 on the 12th day of every month',   '0 35 3 12 * ? ', 'com.novamens.kbee.content.command.RestartCronJobRequest');
  
 */
