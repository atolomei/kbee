package kbee.web.idoc;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.document.IDoc;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.web.workflow.markup.AssignationModal;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Proxy;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.wicket.markup.html.event.AuditTrailContentEvent;
import com.novamens.kbee.wicket.markup.html.event.ClickBackEvent;
import com.novamens.kbee.wicket.markup.html.event.EmailSentEvent;
import com.novamens.kbee.wicket.markup.html.event.GeneralWicketEvent;
import com.novamens.kbee.wicket.markup.html.event.ReassignEvent;
import com.novamens.kbee.wicket.markup.html.event.ReassignToMeEvent;
import com.novamens.kbee.wicket.markup.html.event.ShareContentEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.Identifiable;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.markup.html.modal.ErrorDialog;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;

import kbee.web.content.console.MonitorPage;
import kbee.web.content.console.PendingTasksConsole;
import kbee.web.content.console.PendingTasksPage;
import kbee.web.content.console.WorkspaceConsole;
import kbee.web.content.console.WorkspacePage;
import kbee.web.content.panel.ShareModal;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorNotAuthorizedPanel;
import kbee.web.error.ErrorPanel;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.nav.CursorNavigationEvent;
import kbee.web.nav.NavigablePage;
import kbee.web.nav.Navigator;

import kbee.web.object.AuditTrailModal;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.workflow.task.TaskPage;
import kbee.web.workflow.util.WorkflowContextModel;

@SuppressWarnings("serial")
public class IDocTaskPageV6 extends TaskPage<IDoc> {
				
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(IDocTaskPageV6.class.getName());

	private IDocTaskPanelV6 panel;
	protected WebMarkupContainer modal_container;
	
	private  AuditTrailModal<IDoc> audit_modal = null;
	private  ShareModal<IDoc> share_modal		 = null;
	private  ErrorDialog error_modal;
	private  ConfirmationDialog confirmation_modal;
	private  AssignationModal<IDoc> assign_modal;
	

	
	public IDocTaskPageV6(PageParameters parameters) {
		WorkflowContext context = getWorkflowContext(parameters);
		if (context!=null && ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(((KbeeContext)context).getContent())) {
			setContext(context);
			StringValue tab = parameters.get("tab");
			if (!tab.isNull() && !tab.isEmpty()) {
				setInitialTab(tab.toString());
			}
		}
	}

	/** 
	 * @param context
	 * @param select_preference
	 */
	public IDocTaskPageV6(WorkflowContext context, boolean select_preference) {
		super(context);
	}
	
	
	@SuppressWarnings("unchecked")
	public void onNavigate(Content content) {
		try {
			if (content.getWorkspace()!=null && content.getWorkspace()>0) {
				Task task = content.getService(WorkflowService.class).getTask();
				if (task==null)
					throw  new KbeeRuntimeException("Task is null for content -> " + content.getDisplayName());
				Page page = (TaskPage<Content>)((com.novamens.kbee.content.workflow.WebTask)task).getPage(content.getService(WorkflowService.class).getContext());
				if (page instanceof NavigablePage<?>) 
						((NavigablePage<Content>)page).setNavigator( getNavigator());
				setResponsePage(page);
			}
			else {
				// CursorListModel<Content>
				Page page=(Page) ServiceLocator.getService(BeansService.class).getBean(getContentClass(content) + "-page" , new ObjectModel<Content>(content));
				if (page instanceof NavigablePage<?>)
					((NavigablePage<Content>)page).setNavigator(getNavigator());
				setResponsePage(page);
			}
			
		} catch (Exception e) {
			logger.error(e);
			setResponsePage( new ApplicationErrorPage<>(e));
		}
	}
	
	/**
	 * 
	 */
	@Override
	public void addListeners() {
		super.addListeners();
		
		
		add(new WicketEventListener<EmailSentEvent<IDoc>>() {
			@Override
			public void onEvent(EmailSentEvent<IDoc> event) {
				if ((event.getModel() != null) && (event.getModel().getObject() instanceof Identifiable)) {
					FeedbackHelper.showInfoToast(event.getClass().getSimpleName(),  ((Identifiable) event.getModel().getObject()).getDisplayName());
				}
				else {
					FeedbackHelper.showInfoToast(event.getClass().getSimpleName());
								
				}
				event.getRequestTarget().add(IDocTaskPageV6.this.getModalContainerMarkupContainer());
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				if (event instanceof EmailSentEvent)
					return true;
				return false;
			}
		});

		
		add(new WicketEventListener<ClickBackEvent<IDoc>>() {

			@Override
			public void onEvent(ClickBackEvent<IDoc> event) {

				if (IDocTaskPageV6.this.getModel().getObject().getWorkspace()!=null) {
					if (getSource()==null) {
						setResponsePage( new MonitorPage());
						return;
					}
					if (getSource().equals(WorkspaceConsole.NAME)) {
							setResponsePage(new WorkspacePage());
							return;
					}
					if (getSource().equals(PendingTasksConsole.NAME)) {
						setResponsePage(new PendingTasksPage());
						return;
					}
					else {
						setResponsePage( new MonitorPage());
						return;
					}
				}
				setResponsePage( new MonitorPage());
			}
		});
		
		/**
		 * 
		 */
		add(new WicketEventListener<CursorNavigationEvent<Content>>() {
			public void onEvent(CursorNavigationEvent<Content> event) {
				logger.debug(event.getClass().getName() + " -> " + ((Content) event.getModelObject()).getDisplayName());
				IDocTaskPageV6.this.onNavigate((Content) event.getModelObject());
				event.detach();
			}
		});
		
		add(new WicketEventListener<ErrorEvent<IDoc>>() {
			@Override
			public void onEvent(ErrorEvent<IDoc> event) {
				FeedbackHelper.showErrorToast( 
					event.getThrowable()!=null? event.getThrowable().getClass().getName() : 
					(event.getModel()!=null ? event.getModel().getObject().toString() : "Error"), 
					event.getThrowable()!=null? event.getThrowable().getMessage() : 
						(event.getModel()!=null ? event.getModel().getObject().toString() : "Error")
					);
			}
		});

		
		add(new WicketEventListener<AuditTrailContentEvent<IDoc>>() {
			@Override
			public void onEvent(AuditTrailContentEvent<IDoc> event) {
				logger.debug( event.toString());
				getAuditModal().open(event.getRequestTarget(), event.getModel());
				event.getRequestTarget().add(IDocTaskPageV6.this.getModalContainerMarkupContainer());
			}
		});

		
		
		add(new WicketEventListener<ReassignToMeEvent<IDoc>>() {
			@SuppressWarnings("unchecked")
			@Override
			public void onEvent(ReassignToMeEvent<IDoc> event) {

				try {
					User user = ((com.novamens.kbee.content.workflow.KbeeContext) getContentWorkflowModel(event.getModel()).getObject()).getUser();
					
					if (!user.equals(getSessionUser())) {
						
						String note = getLabel("monitor.reassign", getSessionUser().getFirstLastName(), getSessionUser().getFirstLastName()).getObject();
						event.getModel().getObject().getService(WorkflowService.class).reassign(getSessionUser(), note);

						if (event.getRequestTarget()!=null) {
							event.getRequestTarget().add(IDocTaskPageV6.this);
							FeedbackHelper.showInfoToast( event.getModel().getObject().getDisplayName() + " - " + note );
						}
						else {
							IDocTaskPageV6 page = new IDocTaskPageV6(IDocTaskPageV6.this.getWorkflowModel().getObject(), false);
							page.setNavigator(((TaskPage<IDoc>) getPage()).getNavigator());
							setResponsePage(page);
						}
					}
				} catch (Exception e) {
					logger.error(e);
					setResponsePage (new ApplicationErrorPage<IDoc>(e, IDocTaskPageV6.this.getModel()));
				}
			}
		});
	
		add(new WicketEventListener<ReassignEvent<IDoc>>() {
			@Override
			public void onEvent(ReassignEvent<IDoc> event) {
				try {
					AssignationModal<IDoc> modal = getAssignModal();
					String title = event.getModel().getObject().getTitle();
					IModel<WorkflowContext> model = getContentWorkflowModel(event.getModel());
					Task task = model.getObject().getTask();
					modal.open( event.getRequestTarget(), model, new Modal.Handler() {
						@SuppressWarnings("unchecked")
						@Override
						public void onClick(AjaxRequestTarget target, com.novamens.wicket.markup.html.modal.Modal.Button button) {
							if (button.isSubmit()) {

								Content content=((KbeeContext) getWorkflowModel().getObject()).getContent();
								
								if (content.getWorkspace().equals( getSessionUser().getId()) ) {
									IDocTaskPageV6 page = new IDocTaskPageV6(IDocTaskPageV6.this.getWorkflowModel().getObject(), false);
									page.setNavigator(((TaskPage<IDoc>) getPage()).getNavigator());
									setResponsePage(page);
									return;
								}
								Navigator<Content> navigator=((TaskPage<IDoc>) getPage()).getNavigator();
								if ( 	navigator !=null && 
										navigator.getCursor()!=null) {
									try {	
											if (navigator.getCursor().hasMoreElements()) {
												fire (new GeneralWicketEvent("navigate-next"));
												return;
											}
											else if ((navigator.getIndex()>0)) {
												fire ( new GeneralWicketEvent("navigate-previous"));
												return;
											}
											else {
												IDocTaskPageV6 page = new IDocTaskPageV6(IDocTaskPageV6.this.getWorkflowModel().getObject(), false);
												page.setNavigator(((TaskPage<IDoc>) getPage()).getNavigator());
												setResponsePage(page);
												return;
											}
									} catch (Exception e) {
										logger.error(e);
										setResponsePage(new ApplicationErrorPage<>(e));
									}
								}
								else {
									setResponsePage( new RedirectPage("/home"));
								}
							}
						}
					}, ((KbeeTask)task).getEnabledGroups(), title);
					
				} 
				catch (Exception e) {
					logger.error(e);
					fire (new ErrorEvent<>(event.getRequestTarget(), e));
				}
				
				event.getRequestTarget().add(IDocTaskPageV6.this.getModalContainerMarkupContainer());
			}
		});
		
		
		
		add(new WicketEventListener<ShareContentEvent<IDoc>>() {
			@Override
			public void onEvent(ShareContentEvent<IDoc> event) {
				getShareModal().open(event.getRequestTarget(), event.getModel());
				event.getRequestTarget().add(IDocTaskPageV6.this.getModalContainerMarkupContainer());
			}
		});
		
		
	}

	
	
	public AuditTrailModal<IDoc> getAuditModal() {
		if (audit_modal==null) {
			audit_modal = new AuditTrailModal<IDoc>("audit-trail-modal");
			getModalContainerMarkupContainer().addOrReplace(audit_modal);
		}
		return this.audit_modal;
	}
	
	public ShareModal<IDoc> getShareModal() {
		if (share_modal==null) {
			share_modal = new ShareModal<IDoc>("send-email-modal");
			getModalContainerMarkupContainer().addOrReplace(share_modal);
		}
		return this.share_modal;
	}
	
	public ErrorDialog getErrorDialog()  {
		if (error_modal==null) {
			error_modal = new ErrorDialog("error-dialog");
			getModalContainerMarkupContainer().addOrReplace(error_modal);
		}
		return this.error_modal;
	}
	
	
	public ConfirmationDialog getConfirmationDialog()  {
		if (confirmation_modal==null) {
			confirmation_modal = new ConfirmationDialog("confirmation-dialog");
			getModalContainerMarkupContainer().addOrReplace(confirmation_modal);
		}
		return this.confirmation_modal;
	}
	
	
	public  AssignationModal<IDoc> getAssignModal()  {
		if (assign_modal==null) {
			assign_modal = new  AssignationModal<IDoc>("assign-modal");
			getModalContainerMarkupContainer().addOrReplace(assign_modal);
		}
		return this.assign_modal;
	}

	
	
	@Override
	public void onDetach() {
		super.onDetach();
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();

		modal_container = new WebMarkupContainer("modal-container"); 
		modal_container.setOutputMarkupId(true);
		addOrReplace(modal_container);

		getModalContainerMarkupContainer().add(new InvisiblePanel("assign-modal"));
		getModalContainerMarkupContainer().add(new InvisiblePanel("audit-trail-modal"));
		getModalContainerMarkupContainer().add(new InvisiblePanel("send-email-modal"));
		getModalContainerMarkupContainer().add(new InvisiblePanel("error-dialog"));
		getModalContainerMarkupContainer().add(new InvisiblePanel("confirmation-dialog"));
		
		
		try {
			setTopNavigation(getMainTopbar());
			setMenu(getMainLaternalMenu());

			if (getModel()==null || getModel().getObject()==null)
				throw new IllegalArgumentException("Model can not be null");
			
			if (getWorkflowModel()==null || getWorkflowModel().getObject()==null)
				throw new IllegalArgumentException("Workflow can not be null");
			
			setPageTitle(new Model<String>(getModel().getObject().getTitle()));
			
			if (!ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(((KbeeContext) getWorkflowModel().getObject()).getContent())) {
				addOrReplace(new ErrorNotAuthorizedPanel<>("editor", new Model<String>("Please ask admin user to grant permissions to this page.")));
				return;
			}
			
			setLogVisit(true);

			setEditionEnabled(getRunningActivity()!=null && getRunningActivity().getUser().getId().equals(getSessionUser().getId()));
			
			this.panel =new IDocTaskPanelV6(getWorkflowModel()) {
				@Override
				public boolean isEditionEnabled() {
					return IDocTaskPageV6.this.isEditionEnabled();
				}
				@Override
				public void setEditionEnabled(boolean value) {
					IDocTaskPageV6.this.setEditionEnabled(value);
				}
				@Override
				public boolean isReadOnly() {
					return IDocTaskPageV6.this.isReadOnly();
				}
			};
			addOrReplace(panel);
			
			panel.setInitialTab(getInitialTab());
		
		} catch (Exception e) {
			logger.error(e);
			addOrReplace( new ErrorPanel("editor", e));
		}
	}

	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.TASK;
	}
	
	protected IModel<WorkflowContext> getContentWorkflowModel(IModel<IDoc> model) {
		WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
		if (workflowService!=null) {
			WorkflowContext workflowcontext = workflowService.getContext();
			IModel<WorkflowContext> workflowmodel  =  new WorkflowContextModel<IDoc>(workflowcontext);
			return workflowmodel;
		}
		else
			return null;
	}
	protected  WebMarkupContainer getModalContainerMarkupContainer() {
		return modal_container;
	}

	protected String getContentClass(Content content) {
		return Proxy.getClassName(content).toLowerCase();
	}
}