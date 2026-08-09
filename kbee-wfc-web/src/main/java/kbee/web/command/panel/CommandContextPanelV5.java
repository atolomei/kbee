package kbee.web.command.panel;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.novamens.content.command.Command;


public class CommandContextPanelV5 extends CommandAbstractPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CommandContextPanelV5(IModel<Command> command_model) {
		super("context", command_model);
	}
	
	
	public void onInitialize() {
		super.onInitialize();
		add(new Label("title", "Status"));
	}

}
