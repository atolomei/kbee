package kbee.web.dashboard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.alert.BillboardsPage;
import kbee.web.content.console.ContentBasePage;
import kbee.web.content.console.MonitorPage;
import kbee.web.content.console.PendingTasksPage;
import kbee.web.content.console.WorkspacePage;
import kbee.web.datamanagement.TagManagementPage;
import kbee.web.dataset.DashboardDataSetMembersHomePage;

import kbee.web.emailtemplate.EmailTemplatesPage;
import kbee.web.error.ErrorPanel;
import kbee.web.help.InlineHelpWebService;
import kbee.web.model.DashboardInformationModelPage;
import kbee.web.security.role.RolesPage;
import kbee.web.security.user.UsersPage;

public class DashboardWidgetHomeToolsPanel extends DashboardWidgetBasePanel {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger( DashboardWidgetHomeToolsPanel.class.getName());

	private WebMarkupContainer help;
	private WebMarkupContainer main_container;
	private IModel<Person> model;
	
	final boolean is_root=ServiceLocator.getService(SecurityService.class).isRoot();
	final boolean is_admin = is_root || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_domain_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_monitor = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.MONITOR_AUDIT.getId());
	final boolean is_support = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_notifications = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.NOTIFICATIONS.getId());
	final boolean is_pending = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.PENDING_TASKS.getId());
	final boolean is_security		= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
	final boolean is_useradmin		= ServiceLocator.getService(UserService.class).isUserAdmin();
	
	final boolean role_model = ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	final boolean role_dataset_values = is_admin || 
		ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
	final boolean role_dataset_values_read = role_dataset_values || 
		ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_READ.getId());
    final boolean role_tasks_mytasks = is_admin || 
    	ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.WORKSPACE.getId());



	public  DashboardWidgetHomeToolsPanel(String id, String preferences_key) {
		super(id, preferences_key);
		
		setModel(new ObjectModel<Person>(getPerson()));
		super.setTitle (new ResourceModel("quick-links"));
	}


	@Override
	protected WebMarkupContainer getHelpPanel() {
		InlineHelpWebService se=ServiceLocator.getService(InlineHelpWebService.class);
		 WebMarkupContainer  pa = se.getPanel("help", getLocale(), InlineHelpWebService.HOME_ACCOUNT);
		 if (pa!=null)
			 return pa;
		 return new ErrorPanel("help", new Model<String>(InlineHelpWebService.HOME_ACCOUNT));
	}

	
	/**
	 * 
	 * Usuarios
	 * Roles
	 * Carteleras
	 * 
	 * Configuración General 
	 * Bibliotecas
	 * Mi Carpeta temporal
	 * Carpeta de Recursos comunes 
	 * Modelo de Información - Plantilla de Contenidos 
	 * Modelo de Información - DataSets
	 * Modelo de Información - Clasificadores
	 * Modelo de Información - Atributos
	 * 
	 * */
	
	 
	@SuppressWarnings("serial")
	@Override
	public void onInitialize() {
			super.onInitialize();

			long start=System.currentTimeMillis();
			
			setHelp(true);
			
			main_container = new WebMarkupContainer ("tools");
			add(main_container);
			
			main_container.add(new InvisiblePanel("help"));
			
			ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>("menuitem", null);
			menu.setSort(true);
			
			if (is_domain_admin || is_root || is_support) { 
				menu.addItem(new MenuItemFactory<Void>() {
					@Override
					public AbstractMenuItemPanelV5<Void> getItem(String id) {
						return new MenuItemPanelV5<Void>(id) {
							public void onClick() {
								 setResponsePage(new DashboardInformationModelPage());
							}
							@Override 
							public String getLabel() {
								return (DashboardWidgetHomeToolsPanel.this.getLabel("bc.informationmodel")).getObject();
							}
						};
					}
				});
			}
			
			if (role_tasks_mytasks) { 
				menu.addItem(new MenuItemFactory<Void>() {
					@Override
					public AbstractMenuItemPanelV5<Void> getItem(String id) {
						return new MenuItemPanelV5<Void>(id) {
							public void onClick() {
								 setResponsePage(new WorkspacePage());
							}
							@Override 
							public String getLabel() {
								return (DashboardWidgetHomeToolsPanel.this.getLabel("mytasks")).getObject();
							}
						};
					}
				});
			}
			
			
			if (is_domain_admin || is_root || is_monitor || is_support) {
				menu.addItem(new MenuItemFactory<Void>() {
					@Override
					public AbstractMenuItemPanelV5<Void> getItem(String id) {
						return new MenuItemPanelV5<Void>(id) {
							public void onClick() {
								 setResponsePage(new MonitorPage());
							}
							@Override 
							public String getLabel() {
								return (DashboardWidgetHomeToolsPanel.this.getLabel("monitor")).getObject();
							}
						};
					}
				});
			}

			
			
			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new MenuItemPanelV5<Void>(id) {
						public void onClick() {
							 setResponsePage(new ContentBasePage());
						}
						@Override 
						public String getLabel() {
							return (DashboardWidgetHomeToolsPanel.this.getLabel("libraries")).getObject();
						}
					};
				}
			});
			
	
			

			if (is_domain_admin || is_root) {
				menu.addItem(new MenuItemFactory<Void>() {
					@Override
					public AbstractMenuItemPanelV5<Void> getItem(String id) {
						return new MenuItemPanelV5<Void>(id) {
							public void onClick() {
								 setResponsePage(new BillboardsPage());
							}
							@Override 
							public String getLabel() {
								return (DashboardWidgetHomeToolsPanel.this.getLabel("bc.generalsettings")).getObject();
							}
						};
					}
				});
			}
			
			
			if (is_domain_admin || is_root || is_security || is_support || is_useradmin) { 
				menu.addItem(new MenuItemFactory<Void>() {
					@Override
					public AbstractMenuItemPanelV5<Void> getItem(String id) {
						return new MenuItemPanelV5<Void>(id) {
							public void onClick() {
								 setResponsePage(new UsersPage());
							}
							@Override 
							public String getLabel() {
									return (DashboardWidgetHomeToolsPanel.this.getLabel("users")).getObject();
							}
							
							public boolean isEnabled() {
								return isRoot();
							}
						};
					}
				});
			}


			
			if (is_domain_admin || is_root || role_dataset_values) { 
				menu.addItem(new MenuItemFactory<Void>() {
					@Override
					public AbstractMenuItemPanelV5<Void> getItem(String id) {
						return new MenuItemPanelV5<Void>(id) {
							public void onClick() {
								 setResponsePage(new TagManagementPage());
							}
							@Override 
							public String getLabel() {
								return (DashboardWidgetHomeToolsPanel.this.getLabel("bc.tagmanagementtool")).getObject();
							}
						};
					}
				});
			}	

			
			
			
			if (is_domain_admin || is_root || is_security) { 
				menu.addItem(new MenuItemFactory<Void>() {
					@Override
					public AbstractMenuItemPanelV5<Void> getItem(String id) {
						return new MenuItemPanelV5<Void>(id) {
							public void onClick() {
								 setResponsePage(new RolesPage());
							}
							@Override 
							public String getLabel() {
								return (DashboardWidgetHomeToolsPanel.this.getLabel("bc.roles")).getObject();
							}
						};
					}
				});
			}
			
			
			if (is_domain_admin || is_root) { 
				menu.addItem(new MenuItemFactory<Void>() {
					@Override
					public AbstractMenuItemPanelV5<Void> getItem(String id) {
						return new MenuItemPanelV5<Void>(id) {
							public void onClick() {
								 setResponsePage(new BillboardsPage());
							}
							@Override 
							public String getLabel() {
								return (DashboardWidgetHomeToolsPanel.this.getLabel("billboards")).getObject();
							}
						};
					}
				});
			}
			
			if (is_domain_admin || is_root || is_pending || is_support) {
				menu.addItem(new MenuItemFactory<Void>() {
					@Override
					public AbstractMenuItemPanelV5<Void> getItem(String id) {
						return new MenuItemPanelV5<Void>(id) {
							public void onClick() {
								 setResponsePage(new PendingTasksPage());
							}
							@Override 
							public String getLabel() {
								return (DashboardWidgetHomeToolsPanel.this.getLabel("pending")).getObject();
							}
						};
					}
				});
			}
			

			if (role_dataset_values_read || is_support || role_model) {
				menu.addItem(new MenuItemFactory<Void>() {
					@Override
					public AbstractMenuItemPanelV5<Void> getItem(String id) {
						return new MenuItemPanelV5<Void>(id) {
							public void onClick() {
								 setResponsePage(new DashboardDataSetMembersHomePage());
							}
							@Override 
							public String getLabel() {
								return (DashboardWidgetHomeToolsPanel.this.getLabel("bc.datasetmembers")).getObject();
							}
						};
					}
				});
			}


			if (is_domain_admin || is_root) {
				menu.addItem(new MenuItemFactory<Void>() {
					@Override
					public AbstractMenuItemPanelV5<Void> getItem(String id) {
						return new MenuItemPanelV5<Void>(id) {
							public void onClick() {
								 setResponsePage(new EmailTemplatesPage());
							}
							@Override 
							public String getLabel() {
								return (DashboardWidgetHomeToolsPanel.this.getLabel("bc.emailtemplates")).getObject();
							}
						};
					}
				});
			}
			
			
			menu.setSort(true);

			main_container.setOutputMarkupId(true);
			main_container.add(menu);
			
			
			long end=System.currentTimeMillis();
			logger.debug("FileFactory -> " + String.valueOf(end-start)+ " ms");
	}
	
	
	
	
	@Override
	public void onDetach() {
		super.onDetach();

		if (model!=null)
			model.detach();
		
	}

	@Override
	protected void onTitleClick() {
	}
	
	
	public IModel<Person> getModel() {
		return model;
	}

	public void setModel(IModel<Person> model) {
		this.model = model;
	}

	protected void onHelp(AjaxRequestTarget target) {
		toogleHelp(target);
	}
	

	@Override
	protected void onClickCollapse(AjaxRequestTarget target) {
		main_container.setVisible(!main_container.isVisible());
		refresh(target);
	}
	
	public void toogleHelp(AjaxRequestTarget target) {

		if (help==null) {
			help=getHelpPanel();
			help.setVisible(false);
			main_container.addOrReplace(help);
		}
		
		
		if (help!=null && !(help instanceof InvisiblePanel)) {
			help.setVisible(!help.isVisible());
			main_container.get( "menuitem").setVisible(!main_container.get( "menuitem").isVisible());
			target.add(this.main_container);
		}
	}

	
	

}
