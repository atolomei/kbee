package com.novamens.content.web.admin.markup.datamanagement;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.command.Command;
import com.novamens.content.web.command.batch.markup.BatchCommandStatusPanel;
import com.novamens.kbee.command.CommandService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.StaticField;
import com.novamens.wicket.markup.html.form.TextAreaField;

@SuppressWarnings("serial")
public class CommandExecutionPanel extends Panel {
	private static final long serialVersionUID = 1L;
	
	static private Logger logger = LogManager.getLogger(CommandBeanPanel.class.getName());

	public class CommandModel implements IModel<Command> {
		private Map<String, Object> parameters;
		private Class<? extends Command> commandclass;
		private Command command;
		public CommandModel(Command command) {
			setObject(command);
		}
		public void setObject(Command command) {
			this.parameters = command.getParameters();
			this.command = command;
			this.commandclass = command.getClass();
		}
		public Command getObject() {
			if (command==null) {
				try {
					command = commandclass.newInstance();
					command.setParameters(parameters);
				}
				catch (IllegalAccessException | InstantiationException e) {
					throw new RuntimeException(e);
				}
			}
			return command;
		}
		public void detach() {
			this.command = null;
		}
	}
	
	private IModel<Command> model;

	public CommandExecutionPanel(Command command) {
		super("executor");
		setCommand(command);
		setOutputMarkupId(true);
	}
	
	public void setCommand(Command command) {
		this.model = new CommandModel(command);
	}
	
	public Command getCommand() {
		return this.model.getObject();
	}
	
	public IModel<Command> getModel() {
		return this.model;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addForm();
		addStatus();
	}
	
	@Override
	public void onDetach(){
		super.onDetach();
		getModel().detach();
	}
	
	private void addForm() {
		Form<?> form = new Form<Void>("form");
		
		form.add(new StaticField<String>("command", new Model<String>(getCommand().getDisplayName())));
		
		form.add(new TextAreaField<String>("parameters", new Model<String>(getParameters())) {
			@Override
			public boolean isEnabled() {
				return false;
			}
		});
		
		add(form);
		form.add(new AjaxSubmitLink("start-button", form) {
			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				logger.debug("Sending "+ getCommand().getName());
				startCommand();
				target.add(CommandExecutionPanel.this);
			}
		});
	}
	
	private void startCommand() {
		
		CommandService service = ServiceLocator.getService(CommandService.class);

		Command command = null;
		try {
			
			command = getCommand();
			
			if (command!=null) {
				service.add(command);
				updateStatus((Long) command.getId());
			}	
		} 
		catch (Exception e) {
			logger.error("starting commnad", e);
		}
	}
	
	private String getParameters() {
		return getCommand().getParameters().toString();
	}
	
	private void addStatus() {
		add (new Panel("status") {
			public boolean isVisible() {
				return false;
			}
		});
	}
	
	private void updateStatus( Long cid) {
		replace(new BatchCommandStatusPanel("status", cid) {
			@Override
			public void onAfterExecution(AjaxRequestTarget target) {
				target.add(CommandExecutionPanel.this);
			}
		});
	}
}
