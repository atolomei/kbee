package com.novamens.kbee.content.webapi.type;

import com.novamens.content.command.Command;

import kbee.api.model.ICommand;

public class ICommandAdapter implements Adapter<Command, ICommand> {
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	public ICommandAdapter() {
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	public ICommand adapt(Command command) {
		ICommand icommand = new ICommand();
		icommand.setId(String.valueOf(command.getId()));
		icommand.setState(command.getState().name());
		icommand.setStartTime((new IDateAdapter()).adapt(command.getDateStarted()));
		if (command.getDateTerminated()!=null)
		icommand.setEndTime((new IDateAdapter()).adapt(command.getDateTerminated()));
		icommand.setDisplayName(command.getName());
		icommand.setProgress(command.getProgress());
		return icommand;
	}
}