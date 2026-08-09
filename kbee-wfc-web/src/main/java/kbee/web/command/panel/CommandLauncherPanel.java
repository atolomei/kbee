package kbee.web.command.panel;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.novamens.content.command.Command;
import com.novamens.wicket.markup.html.form.Form;

public class CommandLauncherPanel extends CommandAbstractPanel {

	public CommandLauncherPanel(String id, IModel<Command> model) {
		super(id, model);

	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	
		add(new Label("title", getModel().getObject().getName()));
		add(new Label("description", getModel().getObject().getDescription()));
		
		// form
		// start
		
		// Historial de Parametros del Comando
		
		
		
		
		
		
		
		
		
	}
	
	private void addForm() {
		
		Form<?> form = new Form<Void>("form");
								
		/**
		 * form.add(new ChoiceField<Bean>("command", new PropertyModel<Bean>(this, "command"), new PropertyModel<List<Bean>>(this, "commands")) {
		
			@Override
			protected String getDisplayValue(Bean value) {
				return value.getDisplayName();
			}
			@Override
			protected String getIdValue(Bean value) {
				return value.getName();
			}
		});
		
		form.add(new TextAreaField<String>("parameters", new PropertyModel<String>(this, "parameters")));
		
		
		add(form);
		form.add(new AjaxSubmitLink("start-button", form) {
			protected void onSubmit(AjaxRequestTarget target) {
				// logger.debug("Sending "+ getCommand().getName());
				startCommand();
				target.add(CommandBeanPanel.this);
			}
		});
		*/
	}
	

}
