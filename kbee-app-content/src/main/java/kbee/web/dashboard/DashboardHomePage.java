package kbee.web.dashboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;
import com.novamens.content.form.EForm;
import com.novamens.content.library.Library;
import com.novamens.content.library.LibraryService;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.service.UrlService;
import com.novamens.content.user.UserProfileType;
import com.novamens.content.user.UserService;
import com.novamens.content.web.workflow.markup.AssignationModal;
import com.novamens.content.workflow.ContentProcedure;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.wicket.markup.html.event.AuditTrailContentEvent;
import com.novamens.kbee.wicket.markup.html.event.DeleteContentEvent;
import com.novamens.kbee.wicket.markup.html.event.ReassignEvent;
import com.novamens.kbee.wicket.markup.html.event.ReassignToMeEvent;
import com.novamens.kbee.wicket.markup.html.event.ShareContentEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.security.Identifiable;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.XArray;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.ErrorDialog;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Process;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;

import kbee.web.content.panel.ShareModal;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.object.AuditTrailModal;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.panel.FlagPanel;
import kbee.web.panel.ToastPanel;
import kbee.web.workflow.task.TaskPage;
import kbee.web.workflow.util.WorkflowContextModel;


@SuppressWarnings("serial")
public class DashboardHomePage extends DashboardPage<Person> {
	
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DashboardHomePage.class.getName());
	
	static final public String PROPERTY_UNREAD = "unread";
	static final String KEY = "home";

	final boolean is_root = ServiceLocator
		.getService(SecurityService.class)
		.isRoot();
	final boolean is_admin = is_root || 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_domain_admin = ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_monitor = ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.MONITOR_AUDIT.getId());
	final boolean is_support = ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_workspace = ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.WORKSPACE.getId());
	final boolean is_notifications = ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.NOTIFICATIONS.getId());
	final boolean is_pending = ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.PENDING_TASKS.getId());
	

	
	private List<IModel<XArray>> list_ef;
	private List<IModel<XArray>> list_bp;
	
	private  AuditTrailModal<Content> audit_modal = null;
	private  ShareModal<Content> share_modal		 = null;
	private  ErrorDialog error_modal;
	private  ConfirmationDialog confirmation_modal;
	private  AssignationModal<Content> assign_modal;
	
	/**
	 * 
	 */
	public DashboardHomePage() {
		add(new RefreshBehavior());
	}

	public IModel<String> getTitle() {
		return  new Model<String>(getDomain().getOrganization());
	}
	
	@Override
	public void addListeners() {
		super.addListeners();
	
		add(new WicketEventListener<ErrorEvent<?>>() {
			@Override
			public void onEvent(ErrorEvent<?> event) {
				FeedbackHelper.showErrorToast( 
					event.getThrowable()!=null? event.getThrowable().getClass().getName() : 
					(event.getModel()!=null ? event.getModel().getObject().toString() : "Error"), 
					event.getThrowable()!=null? event.getThrowable().getMessage() : 
						(event.getModel()!=null ? event.getModel().getObject().toString() : "Error")
					);
			}
		});
			
		add(new WicketEventListener<DeleteContentEvent<Content>>() {
			@Override
			public void onEvent(DeleteContentEvent<Content> event) {
				logger.debug( event.toString());
				if ( event.getModel().getObject().getWorkspace()==null || ! event.getModel().getObject().getWorkspace().equals(getSessionUser().getId())) {
					getErrorDialog().open( event.getRequestTarget(), new Model<String>("File No longer in Workspace"));
					event.getRequestTarget().add(DashboardHomePage.this.getModalContainerMarkupContainer());
					return;
				}
				
				getConfirmationDialog().open (
					event.getRequestTarget(), 
					getLabel("cancelconfirmation", 
							event.getModel().getObject().getTitle(), 
							getTask(event.getModel()).getName()),
						
					Dialog.Delete, 
					new Dialog.Handler() {
						@Override
						public void onClick(AjaxRequestTarget target, Button button) {
							if (button.key().equals(Dialog.Delete.key())) {
								 event.getModel().getObject().getService(WorkflowService.class).cancel();
								 setResponsePage( new DashboardHomePage());
								}
							}
					});
				
				event.getRequestTarget().add(DashboardHomePage.this.getModalContainerMarkupContainer());
			}
		});
		
		add(new WicketEventListener<AuditTrailContentEvent<Content>>() {
			@Override
			public void onEvent(AuditTrailContentEvent<Content> event) {
				getAuditModal().open(event.getRequestTarget(), event.getModel());
				event.getRequestTarget().add(DashboardHomePage.this.getModalContainerMarkupContainer());
			}
		});
		
		add(new WicketEventListener<ReassignToMeEvent<Content>>() {
				@Override
				public void onEvent(ReassignToMeEvent<Content> event) {
					User user = ((com.novamens.kbee.content.workflow.KbeeContext) getWorkflowModel(event.getModel()).getObject()).getUser();
					if (!user.equals(getSessionUser())) {
						String note = getLabel("monitor.reassign", getSessionUser().getFirstLastName(), getSessionUser().getFirstLastName()).getObject();
						event.getModel().getObject().getService(WorkflowService.class).reassign(getSessionUser(), note);
						event.getRequestTarget().add(DashboardHomePage.this.getModalContainerMarkupContainer());
						FeedbackHelper.showInfoToast( event.getModel().getObject().getDisplayName() + " - " + note );
					}
				}
			});
		
		add(new WicketEventListener<ReassignEvent<Content>>() {
			@Override
			public void onEvent(ReassignEvent<Content> event) {
				try {
					AssignationModal<Content> modal = getAssignModal();
					String title = event.getModel().getObject().getTitle();
					IModel<WorkflowContext> model = getWorkflowModel(event.getModel());
					Task task = model.getObject().getTask();
					modal.open( event.getRequestTarget(), model, new Modal.Handler() {
						@Override
						public void onClick(AjaxRequestTarget target, com.novamens.wicket.markup.html.modal.Modal.Button button) {
							if (button.isSubmit()) {
								setResponsePage( new DashboardHomePage());
							}
						}
					}, ((KbeeTask)task).getEnabledGroups(), title);
					
				} 
				catch (Exception e) {
					fire (new ErrorEvent<>(event.getRequestTarget(), e));
				}
				event.getRequestTarget().add(DashboardHomePage.this.getModalContainerMarkupContainer());
			}
		});


		add(new WicketEventListener<ShareContentEvent<Content>>() {
			@Override
			public void onEvent(ShareContentEvent<Content> event) {
				getShareModal().open(event.getRequestTarget(), event.getModel());
				event.getRequestTarget().add(DashboardHomePage.this.getModalContainerMarkupContainer());
			}
		});
	}

	@Override
	public void onDetach() {
		super.onDetach();

		if (list_ef!=null)
			list_ef.forEach(item-> item.detach());
		
		if (list_bp!=null)
			list_bp.forEach(item-> item.detach());
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.HOME;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		getModalContainerMarkupContainer().add(new InvisiblePanel("assign-modal"));
		getModalContainerMarkupContainer().add(new InvisiblePanel("audit-trail-modal"));
		getModalContainerMarkupContainer().add(new InvisiblePanel("send-email-modal"));
		getModalContainerMarkupContainer().add(new InvisiblePanel("error-dialog"));
		getModalContainerMarkupContainer().add(new InvisiblePanel("confirmation-dialog"));
	}
	

    @Override
    public void onBeforeRender() {
        if (!hasBeenRendered() && 
        		Session.get()
        	       .getFeedbackMessages()
        	       .hasMessage(message -> true)) {
            addOrReplace(new ToastPanel("feedback"));
        }
        super.onBeforeRender();
    }    
    
    @Override
    public void onAfterRender() {
        super.onAfterRender();
        Session.get().getFeedbackMessages().clear();
    }
	
	/**
 	 * 1st RIGHT
 	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	private List<WidgetFactory> getRightSectionsPanels() {

		List<WidgetFactory> widgets = new ArrayList<WidgetFactory>();

		if (is_workspace) {
			widgets.add(new WidgetFactory() {
				public MarkupContainer getWidget(String id) {
					return new DashboardWidgetFileFactoryPanel(id, DashboardHomePage.KEY) {
						@Override
						protected void onStart(Process process) {
							Content content = ((KbeeContext)process.getContext()).getContent();
							setResponsePage( new RedirectPage(content.getService(UrlService.class).getUrl()));
						}
					};
				}	
				public IModel<String> getLabel() {
					return DashboardHomePage.this.getLabel("factory");
				}
			});
		}

		
		widgets.add(new WidgetFactory() {
			public MarkupContainer getWidget(String id) {
				return new DashboardWidgetAccountPanel("panel", "user-access");
			}	
			public IModel<String> getLabel() {
				return  new StringResourceModel("user-account", DashboardHomePage.this, null);
			}
		});
		

		if (is_admin || is_monitor) { 
			widgets.add(new WidgetFactory() {
				public MarkupContainer getWidget(String id) {
					return new DashboardWidgetHomeToolsPanel("panel", "home-tools");
				}	
				public IModel<String> getLabel() {
					return  new StringResourceModel("tools", DashboardHomePage.this, null);
				}
			});
		}
		
		
		
		return widgets;
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	public List<IModel<XArray>> geteFormsItems() {
		
		if (this.list_ef!=null)
			return this.list_ef;
		
		this.list_ef = new ArrayList<IModel<XArray>>();
		
		for (ContentTemplate con: getContentDao().getContentTemplates(getDomain())) {

			if (con.getState()==ObjectState.ENABLED) {
		
				for (EForm e: con.getForms()) {
					if (e!=null ) {
						String eform_id; 
						if (e instanceof Identifiable)
							eform_id= ((Identifiable) e).getId().toString();
						else
							eform_id=e.getName();
						
						String eform_name   = e.getDisplayName();
						
						
						StringBuilder str = new StringBuilder();
						
						str.append( "<span class=\"highlight\">"  + con.getDisplayName() + " </span> " + " - ");
						str.append(eform_name);
						
						if (e.getFormAccessLevel()!=null) 
							str.append(" (" + e.getFormAccessLevel().getDisplayName()+")");
						
						XArray da= new XArray(    str.toString(),  
												  str.toString(),
												  "",
												  con.getDisplayName()+" "+e.getDisplayName(),
												  eform_id,
												  "/eform/"+ con.getId().toString()+"/" + eform_id
											);
						this.list_ef.add(new Model<XArray>(da));
					}
				}
			}
		}
		
		
		this.list_ef.sort(new Comparator<IModel<XArray>>() {
			@Override
			public int compare(IModel<XArray> a, IModel<XArray> b) {
				try {					
					return a.getObject().getSortLabel().compareToIgnoreCase(b.getObject().getSortLabel());
				} catch (Exception e) {
					return 0;	
				}
			}
		});
		
		return this.list_ef;
	}
	

	public List<IModel<XArray>> getBPItems() {
		
		if (this.list_bp!=null)
			return this.list_bp;
		
		this.list_bp = new ArrayList<IModel<XArray>>();
		
		//List<ProcessLauncher> p_list = getLaunchers();
		List<Procedure> procedures = getProcedures();

		for (Procedure procedure: procedures) {
			ContentTemplate template = ((ContentProcedure)procedure).getContentTemplate();
			if (template!=null && template.getState()==ObjectState.ENABLED) {
				String id = procedure.getId().toString();
				//String lau= p.getId().toString(); 
						XArray da= new XArray(   
						"<span class=\"highlight\">"+template.getDisplayName() +"</span><span class=\"ago\"> - </span><span> " + procedure.getDisplayName()+"</span>",
						 "", //procedure.getLabel(),
						 "",
						 procedure.getDescription(),
						 procedure.getId().toString(),
						"/model/procedure/"+id
						);
				list_bp.add(new Model<XArray>(da));
			}
		}
		
		this.list_bp.sort(new Comparator<IModel<XArray>>() {
			@Override
			public int compare(IModel<XArray> a, IModel<XArray> b) {
				try {
					return a.getObject().getDisplayName().compareToIgnoreCase(b.getObject().getDisplayName());
				} catch (Exception e) {
					return 0;	
				}
			}
		});
		
		return this.list_bp;
	}


	protected WebTask getTask(IModel<Content> model) {
		return (WebTask)model.getObject().getService(WorkflowService.class).getTask();
	}

	
	public AuditTrailModal<Content> getAuditModal() {
		if (audit_modal==null) {
			audit_modal = new AuditTrailModal<Content>("audit-trail-modal");
			getModalContainerMarkupContainer().addOrReplace(audit_modal);
		}
		return this.audit_modal;
	}
	
	public ShareModal<Content> getShareModal() {
		if (share_modal==null) {
			share_modal = new ShareModal<Content>("send-email-modal");
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
	
	
	public  AssignationModal<Content> getAssignModal()  {
		if (assign_modal==null) {
			assign_modal = new  AssignationModal<Content>("assign-modal");
			
			getModalContainerMarkupContainer().addOrReplace(assign_modal);
		}
		return this.assign_modal;
	}
	
	
	protected IModel<WorkflowContext> getWorkflowModel(IModel<Content> model) {
		WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
		if (workflowService!=null) {
			WorkflowContext workflowcontext = workflowService.getContext();
			IModel<WorkflowContext> workflowmodel  =  new WorkflowContextModel<Content>(workflowcontext);
			return workflowmodel;
		}
		else
			return null;
	}
	protected Library getLibrary() {
		Library library = getDomain().getService(LibraryService.class).getDefault();
		return  library;
	}
	
	@Override
	protected boolean hasPermissions() {
		return getDomain()!=null;
	}
	
	protected void addWidgets() {
		
		addWidget(new ListView<WidgetFactory>("widget-left", getLeftSectionsPanels()) {
			protected void populateItem(ListItem<WidgetFactory> item){
				item.addOrReplace(getWidget(item.getModelObject()));
				item.detach();
			}
		});	
		addWidget(new ListView<WidgetFactory>("widget-center", getCenterSectionsPanels()) {
			protected void populateItem(ListItem<WidgetFactory> item){
				item.addOrReplace(getWidget(item.getModelObject()));
				item.detach();
			}
		});	
		
	 
	}

	protected void onSiteClick(IModel<Site> modelObject) {
	}

	@Override
	protected Panel getBreadcrumbPanel() {
		return new FlagPanel("breadcrumb");
	}
	
	@SuppressWarnings("unchecked")
	protected void onClick(IModel<Content> model) {
		WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
		try {
			TaskPage<Content> page = null;
			if (workflowService.getTask()!=null && workflowService.getContext().getProcess().isRunning()) {
				Task task = workflowService.getTask();
				page = (TaskPage<Content>)((WebTask)task).getPage(workflowService.getContext());
				if (model.getObject().getWorkspace()>0) {
					if (getSessionUser().getId().toString().equals(model.getObject().getWorkspace().toString())) {
						page.setEditionEnabled(true);
						page.setReadOnly(false);
					}
					else {
						page.setEditionEnabled(false);
						page.setReadOnly(true);
					}
				}
				else {
					page.setEditionEnabled(false);
					page.setReadOnly(true);
				}
			}
			if (page==null)
				throw new IllegalArgumentException("page is null");
			setResponsePage(page);
		} 
		catch (Exception e) {
			logger.error(e);
			setResponsePage( new ApplicationErrorPage<>(e));
		}
	}

	@Override
	protected String getPageKey() {
		return KEY;
	}

	
	/** -----------------------------------------------
	 * 1st LEFT
	 */
	private List<WidgetFactory> getLeftSectionsPanels() {

		List<WidgetFactory> widgets = new ArrayList<WidgetFactory>();
	
		
		 
		if (is_notifications && isWorkflowProfile()) { 
			widgets.add(new WidgetFactory() {
				public MarkupContainer getWidget(String id) {
					return new DashboardNotificationsWidgetPanel("panel", "my-notfications");
				}	
				public IModel<String> getLabel() {
					return DashboardHomePage.this.getLabel("mynotifications");
				}
			});
		}
		
		if (is_workspace) {
			widgets.add(new WidgetFactory() {
				public MarkupContainer getWidget(String id) {
					return new DashboardMyTasksWidgetPanel(id, DashboardHomePage.KEY);
				}
				public IModel<String> getLabel() {
					return DashboardHomePage.this.getLabel("mytasks");
				}
			});
		}
		
		if (!isWorkflowProfile()) {
			widgets.add(new WidgetFactory() {
				public MarkupContainer getWidget(String id) {
					return new DashboardMyDocumentsWidgetPanel(id, DashboardHomePage.KEY);
				}
				public IModel<String> getLabel() {
					return DashboardHomePage.this.getLabel("mydocuments");
				}
			});
		}
		
		if (isWorkflowProfile() && (is_domain_admin || is_root || is_monitor || is_support)) {
			widgets.add(new WidgetFactory() {
				public MarkupContainer getWidget(String id) {
					return new DashboardMonitorTasksWidgetPanel(id, DashboardHomePage.KEY);
				}
				public IModel<String> getLabel() {
					return DashboardHomePage.this.getLabel("monitor");
				}
			});
		}	
			
		if (isWorkflowProfile()) {
			widgets.add(new WidgetFactory() {
				public MarkupContainer getWidget(String id) {
					return new DashboardLibraryWidgetPanel(id, DashboardHomePage.KEY);
				}
				public IModel<String> getLabel() {
					return DashboardHomePage.this.getLabel("library");
				}
			});
		}	
		
		return widgets;
	}
	

	/** --------------------
	 * 
 	 * CENTER
	 * @return
	 * 
	 */
	private List<WidgetFactory> getCenterSectionsPanels() {

		List<WidgetFactory> widgets = new ArrayList<WidgetFactory>();

		if (is_workspace) {
			widgets.add(new WidgetFactory() {
				public MarkupContainer getWidget(String id) {
					return new DashboardWidgetFileFactoryPanel(id, DashboardHomePage.KEY) {
						@Override
						protected void onStart(Process process) {
							try {
								Content content = ((KbeeContext)process.getContext()).getContent();
								setResponsePage( new RedirectPage(content.getService(UrlService.class).getUrl()));
							} catch (Exception e) {
								logger.error(e);
								setResponsePage( new ApplicationErrorPage<>(e));
							}
						}
					};
				}	
				public IModel<String> getLabel() {
					return DashboardHomePage.this.getLabel("factory");
				}
			});
		}

		
		if ( isWorkflowProfile() && (getDataSets().size()>0)) {  
			widgets.add(new WidgetFactory() {
				public MarkupContainer getWidget(String id) {
					return new DashboardDatasetEntititesWidgetPanel(id, DashboardHomePage.KEY);
				}	
				public IModel<String> getLabel() {
					return DashboardHomePage.this.getLabel("entities");
				}
			});
		}

		if (isWorkflowProfile() && 
			(role_dataset_members || role_dataset_members_write || role_federated_values)) {
			widgets.add(new WidgetFactory() {
				@Override
				public MarkupContainer getWidget(String id) {
					return new DashboardDatasetMembersWidgetPanel(id, DashboardHomePage.KEY);
				}	
				@Override
				public IModel<String> getLabel() {
					return DashboardHomePage.this.getLabel("datasetmembers");
				}
			});
		}
		
		
		widgets.add(new WidgetFactory() {
			public MarkupContainer getWidget(String id) {
				return new DashboardWidgetAccountPanel("panel", "user-access");
			}	
			public IModel<String> getLabel() {
				return  new StringResourceModel("user-account", DashboardHomePage.this, null);
			}
		});

		
		if (isWorkflowProfile() && (is_root || is_admin || is_monitor)) { 
			widgets.add(new WidgetFactory() {
				public MarkupContainer getWidget(String id) {
					return new DashboardWidgetHomeToolsPanel("panel", "home-tools");
					
				}	
				public IModel<String> getLabel() {
					return  new StringResourceModel("tools", DashboardHomePage.this, null);
				}
				
			});
		}
		
		return widgets;
	}
	
	private boolean isWorkflowProfile() {
		UserProfileType userProfileType =  ServiceLocator.getService(UserService.class).getSessionUserProfile().getType();
		return userProfileType==null || userProfileType==UserProfileType.WORKFLOW_PARTICIPANT;
	}
	
	private List<Procedure> getProcedures() {
		return getDomain().getService(WorkflowDomainService.class).getProcedures();
	}
}

