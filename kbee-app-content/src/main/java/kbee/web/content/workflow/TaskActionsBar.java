package kbee.web.content.workflow;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.workflow.EndCondition;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;

import kbee.web.event.wicket.ContentEditorEvent;
import kbee.web.model.procedure.EndConditionModel;
import kbee.web.model.procedure.TaskModel;
import kbee.web.workflow.task.ActionEvent;
import kbee.web.workflow.task.TaskErrorEvent;

@SuppressWarnings("serial")
public class TaskActionsBar<T extends Content> extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	private List<IModel<ManualEndCondition>> actions = null;
	private IModel<WorkflowContext> model;
	private IModel<Task> taskmodel;
	private T contentInEditor;

	public TaskActionsBar(String id, IModel<WorkflowContext> model) {
 		super(id, model);
		setOutputMarkupId(true);
		setWorkflowModel(model);
	}
	
	public void setWorkflowModel(IModel<WorkflowContext> model) {
		this.model = model;
		WorkflowContext context = model.getObject();
		taskmodel = new TaskModel(new ObjectModel<Procedure>(context.getProcedure()), context.getTask());
	}
	
	public IModel<WorkflowContext> getWorkflowModel() {
		return model;
	}
	
	public IModel<Task> getTaskModel() {
		return taskmodel;
	}

	public List<IModel<ManualEndCondition>> getMainActions() {
		List<IModel<ManualEndCondition>> actions = new ArrayList<IModel<ManualEndCondition>>();
		int i = 0;
		for (IModel<ManualEndCondition> actionmodel : getActions()) {
			if (!actionmodel.getObject().isInfrequent()) {
				actions.add(actionmodel);
				i++;
				if (i>2) {
					break;
				}
			}
		};
		return actions;
	}
	
	public List<IModel<ManualEndCondition>> getActions() {
		if (actions==null) {
			actions = new ArrayList<IModel<ManualEndCondition>>();
			for (EndCondition action : getTask().getEndConditions()) {
				if (action instanceof ManualEndCondition) {
					actions.add(new EndConditionModel<ManualEndCondition>(getTaskModel(), (ManualEndCondition)action));
				}
			};
		}
		return actions;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addActions();
		add(new WicketEventListener<ContentEditorEvent>() {
			@SuppressWarnings("unchecked")
			public void onEvent(ContentEditorEvent event) {
				setContentInEditor((T)event.getContent());
				if (event.getRequestTarget()!=null)
				event.getRequestTarget().add(TaskActionsBar.this);
			}
		});
	}
	
	
	@Override
	public void onDetach() {
		super.onDetach();
		for (IModel<ManualEndCondition> model : getActions()) {
			model.detach();
		}
		
		taskmodel.detach();
		contentInEditor = null;
	}
	
	protected Editor<T> getEditor() {
		Assert.isTrue(true, "no editor");
		return null;
	}
	
	private void setContentInEditor(T content) {
		contentInEditor = content;
	}
	
	private T getContentInEditor() {
		if (contentInEditor==null) {
			T content = getEditor().getModelObject();
			getEditor().update(content);
			contentInEditor = content;
		}
		return contentInEditor;
	}

	private boolean isEnabled(ManualEndCondition condition) {
		if (!condition.isEnabled()) {
			return false;
		}
		if ((condition.getPerms()==null || "".equals(condition.getPerms())) &&
			(condition.getCondition()==null || "".equals(condition.getCondition()))) {
			return true;
		}	
		return condition.isEnabled(getContentInEditor());
	}
	
	private void addActions() {
		addOrReplace(new ListView<IModel<ManualEndCondition>>("action", getMainActions()) {
			public void populateItem(ListItem<IModel<ManualEndCondition>> item) {
				AjaxSubmitLink sb = new AjaxSubmitLink("submit-button", getEditor().getForm()) {
					@Override
					public void onSubmit(AjaxRequestTarget target) {
						fire(new ActionEvent(target, getTask(), getAction()));
						target.add(TaskActionsBar.this);
					}
					@Override
					public void onError(AjaxRequestTarget target) {
						fire(new TaskErrorEvent(target, getTask(), getAction()));
					}
					@Override
					public boolean isEnabled() {
						return TaskActionsBar.this.isEnabled(getAction());
					}
					@Override
					public boolean isVisible() {
						return getAction().isEnabled();
					}
					@Override
					protected void onComponentTag(final ComponentTag tag){
						super.onComponentTag(tag);
						if (getAction().getCss()!=null) {
							String css = tag.getAttributes().get("class")!=null ? tag.getAttributes().get("class").toString() : "";
							tag.put("class", css + " "+getAction().getCss());
						}
					}	
					protected ManualEndCondition getAction() {
						return item.getModelObject().getObject();
					}
				};
				sb.add(new Label("label", item.getModelObject().getObject().getLabel()));
				item.add(sb);
			}
		});
		
		addOrReplaceMenu();
	}
	
	private void addOrReplaceMenu() {
		
		WebMarkupContainer container = new WebMarkupContainer("menu-container") {
			public boolean isVisible() {
				return getActions().size()>3;
			}
		};
		
		// Contextual Menu. all actions + info
		//				
		ContextMenuPanel<WorkflowContext> menu = new ContextMenuPanel<WorkflowContext>(getWorkflowModel()) {
			public IModel<WorkflowContext> getModel() {
				return TaskActionsBar.this.getWorkflowModel();
			}
		};
		
		for (final IModel<ManualEndCondition> actionmodel : getActions()) {
			menu.addItem(new MenuItemFactory<WorkflowContext>() {
				@Override
				public AbstractMenuItemPanelV5<WorkflowContext> getItem(String id) {
					return new AjaxMenuItemPanelV5<WorkflowContext>(id) {
						public void onClick(AjaxRequestTarget target) {
							fire(new ActionEvent(target, getTask(), actionmodel.getObject()));
							target.add(TaskActionsBar.this);
						}
						@Override 
						public String getLabel() {
							return actionmodel.getObject().getLabel();
						}
						@Override 
						public boolean isEnabled() {
 							return TaskActionsBar.this.isEnabled(actionmodel.getObject());
						}
						@Override
						protected AbstractLink getNewLink(String id) {
							return new AjaxSubmitLink(id, getEditor().getForm()) {
								@Override
								public void onSubmit(AjaxRequestTarget target) {
									onClick(target);
								}
							};
						}
					};
				}
			});
		}
		
		container.add(menu);

		addOrReplace(container);
	}
	
	private WebTask getTask() {
		return ((WebTask)getWorkflowModel().getObject().getTask());
	}
}