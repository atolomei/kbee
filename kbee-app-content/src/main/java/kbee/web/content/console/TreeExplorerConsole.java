package kbee.web.content.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.library.Library;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.multidimensional.FacetWrapper;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.query.SavedQuery;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.AppMonitoringService;
import com.novamens.content.service.ContentService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListItem;
import com.novamens.content.userlist.UserListService;
import com.novamens.content.web.console.markup.GlyphiconColumnPanel;
import com.novamens.content.web.content.markup.LabelsModal;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.ObjectState;
import com.novamens.dom.Proxy;
import com.novamens.dom.Versionable;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.query.ValueFilter;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.browser.AbstractFacetedBrowser;
import com.novamens.kbee.wicket.markup.html.console.browser.AbstractListBrowser;
import com.novamens.kbee.wicket.markup.html.console.browser.AbstractTreeBrowser;
import com.novamens.kbee.wicket.markup.html.console.browser.GridButton;
import com.novamens.kbee.wicket.markup.html.console.browser.IconButton;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.browser.TreeButton;
import com.novamens.kbee.wicket.markup.html.console.data.DataViewPanel;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.event.GridPanelNullObjectEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.GridPanel;
import com.novamens.kbee.wicket.markup.html.console.grid.LastModifiedColumn;
import com.novamens.kbee.wicket.markup.html.console.layout.AbstractLayout;
import com.novamens.kbee.wicket.markup.html.console.layout.LayoutPanel;
import com.novamens.kbee.wicket.markup.html.console.list.ListDisplayMode;
import com.novamens.kbee.wicket.markup.html.console.list.ListPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.ApplySavedQueryEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.DownloadMenuItemPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.FiltersPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListsApplyUserListEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListsUserListItemUpdateObjectEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.SaveQueryModal;
import com.novamens.kbee.wicket.markup.html.console.panel.SavedQueriesSidePanel;
import com.novamens.kbee.wicket.markup.html.console.panel.SolrCursorModel;
import com.novamens.kbee.wicket.markup.html.console.panel.SubMenuAjaxUserListItemPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.kbee.wicket.markup.html.console.tree.TreeNode;
import com.novamens.kbee.wicket.markup.html.console.tree.TreeProvider;
import com.novamens.kbee.wicket.markup.html.event.GeneralAjaxWicketEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.markup.html.tree.TreeNodeSelection;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.solr.indexer.query.SolrCursor;
import com.novamens.solr.indexer.query.SolrResultSet;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.repeater.util.NavigationOrder;
import com.novamens.wicket.markup.html.repeater.util.Searcher;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Process;
import com.novamens.workflow.Task;

import kbee.util.logging.Logger;
import kbee.web.console.AbstractConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.console.ClassificableNameColumnPanel;
import kbee.web.console.SolrSearcherNavigator;
import kbee.web.console.TitleColumnPanel;
import kbee.web.console.TreeBreadcrumbToolbarItem;
import kbee.web.console.grid.AttributeColumn;
import kbee.web.console.grid.AttributeDateColumn;
import kbee.web.console.grid.ClassifierColumn;
import kbee.web.console.grid.LabelSetPanel;
import kbee.web.content.panel.ShareModal;
import kbee.web.dataset.DataSetMemberHitExpandedPanel;
import kbee.web.dataset.DataSetNode;
import kbee.web.dataset.DataSetTreeProvider;
import kbee.web.dataset.MemberPage;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPanel;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.model.object.ObjectAuditModal;
import kbee.web.nav.NavigablePage;
import kbee.web.nav.Navigator;
import kbee.web.object.AuditTrailModal;
import kbee.web.panel.ClickItemEvent;
import kbee.web.panel.ListAjaxItemMainPanel;
import kbee.web.panel.ListSimpleItemMainPanel;
import kbee.web.query.LibraryQuery;
import kbee.web.query.LibraryTreeQuery;
import kbee.web.searcher.panel.SearcherBrowser;
import kbee.web.security.AclPage;
import kbee.web.workflow.task.TaskPage;

@SuppressWarnings("serial")
public class TreeExplorerConsole extends AbstractConsole<Classificable> {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(TreeExplorerConsole.class.getName());
	
	final boolean role_dataset_members = role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
	
	private IModel<Library> libraryModel;
	private IModel<DataSet> dataSetModel;
	private TreeProvider<TreeNode<DataSetMember>> provider;
	private List<ToolbarItem> items = null;
	private TreeBreadcrumbToolbarItem breadcrumb;;
	private List<GridColumn<SearchResult,String>> columns;
	private boolean is_send_email;

	private IModel<TreeNode<DataSetMember>> nodemodel;
	
	public TreeExplorerConsole(String name, IModel<Library> libraryModel, IModel<DataSet> dataSetModel, Query query) {
		super(name, query);
		Assert.isTrue(dataSetModel!=null, "no tree");
		setOutputMarkupId(true);
		setLibrary(libraryModel);
		setDataSet(dataSetModel);
	}
	
	@Override
	protected String getIcon(IModel<Classificable> model) {
		if (isFolder(model))
			return "cell-icon fal fa-folder";
		return null;
	}
	
	public void setLibrary(IModel<Library> model) {
		this.libraryModel = model;
	}
	
	public Library getLibrary() {
		return libraryModel.getObject();
	}
	
	public IModel<Library> getLibraryModel() {
		return libraryModel;
	}
	
	public void setDataSet(IModel<DataSet> model) {
		this.dataSetModel = model;
	}
	
	public DataSet getDataSet() {
		return dataSetModel.getObject();
	}
	
	public IModel<DataSet> getDataSetModel() {
		return dataSetModel;
	}
	
	public TreeNode<DataSetMember> getNode() {
		return nodemodel!=null ? nodemodel.getObject() : null;
	}
	
	public IModel<TreeNode<DataSetMember>> getNodeModel() {
		return nodemodel;
	}
	
	public void setNode(TreeNode<DataSetMember> node) {
		nodemodel = node!=null ? new Model<TreeNode<DataSetMember>>(node)  :null;
	}

	@Override
	public Query newQuery() {
		if ("tree".equals(getBrowserType()) || "treelist".equals(getBrowserType())) {
			return setUserPreference(new LibraryTreeQuery(getDataSet(), getLibrary(), getQueryIndex()));
		}
		else {
			return setUserPreference(new LibraryQuery(getQueryIndex(), getLibrary()));
		}
	}
	
	public List<Classifier> getClassifiers() {
		return getContentDao().getClassifiers(getDomain());
	}
	
	public List<Attribute> getAttributes() {
		return getContentDao().getAttributes(getDomain());
	}
	
	protected BaseBrowser<Classificable> newTreeBrowser() {
		
		AbstractTreeBrowser<Classificable, TreeNode<DataSetMember>> br = new AbstractTreeBrowser<Classificable, TreeNode<DataSetMember>>("browser", getName(), getQuery()) {
			@Override
			public String getBrowserType() { 
				return TreeExplorerConsole.this.getBrowserType();
			}
			@Override
			protected TreeProvider<TreeNode<DataSetMember>> getTreeProvider() {
				return TreeExplorerConsole.this.getTreeProvider();
			}
			@Override
			public List<NavigationOrder> getOrders() {
				return TreeExplorerConsole.this.getOrders();
			}
			@Override
			public Searcher getSearcher() {
				return TreeExplorerConsole.this.getSearcher();
			}
			@Override
			protected String getContextKey() {
				return TreeExplorerConsole.this.getName() + super.getContextKey();
			}
			@Override
			protected IModel<Classificable> getModel(Classificable object) {
				return TreeExplorerConsole.this.getModel(object);
			}
			@Override
			protected Panel getPanel(IModel<Classificable> model) {
				return TreeExplorerConsole.this.getPanel(model, null);
			}
			@Override
			protected Panel getPanel(IModel<Classificable> model, List<String> snippets) {
				return TreeExplorerConsole.this.getPanel(model, snippets);
			}
			@Override
			protected Panel getItemListPanel(IModel<Classificable> model , int index) {
				return TreeExplorerConsole.this.getItemListPanel(model , index);
			}
			@Override
			protected List<GridColumn<SearchResult, String>> getColumns() {
				return TreeExplorerConsole.this.getColumns();
			}
			@Override
			protected Panel getMenu(IModel<Classificable> model) {
				return TreeExplorerConsole.this.getMenu(model);
			}
			@Override
			public DownloadMenuItemPanel<SavedQuery> getGridExportSavedQueryMenuItem(String id, IModel<SavedQuery> model) {
				return TreeExplorerConsole.this.getGridExportSavedQueryMenuItem(id, model);
			}
			@Override
			protected Panel getTopPanel() {
				return new InvisiblePanel("top");
			}
			@Override
			protected List<ToolbarItem> getToolbarItems() {
				List<ToolbarItem> items = new ArrayList<ToolbarItem>();
				List<ToolbarItem> items_console = TreeExplorerConsole.this.getToolbarItems(this);
				List<ToolbarItem> items_super = super.getToolbarItems();
				items.addAll(items_super);
				items_console.forEach(v -> { if (v.getJustify() == ToolbarItem.JUSTIFY_LEFT) items.add(v);});
				items_console.forEach(v -> { if (v.getJustify() == ToolbarItem.JUSTIFY_RIGHT) items.add(v);}); 
				items.add(TreeExplorerConsole.this.getGridToolbarMenuItem());
				return items;
			}
			@Override
			public List<ToolbarItem> getSelectionToolbarItems() {
				List<ToolbarItem> items = super.getSelectionToolbarItems();
				items.addAll(TreeExplorerConsole.this.getSelectionToolbarItems(this));
				return items;
			}
			protected boolean hasExpander() {
				return true;
			}
			@Override
			public Query getQuery() {
				return TreeExplorerConsole.this.getQuery();
			}
			@Override
			public boolean isMyListsEnabled() {
				return TreeExplorerConsole.this.isMyListsEnabled();
			}
			@Override
			public boolean isRememberQuery() {
				return TreeExplorerConsole.this.isRememberQuery();
			} 
			@Override
			protected boolean isSavedQueriesEnabled() {
				return true;
			}
			@Override
			protected void onUpdateQuery(AjaxRequestTarget target) {
			}
			@Override
			protected TreeNode<DataSetMember> getNode() {
				return TreeExplorerConsole.this.getNode();
			}
			@Override
			protected String getIcon(IModel<Classificable> model) {
				return TreeExplorerConsole.this.getIcon(model);
			}
			@Override
			protected boolean hasIcon(IModel<Classificable> model) {
				return TreeExplorerConsole.this.hasIcon(model);
			}
		};
		
		br.setListBrowserSwitch(isListBrowser());
		
		return br;
	}
	
	protected BaseBrowser<Classificable> newGridBrowser() {
		
		AbstractFacetedBrowser<Classificable> br = new AbstractFacetedBrowser<Classificable>("browser", getName(), getQuery()) {
			@Override
			protected boolean isSavedQueriesEnabled() {
				return true;
			}
			@Override
			protected boolean isFiltersEnabled() {
				return true;
			}
			@Override
			protected boolean isDefaultTopPanelVisible() {
				return TreeExplorerConsole.this.isDefaultTopPanelVisible();
			}
			protected String getDefaultUserPreference(String key) {
				return TreeExplorerConsole.this.getDefaultUserPreference(key);
			}
			@Override
			public List<NavigationOrder> getOrders() {
				return TreeExplorerConsole.this.getOrders();
			}
			@Override
			public Searcher getSearcher() {
				return TreeExplorerConsole.this.getSearcher();
			}
			@Override
			protected String getContextKey() {
				return TreeExplorerConsole.this.getName() + super.getContextKey();
			}
			@Override
			protected IModel<Classificable> getModel(Classificable object) {
				return TreeExplorerConsole.this.getModel(object);
			}
			@Override
			protected Panel getPanel(IModel<Classificable> model) {
				return TreeExplorerConsole.this.getPanel(model);
			}
			@Override
			protected Panel getPanel(IModel<Classificable> model, List<String> snippets) {
				return TreeExplorerConsole.this.getPanel(model, snippets);
			}
			@Override
			protected List<GridColumn<SearchResult, String>> getColumns() {
				return TreeExplorerConsole.this.getColumns();
			}
			@Override
			protected Panel getMenu(IModel<Classificable> model) {
				return TreeExplorerConsole.this.getMenu(model);
			}
			@Override
			public DownloadMenuItemPanel<SavedQuery> getGridExportSavedQueryMenuItem(String id, IModel<SavedQuery> model) {
				return TreeExplorerConsole.this.getGridExportSavedQueryMenuItem(id, model);
			}
			@Override
			protected Panel getTopPanel() {
				return new InvisiblePanel("top");
			}
			@Override
			protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
				return TreeExplorerConsole.this.getRowContainerCss(rowmodel);
			}
			@Override
			protected List<ToolbarItem> getToolbarItems() {
				List<ToolbarItem> items = new ArrayList<ToolbarItem>();
				List<ToolbarItem> items_console = TreeExplorerConsole.this.getToolbarItems(this);
				List<ToolbarItem> items_super = super.getToolbarItems();
				items.addAll(items_super);
				items_console.forEach(v -> { if (v.getJustify() == ToolbarItem.JUSTIFY_LEFT) items.add(v);});
				items_console.forEach(v -> { if (v.getJustify() == ToolbarItem.JUSTIFY_RIGHT) items.add(v);}); 
				items.add(TreeExplorerConsole.this.getGridToolbarMenuItem());
				return items;
			}
			@Override
			public List<ToolbarItem> getSelectionToolbarItems() {
				List<ToolbarItem> items = super.getSelectionToolbarItems();
				List<ToolbarItem> items2 = new ArrayList<ToolbarItem>();
				items2.addAll(items);
				items2.addAll(TreeExplorerConsole.this.getSelectionToolbarItems(this));
				return items2;
			}
			@Override
			protected boolean hasExpander() {
				return true;
			}
			@Override
			public boolean isMyListsEnabled() {
				return TreeExplorerConsole.this.isMyListsEnabled();
			}
			@Override
			protected List<LayoutPanel> getPanels() {
				List<LayoutPanel> panels = super.getPanels();
				panels.add(new LayoutPanel("side", AbstractLayout.SIDE_DISPOSITION) {
					protected WebMarkupContainer getPanel(String id) {
						SavedQueriesSidePanel sq = new SavedQueriesSidePanel(id, getBrowser()) {
							@Override
							public void onClose(AjaxRequestTarget target) {
								onClosePanel(this, target);
							}

							@Override
							public void onFilters(AjaxRequestTarget target) {
								//TreeExplorerConsole.this.onFilters(target);
							}
						};
						return sq;
					}
				});


				return panels;
			}
			@Override
			protected boolean isSelectionEnabled() {
				return true;
			}
			@Override
			protected boolean isMenuEnabled() {
				return true;
			}
			@Override
			protected boolean isVisible(Facet facet) {
				return TreeExplorerConsole.this.isVisible(facet);
			}
			@Override
			public Query getQuery() {
				return TreeExplorerConsole.this.getQuery();
			}
			@Override
			public boolean isRememberQuery() {
				return TreeExplorerConsole.this.isRememberQuery();
			}
			@Override
			protected void onUpdateQuery(AjaxRequestTarget target) {
				TreeExplorerConsole.this.onUpdateQuery(target);
			}
		};
		
		br.setListBrowserSwitch(isListBrowser());
		return br;
	}	
	
	protected BaseBrowser<Classificable> newListBrowser() {
		
		AbstractListBrowser<Classificable> br = new AbstractListBrowser<Classificable>("browser", getName(), getQuery()) {

			@Override
			protected Modal newSaveQueryModal() {
				return new SaveQueryModal("save-filters", getConsoleKey(), null);
			}
			@Override
			protected boolean isFiltersEnabled() {
				return true;
			}
			protected String getDefaultUserPreference(String key) {
				return null;
			}
			@Override
			public List<NavigationOrder> getOrders() {
				return  TreeExplorerConsole.this.getOrders();
			}
			@Override
			public Searcher getSearcher() {
				return TreeExplorerConsole.this.getSearcher();
			}
			@Override
			protected String getContextKey() {
				return getConsoleKey();
			}
			@Override
			protected IModel<Classificable> getModel(Classificable c) {
				return  TreeExplorerConsole.this.getModel(c);
			}
			@Override
			protected Panel getItemListPanel(IModel<Classificable> model, int index) {
				return TreeExplorerConsole.this.getItemListPanel(model, index);
			}
			@Override
			protected Panel getPanel(IModel<Classificable> model) {
				return TreeExplorerConsole.this.getPanel(model);
			}
			@Override
			protected Panel getPanel(IModel<Classificable> model, List<String> snippets) {
				return TreeExplorerConsole.this.getPanel(model, snippets);
			}
			@Override
			protected Panel getPanel(IModel<Classificable> model, int index, boolean expanded) {
				return TreeExplorerConsole.this.getPanel(model);
			}
			@Override
			protected Panel getMenu(IModel<Classificable> model) {
				return TreeExplorerConsole.this.getMenu(model);
			}
			@Override
			protected Panel getTopPanel() {
				return new InvisiblePanel("top");
			}
			@Override
			protected List<ToolbarItem> getToolbarItems() {
				List<ToolbarItem> items = new ArrayList<ToolbarItem>();
				List<ToolbarItem> items_console = TreeExplorerConsole.this.getToolbarItems(this);
				List<ToolbarItem> items_super = super.getToolbarItems();
				items.addAll(items_super);
				items_console.forEach(v -> { if (v.getJustify() == ToolbarItem.JUSTIFY_LEFT) items.add(v);});
				items_console.forEach(v -> { if (v.getJustify() == ToolbarItem.JUSTIFY_RIGHT) items.add(v);}); 
				items.add(TreeExplorerConsole.this.getGridToolbarMenuItem());
				return items;
			}
			@Override
			public List<ToolbarItem> getSelectionToolbarItems() {
				List<ToolbarItem> items = super.getSelectionToolbarItems();
				return items;
			}
			@Override
			protected boolean hasExpander() {
				return true;
			}
			@Override
			public boolean isMyListsEnabled() {
				return TreeExplorerConsole.this.isMyListsEnabled();
			}
			@Override
			protected List<LayoutPanel> getPanels() {
				List<LayoutPanel> panels = super.getPanels();
				return panels;
			}
			@Override
			protected boolean isSelectionEnabled() {
				 return TreeExplorerConsole.this.isSelectionEnabled();
			}
			@Override
			protected boolean isMenuEnabled() {
				return true;
			}
			@Override
			protected boolean isVisible(Facet facet) {
				return TreeExplorerConsole.this.isVisible(facet);
			}
			@Override
			public Query getQuery() {
				return TreeExplorerConsole.this.getQuery();
			}
			@Override
			public boolean isRememberQuery() {
				return TreeExplorerConsole.this.isRememberQuery();
			}
			@Override
			protected void onUpdateQuery(AjaxRequestTarget target) {
				TreeExplorerConsole.this.onUpdateQuery(target);
			}
			@Override
			protected String getIcon(IModel<Classificable> model) {
				return TreeExplorerConsole.this.getIcon(model);
			}
			@Override
			protected boolean hasIcon(IModel<Classificable> model) {
				return TreeExplorerConsole.this.hasIcon(model);
			}
		};
		
		br.setGridBrowserSwitch(isGridBrowser());
		return br;
	}
	
	protected boolean hasIcon(IModel<Classificable> model) {
		return isCheckout(model) || isFolder(model);
	}
	

	protected boolean isFolder(IModel<Classificable> model) {
		if (model.getObject() instanceof Content)
			return false;
		return true;
	}
	 
	protected boolean isCheckout(IModel<Classificable> model) {
	
		if (! (model.getObject() instanceof Versionable))
			return false;
		
		@SuppressWarnings("unchecked")
		Versionable<Classificable> v = (Versionable<Classificable>) model.getObject();
		if ((!v.isHeadVersion()) && (v.getVersion()>0))
			return true;
		
		return false;
	}


	@Override
	public Page getConsolePage(Query query, long index) {
		return null;
	}
	
	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<Classificable> browser) {
		if (this.items==null) {
			this.items = super.getToolbarItems(browser);
	    	this.items.add(new GridButton(this, ToolbarItem.Align.TOP_RIGHT));
	    	this.items.add(new TreeButton(this, ToolbarItem.Align.TOP_RIGHT));
	    	breadcrumb = new TreeBreadcrumbToolbarItem(browser, ToolbarItem.Align.BOTTOM_LEFT, (DataSetNode)getNode()) {
	    		@Override
	        	public boolean isVisible() {
	        		return "tree".equals(getBrowserType()) || "treelist".equals(getBrowserType());
	        	}
	        };
			this.items.add(breadcrumb);
	    	this.items.add(new IconButton<Classificable>(browser, ToolbarItem.Align.BOTTOM_RIGHT) {
				@Override
				public void onClick() {
					DataSetNode node = breadcrumb.getNode();
					if (node!=null) {
						DataSetMember member = node.getObject();
						setResponsePage(TreeExplorerConsole.this.getPage(new ObjectModel<Classificable>(member)));
					}
				}
	    		@Override
	        	public boolean isVisible() {
	        		return "tree".equals(getBrowserType()) || "treelist".equals(getBrowserType());
	        	}
	    		@Override
	        	protected String getIcon() {
	        		return "fal fa-pen-to-square";
	        	}	
			});
		}
		return this.items;
	}
	
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (this.columns!=null)
			return this.columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();
		
		this.columns.add(new GridColumn<SearchResult, String>("locked", getLabel("lockedcolumn")) {
			public boolean isHeaderMenu() {
				return false;
			}
			@Override
			public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
				Object object = resultmodel.getObject().getObject();
				IModel<Classificable> objectmodel =  getModel((Classificable)object);
				cellItem.add(new GlyphiconColumnPanel<Classificable>(componentId, objectmodel) {
					@Override
					public String getCss() {
						return getModelObject() instanceof Content ?
							"cell-icon fal fa-lock" :
							"cell-icon fal fa-folder";
					}
					@Override
					public boolean isVisible() { 
						return (getModelObject() instanceof Content && ((Content)getModelObject()).isLocked()) || getModelObject() instanceof DataSetMember; 
					}
					protected IModel<String> getAnchorTitle() {
						try {
							Classificable object = getModelObject(); 
							if (object instanceof Content && ((Content)object).isLocked()) {
								Content content = ((Content)object);
								String title = "";
								Content wc = getContentDao().findWorkspaceCopyContentByOId(content.getOId());
								if (wc!=null) {
									title = getContentDao().findUserProfileByUserId(wc.getWorkspace()).getPersonFirstLastName();
								}
								return new Model<String>(title);
							}
						}
						catch (Exception e) {
							logger.error(e, (getSessionUser()!=null?getSessionUser().getUserName():"null"));
							return new Model<String>(e.getClass().getSimpleName());
						}
						return null;
					}
					@Override
					public String getCssStyle() {
						return "";
					}
				});
			}
			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				if (!(object.getObject() instanceof Content)) return null;
				Content content = (Content) object.getObject();
				return () -> content.isLocked() ? "locked" : "unlocked";
			}
			@Override
			protected String getContextKey() {
				return TreeExplorerConsole.this.getName() + super.getContextKey();
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
		
	 	this.columns.add(new GridColumn<SearchResult, String>("mylists", getLabel("mylists")) {
 			@Override
 			public String getCssClass()	{
 				return super.getCssClass() + " mylist";
 			}
 			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				List<UserList> list = ((KbeeUser) getSessionUser()).getService(UserListService.class).getUserLists(TreeExplorerConsole.this.getName(), (com.novamens.dom.Object)object.getObject());
				if (list==null)	return new Model<String>("");
				StringBuilder str = new StringBuilder(); 
				for (UserList u:list) {
					if (str.length()>0)
						str.append(", ");
					str.append(u.getTitle());
				}
				return new Model<String>(str.toString());
			}
			@Override
			protected String getContextKey() {
				return TreeExplorerConsole.this.getName() + super.getContextKey();
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
				if (object instanceof Content) {
					IModel<Content> objectmodel = new ObjectModel<Content>((Content)object);
					cellItem.add(new TitleColumnPanel<Content>(componentId, objectmodel) {
						protected String getCss() {
							return "btn-link";
						}
					});
				}
				else {
					IModel<Classificable> objectmodel = getModel((Classificable)object);
					cellItem.add(new ClassificableNameColumnPanel<Classificable>(componentId, objectmodel) {
						protected String getCss() {
							return "btn-link";
						}
					});
				}
			}
			@Override
			public String getCssClass() {
				return "col title col-xs-1 col-md-1 col-lg-1";
			}
			@Override
			protected IModel<String> getLabelModel(SearchResult object) {
				Classificable content = (Classificable) object.getObject();
				return ()-> content.getDisplayName();
			}
			@Override
			protected String getContextKey() {
				return TreeExplorerConsole.this.getName() + super.getContextKey();
			}
			@Override
			public int getDefaultWidth() {
				return GridColumn.DEFAULT_TITLE_COLUMN_WIDTH;
			}
		});
		
		this.columns.add(new LastModifiedColumn<Classificable>("date", getLabel("datecolumn"), "modified") {
			@Override
			protected String getContextKey() {
				return TreeExplorerConsole.this.getName() + super.getContextKey();
			}
		});
		
		String key = getLibrary().getKey();
		
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
				return object.getObject() instanceof Content ?
					new Model<String>(((Content)object.getObject()).getContentTemplate().getDisplayName()) :
					new Model<String>("");
			}
			@Override
			protected String getContextKey() {
				return TreeExplorerConsole.this.getName() + super.getContextKey();
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

		return this.columns;
	}
	
	@Override
	public void onInitialize() {
		this.is_send_email = (root || role_admin) || getPerson().getProfile(UserProfile.class).isSendFilesEmail();
		super.onInitialize();
    	if (getQuery().getParameters().containsKey("text") ||
    		getQuery().getParameters().containsKey("userlist") ||
    		getQuery().getParameters().containsKey("members")) {
    		String bt = getBrowserType().equals("list") || getBrowserType().equals("treelist") ? "list" : "grid";
			setBrowserType("bt");
    		addOrReplace("grid".equals(bt) ? newGridBrowser() : newListBrowser());
    	}
	}
	
	@Override
	protected Panel getMenu(IModel<Classificable> model) {
		
		ContextMenuPanel<Classificable> menu = new ContextMenuPanel<Classificable>(model);
		
		menu.addItem(id ->
			new MenuItemPanelV5<Classificable>(id) {
				public void onClick() {
					try {
						setResponsePage(TreeExplorerConsole.this.getPage(getModel()));
					} 
					catch (Exception e) {
						logger.error(e);
						setResponsePage(new ApplicationErrorPage<>(e));
					}
				}
				@Override 
				public String getLabel() {
					return TreeExplorerConsole.this.getLabelString("contentbase.contextmenu.open");
				}
		});
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Classificable>(id) {
				@SuppressWarnings("unchecked")
				public void onClick(AjaxRequestTarget target) {
					Modal modal = TreeExplorerConsole.this.getSendByEmailModal();
					ObjectModel<Content> model = new ObjectModel<Content>((Content)getModelObject());
					((ShareModal<Content>)modal).open(target, model);
				}
				@Override 
				public String getLabel() {
					return TreeExplorerConsole.this.getLabelString("contentbase.contextmenu.share");
				}
				public boolean isVisible() {
					return getModelObject() instanceof Content;
				}
				@Override 
				public boolean isEnabled() {
					if (isSupportUser())
						return false;
					return isRoot() || isSendByEmail();
				}
		});
		
		if (model.getObject() instanceof Content)
			menu.addItem(id ->
				new SubMenuAjaxUserListItemPanel<Classificable>(id, model, getName(), UserListItem.PUBLISHED)
			);
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Classificable>(id) {
				@SuppressWarnings("unchecked")
				public void onClick(AjaxRequestTarget target) {
					if (getModelObject() instanceof Content) {
						Modal modal = TreeExplorerConsole.this.getAuditTrailModal(getModel());
						ObjectModel<Content> model = new ObjectModel<Content>((Content)getModelObject());
						((AuditTrailModal<Content>)modal).open(target, model);
					}
					else {
						Modal modal = TreeExplorerConsole.this.getAuditTrailModal(getModel());
						ObjectModel<DataSetMember> model = new ObjectModel<DataSetMember>((DataSetMember)getModelObject());
						((ObjectAuditModal<DataSetMember>)modal).open(target, model);
					}
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
					if (isAuditReadable(getModel()))
						return true;
					return false;
				}
		});
		
		menu.addItem(id ->
			new MenuItemPanelV5<Classificable>(id) {
				public void onClick() {
					ObjectModel<Content> model = new ObjectModel<Content>((Content)getModelObject());
					setResponsePage(new AclPage(model));
				}
				@Override 
				public String getLabel() {
					return getLabelString("contentbase.contextmenu.acl");
				}
				@Override 
				public String getTarget() {
					return "_blank";
				}
				@Override 
				public boolean isEnabled() {
					try {
						if (!(getModel().getObject() instanceof Content))
							return false;
						if (isSupportUser())
							return true;
						return isWriteable(getModel());
					} 
					catch (Exception e) {
						logger.error(e, getSessionUser()!=null?getSessionUser().getUserName():"null");
						return false;
					}
				}
			});
		
		int index = 0;
		final int launchers =  getLaunchers(model).size();
		for (int process_launcher=0;  process_launcher<launchers;  process_launcher++) {
			
			final int p_i= index++;
				
			menu.addItem(new MenuItemFactory<Classificable>() {
				@Override
				public AbstractMenuItemPanelV5<Classificable> getItem(String id) {
					return new MenuItemPanelV5<Classificable>(id) {
						public void onClick() {
							try {
								Content content = (Content)getModel().getObject();
								if (!content.isLocked()) {
									Procedure procedure = null;
									procedure = getLaunchers(model).get(p_i).getProcedure();
									Process process = content.getService(WorkflowService.class).startProcess(procedure);
									Content newcontent = ((KbeeContext)process.getContext()).getContent();
									IModel<Content> sourcemodel = new ObjectModel<Content>(content);
									IModel<Content> model = new ObjectModel<Content>(newcontent);
									model.detach();
									Page page = TreeExplorerConsole.this.getTaskPage(sourcemodel, model);
									setResponsePage(page);
								}
							} 
							catch (Exception e) {
								logger.error(e);
								setResponsePage( new ApplicationErrorPage<>(e));
							}
						}
						public String getLabel() {
							return getLabelString("contentbase.contextmenu.checkout") + " - " + getLaunchers(model).get(p_i).getDisplayName();
						}
						@Override
						public boolean isVisible() {
							if (!getLibrary().isReadable())
								return false;
							if (!isWriteable(getModel()))
								return false;
							if  (getDomain().getService(WorkflowDomainService.class)!=null      	&& 
								getLaunchers(model).size()>0 										&& 
								getLaunchers( model).get(p_i).executeable())
									return true;
							return false;
						}
						@Override
						public boolean isEnabled() {
							if (isSupportUser() && !isRoot())
								return false;
							return !((Content)getModel().getObject()).isLocked();
						}
					};
				}
			});
		}
		
		
		menu.addItem(id -> 
			new SeparatorMenuItemPanelV5<Classificable>(id) {
				@Override
				public String getCssClass() {
					return "divider";
				}
			});
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Classificable>(id) {
				public void onClick(AjaxRequestTarget target) {
					if (!getContent().isLocked()) {
						try {
							getContent().getService(ContentService.class).recycle();
						} 
						catch (ContentMgmtException | ServiceNotFoundException e) {
							logger.error(e, (getSessionUser()!=null?getSessionUser().getUserName():"null"));
							fire (new ErrorEvent<>(target, e));
						}
						resetSelection();
					}
					refresh(target);
				}
				@Override 
				public String getLabel() {
					return getLabelString("contentbase.contextmenu.delete");
				}
				@Override
				public String getWorkingLabel() {
					return getLabelString("contentbase.contextmenu.delete.working");
				}
				@Override
				public boolean isEnabled() {
					if (!(getModel().getObject() instanceof Content))
						return false;
					if (isSupportUser() && !isRoot())
						return false;
					return !getContent().isLocked() && isDeleteable(getModel());
				}
				public Content getContent() {
					return (Content)getModel().getObject();
				}
			});
		
		return menu;
	}
	
	@Override
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<GridPanelNullObjectEvent<?>>() {
			@Override
			public void onEvent(GridPanelNullObjectEvent<?> event) {
				ServiceLocator.getService( AppMonitoringService.class).attempToFixIndex(getSessionUser());
			}
		});
		
		add(new WicketEventListener<MyListsApplyUserListEvent>() {
			@Override
			public void onEvent(MyListsApplyUserListEvent event) {
				setBrowserType("grid");
				setQuery(newQuery());
				IModel<UserList> list= event.getUserList();
				if (event.isApply()) {
					getQuery().setParameter("userlist", String.valueOf(list.getObject().getId()));
				} 
				else {
					setQuery(newQuery());
				}
				BaseBrowser<Classificable> browser = newGridBrowser();
				addOrReplace(browser);
				FiltersPanel panel = getBrowser().getPanel(FiltersPanel.class);
				panel.getParameters().put("userlist", new ValueFilter("userlist", String.valueOf(list.getObject().getId()), list.getObject().getDisplayName()));
				panel.setParameters(panel.getParameters());
				panel.setQuery(getQuery());
				getBrowser().refresh(event.getRequestTarget());
				refresh(event.getRequestTarget());
				list.detach();
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof MyListsApplyUserListEvent;
			}
		});
		
		add(new WicketEventListener<MyListsUserListItemUpdateObjectEvent<Content>>() {
			@Override
			public void onEvent(MyListsUserListItemUpdateObjectEvent<Content> event) {
				FeedbackHelper.showInfoToast(event.getListModel().getObject().getName() + " <br/> " + event.getModel().getObject().getDisplayName());
				TreeExplorerConsole.this.refresh(event.getRequestTarget());		
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof MyListsUserListItemUpdateObjectEvent;
			}
		});
		
		add(new WicketEventListener<ClickEvent<Classificable>>() {
			@Override
			public void onEvent(ClickEvent<Classificable> event) {
				onClickEvent(event);
			}
		});

		add(new WicketEventListener<ClickItemEvent<Classificable>>() {
			@Override
			public void onEvent(ClickItemEvent<Classificable> event) {
				onClickEvent(event);
			}
		});
		
		add(new WicketEventListener<ApplySavedQueryEvent>() {
			@Override
			public void onEvent(ApplySavedQueryEvent event) {
				Map<String, Object> parameters = event.getQuery().getParameters();
				String browsertype = event.getQuery().getBrowser();
				if ("tree".equals(browsertype)) {
					setBrowserType("tree");
					setQuery(newQuery());
					
					DataSetNode  node = null;
					@SuppressWarnings("unchecked")
					List<String> members = (List<String>)parameters.get("members");
					if (members!=null && !members.isEmpty()) {
						DataSetMember member = null;
						String memberstring = members.get(0);
						int i = memberstring.lastIndexOf("/");
						if (i>0) {
							String memberid = memberstring.substring(i+1);
							member = getContentDao().findMemberById(Long.valueOf(memberid));
						}
						node = (DataSetNode)getTreeProvider().getNode(member, null);
					}
					setNode(node);
					
					BaseBrowser<Classificable> browser = newTreeBrowser();
					addOrReplace(browser);
					getQuery().setParameter("node", node);
		            refresh(event.getRequestTarget());
				}
				else {
					setBrowserType("grid");
					setQuery(newQuery());
					getQuery().setParameters(parameters);
					BaseBrowser<Classificable> browser = newGridBrowser();
					addOrReplace(browser);

					refresh(event.getRequestTarget());
				}
			}
		});
	}
	
    @Override
	protected void handle(GeneralAjaxWicketEvent event) {
    	if ("grid-browser".equals(event.getName())) {
			setBrowserType("grid");
    		setQuery(newQuery());
	        fireScanAll(new TreeNodeSelection<DataSetMember>(event.getRequestTarget(), null));
            refresh(event.getRequestTarget());
    	}
    	if ("tree-browser".equals(event.getName())) {
    		if (!"tree".equals(getBrowserType()) && !"treelist".equals(getBrowserType())) {
	    		FiltersPanel panel = getBrowser().getPanel(FiltersPanel.class);
	    		if (panel!=null && panel.isFiltersApplied())
	    			panel.clearAll();
				setBrowserType("tree");
				setQuery(newQuery());
				panel.setQuery(getQuery());
		        fireScanAll(new TreeNodeSelection<DataSetMember>(event.getRequestTarget(), null));
	            refresh(event.getRequestTarget());
    		}
    	}
    	super.handle(event);
	}
    
    @Override
	protected void loadLastQuery() {
		if (!"tree".equals(getBrowserType()) && !"treelist".equals(getBrowserType())) {
			super.loadLastQuery();
		}
	}
	
	protected void onClickEvent(ClickEvent<Classificable> event) {
		Classificable object = event.getModelObject();
		if (object instanceof Content) {
			Page page = getPage(event.getModel());
			if (page!=null)
			setResponsePage(page);
		}
		else {
			if (object instanceof DataSetMember) {
				DataSetNode node = (DataSetNode)getTreeProvider().getNode(object, 
					breadcrumb.getNode()!=null ? breadcrumb.getNode().getTreePath() : null);
				fireScanAll(new TreeNodeSelection<DataSetNode>(event.getRequestTarget(), new Model<DataSetNode>(node)));
			}
		}
	}
	
 	protected boolean isVisible(Facet facet) {
		
		Facet realfacet;
		
		if (facet instanceof FacetWrapper) {
			boolean visible = ((FacetWrapper)facet).isVisible(getName());
			if (!visible) return false;
			realfacet = ((FacetWrapper)facet).getFacet();
		}
		else
			realfacet = facet;
		
		return !realfacet.getName().equals("state");
	}
	
	protected void onClickEvent(ClickItemEvent<Classificable> event) {
	}
	
	protected Page getPage(IModel<Classificable> model) {
		Page page;
		try {
			page = model.getObject() instanceof Content 
				? getPage((Content)model.getObject())
				: getPage((DataSetMember)model.getObject());		
		} 
		catch (Exception e) {
			page = new ApplicationErrorPage<>(e);
		}
		return page;
	}
	
	@SuppressWarnings("unchecked")
	protected Page getPage(Content content) {
		IModel<Content> model = new ObjectModel<>(content);
		Page page = (Page)ServiceLocator.getService(BeansService.class).getBean(getContentClass(content) + "-page", model);
		if (page instanceof NavigablePage<?>) {
			((NavigablePage<Content>)page).setNavigator(getNavigator(content));
		}
		return page;
	}
	
    protected Page getPage(DataSetMember member) {
		IModel<DataSetMember> model = new ObjectModel<>(member);
        Searcher searcher = getSearcher();
        SolrCursor soc = new SolrCursor((SolrResultSet) searcher.getResultSet(), getIndex(member));
        MemberPage page = new MemberPage(model, new SolrCursorModel(soc));
        return page;
    }
    
	protected Panel getPanel(IModel<Classificable> model) {
		return getPanel(model, null);
	}
	
	protected Panel getPanel(IModel<Classificable> model, List<String> snippets) {
		try {
			Panel panel = null;
			if (model.getObject() instanceof Content) {
				Content content = (Content)com.novamens.kbee.content.dao.Proxy.Unproxy(model.getObject());
				String bean = getContentClass(content)+"-V6panel";
				ViewMode viewMode = ((DataViewPanel<?>) getBrowser().getPanel(DataViewPanel.class)).getViewMode();
				String text = (String)getQuery().getParameters().get("text");
				panel = (Panel)ServiceLocator.getService(BeansService.class).getBean(bean, new ObjectModel<Content>(content), viewMode, false, text, snippets);
			}
			else {
				if (model.getObject() instanceof DataSetMember) {
			        panel = new DataSetMemberHitExpandedPanel("editor", this, new ObjectModel<DataSetMember>((DataSetMember)model.getObject()), snippets);
				}
			}
			return panel;
		} 
		catch (Exception e) {
			logger.error(e);
			return new ErrorPanel("editor", e);
		}
	}
	
	@Override
	protected Panel getItemListPanel(IModel<Classificable> model , int index) {

		if (model.getObject() instanceof Content) {
		
			return new ListSimpleItemMainPanel<Classificable>("item", model, index,false) {
			
				protected void onClick() {
					fireScanAll(new ClickEvent<Classificable>(null, getModel(), 0));
				}
				@Override
				protected WebMarkupContainer getItemTags(IModel<Classificable> modelObject) {
					return TreeExplorerConsole.this.getItemTags(modelObject);
				}
				protected WebMarkupContainer getMoreInfoPanel(IModel<Classificable> modelObject) {
					return new InvisiblePanel("more-info-container");
				}
				protected IModel<String> getItemLabel(IModel<Classificable> modelObject) {
				
					if (isCheckout(modelObject)) {
						String is=SearcherBrowser.EDITABLE_ICON;
						//String iconStr = " <i class=\"ml-2 fa-duotone fa-solid fa-pen-to-square\"></i>";
						return  new Model<String>(modelObject.getObject().getDisplayName() + is);
					}
					else
						return  new Model<String>(modelObject.getObject().getDisplayName());
				
				}
				protected IModel<String> getItemLabelMeta(IModel<Classificable> model) {
					return TreeExplorerConsole.this.getItemLabelMeta(model);
				}
			};
		}	
		else {
			IModel<DataSetMember> membermodel = new ObjectModel<DataSetMember>((DataSetMember)model.getObject());
			return new ListAjaxItemMainPanel<DataSetMember>("item", membermodel, index ,false) {
				@Override
				protected void onClick(AjaxRequestTarget target) {
					fireScanAll(new ClickEvent<DataSetMember>(target, getModel(), getIndex()));
				}
				@Override
				protected WebMarkupContainer getItemTags(IModel<DataSetMember> modelObject) {
					return new InvisiblePanel("labels");
				}
				@Override
				protected WebMarkupContainer getMoreInfoPanel(IModel<DataSetMember> modelObject) {
					return new InvisiblePanel("more-info-container");
				}
				@Override
				protected IModel<String> getItemLabel(IModel<DataSetMember> modelObject) {
					return  new Model<String>(modelObject.getObject().getDisplayName());
				}
				@Override
				protected IModel<String> getItemLabelMeta(IModel<DataSetMember> modelObject) {
					return TreeExplorerConsole.this.getItemLabelMeta(model);
				}
			};
		}
	}
	
	protected WebMarkupContainer getItemTags(IModel<Classificable> modelObject) {
		try {
			Classificable c=(Classificable) modelObject.getObject();
			String nr = c instanceof Content ? (String) ((Content)c).getService(PropertyService.class).getProperty(PropertyService.PROPERTY_HAS_TAGS) : null;
			if (nr==null || nr.equals("0"))
				return new InvisiblePanel("labels");
			return new LabelSetPanel<Classificable>("labels", modelObject, false, true, false);
		}
		catch (Exception e) {
			logger.error(e);
			return new ErrorPanel("labels", e);
		}
	}
	
	protected IModel<String> getItemLabelMeta(IModel<Classificable> model) {
		
		if (!(model.getObject() instanceof Content)) {
			String label= "";
			Map<String, List<String>> map = model.getObject().getAttributesAsMap();
			for (List<String> values : map.values()) {
				for (String value : values) {
					if (!"".equals(label)) label += " ";
					label += value;
				}
			}
			label = label.replace("</br>", "");
			label = label.replace("<p>", "");
			label = label.replace("</p>", " - ");
			return new Model<String>(label);
		}
		
		Content content = (Content)model.getObject();

		@SuppressWarnings("unchecked")
		ListPanel<Classificable> panel = (ListPanel<Classificable>) getBrowser().getPanel(ListPanel.class);
		
		if (panel==null) 
			return null;
		
		ListDisplayMode mode = panel.getListDisplayMode();
		
		if (mode.isCompact())
			return null;
		
		String s;
		try {
			s = content.getService(ContentService.class).getConsoleSubtitle();
		} 
		catch (Exception e) {
			logger.error(e);
			s = e.getClass().getName();
		}
		return new Model<String>(s);
	}
	
	protected Page getTaskPage(IModel<Content> sourcemodel, IModel<Content> model) {
		return getTaskPage(sourcemodel, model, false);
	}
	
	@SuppressWarnings("unchecked")
	protected Page getTaskPage(IModel<Content> sourcemodel, IModel<Content> model, boolean select_preference) {
		try {
			WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
			Task task = workflowService.getTask();
			
			TaskPage<Content> page = (TaskPage<Content>)((WebTask)task).getPage(workflowService.getContext());
		
			page.setNavigator(getNavigator(sourcemodel));
			page.setSource(TreeExplorerConsole.this.getName());
			
			if (model.getObject().getWorkspace()>0 && 
					getSessionUser().getId().toString().equals(model.getObject().getWorkspace().toString())) {
				page.setEditionEnabled(true);
				page.setReadOnly(false);
			}
			else {
				page.setEditionEnabled(false);
				page.setReadOnly(true);
			}
			return page;
		} 
		catch (Exception e) {
			logger.error(e, (getSessionUser()!=null?getSessionUser().getUserName():"null"));
			return new kbee.web.error.ApplicationErrorPage<Void>(e);
		}
	}
	
	protected Navigator<Content> getNavigator(IModel<Content> model) {
		Navigator<Content> c=new SolrSearcherNavigator<Content>(getSearcher(), getIndex(model.getObject()));
		return c;
	}
	
	protected TreeProvider<TreeNode<DataSetMember>> getTreeProvider() {
		if (provider==null) {
			provider = new DataSetTreeProvider(getDataSet());
		}
		return provider;
	}
	
	
	@Override
	protected void addModals () {
		super.addModals();
		addOrReplace(new AuditTrailModal<Content>("audit-trail-modal"));
		addOrReplace(new ObjectAuditModal<DataSetMember>("objectaudit-modal"));
		addOrReplace(new ShareModal<Content>("send-email-modal"));
		addOrReplace(new LabelsModal<Content>("labels-modal"));
	}
	
	protected Modal getAuditTrailModal(IModel<Classificable> model) {
		if (model.getObject() instanceof Content)
			return (Modal) get("audit-trail-modal");
		else
			return (Modal) get("objectaudit-modal");
	}
	
	protected boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}
	
	protected boolean isAuditReadable(IModel<Classificable> model) {
		if (model.getObject() instanceof Content)
			return ServiceLocator.getService(ContentSystemSecurityService.class).isAuditTrailReadable((Content)model.getObject());
		else
			return role_dataset_members;
	}
	
	protected boolean isWriteable(IModel<Classificable> model) {
		if (model.getObject() instanceof Content)
			return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable((Content)model.getObject());
		else
			return role_dataset_members;
	}
	
	protected boolean isDeleteable(IModel<Classificable> model) {
		if (model.getObject() instanceof Content)
			return ServiceLocator.getService(ContentSystemSecurityService.class).isDeleteable((Content)model.getObject());
		else
			return role_dataset_members;
	}
	
	protected String getContentClass(Content content) {
		return Proxy.getClassName(content).toLowerCase();
	}
	
	protected Navigator<Content> getNavigator(Content content) {
		Navigator<Content> c = new SolrSearcherNavigator<Content>(getSearcher(), getIndex(content));
		return c;
	}
	
	protected boolean isSendByEmail() {
		return is_send_email;
	}
	
	public boolean isListBrowser() {
		return true;
	}
	
	protected boolean isMyListsEnabled() {
		return true;
	}
	
	private List<ProcessLauncher> getLaunchers(IModel<Classificable> model) {
		if (!(model.getObject() instanceof Content) || getDomain()==null)
			return  new ArrayList<ProcessLauncher>();
		return getDomain().getService(WorkflowDomainService.class)==null ? new ArrayList<ProcessLauncher>() :
			getDomain().getService(WorkflowDomainService.class).getContextLaunchers((Content)model.getObject());
	}
}