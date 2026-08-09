package kbee.web.command.panel;


import java.time.Duration;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.service.ServiceLocator;

@SuppressWarnings("serial")
public class AsyncExecutorPanel extends Panel {
			
	private static final long serialVersionUID = 1L;
																							
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AsyncExecutorPanel.class.getName());
	
	private AsyncCommand command;
	
	public AsyncExecutorPanel(String id) {
		super(id);
		
		add(new Label("confirmation", getConfirmationMessage()) {
			public boolean isVisible() {
				return command==null;
			}
		});
		
		add(new Label("execution", getExecutionMessage()) {
			public boolean isVisible() {
				return isRunning();
			}
		});
		
		WebMarkupContainer progressbar = new WebMarkupContainer("progressbar") {
			public boolean isVisible() {
				return isRunning();
			}
		};
		progressbar.setOutputMarkupId(true);
		progressbar.add(new AttributeModifier("style", new Model<String>() {
			public String getObject() {
				return isLaunched() ? "width:"+String.valueOf(getProgress())+"%;" : "width:0%;";
			}
		}));
		progressbar.add(new Label("progresslabel", new Model<String>() {
			public String getObject() {
				return isLaunched() ? String.valueOf(getProgress())+"%" : "0%";
			}
		}));
		add(progressbar);
		
		IModel<String> timeleftmodel = new Model<String>() {
			public String getObject() {
				StringResourceModel model = new StringResourceModel("timeleft", AsyncExecutorPanel.this);
				model.setParameters(estimatedSecsToEnd());
				return model.getObject();
			}
		};
		
		add(new Label("timeleft", timeleftmodel) {
			public boolean isVisible() {
				return isRunning() && estimatedSecsToEnd()>0;
			}
		});
		
		add(new Label("report", getReportMessage()) {
			public boolean isVisible() {
				return isTerminated();
			}
		});
		
		add(new AbstractAjaxTimerBehavior(Duration.ofSeconds(1)) {
			@Override
			protected void onTimer(AjaxRequestTarget target) {
				target.add(AsyncExecutorPanel.this);
				onUpdate(target);
				if (isTerminated()) 
					stop(target);
			}
		});
	}
	
	public boolean isLaunched() {
		return command!=null;
	}
	
	public boolean isRunning() {
		return getCommand()!=null && !getCommand().isTerminated();
	}
	
	public boolean isTerminated() {
		return getCommand()!=null && getCommand().isTerminated();
	}
	
	public double estimatedSecsToEnd() {
		return getCommand()!=null ? getCommand().estimatedSecsToEnd() : 0;
	}
	
	public int getProgress() {
		return  command!=null ? (int)ServiceLocator.getService(CommandService.class).getProgress((Long)command.getId()) : 0;
	}
	
	public void execute(AjaxRequestTarget target) {
		setCommand(newCommand());
		getBehaviors(AbstractAjaxTimerBehavior.class).get(0).restart(target);
	}
	
	public void cancel(AjaxRequestTarget target) {
		if (getCommand()!=null) getCommand().stop();
	}
	
	public String getReportMessage() {
		if (getCommand()==null) return "";
		return getCommand().getResultDetails();
	}
	
	protected AsyncCommand getCommand() {
		return command!=null ? (AsyncCommand)ServiceLocator.getService(CommandService.class).getCommand((Long)command.getId()) : null;
	}
	
	protected void setCommand(AsyncCommand command) {
		this.command = command;
		ServiceLocator.getService(CommandService.class).add(command);
	}
	
	protected AsyncCommand newCommand() {
		return null;
	}
	
	protected void onUpdate(AjaxRequestTarget target) {
		
	}
	
	protected IModel<String> getConfirmationMessage() {
		return new Model<String>("confirm");
	}
	
	protected IModel<String> getExecutionMessage() {
		return new Model<String>("executing...");
	}
}