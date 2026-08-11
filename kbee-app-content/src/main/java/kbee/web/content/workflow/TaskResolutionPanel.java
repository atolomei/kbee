package kbee.web.content.workflow;

import java.util.Optional;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.workflow.WorkflowContext;

import kbee.web.workflow.FeedbackPanel;
import kbee.web.workflow.task.TaskEditor;

@SuppressWarnings("serial")
public class TaskResolutionPanel <T extends Content> extends ModelPanel<WorkflowContext> {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TaskResolutionPanel.class.getName());
	
	
	private static final long serialVersionUID = 1L;
	
	private IModel<WorkflowContext> model;
	private Panel step = null;

	private TaskActionsPanel<T> task_actions;
	
	public TaskResolutionPanel(String id, IModel<WorkflowContext> model) {
		super(id);
		setOutputMarkupId(true);
		setWorkflowModel(model);
	}
	
	public void setWorkflowModel(IModel<WorkflowContext> model) {
		this.model = model;
	}

	public IModel<WorkflowContext> getWorkflowModel() {
		return model;
	}
	
	public void setAction(ManualEndCondition action) {
		if (validate(action)) {
			setStep(getResolutionEditor(action));
		}
		else {
			setStep(new FeedbackPanel("step"));
		}
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	}
	
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (step==null) {
			setStep(getActionsPanel());
		}
		else {
			if (step instanceof TaskResolutionEditor<?>) {
				Optional<AjaxRequestTarget> target = RequestCycle.get().find(AjaxRequestTarget.class);
				if (target!=null && target.isPresent()) {
					target.get().focusComponent(((TaskResolutionEditor<?>)step).getFocusField());
				}
			}
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		step = null;
	}
	
	protected void refresh(AjaxRequestTarget target) {
		target.add(this);
	}
	
	protected void setStep(Panel step) {
		this.step = step;
		addOrReplace(step);
	}
	
	
	
	
	protected Panel getActionsPanel() {
	
		task_actions = new TaskActionsPanel<T>("step", getWorkflowModel()) {
			@Override
			public void onAction(AjaxRequestTarget target, ManualEndCondition action) {
				if (validate(action)) {
					setStep(getResolutionEditor(action));
				}
				else {
					setStep(new FeedbackPanel("step"));
				}
				refresh(target);
			}
		};
		
		return task_actions;
		
	}
	
	protected boolean validate(ManualEndCondition action) {
		return getEditor().validate(action);
	}
	
	protected Panel getResolutionEditor(ManualEndCondition action) {
		return new TaskResolutionEditor<T>("step", getWorkflowModel(), action) {
			@Override
			public void onCancel(AjaxRequestTarget target) {
				setStep(getActionsPanel());
				refresh(target);
			}
			
			@Override
			public void onSubmit(AjaxRequestTarget target) {
				logger.debug("Submit");
				refresh(target);
			}
			
		};
	}
	
	protected TaskEditor getEditor() {
		MarkupContainer parent = getParent();
		while (parent!=null) {
			if (parent instanceof TaskEditor) {
				return (TaskEditor)parent;
			}
			else
				parent = parent.getParent();
		}
		Assert.notNull(null, "no editor");
 		return null;
	}
	
	
	public String getPreference(String key, String defaultValue) {
		return ((com.novamens.kbee.security.KbeeUser) getSessionUser()).getService(PreferencesService.class).getValue( this.getClass().getName(), key, defaultValue);
	}

	public int getIntPreference(String key, int defaultValue) {
		return ((com.novamens.kbee.security.KbeeUser) getSessionUser()).getService(PreferencesService.class).getIntValue( this.getClass().getName(), key, defaultValue);
	}

	protected void setPreference(String key, String value) {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user != null)
			user.getService(PreferencesService.class).setValue( this.getClass().getName(), key, value);
	}
	
	protected User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
		} 
		catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
}