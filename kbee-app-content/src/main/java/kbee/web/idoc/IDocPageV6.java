package kbee.web.idoc;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.document.IDoc;
import com.novamens.content.form.EFormAccessLevel;
import com.novamens.content.form.EForm;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.UrlService;
//import com.novamens.content.web.console.markup.ErrorPanel;
import com.novamens.content.web.content.markup.CustomAttributesPanel;
import com.novamens.content.web.idoc.markup.ContentPageV6;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.indexer.query.Cursor;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.event.AuditTrailContentEvent;
import com.novamens.kbee.wicket.markup.html.event.ClickBackEvent;
import com.novamens.kbee.wicket.markup.html.event.ShareContentEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.HeaderMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.markup.html.tabs.SeparatorTabKB;
import com.novamens.wicket.markup.html.tabs.TitleTabKB;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Task;

import kbee.web.content.console.ContentBasePage;
import kbee.web.content.console.MonitorPage;
import kbee.web.content.console.WorkspaceConsole;
import kbee.web.content.console.WorkspacePage;
import kbee.web.content.eform.ContentFormViewer;
import kbee.web.content.panel.ContentLibraryPanel;
import kbee.web.content.panel.ContentLinksPanel;
import kbee.web.content.panel.ShareModal;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPanel;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.model.object.AuditTrailObjectPanel;
import kbee.web.nav.ContentBaseBC;
import kbee.web.nav.CursorNavigationEvent;
import kbee.web.nav.ErrorNavigationBar;
import kbee.web.nav.NavigablePage;
import kbee.web.nav.Navigator;
import kbee.web.nav.NavigatorPanelV6;
import kbee.web.object.AuditTrailModal;
import kbee.web.panel.ClickItemEvent;
import kbee.web.searcher.panel.SearcherDetailHeaderPanel;
import kbee.web.searcher.panel.SearcherDetailMainPanel;
import kbee.web.workflow.ProcessHistoryPanel;
import kbee.web.workflow.task.TaskPage;

@SuppressWarnings("serial")
public class IDocPageV6 extends ContentPageV6<IDoc> {

	private static final long serialVersionUID = 1L;
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(IDocPageV6.class.getName());
	
	private  AuditTrailModal<Content> audit_modal = null;
	private  ShareModal<Content> share_modal	  = null;

	protected final boolean root		   = ServiceLocator.getService(SecurityService.class).isRoot();
	protected final boolean role_admin     = root || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	protected WebMarkupContainer modal_container;
	List<EForm> forms;
	
	
	public IDocPageV6() {
	}

	public IDocPageV6(PageParameters parameters) {
		IDoc idoc = getContent(parameters);
		if (idoc!=null)
			 setModel(new ObjectModel<IDoc>(idoc));
	}

	public IDocPageV6(IModel<IDoc> model) {
		super(model);
	}
	
	public void onDetach() {
		super.onDetach();
		this.forms=null;
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();

		try {
			
			if (getModel()==null || getModel().getObject()==null) 
				throw new IllegalArgumentException("Model can not be null");
					
			this.modal_container = new WebMarkupContainer("modal-container"); 
			this.modal_container.setOutputMarkupId(true);
			addOrReplace(modal_container);
		
			getModalContainerMarkupContainer().add(new InvisiblePanel("audit-trail-modal"));
			getModalContainerMarkupContainer().add(new InvisiblePanel("send-email-modal"));
	
			/** this page is not using hasPermissions */
			
			if (!ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(getModel().getObject())) {
				setTopNavigation(new ErrorNavigationBar<IDoc>("navigation"));
				setMenu(new InvisiblePanel("menu"));
				addOrReplace(new ErrorPanel("tabs", (new ContentBaseBC()).getLabel(), new Model<String>("content not found or access denied.")));
				return;
			}
				
			setLogVisit(true);
			setPageTitle(new Model<String>(getModel().getObject().getTitle()));
					
			setTopNavigation(getMainTopbar());
			setMenu(getMainLaternalMenu());
					
			List<ITab> tabs = new ArrayList<ITab>();
					
			
			/**
			 * 
			 */
			
			if (getForms().size()>0)
				tabs.add(new TitleTabKB("structure", new StringResourceModel("content"),  "title-no-nav"));
			
			
			for (EForm eform: getForms()) {
				try {
					tabs.add(new AbstractTabKB(new Model<String>(eform.getDisplayName()), eform.getName()) {
						@Override
						public Panel getPanel(String panelId) {
							return new ContentFormViewer<IDoc>(panelId, getModel(), eform);
						}
					});
				}
				catch (Throwable e) {
					logger.error(e);
					IModel<String> title = eform!=null? new Model<String>(eform.getDisplayName()): new Model<String>("null");
					tabs.add(new AbstractTabKB(title, title.getObject()) {
						@Override
						public Panel getPanel(String panelId) {
							String message = e.getCause()!=null ? e.getCause().getMessage() : e.getMessage();
							return new ErrorPanel(panelId, new Model<String>("Error"), new Model<String>(message));
						}
					});
				}
			}
		
			if (getForms().size()==0) {
				tabs.add(new AbstractTabKB(getLabel("tab.main"), "main") {
					@Override
					public Panel getPanel(String panelId) {
						return  new SearcherDetailMainPanel<IDoc>(panelId, getModel(), getSiteModel(), true);
					}
				});
			}
		
			if (getModel().getObject().getContentTemplate().isCustomAttributes()) {
				tabs.add(new AbstractTabKB(new Model<String>(getModel().getObject().getContentTemplate().getCustomattributes_label()), "customtags") {
					@Override
					public Panel getPanel(String panelId) {
						return new CustomAttributesPanel<IDoc>(panelId, getModel() );
					}
				});
			}
			
			
			
			/** --------------------------------
			 *	Support
			 */
			
			boolean isTitleSupport = getModel().getObject().getContentTemplate().isInstanceTimeBasedNotification() && isAdmin();
			
			if (isTitleSupport) {
				tabs.add(new SeparatorTabKB("separator-notes"));
				tabs.add(new TitleTabKB("support", getLabel("tab.supporting"),  "title-no-nav"));
				
				tabs.add(new AbstractTabKB(getLabel("time-based-notifications"), "timedalerts") {
						@Override
						public Panel getPanel(String panelId) {
							ContentReminderPanel<IDoc> panel = new  ContentReminderPanel<IDoc>(panelId, IDocPageV6.this.getModel());
							return panel;
						}
					});
			}
			
			
			
			

			
			
			tabs.add(new SeparatorTabKB("separator-audit"));
			tabs.add(new TitleTabKB("audit", getLabel("audit"),  "title-no-nav"));

			
			
			if (getModelObject().getService(ContentService.class).getText()!=null) {
				tabs.add(new AbstractTabKB(getLabel("tab.links"), "links") {
					@Override
					public Panel getPanel(String panelId) {
						return new ContentLinksPanel<IDoc>(panelId, new ObjectModel<IDoc>((IDoc)getModelObject()));
					}
				});
			}
			
			tabs.add(new AbstractTabKB(getLabel("tab.versioncontrol"), "version") {
				@Override
		 		public Panel getPanel(String panelId) {
		 			return  new ContentLibraryPanel<IDoc>(panelId, getModel(), getSiteModel(), isConsole());
		 		}
		 	});
		 			
		 	tabs.add(new AbstractTabKB(getLabel("tab.history"), "history") {
				@Override
		 		public Panel getPanel(String panelId) {
		 			return new ProcessHistoryPanel<IDoc>(panelId, getModel(), getActivities());
		 		}
		 	});
		 		
		 	if (isWriteable(getModel())) {
		 		tabs.add(new AbstractTabKB(getLabel("tab.audit"), "audit") {
					@Override
 					public Panel getPanel(String panelId) {
 						return  new AuditTrailObjectPanel<IDoc>(panelId, getModel());
					}
		 		});
		 	}
		 	
 			VerticalLayout<ITab> panel = new VerticalLayout<ITab>("tabs", "content", tabs);
 			panel.setTitle(new StringResourceModel("sections", this, null));
 			panel.setSections(VerticalLayout.COLS_9X3);
		
 			try {
		 				if (getNavigator()!=null) {
		 					NavigatorPanelV6<Content> na = new NavigatorPanelV6<Content>("panel", getNavigator(), Content.class);
			 				na.setResultsPanel(true);
			 				SearcherDetailHeaderPanel<IDoc> hpanel=new SearcherDetailHeaderPanel<IDoc>("page-content-header", getModel(), getSiteModel(), na, true);
			 				hpanel.add(new AttributeModifier("class", "page-header"));
			 				setPageContentHeader(hpanel);
			 			}
			 			else {
			 				SearcherDetailHeaderPanel<IDoc> hpanel=new SearcherDetailHeaderPanel<IDoc>("page-content-header", getModel(), getSiteModel(), null, true);
			 				hpanel.add(new AttributeModifier("class", "page-header"));
			 				setPageContentHeader(hpanel);
			 			}
			} 
			catch (Exception e) {
						logger.error(e);
		 				SearcherDetailHeaderPanel<IDoc> hpanel=new SearcherDetailHeaderPanel<IDoc>("page-content-header", getModel(), getSiteModel(), null, true);
		 				hpanel.add(new AttributeModifier("class", "page-header"));
		 				setPageContentHeader(hpanel);
 			}
 			panel.setMenuItemFactory(getMenuItems());
 			panel.setContentPanelCss("main-area detail text");
 			add(panel);
		} 
		catch (Exception e) {
			logger.error(e);
			addOrReplace( new ErrorPanel("tabs", e));
		}
	}

	@SuppressWarnings("unchecked")
	public void onNavigate(Content content) {
		try {
			if (content.getWorkspace()!=null && content.getWorkspace()>0) {
				Task task = content.getService(WorkflowService.class).getTask();
				
				if (task==null)
					throw new KbeeRuntimeException("Task is null for content -> " + content.getDisplayName());
				
				Page page = (TaskPage<Content>)((com.novamens.kbee.content.workflow.WebTask)task).getPage(content.getService(WorkflowService.class).getContext());
				
				if (page instanceof NavigablePage<?>) 
						((NavigablePage<Content>)page).setNavigator( getNavigator() );
				
				setResponsePage(page);
			}
			else {
				Page page=(Page) ServiceLocator.getService(BeansService.class).getBean(getContentClass(content) + "-page" , new ObjectModel<Content>(content));
				if (page instanceof NavigablePage<?>) { 
					if (getNavigator()!=null)
						((NavigablePage<Content>)page).setNavigator(getNavigator() );
				}
				setResponsePage(page);
			}
			
		} 
		catch (Exception e) {
			logger.error(e);
			setResponsePage( new ApplicationErrorPage<>(e));
		}
	}
	
	@Override
	public void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<ClickBackEvent<Content>>() {
			@Override
			public void onEvent(ClickBackEvent<Content> event) {
				if (IDocPageV6.this.getModel().getObject().getWorkspace()!=null) {

					if (getSource()==WorkspaceConsole.NAME) {
						setResponsePage( new WorkspacePage());
						return;
					}
					else {
						setResponsePage( new MonitorPage());
						return;
					}
				}
				else
					setResponsePage( new ContentBasePage());
			}
		});
		
		add(new WicketEventListener<ClickItemEvent<Content>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(ClickItemEvent<Content> event) {
				try {
					logger.debug(event.toString());
					Content content = event.getModel().getObject();
		
					if (!(content instanceof IDoc)) {
						logger.error("TBA ASSUMES CONTENT IS IDOC !!!");
						return;
					}
					// TBA ASSUMES CONTENT IS IDOC
					if (content.getWorkspace()!=null) {
						IDocTaskPageV6 page = new IDocTaskPageV6(content.getService(WorkflowService.class).getContext(), false);
						Navigator<Content> navigator= getNavigator();
						navigator.getCursor().setIndex(event.getIndex());
						page.setNavigator(navigator);
						setResponsePage(page);
						getPage().detach();
						return;
					}
					else {
						IDocPageV6 page = new IDocPageV6( new ObjectModel<IDoc>( (IDoc) content));
						page.setNavigator(getNavigator());
						getNavigator().getCursor().setIndex(event.getIndex());
						setResponsePage(page);
						getPage().detach();
						return;
					}
				} 
				catch (Exception e) {
					logger.error(e);
					
				}
			}
		});
		
		add(new WicketEventListener<ClickEvent<IDoc>>() {
			@Override
			public void onEvent(ClickEvent<IDoc> event) {
				logger.debug( event.toString());
			}
		});
		
		add(new WicketEventListener<CursorNavigationEvent<Content>>() {
			public void onEvent(CursorNavigationEvent<Content> event) {
				IDocPageV6.this.onNavigate((Content) event.getModelObject());
				event.detach();
			}
		});
		
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
		
		add(new WicketEventListener<AuditTrailContentEvent<Content>>() {
			@Override
			public void onEvent(AuditTrailContentEvent<Content> event) {
				getAuditModal().open(event.getRequestTarget(), event.getModel());
				event.getRequestTarget().add(IDocPageV6.this.getModalContainerMarkupContainer());
			}
		});
		
		add(new WicketEventListener<ShareContentEvent<Content>>() {
			@Override
			public void onEvent(ShareContentEvent<Content> event) {
				getShareModal().open(event.getRequestTarget(), event.getModel());
				event.getRequestTarget().add(IDocPageV6.this.getModalContainerMarkupContainer());
			}
		});
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

	
	@Override
	protected boolean hasLateralMenu() {
		return true;
	}
	
	private List<MenuItemFactory<Panel>> getMenuItems() {
	
		List<MenuItemFactory<Panel>> list = new ArrayList<MenuItemFactory<Panel>>();
		
		if (getLaunchers().size()>0) {
			list.add(id ->
				new HeaderMenuItemPanelV5<Panel>(id) {
					@Override
					public String getLabel() {
						return new StringResourceModel("edit", this, null).getObject();
					}
			});
				
			for (int p=0; p<getLaunchers().size(); p++) {
				int p_i = p;
				list.add(new MenuItemFactory<Panel>() {
					@Override
					public AbstractMenuItemPanelV5<Panel> getItem(String id) {
						return new MenuItemPanelV5<Panel>(id) {
							@Override
							public void onClick() {
								try {
									ProcessLauncher launcher = getLaunchers().get(p_i);
									Content content = IDocPageV6.this.getModel().getObject().getService(ContentService.class).checkout();
									content.getService(WorkflowService.class).startProcess(launcher.getProcedure());
									setResponsePage(new RedirectPage(content.getService(UrlService.class).getTaskUrl()));
								} 
								catch (Exception e) {
									logger.error(e.getClass().getName() + "| Checkout in ContentBaseConsole contextual menu" );
									setResponsePage(new ApplicationErrorPage<>(e));
								}
							}
							@Override
							public String getLabel() {	
								return  IDocPageV6.this.getLabel("checkout").getObject()+ " - " + getLaunchers().get(p_i).getDisplayName();
							}
							@Override 
							public boolean isVisible() {
								if (isReadOnly())
									return false;
								if (IDocPageV6.this.getModelObject().isArchived())
									return false;
								if (IDocPageV6.this.getModelObject().isRecycled())
									return false;
								return true;
							}
							@Override 
							public boolean isEnabled() {
								if (isReadOnly() || getDomain()==null)
									return false;
								return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(IDocPageV6.this.getModelObject()) &&
									 IDocPageV6.this.getModelObject().isHeadVersion() 	&&
									!IDocPageV6.this.getModelObject().isLocked() 		&&
									!IDocPageV6.this.getModelObject().isRecycled() 		&&
									!IDocPageV6.this.getModelObject().isArchived();
							}
						};	
					}	
				});
			}
				
			list.add(new MenuItemFactory<Panel>() {
				@Override
				public AbstractMenuItemPanelV5<Panel> getItem(String id) {
					return new SeparatorMenuItemPanelV5<Panel>(id) {
						@Override
						public String getCssClass() {
							return "divider";
						}
						@Override
						public boolean isVisible() {
								return true;
						}
					};
				}
			});
		}
		
		list.add(new MenuItemFactory<Panel>() {
			@Override
			public AbstractMenuItemPanelV5<Panel> getItem(String id) {
				return new MenuItemPanelV5<Panel>(id) {
					@Override
					public void onClick() {
						try {
							// new WorkspacePage();
						} 
						catch (Exception e) {									
							logger.error(e.getClass().getName() + "archive in ContentBaseconsole contextual menu" );
						}
					}
					@Override
					public String getLabel() {	
						return  IDocPageV6.this.getLabel("modal.sendbyemail.title").getObject();
					}
				};	
			}	
		});
		
		list.add(new MenuItemFactory<Panel>() {
			@Override
			public AbstractMenuItemPanelV5<Panel> getItem(String id) {
				return new AjaxMenuItemPanelV5<Panel>(id) {
					public void onClick(AjaxRequestTarget target) {
						if (!IDocPageV6.this.getModel().getObject().isLocked()) {
							try {
								IDocPageV6.this.getModel().getObject().getService(ContentService.class).recycle();
								
							} catch (ContentMgmtException | ServiceNotFoundException e) {
								logger.error(e, (getSessionUser()!=null?getSessionUser().getUserName():"null"));
								fire (new ErrorEvent<>(target, e));
							}
						}
						setResponsePage( new ContentBasePage());
					}
					@Override 
					public String getLabel() {
						return IDocPageV6.this.getLabel("delete").getObject();
					}
					@Override
					public String getWorkingLabel() {
						return IDocPageV6.this.getLabel("working").getObject();
					}
					@Override
					public boolean isEnabled() {
						return !IDocPageV6.this.getModel().getObject().isLocked() && isDeletable(IDocPageV6.this.getModel());
					}
				};
			}
		});		
		
		return list;
	}
	
	protected boolean isConsole() {
		return true;
	}
	
	
	
	protected List<EForm> getForms() {
	
		if (forms!=null)
			return forms;
		
		forms = new ArrayList<EForm>();
		for (EForm form : getModelObject().getContentTemplate().getForms()) {
			if (form!=null && form.getFormAccessLevel().equals(EFormAccessLevel.GENERAL)) {
				if (form.isVisible(getModelObject().getFormData(form))) {
					forms.add(new KbeeTaskForm(form));
				}
			}
			else if (form!=null && form.getFormAccessLevel().equals(EFormAccessLevel.INTERNAL_INFO) && isInternalInfoReadable()) {
				forms.add(new KbeeTaskForm(form));
			}
		}
		return forms;
	}
	
	protected boolean isInternalInfoReadable() {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isPrivateEnabled(getModelObject(), getSessionUser());
	}

	protected  WebMarkupContainer getModalContainerMarkupContainer() {
		return modal_container;
	}

	@SuppressWarnings("unchecked")
	protected Page getTaskPage(IModel<Content> model, IModel<Cursor> cursor) {
		try {
			WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
			Task task = workflowService.getTask();
			TaskPage<Content> page = (TaskPage<Content>)((WebTask)task).getPage(workflowService.getContext());
			page.setNavigator(getNavigator());
			return page;
		} 
		catch (Exception e) {
			logger.error(e, (getSessionUser()!=null?getSessionUser().getUserName():"null"));
			return new kbee.web.error.ApplicationErrorPage<Void>(e);
		}
	}
	
	private List<ProcessLauncher> getLaunchers() {
		if (getDomain()==null)
			return  new ArrayList<ProcessLauncher>();
		return getDomain().getService(WorkflowDomainService.class)==null ? new ArrayList<ProcessLauncher>() :
			getDomain().getService(WorkflowDomainService.class).getContextLaunchers(IDocPageV6.this.getModelObject());
	}
	
	protected boolean isAdmin() {
		return this.role_admin;
	}
}