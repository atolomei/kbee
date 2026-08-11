package kbee.web.portal6;


import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.googlecode.wicket.jquery.core.panel.LabelPanel;
import com.novamens.content.library.Library;
import com.novamens.content.service.DomainService;
import com.novamens.content.user.UserService;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.portal.service.SiteFactoryService;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteTemplate;
import com.novamens.portal6.model.SiteType;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemWithModelPanel;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.error.ApplicationErrorPage;
import kbee.web.portal6.editor.PortalSiteEditorPage;
import kbee.web.searcher.editor.SearcherSiteEditorPage;

public class SiteFactoryPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SiteFactoryPanel.class.getName());

	final boolean is_support				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean is_admin					= is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());;
	final boolean is_portal					= is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.PORTAL_ADMIN.getId());;

	
	public SiteFactoryPanel() {
		this("new-site");
	}

	@SuppressWarnings("serial")
	public SiteFactoryPanel(String id) {
		super(id);

		ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);

		WebMarkupContainer newm = new WebMarkupContainer("new-multiple-button");
		newm.add(new AttributeModifier("class", "btn-md btn btn-primary dropdown-toggle"));
		newm.add(new AttributeModifier("data-toggle", "dropdown"));
		add(newm);

		add(menu);

		// Intranet Site --------------------------------------------------
		//
		/**
		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public MenuItemPanel<Void> getItem(String id) {
				return new MenuItemPanelV5<Void>(id) {
					@Override
					public void onClick() {
						DiagrammableSite site;
						try {
							site = SiteFactoryPanel.this.createSite();
							DiagrammablePage page = site.getHomePage();
							setResponsePage(new KBPWebPage(new ObjectModel<DiagrammableSite>(site), new ObjectModel<DiagrammablePage>(page), "site"));

						} catch (ContentMgmtException e) {

							// TODO ERROR MSG WINDOW
							logger.error(e.getMessage());

						} catch (ContentCreationException e) {

							// TODO ERROR MSG WINDOW
							logger.error(e.getMessage());
						}

					}

					@Override
					public String getLabel() {
						return new StringResourceModel("new-intranet-site", SiteFactoryPanel.this, null).getObject();
					}

					@Override
					public String getTarget() {
						return "_blank";
					}

					@Override
					public boolean isEnabled() {
						return ServiceLocator.getService(PortalSecurityService.class).isCreateIntranetSiteSessionUser();
					}
				};
			}
		});

		*/
		
		// Library Site --------------------------------------------------
		//
		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new MenuItemPanelV5<Void>(id) {
					@Override
					public void onClick() {
						
						try {
							
							getDomain().getService(DomainService.class).createSiteDataSetIfNotExists();
							getDomain().getService(DomainService.class).createSiteRepositoryDataSetIfNotExists();
							SiteFactoryService service = ServiceLocator.getService(SiteFactoryService.class);
							
							Site site;
							Library library = null;
							
							for (Library lib: getRepository(Library.class).findAll()) {
								
								
								if (lib.getKey().equals("all")) {
									site = service.createLibrarySite( lib );
									setResponsePage( new PortalSiteEditorPage(new ObjectModel<Site>(site)));
									return;
								}
								
								if (lib.getState()==ObjectState.ENABLED && library == null)
									library = lib;
							}
							
							
							if (library!=null) {
								site = service.createLibrarySite( library );
								setResponsePage( new PortalSiteEditorPage(new ObjectModel<Site>(site)));
								
							}
							else {
								setResponsePage( new ApplicationErrorPage<>(new Model<String>("There are no Libraries in state Enabled")));
							}
							
							
						} catch (Exception e) {
							setResponsePage( new ApplicationErrorPage<Void>(e));
							logger.error(e);
						}
					}

					@Override
					public String getLabel() {
						return new StringResourceModel("new-searcher-site", SiteFactoryPanel.this, null).getObject();
					}

					@Override
					public String getTarget() {
						return "_blank";
					}

					@Override
					public boolean isEnabled() {
						return is_admin || is_portal;
					}
					
					protected boolean isContextualHelp() {
						return true;
						
					}
					
					protected Panel getContextualDetailPanel() {
						return new LabelPanel("contextual-help-detail", new Model<String>("method getContextualDetailPanel must be overriden"));
					}


					
				};
			}
		});
		

		/**
		if (logger.isDebugEnabled() ) {
			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new MenuItemPanelV5<Void>(id) {
						@Override
						public void onClick() {
							try {
								
								getDomain().getService(DomainService.class).createSiteDataSetIfNotExists();
								getDomain().getService(DomainService.class).createSiteRepositoryDataSetIfNotExists();
								
								SiteFactoryService service = ServiceLocator.getService(SiteFactoryService.class);
								Site site = service.createGeneralDashboardSite();
								setResponsePage( new PortalSiteEditorPage(new ObjectModel<Site>(site)));
							} catch (Exception e) {
								logger.error(e);
							}
	
						}
	
						@Override
						public String getLabel() {
							return "General Dashboard ROOT";
						}
	
						@Override
						public String getTarget() {
							return "_blank";
						}
	
						@Override
						public boolean isEnabled() {
							return is_root;
						}
					};
				}
			});
			
		}	
			**/
	
		
		
		
		// KBase Site --------------------------------------------------
		//
		/**
		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new MenuItemPanelV5<Void>(id) {
					@Override
					public void onClick() {
						
						try {
							getDomain().getService(DomainService.class).createSiteDataSetIfNotExists();
							getDomain().getService(DomainService.class).createSiteRepositoryDataSetIfNotExists();
							
							SiteFactoryService service = ServiceLocator.getService(SiteFactoryService.class);
							Site site = service.createKBaseSite();
							// setResponsePage(new SearcherSiteEditorPage(new ObjectModel<Site>(site), true));
						} catch (Exception e) {
							logger.error(e);
						}
					}

					@Override
					public String getLabel() {
						return new StringResourceModel("new-kbase-site", SiteFactoryPanel.this, null).getObject();
					}

					@Override
					public String getTarget() {
						return "_blank";
					}

					@Override
					public boolean isEnabled() {
						return is_admin || is_portal;
					}
				};
			}
		});
		*/

		
		
		
		
		
		
		
		/**
		// Deal room --------------------------------------------------
		//
		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new MenuItemPanelV5<Void>(id) {
					@Override
					public void onClick() {
						try {
							getDomain().getService(DomainService.class).createSiteDataSetIfNotExists();
							getDomain().getService(DomainService.class).createSiteRepositoryDataSetIfNotExists();
							SiteFactoryService service = ServiceLocator.getService(SiteFactoryService.class);
							Site site = service.createDealRoomSite();
							setResponsePage(new SearcherSiteEditorPage(new ObjectModel<Site>(site)));
						} catch (Exception e) {
							logger.error(e);
						}

					}

					@Override
					public String getLabel() {				
						return new StringResourceModel("new-dealroom-site", SiteFactoryPanel.this, null).getObject();
					}

					@Override
					public String getTarget() {
						return "_blank";
					}

					@Override
					public boolean isEnabled() {
						return is_admin || is_portal;
					}
				};
			}
		});
		*/

		
		
		/*
		// External Site --------------------------------------------------
		//
		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new MenuItemPanelV5<Void>(id) {
					@Override
					public void onClick() {
						NewSiteData data = new NewSiteData();
						data.setExternal(true);
						setResponsePage(new NewSitePage(new Model<NewSiteData>(data)));
					}

					@Override
					public String getLabel() {
						return new StringResourceModel("new-external-site", SiteFactoryPanel.this, null).getObject();
					}

					@Override
					public String getTarget() {
						return "_blank";
					}

					@Override
					public boolean isEnabled() {
						return is_admin || is_portal;
					}
					// return ServiceLocator.getService(PortalSecurityService.class).isCreateExternalSessionUser();
				};
			}
		});
		*/
	}

 
	

	/**
	 * 
	 */
	protected Domain getDomain() {
		try {
			return ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	
	protected <R> DomRepository<R> getRepository(Class<R> objectclass) {
		DomRepository<R> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
	}

}
