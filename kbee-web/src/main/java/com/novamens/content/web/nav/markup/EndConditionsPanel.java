package com.novamens.content.web.nav.markup;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.web.workflow.markup.TaskPanel;
import com.novamens.content.workflow.EndCondition;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.workflow.WorkflowContext;

import kbee.web.event.wicket.EditorEvent;
import kbee.web.workflow.task.TaskEditor;

@SuppressWarnings("serial")
public class EndConditionsPanel<T extends Content> extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	private IModel<WorkflowContext> model;
	private String activecondition;

	
	//
	// ESTO DEBE SER UN MODEL. POR AHORA ANDA PORQUE SE USAN PROXYS SERIALIZABLES
	//
	List<ManualEndCondition> conditions = null;

	
	
	public EndConditionsPanel(IModel<WorkflowContext> model) {
		super("endconditions");
		
		setOutputMarkupId(true);
		setWorkflowModel(model);
		
		add(new WicketEventListener<EditorEvent>() {
			public void onEvent(EditorEvent event) {
				if (get("conditions")!=null && event.getElement()!=null) {
					addOrReplaceMenu();
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
	
	public void setWorkflowModel(IModel<WorkflowContext> model) {
		this.model = model;
	}
	
	public IModel<WorkflowContext> getWorkflowModel() {
		return model;
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("conditions")==null) {
			addConditions();
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();

//		if (m_conditions != null) {
//			for (ManualEndConditionModel m: m_conditions) 
		
//				m.detach();
//		}
		
		if (model!=null)
			model.detach();
	}
	
	private void addConditions() {
		
		WebMarkupContainer conditions = new WebMarkupContainer("conditions");
		conditions.setOutputMarkupId(true);
		
		
		conditions.add(new ListView<ManualEndCondition>("condition", getFirstEndConditions()) {
								
			public void populateItem(ListItem<ManualEndCondition> item) {
				
					AjaxSubmitLink sb = new AjaxSubmitLink("submit-button", getEditor().getForm()) {
							@Override
							public void onSubmit(AjaxRequestTarget target) {
								activecondition = item.getModelObject().getEvent();
								Editor<T> editor = getEditor();
								((TaskEditor)editor).showEndConditionPanel(target, item.getModelObject());
								target.add(EndConditionsPanel.this.get("conditions"));
							}
							@Override
							public boolean isEnabled() {
								return (activecondition==null || !item.getModelObject().getEvent().equals(activecondition)) &&
									EndConditionsPanel.this.isEnabled(item.getModelObject());
							}
							@Override
							public boolean isVisible() {
								return item.getModelObject().isEnabled();
							}
							@Override
							protected void onComponentTag(final ComponentTag tag){
								super.onComponentTag(tag);
								if (item.getModelObject().getCss()!=null) {
									String css = tag.getAttributes().get("class")!=null ? tag.getAttributes().get("class").toString() : "";
									tag.put("class", css + " "+item.getModelObject().getCss());
								}
							}	
						};
						
						
						
						sb.add(new Label("label", item.getModelObject().getLabel()));
						
						item.add(sb);
				
						StringBuilder css = new StringBuilder();
						css.append("dropdown hidden-xs "); 											// en xs ningun boton
						if 	(item.getIndex()>1) css.append(css.append(" hidden-sm  hidden-md"));  	// en sm y md solo botones 0 y 1 botones
						if 	(item.getIndex()>2) css.append(css.append(" hidden-lg "));            	// en lg botones 0 1 y 2 
						item.add(new AttributeModifier("class", css.toString()));
						
						
						
			}
		});
		
		addOrReplace(conditions);
		
		addOrReplaceMenu();
	}
	
	private void addOrReplaceMenu() {
		
		WebMarkupContainer conditions =( WebMarkupContainer)get("conditions");
		
		// Contextual Menu. all actions + info
		//				
		ContextMenuPanel<WorkflowContext> menu = new ContextMenuPanel<WorkflowContext>(getWorkflowModel()) {
			public IModel<WorkflowContext> getModel() {
				return EndConditionsPanel.this.getWorkflowModel();
			}
		};
		
		
		for (final ManualEndCondition condition : getEndConditions()) {
			
			menu.addItem(new MenuItemFactory<WorkflowContext>() {
				@Override
				public AbstractMenuItemPanelV5<WorkflowContext> getItem(String id) {
					return new AjaxMenuItemPanelV5<WorkflowContext>(id) {
						public void onClick(AjaxRequestTarget target) {
							Editor<T> editor = getEditor();
							((TaskPanel<T>)editor).showEndConditionPanel(target, condition);
							target.add(EndConditionsPanel.this.getParent());
						}
						@Override 
						public String getLabel() {
							return condition.getLabel();
						}
						@Override 
						public boolean isEnabled() {
							return (activecondition==null || !condition.getEvent().equals(activecondition)) &&
								EndConditionsPanel.this.isEnabled(condition);
						}
						@Override 
						public boolean isVisible() {
							return true;
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
						@Override
						public String getCssClass() {
							return null;
						}
					};
				}
			});
		}
		
		menu.addItem(new MenuItemFactory<WorkflowContext>() {
			@Override
			public AbstractMenuItemPanelV5<WorkflowContext> getItem(String id) {
				return new SeparatorMenuItemPanelV5<WorkflowContext>(id) {
					@Override
					public boolean isVisible() {
 						return true;
					}
					@Override
					public String getCssClass() {
						return "divider";
					}
				};
			}
		});
		
		
		menu.addItem(new MenuItemFactory<WorkflowContext>() {
			@Override
			public AbstractMenuItemPanelV5<WorkflowContext> getItem(String id) {
				return new AjaxMenuItemPanelV5<WorkflowContext>(id) {
					public void onClick(AjaxRequestTarget target) {
						activecondition = null;
						Editor<T> editor = getEditor();
							((KbeeUser) getSessionUser()).getService(PreferencesService.class).setValue(TaskPanel.class.getSimpleName(), "one-panel", "no");
							((TaskPanel<T>)editor).showInfoPanel(target);
						target.add(EndConditionsPanel.this.getParent());
					}
					@Override
					public String getLabel() {
						return new StringResourceModel("info", EndConditionsPanel.this, null).getObject();
					}
					@Override
					public boolean isEnabled() {
						return true;
					}
				};
			}
		});

		conditions.addOrReplace(menu);
		
	}

	public List<ManualEndCondition> getEndConditions() {
		if (conditions==null) {
			conditions = new ArrayList<ManualEndCondition>();
			for (EndCondition condition : getTask().getEndConditions()) {
				if (condition instanceof ManualEndCondition) {
					conditions.add((ManualEndCondition)condition);
				}
			};
		}
		return conditions;
	}
	
	public List<ManualEndCondition> getFirstEndConditions() {
		List<ManualEndCondition> conditions = new ArrayList<ManualEndCondition>();
		int i = 0;
		for (EndCondition condition : getEndConditions()) {
			if (!condition.isInfrequent()) {
				conditions.add((ManualEndCondition)condition);
				i++;
				if (i>2) {
					break;
				}
			}
		};
		return conditions;
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
	
	protected Editor<T> getEditor() {
		Assert.isTrue(true, "no editor");
		return null;
	}
	
	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private WebTask getTask() {
		return ((WebTask)getWorkflowModel().getObject().getTask());
	}
}
