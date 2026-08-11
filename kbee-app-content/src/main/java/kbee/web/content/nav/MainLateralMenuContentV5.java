package kbee.web.content.nav;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.library.Library;
import com.novamens.content.library.LibraryService;
import com.novamens.content.model.DataSet;
import com.novamens.content.user.UserService;
import com.novamens.content.web.admin.markup.SystemInfoGeneralPage;
import com.novamens.content.web.admin.markup.SystemInfoPage;
import com.novamens.content.web.console.markup.DashboardPage;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.portal6.model.Site;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.PropertiesFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.LinkMenuItemPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SubmenuItemPanelV5;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;
import kbee.web.content.console.ArchivePage;
import kbee.web.content.console.ContentBasePage;
import kbee.web.content.console.MonitorPage;
import kbee.web.content.console.PendingTasksPage;
import kbee.web.content.console.RecycleBinPage;
import kbee.web.dashboard.UserDashboardService;
import kbee.web.datamanagement.ReindexPage;
import kbee.web.datamanagement.TagManagementPage;
import kbee.web.error.ApplicationErrorPage;

import kbee.web.page.ApplicationMenuSection;
import kbee.web.rule.ActionRulesPage;
import kbee.web.service.PortalPanelService;
import kbee.web.service.ReportsLibraryService;

/** 
* <p>Main Lateral Menu for the Web App (but not the Factory tool)</p>
**/

@SuppressWarnings("serial")
public class MainLateralMenuContentV5 extends MainLateralMenuBaseV5 {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(MainLateralMenuContentV5.class.getName());
	
	private boolean menu_tasks = is_root || role_tasks_mytasks || role_tasks_auditor || role_tasks_pending  || role_support; 
	
	private static String DraftResourcesEnabled =
		PropertiesFactory
			.getInstance("kbee")
			.getProperties()
			.getProperty("kbee.user.resources.draft.enabled", "true");

	private List<IModel<Library>> libraries;
	
	public MainLateralMenuContentV5(String id, String applicationMenuSection) {
		super(id, applicationMenuSection);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addPremiumRegularMenu();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		if (libraries!=null)
			libraries.forEach(item -> item.detach());
	}
	
	/**
	 * Basic
	 * -----
	 * Files -> root
	 * Portal -> root
	 * Settings -> Admin  (General Settings) 
	 * 
	 */
	protected void addPremiumRegularMenu() {
		
		if (menu_tasks || 
			role_admin ||
			role_security) {
			addHomeMenu();
		}
		
		if (role_tasks_mytasks) {
			addMyWorkMenu();
		}
		
		if (this.menu_tasks) {
			addTasksMenu();
		}	
		
		addContentMenu();

		if ("true".equals(DraftResourcesEnabled)) {
			addDraftResourcesMenu();
		}
		
		if (is_root) {
			addPortalMenu();
		}	
		
		if (this.menu_settings)
			addSettingsMenu();

		if (this.is_root || 
			role_admin || 
			role_security || 
			role_support || 
			role_federated_security || 
			isUserAdmin()) {
			addSecurityMenu();
		}	
		if (this.is_root || role_admin || role_support) {
			addAlertSettingsMenu();
			addManagementMenu();
			if (getDomain().hasIntegrationService()) {
				addIntegrationMenu();
			}
		}

		if (this.is_root || role_admin || role_auditor) {
			addAuditMenu();
		}
	}
	
	
	protected void addBasicRegularMenu() {
	
		if (this.is_root) {
			addTasksMenu();
			addContentMenu();
			addUSerMessagesMenu();
			
			// TODO VER AT
			addPortalMenu();
			
			addSettingsMenu();
			addSecurityMenu();
			addAlertSettingsMenu();
			addManagementMenu();
			addIntegrationMenu();

			// TODO VER AT
			// addReportsMenu();
			addAuditMenu();
			addSystemInfoMenu();
		}
		else {
			addSettingsMenu();
			addContentMenu();
			addSecurityMenu();
		}
	}
		
		 
	 
	
	/**
	 * MANAGEMENT
	 * 
	 */
	private void addManagementMenu() {
		addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return getDataManagementSubmenu(id);
			};
		});
	}
	
	
	
	private AbstractMenuItemPanelV5<Void> getDataManagementSubmenu(String id) {

		String css_selected = getApplicationSectionMenu().equals(ApplicationMenuSection.DATA_MANAGEMENT.getKey())? " selected": "" ;
		
		SubmenuItemPanelV5<Void> menu = new SubmenuItemPanelV5<Void>(id, FA_PREFIX + " fa-database " + css_selected) {
			@Override
			public String getLabel() {
				return getMenuLabel("mainmenu.datamanagement");
			}
			@Override
			public String getIcon() {
				return ICON_DATA_MANAGEMENT;
			}
		};
		
		

			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new LinkMenuItemPanel<Void>(id) {
						@Override
						public void onClick() {
							try {
								getPage().setResponsePage(new TagManagementPage());
							} 
							catch (Exception e) {
								logger.error(e);
								setResponsePage(new ApplicationErrorPage<Void>(e));
							}
						}
						@Override
						public String getLabel() {
							return getMenuLabel("mainmenu.tag-management-tool");
						}
						@Override
						public String getUrl() {
							return "/datamanagement/tagtool";
						}
						@Override
						public String getBeforeClick() {
							return "if (typeof submit === \"function\") { submit(); }";
						}
					}; 
				}
			});
			
			
			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new LinkMenuItemPanel<Void>(id) {
						@Override
						public void onClick() {
							try {
								getPage().setResponsePage(new ActionRulesPage());
							} 
							catch (Exception e) {
								logger.error(e);
								setResponsePage(new ApplicationErrorPage<Void>(e));
							}
						}
						@Override
						public String getLabel() {
							return getMenuLabel("mainmenu.time-dependent-actions");
						}
						@Override
						public String getUrl() {
							return "/actionrules";
						}
						@Override
						public String getBeforeClick() {
							return "if (typeof submit === \"function\") { submit(); }";
						}
					}; 
				}
			});
			

			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new LinkMenuItemPanel<Void>(id) {
						@Override
						public void onClick() {
							try {
								getPage().setResponsePage(new ReindexPage());
							} 
							catch (Exception e) {
								logger.error(e);
								setResponsePage(new ApplicationErrorPage<Void>(e));
							}
						}
						@Override
						public String getLabel() {
							return getMenuLabel("search");
						}

						@Override
						public boolean isVisible() {
							return is_root;
						}
						
						@Override
						public String getUrl() {
							return "/datamanagement/reindex";
						}
						@Override
						public String getBeforeClick() {
							return "if (typeof submit === \"function\") { submit(); }";
						}
					}; 
				}
			});

			
			
		return menu;
	}
	
	
	/***
	 * TASKS
	 */
	private void addTasksMenu() {
		addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return getTasksSubmenu(id);
			};
		});
	}
	
	private AbstractMenuItemPanelV5<Void> getTasksSubmenu(String id) {
		
		String css_selected = getApplicationSectionMenu().equals(ApplicationMenuSection.TASK.getKey())? " selected": "" ;
		
		SubmenuItemPanelV5<Void> menu = new SubmenuItemPanelV5<Void>(id,  FA_PREFIX +  " fa-coffee " + css_selected) {
			@Override
			public String getLabel() {
				return getMenuLabel("mainmenu.tasks");
			}
			@Override
			public String getIcon() {
				return ICON_TASK;
			}
		};
		 	

		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new LinkMenuItemPanel<Void>(id) {
					@Override
					public void onClick() {
						try {
							fireNavigationEvent();
							getPage().setResponsePage(new MonitorPage());
						} 
						catch (Exception e) {
							logger.error(e);
							setResponsePage(new ApplicationErrorPage<Void>(e));
						}
					}
					@Override
					public String getLabel() {
						return getMenuLabel("mainmenu.tasks.monitor");
					}
					@Override
					public String getUrl() {
						return "/monitor";
					}
					@Override
					public  boolean isVisible() {
						return role_tasks_auditor;
					}
				};
			}
		});

		
		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new LinkMenuItemPanel<Void>(id) {
					@Override
					public void onClick() {
						try {
							fireNavigationEvent();
							getPage().setResponsePage(new PendingTasksPage());
						}
						catch (Exception e) {
							logger.error(e);
							setResponsePage(new ApplicationErrorPage<Void>(e));
						}
					}
					@Override
					public String getLabel() {
						return getMenuLabel("mainmenu.tasks.pendings");
					}
					@Override
					public String getUrl() {
						return "/pendingtasks";
					}
					@Override
					public  boolean isVisible() {
						return role_admin || role_support || role_tasks_pending;
					}
				};
			}
		});

		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new LinkMenuItemPanel<Void>(id) {
					@Override
					public void onClick() {
						try {
							fireNavigationEvent();
							getPage().setResponsePage(new DashboardPage());
						} 
						catch (Exception e) {
							logger.error(e);
							setResponsePage(new ApplicationErrorPage<Void>(e));
						}
					}
					@Override
					public String getLabel() {
						return getMenuLabel("mainmenu.tasks.dashboard");
					}
					@Override
					public String getUrl() {
						return "/monitor/dashboard";
					}
					@Override
					public  boolean isVisible() {
						return  role_tasks_dashboard;
					}
				}; 
			}
		});
		
		return menu;
	}
	
	
	/***
	 * CONTENT
	 */
	private void addContentMenu() {
		addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return getContentSubmenu(id);
			};
		});
	}
	
	private AbstractMenuItemPanelV5<Void> getContentSubmenu(String id) {
	
		String css_selected = getApplicationSectionMenu().equals(ApplicationMenuSection.CONTENT.getKey())? " selected": "" ;
		
		SubmenuItemPanelV5<Void> menu = new SubmenuItemPanelV5<Void>(id,  FA_PREFIX +  " fa-university	 " + css_selected) {
			@Override
			public String getLabel() {
				return getMenuLabel("mainmenu.content.library");
			}
			@Override
			public String getIcon() {
				return ICON_LIBRARY; // "bank-1";
			}
			@Override
			public boolean isVisible() {
				return  role_admin || is_root || role_library || role_support;
			}
		};
		
		for (IModel<Library> model: getLibraries()) {
			
				menu.addModelItem(new MenuItemFactory<Library>() {
					@Override
					public AbstractMenuItemPanelV5<Library> getItem(String id) {
						return new LinkMenuItemPanel<Library>(id, model) {
							@Override
							public String getLabel() {
								return getModelObject().getDisplayName();
							}
							@Override
							public String getUrl() {
								return "/content/"+getModelObject().getKey();
							}
							@Override
							public void onClick() throws Exception {
								try {
									setResponsePage(getConsolePage());
								} 
								catch (Exception e) {
									logger.error(e);
									setResponsePage(new ApplicationErrorPage<Void>(e));
								}
							}
							public Page getConsolePage() {
								Page page;
								if (getModelObject().getPage()==null) {
									page = new ContentBasePage(getModel());
								}
								else {
									page = (Page)ServiceLocator.getService(BeansService.class).getBean(getModelObject().getPage(), getModel());
								}
								return page;
							}
						};
					};
				});
			
		};
		
		
		

		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new SeparatorMenuItemPanelV5<Void>(id) {
					@Override
					public String getCssClass() {
						return "divider sidebar-popup-menu-item";
					}
					@Override
					public boolean isVisible() {
						return  true;
					}
				};
			}
		});


		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id ) {
				return new LinkMenuItemPanel<Void>(id) {
					@Override
					public void onClick() {
						try {
							getPage().setResponsePage(new ArchivePage());
						} 
						catch (Exception e) {
							logger.error(e);
							setResponsePage(new ApplicationErrorPage<Void>(e));
						}
					}
					@Override
					public String getLabel() {
						return getMenuLabel("mainmenu.content.archive");
					}
					@Override
					public String getUrl() {
						return "/archive";
					}
					@Override
					public String getBeforeClick() {
						return "if (typeof submit === \"function\") { submit(); }";
					}
					@Override
					public boolean isVisible() {
						return role_admin || role_support ;
					}
					
				};
			}
		});
		
		
		
		// --------------------------- Recycle ------------------------------
		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new LinkMenuItemPanel<Void>(id) {
					@Override
					public void onClick() {
						try {
							getPage().setResponsePage(new RecycleBinPage());
						} 
						catch (Exception e) {
							logger.error(e);
							setResponsePage(new ApplicationErrorPage<Void>(e));
						}
					}
					@Override
					public String getLabel() {
						return getMenuLabel("mainmenu.content.recyclebin");
					}
					@Override
					public String getUrl() {
						return "/recyclebin";
					}
					@Override
					public String getBeforeClick() {
						return "if (typeof submit === \"function\") { submit(); }";
					}
					@Override
					public boolean isVisible() {
						return role_library;
					}
				};
			}
		});
		return menu;
	};
	
	
	/***
	 * PORTAL
	 * 
	 */
	private void addPortalMenu() {
		
		//if (role_admin) {
			addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return getPortalSubmenu(id);
				};
			});
		//}
	}
									
	private AbstractMenuItemPanelV5<Void> getPortalSubmenu(String id) {
	
		String css_selected = getApplicationSectionMenu().equals(ApplicationMenuSection.SITES.getKey())? " selected": "" ;
		
		SubmenuItemPanelV5<Void> menu = new SubmenuItemPanelV5<Void>(id, FA_PREFIX +  " fa-sitemap " + css_selected) {
			@Override
			public String getLabel() {
				return getMenuLabel("mainmenu.portal");
			}
			@Override
			public String getIcon() {
				return ICON_PORTAL; // "organization-hierarchy-3";
			}
			@Override
			public boolean isVisible() {
				return true; 
			}
		};
		
		/**
		 * Site Directory
		 */
		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new LinkMenuItemPanel<Void>(id) {
					@Override
					public void onClick() {
						try {
							WebPage page = (WebPage) ServiceLocator.getService(BeansService.class).getBean("sitesPage");
							if (page!=null)
								getPage().setResponsePage(page);
							else
								logger.error("page is null");
						} 
						catch (Exception e) {
							logger.error(e);
							setResponsePage(new ApplicationErrorPage<Void>(e));
						}
					}
					@Override
					public String getLabel() {
						return getMenuLabel("mainmenu.portal.sites");
					}
					@Override
					public String getUrl() {
						return "/portal";
					}
					@Override
					public String getBeforeClick() {
						return "if (typeof submit === \"function\") { submit(); }";
					}
				};
			}
		});
		
		
		// DIVIDER
		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new SeparatorMenuItemPanelV5<Void>(id) {
					@Override
					public String getCssClass() {
						return "divider sidebar-popup-menu-item";
					}
					@Override
					public boolean isVisible() {
						return  true;
					}
				};
			}
		});
		
		
		KbeeUser us = (KbeeUser) getSessionUser();
		List<Site> sites = us.getService(UserDashboardService.class).getMySites();

		for (Site site: sites) {
			
			final Serializable siteid = site.getId();
			final String siteName = site.getTitle();
			
			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new LinkMenuItemPanel<Void>(id) {
						@Override
						public void onClick() {
							try {
								WebPage page=ServiceLocator.getService(PortalPanelService.class).getWebPage(siteid);
								setResponsePage(page);
							} 
							catch (Exception e) {
								logger.error(e);
								setResponsePage(new ApplicationErrorPage<Void>(e));
							}
						}
						@Override
						public String getLabel() {
							return siteName;
						}
						//@Override
						//public String getUrl() {
						//	return "/portal";
						//}
						@Override
						public String getBeforeClick() {
							return "if (typeof submit === \"function\") { submit(); }";
						}
					};
				}
			});
		}
		return menu;
	}
	
	
	
	
	
	@Override
	protected List<IModel<Library>> getLibraries() {
		
		if (libraries!=null)
			return libraries;
				
		libraries = new ArrayList<IModel<Library>>();
		for (Library library : getDomain().getService(LibraryService.class).getLibraries(ObjectState.ENABLED, "listOrder")) {
			if (library.isReadable()) {
				libraries.add(new ObjectModel<Library>(library));
			}
		}
		
		return libraries;
	}
	
	protected boolean hasReports() {
		return ServiceLocator.getService(ReportsLibraryService.class).hasReports(getDomain());
	}
	
	protected DataSet getDataSet(String id) {
		for(DataSet dataset : getContentDao().getDataSets(getDomain())) {
			if (id.equals(String.valueOf(dataset.getId()))) {
				return dataset;
			};
		}
		return null;
	}
	
	protected String getMenuLabel(String key) {
		return (new StringResourceModel(key, this, null)).getObject();
	}
	
	@Override
	protected String getUitheme() {
		return "brand-"+((KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser()).getUitheme();
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	private void addSystemInfoMenu() {
		addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return getInfoSubmenu(id); // Info
			};
		});
	}
	
	private AbstractMenuItemPanelV5<Void> getInfoSubmenu(String id) {
		
		String css_selected = getApplicationSectionMenu().equals(ApplicationMenuSection.INFO.getKey())? " selected": "" ;
			
		
		
		SubmenuItemPanelV5<Void> menu = new SubmenuItemPanelV5<Void>(id,  FA_PREFIX +  " fa-info " + css_selected) {
			@Override
			public String getLabel() {
				return "Info";
			}
			@Override
			public String getUrl() {
				return "/systeminfo/keymetrics";
			}
			
			@Override
			public String getIcon() {
				return ICON_INFO; // "building-8";
			}
			
			 
		
			
		};

		 // menu.setDirectionUp(true);
		 
		 
		menu.addItem(itemid ->
			new LinkMenuItemPanel<Void>(itemid) {
				@Override
				public void onClick() {
					getPage().setResponsePage(new SystemInfoPage());
				}
				@Override
				public String getLabel() { 
					return  getMenuLabel("mainmenu.dashboard");  
				}
				@Override
				public String getUrl() { 
					return  "/systeminfo/keymetrics"; 
				}
				@Override
				public String getBeforeClick() {
					return "if (typeof submit === \"function\") { submit(); }";
				}
			}
		);
				

		menu.addItem(new MenuItemFactory<Void>() {
					@Override
					public AbstractMenuItemPanelV5<Void> getItem(String id) {
						return new LinkMenuItemPanel<Void>(id) {
							@Override
							public void onClick() {
								 getPage().setResponsePage(new SystemInfoGeneralPage("hardware"));
							}
							@Override
							public String getLabel() {
								return  getMenuLabel("mainmenu.hardware.os");
							}
							@Override
							public String getUrl() {
								return  "/systeminfo/hardware"; 
							}
							@Override
							public String getBeforeClick() {
								return "if (typeof submit === \"function\") { submit(); }";
							}
						}; 
					}
			});


			menu.addItem(new MenuItemFactory<Void>() {
					@Override
					public AbstractMenuItemPanelV5<Void> getItem(String id) {
						return new LinkMenuItemPanel<Void>(id) {
							@Override
							public void onClick() {
								getPage().setResponsePage(new SystemInfoGeneralPage("logs"));
							}
							@Override
							public String getLabel() {
								return "Logs"; //getMenuLabel("mainmenu.domains.info");
							}
							
							@Override
							public String getUrl() {
								return  "/systeminfo/logs"; 
							}
							
							@Override
							public String getBeforeClick() {
								return "if (typeof submit === \"function\") { submit(); }";
							}
						}; 
					}
				});

				
				menu.addItem(new MenuItemFactory<Void>() {
					@Override
					public AbstractMenuItemPanelV5<Void> getItem(String id) {
						return new LinkMenuItemPanel<Void>(id) {
							@Override
							public void onClick() {
								getPage().setResponsePage(new SystemInfoGeneralPage("jvm-threads"));
							}
							@Override
							public String getLabel() {
								return  "JVM Threads"; //getMenuLabel("mainmenu.domains.info");
							}
							
							 @Override
								public String getUrl() {
									return  "/systeminfo/jvm-threads"; 
								}

							@Override
							public String getBeforeClick() {
								return "if (typeof submit === \"function\") { submit(); }";
							}
						}; 
					}
				});
		
				
				 
				
				return menu;
	}

}

