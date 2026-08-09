package kbee.web.command;


import com.novamens.content.command.CommandState;

import kbee.web.command.panel.CommandAbstractPanel;
import kbee.web.command.panel.CommandExecutingStatusPanelV5;
import kbee.web.command.panel.CommandResultsPanelV5;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import com.novamens.content.command.Command;
 

			
public class CommandStatusPanelV5 extends CommandAbstractPanel {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(CommandStatusPanelV5.class.getName());

	private AbstractAjaxTimerBehavior timer;
	private IModel<String> css;
	
	
	public CommandStatusPanelV5(String id, IModel<Command> command_model) {
		super(id, command_model);
		setOutputMarkupId(true);
	}
	
	@Override
	public void onDetach() {
 		super.onDetach();
 		
 		if (getModel()!=null)
 			getModel().detach();
 		
 	}
	
	public void setCss(IModel<String> css) {
		this.css=css;
	}
	
	public IModel<String> getCss() {
		return css;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
			
		add(new AttributeModifier("class", getCss()));
		
		
        WebMarkupContainer stc= new WebMarkupContainer("container");
        stc.setOutputMarkupId(true);
        add(stc);

		
        // STATUS
        //
        stc.add(new CommandExecutingStatusPanelV5("executing_status", getModel()) {
			private static final long serialVersionUID = 1L;
				protected void onStop(AjaxRequestTarget target) {
        			CommandStatusPanelV5.this.timer.stop(target);
        			onAfterExecution(target);
        		}
        	
        });

        
        // RESULTS
        //

        stc.add(new CommandResultsPanelV5("results", getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
        	public boolean isVisible() {
				return true;
       	}
        });

		this.timer = new AbstractAjaxTimerBehavior(java.time.Duration.ofMillis(750)) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void onTimer(AjaxRequestTarget target) {
				try {
					if (getModel().getObject().isTerminated() || getModel().getObject().getState() == CommandState.CANCELED) {
						this.stop(target);
						onAfterExecution(target);
					}
					target.add(CommandStatusPanelV5.this.get("container"));

				}  catch (Exception e) {
					logger.error(e);
				}
			}
		};
		
		add(timer);
	}

	public void onAfterExecution(AjaxRequestTarget target) {
		target.add(CommandStatusPanelV5.this);
	}

}
