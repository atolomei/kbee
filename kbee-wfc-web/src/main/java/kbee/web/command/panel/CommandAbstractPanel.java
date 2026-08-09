package kbee.web.command.panel;


import org.apache.wicket.model.IModel;

import com.novamens.content.command.Command;
import com.novamens.wicket.markup.html.panel.KBPanel;

public class CommandAbstractPanel extends KBPanel implements CommandPanel {
	
	private static final long serialVersionUID = 1L;
	
	IModel<Command> command_model;
	
	public CommandAbstractPanel(String id, IModel<Command> command_model) {
		super(id);
		setModel(command_model);
	}
	
	public IModel<Command> getModel() {
		return command_model;
	}
	
	public void setModel(IModel<Command> command_model ) {
		this.command_model=command_model;
	}
	
	
	public void onDetach() {
		super.onDetach();
		if (this.command_model!=null)
			this.command_model.detach();
	}
}



