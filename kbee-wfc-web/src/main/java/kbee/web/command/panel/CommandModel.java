package kbee.web.command.panel;

import java.io.Serializable;

import org.apache.wicket.model.IModel;

import com.novamens.content.command.Command;
import com.novamens.kbee.command.CommandService;
import com.novamens.service.ServiceLocator;

public class CommandModel implements IModel<Command> {

	private static final long serialVersionUID = 1L;

	private Serializable command_id;
	
	private Command command = null;

	
	public CommandModel(Command command) {
		setObject(command);
	}
	
	@Override
	public Command getObject() {
		
		if (command!=null)
			return command;
		
		command=ServiceLocator.getService(CommandService.class).getCommand(command_id);
		return command;
	}
	
	@Override
	public void detach() {
		this.command=null;
	}
	
	
	@Override
	public void setObject(Command command) {
		this.command_id=command.getId();
		this.command=command;
	}

}
