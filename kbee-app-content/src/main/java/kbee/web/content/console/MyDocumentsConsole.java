package kbee.web.content.console;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import com.novamens.indexer.query.*;
import com.novamens.kbee.wicket.markup.html.console.browser.GridMenu;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.web.console.markup.GlyphiconColumnPanel;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.GridPanel;
import com.novamens.kbee.wicket.markup.html.console.grid.LastModifiedColumn;
import com.novamens.portal6.model.Site;
import com.novamens.service.ContentExportService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BreadCrumb;

import kbee.web.console.AdvancedSearchContentSelectorPanel;
import kbee.web.console.BaseBrowser;
import kbee.web.console.TitleColumnPanel;
import kbee.web.console.grid.AttributeColumn;
import kbee.web.console.grid.AttributeDateColumn;
import kbee.web.console.grid.ClassifierColumn;
import kbee.web.content.panel.ShareModal;
import kbee.web.datamanagement.TagManagementPage;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.nav.ContentSectionBC;
import kbee.web.nav.NavigablePage;
import kbee.web.object.AuditTrailModal;
import kbee.web.query.MyDocumentsQuery;
import kbee.web.searcher.panel.SearcherBrowser;
import kbee.web.searcher.panel.SearcherSimpleErrorPanel;

@SuppressWarnings("serial")
public abstract class MyDocumentsConsole extends ContentConsole<Content> {
	
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MyDocumentsConsole.class.getName());
	
	public static final  String NAME = "mydocs";
	
	private List<GridColumn<SearchResult,String>> columns;

	private List<ToolbarItem> items = null;
	private List<ToolbarItem> selection_toolbar;
	
	
	public MyDocumentsConsole(Query query) {		
		super(NAME, query);
	}
	
	@Override
	public IModel<String> getDisplayName() {
		return new Model<String>("mydocs");
	}
	
	public IModel<Site> getSiteModel() {
		return null;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		try {
			this.items=null;
			if (this.selection_toolbar!=null) {
				for (ToolbarItem item: selection_toolbar) {
					item.detach();
				}
			}
			for (GridColumn<?,?> column: getColumns()) 
				column.detach();
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}
    		
	@Override
	protected GridMenu getGridToolbarMenuItem() {
		GridMenu gridMenu = super.getGridToolbarMenuItem();

		gridMenu.addItem(
				(itemId) -> new AjaxMenuItemPanelV5<Void>(itemId){
					@Override
					public void onClick(AjaxRequestTarget target) throws Exception {
						final int maxItems = 1000;
						List<IModel<Content>> list = new ArrayList<>();
						ResultSet rs = MyDocumentsConsole.this.getBrowser().getQuery().execute();
						final int currentSize = rs.size();
						if(currentSize <maxItems) {
							while (rs.hasNext()) {
								list.add(new ObjectModel<Content>((Content) rs.next().getObject()));
							}
							TagManagementPage page = new TagManagementPage();
							page.setSelection(list);
							setResponsePage(page);
						}else{
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
				}
		);
		return gridMenu;
	}
	
	@Override
	public Query newQuery() {
		return setUserPreference(new MyDocumentsQuery(getQueryIndex()));
	}

	@Override
	protected BreadCrumb getBreadCrumb() {
		return new BreadCrumb(new ContentSectionBC());
	}

	/**
	 * Console Searcher and item index
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected Page getPageV6(IModel<Content> model) {
		
		//Searcher searcher = getSearcher();
		//long index=ContentBaseConsole.this.getIndex(model.getObject());
		
		Page page;
		try {
			// IDOCPageV6 TextPageV6
			//page = (Page)ServiceLocator.getService(BeansService.class).getBean(getContentClass(model.getObject()) + "-page", model, searcher, index);
			page = (Page)ServiceLocator.getService(BeansService.class).getBean(getContentClass(model.getObject()) + "-page", model);
			
			if (page instanceof NavigablePage<?>) {
				((NavigablePage<Content>)page).setNavigator(getNavigator(model));
			}
			
			// IDoc idoc = (IDoc)model.getObject();
			// page=new IDocPageV6(new ObjectModel<IDoc>(idoc));
			
		} catch (Exception e) {
			page=new kbee.web.error.ApplicationErrorPage<>(e);
		}
		return page;

	}

	/**
	 * External contents are read only except for root 
	 */
	@Override
	protected Panel getMenu(IModel<Content> model) {
		
		ContextMenuPanel<Content> menu = new ContextMenuPanel<Content>(model);
		
		menu.addItem(id ->
			new MenuItemPanelV5<Content>(id) {
				public void onClick() {
					try {
						setResponsePage(MyDocumentsConsole.this.getPageV6(getModel()));
					} 
					catch (Exception e) {
						logger.error(e);
						setResponsePage( new ApplicationErrorPage<>(e));
					}
				}
				@Override 
				public String getLabel() {
					return getLabelString("contentbase.contextmenu.open");
				}
			});
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Content>(id) {
				@SuppressWarnings("unchecked")
				public void onClick(AjaxRequestTarget target) {
					Modal modal = MyDocumentsConsole.this.getSendByEmailModal();
					((ShareModal<Content>)modal).open(target, getModel());
				}
				@Override 
				public String getLabel() {
					return getLabelString("contentbase.contextmenu.share");
				}
				@Override 
				public boolean isEnabled() {
					if (isSupportUser())
						return false;
					return isRoot() || isSendByEmail();
				}
			});

		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Content>(id) {
				@SuppressWarnings("unchecked")
				public void onClick(AjaxRequestTarget target) {
					Modal modal = MyDocumentsConsole.this.getAuditTrailModal();
					((AuditTrailModal<Content>)modal).open(target, getModel());
				}
				@Override 
				public String getLabel() {
					return getLabelString("contentbase.contextmenu.audittrail");
				}
				@Override
				public boolean isEnabled() {
					if (isSupportUser())
						return true;
					if (isWriteable(getModel()))
							return true;
					if ( isAuditReadable(getModel()))
						return true;
					return false;
				}
			});

		menu.addItem(id ->
			new com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5<Content>(id) {
				@Override 
				public String getLabel() {
					return getLabelString("contentbase.contextmenu.download");
				}
				@Override
				public boolean isDeleteFileAfterDownload()  {
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
				public boolean isVisible()  {
					return true;
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
		
		if (this.columns!=null)
			return this.columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();
	
		/**
		this.columns.add(new GridColumn<SearchResult, String>("locked", getLabel("lockedcolumn")) {
			public boolean isHeaderMenu() {
				return false;
			}
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				Object object = resultmodel.getObject().getObject();
				IModel<Content> objectmodel = getModel((Content)object);
				
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
								if (oid!=null) {
									Content content=getContentDao().findWorkspaceCopyContentByOId(oid);
									if (content!=null) {
										name = getContentDao().findUserProfileByUserId(content.getWorkspace()).getPersonFirstLastName();
										str.append(name);
									}
								}
								return new Model<String>(str.toString());
							}
						}
						catch (Exception e) {
							logger.error(e, (getSessionUser()!=null?getSessionUser().getUserName():"null"));
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
				return MyDocumentsConsole.this.getName() + super.getContextKey();
			}
			@Override
			public int getWidth() {
				return GridPanel.ICON_COL_WIDTH;
			}
			@Override
			public int getXPadding()	{
				return 3;
			}
			@Override
			public boolean isPreferred() {
				return false;
			}
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
		*/
		
		
		
		this.columns.add(new GridColumn<SearchResult, String>("type", new Model<String>()) {
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				try {
					Object object = resultmodel.getObject().getObject();
					IModel<Content> objectmodel = getModel((Content)object);
					cellItem.add(new GlyphiconColumnPanel<Content>(componentId, objectmodel) {
						@Override
						public String getCss() {
							return SearcherBrowser.EDITABLE_ICON + " panel-centered";  
						}
						
						@Override
						public boolean isVisible() {
							try { 
								Content content = (Content) objectmodel.getObject();
								if (content!=null) {
									if (content.getWorkspace()!=null) {
										return true; 
									}
								}
								return false;
								
							} catch (Exception e) {
								logger.error(e, getSessionUser().getUserName());
								return false;
							}
						};
						
						@Override
						protected IModel<String> getAnchorTitle() {
							return getConsoleLabel("task");
						}
					});
				} 
				catch (Exception e) {
					cellItem.add(new Label(componentId, ""));
				}
			}
			@Override
			public boolean isHeaderMenu() {
				return false;
			}
			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				Content content = (Content)object.getObject();
				if (content!=null) {
					if (content.getWorkspace()!=null) {
						return getConsoleLabel("task"); 
					}
				}
				return null;
				
			}
			@Override
			public boolean isExportable() {
				return false;
			}
			@Override
			protected String getContextKey() {
				return MyDocumentsConsole.this.getName() + super.getContextKey();
			}
			@Override
			public int getWidth() {
				return GridPanel.ICON_COL_WIDTH;
			}
			@Override
			public int getXPadding()	{
				return 3;
			}
 			@Override
			public String getCssClass() {
				return "col short col-xs-1 col-md-1 col-lg-1";
			}
			@Override
			public boolean isPreferred() {
				return false;
			}
			@Override
			public boolean isFixed() {
				return true;
			}
			@Override
			public boolean isResizable() {
				return false;
			}
		});

		
		this.columns.add(new GridColumn<SearchResult, String>("title", getLabel("titlecolumn"), "title_sort") {
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				Object object = resultmodel.getObject().getObject();
				IModel<Content> objectmodel = getModel((Content)object);
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
				return ()-> content.getTitle();
			}
			@Override
			protected String getContextKey() {
				return MyDocumentsConsole.this.getName() + super.getContextKey();
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
				return MyDocumentsConsole.this.getName() + super.getContextKey();
			}
		});
		
		
		String key = "mydocs";
		
 		for (Classifier classifier : getClassifiers()) {
 			if (classifier.isContentType()) {
 				if (classifier.isVisible(key) && classifier.getState()==ObjectState.ENABLED) {
 					this.columns.add(new ClassifierColumn<Content>(new ObjectModel<Classifier>(classifier), this.getName()));
 				}
 			}
		}

		for (Classifier classifier : getClassifiers()) {
			if (!classifier.isContentType()) {
				if (classifier.isVisible(key) && classifier.getState()==ObjectState.ENABLED) {
					this.columns.add(new ClassifierColumn<Content>(new ObjectModel<Classifier>(classifier), this.getName()));
				}
			}
		}
		
		this.columns.add(new GridColumn<SearchResult, String>("contentclass", getLabel("contentclasscolumn")) {
			@Override
			protected IModel<String> getLabelModel(SearchResult object) {		
				return new Model<String>(((Content)object.getObject()).getContentTemplate().getDisplayName());
			}
			@Override
			protected String getContextKey() {
				return MyDocumentsConsole.this.getName() + super.getContextKey();
			}
		});
		
		
		for (Attribute attribute: getAttributes()) {
			if (attribute.getState()==ObjectState.ENABLED && attribute.isVisible(key)) {
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
					return new Model<String>(String.valueOf(((Content)object.getObject()).getLastModifiedUser().getFirstLastName()));
				} catch (Exception e) {
					return new Model<String>(e.getClass().getSimpleName());
				}
			}
			@Override
			protected String getContextKey() {
				return MyDocumentsConsole.this.getName() + super.getContextKey();
			}
			@Override
			public boolean isPreferred() {
				return false;
			}
		});
		
		this.columns.add(new GridColumn<SearchResult, String>("id", getLabel("idcolumn")) {
			@Override
			protected IModel<String> getLabelModel(SearchResult object) {		
				return new Model<String>(String.valueOf(((Content)object.getObject()).getOId()));
			}
			@Override
			protected String getContextKey() {
				return MyDocumentsConsole.this.getName() + super.getContextKey();
			}

			@Override
			public boolean isPreferred() {
				return false;
			}
		});
		
		
		{
			this.columns.add(new GridColumn<SearchResult, String>("subtitle", getLabel("subtitle")) {
				@Override
				protected IModel<String> getLabelModel(SearchResult object) {		
					return new Model<String>(  getSubtitleColumn(object));
				}
				@Override
				protected String getContextKey() {
					return MyDocumentsConsole.this.getName() + super.getContextKey();
				}

				@Override
				public boolean isPreferred() {
					return false;
				}
			});
		}

		return this.columns;
	}

	



	@Override					
	protected List<ToolbarItem> getSelectionToolbarItems(BaseBrowser<Content> browser) {
		
		if (this.selection_toolbar!=null)
			return this.selection_toolbar;
		
		this.selection_toolbar = new ArrayList<ToolbarItem>();
		
		return this.selection_toolbar;
	}

	/***
	 *  
	 *  Grid Toolbar
	 *  
	 *  used by {@link GridPanel}
	 *  
	 *  <b>LEFT</b>. New, Sub section Selector, and in some cases actions that apply to the selected items
	 *  <b>RIGHT</b>. Actions that apply to all items in the current grid  
	 *  
	 */
	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<Content> browser) {
	
		if (this.items==null) {
			this.items = super.getToolbarItems(browser);
		}
		
	
		InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					InfoDialog infoDialog = (InfoDialog) getInformationModal();
					infoDialog.open(target,() -> {return MyDocumentsConsole.this.getName();}, new Model<String>(MyDocumentsConsole.this.getDescription()));
				} catch (Exception e) {
					logger.error(e);
					fire (new ErrorEvent<>(target, e));
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
//		if (getLibrary().getDescription()!=null) {
//			str.append("<section>");
//			str.append("<h3>"+ getLabel("description").getObject() + "</h3> <p>" +getLibrary().getDescription()+"</p>");
//			str.append("</section>");
//		}
//		if (getLibrary().getCriteria()!=null) {
//			str.append("<section>");
//			str.append("<h3>"+getLabel("criteria").getObject()+"</h3>");
//				if (getLibrary().getCriteria() instanceof IqlCriteria)
//					str.append("<p>" + ((IqlCriteria) getLibrary().getCriteria()).getStatement()+"</p>");
//			str.append("</section>");
//		}
//		str.append("<section>");
//		str.append("<h3>"+ getLabel("settings-title").getObject() + "</h3>"); 
//		str.append("<p>"+getLabel("open-settings").getObject()+
//				" <a href= \"/libraries/"+ 
//					getLibrary().getId().toString()+"\" target=\"_blank\">"+getLibrary().getName()+"</a></p>");
//		str.append("</section>");
		
		return str.toString();
	}
	
	@Override
	protected Panel getTopPanel() {
		try {
			return new AdvancedSearchContentSelectorPanel("top", getName());
		} 
		catch (Exception e) {
			logger.error(e);
			return new SearcherSimpleErrorPanel("top", e.getClass().getSimpleName(), e.getMessage());
		}
	}
	
	@Override
	protected String getIcon(IModel<Content> model) {
		if (model.getObject().isLocked())
			return "cell-icon fal fa-lock";
		return null;
	}
	
    protected boolean isDefaultTopPanelVisible() {
		return false;
	}

	@Override
	protected boolean isEditionEnabled() {
		return true;
	}
	
	@Override
	protected boolean hasTopPanel() {
		return true;
	}
	
	@Override
	protected boolean isReadOnly() {
		return false;
	}
}
  