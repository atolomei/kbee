package com.novamens.kbee.command;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import com.novamens.content.command.Command;
import com.novamens.content.command.CommandState;
import com.novamens.service.SystemService;

public interface CommandService extends SystemService {
	
	// metric all_command: all commands 
	// ------------------
	//
	// for commands that must be sent to the scheduler
	// 
	public void add(Command command);

	
	// metric: async_command
	// ------
	//execute the command using a Thread Pool dispatcher
	//public void execute(Command runnable);
	public void run(Command command);
	public void registerAndRun(Command command);
	
	
	// register is for commands that do not need to be sent to the scheduler
	//
	public void register(Command command);
	
	
	// metric: command_executed
	// ------
	public void executed(Command command);
	
	public void remove(Serializable id);

	public double getProgress(Serializable id);
	public Command getCommand(Serializable id);
	
	public Map<Serializable, Command> getCommands();
	public List<Command> getCommands(CommandState state);

	void stopAll();
	
	public List<Command> getCommandsAsList();
	public List<Command> getCommandsAsList(Serializable domain_id);
	
	public int getTotalCommands();
	public int getTotalTerminatedCommands();
	public OffsetDateTime getDateLastCleanUp();

	public boolean contains(Serializable commandId);


	
	
	
	
	
	
}