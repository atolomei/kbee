package com.novamens.kbee.command;

import java.util.HashMap;
import java.util.Map;

import com.novamens.beans.BeansService;
import com.novamens.content.command.Command;


import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;


/**
 * 
 * 
 * <p>
 * <changeSet author="atolomei" id="BatchMonitorIndexClean'">
		<sql><![CDATA[
			
			delete from kb_cronjob where clazz='BatchMonitorIndexClean';
			
			insert into  kb_cronjob (id, lastmodifieduser, name, description, cronexpression, clazz, parameter) 
			values ((select nextval('objectid_sequence')), 
			(select id from users where username='root@kbee'), 
			'Clean Monitor Indexes once a day',  
			'BatchMonitorIndexClean',
			'15 15 4 * * *', 
			'com.novamens.kbee.command.CommandExecutionJob', 
			'command=BatchMonitorIndexClean');

		]]>
		</sql>
	</changeSet>
	</p>
 * 
 *
 */
public class CommandExecutionJob extends AbstractCronJobRequest {
			
	private static final long serialVersionUID = 1L;
													
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger("Scheduler");
	private static kbee.util.logging.Logger blogger = kbee.util.logging.Logger.getLogger(CommandExecutionJob.class.getName());
										
	
	
	public CommandExecutionJob() {
		setName(this.getClass().getName());
		setDescription(this.getClass().getName());
	}

	@Override
	public void execute() {
		try {

			Command command = null;
			
			 blogger.debug(getParameters()!=null? getParameters(): "paramaters is null");
			
			
			if (getParameters()==null)
				throw new KbeeRuntimeException("Parameters is null");
			
			
			String commandbean = getParameters().get("command");
			
			
			/**
			 *  KbeeRulesCommand
			 */
			
			if (commandbean != null) {
				command = (Command)ServiceLocator.getService(BeansService.class).getBean(commandbean);
			}
			
			if (command != null) {
				
				command.setParameters(getCommandParameters());
								
				logger.debug("Running Command: " + command.getClass().getName() + " -> " + command.getName());
				
				ServiceLocator.getService(CommandService.class).register(command);
				ServiceLocator.getService(CommandService.class).run(command);
				
			}
			else {
				logger.error("Job command "+ commandbean + " not found");
			}
			
		}
		catch (Exception e) {
			blogger.error(e);
			logger.error(e);
		}
	}
	
	public String toString() {
		return super.toString() + " | " + (getParameters()!=null? getParameters().toString() : "null"); 
	}
	
	private Map<String, Object> getCommandParameters() {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		
		if (getParameters()!=null) {
			for (String parameter : getParameters().keySet()) {
				parameters.put(parameter, getParameters().get(parameter));
			}
		}
			
		logger.debug(this.getClass().getSimpleName() +" : " + parameters.toString());
		blogger.debug(this.getClass().getSimpleName() +" : " + parameters.toString());
		
		return parameters;
	}
}
