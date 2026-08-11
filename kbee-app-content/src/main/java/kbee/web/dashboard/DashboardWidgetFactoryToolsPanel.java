package kbee.web.dashboard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.content.web.admin.api.APIRequestsReportPage;
import com.novamens.content.web.admin.markup.SystemInfoGeneralPage;
import com.novamens.content.web.admin.markup.SystemParametersPage;
import com.novamens.content.web.admin.markup.datamanagement.SystemDataManagementGeneralPage;
import com.novamens.content.web.admin.markup.datamanagement.SystemSchedulerMonitorPage;
import com.novamens.content.web.console.markup.AuditEmailPage;
import com.novamens.content.web.sql.markup.SQLGatewayPage;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.alert.BillboardsPage;
import kbee.web.content.console.AuditResourcesPage;
import kbee.web.datamanagement.ReindexPage;
import kbee.web.datamanagement.ThumbnailServicePage;
import kbee.web.domain.DomainsPage;
import kbee.web.error.ErrorPanel;
import kbee.web.help.InlineHelpWebService;
import kbee.web.security.role.RolesPage;
import kbee.web.security.user.UsersPage;
import kbee.web.service.ApplicationSiteMapService;
				
public class DashboardWidgetFactoryToolsPanel extends DashboardWidgetBasePanel {
			
	
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DashboardWidgetFactoryToolsPanel.class.getName());

	private WebMarkupContainer help;
	private WebMarkupContainer main_container;
	private IModel<Person> model;
	

	public DashboardWidgetFactoryToolsPanel(String id, String preferences_key) {
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
			

			
			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new MenuItemPanelV5<Void>(id) {
						public void onClick() {
							 setResponsePage(new SQLGatewayPage());
						}
						@Override 
						public String getLabel() {
							//return "SQL Gateway";
							return (DashboardWidgetFactoryToolsPanel.this.getLabel("sqlgateway")).getObject();
							
							//return (DashboardWidgetFactoryToolsPanel.this.getLabel("domains")).getObject();
						}
						
						public boolean isEnabled() {
							return isRoot();
						}
					};
				}
			});


			
			
			
			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new MenuItemPanelV5<Void>(id) {
						public void onClick() {
							 setResponsePage(new DomainsPage());
						}
						@Override 
						public String getLabel() {
							return (DashboardWidgetFactoryToolsPanel.this.getLabel("domains")).getObject();
						}
					};
				}
			});

			
			
			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new MenuItemPanelV5<Void>(id) {
						public void onClick() {
							 setResponsePage(new SystemSchedulerMonitorPage());
						}
						@Override 
						public String getLabel() {
							return (DashboardWidgetFactoryToolsPanel.this.getLabel("scheduler")).getObject();
						}
					};
				}
			});
			
 
			
			
			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new MenuItemPanelV5<Void>(id) {
						public void onClick() {
							 setResponsePage(new ReindexPage());
						}
						@Override 
						public String getLabel() {
							return (DashboardWidgetFactoryToolsPanel.this.getLabel("reindex")).getObject();
						}
					};
				}
			});
			
			
			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new MenuItemPanelV5<Void>(id) {
						public void onClick() {
							 setResponsePage( new RedirectPage(getServerUrl()+"/systeminfo/api-dashboard"));
							 
						}
						@Override 
						public String getLabel() {
							return "API Dashboard"; 
							//return (DashboardWidgetFactoryToolsPanel.this.getLabel("cache")).getObject();
						}
					};
				}
			});

			

			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new MenuItemPanelV5<Void>(id) {
						public void onClick() {
							 setResponsePage( new APIRequestsReportPage());
						}
						@Override 
						public String getLabel() {
							return "API Reportes"; 
							//return (DashboardWidgetFactoryToolsPanel.this.getLabel("cache")).getObject();
						}
					};
				}
			});

			
				
			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new MenuItemPanelV5<Void>(id) {
						public void onClick() {
							 setResponsePage( new ThumbnailServicePage());
						}
						@Override 
						public String getLabel() {
							return (DashboardWidgetFactoryToolsPanel.this.getLabel("cache")).getObject();
						}
					};
				}
			});
 

			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new MenuItemPanelV5<Void>(id) {
						public void onClick() {
							 setResponsePage(new SystemDataManagementGeneralPage("commands"));
						}
						@Override 
						public String getLabel() {
							return (DashboardWidgetFactoryToolsPanel.this.getLabel("commands")).getObject();
						}
					};
				}
			});

			
			
			
			
			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new MenuItemPanelV5<Void>(id) {
						public void onClick() {
							 setResponsePage( new SystemInfoGeneralPage("jvm-threads"));
						}
						@Override 
						public String getLabel() {
							return (DashboardWidgetFactoryToolsPanel.this.getLabel("jvm-threads")).getObject();
						}
					};
				}
			});
		

			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new MenuItemPanelV5<Void>(id) {
						public void onClick() {
							 setResponsePage(new UsersPage());
						}
						@Override 
						public String getLabel() {
						return "Usuarios";
							//return (new ResourceModel("version")).getObject();
						}
					};
				}
			});

			
			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new MenuItemPanelV5<Void>(id) {
						public void onClick() {
							 setResponsePage(new RolesPage());
						}
						@Override 
						public String getLabel() {
							//return DashboardWidgetFactoryToolsPanel.this.getLabel("roles").getObject();
							return "Roles";
						}
					};
				}
			});
			
			
			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new MenuItemPanelV5<Void>(id) {
						public void onClick() {
							 setResponsePage( new SystemInfoGeneralPage("logs"));
						}
						@Override 
						public String getLabel() {
							return DashboardWidgetFactoryToolsPanel.this.getLabel("logs").getObject();
						}
					};
				}
			});
		
	
			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new MenuItemPanelV5<Void>(id) {
						public void onClick() {
							 setResponsePage(new SystemInfoGeneralPage("version"));
						}
						@Override 
						public String getLabel() {
						return "Version";
							//return (new ResourceModel("version")).getObject();
						}
					};
				}
			});
	
	
			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new MenuItemPanelV5<Void>(id) {
						public void onClick() {
							 setResponsePage(new AuditEmailPage());
						}
						@Override 
						public String getLabel() {
						return "Correos enviados";
							//return (new ResourceModel("version")).getObject();
						}
					};
				}
			});
	
	
			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new MenuItemPanelV5<Void>(id) {
						public void onClick() {
							 setResponsePage(new AuditResourcesPage());
						}
						@Override 
						public String getLabel() {
							return DashboardWidgetFactoryToolsPanel.this.getLabel("resources").getObject();
						}
					};
				}
			});
	
			
			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new MenuItemPanelV5<Void>(id) {
						public void onClick() {
							 setResponsePage(new BillboardsPage());
						}
						@Override 
						public String getLabel() {
							return (DashboardWidgetFactoryToolsPanel.this.getLabel("billboards")).getObject();
						}
					};
				}
			});
			
			
			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new MenuItemPanelV5<Void>(id) {
						public void onClick() {
							 setResponsePage(new SystemParametersPage());
						}
						@Override 
						public String getLabel() {
							return (DashboardWidgetFactoryToolsPanel.this.getLabel("system-parameters")).getObject();
						}
					};
				}
			});

			
			
			
			
			/**
			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new SeparatorMenuItemPanelV5<Void>(id) {
						@Override
						public boolean isVisible() {
							return true;
						}
						@Override
						public String getCssClass() {
							return "divider";
						}
					};
				}
			});
			**/
			
 
			
			
			

			main_container.setOutputMarkupId(true);
			main_container.add(menu);
			
			
			long end=System.currentTimeMillis();
			logger.debug("FileFactory -> " + String.valueOf(end-start)+ " ms");
	}
	
	
	
	@Override
	protected void onTitleClick() {
	}
	
	@Override
	public void onDetach() {
		super.onDetach();

		if (model!=null)
			model.detach();
		
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
