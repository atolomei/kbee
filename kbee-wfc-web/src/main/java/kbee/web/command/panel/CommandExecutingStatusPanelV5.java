package kbee.web.command.panel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.command.Command;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.util.NumberFormatter;

@SuppressWarnings("serial")
public class CommandExecutingStatusPanelV5 extends CommandAbstractPanel {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(CommandExecutingStatusPanelV5.class.getName());

	private List<Panel> panels;
	
	public CommandExecutingStatusPanelV5(String id, IModel<Command> command_model) {
		super(id, command_model);
		setOutputMarkupId(true);
	}
	
	
	@Override 
	public void onInitialize() {
		super.onInitialize();
		
		add(new Label("title", "Status"));
						
		WebMarkupContainer actions = new WebMarkupContainer("actions") {
			@Override
			public boolean isVisible() {
				return CommandExecutingStatusPanelV5.this.getModel().getObject().getState()==CommandState.RUNNING;
			}
		};
		add(actions);
		
		actions.add(new WorkingIndicatorAjaxLinkV5<Void>("stop", getLabelString("stop")) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					CommandExecutingStatusPanelV5.this.getModel().getObject().stop();
					target.add(CommandExecutingStatusPanelV5.this.getParent());
					onStop(target);
					
				}  
				catch (Exception e) {
					logger.error(e);
				}
			}
			@Override
			protected String getLabel() {
				return getLabelString("stop");
			}
			@Override
			protected String getWorkingLabel() {
				return getLabelString("working");
			}
			@Override
			public boolean isVisible() {
				return CommandExecutingStatusPanelV5.this.getModel().getObject().getState()==CommandState.RUNNING;
			}
		});
		
		
		add(new ListView<Panel>("result",  getPanels()) {
			protected void populateItem(ListItem<Panel> item){

				item.setOutputMarkupId(true);
				item.add(item.getModelObject());
				item.setVisible(item.getModelObject().isVisible());
			}
		});	
	}
	

	protected void onStop(AjaxRequestTarget target) {
		
	}


	/**
	 * 
	 * 
	 * @return
	 */
	@SuppressWarnings("serial")
	public List<Panel> getPanels() {
		
		if (this.panels!=null)
			return this.panels;
		
		this.panels = new ArrayList<Panel>();
		
		this.panels.add(new CommandAttributePanelV5("command_item", new Model<String>("Name"), new Model<String>(CommandExecutingStatusPanelV5.this.getModel().getObject().getName())));
		this.panels.add(new CommandAttributePanelV5("command_item", new Model<String>("Description"), new Model<String>(CommandExecutingStatusPanelV5.this.getModel().getObject().getDescription())));

																				
		
		
		
		Model<String> dst=new Model<String>() {
			public String getObject() {
				return ServiceLocator.getService(DateTimeService.class).timeElapsed(CommandExecutingStatusPanelV5.this.getModel().getObject().getDateStarted());
			}
		};
		
		
		this.panels.add(new CommandAttributePanelV5("command_item", new Model<String>("Started"), dst));

		Model<String> st=new Model<String>() {
			public String getObject() {
					return	"<span class=\""+CommandExecutingStatusPanelV5.this.getModel().getObject().getState().getCss()+"\">" +
							CommandExecutingStatusPanelV5.this.getModel().getObject().getState().getLabel() + "</span>";
			}		
		}; 
		this.panels.add(new CommandAttributePanelV5("command_item", new Model<String>("Status"), st));


		Model<String> st_a=new Model<String>() {
			public String getObject() {
					return	CommandExecutingStatusPanelV5.this.getModel().getObject().getStatusInfo();
			}		
		}; 
		
		this.panels.add(new CommandAttributePanelV5("command_item", new Model<String>("Status Activity"),st_a));
		
		this.panels.add(new CommandAttributePanelV5("command_item", new Model<String>("Threads"), new Model<String>(String.valueOf(CommandExecutingStatusPanelV5.this.getModel().getObject().getThreads()))));
		
		Map<String, Object> map = CommandExecutingStatusPanelV5.this.getModel().getObject().getParameters();
		for(Entry<String, Object> entry: map.entrySet()) {
			String lab = entry.getKey().toString();
			String val = entry.getValue()!=null?entry.getValue().toString():"null";
			this.panels.add(new CommandAttributePanelV5("command_item", new Model<String>("Parameter -> "+lab), 
					new Model<String>(val)));
		};
		
		Model<String> pm=new Model<String>() {
			public String getObject() {
				String s= NumberFormatter.formatNumber(CommandExecutingStatusPanelV5.this.getModel().getObject().getProgress());
				return s.trim()+" %";
			}
		};
		this.panels.add(new CommandAttributePanelV5("command_item", new Model<String>("Progress"),pm)); 
		
		Model<String> ec=new Model<String>() {
			public String getObject() {
				return getEstimatedTimeComplete();
			}
		};
		this.panels.add(new CommandAttributePanelV5("command_item", new Model<String>("Time to complete"), ec));
		
		Model<String> s_done=new Model<String>() {
			public String getObject() {
				String s_done  = NumberFormatter.formatNumber(CommandExecutingStatusPanelV5.this.getModel().getObject().getTotalItemsProcessed());
				return s_done;
			}
		};
		
		Model<String> s_total=new Model<String>() {
			public String getObject() {
				String s_total = NumberFormatter.formatNumber(CommandExecutingStatusPanelV5.this.getModel().getObject().getTotalItems());
				return s_total;
			}
		};
		
		this.panels.add(new CommandAttributePanelV5("command_item", new Model<String>("Items Total"), s_total));
		this.panels.add(new CommandAttributePanelV5("command_item", new Model<String>("Items Done or Processing"), s_done));
		
		
		return this.panels;
	}

	
	@Override
	public void onDetach() {
		super.onDetach();
		
		if (getModel()!=null)
 			getModel().detach();

		if (this.panels!=null)
			for (Panel panel: this.panels)
				panel.detach();
	}



	/**
	 * @return
	 */
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}	
	
	protected String getEstimatedTimeComplete() {
		Command cmd = getModel().getObject();
		Double value =  Double.valueOf(cmd.estimatedSecsToEnd() * 1000.0);
			Long lv = value.longValue();
			if (lv<0)
				return "-";
			
		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		return service.formatLapseSeconds(lv, getSessionUser().getLocale(), "ago");
		
	}


}
