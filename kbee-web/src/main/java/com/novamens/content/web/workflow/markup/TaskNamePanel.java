package com.novamens.content.web.workflow.markup;

import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.workflow.WorkflowContext;

/**
 * 
 * Task Name
 * Version 
 * Priority
 *
 */
@SuppressWarnings("serial")
public class TaskNamePanel extends ModelPanel<WorkflowContext> {
	private static final long serialVersionUID = 1L;

	static Logger logger = LogManager.getLogger(TaskNamePanel.class.getName());
	
	public TaskNamePanel(IModel<WorkflowContext> model) {
		super("task-name", model);
		
		add(new Label("task-name", new Model<String>() {
			public String getObject() {
				return parse(TaskNamePanel.this.getModelObject().getTask().getName());
			}
		}));
					
		add(new Label("version", new Model<String>() {
			public String getObject() {
				if (TaskNamePanel.this.getModelObject() instanceof KbeeContext) 
					return String.valueOf(((KbeeContext) TaskNamePanel.this.getModelObject()).getContent().getVersion());
				return "-";
			}
		})); 
		
		WebMarkupContainer con = new WebMarkupContainer("task-priority-tag"); 
		con.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				return "tag " + TaskNamePanel.this.getModelObject().getPriority().getCss();
			}
		}));
		add(con);
		
		con.add(new Label("task-priority", new Model<String>() {
			public String getObject() {
				try {
					Locale locale = ( (KbeeContext) TaskNamePanel.this.getModel().getObject()).getUser().getLocale();
					
					if (TaskNamePanel.this.getModelObject().getPriority()!=null)
						return TaskNamePanel.this.getModelObject().getPriority().getLabel(locale);
					
					return "null";
					
				} catch (Exception e) {
					logger.error(e.getStackTrace());
				}
				return "";
				
				
				
				
				
				
				
			}
		}));
	}

	
	protected String parse(String name) {
		if (name==null)
			return "";
		if (name.length()>20)
			return name.substring(0,18)+"..";
		return name;
	}
}
