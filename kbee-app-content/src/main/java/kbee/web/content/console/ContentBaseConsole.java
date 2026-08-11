package kbee.web.content.console;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import com.novamens.indexer.query.*;
import com.novamens.kbee.wicket.markup.html.console.browser.GridMenu;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarButton;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.library.Library;
import com.novamens.content.library.LibraryService;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.multidimensional.FacetWrapper;
import com.novamens.content.service.ContentService;
import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListItem;
import com.novamens.content.userlist.UserListService;
import com.novamens.content.web.console.markup.GlyphiconColumnPanel;
import com.novamens.content.web.content.markup.GenericBatchActionPage;
import com.novamens.content.web.security.markup.AclModal;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.library.IqlCriteria;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.browser.DeleteButton;
import com.novamens.kbee.wicket.markup.html.console.browser.AjaxToolbarButton;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.GridPanel;
import com.novamens.kbee.wicket.markup.html.console.grid.LastModifiedColumn;
import com.novamens.kbee.wicket.markup.html.console.panel.SubMenuAjaxUserListItemPanel;
import com.novamens.portal6.model.Site;
import com.novamens.service.ContentExportService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.BreadCrumb;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Process;

import kbee.web.console.AdvancedSearchContentSelectorPanel;
import kbee.web.console.BaseBrowser;
import kbee.web.console.TitleColumnPanel;
import kbee.web.console.grid.AttributeColumn;
import kbee.web.console.grid.AttributeDateColumn;
import kbee.web.console.grid.ClassifierColumn;
import kbee.web.console.tools.ExportContentToolButton;
import kbee.web.content.nav.ContentNavigationBar;
import kbee.web.content.panel.ShareModal;
import kbee.web.datamanagement.TagManagementPage;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.event.wicket.ErrorEvent;

import kbee.web.nav.ContentBaseBC;
import kbee.web.nav.ContentSectionBC;
import kbee.web.nav.NavigablePage;
import kbee.web.object.AuditTrailModal;
import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ExportContentsPage;
import kbee.web.query.LibraryQuery;
import kbee.web.searcher.panel.SearcherSimpleErrorPanel;

public abstract class ContentBaseConsole extends ContentConsole<Content> {

	private static final long serialVersionUID = 1L;

	static final public String NAME = "contentbase";

	final boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot();

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ContentBaseConsole.class.getName());

	private List<GridColumn<SearchResult, String>> columns;
	private IModel<Library> librarymodel;

	private List<ToolbarItem> items = null;
	private List<ToolbarItem> selection_toolbar;

	public ContentBaseConsole(String name, IModel<Library> model, Query query) {
		super(name, query);
		setLibraryModel(model);
	}

	@Override
	public void onDetach() {
		super.onDetach();

		try {

			this.items = null;

			if (this.librarymodel != null)
				this.librarymodel.detach();

			if (this.selection_toolbar != null) {
				for (ToolbarItem item : selection_toolbar) {
					item.detach();
				}
			}
			for (GridColumn<?, ?> column : getColumns())
				column.detach();

		} catch (Exception e) {
			logger.error(e);
		}
	}

	protected boolean isDefaultTopPanelVisible() {
		return false;
	}

	@Override
	protected GridMenu getGridToolbarMenuItem() {
		GridMenu gridMenu = super.getGridToolbarMenuItem();

		gridMenu.addItem((itemId) -> new AjaxMenuItemPanelV5<Void>(itemId) {
			@Override
			public void onClick(AjaxRequestTarget target) throws Exception {
				final int maxItems = 1000;
				List<IModel<Content>> list = new ArrayList<>();
				ResultSet rs = ContentBaseConsole.this.getBrowser().getQuery().execute();
				final int currentSize = rs.size();
				if (currentSize < maxItems) {
					while (rs.hasNext()) {
						list.add(new ObjectModel<Content>((Content) rs.next().getObject()));
					}
					TagManagementPage page = new TagManagementPage();
					page.setSelection(list);
					setResponsePage(page);
				} else {
					getErrorDialog().open(target, () -> getString("information"), getLabel("gridTagTool.tooManyItems", String.valueOf(maxItems), String.valueOf(currentSize)));
				}
			}

			protected IModel<String> getLabel(String key, String... parameter) {
				StringResourceModel model = new StringResourceModel(key, this);
				model.setParameters((Object[]) parameter);
				return model;
			}

			@Override
			public String getLabel() {
				return getString("tools.openInTagTool");
			}

			@Override
			public boolean isEnabled() {
				return isAdmin() || isRoot() || isSupport();
			}

			@Override
			public boolean isVisible() {
				return isAdmin() || isRoot() || isSupport();
			}
		});
		return gridMenu;
	}

	/**
	 *
	 * 
	 */
	@Override
	public void addListeners() {
		super.addListeners();
	}

	/**
	 * 
	 */
	@Override
	public Query newQuery() {
		return setUserPreference(new LibraryQuery(getQueryIndex(), getLibrary()));
	}

	/**
	 * 
	 */
	protected Library getLibrary() {
		if (getLibraryModel() == null) {
			Library library = getDomain().getService(LibraryService.class).getDefault();
			setLibraryModel(new ObjectModel<Library>(library));
		}
		return getLibraryModel().getObject();
	}

	/**
	 * 
	 */
	protected List<Library> getLibraries() {
		List<Library> cabinets = new ArrayList<Library>();
		for (Library cabinet : getRepository(Library.class).findAll()) {
			if (cabinet.isReadable())
				cabinets.add(cabinet);
		}
		;
		return cabinets;
	}

	protected IModel<Library> getLibraryModel() {
		return librarymodel;
	}

	protected void setLibraryModel(IModel<Library> model) {
		this.librarymodel = model;
	}

	@Override
	protected boolean isReadOnly() {
		return getLibrary().isReadOnly() && !isRoot();
	}

	@Override
	protected BreadCrumb getBreadCrumb() {
		return new BreadCrumb(new ContentSectionBC());
	}

	@Override
	protected boolean isEditionEnabled() {
		return true;
	}

	/**
	 * Console Searcher and item index
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected Page getPageV6(IModel<Content> model) {
		Page page;
		try {
			page = (Page) ServiceLocator.getService(BeansService.class).getBean(getContentClass(model.getObject()) + "-page", model);
			if (page instanceof NavigablePage<?>) {
				((NavigablePage<Content>) page).setNavigator(getNavigator(model));
			}
		} catch (Exception e) {
			page = new kbee.web.error.ApplicationErrorPage<>(e);
		}
		return page;

	}

	/**
	 * External contents are read only except for root
	 */
	@Override
	protected Panel getMenu(IModel<Content> model) {

		ContextMenuPanel<Content> menu = new ContextMenuPanel<Content>(model);

		menu.addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new MenuItemPanelV5<Content>(id) {
					public void onClick() {
						try {
							setResponsePage(ContentBaseConsole.this.getPageV6(getModel()));
						} catch (Exception e) {
							logger.error(e);
							setResponsePage(new ApplicationErrorPage<>(e));
						}
					}

					@Override
					public String getLabel() {
						return ContentBaseConsole.this.getLabel("contentbase.contextmenu.open").getObject();
					}
				};
			}
		});

		menu.addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new AjaxMenuItemPanelV5<Content>(id) {
					@SuppressWarnings("unchecked")
					public void onClick(AjaxRequestTarget target) {
						Modal modal = ContentBaseConsole.this.getSendByEmailModal();
						((ShareModal<Content>) modal).open(target, getModel());
					}

					@Override
					public String getLabel() {
						return ContentBaseConsole.this.getLabel("contentbase.contextmenu.share").getObject();
					}

					@Override
					public boolean isEnabled() {
						if (isSupportUser())
							return false;
						return isRoot() || isSendByEmail();
					}

				};
			}
		});

		menu.addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new SubMenuAjaxUserListItemPanel<Content>(id, model, ContentBaseConsole.this.getName(), UserListItem.PUBLISHED);
			}
		});

		menu.addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new AjaxMenuItemPanelV5<Content>(id) {
					@SuppressWarnings("unchecked")
					public void onClick(AjaxRequestTarget target) {
						Modal modal = ContentBaseConsole.this.getAuditTrailModal();
						((AuditTrailModal<Content>) modal).open(target, getModel());
					}

					@Override
					public String getLabel() {
						return ContentBaseConsole.this.getLabel("contentbase.contextmenu.audittrail").getObject();
					}

					@Override
					public boolean isEnabled() {
						if (isSupportUser())
							return true;
						if (isWriteable(getModel()))
							return true;
						if (isAuditReadable(getModel()))
							return true;
						return false;
					}
				};
			}
		});

		/***
		 */
		menu.addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5<Content>(id) {
					@Override
					public String getLabel() {
						return ContentBaseConsole.this.getLabel("contentbase.contextmenu.download").getObject();
					}

					@Override
					public boolean isDeleteFileAfterDownload() {
						return true;
					}

					@Override
					protected File getFile() {
						return getModelObject().getService(ContentExportService.class).getHTMLExport();
					}

					@Override
					public boolean isEnabled() {
						if (isSupportUser())
							return false;
						return isRoot() || isSendByEmail();
					}

					@Override
					public boolean isVisible() {
						return true;
					}
				};
			}
		});

		int index = 0;
		final int size = getLaunchers(model).size();

		for (int process_launcher = 0; process_launcher < size; process_launcher++) {

			final int p_i = index++;

			menu.addItem(new MenuItemFactory<Content>() {
				@Override
				public AbstractMenuItemPanelV5<Content> getItem(String id) {
					return new MenuItemPanelV5<Content>(id) {
						public void onClick() {
							try {
								if (!getModel().getObject().isLocked()) {
									Procedure procedure = null;
									procedure = getLaunchers(model).get(p_i).getProcedure();

									Content content = getModel().getObject();

									// El start process hace el checkout dentro de la misma transaccion
									Process process = content.getService(WorkflowService.class).startProcess(procedure);
									Content newcontent = ((KbeeContext) process.getContext()).getContent();
									IModel<Content> model = ContentBaseConsole.this.getModel(newcontent);
									model.detach();
									Page page = ContentBaseConsole.this.getTaskPage(model);
									setResponsePage(page);
								}
							} catch (Exception e) {
								logger.error(e);
								setResponsePage(new ApplicationErrorPage<>(e));

							}
						}

						/**
						 * @Override public PopupSettings getPopupSettings() { return new
						 *           PopupSettings(PopupSettings.LOCATION_BAR | PopupSettings.MENU_BAR |
						 *           PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS |
						 *           PopupSettings.STATUS_BAR | PopupSettings.TOOL_BAR); }
						 **/
						public String getLabel() {
							return ContentBaseConsole.this.getLabel("contentbase.contextmenu.checkout").getObject() + " - " + getLaunchers(model).get(p_i).getDisplayName();
						}

						@Override
						public boolean isVisible() {
							if (!ContentBaseConsole.this.getLibrary().isReadable())
								return false;
							if (!isWriteable(getModel()))
								return false;
							if (getDomain().getService(WorkflowDomainService.class) != null && getLaunchers(model).size() > 0 && getLaunchers(model).get(p_i).executeable())
								return true;
							return false;
						}

						@Override
						public boolean isEnabled() {
							if (isSupportUser() && !isRoot())
								return false;
							return !getModel().getObject().isLocked();
						}

					};
				}
			});

		}

		menu.addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new AjaxMenuItemPanelV5<Content>(id) {
					public void onClick(AjaxRequestTarget target) {

						if (!getContent().isLocked()) {
							getContent().getService(ContentService.class).archive();
							resetSelection();
						}
						refresh(target);
					}

					@Override
					public String getLabel() {
						return ContentBaseConsole.this.getLabel("contentbase.contextmenu.sendtoarchive").getObject();
					}

					@Override
					public String getWorkingLabel() {
						return ContentBaseConsole.this.getLabel("contentbase.contextmenu.sendtoarchive.working").getObject();
					}

					@Override
					public boolean isVisible() {
						// if (getModel().getObject().isExternal() && !isRoot())
						// return false;
						return true;
					}

					@Override
					public boolean isEnabled() {

						if (isSupportUser() && !isRoot())
							return false;

						return !getContent().isLocked() && isWriteable(getModel());
					}

					public Content getContent() {
						return getModel().getObject();
					}
				};
			}
		});

		menu.addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new SeparatorMenuItemPanelV5<Content>(id) {
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

		menu.addItem(new MenuItemFactory<Content>() {
			@Override
			public AbstractMenuItemPanelV5<Content> getItem(String id) {
				return new AjaxMenuItemPanelV5<Content>(id) {
					public void onClick(AjaxRequestTarget target) {
						if (!getContent().isLocked()) {
							try {

								getContent().getService(ContentService.class).recycle();

							} catch (ContentMgmtException | ServiceNotFoundException e) {
								logger.error(e, (getSessionUser() != null ? getSessionUser().getUserName() : "null"));
								fire(new ErrorEvent<>(target, e));
							}
							resetSelection();
						}
						refresh(target);
					}

					@Override
					public String getLabel() {
						return ContentBaseConsole.this.getLabel("contentbase.contextmenu.delete").getObject();
					}

					@Override
					public String getWorkingLabel() {
						return ContentBaseConsole.this.getLabel("contentbase.contextmenu.delete.working").getObject();
					}

					@Override
					public boolean isVisible() {
						return true;
					}

					@Override
					public boolean isEnabled() {

						if (isSupportUser() && !isRoot())
							return false;

						return !getContent().isLocked() && isDeleteable(getModel());

					}

					public Content getContent() {
						return getModel().getObject();
					}
				};
			}
		});

		return menu;
	}

	/**
	 * 
	 * This list is used by the {@link GridPanel}
	 * 
	 */
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {

		if (this.columns != null)
			return this.columns;

		this.columns = new ArrayList<GridColumn<SearchResult, String>>();

		this.columns.add(new GridColumn<SearchResult, String>("locked", getLabel("lockedcolumn")) {

			public boolean isHeaderMenu() {
				return false;
			}

			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				Object object = resultmodel.getObject().getObject();
				IModel<Content> objectmodel = getModel((Content) object);

				cellItem.add(new GlyphiconColumnPanel<Content>(componentId, objectmodel) {
					@Override
					public String getCss() {
						return "cell-icon fal fa-lock";
					}

					@Override
					public boolean isVisible() {
						return getModelObject().isLocked();
					};

					protected IModel<String> getAnchorTitle() {
						try {
							if (getModelObject().isLocked()) {
								StringBuilder str = new StringBuilder();
								String name;
								Long oid = getModel().getObject().getOId();
								if (oid != null) {
									Content content = getContentDao().findWorkspaceCopyContentByOId(oid);
									if (content != null) {
										name = getContentDao().findUserProfileByUserId(content.getWorkspace()).getPersonFirstLastName();
										str.append(name);
									}
								}
								return new Model<String>(str.toString());
							}
						} catch (Exception e) {
							logger.error(e, (getSessionUser() != null ? getSessionUser().getUserName() : "null"));
							return new Model<String>(e.getClass().getSimpleName());
						}
						return null;
					}
				});
			}

			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				Content content = (Content) object.getObject();
				return () -> content.isLocked() ? "locked" : "unlocked";
			}

			@Override
			protected String getContextKey() {
				return ContentBaseConsole.this.getName() + super.getContextKey();
			}

			@Override
			public int getWidth() {
				return GridPanel.ICON_COL_WIDTH;
			}

			@Override
			public int getXPadding() {
				return 3;
			}

			// Si es Fixed no debe ser Preferred
			@Override
			public boolean isPreferred() {
				return false;
			}

			// No se puede ocultar / mostrar, ni cambiar de orden en las cols.
			@Override
			public boolean isFixed() {
				return true;
			}

			@Override
			public boolean isResizable() {
				return false;
			}

			@Override
			public String getCssClass() {
				return "col short col-xs-1 col-md-1 col-lg-1";
			}
		});

		this.columns.add(new GridColumn<SearchResult, String>("mylists", getLabel("mylists")) {

			@Override
			public String getCssClass() {
				return super.getCssClass() + " mylist";
			}

			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				try {
					List<UserList> list = ((KbeeUser) getSessionUser()).getService(UserListService.class).getUserLists(ContentBaseConsole.this.getName(), (Content) object.getObject());
					if (list == null)
						return new Model<String>("");
					StringBuilder str = new StringBuilder();
					for (UserList u : list) {
						if (str.length() > 0)
							str.append(", ");
						str.append(u.getTitle());
					}
					return new Model<String>(str.toString());

				} catch (Exception e) {
					logger.error(e, getSessionUser().getUserName());
					return new Model<String>(e.getClass().getName() + " " + e.getMessage());
				}
			}

			@Override
			protected String getContextKey() {
				return ContentBaseConsole.this.getName() + super.getContextKey();
			}

			@Override
			public boolean isPreferred() {
				return false;
			}

		});

		this.columns.add(new GridColumn<SearchResult, String>("title", getLabel("titlecolumn"), "title_sort") {
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				Object object = resultmodel.getObject().getObject();
				IModel<Content> objectmodel = getModel((Content) object);
				cellItem.add(new TitleColumnPanel<Content>(componentId, objectmodel) {
					protected String getCss() {
						return "btn-link";
					}
				});

			}

			@Override
			public String getCssClass() {
				return "col title col-xs-1 col-md-1 col-lg-1";
			}

			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				Content content = (Content) object.getObject();
				return () -> content.getTitle();
			}

			@Override
			protected String getContextKey() {
				return ContentBaseConsole.this.getName() + super.getContextKey();
			}

			@Override
			public int getDefaultWidth() {
				return GridColumn.DEFAULT_TITLE_COLUMN_WIDTH;
			}

		});

		this.columns.add(new LastModifiedColumn<Content>("date", getLabel("datecolumn"), "modified") {
			private static final long serialVersionUID = 1L;

			@Override
			protected String getContextKey() {
				return ContentBaseConsole.this.getName() + super.getContextKey();
			}
		});

		String key = getLibrary().getKey();

		// Content Type
		//
		for (Classifier classifier : getClassifiers()) {
			if (classifier.isContentType()) {
				if (classifier.isVisible(key) && classifier.getState() == ObjectState.ENABLED) {
					this.columns.add(new ClassifierColumn<Content>(new ObjectModel<Classifier>(classifier), this.getName()));
				}
			}
		}

		for (Classifier classifier : getClassifiers()) {
			if (!classifier.isContentType()) {
				if (classifier.isVisible(key) && classifier.getState() == ObjectState.ENABLED) {
					this.columns.add(new ClassifierColumn<Content>(new ObjectModel<Classifier>(classifier), this.getName()));
				}
			}
		}

		this.columns.add(new GridColumn<SearchResult, String>("contentclass", getLabel("contentclasscolumn")) {
			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				return new Model<String>(((Content) object.getObject()).getContentTemplate().getDisplayName());
			}

			@Override
			protected String getContextKey() {
				return ContentBaseConsole.this.getName() + super.getContextKey();
			}
		});

		for (Attribute attribute : getAttributes()) {
			if (attribute.getState() == ObjectState.ENABLED && attribute.isVisible(key)) {
				if (attribute.isDate())
					this.columns.add(new AttributeDateColumn(new ObjectModel<Attribute>(attribute), getName()));
				else
					this.columns.add(new AttributeColumn(new ObjectModel<Attribute>(attribute), getName()));
			}
		}

		this.columns.add(new GridColumn<SearchResult, String>("modifieduser", getLabel("modifieduser")) {
			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				try {
					return new Model<String>(String.valueOf(((Content) object.getObject()).getLastModifiedUser().getFirstLastName()));
				} catch (Exception e) {
					return new Model<String>(e.getClass().getSimpleName());
				}
			}

			@Override
			protected String getContextKey() {
				return ContentBaseConsole.this.getName() + super.getContextKey();
			}

			@Override
			public boolean isPreferred() {
				return false;
			}
		});

		this.columns.add(new GridColumn<SearchResult, String>("id", getLabel("idcolumn")) {
			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				return new Model<String>(String.valueOf(((Content) object.getObject()).getOId()));
			}

			@Override
			protected String getContextKey() {
				return ContentBaseConsole.this.getName() + super.getContextKey();
			}

			@Override
			public boolean isPreferred() {
				return false;
			}
		});

		{
			// KbeePredicateGridColumn<SearchResult> column = new
			// KbeePredicateGridColumn<>("subtitle", getLabel("subtitle"), obj ->
			// getSubtitleColumn(obj) );
			// column.setContextKey(this.getName() + column.getContextKey());
			// this.columns.add(column);

			this.columns.add(new GridColumn<SearchResult, String>("subtitle", getLabel("subtitle")) {
				@Override
				protected IModel<String> getLabelModel(SearchResult object) {
					return new Model<String>(getSubtitleColumn(object));
				}

				@Override
				protected String getContextKey() {
					return ContentBaseConsole.this.getName() + super.getContextKey();
				}

				@Override
				public boolean isPreferred() {
					return false;
				}
			});

		}

		return this.columns;
	}

	protected String getSubtitleColumn(SearchResult obj) {
		try {
			Content c = (Content) obj.getObject();
			String ty = c.getService(ContentService.class).getConsoleSubtitleDefaultIfNull();
			return ty;
		} catch (Exception e) {
			logger.error(e);
			return e.getClass().getName();
		}

	}

	/***
	 * 
	 * SELECTED ITEMS (LEFT) These are actions that apply to the selected items in
	 * the Grid
	 */
	@Override
	protected List<ToolbarItem> getSelectionToolbarItems(BaseBrowser<Content> browser) {

		if (this.selection_toolbar != null)
			return this.selection_toolbar;

		this.selection_toolbar = new ArrayList<ToolbarItem>();

		// Checkout
		//
		//
		this.selection_toolbar.add(new AjaxToolbarButton(browser, ToolbarItem.Align.TOP_LEFT) {

			@Override
			public void onClick(AjaxRequestTarget target) {

				GenericBatchActionPage page = new GenericBatchActionPage(ContentBaseConsole.this.getBrowser().getSelection()) {

					public String getIcon() {
						return "far fa-inbox fa-fw";
					}

					@Override
					public boolean isEnabled() {

						return true;
					}

					public IModel<String> getTitle() {
						return getConsoleLabel("contentbase.batch.checkout");
					}

					public IModel<String> getType() {
						return getConsoleLabel("contentbase.batch.class");
					}

					@Override
					public IModel<String> getExecuteButtonLabel() {
						return getConsoleLabel("checkout");
					}

					@Override
					public void onReturn() {
						setResponsePage(getConsolePage(getQuery()));
					}

					public List<ProcessLauncher> getLaunchers(IModel<Content> model) {
						return getDomain().getService(WorkflowDomainService.class) == null ? new ArrayList<ProcessLauncher>() : getDomain().getService(WorkflowDomainService.class).getContextLaunchers(model.getObject());
					}

					@Override
					protected String executeAction(IModel<Content> model) {
						List<ProcessLauncher> launchers = getLaunchers(model);

						if (model.getObject().isLocked())
							return ContentBaseConsole.this.getLabel("locked").getObject();

						if (!isWriteable(model) || launchers.isEmpty())
							return ContentBaseConsole.this.getLabel("not-authorized").getObject();

						ProcessLauncher launcher = launchers.get(0);

						long start = System.currentTimeMillis();
						Content content = model.getObject().getService(ContentService.class).checkout();
						logger.debug("ContentService.class).update() -> " + String.valueOf(System.currentTimeMillis() - start) + " ms");

						content.getService(WorkflowService.class).startProcess(launcher);
						return "";
					}

					@Override
					protected Page getPage(IModel<Content> model) {
						Page page = ContentBaseConsole.this.getTaskPage(model);
						((AbstractApplicationPage<?>) page).setTopNavigation(new ContentNavigationBar<Content>(model));
						return page;
					}
				};

				List<BCElement> list = new ArrayList<BCElement>();
				list.add(new ContentSectionBC());
				list.add(new ContentBaseBC());
				list.add(new BCElement() {
					public IModel<String> getLabel() {
						return new Model<String>(ContentBaseConsole.this.getLibraryModel().getObject().getDisplayName());
					}

					public void onClick() {
						setResponsePage(new ContentBasePage());
					}
				});

				list.add(new BCElement(getConsoleLabel("checkout")));
				page.setBreadCrumb(list);
				setResponsePage(page);
			}

			@Override
			protected String getLabelStr() {
				return new StringResourceModel("tools.checkout", this, null).getObject();
			}

			@Override
			protected String getIcon() {
				return "far fa-inbox fa-fw";
			}

			@Override
			public boolean isEnabled() {
				if (!super.isEnabled())
					return false;
				return true;
			}
		});

		// Archive
		//
		//
		this.selection_toolbar.add(new AjaxToolbarButton(browser, ToolbarItem.Align.TOP_LEFT) {
			@Override
			public void onClick(AjaxRequestTarget target) {

				GenericBatchActionPage page = new GenericBatchActionPage(ContentBaseConsole.this.getBrowser().getSelection()) {
					public String getIcon() {
						return "far fa-archive";
					}

					@Override
					public boolean isEnabled() {

						return true;
					}

					public IModel<String> getTitle() {
						return getConsoleLabel("contentbase.batch.archive");
					}

					public IModel<String> getType() {
						return getConsoleLabel("contentbase.batch.class");
					}

					@Override
					public void onReturn() {
						setResponsePage(getConsolePage(getQuery()));
					}

					@Override
					public IModel<String> getExecuteButtonLabel() {
						return getConsoleLabel("archive");
					}

					@Override
					protected String executeAction(IModel<Content> model) {
						if (model.getObject().isLocked())
							return ContentBaseConsole.this.getLabel("locked").getObject();
						// if (model.getObject().isExternal() && !isRoot())
						// return ContentBaseConsole.this.getLabel("readonly").getObject();
						if (!isDeleteable(model))
							return ContentBaseConsole.this.getLabel("not-authorized").getObject();
						model.getObject().getService(ContentService.class).archive();
						return "";
					}

					@Override
					protected Page getPage(IModel<Content> model) {
						Page page = ContentBaseConsole.this.getPage(model);
						((AbstractApplicationPage<?>) page).setTopNavigation(new ContentNavigationBar<Content>(model));
						return page;
					}
				};

				List<BCElement> list = new ArrayList<BCElement>();
				list.add(new ContentSectionBC());
				list.add(new ContentBaseBC());
				list.add(new BCElement() {
					public IModel<String> getLabel() {
						return new Model<String>(ContentBaseConsole.this.getLibraryModel().getObject().getDisplayName());
					}

					public void onClick() {
						setResponsePage(new ContentBasePage());
					}
				});

				list.add(new BCElement(new StringResourceModel("tools.archive", ContentBaseConsole.this, null)));
				page.setBreadCrumb(list);
				setResponsePage(page);
			}

			@Override
			protected String getLabelStr() {
				return new StringResourceModel("tools.archive", this, null).getObject();
			}

			@Override
			protected String getIcon() {
				return "far fa-archive fa-fw";
			}

			@Override
			public boolean isEnabled() {
				if (!super.isEnabled())
					return false;
				// if (getLibrary().isReadOnly() && !isRoot())
				// return false;

				return true;
			}
		});

		//
		// Delete
		//
		//
		this.selection_toolbar.add(new DeleteButton(browser, ToolbarItem.Align.TOP_LEFT, true) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				List<BCElement> list = new ArrayList<BCElement>();
				list.add(new ContentSectionBC());
				list.add(new ContentBaseBC());
				list.add(new BCElement() {
					public IModel<String> getLabel() {
						return new Model<String>(ContentBaseConsole.this.getLibraryModel().getObject().getDisplayName());
					}

					public void onClick() {
						setResponsePage(new ContentBasePage());
					}
				});

				list.add(new BCElement(getLabel("contentbase.batch.delete")));

				GenericBatchActionPage page = new GenericBatchActionPage(ContentBaseConsole.this.getBrowser().getSelection()) {
					public String getIcon() {
						return "far fa-trash-alt";
					}

					public IModel<String> getTitle() {
						return getConsoleLabel("contentbase.batch.delete");
					}

					public IModel<String> getType() {
						return getConsoleLabel("contentbase.batch.class");
					}

					@Override
					public void onReturn() {
						setResponsePage(getConsolePage(getQuery()));
					}

					@Override
					protected String executeAction(IModel<Content> model) {
						if (model.getObject().isLocked())
							return ContentBaseConsole.this.getLabel("locked").getObject();
						if (!isDeleteable(model))
							return ContentBaseConsole.this.getLabel("not-authorized").getObject();
						try {
							model.getObject().getService(ContentService.class).recycle();
						} catch (ContentMgmtException | ServiceNotFoundException e) {
							logger.error(e, (getSessionUser() != null ? getSessionUser().getUserName() : "null"));
						}
						return "";
					}

					@Override
					protected String getExecuteButtonCss() {
						return "btn btn-sm btn-danger";
					}

					@Override
					public IModel<String> getExecuteButtonLabel() {
						return getConsoleLabel("delete");
					}

					@Override
					protected Page getPage(IModel<Content> model) {
						Page page = ContentBaseConsole.this.getPage(model);
						((AbstractApplicationPage<?>) page).setTopNavigation(new ContentNavigationBar<Content>(model));
						return page;
					}
				};

				page.setBreadCrumb(list);
				setResponsePage(page);
			}

			@Override
			public boolean isEnabled() {
				if (!super.isEnabled())
					return false;
				// if (getLibrary().isReadOnly() && !isRoot())
				// return false;
				return true;
			}
		});

		/**
		 * TAG TOOL
		 */
		this.selection_toolbar.add(new ToolbarButton(browser, ToolbarItem.Align.TOP_LEFT) {
			@Override
			public void onClick() {

				List<Content> list = new ArrayList<Content>();
				for (IModel<Content> mod : ContentBaseConsole.this.getBrowser().getSelection()) {
					list.add(mod.getObject());
				}
				kbee.web.datamanagement.TagManagementPage page = new TagManagementPage();
				page.setSelection(ContentBaseConsole.this.getBrowser().getSelection());
				setResponsePage(page);
			}

			@Override
			public boolean isEnabled() {
				if (!super.isEnabled())
					return false;
				return isAdmin() || isRoot();
			}

			@Override
			public boolean isVisible() {
				return isAdmin() || isRoot();
			}

			protected String getLabelStr() {
				return ContentBaseConsole.this.getLabel("tools.openInTagTool").getObject();
			}

		});

		// Export
		//
		this.selection_toolbar.add(new ExportContentToolButton<Content>(browser, ToolbarItem.Align.TOP_LEFT, true) {
			@Override
			protected void onClick(AjaxRequestTarget target) {
				setResponsePage(new ExportContentsPage(getListModel(), new ContentBaseBC()) {
					@Override
					public void onClose() {
						setResponsePage(new ContentBasePage());
					}
				});
				refresh(target);
			}
		});

		return this.selection_toolbar;
	}

	/***
	 * 
	 * Grid Toolbar
	 * 
	 * used by {@link GridPanel}
	 * 
	 * <b>LEFT</b>. New, Sub section Selector, and in some cases actions that apply
	 * to the selected items <b>RIGHT</b>. Actions that apply to all items in the
	 * current grid
	 * 
	 */
	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<Content> browser) {

		if (this.items == null) {
			this.items = super.getToolbarItems(browser);
		}

		InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					InfoDialog infoDialog = (InfoDialog) getInformationModal();
					infoDialog.open(target, () -> {
						return ContentBaseConsole.this.getName();
					}, new Model<String>(ContentBaseConsole.this.getDescription()));
				} catch (Exception e) {
					logger.error(e);
					fire(new ErrorEvent<>(target, e));
				}
			}

			@Override
			public boolean isVisible() {
				return true;
			}

		};
		this.items.add(infoButton);

		return this.items;
	}

	protected String getDescription() {
		StringBuilder str = new StringBuilder();
		if (getLibrary().getDescription() != null) {
			str.append("<section>");
			str.append("<h3>" + getLabel("description").getObject() + "</h3> <p>" + getLibrary().getDescription() + "</p>");
			str.append("</section>");
		}
		if (getLibrary().getCriteria() != null) {
			str.append("<section>");
			str.append("<h3>" + getLabel("criteria").getObject() + "</h3>");
			if (getLibrary().getCriteria() instanceof IqlCriteria)
				str.append("<p>" + ((IqlCriteria) getLibrary().getCriteria()).getStatement() + "</p>");
			str.append("</section>");
		}
		str.append("<section>");
		str.append("<h3>" + getLabel("settings-title").getObject() + "</h3>");
		str.append("<p>" + getLabel("open-settings").getObject() + " <a href= \"/libraries/" + getLibrary().getId().toString() + "\" target=\"_blank\">" + getLibrary().getName() + "</a></p>");
		str.append("</section>");

		return str.toString();
	}

	@Override
	protected boolean isVisible(Facet facet) {

		Facet realfacet;

		if (facet instanceof FacetWrapper) {
			boolean visible = ((FacetWrapper) facet).isVisible(getName());
			if (!visible)
				return false;
			realfacet = ((FacetWrapper) facet).getFacet();
		} else
			realfacet = facet;

		return !realfacet.getName().equals("state");
	}

	@Override
	protected void addModals() {
		super.addModals();
		add(new AclModal<Content>("acl-modal"));
	}

	public IModel<Site> getSiteModel() {
		return null;
	}

	@Override
	protected boolean hasTopPanel() {
		return true;
	}

	@Override
	protected Panel getTopPanel() {
		try {
			return new AdvancedSearchContentSelectorPanel("top", getName());

		} catch (Exception e) {
			logger.error(e);
			return new SearcherSimpleErrorPanel("top", e.getClass().getSimpleName(), e.getMessage());
		}
	}

	@Override
	public IModel<String> getDisplayName() {
		return new Model<String>(getLibrary().getDisplayName());
	}

	@Override
	protected String getIcon(IModel<Content> model) {
		if (model.getObject().isLocked())
			return "cell-icon fal fa-lock";

		if (isFolder(model))
			return "cell-icon fa-light fa-folder";

		return null;

	}

	protected boolean isCheckout(IModel<Content> model) {
		if ((model.getObject().isHeadVersion()) && (model.getObject().getVersion() > 0))
			return true;
		return false;
	}

	private List<ProcessLauncher> getLaunchers(IModel<Content> model) {
		if (getDomain() == null)
			return new ArrayList<ProcessLauncher>();

		return getDomain().getService(WorkflowDomainService.class) == null ? new ArrayList<ProcessLauncher>() : getDomain().getService(WorkflowDomainService.class).getContextLaunchers(model.getObject());
	}
}
