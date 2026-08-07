package com.novamens.content.web.workflow.markup;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.iql.AttributePredicate;
import com.novamens.content.iql.ClassifierPredicate;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.ContentService;
import com.novamens.content.user.UserService;
import com.novamens.content.web.content.markup.ClassificationMessage;
import com.novamens.content.web.content.markup.RelationMessage;
import com.novamens.content.workflow.Validator;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.datetime.DateTimeService;
import com.novamens.indexer.iql.Expression;
import com.novamens.indexer.iql.IqlService;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.content.security.PredicatesIqlEvaluator;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.KbeeValidator;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.event.AuditTrailEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.ErrorDialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.WorkflowContext;

import kbee.web.content.editor.ContentEditor;
import kbee.web.event.wicket.CancelWorkflowEvent;
import kbee.web.event.wicket.PreviewClickEvent2;
import kbee.web.object.AuditTrailModal;
import kbee.web.util.NavigationEvent;
import kbee.web.workflow.task.TaskEditor;
import kbee.web.workflow.util.WorkflowContextModel;

@SuppressWarnings("serial")
@Deprecated
public class TaskPanel<T extends Content> extends ContentEditor<T> implements TaskEditor {
	private static final long serialVersionUID = 1L;

	private boolean isnew = false;
	private boolean is_right_visible = true;
	private Boolean validate;
	private IModel<WorkflowContext> workflowmodel;
	private Map<ModelElementTemplate, Serializable> errors;

	@SuppressWarnings("unchecked")
	public TaskPanel(IModel<WorkflowContext> model) {
		addListeners(); 
		setOutputMarkupId(true);
		setModel(((WorkflowContextModel<T>)model).getModel());
		
		setWorkflowModel(model);
		add(new ErrorDialog("error-dialog"));
		add(new AuditTrailModal<T>("audit-modal"));
		
		add(new Dialog("cancel-dialog", "dialog.cancel.title", "dialog.cancel.message", Dialog.Cancel, new Dialog.Button("dialog.cancel.button", "btn btn-sm btn-danger")));
	}
	
	public void showInfoPanel(AjaxRequestTarget target) {}
	public void showEndConditionPanel(AjaxRequestTarget target, ManualEndCondition condition) {}
	public void showKnowledgePanel(AjaxRequestTarget target) {}
	public void showAttributes(AjaxRequestTarget target, Classifier classifier) {}
	public void showAttributes(AjaxRequestTarget target, AttributeTemplate tamplet) {}
	
	public Panel getRightPanel() {
		return null;
	}
	
	public void setWorkflowModel(IModel<WorkflowContext> model) {
		this.workflowmodel = model;
	}
	
	public IModel<WorkflowContext> getWorkflowModel() {
		return workflowmodel;
	}
	
	@Override
	public boolean isFullWidth() {
		return false;
	}
	
	 
	public void setRightPanelVisible(boolean value) {
		this.is_right_visible = value;
	}
	
	public boolean isRightPanelVisible() {
		return is_right_visible;
	}
	
	public String getContentTitle() {
		if (getModel().getObject().getTitle()!=null)
			return getModel().getObject().getTitle();
		
		if (getModel().getObject().getContentTemplate()!=null)
			return getModel().getObject().getContentTemplate().getName();
		
		if (getModel().getObject().getOId()!=null)
			return getModel().getObject().getOId().toString();
		
		return "-";
	}
	
	
	@Override
	public boolean isNew() {
		return isnew;
	}

	@Override
	public void setIsNew(boolean isnew) {
		this.isnew=isnew;
	}
	
	@Override
	public boolean isVisible() {
		return getWorkflowContext()!=null;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		
		if (workflowmodel!=null)
			workflowmodel.detach();
	
		errors = null;
		
		validate = null;
	}

	protected void onNavigate() {
	}
	
	protected WorkflowService getWorkflowService() {
		return getModelObject().getService(WorkflowService.class);
	}
	
	protected WebTask getTask() {
		return ((WebTask) getWorkflowModel().getObject().getTask());
	}
	
	protected WebTask getPreviousTask() {
		return (WebTask)((KbeeContext)getWorkflowModel().getObject()).getPreviousTask();
	}
	
	protected Activity getPreviousTaskResolution() {
		return ((KbeeContext)getWorkflowModel().getObject()).getPreviousTaskResolution();	
	}
	
	protected Activity getTaskResolution() {
		return ((KbeeContext)getWorkflowModel().getObject()).getPreviousTaskResolution();	
	}
	
	public Panel getEditor(ClassifierTemplate template) {
		return null;
	}
	
	protected boolean validateUser() {
		if (getModelObject()!=null && 
				getModelObject().getWorkspace()!=null &&
				getModelObject().getWorkspace().equals(Long.valueOf(getUser().getId())))
			return true;
		return false;
	}
	
	public boolean validate(ManualEndCondition condition) {
		return this.validate(condition, false);
	}
	
	protected boolean validate(ManualEndCondition condition, boolean forcevalidation) {
		if (this.validate!=null && !forcevalidation) return this.validate;
		getFeedbackMessages().clear(null);
		boolean validate = true;
		if (!validateUser()) {
			error(new FeedbackMessage(TaskPanel.this, "ivalid user", FeedbackMessage.ERROR));
			return false;
		}
		for (ClassifierTemplate template : getClassifiers()) {
			if (template.isMandatory()) {
				if (!classified(template.getClassifier())) {
					validate = false;
					String message = TaskPanel.this.getLabel("task.error.required", template.getClassifier().getName()).getObject();
					setError(template, message);
					error(new ClassificationMessage(TaskPanel.this, template.getClassifier(), message, FeedbackMessage.ERROR));
				}
			}
		}
		for (AttributeTemplate template : getAttributes()) {
			if (template.isMandatory()) {
				if (getAttributeValue(template.getAttribute())==null || getAttributeValue(template.getAttribute()).isEmpty()) {
					validate = false;
					String message = TaskPanel.this.getLabel("task.error.required", template.getAttribute().getName()).getObject();
					setError(template, message);
					error(new ClassificationMessage(TaskPanel.this, template, message, FeedbackMessage.ERROR));
				}
			}
		}
		for (RelationTemplate template : getModelObject().getContentTemplate().getRelations()) {
			if (template.isMandatory())	{
				if (getModelObject().getRelations(template).isEmpty()) {
					validate = false;
					String message = TaskPanel.this.getLabel("task.error.required", template.getTargetLabel()).getObject();
					error(new RelationMessage(TaskPanel.this, message, FeedbackMessage.ERROR));
				}
			}
		}
//		if (condition.getRequiredResources() && getModelObject().getService(ContentService.class).getActivityResources().isEmpty()) {
//			String message = TaskPanel.this.getLabel("task.error.resources").getObject();
//			error(new FeedbackMessage(TaskPanel.this, message, FeedbackMessage.ERROR));
//			validate = false;
//		}
		if (validityError()) {
			validate = false;
			AttributeTemplate from = getFromValidity();
			AttributeTemplate to = getToValidity();
			String message = TaskPanel.this.getLabel("task.error.validity", from.getAttribute().getName(), to.getAttribute().getName()).getObject();
			error(new ClassificationMessage(TaskPanel.this, from, message, FeedbackMessage.ERROR));
		}
		if (condition.getPrecondition()!=null && !condition.getPrecondition().isEmpty()) {
			for (Validator validator : condition.getPrecondition()) {
				if (!validator.validate(getWorkflowModel().getObject())) {
					validate = false;
					String iqlcondition = ((KbeeValidator)validator).getCondition();
					List<ModelElement> elements = getElements(iqlcondition);
					if (elements!=null) {
						for (ModelElement element : elements) {
							if (element instanceof Classifier) {
								ClassifierTemplate template = getTemplate((Classifier)element);
								setError(template, validator.getMessage());
							}
							if (element instanceof Attribute) {
								AttributeTemplate template = getTemplate((Attribute)element);
								setError(template, validator.getMessage());
							}
						}
					}
					error(new FeedbackMessage(TaskPanel.this, validator.getMessage(), FeedbackMessage.ERROR));
				}
			}
		}
		this.validate = validate;
		return validate;
	}
	
	protected boolean validityError() {
		Attribute validityFrom = null, validityTo = null;
		for (AttributeTemplate template : getAttributes()) {
			if (template.getAttribute().getType().equals(AttributeType.VALIDITY_FROM)) {
				validityFrom = template.getAttribute();
			}
			if (template.getAttribute().getType().equals(AttributeType.VALIDITY_TO)) {
				validityTo = template.getAttribute();
			}
		}	
		if (validityTo==null || validityFrom==null)
			return false;
		List<String> fromvalues = getAttributeValue(validityFrom);
		List<String> tovalues = getAttributeValue(validityTo);
		if (fromvalues==null || tovalues==null || fromvalues.isEmpty() || tovalues.isEmpty())
			return false;
		OffsetDateTime from = ServiceLocator.getService(DateTimeService.class).parseStrDate(fromvalues.get(0));
		OffsetDateTime to = ServiceLocator.getService(DateTimeService.class).parseStrDate(tovalues.get(0));
		if (from.isAfter(to))
			return true;
		return false;
	}
	
	protected AttributeTemplate getFromValidity () {
		AttributeTemplate validityFrom = null;
		for (AttributeTemplate template : getAttributes()) {
			if (template.getAttribute().getType().equals(AttributeType.VALIDITY_FROM)) {
				validityFrom = template;
				break;
			}
		}	
		return validityFrom;
	}
	
	protected AttributeTemplate getToValidity () {
		AttributeTemplate validityTo = null;
		for (AttributeTemplate template : getAttributes()) {
			if (template.getAttribute().getType().equals(AttributeType.VALIDITY_TO)) {
				validityTo = template;
				break;
			}
		}	
		return validityTo;
	}
	
	protected void clearValidation() {
		validate =  null;
	}

	protected List<ClassifierTemplate> getClassifiers() {
		List<ClassifierTemplate> templates = getTask().getClassifiers();
		if (templates.isEmpty() && getTask().getSections().isEmpty()) {
			templates = getModelObject().getContentTemplate().getClassifiers();
		}
		return templates;
	}
	
	protected ClassifierTemplate getTemplate(Classifier classifier) {
		for (ClassifierTemplate template : getClassifiers()) {
			if (template.getClassifier().equals(classifier)) {
				return template;
			}
		}
		return null;
	}
	
	protected AttributeTemplate getTemplate(Attribute attribute) {
		for (AttributeTemplate template : getAttributes()) {
			if (template.getAttribute().equals(attribute)) {
				return template;
			}
		}
		return null;
	}

	protected List<AttributeTemplate> getAttributes() {
		List<AttributeTemplate> templates = getTask().getAttributes();
		if (templates.isEmpty() && getTask().getSections().isEmpty()) {
			templates = getModelObject().getContentTemplate().getAttributes();
		}
		return templates;
	}
	 
	protected boolean classified(Classifier classifer) {
		for (Classification classification : getClassification()) {
			if (classification!=null && classification.getClassifier().equals(classifer))
				return true;
		}
		return false;
	}
	
	protected boolean isPrivateNotesEnabled() {
		if (!getModelObject().getContentTemplate().isPrivateNotes())
			return false;
		return ServiceLocator.getService(ContentSystemSecurityService.class).isPrivateEnabled(getModelObject());
	}
	
	protected boolean isPrivateNotesEnabled(T content) {
		if (!getModelObject().getContentTemplate().isPrivateNotes())
			return false;
		return ServiceLocator.getService(ContentSystemSecurityService.class).isPrivateEnabled(content);
	}
	
	protected void addListeners() {
		
		add(new WicketEventListener<NavigationEvent>() {
			public void onEvent(NavigationEvent event) {
				update(false);
				onNavigate();
			}
		});
		
		add(new WicketEventListener<PreviewClickEvent2<T>>() {
			@Override
			public void onEvent(PreviewClickEvent2<T> event) {
				WebPage page = getPortalPreviewPage(event.getModel());
				if (page!=null)
					setResponsePage(page);
			}
		});
		
		add(new WicketEventListener<AuditTrailEvent>() {
			@Override
			public void onEvent(AuditTrailEvent event) {
				openAudit(event.getRequestTarget());
			}
		});
		
		
		add(new WicketEventListener<CancelWorkflowEvent>() {
			@Override
			public void onEvent(CancelWorkflowEvent event) {
				openCancelWorkflow(event.getRequestTarget());
			}
		});
	}

	/** --------------------------------------------------------------------------
	 */
	public Procedure getProcedure() {
		return getWorkflowModel().getObject().getProcedure();
	}
	
	public WorkflowContext getWorkflowContext() {
		return getWorkflowModel().getObject();
	}
	
	@SuppressWarnings("unchecked")
	protected void openAudit(AjaxRequestTarget target) {
		((AuditTrailModal<T>)get("audit-modal")).open(target, getModel());
	}
	
	
	protected void openCancelWorkflow(AjaxRequestTarget target) {
		((Dialog)TaskPanel.this.get("cancel-dialog")).open(target, new Dialog.Handler() {
			@Override
			public void onClick(AjaxRequestTarget target, Button button) {
				if (button.key().equals("dialog.cancel.button")) {
					TaskPanel.this.getModel().getObject().getService(WorkflowService.class).cancel();
					fire(new NavigationEvent());
				}
			}
		}, getProcedure().getName(), getContentTitle());
	}
	
	
	protected WebPage getPortalPreviewPage(IModel<T> model) {
		WebPage page;
		
		if (model.getObject().getContentTemplate().isVideo() ||
			model.getObject().getContentTemplate().isAudio()) {
			 page = (WebPage) ServiceLocator.getService(BeansService.class).getBean("portal-detail-video",  model.getObject());
		}
		else if (model.getObject().getContentTemplate().isImage()) {
			    page = (WebPage) ServiceLocator.getService(BeansService.class).getBean("portal-detail-video",  model.getObject());
		}
		else {
			page = (WebPage) ServiceLocator.getService(BeansService.class).getBean("portal-detail-text" , model.getObject());
		}
		return page;
	}
	
	 
	protected Content getContent() {
		return getModelObject();
	}
	
	 
	protected boolean isWriteable(Content content) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(content);
	}
	
	protected IModel<String> getLabel(String key, String... parameter) {
		StringResourceModel model = new StringResourceModel(key, this);
		model.setParameters((Object[])parameter);
		return model;
	}
	
	protected void setError(ModelElementTemplate template, Serializable message) {
		if (errors==null) {
			errors = new HashMap<ModelElementTemplate, Serializable>();
		}
		errors.put(template, message);
	}
	
	protected Map<ModelElementTemplate, Serializable> getErrors() {
		return errors;
	}
	
	protected void error(FeedbackMessage message) {
		getFeedbackMessages().add(message);
	}
	
	protected KbeeUser getUser() {
		return (KbeeUser) ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}
	
	private List<ModelElement> getElements(String condition) {
		try {
			List<ModelElement> elements = new ArrayList<ModelElement>();
			IqlService iqlservice = getModelObject().getDomain().getService(IqlService.class);
			Expression expression = iqlservice.getExpression(condition);
			PredicatesIqlEvaluator evaluator = new PredicatesIqlEvaluator(expression);
			Map<String, List<String>> predicates = evaluator.evaluate();
			for (String predicatename : predicates.keySet()) {
				com.novamens.indexer.iql.Predicate predicate = iqlservice.getPredicateManager().getPredicate(predicatename);
				if (predicate instanceof ClassifierPredicate) {
					elements.add(((ClassifierPredicate)predicate).getClassifier());
				}
				else
				if (predicate instanceof AttributePredicate) {
					elements.add(((AttributePredicate)predicate).getAttribute());
				}
			}
			return elements;
		}
		catch (Exception e) {
			return null;
		}
	}
	
}
