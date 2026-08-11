package kbee.web.portal6;





import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.library.Library;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.service.AppMonitoringService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.event.GridPanelNullObjectEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.LastModifiedColumn;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;

//import com.novamens.portal.service.PortalSecurityService;

import com.novamens.portal.service.PortalUserService;
import com.novamens.portal.service.SiteFactoryService;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BreadCrumb;

import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.ExpandedPanel;
import kbee.web.console.grid.ClassifierColumn;
import kbee.web.console.grid.TargetBlankObjectTitleColumnPanel;
import kbee.web.dataset.DataSetMembersConsole;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.portal.dataprovider.PortalBlockTextDataProvider;
import kbee.web.portal6.editor.PortalPageStructureEditorPage;
import kbee.web.portal6.editor.PortalSiteEditorPage;

import kbee.web.service.PortalPanelService;

/**
 *
 */
@SuppressWarnings("serial")
public abstract class SitesConsole extends AbstractFacetedConsole<Site> {

	private static final long serialVersionUID = 1L;

	final boolean is_support				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean is_admin					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());;
	//final boolean is_portal					= is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.PORTAL_ADMIN.getId());;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SitesConsole.class.getName());

	private IModel<Site> site_model;
				
	private List<GridColumn<SearchResult, String>> columns = null;
	
	public SitesConsole(Query query) {
		super("sites", query);
	}

	/**
	 * Check column for multiple selection
	 */
	@Override
	public boolean isSelectionEnabled() {
		return false;
	}


	@Override
	public void onDetach() {
		super.onDetach();
		if (this.site_model != null)
			this.site_model.detach();
		this.columns = null;

	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		/**
		for (Library li: getContentDao().getLibraries(getDomain())) {
			if (li.getState()==ObjectState.ENABLED) {
					Site site=getPortalDao().getLibrarySite(li);
					if (site==null) {
						ServiceLocator.getService(SiteFactoryService.class).createLibrarySite(li);
					}
			}
		}
		**/
		//ServiceLocator.getService(PortalMVCService.class).registerViewer("block-portal-text", PortalBlockTextDataProvider.class.getName());
		//addOrReplace(this.hasTopPanel() ? new AdvancedSearchButton<Person>("advancedsearch") : new InvisiblePanel("advancedsearch"));
	}
	
	
	@Override
	public Query newQuery() {
		return setUserPreference(new SitesHibernateQuery());
		// return setUserPreference(new SitesSolrQuery(getQueryIndex()));
	}


	public void setSiteModel(IModel<Site> model) {
		this.site_model = model;
	}

	@Override
	 protected  IModel<Site> getModel(Site object) {
			return new ObjectModel<Site>(object, true);
	}

	
	public Site getSite() {
		return site_model.getObject();
	}


	// protected abstract Page getConsolePage(Query query, long index);

	public Page getConsolePage(Query query) {
		return getConsolePage(query, -1);
	}

	protected BreadCrumb getBreadCrumb() {
		return null;
	};

	@Override
	protected boolean hasExpander() {
		return true;
	}

	@Override
	protected Panel getPanel(IModel<Site> model) {
		return new ExpandedPanel<Site>("editor", this, model);
	};

	@Override
	protected Panel getPanel(IModel<Site> model, List<String> snippets) {
		return new ExpandedPanel<Site>("editor", this, model, snippets);
	};

	protected void addListeners() {
		super.addListeners();

		add(new WicketEventListener<GridPanelNullObjectEvent>() {
			@Override
			public void onEvent(GridPanelNullObjectEvent event) {
					ServiceLocator.getService( AppMonitoringService.class).attempToFixSiteIndex();
			}
		});

		
		add(new WicketEventListener<com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent>() {
			
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onEvent(SidePanelEvent event) {
			}
		});

		add(new WicketEventListener<ClickEvent<Site>>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onEvent(ClickEvent<Site> event) {
		
				WebPage page=ServiceLocator.getService(PortalPanelService.class).getWebPage(event.getModel().getObject());
				setResponsePage(page);
				
				// setResponsePage( new PortalSiteEditorPage(event.getModel()));
				// setResponsePage( ServiceLocator.getService(PortalPanelService.class).getWebPage(event.getModel().getObject()));
			}
		});
	}

	@Override
	protected List<ToolbarItem> getToolbarItems(kbee.web.console.BaseBrowser<Site> browser) {
		List<ToolbarItem> items = new ArrayList<ToolbarItem>();
		
		if (is_admin ||  is_root)
			items.add(new NewSiteButton(browser, ToolbarItem.Align.TOP_LEFT));
		// 	items.add(new SiteStatusSelector(browser, ToolbarItem.Align.TOP_RIGHT));
		return items;
	}

	@Override
	protected void addModals() {
		super.addModals();
	}

	protected Component newIcon() {
		return new WebMarkupContainer("icon");
	}

	
	
	/**
	 * 
	 * 
	 * 
	 * 
	 */
	@Override
	protected Panel getMenu(IModel<Site> model) {

		ContextMenuPanel<Site> menu = new ContextMenuPanel<Site>(model);
		menu.setOutputMarkupId(true);


		// -------------------- 
		//
		//
		menu.addItem(new MenuItemFactory<Site>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Site> getItem(String id) {
				return new MenuItemPanelV5<Site>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick() {
						//Site site=getModel().getObject();
						//com.novamens.portal6.model.Page page=site.getHomePage();
						setResponsePage( new PortalSiteEditorPage(getModel()));
					}

					@Override
					public String getLabel() {
						return getConsoleLabel("edit-site").getObject();
					}

					@Override
					public String getTarget() {
						return "_blank";
					}
					
					@Override
					public boolean isEnabled() {
						
						if (is_admin || is_support)
							return true;
						
						if (getModel().getObject().isPublic())
							return true;
						
						// return ServiceLocator.getService(PortalSecurityService.class).isReadSiteSessionUser(getModel().getObject());
						
						return true;
					}
				};
			}
		});


		
		
		
		
		
		
		// -------------------- 
		//
		//
		menu.addItem(new MenuItemFactory<Site>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Site> getItem(String id) {
				return new MenuItemPanelV5<Site>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick() {
						try {
						Site site = getModel().getObject();
						com.novamens.portal6.model.Page page =site.getHomePage();
						setResponsePage( new PortalPageStructureEditorPage(new ObjectModel<com.novamens.portal6.model.Page>(page)));
						} catch (Exception e) {
							logger.error(e);
							setResponsePage( new ApplicationErrorPage<Site>(e));
						}
					}

					@Override
					public String getLabel() {
						return getConsoleLabel("edit-home").getObject();
					}

					@Override
					public String getTarget() {
						return "_blank";
					}
					
					@Override
					public boolean isEnabled() {
						
						if (is_admin  )
							return true;
						
						if (getModel().getObject().isPublic())
							return true;
						
						// return ServiceLocator.getService(PortalSecurityService.class).isReadSiteSessionUser(getModel().getObject());
						
						return true;
					}
				};
			}
		});

		

		
		menu.addItem(new MenuItemFactory<Site>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Site> getItem(String id) {
				return new SeparatorMenuItemPanelV5<Site>(id) {
					private static final long serialVersionUID = 1L;

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

		// -------------------- 
		// Open Site
		//
		menu.addItem(new MenuItemFactory<Site>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Site> getItem(String id) {
				return new MenuItemPanelV5<Site>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick() {
						WebPage page=ServiceLocator.getService(PortalPanelService.class).getWebPage(getModel().getObject());
						setResponsePage(page);
					}
					@Override
					public String getLabel() {
						return getConsoleLabel("open").getObject();
					}
					@Override
					public String getTarget() {
						return "_blank";
					}
					@Override
					public boolean isEnabled() {
						if (is_admin || is_support  )
							return true;
						if (getModel().getObject().isPublic())
							return true;
						// return ServiceLocator.getService(PortalSecurityService.class).isReadSiteSessionUser(getModel().getObject());
						return true;
					}
				};
			}
		});

		

		
		// --------------------
		//
		// Open Editor  
		// 
		/**
		menu.addItem(new MenuItemFactory<Site>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Site> getItem(String id) {
				return new MenuItemPanelV5<Site>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick() {
						setResponsePage( ServiceLocator.getService(PortalPanelService.class).getEditorWebPage(getModel().getObject()));
					}
					
					@Override
					public String getLabel() {
						return getConsoleLabel("open-site-editor").getObject();
					}

					@Override
					public String getTarget() {
						return "_blank";
					}

					@Override
					public boolean isVisible() {
						return !getModel().getObject().isExternal();
					}
					
					@Override
					public boolean isEnabled() {
						return is_admin || is_support || is_portal;
					}
				};
			}
		});
*/
		
		// ---------------------------
		// Add To Favs
		//
		menu.addItem(new MenuItemFactory<Site>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Site> getItem(String id) {
				return new AjaxMenuItemPanelV5<Site>(id) {
					private static final long serialVersionUID = 1L;

					public void onClick(AjaxRequestTarget target) {
						try {
							PortalUserService srv = (PortalUserService) ((KbeeUser) getSessionUser()).getService(PortalUserService.class);
							srv.addFavorite(getModel().getObject());
							refresh(target);

						} catch (Exception e) {
							logger.error(e);
							getErrorDialog().open(target, new Model<String>(e.getClass().getSimpleName() + ". " + (e.getMessage() != null ? e.getMessage() != null : "")));
						}
					}

					@Override
					public String getLabel() {
						return getConsoleLabel("addtofavs").getObject();
					}

					@Override
					public String getWorkingLabel() {
						return getConsoleLabel("siteconsole.working").getObject();
					}

					@Override
					public boolean isEnabled() {
						return (getModel().getObject().getState() == ObjectState.ENABLED);
					}

					@Override
					public boolean isVisible() {
						return !((PortalUserService) ((KbeeUser) getSessionUser()).getService(PortalUserService.class)).isSiteInFavorites(getModel().getObject());
					}
				};
			}
		});

		// ---------------------------
		// Remove from Favs
		//
		menu.addItem(new MenuItemFactory<Site>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Site> getItem(String id) {
				return new AjaxMenuItemPanelV5<Site>(id) {
					private static final long serialVersionUID = 1L;

					public void onClick(AjaxRequestTarget target) {
						try {
							PortalUserService srv = (PortalUserService) ((KbeeUser) getSessionUser()).getService(PortalUserService.class);
							srv.removeFavorite(getModel().getObject());
							refresh(target);

						} catch (ServiceNotFoundException e) {
							logger.error(e);
							getErrorDialog().open(target, new Model<String>(e.getClass().getSimpleName() + ". " + (e.getMessage() != null ? e.getMessage() != null : "")));
						}
					}

					@Override
					public String getLabel() {
						return getConsoleLabel("removefromfavs").getObject();
					}

					@Override
					public String getWorkingLabel() {
						return getConsoleLabel("siteconsole.working").getObject();
					}

					@Override
					public boolean isEnabled() {
						return true;
					}

					@Override
					public boolean isVisible() {
						return ((PortalUserService) ((KbeeUser) getSessionUser()).getService(PortalUserService.class)).isSiteInFavorites(getModel().getObject());
					}
				};
			}
		});

		/**
		// ----------------------------
		// Archive
		//
		menu.addItem(new MenuItemFactory<Site>() {
			private static final long serialVersionUID = 1L;

			@Override
			public MenuItemPanel<Site> getItem(String id) {
				return new AjaxMenuItemPanelV5<Site>(id) {
					private static final long serialVersionUID = 1L;

					public void onClick(AjaxRequestTarget target) {
						try {
							getModel().getObject().setState(ObjectState.ARCHIVED);
							getModel().getObject().getService(SiteService.class).update("Archived");
							SitesConsole.this.refresh(target);

						} catch (ContentMgmtException | ServiceNotFoundException e) {
							logger.error(e);
							getErrorDialog().open(target, new Model<String>(e.getClass().getSimpleName() + ". "	+ (e.getMessage() != null ? e.getMessage() != null : "")));
						}
					}

					@Override
					public String getLabel() {
						return getConsoleLabel("archive").getObject();
					}

					@Override
					public boolean isEnabled() {
						if (! (getModel().getObject() instanceof DiagrammableSite))
							return false;
						return ServiceLocator.getService(PortalSecurityService.class).isAdminSiteSessionUser(getModel().getObject());
					}

					@Override
					public String getWorkingLabel() {
						return getConsoleLabel("siteconsole.working").getObject();
					}

					@Override
					public boolean isVisible() {
						if (! (getModel().getObject() instanceof DiagrammableSite))
							return false;
						if (getModel().getObject().isExternal())
							return false;
						return (getModel().getObject().getState() == ObjectState.ENABLED);
					}
				};
			}
		});
		*/

		// ----------------------------
		// Publish
		//
		menu.addItem(new MenuItemFactory<Site>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Site> getItem(String id) {
				return new AjaxMenuItemPanelV5<Site>(id) {
					private static final long serialVersionUID = 1L;

					public void onClick(AjaxRequestTarget target) {
						try {
							getModel().getObject().setState(ObjectState.ENABLED);
							getModel().getObject().getService(SiteService.class).update("Publish");
							SitesConsole.this.refresh(target);

						} catch (Exception e) {
							logger.error(e);
							getErrorDialog().open(target, new Model<String>(e.getClass().getSimpleName() + ". " + (e.getMessage() != null ? e.getMessage() != null : "")));
						}
					}

					@Override
					public String getLabel() {
						return getConsoleLabel("publish").getObject();
					}

					@Override
					public boolean isEnabled() {
						return true;
						//return ServiceLocator.getService(PortalSecurityService.class).isAdminSiteSessionUser(getModel().getObject());
					}

					@Override
					public boolean isVisible() {
						return (getModel().getObject().getState() != ObjectState.ENABLED);
					}

					@Override
					public String getWorkingLabel() {
						return getConsoleLabel("siteconsole.working").getObject();
					}

				};
			}
		});

		
		menu.addItem(new MenuItemFactory<Site>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Site> getItem(String id) {
				return new SeparatorMenuItemPanelV5<Site>(id) {
					private static final long serialVersionUID = 1L;
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

		/**
		// -----------------------------------------------------
		// Archive all Areas (this is temporary for debuggin)
		//
		menu.addItem(new MenuItemFactory<Site>() {
			private static final long serialVersionUID = 1L;

			@Override
			public MenuItemPanel<Site> getItem(String id) {
				return new AjaxMenuItemPanelV5<Site>(id) {
					private static final long serialVersionUID = 1L;

					public void onClick(AjaxRequestTarget target) {
						try {
							// TODO VER
							// getModel().getObject().setStateAll(ObjectState.ARCHIVED);
							List<String> list = new ArrayList<String>();
							list.add("Archive all components");
							getModel().getObject().getService(SiteService.class).update(list);
							SitesConsole.this.refresh(target);

						} catch (ContentMgmtException | ServiceNotFoundException e) {
							logger.error(e);
							getErrorDialog().open(target, new Model<String>(e.getClass().getSimpleName() + ". " + (e.getMessage() != null ? e.getMessage() != null : "")));
						}
					}

					@Override
					public String getLabel() {
						return getConsoleLabel("archive-areas").getObject();
					}

					@Override
					public boolean isEnabled() {
						if ( !(getModel().getObject() instanceof DiagrammableSite))
							return false;
						return ServiceLocator.getService(PortalSecurityService.class).isAdminSiteSessionUser(getModel().getObject());
					}

					@Override
					public String getWorkingLabel() {
						return getConsoleLabel("siteconsole.working").getObject();
					}

					@Override
					public boolean isVisible() {

						if ( !(getModel().getObject() instanceof DiagrammableSite))
							return false;

						if (getModel().getObject().isExternal())
							return false;

						return (getModel().getObject().getState() == ObjectState.ENABLED);
					}
				};
			}
		});
		*/
		

		// 
		// Delete
		//
		menu.addItem(new MenuItemFactory<Site>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Site> getItem(String id) {
				return new AjaxMenuItemPanelV5<Site>(id) {
					private static final long serialVersionUID = 1L;

					public void onClick(AjaxRequestTarget target) {
						
						//if (ServiceLocator.getService(PortalSecurityService.class).isAdminSiteSessionUser(getModel().getObject())) {
							
							getConfirmationDialog()
									.open(target,
											getConsoleLabel("sitesconsole.deleteconfirmation.message",
													getModel().getObject().getTitle()),
											Dialog.Delete, new Dialog.Handler() {

												private static final long serialVersionUID = 1L;

												@Override
												public void onClick(AjaxRequestTarget target, Button button) {
													if (button.key().equals(Dialog.Delete.key())) {
														try {
															getModel().getObject().getService(SiteService.class).delete();

														} catch (Exception e) {
															logger.error(e);
															getErrorDialog().open(target,
																	new Model<String>(e.getClass().getSimpleName() + ". "
																					+ (e.getMessage() != null ? e.getMessage() : "") +" " + " <br />The Site will be marked as deleted"));
														
															
															Site site = getPortalDao().findSiteById(getModel().getObject().getId());
															if (site!=null) {
																site.getService(SiteService.class).markAsDeleted();
															}
															
														}
														SitesConsole.this.refresh(target);
													}
												}
											});
						// }
					}

					@Override
					public String getLabel() {
						return getConsoleLabel("delete").getObject();
					}

					@Override
					public boolean isEnabled() {
						return true;
						//return ServiceLocator.getService(PortalSecurityService.class).isAdminSiteSessionUser(getModel().getObject());
					}
				};
			}
		});
		
		return menu;
	}

	
	
	

	/**
	 * 
	 * 
	 */
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {

		if (this.columns != null)
			return this.columns;

		this.columns = new ArrayList<GridColumn<SearchResult, String>>();

		this.columns.add(new GridColumn<SearchResult, String>("title", getLabel("title"), "title") {
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				Object object = resultmodel.getObject().getObject();
				IModel<Site> objectmodel = getModel((Site) object);
				cellItem.add(new TargetBlankObjectTitleColumnPanel<Site>(componentId, objectmodel) {
					@Override
					protected String getCss() {
						return "cell-label btn-link";
					}
				});
			}
			@Override
			public String getCssClass() {
				return "col title col-xs-1 col-md-1 col-lg-1";
			}
			@Override
			protected String getContextKey() {
				return SitesConsole.this.getName() + super.getContextKey();
			}
		});

		this.columns.add(new GridColumn<SearchResult, String>("template", getLabel("template")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				try {
					User user = getUser();
					Locale locale = (user != null ? user.getLocale() : Locale.getDefault());
					if (((Site) object.getObject()).getSiteType() == null)
						return new Model<String>("");
					else {
						String la = (((Site) object.getObject()).getSiteType().getLabel(locale));
						return new Model<String>(la);
					}
				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass().getName());
				}
			}

			@Override
			protected String getContextKey() {
				return SitesConsole.this.getName() + super.getContextKey();
			}
		});
		
		

		DataSet portal=getContentDao().findDataSetByAlias(DataSet.PORTAL, getDomain().getId());
		
		if (portal!=null)  {
		
	        for (ModelElementTemplate template : portal.getStructure()) {
	            if (template != null) {
	                if (template.getElement() != null && template.getElement() instanceof Classifier) {
	                    if (((Classifier) template.getElement()).getState() == ObjectState.ENABLED) {
	                        ClassifierColumn<DataSetMember> cc = new ClassifierColumn<DataSetMember>(new ObjectModel<Classifier>((Classifier) template.getElement()), this.getName());
	                        if (((Classifier) template.getElement()).isDefaultStructure())
	                            cc.setPreferred(true);
	                        else if (((Classifier) template.getElement()).isDefaultGridColumn())
	                            cc.setPreferred(true);
	                        else
	                            cc.setPreferred(false);
	
	                        this.columns.add(cc);
	                    }
	                } else {
	                    if (template.getElement() != null && template.getElement() instanceof Attribute) {
	                        if (((Attribute) template.getElement()).getState() == ObjectState.ENABLED) {
	                            GridColumn<SearchResult, String> gc = new GridColumn<SearchResult, String>(String.valueOf(template.getElement().getId()), new Model<String>(template.getElement().getName())) {
	                                @Override
	                                protected IModel<String> getLabelModel(SearchResult object) {
	                                    StringBuilder str = new StringBuilder();
	                                    try {
	                                        DataSetMember member = (DataSetMember) object.getObject();
	                                        for (ModelElementTemplate template : member.getDataSet().getStructure()) {
	                                            if (template != null && String.valueOf(template.getElement().getId()).equals(this.getId())) {
	                                                for (String s : member.getAttributeValues((Attribute) template.getElement())) {
	                                                    if (str.length() > 0)
	                                                        str.append(", ");
	                                                    str.append(s);
	                                                }
	                                                break;
	                                            }
	                                        }
	                                    } catch (Exception e) {
	                                        logger.error(e);
	                                        str.append(e.getClass().getName() + " | " + e.getMessage());
	                                    }
	                                    return new Model<String>(str.toString());
	                                }
	
	                                @Override
	                                protected String getContextKey() {
	                                    return SitesConsole.this.getName() + super.getContextKey();
	                                }
	                            };
	
	                            gc.setPreferred(false);
	                            this.columns.add(gc);
	                        }
	                    }
	                }
	            }
	        }
		}
		

		
		
		
		
		
		
		/**
		this.columns.add(new GridColumn<SearchResult, String>("type", getLabel("type")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				try {
					User user = getUser();
					Locale locale = (user != null ? user.getLocale() : Locale.getDefault());

					if (((Site) object.getObject()).getSiteType() == null)
						return new Model<String>("");
					else
						return new Model<String>((((Site) object.getObject()).getSiteType().getLabel(locale)));
				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass().getName());
				}
			}

			@Override
			protected String getContextKey() {
				return SitesConsole.this.getName() + super.getContextKey();
			}
		});
		
		*/

		this.columns.add(new LastModifiedColumn<Site>("modified", getLabel("modified"), "modified") {
			private static final long serialVersionUID = 1L;

			@Override
			protected String getContextKey() {
				return SitesConsole.this.getName() + super.getContextKey();
			}
		});

		this.columns.add(new GridColumn<SearchResult, String>("access", getLabel("access")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				try {
					return new StringResourceModel((((Site) object.getObject()).isPublic() ? "public" : "private"),
							SitesConsole.this, null);
				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass().getName());
				}
			}

			@Override
			protected String getContextKey() {
				return SitesConsole.this.getName() + super.getContextKey();
			}
		});

		this.columns.add(new GridColumn<SearchResult, String>("subtitle", getLabel("column.subtitle")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				try {
					if (((Site) object.getObject()).getSubtitle() == null)
						return new Model<String>("");
					else
						return new Model<String>(((Site) object.getObject()).getSubtitle());
				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass().getName());
				}
			}

			@Override
			protected String getContextKey() {
				return SitesConsole.this.getName() + super.getContextKey();
			}

			@Override
			public boolean isPreferred() {
				return false;
			}
		});

		this.columns.add(new GridColumn<SearchResult, String>("external", getLabel("external-title")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				try {
					return new StringResourceModel((((Site) object.getObject()).isExternal() ? "external" : "intranet"),
							SitesConsole.this, null);
				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass().getName());
				}

			}

			@Override
			protected String getContextKey() {
				return SitesConsole.this.getName() + super.getContextKey();
			}

			@Override
			public boolean isPreferred() {
				return false;
			}

		});

		this.columns.add(new GridColumn<SearchResult, String>("url", getLabel("url"), "url") {
			private static final long serialVersionUID = 1L;

			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				try {
					return new Model<String>(((Site) object.getObject()).getUrl());
				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass().getName());
				}
			}

			@Override
			protected String getContextKey() {
				return SitesConsole.this.getName() + super.getContextKey();
			}

			@Override
			public boolean isPreferred() {
				return true;
			}
		});

		/**
		this.columns.add(new GridColumn<SearchResult, String>("tools", getLabel("tools")) {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				try {
					return new StringResourceModel((((Site) object.getObject()).isDetailToolsEnabled() ? "yes" : "no"),
							SitesConsole.this, null);
				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass().getName());
				}
			}

			@Override
			protected String getContextKey() {
				return SitesConsole.this.getName() + super.getContextKey();
			}

			@Override
			public boolean isPreferred() {
				return false;
			}
		});
		*/

		/**
		this.columns.add(new GridColumn<SearchResult, String>("suscriptors", getLabel("suscriptors")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				try {
					Site site = ((Site) object.getObject());

					if (((Site) object.getObject()).isExternal())
						return new Model<String>("");
					else
						return new Model<String>(String.valueOf(getTotalSubs(site)));
				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass().getName());
				}
			}

			@Override
			protected String getContextKey() {
				return SitesConsole.this.getName() + super.getContextKey();
			}

			@Override
			public boolean isPreferred() {
				return false;
			}

			@Override
			public String getCssClass() {
				return "col col-xs-1 col-md-1 col-lg-1 ui-resizable centered";
			}

			@Override
			public boolean isEscapeModelString() {
				return false;
			}

			
			@Override
			protected String getLabelCss() {
				return "number-md";
			}

			private int getTotalSubs(Site site) {
				try {
					
					return site.getService(SiteSubscriptionService.class).getTotalSubscribers(SiteSubscriptionEvent.SITE_PUBLISH_CONTENT);
					
				} catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}

		});
		*/

		columns.add(new GridColumn<SearchResult, String>("status", getLabel("status")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected IModel<String> getLabelModel(SearchResult result) {

				if (result.getObject() == null)
					return new Model<String>("err");

				try {					
					ObjectState state = ((Site) result.getObject()).getState();

					if (state == null)
						return new Model<String>("err");

					return new Model<String>(state.getHTMLLabel(getUser().getLocale()));
				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass().getName());
				}

			}

			@Override
			protected String getContextKey() {
				return SitesConsole.this.getName() + super.getContextKey();
			}

			@Override
			public boolean isPreferred() {
				return false;
			}
		});

		this.columns.add(new GridColumn<SearchResult, String>("id", getLabel("id")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				try {												
					return new Model<String>(String.valueOf(((Site) object.getObject()).getOId()));
				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass().getName());
				}
			}

			@Override
			protected String getContextKey() {
				return SitesConsole.this.getName() + super.getContextKey();
			}

			@Override
			public boolean isPreferred() {
				return false;
			}
		});

		return this.columns;
	}
	


	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}

	
	protected boolean isFavorite(IModel<Site> sitemodel) {
		try {
			UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
			KbeeUser user = (KbeeUser) userProfile.getUser();
			PortalUserService srv = (PortalUserService) user.getService(PortalUserService.class);
			return srv.isSiteInFavorites(sitemodel.getObject());

		} catch (Exception e) {
			logger.error(e);
			return false;
		}
		
	}
	
	@Override
	protected String getIcon(IModel<Site> model) {
		return null;
	}
	
	protected PortalDao getPortalDao() {
		return (PortalDao)ServiceLocator.getService(BeansService.class).getBean("portalDao");
	}	
	
}
