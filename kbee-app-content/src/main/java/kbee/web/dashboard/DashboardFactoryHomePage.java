package kbee.web.dashboard;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;
import com.novamens.content.service.AppMonitoringService;
import com.novamens.kbee.wicket.markup.html.areainfo.GridInfoPanel;
import com.novamens.kbee.wicket.markup.html.event.GeneralWicketEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.markup.html.modal.ErrorDialog;
import com.novamens.wicket.markup.html.tabs.AjaxTabbedPanel;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.DummyBlockPanel;

import kbee.web.content.panel.ShareModal;
import kbee.web.help.InlineHelpWebService;
import kbee.web.object.AuditTrailModal;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.panel.FlagPanel;

public class DashboardFactoryHomePage extends DashboardPage<Person> {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DashboardHomePage.class.getName());

	static final String KEY = "home-factory";

	final boolean is_root=ServiceLocator.getService(SecurityService.class).isRoot();
	final boolean is_admin = is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_domain_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_monitor = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.MONITOR_AUDIT.getId());
	final boolean is_support = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());

	private  AuditTrailModal<Content> audit_modal 	= null;
	private  ShareModal<Content> share_modal	 	= null;
	private  ErrorDialog error_modal;
	private  ConfirmationDialog confirmation_modal;
	
	/**
	 * 
	 */
	public DashboardFactoryHomePage() {
		add(new RefreshBehavior());
	}
	
	public IModel<String> getTitle() {
		return  new Model<String>(getDomain().getOrganization());
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
	public void addListeners() {
		super.addListeners();
		add(new WicketEventListener<GeneralWicketEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(GeneralWicketEvent event) {
				logger.debug( event.getName());
			}
		});
		
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.HOME;
	}


	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	protected void addWidgets() {
		addWidget(new ListView<WidgetFactory>("widget-left", getLeftSectionsPanels()) {
			private static final long serialVersionUID = 1L;
			protected void populateItem(ListItem<WidgetFactory> item){
				item.addOrReplace(getWidget(item.getModelObject()));
				item.detach();
			}
		});	

		addWidget(new ListView<WidgetFactory>("widget-right", getRightSectionsPanels()) {
			private static final long serialVersionUID = 1L;
			protected void populateItem(ListItem<WidgetFactory> item){
				item.addOrReplace(getWidget(item.getModelObject()));
				item.detach();
			}
		});
	}

	/** -----------------------------------------------
	 * 1st LEFT
	 */
	 
	private List<WidgetFactory> getLeftSectionsPanels() {
	
		List<WidgetFactory> widgets = new ArrayList<WidgetFactory>();

		widgets.add(new WidgetFactory() {
			public MarkupContainer getWidget(String id) {
				DashboardWidgetSimpleWrapperPanel<Person> wr = new DashboardWidgetSimpleWrapperPanel<Person>("panel", new ObjectModel<Person>(getPerson()), DashboardFactoryHomePage.KEY);
				List<ITab> tabs = new ArrayList<ITab>();
				tabs.add(new AbstractTab(DashboardFactoryHomePage.this.getLabel("metrics")) {
					@Override
					public Panel getPanel(String panelId) {
						GridInfoPanel p=new GridInfoPanel(panelId,  ServiceLocator.getService(AppMonitoringService.class).keyMetricsInfo(), null, true);
						p.setPanelLink(getServerUrl()+"/systeminfo/keymetrics");
						p.setPanelLinkLabel(DashboardFactoryHomePage.this.getLabel("metrics"));
						return (p);
						
					}
				});
				
				
				tabs.add(new AbstractTab(DashboardFactoryHomePage.this.getLabel("subsystems")) {
						@Override
						public Panel getPanel(String panelId) {
							return (new GridInfoPanel(panelId,  ServiceLocator.getService(AppMonitoringService.class).serversInfo(), null, true));
						}
				});
				
				
				tabs.add(new AbstractTab(DashboardFactoryHomePage.this.getLabel("hardware")) {
					@Override
					public Panel getPanel(String panelId) {
						GridInfoPanel p=new GridInfoPanel(panelId,  ServiceLocator.getService(ApplicationServerService.class).infrastructureInfo(), null, true);
						p.setPanelLink(getServerUrl()+"/systeminfo/hardware");
						p.setPanelLinkLabel(DashboardFactoryHomePage.this.getLabel("hardware"));
						return (p);
				}
			
				});

				tabs.add(new AbstractTab(DashboardFactoryHomePage.this.getLabel("scheduler")) {
					@Override
					public Panel getPanel(String panelId) {
						GridInfoPanel p=new GridInfoPanel(panelId,  ServiceLocator.getService(ApplicationServerService.class).schedulerInfo(), null, true);
						p.setPanelLink(getServerUrl()+"/datamanagement/scheduler");
						p.setPanelLinkLabel(DashboardFactoryHomePage.this.getLabel("scheduler"));
						return (p);
				}
				});


				tabs.add(new AbstractTab(DashboardFactoryHomePage.this.getLabel("vault")) {
					@Override
					public Panel getPanel(String panelId) {
						GridInfoPanel p=new GridInfoPanel(panelId,  ServiceLocator.getService(AppMonitoringService.class).vaultInfo(), null, true);
						//p.setPanelLink(getServerUrl()+"/systeminfo/parameters");
						//p.setPanelLinkLabel(DashboardFactoryHomePage.this.getLabel("system-parameters"));
						return (p);
				}
				});
				

				
				
				tabs.add(new AbstractTab(DashboardFactoryHomePage.this.getLabel("system-monitor")) {
					@Override
					public Panel getPanel(String panelId) {
						GridInfoPanel p=new GridInfoPanel(panelId,  ServiceLocator.getService(AppMonitoringService.class).pingMonitorInfo(), null, true);
						p.setPanelLink(getServerUrl()+"/systeminfo/parameters");
						p.setPanelLinkLabel(DashboardFactoryHomePage.this.getLabel("system-parameters"));
						return (p);
				}
				});
				
				
				
				
				AjaxTabbedPanel<ITab> panel = new AjaxTabbedPanel<ITab>("payload",tabs) {
					private static final long serialVersionUID = 1L;
					protected String getNavCss() {
						return "nav nav-buttons";
					}
				};
				wr.setSimplePayloadPanel(panel);
				wr.setHelpKey(InlineHelpWebService.FACTORY_HOME_SERVER_INFO);
				wr.setTitle(DashboardFactoryHomePage.this.getLabel("system-info"));
				return wr;
				
			}

			@Override
			public IModel<String> getLabel() {
				return DashboardFactoryHomePage.this.getLabel("system-info");
			}	
		});
		

		widgets.add(new WidgetFactory() {
			public MarkupContainer getWidget(String id) {
				DashboardWidgetSimpleWrapperPanel<Person> wr = new DashboardWidgetSimpleWrapperPanel<Person>("panel", new ObjectModel<Person>(getPerson()), DashboardFactoryHomePage.KEY);
				
				wr.setHelpKey(InlineHelpWebService.FACTORY_HOME_SERVER_INFO);
				wr.setTitle(DashboardFactoryHomePage.this.getLabel("data-management"));
				
				List<ITab> tabs = new ArrayList<ITab>();
				
				tabs.add(new AbstractTab(DashboardFactoryHomePage.this.getLabel("database")) {
					private static final long serialVersionUID = 1L;
						@Override
						public Panel getPanel(String panelId) {
							GridInfoPanel p=new GridInfoPanel(panelId,  ServiceLocator.getService(AppMonitoringService.class).databaseInfo(), null, true);
							p.setPanelLink(getServerUrl()+"/systeminfo/database");
							p.setPanelLinkLabel(DashboardFactoryHomePage.this.getLabel("database"));
							return p;

							
							
						}
					});
				
				tabs.add(new AbstractTab(DashboardFactoryHomePage.this.getLabel("object-storage")) {
					@Override
					public Panel getPanel(String panelId) {
						GridInfoPanel p=new GridInfoPanel(panelId,  ServiceLocator.getService(AppMonitoringService.class).KBFSInfo(), null, true);
						p.setPanelLink(getServerUrl()+"/datamanagement/objectstorage");
						p.setPanelLinkLabel(DashboardFactoryHomePage.this.getLabel("object-storage"));
						return p;
					}
				});
				
				tabs.add(new AbstractTab(DashboardFactoryHomePage.this.getLabel("search")) {
					@Override
					public Panel getPanel(String panelId) {
						
						GridInfoPanel p=new GridInfoPanel(panelId,  ServiceLocator.getService(AppMonitoringService.class).searchInfo(), null, true);
						p.setPanelLink(getServerUrl()+"/systeminfo/search");
						p.setPanelLinkLabel(DashboardFactoryHomePage.this.getLabel("search"));
						return p;
					}
				});

				
				
				
				
				AjaxTabbedPanel<ITab> panel = new AjaxTabbedPanel<ITab>("payload",tabs) {
					private static final long serialVersionUID = 1L;
					protected String getNavCss() {
						return "nav nav-buttons";
					}
				};
				
				wr.setSimplePayloadPanel(panel);
				
				
				return wr;
				
			}	
			public IModel<String> getLabel() {
				return DashboardFactoryHomePage.this.getLabel("data-management");
			}
		});




	 
		
		widgets.add(new WidgetFactory() {
			public MarkupContainer getWidget(String id) {
		
				DashboardWidgetSimpleWrapperPanel<Person> wr = new DashboardWidgetSimpleWrapperPanel<Person>("panel", new ObjectModel<Person>(getPerson()), DashboardFactoryHomePage.KEY);
				
				wr.setHelpKey(InlineHelpWebService.FACTORY_HOME_SERVER_INFO);
				wr.setTitle(DashboardFactoryHomePage.this.getLabel("api"));
				
				List<ITab> tabs = new ArrayList<ITab>();
				
				tabs.add(new AbstractTab(DashboardFactoryHomePage.this.getLabel("status")) {
					private static final long serialVersionUID = 1L;
						@Override
						public Panel getPanel(String panelId) {
							GridInfoPanel p=new GridInfoPanel(panelId,  ServiceLocator.getService(AppMonitoringService.class).recentActivityAPIInfo(), null, true);
							p.setPanelLink(getServerUrl()+"/systeminfo/api-dashboard");
							p.setPanelLinkLabel(DashboardFactoryHomePage.this.getLabel("api"));
							return p;
						}
					});
				
				tabs.add(new AbstractTab(DashboardFactoryHomePage.this.getLabel("requests")) {
					@Override
					public Panel getPanel(String panelId) {
						//return (new GridInfoPanel(panelId,  ServiceLocator.getService(AppMonitoringService.class).KBFSInfo(), null, true));
						return   new DummyBlockPanel(panelId, new Model<String>("API Requests"));
					}
				});
				
				AjaxTabbedPanel<ITab> panel = new AjaxTabbedPanel<ITab>("payload",tabs) {
					private static final long serialVersionUID = 1L;
					protected String getNavCss() {
						return "nav nav-buttons";
					}
				};
				
				wr.setSimplePayloadPanel(panel);
				
				
				return wr;
				
			}	
			public IModel<String> getLabel() {
				return DashboardFactoryHomePage.this.getLabel("api");
			}
		});
		
		return widgets;
	}
	
	/**
 	 * 1st RIGHT
 	 * 
	 * @return
	 */
	@SuppressWarnings("serial")
	private List<WidgetFactory> getRightSectionsPanels() {

		List<WidgetFactory> widgets = new ArrayList<WidgetFactory>();

		
		// Domains
		
		widgets.add(new WidgetFactory() {
			public MarkupContainer getWidget(String id) {
				return new DashboardDomainsWidgetPanel(id, DashboardHomePage.KEY);
			}
			public IModel<String> getLabel() {
				return DashboardFactoryHomePage.this.getLabel("domains");
			}
		});

		
		// Herramientas
		
		
		widgets.add(new WidgetFactory() {
			public MarkupContainer getWidget(String id) {
				return new DashboardWidgetFactoryToolsPanel("panel", "factory-tools");
				
			}	
			public IModel<String> getLabel() {
				return  new StringResourceModel("quick-links", DashboardFactoryHomePage.this, null);
			}
		});
		

		
		widgets.add(new WidgetFactory() {
			public MarkupContainer getWidget(String id) {
				return new DashboardWidgetAccountPanel("panel", "user-access");
			}	
			public IModel<String> getLabel() {
				return  new StringResourceModel("user-account", DashboardFactoryHomePage.this, null);
			}
		});
		return widgets;
	}
	

	/**
	 *  LEFT
	 *  
	 *  1. Info Servidor (Key Metrics, Server, Hardware)
		2. Data Management (Database,Object Storage,Search)

		RIGHT
		-----
	 *  3. Domains
	 *  4. API (Dashboard, REQUESTS)

	 *  Users
	 *  
	 *  Commands
	 *  Scheduler
	 * 
	 */
	
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
	
	protected boolean hasPermissions() {
		return getDomain()!=null && getDomain().getName().equals("kbee");
	}
	
	protected boolean isSectionHome() {
		return true;
	}
	
	@Override
	protected Panel getBreadcrumbPanel() {
		return new FlagPanel("breadcrumb");
	}

	
}
