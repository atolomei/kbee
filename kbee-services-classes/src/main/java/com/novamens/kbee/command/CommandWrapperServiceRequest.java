package com.novamens.kbee.command;


import java.io.Serializable;
import java.util.Map;

import org.springframework.util.Assert;

import com.novamens.content.command.Command;
import com.novamens.content.command.CommandState;
import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

/**
 * <p>{@link ServiceRequest} that encapsulates a {@link Command} and sends it through the {@link Scheduler}.
 * The Command must be already registered in the {@link CommandService}
 * </p>
 * 
 * <p>
 * ServiceRequests are {@link Serializable} (Commands are not), when the CommandRequest is de-serialized
 * by the Scheduler, it recovers the Command by asking the CommandService with the {code commandId}.
 * </p>
 * 
 * <p>
 * <b>CommandService and Commands are not persistent</b>. If the application is restarted, 
 * CommandRequest from the Scheduler queue will not be able to find their Command.
 * </p>
 *
 */
public class CommandWrapperServiceRequest extends AbstractServiceRequest  {
	
	private static final long serialVersionUID = 4284631937037817264L;
	
    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(CommandWrapperServiceRequest.class.getName());

	private Serializable commandId;

	boolean executed = false;
	private boolean exactly_one_semantics = false;
	private boolean isStopped = false;

	
	public CommandWrapperServiceRequest(Map<String, String> map) {
		setParameters(map);
		
		Assert.isTrue(map!=null, "Map is null");
		Assert.isTrue(map.get("command")!=null, "Command is null");
		
		
		// boolean exactly_one_semantics = map.get("exactlyonesemantics")!=null && (map.get("exactlyonesemantics").equals("true") || map.get("exactlyonesemantics").equals("yes"));
		
		String command_class_name = (String) map.get("command");
		
		/**
		Class<?> javaclass;
		try {
			javaclass = Class.forName(command_class_name);
		} catch (ClassNotFoundException e) {

			throw new KbeeRuntimee;
		}
		
		Object command = javaclass.newInstance();
		

		if (command instanceof Command)
				initial( (Command) command , exactly_one_semantics);
				*/
		
	}
	
	
	/**
	 * @param command
	 */
	public CommandWrapperServiceRequest(Command command) {
		this(command, command.isExactlyOneSemantics());
	}
		
	public CommandWrapperServiceRequest(Command command,  boolean exactly_one_semantics) {
		initial(command, exactly_one_semantics);
	}
	
	public CommandWrapperServiceRequest(Command command, Map<String, Object> map, boolean exactly_one_semantics) {
		command.setParameters(map);
		initial(command , exactly_one_semantics);
	}
	
	
	/**
	 * 
	 * 
	 * @param command
	 * @param exactly_one_semantics
	 */
	private void initial(Command command, boolean exactly_one_semantics) {
		
		Assert.isTrue(command!=null, "Command is null");
		this.commandId = command.getId();
		
		if (!ServiceLocator.getService(CommandService.class).contains(this.commandId)) {
			logger.warn("Command is not registered in CommandService " + String.valueOf(this.commandId));
			ServiceLocator.getService(CommandService.class).register(command);
		}
		
		logger.debug("CommandRequest:  id -> " + command.getId().toString() + "  | description ->" + command.getDescription());
		
		setName("CommandRequest - " + command.getName() + ". " + command.getId().toString());
		setExactlyOneSemantics(exactly_one_semantics);
		setPriority(SchedulerService.LOW_PRIORITY);
		setCost(SchedulerService.STANDARD_PROCESSING_COST);
		setDescription(command.getDescription());
		setExactlyOneSemantics(command.isExactlyOneSemantics());
		
		
	}
	public boolean isExactlyOneSemantics() {
		return this.exactly_one_semantics;
	}
	
	public void setExactlyOneSemantics(boolean b) {
		this.exactly_one_semantics=b;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(getName()+  " | ");
		str.append(super.toString());
		return str.toString();	
	}
	
	@Override
	public void stop() {
		logger.debug("Stopping Request");
		CommandService service = ServiceLocator.getService(CommandService.class);
		Command command = null;
		if (commandId!=null) {
			command = service.getCommand((Long)commandId);
			if (command!=null) {
				command.stop();
			}
		}
		isStopped = true;
	}
	
	@Override
	public void execute() {

		Command command = null;
		boolean sucess = false;
		
		try  {
			
			logger.debug("executing CommandRequest ");
			
			if (this.isExactlyOneSemantics()) {
				logger.debug("this.isExactlyOneSemantics() true ");
				
				if (isExecuted()) {
					logger.debug("is executed true ");
					sucess = true;
					logger.error("-----------------------------------------------------------------------------");
					logger.error(getName()+ " | id:" + getId().toString() +". request already executed         ");
					logger.error("-----------------------------------------------------------------------------");
					return;
				}
				else {
					logger.debug("add Request to tken ");
					ServiceLocator.getService(SchedulerService.class).addRequestToken(getId());
				}
			}
			
			logger.debug("before service getcommand " + commandId);
			
			CommandService service = ServiceLocator.getService(CommandService.class);
			command = service.getCommand((Long)commandId);
			
			if (command!=null) {
				command.setState(CommandState.RUNNING);
				/**
				 	Si el Comando es un thread, el Request termina enseguida, y el thread queda trabajando.
				 	Sino el Request no termina hasta que el command complete su ejecucion.
				 */
				
				command.execute();
				
				if (!isStopped()) 
					sucess=true;
				else {
					command.setState(CommandState.CANCELED);
					command.setResultDetails("Request was stopped.");
				}
			}
			else {
				logger.error("Command {} does not exist in the Service", this.commandId.toString());
			}
		} 
		
		catch (KbeeRuntimeException e) {
			logger.error(e);
			throw(e);
		}
		
		catch (Exception e) {
			logger.error(e);
			if (command!=null) {
				command.setState(CommandState.ERROR);
				command.setResultDetails(e.getClass().getName() + ": " + e.getMessage());
				CommandService service = ServiceLocator.getService(CommandService.class);
				service.executed(command);
			}
			throw(e);
		}
		catch (NoSuchMethodError e) {
			logger.error(e);
			if (command!=null) {
				command.setState(CommandState.ERROR);
				command.setResultDetails(e.getMessage()!=null?e.getMessage().toString():"");
				CommandService service = ServiceLocator.getService(CommandService.class);
				service.executed(command);
			}
		}
		finally {
			
			try {	
				this.executed=true;
				if (!sucess && exactly_one_semantics)
					ServiceLocator.getService(SchedulerService.class).removeRequestToken(getId());
			} 
			catch (Exception e) {
				logger.error(e);
			}
			
			if (command!=null) {
				ServiceLocator.getService(CommandService.class).executed(command);
				if (command instanceof Runnable) 
					logger.debug("Command launched");	
				else 
					logger.debug("done. ");
			}
		}
	}
	
	protected boolean isStopped() {
		return isStopped;
	}
	
	protected boolean isExecuted() {
		if (this.executed)
			return true;
		if (!exactly_one_semantics)
			return false;
		try {
			return ServiceLocator.getService(SchedulerService.class).containsRequestToken(getId());
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}
}