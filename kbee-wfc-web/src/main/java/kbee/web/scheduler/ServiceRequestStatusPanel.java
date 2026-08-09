package kbee.web.scheduler;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.command.CommandState;
import com.novamens.datetime.DateTimeService;
import com.novamens.scheduler.ServiceRequest;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.KBPanel;

import kbee.web.command.CommandStatusPanelV5;
import kbee.web.command.panel.CommandAttributePanelV5;
import kbee.web.command.panel.CommandResultsPanelV5;

public class ServiceRequestStatusPanel extends KBPanel {
		
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ServiceRequestStatusPanel.class.getName());

	
	private static final long serialVersionUID = 1L;
	
	ServiceRequest request;  // they are Serializable
	
	public ServiceRequestStatusPanel(String id, ServiceRequest request) {
		super(id);
		this.request=request;
	}
	
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		add(new StatusFragment("executing_status"));
		add(new ResultsFragment("results"));
	}

	/**
	 * 
	 *
	 */
	private class StatusFragment extends Fragment {
		
		private AbstractAjaxTimerBehavior timer;

		
		private static final long serialVersionUID = 1L;
		
		public StatusFragment(String id) {
			super(id, "executing-status-fragment", ServiceRequestStatusPanel.this);
			setOutputMarkupId(true);
		}
		
		public void onInitialize() {
			super.onInitialize();
			
			
			
			timer = new AbstractAjaxTimerBehavior(java.time.Duration.ofMillis(750)) {
				private static final long serialVersionUID = 1L;
				@Override
				protected void onTimer(AjaxRequestTarget target) {
					try {
						//if (getModel().getObject().isTerminated() || getModel().getObject().getState() == CommandState.CANCELED) {
						//	this.stop(target);
						//	onAfterExecution(target);
						//}
						//target.add(CommandStatusPanelV5.this.get("container"));

					}  catch (Exception e) {
						logger.error(e);
					}
				}
			};
			
			add(timer);
			
		}
		
		public void onAfterExecution(AjaxRequestTarget target) {
			target.add( ServiceRequestStatusPanel.this);
		}
		
	}

	/**
	 * 
	 *
	 */
	private class ResultsFragment extends Fragment {
		private static final long serialVersionUID = 1L;
		
		private List<Panel> panels;
		
		public ResultsFragment(String id) {
			super(id, "results-fragment", ServiceRequestStatusPanel.this);
			setOutputMarkupId(true);
		}
		
		public void onInitialize() {
			super.onInitialize();
			
	        add(new Label("title", "Results"));

	        add(new ListView<Panel>("result", getPanels()) {

	            private static final long serialVersionUID = 1L;

	            protected void populateItem(ListItem<Panel> item) {
	                item.setOutputMarkupId(true);
	                item.add(item.getModelObject());
	                item.setVisible(item.getModelObject().isVisible());
	            }
	        });

			
		}
		
		public List<Panel> getPanels() {

	        if (this.panels != null)
	            return this.panels;

	        this.panels = new ArrayList<Panel>();


	        Model<String> dt = new Model<String>() {
	            public String getObject() {
	                //if (CommandResultsPanelV5.this.getModel().getObject().isTerminated())
	                 //   return ServiceLocator.getService(DateTimeService.class).timeElapsed(CommandResultsPanelV5.this.getModel().getObject().getDateTerminated());
	                //else
	                 //   return "n/a";
	            	return "";
	            }
	        };

	        this.panels.add(new CommandAttributePanelV5("command_item", new Model<String>("Terminated"), dt));

	        Model<String> dm = new Model<String>() {
	            public String getObject() {
	                DateTimeService service = ServiceLocator.getService(DateTimeService.class);
	                return "";
	                //return service.formatLapseSeconds(getModel().getObject().getDuration(), getSessionUser().getLocale(), "ago");
	            }
	        };

	        
	        //this.panels.add(new CommandAttributePanelV5("command_item", new Model<String>("Results"), () -> CommandResultsPanelV5.this.getModel().getObject().getResult()));
	        //this.panels.add(new CommandAttributePanelV5("command_item", new Model<String>("Comments"), () -> CommandResultsPanelV5.this.getModel().getObject().getResultComment()));
	        
	        return this.panels;
	    }

	}

	
	
	
	
	
	
	
	public ServiceRequest getServiceRequest() {
		return this.request;
	}
	
	
	
	
	
}
