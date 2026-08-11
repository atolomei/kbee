package kbee.web.idoc;


import java.time.OffsetDateTime;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.base.Content;
import com.novamens.content.document.IDoc;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.TokenService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Json;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.WorkflowContext;

import kbee.web.error.ErrorNotAuthorizedPanel;
import kbee.web.error.ErrorPanel;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.nav.CursorNavigationEvent;

import kbee.web.page.ApplicationMenuSection;
import kbee.web.workflow.task.TaskPage;
import kbee.web.workflow.util.WorkflowContextModel;

@SuppressWarnings("serial")
public class SharedTaskPage extends TaskPage<Content> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SharedTaskPage.class.getName());

	private String user;
	private IDocTaskPanelV6 panel;
	protected WebMarkupContainer modal_container;
	private OffsetDateTime expirationDate = null;
	
	/**
	 * 
	 */
	public SharedTaskPage(PageParameters parameters) {
		WorkflowContext context = getWorkflowContext(parameters);
		if (context!=null) {
		
			setContext(context);
			setExpirationDate(getExpirationDate(parameters));
			
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
	public SharedTaskPage(WorkflowContext context) {
		super(context);
		setUser( getSessionUser().getUserName());
	}
	
	
	public void onNavigate(Content content) {
//		try {
//			if (content.getWorkspace()!=null && content.getWorkspace()>0) {
//				Task task = content.getService(WorkflowService.class).getTask();
//				if (task==null)
//					throw  new KbeeRuntimeException("Task is null for content -> " + content.getDisplayName());
//				Page page = (TaskPage<Content>)((kbee.web.workflow.task.WebTask)task).getPage(content.getService(WorkflowService.class).getContext());
//				if (page instanceof NavigablePage<?>) 
//						((NavigablePage<Content>)page).setNavigator( getNavigator());
//				setResponsePage(page);
//			}
//			else {
//				// CursorListModel<Content>
//				Page page=(Page) ServiceLocator.getService(BeansService.class).getBean(getContentClass(content) + "-page" , new ObjectModel<Content>(content));
//				if (page instanceof NavigablePage<?>)
//					((NavigablePage<Content>)page).setNavigator(getNavigator());
//				setResponsePage(page);
//			}
//			
//		} catch (Exception e) {
//			logger.error(e);
//			setResponsePage( new ApplicationErrorPage<>(e));
//		}
	}
	
	/**
	 * 
	 */
	@Override
	public void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<CursorNavigationEvent<Content>>() {
			public void onEvent(CursorNavigationEvent<Content> event) {
				logger.debug(event.getClass().getName() + " -> " + ((Content) event.getModelObject()).getDisplayName());
				SharedTaskPage.this.onNavigate((Content) event.getModelObject());
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

		
//		add(new WicketEventListener<AuditTrailContentEvent<IDoc>>() {
//			@Override
//			public void onEvent(AuditTrailContentEvent<IDoc> event) {
//				logger.debug( event.toString());
//				getAuditModal().open(event.getRequestTarget(), event.getModel());
//				event.getRequestTarget().add(SharedTaskPage.this.getModalContainerMarkupContainer());
//			}
//		});
		
//		add(new WicketEventListener<ReassignToMeEvent<IDoc>>() {
//			@SuppressWarnings("unchecked")
//			@Override
//			public void onEvent(ReassignToMeEvent<IDoc> event) {
//				try {
//					User user = ((com.novamens.kbee.content.workflow.KbeeContext) getContentWorkflowModel(event.getModel()).getObject()).getUser();
//					
//					if (!user.equals(getSessionUser())) {
//						
//						String note = getLabel("monitor.reassign", getSessionUser().getFirstLastName(), getSessionUser().getFirstLastName()).getObject();
//						event.getModel().getObject().getService(WorkflowService.class).reassign(getSessionUser(), note);
//
//						if (event.getRequestTarget()!=null) {
//							// event.getRequestTarget().add(IDocTaskPageV6.this.getModalContainerMarkupContainer());
//							event.getRequestTarget().add(SharedTaskPage.this);
//							FeedbackHelper.showInfoToast( event.getModel().getObject().getDisplayName() + " - " + note );
//						}
//						else {
//							SharedTaskPage page = new SharedTaskPage(SharedTaskPage.this.getWorkflowModel().getObject(), false);
//							page.setNavigator(((TaskPage<IDoc>) getPage()).getNavigator());
//							setResponsePage(page);
//						}
//					}
//				} 
//				catch (Exception e) {
//					logger.error(e);
//					setResponsePage (new ApplicationErrorPage<IDoc>(e, SharedTaskPage.this.getModel()));
//				}
//			}
//		});
	
//		add(new WicketEventListener<ReassignEvent<IDoc>>() {
//			@Override
//			public void onEvent(ReassignEvent<IDoc> event) {
//				try {
//					AssignationModal<IDoc> modal = getAssignModal();
//					String title = event.getModel().getObject().getTitle();
//					IModel<WorkflowContext> model = getContentWorkflowModel(event.getModel());
//					Task task = model.getObject().getTask();
//					modal.open( event.getRequestTarget(), model, new Modal.Handler() {
//						@SuppressWarnings("unchecked")
//						@Override
//						public void onClick(AjaxRequestTarget target, com.novamens.wicket.markup.html.modal.Modal.Button button) {
//							if (button.isSubmit()) {
//
//								Content content=((KbeeContext) getWorkflowModel().getObject()).getContent();
//								
//								if (content.getWorkspace().equals( getSessionUser().getId()) ) {
//									SharedTaskPage page = new SharedTaskPage(SharedTaskPage.this.getWorkflowModel().getObject(), false);
//									page.setNavigator(((TaskPage<IDoc>) getPage()).getNavigator());
//									setResponsePage(page);
//									return;
//								}
//								
//								Navigator<Content> navigator=((TaskPage<IDoc>) getPage()).getNavigator();
//								
//								// logger.debug(navigator.getClass().getName());
//								
//								
//								if ( 	navigator !=null && 
//										navigator.getCursor()!=null) {
//									try {	
//											if (navigator.getCursor().hasMoreElements()) {
//												fire (new GeneralWicketEvent("navigate-next"));
//												return;
//											}
//											else if ((navigator.getIndex()>0)) {
//												fire ( new GeneralWicketEvent("navigate-previous"));
//												return;
//											}
//											else {
//												SharedTaskPage page = new SharedTaskPage(SharedTaskPage.this.getWorkflowModel().getObject(), false);
//												page.setNavigator(((TaskPage<IDoc>) getPage()).getNavigator());
//												setResponsePage(page);
//												return;
//											}
//									} catch (Exception e) {
//										logger.error(e);
//										setResponsePage(new ApplicationErrorPage<>(e));
//									}
//								}
//								else {
//									setResponsePage( new RedirectPage("/home"));
//								}
//								
//								
//								//FeedbackHelper.showInfoToast( event.getModel().getObject().getDisplayName());
//								//target.add(IDocTaskPageV6.this.panel);
//							}
//						}
//					}, ((KbeeTask)task).getEnabledGroups(), title);
//					
//				} 
//				catch (Exception e) {
//					logger.error(e);
//					fire (new ErrorEvent<>(event.getRequestTarget(), e));
//				}
//				
//				//event.getRequestTarget().add(SharedTaskPage.this.getModalContainerMarkupContainer());
//			}
//		});
		
		
		
//		add(new WicketEventListener<ShareContentEvent<IDoc>>() {
//			@Override
//			public void onEvent(ShareContentEvent<IDoc> event) {
//				getShareModal().open(event.getRequestTarget(), event.getModel());
//				event.getRequestTarget().add(SharedTaskPage.this.getModalContainerMarkupContainer());
//			}
//		});
//		
		
	}

	
	
//	public AuditTrailModal<IDoc> getAuditModal() {
//		if (audit_modal==null) {
//			audit_modal = new AuditTrailModal<IDoc>("audit-trail-modal");
//			getModalContainerMarkupContainer().addOrReplace(audit_modal);
//		}
//		return this.audit_modal;
//	}
//	
//	public ShareModal<IDoc> getShareModal() {
//		if (share_modal==null) {
//			share_modal = new ShareModal<IDoc>("send-email-modal");
//			getModalContainerMarkupContainer().addOrReplace(share_modal);
//		}
//		return this.share_modal;
//	}
//	
//	public ErrorDialog getErrorDialog()  {
//		if (error_modal==null) {
//			error_modal = new ErrorDialog("error-dialog");
//			getModalContainerMarkupContainer().addOrReplace(error_modal);
//		}
//		return this.error_modal;
//	}
	
	
//	public ConfirmationDialog getConfirmationDialog()  {
//		if (confirmation_modal==null) {
//			confirmation_modal = new ConfirmationDialog("confirmation-dialog");
//			getModalContainerMarkupContainer().addOrReplace(confirmation_modal);
//		}
//		return this.confirmation_modal;
//	}
//	
	
//	public  AssignationModal<IDoc> getAssignModal()  {
//		if (assign_modal==null) {
//			assign_modal = new  AssignationModal<IDoc>("assign-modal");
//			
//			getModalContainerMarkupContainer().addOrReplace(assign_modal);
//		}
//		return this.assign_modal;
//	}
	

	public String getUser() {
		return user;
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		try {
			ServiceLocator.getService(SecurityService.class).authenticate(getUser());
		}
		catch (Exception e) {
			addOrReplace(new ErrorPanel("editor", new Model<String>("eform"), new Model<String>("content or form not found or access denied.")));
		}
	}
	
	@Override
	public void onAfterRender() {
		super.onAfterRender();
	}

	
	@Override
	public void onDetach() {
		super.onDetach();
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();

		try {
			
			try {
				ServiceLocator.getService(SecurityService.class).authenticate(getUser());
			}
			catch (Exception e) {
				addOrReplace(new ErrorPanel("editor", new Model<String>("eform"), new Model<String>("content or form not found or access denied.")));
				return;
			}

			if (getSessionUser()==null) {
				addOrReplace(new ErrorPanel("editor", new Model<String>("eform"), new Model<String>("User is null")));
				return;
			}
			
			try {
				if (getExpirationDate()!=null && OffsetDateTime.now().isAfter(getExpirationDate())) {
					addOrReplace(new ErrorPanel("editor", new Model<String>("eform"), new Model<String>("page expired.")));
					return;
				}
			}
			catch (Exception e) {
				addOrReplace(new ErrorPanel("editor", new Model<String>("eform"), new Model<String>("content or form not found or access denied.")));
				return;
			}

			
			if (getModel()==null) {
				addOrReplace( new ErrorPanel("editor", getLabel("not-found.title"), getLabel("not-found.message")));
				return;
			}	
			
			
			setPageTitle(new Model<String>(getModel().getObject().getTitle()));
			
			setTopNavigation(new SharedContentTopBar("navigation", getModel()));
			
			
			if (getWorkflowModel()==null || getWorkflowModel().getObject()==null)
				throw new IllegalArgumentException("Workflow can not be null");
			
			
			if (!ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(((KbeeContext) getWorkflowModel().getObject()).getContent())) {
				addOrReplace(new ErrorNotAuthorizedPanel<>("editor", new Model<String>("Not authorized")));
				return;
			}
			
			setLogVisit(true);

			setEditionEnabled(getRunningActivity()!=null && getRunningActivity().getUser().getId().equals(getSessionUser().getId()));
			
			this.panel =new IDocTaskPanelV6(getWorkflowModel()) {
				@Override
				public boolean isEditionEnabled() {
					return SharedTaskPage.this.isEditionEnabled();
				}
				@Override
				public void setEditionEnabled(boolean value) {
					SharedTaskPage.this.setEditionEnabled(value);
				}
				@Override
				public boolean isReadOnly() {
					return SharedTaskPage.this.isReadOnly();
				}
				@Override
				protected Panel getBreadCrumb() {
					return new InvisiblePanel("breadcrumb");
				}
//				@Override
//				public void onAfterRender() {
//					super.onAfterRender();
//					HttpServletRequest request = ((HttpServletRequest) getRequest().getContainerRequest());
//					HttpServletResponse response = ((HttpServletResponse) getResponse().getContainerResponse());
//					Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//					SecurityContextLogoutHandler h = new SecurityContextLogoutHandler();
//					h.logout(request, response, auth);
//					
//				}
			};
			addOrReplace(panel);
			
			panel.setInitialTab(getInitialTab());
		
		} 
		catch (Exception e) {
			logger.error(e);
			addOrReplace( new ErrorPanel("editor", e));
		}
	}

	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.TASK;
	}
	
	protected void setUser(String user) {
		this.user = user;
	}

	
	//
	// Class<IDoc> contentclass = (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	//

	
	protected WorkflowContext getWorkflowContext(PageParameters parameters) {
		
		try {
			String id = null, task = null;
			WorkflowContext context = null;
			IDoc content = null;
			StringValue token = parameters.get("token");
			if (!token.isNull() && !token.isEmpty()) {
				Json data = ServiceLocator.getService(TokenService.class).decode(token.toString());
				if (data!=null) {
					id = (String)data.get("content");
					task = (String)data.get("task");
					setUser((String)data.get("user"));
				}
			}
			if (id!=null && task!=null) {
				content = (IDoc)getContentDao().findContentById(IDoc.class, Long.valueOf(id.toString()));
				if (content!=null) {
					WorkflowService workflowService = content.getService(WorkflowService.class);
					if (workflowService!=null && workflowService.getTask()!=null) {
						String task_id = workflowService.getTask().getId().replaceAll("\\s", "-").toLowerCase();
						if (task_id.equals(task.toString()))
							context = workflowService.getContext();
					}
				}
			}
			return context;
		} 
		catch (Exception e) { 
			logger.error(e);
			return null;
		}
	}
	
	
	protected OffsetDateTime getExpirationDate(PageParameters parameters) {
		try {
			String date = null;		
			OffsetDateTime expiration = null;
			StringValue token = parameters.get("token");
			if (!token.isNull() && !token.isEmpty()) {
				Json data = ServiceLocator.getService(TokenService.class).decode(token.toString());
				if (data!=null && data.get("expiration")!=null) {
					date = (String)data.get("expiration");
					ServiceLocator.getService(DateTimeService.class).parseStrDate(date);
				}
			}
			return expiration;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
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
	
	
	@Override
	protected boolean hasLateralMenu() {
		return false;
	}

	private void setExpirationDate(OffsetDateTime expirationDate) {
		this.expirationDate= expirationDate;
		
	}

	private OffsetDateTime getExpirationDate() {
		return this.expirationDate;
	}
		

}




//private  AuditTrailModal<IDoc> audit_modal = null;
//private  ShareModal<IDoc> share_modal		 = null;
//private  ErrorDialog error_modal;
//private  AssignationModal<IDoc> assign_modal;

//protected String getContentClass(Content content) { 
//return Proxy.getClassName(content).toLowerCase();
//}
