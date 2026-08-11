package kbee.web.model.procedure;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.workflow.EndCondition;
import com.novamens.content.workflow.WorkflowDao;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.content.workflow.TimeOutEndCondition;
import com.novamens.kbee.content.workflow.UserTask;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.page.PageMainTabs;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Task;

import kbee.util.logging.Logger;

@SuppressWarnings("serial")
public class TaskMainPanel extends ObjectEditor<Task>  implements PageMainTabs {
	private static final long serialVersionUID = 1L;
			
	static private Logger logger = Logger.getLogger(TaskMainPanel.class.getName());

	//private IModel<Procedure> proc_model;
	
	public TaskMainPanel(IModel<Task> model) {
		super("editor", model);
		setOutputMarkupId(true);
		//this.proc_model = proc_model;
	}
	
	@Override
	public void setEditionEnabled(boolean editionEnabled) {
		super.setEditionEnabled(editionEnabled);
		if (editionEnabled) {
			@SuppressWarnings("unchecked")
			VerticalLayout<ITab> editor = (VerticalLayout<ITab>)get("tabs");
			if (editor!=null)
				editor.setSelectedTab(0);		
		}
	}
	
	public void onCancel(AjaxRequestTarget target) {
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		add(getTabs());
	}
	
	protected VerticalLayout<ITab> getTabs() {
		
		List<ITab> tabs = new ArrayList<>();
		
		tabs.add(new AbstractTabKB(getLabel("editor.task"), "task") {
			@Override
			public Panel getPanel(String panelId) {
				return new TaskEditor(panelId, getModel()) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
					}
					@Override
					public void onSelect(AjaxRequestTarget target, IModel<EndCondition> conditionmodel) {
						TaskMainPanel.this.addOrReplace(getTabs());
						final int starting=4;
						EndCondition condition = conditionmodel.getObject();
						@SuppressWarnings("unchecked")
						VerticalLayout<ITab> tabs = (VerticalLayout<ITab>)TaskMainPanel.this.get("tabs");
						for (int index=starting; index<tabs.getTabs().size(); index++) {
							List<EndCondition> conditions = ((UserTask)getTask()).getEndConditions();
							if (condition.getEvent().equals(conditions.get(index-starting).getEvent())) {
								tabs.setSelectedTab(index);
								target.add(tabs);
								break;
							}
						}
					}
					@Override
					public void onCreateCondition(AjaxRequestTarget target) {
						addEndCondition();
					}
					@Override
					public void onDelete(AjaxRequestTarget target, EndCondition condition) {
						deleteEndCondition(condition);
						TaskMainPanel.this.addOrReplace(getTabs());
						target.add(TaskMainPanel.this);
					}
				};
			}
		});
		
		tabs.add(new AbstractTab(getLabel("editor.forms")) {
			@Override
			public Panel getPanel(String panelId) {
				return new TaskFormsEditor(panelId, getModel());
			}
		});
		
		tabs.add(new AbstractTab(getLabel("editor.alerts")) {
			@Override
			public Panel getPanel(String panelId) {
				return new TaskAlertsEditor(panelId, getModel());
			}
		});
		
		tabs.add(new AbstractTabKB(getLabel("contingency"), "contingency") {
			@Override
			public Panel getPanel(String panelId) {
				return new TaskBackupEditor(panelId, getModel());
			}
		});
		
		Task task = getModelObject(); 
		
		if (task instanceof UserTask) {
			for (EndCondition condition : ((UserTask)task).getEndConditions()) {
				if (condition instanceof ManualEndCondition) {
					IModel<ManualEndCondition> conditionmodel = new EndConditionModel<ManualEndCondition>(getModel(), (ManualEndCondition)condition);
					tabs.add(new AbstractTabKB(new Model<String>(condition.getLabel() + " <span class=\"ago\">" + getLabel("action-router").getObject() +  "</span>"), (condition.getLabel()!=null?condition.getLabel().toLowerCase().trim():"")) {
						@Override
						public Panel getPanel(String panelId) {
							return new ManualConditionEditor(panelId, getModel(), conditionmodel);
						}
					});
				}
				else {
					if (condition instanceof TimeOutEndCondition) {
						IModel<TimeOutEndCondition> conditionmodel = new EndConditionModel<TimeOutEndCondition>(getModel(), (TimeOutEndCondition)condition);
						tabs.add(new AbstractTabKB(new Model<String>(condition.getLabel() +  " <span class=\"ago\">" + getLabel("timeout").getObject() +  "</span>"), "timeoutaction") {
							@Override
							public Panel getPanel(String panelId) {
								return new TimeoutConditionEditor(panelId, getModel(), conditionmodel);
							}
						});
					}
				}
			}
		}

		
		
		/**
		// Alerts
		// Related  
		// Routing
		 */
		tabs.add(new AbstractTabKB(getLabel("editor.related-queries"), "queries") {
			@Override
			public Panel getPanel(String panelId) {
				return new TaskRelatedQueriesEditor(panelId, getModel()) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
					}
				};
			}
		});
		

		
		
		VerticalLayout<ITab> editor = new VerticalLayout<ITab>("tabs", this.getClass().getName(),tabs, VerticalLayout.VERTICAL) {
			@Override
			protected Component newTitle(final String titleId, final IModel<?> titleModel, final int index, String css) {
				Label label = new Label(titleId, titleModel);
				label.setEscapeModelStrings(false);
				if (css!=null)
					label.add( new AttributeModifier("class", css));
				return label;
			}
		};
		
		editor.setTitle(new StringResourceModel("sections", this, null));
		
		return editor;
	}
	
	protected void onClose(AjaxRequestTarget target) {
	}
	
	private void addEndCondition() {
		int i = 0;
		String event = "new event";
		String label = "new condition";
		WebTask task = (WebTask)getModelObject();
		for (EndCondition endcondition : task.getEndConditions()) {
			if (event.equals(endcondition.getEvent())) i++;
		}
		event = i>0 ? event + " " + String.valueOf(i) : event;
		label = i>0 ? label + " " + String.valueOf(i) : label;
		ManualEndCondition condition = new ManualEndCondition(label, event);
		List<EndCondition> endconditions = task.getEndConditions();
		endconditions.add(condition);
		update(endconditions);
	}
	
	private void deleteEndCondition(EndCondition conditiontodelete) {
		WebTask task = (WebTask)getModelObject();
		List<EndCondition> endconditions = task.getEndConditions();
		for (EndCondition condition : endconditions) {
			if (condition.getEvent().equals(conditiontodelete.getEvent())) {
				endconditions.remove(condition);
				break;
			}
		}
		update(endconditions);
	}
	
	private void update(List<EndCondition> endconditions) {
		WebTask task = (WebTask)getModelObject();
		task.setEndConditions(endconditions);
		Procedure procedure = task.getProcedure();
		
		procedure.setTasks(procedure.getTasks());
		procedure.getMaster().setSubprocedures(procedure.getMaster().getSubprocedures());
		
		getWorkflowDao().update(procedure.getMaster());
	}
	
	private WorkflowDao getWorkflowDao() {
		return (WorkflowDao)ServiceLocator.getService(BeansService.class).getBean("WorkflowDao");
	}
	
	private String initial_tab;
	
	@Override
	@SuppressWarnings("unchecked")
	public void setInitialTab(String a) {
		try {
			initial_tab=a;
			((VerticalLayout<ITab>) get("tabs")).setSelectedTab(a);
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}

	@Override
	public String getInitialTab() {
		return initial_tab;
	}
}