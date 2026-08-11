package kbee.web.content.workflow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.workflow.EndCondition;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.KbeeTakeTaskEvent;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;

import kbee.web.event.wicket.EditorEvent;
import kbee.web.model.procedure.EndConditionModel;
import kbee.web.model.procedure.TaskModel;
import kbee.web.panel.AlertPanel;

@SuppressWarnings("serial")
public class TaskActionsPanel<T extends Content> extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	private IModel<WorkflowContext> model;
	private String activecondition;
	private List<IModel<ManualEndCondition>> conditions = null;
	private IModel<Task> taskmodel;
	private Editor<T> editor;

	private boolean helpVisible = true;
	
	public TaskActionsPanel(String id, IModel<WorkflowContext> model) {
		super(id);
		setOutputMarkupId(true);
		setWorkflowModel(model);
	}

	public void setWorkflowModel(IModel<WorkflowContext> model) {
		this.model = model;
		WorkflowContext context = model.getObject();
		taskmodel = new TaskModel(new ObjectModel<Procedure>(context.getProcedure().getMaster()), context.getTask());
	}

	public IModel<WorkflowContext> getWorkflowModel() {
		return model;
	}
	
	public IModel<Task> getTaskModel() {
		return taskmodel;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setAlert(null);
		
		helpVisible = getPreference("show-help", "no").equals("yes");
		
		
		AjaxLink<Void> show_help = new AjaxLink<Void>("show-help") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				helpVisible  = !helpVisible;
				setPreference("show-help", helpVisible ?  "yes" : "no");
				target.add(TaskActionsPanel.this);
			}
		};
		show_help.add(
				
				new Label("help", new Model<String>() {
			public String getObject() {
				return getLabelString(helpVisible ?  "hide-help" : "show-help"); 
			}
		}).setEscapeModelStrings(false));
		add(show_help);

		if (isPending() && isTakeable()) {
			add(new InvisiblePanel("conditions-container") );
			setAlert(getLabel("task-is-pending"));
			addTakeAction();
			return;
		}
		
		add(new InvisiblePanel("take-panel") );
		
		if (getWorkflowModel().getObject().getCurrentActivity()!=null				&&
			getWorkflowModel().getObject().getCurrentActivity().getUser() !=null 	&&
			getSessionUser().getId().equals(getWorkflowModel().getObject().getCurrentActivity().getUser().getId())) {
			
			addConditions();
			
		}
		else {
			setAlert(getLabel("warning-owner", getWorkflowModel().getObject().getCurrentActivity().getUser().getFirstLastName()));
			add(new InvisiblePanel("conditions-container") );
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		for (IModel<ManualEndCondition> model : getEndConditions()) {
			model.detach();
		}
		taskmodel.detach();
		if (model!=null) {
			model.detach();
		}	
	}

	public List<IModel<ManualEndCondition>> getEndConditions() {
		if (conditions==null) {
			conditions = new ArrayList<IModel<ManualEndCondition>>();
			for (EndCondition condition : getTask().getEndConditions()) {
				if (condition instanceof ManualEndCondition) {
					conditions.add(new EndConditionModel<ManualEndCondition>(getTaskModel(), (ManualEndCondition)condition));
				}
			};
		}
		return conditions;
	}

	public boolean isHelpVisible() {
		return helpVisible;
	}

	public void setHelpVisible(boolean helpVisible) {
		this.helpVisible = helpVisible;
	}
	
	protected void setAlert(IModel<String> model) {
		
		
		/**WebMarkupContainer w = new WebMarkupContainer("alert-container");
		w.add( (new Label("text", model!=null?model.getObject():"")).setEscapeModelStrings(false));
		w.setVisible(model!=null);
		addOrReplace(w);
		**/
		
		AlertPanel<Void> pa=new AlertPanel<Void>("pending-text",AlertPanel.INFO,  null, 
				null, 
				model);
		pa.setIcon(AlertPanel.HELP_INFO);
		pa.setVisible(model!=null);
		addOrReplace(pa);
		

		
		
	}
	
	protected boolean isEnabled(ManualEndCondition condition) {
		if (!condition.isEnabled()) {
			return false;
		}
		if ((condition.getPerms()==null || "".equals(condition.getPerms())) &&
				(condition.getCondition()==null || "".equals(condition.getCondition()))) {
			return true;
		}	
		T content = getEditor().getModelObject();
		getEditor().update(content);
		return condition.isEnabled(content);
	}
	
	protected void onAction(AjaxRequestTarget target, ManualEndCondition action) {
	}
	
	protected Editor<T> getEditor() {
		if (this.editor==null) {
			this.editor = getEditor(getPage().iterator());
		}
		return this.editor;
	}

	@SuppressWarnings("unchecked")
	protected Editor<T> getEditor(Iterator<Component> components) {
		while (components.hasNext()) {
			Component component = components.next();
			if (component instanceof Editor<?>) {
				return (Editor<T>)component;
			}
			else {
				if (component instanceof WebMarkupContainer) {
					Editor<T> editor = getEditor(((WebMarkupContainer)component).iterator());
					if (editor!=null) {
						return editor;
					}
				}
			}
		}
		return null;
	}

	@Override
	protected void addListeners() {
		super.addListeners();
		add(new WicketEventListener<EditorEvent>() {
			public void onEvent(EditorEvent event) {
				if (get("conditions")!=null && event.getElement()!=null) {
					event.getRequestTarget().add(get("conditions"));
				}
				if (event.getKey()!=null) {
					if (get("conditions")!=null) {
						activecondition = null;
						event.getRequestTarget().add(get("conditions"));
					}
				}
			}
		});
	}
	
	private Activity getRunningActivity() {
		List<Activity> activities = getWorkflowModel().getObject().getProcess().getActivities();
		Activity activity = !activities.isEmpty() && activities.get(0).isRunning() ? activities.get(0) : null;
		return activity;
	}

	private WebTask getTask() {
		return ((WebTask)getWorkflowModel().getObject().getTask());
	}
	
	private void addConditions() {
		WebMarkupContainer c = new WebMarkupContainer("conditions-container");
		add(c);
		c.addOrReplace(new ListView<IModel<ManualEndCondition>>("condition", getEndConditions()) {
			public void populateItem(ListItem<IModel<ManualEndCondition>> item) {
				AjaxSubmitLink sb = new AjaxSubmitLink("submit-button", getEditor().getForm()) {
					@Override
					public void onSubmit(AjaxRequestTarget target) {
						onAction(target, getCondition());
					}
					@Override
					public boolean isEnabled() {
						
						return (activecondition==null || !getCondition().getEvent().equals(activecondition)) &&
							TaskActionsPanel.this.isEnabled(getCondition()) &&
							getTask()!=null && getRunningActivity()!=null && 
							getRunningActivity().getUser().equals(getSessionUser());
						
						
						
					}
					@Override
					public boolean isVisible() {
						return getCondition().isEnabled();
					}
					@Override
					protected void onComponentTag(final ComponentTag tag){
						super.onComponentTag(tag);
						if (getCondition().getCss()!=null) {
							String css = tag.getAttributes().get("class")!=null ? tag.getAttributes().get("class").toString() : "";
							tag.put("class", css + " "+getCondition().getCss());
						}
					}	
					protected ManualEndCondition getCondition() {
						return item.getModelObject().getObject();
					}
				};
				sb.add(new Label("label", item.getModelObject().getObject().getLabel()));
				
				String des=item.getModelObject().getObject().getDescription();
				
				Label description = new Label("description", des);
				
				description.setVisible( isHelpVisible() && des!=null && !"".equals(des.trim()) );
				
				item.add(sb);
				sb.add(description);
			}
		});
	}
	
	private void addTakeAction() {
		WebMarkupContainer c = new WebMarkupContainer("take-panel");
		AjaxLink<?> sb = new AjaxLink<Void>("take-button") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				fireScanAll(new KbeeTakeTaskEvent(getTask()));
			}
		};
		sb.add(new Label("label", getLabel("take-action-label")));
		
		Label description = new Label("description", getLabel("take-action-help")) {
			public boolean isVisible() {
				return isHelpVisible();
			}
		};
		sb.add(description);
		c.add(sb);
		addOrReplace(c);
	}
	
	protected boolean isPending() {
		return getWorkflowModel().getObject().isPending();
	}
	
	protected boolean isTakeable() {
		Content content = ((KbeeContext)getWorkflowModel().getObject()).getContent();
        return ServiceLocator.getService(ContentSystemSecurityService.class).isTakeable(content);
    }
	
	protected void setPreference(String key, String value) {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user != null)
			user.getService(PreferencesService.class).setValue(this.getClass().getName(), key, value);
	}
					
	protected String getPreference(String key, String default_value) {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null) {
			String p=user.getService(PreferencesService.class).getValue(this.getClass().getName(), key);
			if (p!=null)
				return p;
		}
		return default_value;
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}