package com.novamens.kbee.command;

import java.util.HashMap;
import java.util.Map;

 
import com.novamens.content.command.Command;
import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.service.ServiceLocator;

import kbee.util.logging.Logger;


/**
 * Executes the Command once.
 * Similar to {@link CommandExecutionJob}
 * 
 *  isbean:  true | false (default: false)
 *  
 *  command: bean or classname
 *
 */
public class RequestCommandByClassNameServiceRequest extends AbstractServiceRequest {

	private static final long serialVersionUID = 1L;
	
	
	private static Logger logger = Logger.getLogger("Scheduler");
	
	String classname;
	String command_par;
	
	public RequestCommandByClassNameServiceRequest( String classname, String command_par) {
		setName(this.getClass().getSimpleName()+ " -> " + classname);
		this.classname = classname;
		this.command_par=command_par;
	}
	
	protected void executeClassName() {
		try {

			
			Command command = null;
			
			String command_class = this.classname;
			
			/**
			 * 
			 */
			
			if (command_class != null) {
				
				Class<?> javaclass = Class.forName(command_class.trim());
				
				Object instance = javaclass.newInstance();
				
				if (instance != null && instance instanceof Command) {
					command = (Command) instance;
				}
				else {
					logger.error("Error parameter 'Command' " + command_class + "  | Class:" + (instance!=null?instance.toString():"null)"));
				}
			}
			
			if (command != null) {
				command.setParameters(getCommandParameters());
				
				logger.debug("Running Command: " + command.getClass().getName() + " -> " + command.getName());
				
				if (command instanceof Runnable) {
					ServiceLocator.getService(CommandService.class).register(command);
					ServiceLocator.getService(CommandService.class).run(command);
				}
				else {
					ServiceLocator.getService(CommandService.class).add(command);
				}
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
	}

	
	
	/**
	 * 
	 * 
	
	protected void executeBean() {
		try {

			Command command = null;
			
			String commandbean = getParameters().get("command");
			
			
			if (commandbean != null) {
				command = (Command)ServiceLocator.getService(BeansService.class).getBean(commandbean);
			}
			
			if (command != null) {
				command.setParameters(getCommandParameters());
								
				logger.debug("Running Command " + command.getClass().getName() + " -> " + command.getName());
				
				ServiceLocator.getService(CommandService.class).register(command);
				ServiceLocator.getService(CommandService.class).run(command);
				
			}
			else {
				logger.error("Job command "+ commandbean + " not found");
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
		
	}
	 */
	public void execute() {
			executeClassName();
	}
	
	

	private Map<String, Object> getCommandParameters() {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		return parameters;
		
		//for (String parameter : getParameters().keySet()) {
		//	parameters.put(parameter, getParameters().get(parameter));
		//}
		//logger.debug(parameters.toString());
		//return parameters;
	}

	

}
