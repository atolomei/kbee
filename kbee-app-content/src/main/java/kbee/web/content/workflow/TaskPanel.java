package kbee.web.content.workflow;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.document.IDoc;
import com.novamens.content.form.EForm;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.ModelSection;
import com.novamens.content.service.ContentService;
import com.novamens.content.user.UserService;
import com.novamens.content.workflow.EndCondition;
import com.novamens.content.workflow.Validator;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.content.form.KbeeDefaultForm;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.KbeeTakeTaskEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.event.AuditTrailEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.lock.ValueLockerService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.ErrorDialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.WorkflowException;

import kbee.web.content.editor.ClassificationPanel;
import kbee.web.content.editor.ContentEditor;
import kbee.web.content.eform.ContentFormEditor;
import kbee.web.event.wicket.CancelWorkflowEvent;
import kbee.web.event.wicket.PreviewClickEvent2;
import kbee.web.object.AuditTrailModal;
import kbee.web.panel.AlertPanel;
import kbee.web.util.NavigationEvent;
import kbee.web.workflow.ProcessChartPanel;
import kbee.web.workflow.task.ActionEvent;
import kbee.web.workflow.task.EFormEvent;
import kbee.web.workflow.task.TaskEditor;
import kbee.web.workflow.task.TaskPage;
import kbee.web.workflow.util.WorkflowContextModel;

@SuppressWarnings("serial")
public class TaskPanel<T extends Content> extends ContentEditor<T> implements TaskEditor {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TaskPanel.class.getName());
	
	private boolean isnew = false;
	private Boolean validate;
	private IModel<WorkflowContext> workflowmodel;
	Boolean is_overdue = null;
	private Panel task_billboard = null;

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
	
	public void showEndConditionPanel(AjaxRequestTarget target, ManualEndCondition condition) {
	}
	
	public void showAttributes(AjaxRequestTarget target, Classifier classifier) {
	}
	
	public VerticalLayout<ITab> getLayout() {
		return null;
	}
	
	public void setWorkflowModel(IModel<WorkflowContext> model) {
		this.workflowmodel = model;
	}
	
	public IModel<WorkflowContext> getWorkflowModel() {
		return workflowmodel;
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
	
	public Procedure getProcedure() {
		return getWorkflowModel().getObject().getProcedure();
	}
	
	public WorkflowContext getWorkflowContext() {
		return getWorkflowModel().getObject();
	}
	
	public ContentTemplate getContentTemplate() {
		return getModel().getObject().getContentTemplate();
	}
	
	public ClassificationPanel<T> getEditor(EForm form) {
		try {
			for (ClassificationPanel<T> editor : getEditors()) {
				if (editor instanceof ContentFormEditor) {
					if (((ContentFormEditor<T>)editor).getForm().getName().equals(form.getName())) {
						try {
							if (isPending()) {
								StringResourceModel s = new StringResourceModel("pending-task", TaskPanel.this, null);
								AlertPanel<T> a=new AlertPanel<T>("alert",  AlertPanel.INFO, TaskPanel.this.getModel(), null, s);
								a.setIcon(AlertPanel.HELP_INFO);
								((ContentFormEditor<T>) editor).setAlert(a);
								
							}
							/**
							else if (!getSessionUser().getId().equals(getWorkflowModel().getObject().getCurrentActivity().getUser().getId())) {
								StringResourceModel s = new StringResourceModel("only-owner", TaskPanel.this, null);
								TaskPanel.this.getWorkflowModel().getObject().getCurrentActivity().getUser().getFirstLastName();
								s.setParameters(new Object[] {TaskPanel.this.getWorkflowModel().getObject().getCurrentActivity().getUser().getFirstLastName()});
								AlertPanel<T> a=new AlertPanel<T>("alert",  AlertPanel.INFO, TaskPanel.this.getModel(), null, s);
						 		((ContentFormEditor<T>) editor).setAlert(a);
							}
							**/
						} 
						catch (Exception e) {
							logger.error(e);
							AlertPanel<T> a=new AlertPanel<T>("alert",  AlertPanel.DANGER, TaskPanel.this.getModel(), null, 
									new Model<String>(e.getClass().getName() + " " + e.getMessage()));
					 		((ContentFormEditor<T>) editor).setAlert(a);
						}
						
				 		return editor;
					}
				}
			}
		} 
		catch (Exception e) {
			logger.error(e);
		}
		return null;
	}
	
	public boolean validate(ManualEndCondition condition) {
		return this.validate(condition, false);
	}
	
	@Override
	public void update(AjaxRequestTarget target) {
		update(target, false);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (workflowmodel!=null)
			workflowmodel.detach();
		validate = null;
	}
	
	protected ITab getResolutionTab() {
		List<ITab> tabs = getLayout().getTabs(TaskResolutionPanel.class);
		if (tabs.isEmpty()) return null;
		return tabs.get(0);
	}
	
	@SuppressWarnings("unchecked")
	protected void handle(ActionEvent event) {
		ITab resolutionTab = getResolutionTab();
		if (resolutionTab==null) return;
		getLayout().setSelectedTab(resolutionTab);
		TaskResolutionPanel<IDoc> resolutionPanel = (TaskResolutionPanel<IDoc>)resolutionTab.getPanel("panel");
		event.getRequestTarget().add(this);
		EndCondition action = event.getAction();
		if (action!=null && action instanceof ManualEndCondition) {
			resolutionPanel.setAction((ManualEndCondition)action);
			event.getRequestTarget().add(resolutionPanel);
			event.getRequestTarget().add(this);
		}
	}
	
	protected void handle(EFormEvent event) {
		for (ITab tab : getLayout().getTabs()) {
			if (tab.getTitle().getObject().equals(event.getName())) {
				getLayout().setSelectedTab(tab);
				event.getRequestTarget().add(this);
				break;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void handle(KbeeTakeTaskEvent event) {
		
		Optional<AjaxRequestTarget> optionaltarget = RequestCycle.get().find(AjaxRequestTarget.class);
		
		try {
			
			lock(getContent());
			
			if (isTaskStarted()) {
				unlock(getContent());
				if (optionaltarget.isPresent())
					getErrorDialog().open(optionaltarget.get(), getLabel("not-pending"));
				logger.error(getLabelString("not-pending"));
			}
			else {
				
				getContent().getService(WorkflowService.class).startTask();
				((TaskPage<T>)getPage()).setEditionEnabled(true);
				
				if (event.getTarget()!=null) {
					TaskPanel.this.onInitialize();
					
					event.getTarget().add(TaskPanel.this);
				}
				else if (optionaltarget.isPresent()) {
					optionaltarget.get().add(getPage());
				}
			}
		}
		catch (WorkflowException e) {
			logger.error(e.getMessage());
			
			unlock(getContent());
			if (event.getTarget()!=null)
				getErrorDialog().open(event.getTarget(), new Model<String>(e.getMessage()));
			else if (optionaltarget.isPresent())
				getErrorDialog().open(optionaltarget.get(), new Model<String>(e.getMessage()));
			
			
		}
		finally {
			unlock(getContent());
		}
	}
	
	protected Content getContent() {
		return getModelObject();
	}
	
	protected WorkflowService getWorkflowService() {
		return getModelObject().getService(WorkflowService.class);
	}
	
	protected WebTask getTask() {
		return ((WebTask) getWorkflowModel().getObject().getTask());
	}
	
	protected Activity getRunningActivity() {
		List<Activity> activities = getWorkflowModel().getObject().getProcess().getActivities();
		Activity activity = !activities.isEmpty() && activities.get(0).isRunning() ? activities.get(0) : null;
		return activity;
	}

	protected Activity getTaskResolution() {
		return ((KbeeContext)getWorkflowModel().getObject()).getPreviousTaskResolution();	
	}
	
	protected Task getPreviousTask() {
		return ((KbeeContext)getWorkflowModel().getObject()).getPreviousTask();	
	}
	
	protected boolean validate(ManualEndCondition condition, boolean forcevalidation) {
		if (this.validate!=null && !forcevalidation) 
			return this.validate;
		
		getFeedbackMessages().clear(null);
		
		if (!validateUser()) {
			error(new FeedbackMessage(TaskPanel.this, "invalid user", FeedbackMessage.ERROR));
			return false;
		}
		
		for (ClassificationPanel<T> editor : getEditors()) {
			editor.validate();
		}

		if (condition.getPrecondition()!=null && !condition.getPrecondition().isEmpty()) {
			for (Validator validator : condition.getPrecondition()) {
				if (!validator.validate(getWorkflowModel().getObject())) {
					error(new FeedbackMessage(TaskPanel.this, validator.getMessage(), FeedbackMessage.ERROR));
				}
			}
		}
		
//		if (condition.getRequiredResources() && getModelObject().getService(ContentService.class).getActivityResources().isEmpty()) {
//			String message = TaskPanel.this.getLabel("task.error.resources").getObject();
//			error(new FeedbackMessage(TaskPanel.this, message, FeedbackMessage.ERROR));
//			validate = false;
//		}
		
		this.validate =	!getFeedbackMessages().hasMessage(FeedbackMessage.ERROR);
		
		return this.validate;
	}
	
	protected boolean validateUser() {
		if (getModelObject()!=null && 
				getModelObject().getWorkspace()!=null &&
				getModelObject().getWorkspace().equals(Long.valueOf(getSessionUser().getId())))
			return true;
		return false;
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
	
	protected void addListeners() {
		add(new WicketEventListener<NavigationEvent>() {
			public void onEvent(NavigationEvent event) {
				update(false);
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

	@SuppressWarnings("unchecked")
	protected void openAudit(AjaxRequestTarget target) {
		((AuditTrailModal<T>)get("audit-modal")).open(target, getModel());
	}
	
	protected List<EForm> getForms() {
		List<EForm> forms = new ArrayList<EForm>();
		for (EForm form : getTask().getForms()) {
			if (form.isVisible(getModelObject().getFormData(form))) {
			forms.add(form);
			}
		}
		if (getTask().getIncludeCallerForms() && getPreviousTask()!=null) {
			for (EForm form : ((WebTask)getPreviousTask()).getForms()) {
				forms.add(form);
			}
		}
		if (forms.isEmpty()) {
			forms.addAll(getDefaultForms());
		}
		return forms;
	}
	
	protected List<EForm> getDefaultForms() {
		List<EForm> forms = new ArrayList<EForm>();
//		List<ModelSection> sections;
//		WorkflowContext context = getWorkflowModel().getObject();
//		if (context.getTask()!=null && 
//				context.getTask() instanceof WebTask &&
//				!((WebTask)context.getTask()).getSections().isEmpty()) {
//			sections = ((WebTask)context.getTask()).getSections();
//		}
//		else {
//			sections = ((KbeeContext)context).getContent().getContentTemplate().getSections();
//		}	
//		boolean first = true;
//		for (ModelSection section : sections) {
//			forms.add(new KbeeDefaultForm(context, section, first));
//			first = false;
//		}
		forms.add(new KbeeDefaultForm(getContent().getContentTemplate()));
		return forms;
	}
	
	protected Panel getProcessChartPanel(String id) {
		if (!getProcedure().getPhases().isEmpty()) {
			return new ProcessChartPanel(id, getWorkflowModel());
		}
		else {
			return new InvisiblePanel(id);
		}
	}
	
	protected boolean isOverdue() {
	
		if (is_overdue!=null)
			return is_overdue.booleanValue(); 
		
		
		OffsetDateTime dueDate=getWorkflowModel().getObject().getDueDate();
		
		
				
				
		if (dueDate!=null) {
			if (getWorkflowModel().getObject().getDueDate().isBefore(OffsetDateTime.now())) {
				is_overdue =Boolean.valueOf(true);
				return  is_overdue.booleanValue();
			}
			
		}
		is_overdue =Boolean.valueOf(false);
		return  is_overdue.booleanValue();
	}
	
	protected Panel getOverduePanel(String id) {
		
		if (isOverdue()) {
			DateTimeService service = ServiceLocator.getService(DateTimeService.class);
			User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
			String zid = service.getMapZoneIds().get(user.getTimeZone());
			
			if (zid==null)
				zid=ZoneId.systemDefault().getId();
			
			if (getWorkflowModel().getObject().getDueDate().isBefore(OffsetDateTime.now())) {
				StringResourceModel s=new StringResourceModel("past-due", this, null);
				s.setParameters(
						ServiceLocator.getService(DateTimeService.class).format(
						getWorkflowModel().getObject().getDueDate(), 
						 ZoneId.of(zid).getId(),
						user.getLocale(),
						DateTimeService.DATE_COLlOQUIAL_AGO)
				);
				
				AlertPanel<T> a = new AlertPanel<T>(id, AlertPanel.WARNING, getModel(), 
						new StringResourceModel("past-due-title", this, null), s);
				a.setIcon("fa-duotone fa-calendar-days");
				return a;
			}
			else {
				if (getWorkflowModel().getObject().getDueDate().truncatedTo(ChronoUnit.DAYS).isEqual(OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS))) {
					StringResourceModel s=new StringResourceModel("due-today", this, null);
					s.setParameters(
							ServiceLocator.getService(DateTimeService.class).format(
							getWorkflowModel().getObject().getDueDate(), 
							 ZoneId.of(zid).getId(),
							user.getLocale(),
							DateTimeService.DATE_COLlOQUIAL_AGO)
					);
					AlertPanel<T> a = new AlertPanel<T>(id, AlertPanel.INFO, getModel(), new StringResourceModel("past-due-title", this, null), s);
					a.setIcon("fa-duotone fa-calendar-days");
					return a;
					
				}
			}
		}
		return new InvisiblePanel(id);
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected boolean isPending() {
		return getWorkflowModel().getObject().isPending();
	}
	
	protected boolean isTaskStarted() {
		return getWorkflowModel().getObject().getTime()!=null;
	}
	
	protected Panel getBillboard() {
		if (task_billboard ==null)
			task_billboard = new InvisiblePanel("task-billboard");
		return task_billboard;
	}
	
	protected ErrorDialog getErrorDialog() {
		return (ErrorDialog) get("error-dialog");
	}
	
	
	protected void onAfterCancelWorkflow(AjaxRequestTarget target) {
		fireScanAll(new NavigationEvent(target));
	}
	
	private void openCancelWorkflow(AjaxRequestTarget target) {
		((Dialog)TaskPanel.this.get("cancel-dialog")).open(target, new Dialog.Handler() {
			@Override
			public void onClick(AjaxRequestTarget target, Button button) {
				if (button.key().equals("dialog.cancel.button")) {
					TaskPanel.this.getModel().getObject().getService(WorkflowService.class).cancel();
						onAfterCancelWorkflow(target);
				}
			}
		}, getProcedure().getName(), getContentTitle());
	}
	
	private WebPage getPortalPreviewPage(IModel<T> model) {
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
	
	private void error(FeedbackMessage message) {
		getFeedbackMessages().add(message);
	}
	
	private void lock(Content content) {
		ServiceLocator.getService(ValueLockerService.class).lock(content.getId());
	}
	
	private void unlock(Content content) {
		ServiceLocator.getService(ValueLockerService.class).unlock(content.getId());
	}
}