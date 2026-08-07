package com.novamens.kbee.command;

import java.io.Serializable;

import com.novamens.content.command.Command;
import com.novamens.content.command.CommandState;
import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

public class SynchronousCommandRequest extends AbstractServiceRequest implements Serializable {

	private static final long serialVersionUID = 4284631937037817264L;
											
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SynchronousCommandRequest.class.getName()); 
	
	private Serializable commandId;
	
	private boolean executed = false;
	private boolean exactly_one_semantics = false;
	
	
	public SynchronousCommandRequest(Command command) {
		this(command, true);
	}
	
	
	public SynchronousCommandRequest(Command command, boolean exactly_one_semantics) {
		
		if (command==null) {
			logger.error("Command is null");
			throw new KbeeRuntimeException("Command is null");
		}
		
		this.commandId=command.getId();
		
		setExactlyOneSemantics(exactly_one_semantics);
		setName("CommandRequest - " + command.getName() + ". " + command.getId().toString());
		setPriority(SchedulerService.LOW_PRIORITY);
		setCost(SchedulerService.STANDARD_PROCESSING_COST);
		setDescription(command.getDescription());
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
	public void execute() {
		
		Command command = null;
		
		boolean sucess = false;
		
		try  {
			
			if (this.exactly_one_semantics) { 
				if (isExecuted()) {
					sucess = true;
					logger.error("--------------------------------------------------------------------------------");
					logger.error(getName() + " | id:" + getId().toString() +" | (Command: " + (this.commandId!=null?this.commandId.toString():"") +" | request already executed");
					logger.error("--------------------------------------------------------------------------------");
					return;
				}
				else 
					ServiceLocator.getService(SchedulerService.class).addRequestToken(getId());
			}
			
			CommandService service = ServiceLocator.getService(CommandService.class);
			command = service.getCommand((Long)commandId);
			
			if (command!=null) {
				command.setState(CommandState.RUNNING);
				/**
				 	If the Command is a thread, the Request ends immediately, and the thread remains working.
					Otherwise the Request does not finish until the command completes its execution.
				*/
				command.execute();
				sucess=true;
			}
			else {
				
			}
		} 
		catch (Exception e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + e.getMessage() + " | " + (command!=null?command.getName():""));
			if (logger.isDebugEnabled()) {
				logger.error(e);
			}
			if (command!=null) {
				command.setState(CommandState.ERROR);
				command.setResultDetails(e.getClass().getName());
				CommandService service = ServiceLocator.getService(CommandService.class);
				service.executed(command);
			}
		}
		catch (NoSuchMethodError e) {
			if (e.getMessage()!=null)
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName()+ " | " + e.getMessage() + (command!=null?command.getName():""));
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
				
			} catch (Exception e) {
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " |  finally block.");
			}
			
			if (command!=null) {
				if (command instanceof Runnable) {
					logger.debug("Command Thread launched: " + command.getDisplayName() + " (" + command.getClass().getName() +")");	
				}
				else {
					logger.debug("Command done: " + command.getDisplayName()+ " (" + command.getClass().getName() +")");
				}
			}
		}
	}
	
	protected boolean isExecuted() {
		if (this.executed)
			return true;
		if (!exactly_one_semantics)
			return false;
		return ServiceLocator.getService(SchedulerService.class).containsRequestToken(getId());
	}
}
