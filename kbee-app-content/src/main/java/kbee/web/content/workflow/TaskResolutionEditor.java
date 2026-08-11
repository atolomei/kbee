package kbee.web.content.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.entity.Person;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.model.ContentId;
import com.novamens.content.model.DataSet;
import com.novamens.content.service.PersonService;
import com.novamens.content.text.template.ContentTextTemplate;
import com.novamens.content.web.workflow.markup.FeedbackPanel;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.DomainType;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.content.text.template.KbeeContentTextTemplate;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.lock.ValueLockerService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.StaticField;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.model.SerializableModel;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Priority;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Reason;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.WorkflowRuntimeException;

import kbee.web.content.editor.ContentEditor;
import kbee.web.eform.EFormCapture;
import kbee.web.event.wicket.EditorEvent;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.model.procedure.EndConditionModel;
import kbee.web.model.procedure.TaskModel;
import kbee.web.workflow.PersonCollaboratorSelector;
import kbee.web.workflow.ResolutionModal;
import kbee.web.workflow.task.KbeeWebWorkflowEvent;

@SuppressWarnings("serial")
public class TaskResolutionEditor<T extends Content> extends ObjectEditor<WorkflowContext> {
	private static final long serialVersionUID = 1L;
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TaskResolutionEditor.class.getName());
	
	private IModel<ManualEndCondition> conditionmodel;
	private IModel<Person> collaboratormodel = null;
	private ContentEditor<T> content_editor;
	private List<IModel<Content>> templates;
	private IModel<Content> template;
	private boolean rendered = false;
	private List<Priority> priorities = null;
	
	/**
	 * @param id
	 * @param model
	 * @param condition
	 */
	public TaskResolutionEditor(String id, IModel<WorkflowContext> model, ManualEndCondition condition) {
		super(id, model);
		setOutputMarkupId(true);
		setCondition(condition);
		setTemplates(condition);
	}

	public ManualEndCondition getCondition() {
		return conditionmodel.getObject();
	}
	
	public IModel<ManualEndCondition> getConditionModel() {
		return conditionmodel;
	}

	public void setCondition(ManualEndCondition condition) {
		WorkflowContext context = getModelObject();
		IModel<Task> taskmodel = new TaskModel(new ObjectModel<Procedure>(context.getProcedure().getMaster()), context.getTask());
		this.conditionmodel = new EndConditionModel<ManualEndCondition>(taskmodel, condition);
	}
	
	public void onCancel(AjaxRequestTarget target) {
	}
	
	public void onSubmit(AjaxRequestTarget target) {
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
		if (getCondition().getCollaboration()) {
			return ((PersonCollaboratorSelector)get("collaborator")).getFocusField();
		}
		else {
			if (((Field<?>)get("note"))!=null) {
				((Field<?>)get("note")).onBeforeRender();
				return ((Field<String>)get("note")).getInput();
			}
			else {
				return null;
			}
		}
	}
	
	public List<Reason> getReasons() {
		return getCondition().getReasons();
	}
	
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
	
	public void setCollaborator(Person person) {
		collaboratormodel = new ObjectModel<Person>(person);
	}
	
	public Person getCollaborator () {
		return collaboratormodel != null ? collaboratormodel.getObject() : null;
	}
	
	public void updateModel() {
		getWorkflowService().update();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		if (get("task")==null) 
			addFields();
		String str = getCondition().getLabel();
		((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
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
		content_editor=null;
		conditionmodel.detach();
	}
	
	protected void addFields() {
		
	 	add(new StaticField<String>("action",new Model<String>(getCondition().getLabel())));
	 	
	 	add(new Label("description",new Model<String>(getCondition().getDescription())));
	 	
		add(new FeedbackPanel("feedback", getModel()) {
			@Override
			public boolean isVisible() {
				return !validateContent() || TaskResolutionEditor.this.hasErrorMessage();
			}
		});
		
		if (getCondition().isTokenValidation()) {
			add(new TokenValidationPanel<T>("token", getModel()));
		}
		else {
			add(new InvisiblePanel("token"));
		}
		
		add(new ChoiceField<Priority>("priority", new PropertyModel<List<Priority>>(this, "priorities")) {
			@Override
			public boolean isVisible() {
				if (isFreeVersion())
					return false;
				return getCondition()!=null && getCondition().isEnablePriority() && validateContent(); 
			}
			@Override 
			public String getDisplayValue(Priority pr) {
				return pr.getLabel(getSessionUser().getLocale());
			}
		});
		
		add(new PersonCollaboratorSelector(getConditionModel(), 
			new PropertyModel<Person>(this, "collaborator"),
			getCollaborationSetModel()));
			
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
				new PropertyModel<List<IModel<Content>>>(this, "templates")
				) {
				 
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

				target.add(TaskResolutionEditor.this);
			}
		});
	
		IModel<ContentTextTemplate> templateModel = new SerializableModel<ContentTextTemplate>() {
			public ContentTextTemplate getObject() {
				return getTemplate()!=null ? new KbeeContentTextTemplate((OrganizationalText)getTemplate().getObject()) : null;
			}
		};
		
		add(new ResolutionLetterPanel<T>(getModel(), templateModel) {
			public boolean isVisible() {
				return getTemplate()!=null;
			}
		});
		
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
				return TaskResolutionEditor.this.getLabel("property.reason", getCondition().getLabel());
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
 	
				super.onSubmit(target);
				Serializable contentId = getContent().getId();
				if (TaskResolutionEditor.this.validateContent()) {
					try {
						lock(contentId);
						if (TaskResolutionEditor.this.validateWorkflow()) { 
							if(TaskResolutionEditor.this.reValidateContent()) {
								if (getCondition().getCollaboration()) {
									getContext().setCollaborator(getOrCreateCollaboratorUser());
								}
								for (EForm form : getTask().getForms()) {
									getContext().setFormCapture(form, getSnapshot(getContent().getFormData(form)));
								}
								if (getReasons().isEmpty()) {
									getContext().setReason(null);
								}
								if (getTemplate()==null) {
									getWorkflowService().setResolution(null, null);
								}
								fire(new KbeeWebWorkflowEvent(getCondition().getEvent(), getCondition().getLabel(), target));
								TaskResolutionEditor.this.onSubmit(target);
							}
						}
						else {
							logger.error("TaskResolutionEditor.this.validateWorkflow() -> false");
							throw new RuntimeException("NO TASK");
						}
					}
					catch(WorkflowRuntimeException e) {
						logger.error(e);
						TaskResolutionEditor.this.error(TaskResolutionEditor.this.getLabel("error.workflow", e.getMessage()).getObject());
						onError(target);
					}
					finally {
						unlock(contentId);
					}
				}
				else {
					target.add(getContentEditor().getForm());
				}
			};
			@Override 
			protected void onError(final AjaxRequestTarget target) {
				super.onError(target);
				target.add(TaskResolutionEditor.this);
				 
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
				fire(new EditorEvent(target, "CANCEL"));
				TaskResolutionEditor.this.onCancel(target);
			}
			@Override
			public boolean isVisible() {
				return rendered || validateContent();
			}
		});
		
		this.rendered = validateContent();
		
		add(new ResolutionModal());
	}

	protected boolean validateContent() {
		return true;
	}
	
	protected boolean reValidateContent() {
		return true;
	}
	
	protected boolean validateWorkflow() {
		return getWorkflowService().getTask().equals(getWorkflowService().reloadTask());
	}
	
	protected void setTemplate(IModel<Content> template) {
		this.template = template;
	}
	
	protected IModel<Content> getTemplate() {
		return template;
	}

	protected void setTemplates(ManualEndCondition condition) {
		templates = new ArrayList<IModel<Content>>();
		for (Content template : condition.getLetterTemplates()) {
			templates.add(new ObjectModel<Content>(template));
		}
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
	
	@SuppressWarnings("unchecked")
	protected T getContent() {
		return (T)((KbeeContext)getModelObject()).getContent();
	}
	
	protected KbeeTask getTask() {
		return (KbeeTask)((KbeeContext)getModelObject()).getTask();
	}
	
	protected KbeeContext getContext() {
		return ((KbeeContext)getModelObject());
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
	
	protected String getSnapshot(EFormData data) {
		return (new EFormCapture(data)).getString(); 
	}
	
	@Override
	protected void addListeners() {
		super.addListeners();
		add(new WicketEventListener<ErrorEvent<?>>() {
			public void onEvent(ErrorEvent<?> event) {
				event.getRequestTarget().add(TaskResolutionEditor.this);
			}
		});
	}
	
	private User getOrCreateCollaboratorUser() {
		Person collaborator = getCollaborator();
		User collaboratoruser = getUser(collaborator);
		if (collaboratoruser == null) {
			collaboratoruser = collaborator.getService(PersonService.class).createUser();
		}
		return collaboratoruser;
	}
	
	private User getUser(Person person) {
		return person.getService(PersonService.class).getUser();
	}
	
	private IModel<DataSet> getCollaborationSetModel() {
		DataSet collaborationset = getCondition().getCollaborationSet();
		if (collaborationset==null) {
			collaborationset = getContentDao().getUserSet();
		}
		return new ObjectModel<DataSet>(collaborationset);
	}
	
	private Activity getPreviousActivity() {
		return ((KbeeContext)getModel().getObject()).getPreviousTerminatedActivity();
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
	
	private WorkflowService getWorkflowService() {
		return getContent().getService(WorkflowService.class);
	}
	
	private KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private boolean isFreeVersion() {
		return getDomain().getDomainType()==DomainType.EXPRESS;
	}

	/*
	 * private Domain getDomain() { return
	 * ServiceLocator.getService(UserService.class).getDomain(); }
	 */
}