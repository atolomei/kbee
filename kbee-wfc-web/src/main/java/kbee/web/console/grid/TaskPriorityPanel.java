package kbee.web.console.grid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Priority;
 
public class TaskPriorityPanel extends Panel {
				
	private static final long serialVersionUID = 1L;

	private String err;
	private Priority priority;
	
	static private Logger logger = LogManager.getLogger(TaskPriorityPanel.class.getName());
	
	public TaskPriorityPanel(String id, Priority object) {
		super(id);
		this.priority=object;
	}
	
	public void onInitialize() {
		super.onInitialize();

	try {
		
		if (this.priority==null)  {
			WebMarkupContainer tag = new WebMarkupContainer("tag");
			add(tag);
			tag.add( new AttributeModifier("class", "tag"));
			Label xlabel = new Label("label", this.err!=null?this.err:"");
			tag.add(xlabel);
		}
		else {
			String label 	= this.priority.getLabel( getSessionUser().getLocale());
			String css 		= "tag " + this.priority.getCss();
	
			WebMarkupContainer tag = new WebMarkupContainer("tag");
			tag.add( new AttributeModifier("class", css));
			add(tag);
			
			Label xlabel = new Label("label", label);
			xlabel.add( new AttributeModifier("class", "label"));
			tag.add(xlabel);
		}
	} catch (Exception e) {
			logger.error(e.getClass().getName());
			WebMarkupContainer tag = new WebMarkupContainer("tag");
			addOrReplace(tag);
			tag.add( new Label("label", e.getClass().getSimpleName()));
	}
 }

	
	public void setError(String error) {
		this.err=error;
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
}
}
