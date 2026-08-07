package com.novamens.content.web.workflow.markup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.model.ContentId;
import com.novamens.content.user.UserService;
import com.novamens.content.web.suggestion.service.UserSuggestionService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.content.text.template.KbeeContentTextTemplate;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.lock.ValueLockerService;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.StaticField;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.modal.Modal.Button;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Priority;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Reason;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.WorkflowRuntimeException;

import kbee.web.content.editor.ContentEditor;
import kbee.web.event.wicket.EditorEvent;
import kbee.web.form.AutoCompleteFieldV5;
import kbee.web.workflow.ResolutionModal;
import kbee.web.workflow.task.KbeeWebWorkflowEvent;

@SuppressWarnings("serial")
public class ConditionEditor<T extends Content> extends ObjectEditor<WorkflowContext> {
	private static final long serialVersionUID = 1L;
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ConditionEditor.class.getName());
	
	private ManualEndCondition condition;
	
	private ContentEditor<T> content_editor;
	
	private List<IModel<Content>> templates;
	private IModel<Content> template;
	private boolean rendered = false;
	private boolean is_termination_rule_visible=false;
	private List<Priority> priorities = null;
	
	class LetterValidator implements IValidator<IModel<Content>> {
		@Override
		public void validate(final IValidatable<IModel<Content>> validatable) {
			if (getTemplate()!=null && getModelObject().getResolution()==null) {
				validatable.error(new ValidationError(this));
			}
		}
	}
	
	public ConditionEditor(String id, IModel<WorkflowContext> model, ManualEndCondition condition) {
		super(id, model);
		setOutputMarkupId(true);
		setCondition(condition);
		setTemplates(condition);
	}

	public ManualEndCondition getCondition() {
		return condition;
	}

	public void setCondition(ManualEndCondition condition) {
		this.condition = condition;
	}
	
	public List<Priority> getPriorities() {
		if (priorities!=null)
			return priorities;
		priorities = new ArrayList<Priority>();
		priorities.add(Priority.Standard);
		priorities.add(Priority.High);
		priorities.add(Priority.Urgent);
		return priorities;
	}
		
	@SuppressWarnings("unchecked")
	public Component getFocusField() {
		onBeforeRender();
		((Field<?>)get("note")).onBeforeRender();
		((Field<?>)get("collaborator")).onBeforeRender();
		if (getCondition()!=null && getCondition().getCollaboration()) {
			return ((Field<?>)get("collaborator")).getInput();
		}
		return ((Field<String>)get("note")).getInput();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (get("task")==null) 
			addFields();
		
		String str = getCondition().getLabel();
		((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
	}

	
	
	protected String getActionPreConditionRule() {
		if (getCondition().getRule()!=null)
			return getCondition().getRule().getDescription();
		else			
			return null;
	}
	
	
	protected String getActionTerminationRule() {
		if (getCondition().getRule()!=null)
			return getCondition().getRule().getDescription();
		else			
			return null;
	}
	
	protected boolean isAdmin() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	}

	protected boolean isSupport() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}
	
	protected boolean isRoot() {
		return ServiceLocator.getService(SecurityService.class).isRoot();
	}
	
	protected void addFields() {
		

		WebMarkupContainer atcmkp = new WebMarkupContainer("action-termination-rule-container-mkp");
		atcmkp.setOutputMarkupId(true);
		add(atcmkp);
		
		AjaxLink<Void> atrl=new AjaxLink<Void>("action-termination-rule-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				ConditionEditor.this.is_termination_rule_visible =! ConditionEditor.this.is_termination_rule_visible;
				target.add(ConditionEditor.this.get("action-termination-rule-container-mkp"));
				
			}
		};
		add(atrl);
		
		WebMarkupContainer atc = new WebMarkupContainer("action-termination-rule-container") {
			public boolean isVisible() {
				return ConditionEditor.this.is_termination_rule_visible;
			}
		};
		
		atc.setOutputMarkupId(true);
		
		Label  la = new Label("action-termination-rule", getActionTerminationRule()) {
			public boolean isVisible() {
				return ConditionEditor.this.is_termination_rule_visible;  
			}
		};
		
		la.setEscapeModelStrings(false);
		atc.add(la);
		atcmkp.add(atc);
		
				
		add(new FeedbackPanel("feedback", getModel()) {
			@Override
			public boolean isVisible() {
				return !validateContent() || ConditionEditor.this.hasErrorMessage();
			}
		});
		
		add(new StaticField<String>("task", new Model<String>() {
			public String getObject() {
				return ConditionEditor.this.getModelObject().getTask().getName();
			}
		}) {
			public boolean isVisible() {
				return getProcedure()!=null && getProcedure().getTasks().size()>1;
			}
		});
			
		add(new ChoiceField<Priority>("priority", new PropertyModel<List<Priority>>(this, "priorities")) {
			@Override
			public boolean isVisible() {
				return getCondition()!=null && getCondition().isEnablePriority() && validateContent(); 
			}
			@Override 
			public String getDisplayValue(Priority pr) {
				return pr.getLabel(getSessionUser().getLocale());
			}
		});
			
		add(new AutoCompleteFieldV5<User>("collaborator", true) {
			@Override
			public List<Suggestion> getSuggestions(String pattern) {
				Map<String, Object> parameters= new HashMap<String, Object>();
				if (getCondition().getCollaborationGroups()!=null) {
					parameters.put("groups", getCondition().getCollaborationGroups());
				}
				return ServiceLocator.getService(UserSuggestionService.class).getSuggestions(pattern, parameters);
			}
			@Override
			public boolean isVisible() {
				return getCondition()!=null && getCondition().getCollaboration() && validateContent(); 
			}
			@Override
			public boolean isEnabled() {
				return true;
			}
			@Override 
			public String getHistoryKey() {
				return "collaborator"; 
			}
		});
			
		add(new TextAreaField<String>("note", 8, 10) {
			@Override
			public boolean isEnabled() {
				return true;
			}
			@Override
			public boolean isVisible() {
				return rendered || validateContent();
			}
		});
		
		setTemplate(getContextTemplate());
		
		add(new ChoiceField<IModel<Content>>("template", 
				new PropertyModel<IModel<Content>>(this, "template"), 
				new PropertyModel<List<IModel<Content>>>(this, "templates"),
				new LetterValidator()) {
			@Override
			public boolean isVisible() {
				return !getTemplates().isEmpty(); 
			}
			@Override
			public boolean isRequired() {
				return getCondition().isMandatoryLetter(); 
			}
			@Override
			public boolean isNullValid() {
				return !getCondition().isMandatoryLetter(); 
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				Map<String, String> parameters = ((KbeeContext)getModelObject()).getParameters();
				if (getTemplate()==null) {
					parameters.clear();
				}
				setTemplate(getValue());
				if (getValue()!=null) {
					parameters.put("template", (new ContentId(getValue().getObject())).toString());
				}
				else {
					parameters.remove("template");
					parameters.remove("resolution");
				}
				((KbeeContext)getModelObject()).getContent().getService(WorkflowService.class).setParameters(parameters);

				target.add(ConditionEditor.this.get("resolution"));
			}
		});
		
		WebMarkupContainer response = new WebMarkupContainer("resolution");
		response.setOutputMarkupId(true);
		response.add(new Link<Void>("editor-link") {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void onClick() {
				setResponsePage(new ResolutionEditorPage(ConditionEditor.this.getModel(),   
					new KbeeContentTextTemplate((OrganizationalText)getTemplate().getObject())));
			}
			public boolean isVisible() {
				return getTemplate()!=null;
			}
		});
		response.add(new AjaxLink<Void>("preview-link") {
			public void onClick(AjaxRequestTarget target) {
				ResolutionModal modal = (ResolutionModal)ConditionEditor.this.get("resolution-modal");
				modal.open(target, ConditionEditor.this.getModel(), new Modal.Handler() {
					@Override
					public void onClick(AjaxRequestTarget target, Button button) {
					}
				});
			}
			public boolean isVisible() {
				return getTemplate()!=null;
			}
		});
		add(response);
		
		add(new ChoiceField<Reason>("reason", new PropertyModel<List<Reason>>(this, "reasons")) {
			@Override
			public boolean isVisible() {
				return !getReasons().isEmpty(); 
			}
			@Override
			public boolean isRequired() {
				return true; 
			}
			@Override
			public boolean isNullValid() {
				return false; 
			}
			@Override
			public IModel<String> getLabel() {
				return ConditionEditor.this.getLabel("property.reason", getCondition().getLabel());
			}
		});
		
		WebMarkupContainer actions = new WebMarkupContainer("actions") {
			public boolean isVisible() {
				return rendered || validateContent();
			}	
		};
				
		add(actions);
		
		actions.add(((ContentEditor<T>)getContentEditor()).new SubmitButton("submit-button", getContentEditor().getForm()) {
			@Override
			protected void onSubmit(AjaxRequestTarget target) {
//				if (!ConditionEditor.this.validateWorkflow()) {
//					throw new WorkflowRuntimeException("NO TASK!");
//				}			
				super.onSubmit(target);
				Serializable contentId = getContent().getId();
				if (ConditionEditor.this.validateContent()) {
					try {
						lock(contentId);
						if (ConditionEditor.this.validateWorkflow()) { 
							if(ConditionEditor.this.reValidateContent()) {
								if (getReasons().isEmpty()) {
									((KbeeContext)ConditionEditor.this.getModelObject()).setReason(null);
								}
								if (getTemplate()==null) {
									getWorkflowService().setResolution(null, null);
								}
								fire(new KbeeWebWorkflowEvent(getCondition().getEvent(), getCondition().getLabel(), target));
								ConditionEditor.this.onSubmit(target);
							}
						}
						else {
							throw new RuntimeException("NO TASK");
						}
					}
					catch(WorkflowRuntimeException e) {
						ConditionEditor.this.error(ConditionEditor.this.getLabel("error.workflow", e.getMessage()).getObject());
						onError(target);
					}
					finally {
						unlock(contentId);
					}
				}
				else {
					target.add(getContentEditor().getForm());
					target.add(ConditionEditor.this.get("task"));
				}
			};
			@Override 
			protected void onError(final AjaxRequestTarget target) {
				super.onError(target);
				target.add(getContentEditor().getForm());
			}
			@Override
			public boolean isEnabled() {
				return true;
			}
			@Override
			public boolean isVisible() {
				return rendered || validateContent();
			}
			
			@Override
			public IModel<String> getLabel() {
				return new Model<String>(getCondition().getLabel());
			}
		});	
		
		actions.add(new AjaxLink<Void>("cancel-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				ConditionEditor.this.onCancel(target);
				fire(new EditorEvent(target, "CANCEL"));
			}
			@Override
			public boolean isVisible() {
				return rendered || validateContent();
			}
		});
		
		this.rendered = validateContent();
		
		add(new ResolutionModal());
	}
	
	
	protected boolean isFreeVersion() {
		try {
			return getDomain().getDomainType()==DomainType.EXPRESS;
		}
		 catch (Exception e) {
			 logger.error(e);
			 return false;
		 }
	}

	public void onCancel(AjaxRequestTarget target) {}
	
	public void onSubmit(AjaxRequestTarget target) {}
		
	public List<IModel<Content>> getTemplates() {
		
		List<IModel<Content>> templates = new ArrayList<IModel<Content>>();
		
		for (IModel<Content> model : this.templates) {
			if (model.getObject().getContentTemplate().isTemplate() || model.getObject().getContentTemplate().isText()) 
				templates.add(model);
		}
		
		Collections.sort(templates, new Comparator<IModel<Content>>() {
			@Override
			public int compare(IModel<Content> a, IModel<Content> b) {
				try {
					return a.getObject().getTitle().compareToIgnoreCase(b.getObject().getTitle());
				} 
				catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
		});
		
		return templates;
	}
	
	public List<Reason> getReasons() {
		return getCondition().getReasons();
	}
	
	@Override
	public void onAfterRender() {
		super.onAfterRender();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		if (templates!=null) {
			for (IModel<Content> model : templates) 
				model.detach();
		}
		
		if (template!=null)
			template.detach();

		// no se puede detach porque es recursivo y da stackoverflow
		content_editor=null;
	}

	protected void setTemplates(ManualEndCondition condition) {
		templates = new ArrayList<IModel<Content>>();
		for (Content template : condition.getLetterTemplates()) {
			templates.add(new ObjectModel<Content>(template));
		}
	}
	
	protected void setTemplate(IModel<Content> template) {
		this.template = template;
	}
	
	protected IModel<Content> getContextTemplate() {
		String templateId = getModelObject().getParameter("template");
		if (templateId!=null) {
			for (IModel<Content> model : getTemplates()) {
				if (templateId.equals((new ContentId(model.getObject())).toString())) {
					return model;
				}
			}
		}
 		return null;
	}
	
	protected IModel<Content> getTemplate() {
		return template;
	}
	
	protected boolean validateContent() {
		return true;
	}
	
	protected boolean reValidateContent() {
		return true;
	}
	
	protected boolean validateWorkflow() {
		boolean validate = getWorkflowService().getTask().equals(getWorkflowService().reloadTask());
		return validate;
	}
	
	protected WorkflowService getWorkflowService() {
		return getContent().getService(WorkflowService.class);
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	@SuppressWarnings("unchecked")
	protected T getContent() {
		return (T)((KbeeContext)getModelObject()).getContent();
	}

	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected IModel<String> getLabel(String key, String... parameter) {
		StringResourceModel model = new StringResourceModel(key, this);
		model.setParameters((Object[]) parameter);
		return model;
	}
	
	protected String getPreviousTaskResolution() {
		Activity previous = getPreviousActivity();
		if (previous!=null) {
			String resolution = previous.getResolution();
			return resolution;
		}
		return null;
	}
	
	protected String getPreviousTaskResolutionTitle() {
		Activity previous = getPreviousActivity();
		if (previous!=null) {
			String resolution = previous.getResolutionTitle();
			return resolution;
		}
		return null;
	}
	
	protected Activity getPreviousActivity() {
		return ((KbeeContext)getModel().getObject()).getPreviousTerminatedActivity();
	}
	
	protected Procedure getProcedure() {
		return getModelObject()!=null ? getModelObject().getProcedure() : null;
	}
	
	private void lock(Serializable value) {
		ServiceLocator.getService(ValueLockerService.class).lock(value);
	}
	
	private void unlock(Serializable value) {
		ServiceLocator.getService(ValueLockerService.class).unlock(value);
	}
	
	@SuppressWarnings("unchecked")
	private ContentEditor<T> getContentEditor() {
 		if (content_editor==null) {
			MarkupContainer parent = getParent();
			while (content_editor==null && parent!=null) {
				if (parent instanceof ContentEditor) {
					content_editor = (ContentEditor<T>)parent;
				}
				else
					parent = parent.getParent();
			}
			Assert.notNull(content_editor, "no editor");
		}
		return this.content_editor;
	}
	
	
}
