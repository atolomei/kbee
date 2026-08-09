package kbee.web.searcher.panel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.SecuredMember;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.userlist.UserList;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Filter;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.query.ValueFilter;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.kbee.wicket.markup.html.console.browser.AbstractTreeBrowser;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.list.ListDisplayMode;
import com.novamens.kbee.wicket.markup.html.console.list.ListPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListsApplyUserListEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListsUserListItemUpdateObjectEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.SaveQueryModal;
import com.novamens.kbee.wicket.markup.html.console.tree.TreeNode;
import com.novamens.kbee.wicket.markup.html.console.tree.TreeProvider;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.markup.html.tree.TreeNodeSelection;
import com.novamens.kbee.wicket.util.DisplayNameExtractor;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.modal.Modal.Button;
import com.novamens.wicket.markup.html.repeater.util.NavigationOrder;
import com.novamens.wicket.markup.html.repeater.util.Searcher;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;
import kbee.web.console.BaseBrowser;
import kbee.web.console.SolrSearcherNavigator;
import kbee.web.console.TreeBreadcrumbToolbarItem;
import kbee.web.dataset.DataSetNode;
import kbee.web.dataset.DataSetTreeProvider;
import kbee.web.nav.Navigator;
import kbee.web.panel.ListAjaxItemMainPanel;
import kbee.web.panel.ListContentItemMainPanel;
import kbee.web.panel.ListSimpleItemMainPanel;
import kbee.web.query.SiteTreeQuery;
import kbee.web.searcher.page.SearcherResultsPage;
import kbee.web.security.SecuredMemberAclPage;

@SuppressWarnings("serial")
public class SearcherExplorerBrowser extends SearcherBrowser {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(SearcherExplorerBrowser.class.getName());

	private IModel<DataSet> hierachicalSetModel;
	private TreeProvider<TreeNode<DataSetMember>> treeProvider;
	List<ToolbarItem> toolbarItems;
	TreeBreadcrumbToolbarItem breadcrumb;
	private Map<String, Object> parameters;
	private IModel<TreeNode<DataSetMember>> nodemodel;
	
	public SearcherExplorerBrowser(String id, IModel<Site> siteModel, Map<String, Object> parameters) {
		super(id, siteModel);
		this.parameters = parameters;
		
		setQuery(new SiteTreeQuery(getSite(), 
				getHierachicalSet(), 
				getQueryIndex(), 
				LoadableDetachableModel.of(this::getTreeProvider),
				parameters));
		
		addListeners();
		
		if (parameters!=null) {
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
			getQuery().setParameter("node", node);
			setNode(node);
		}
	}
	
	// ??? de donde sale esto ??
	public DataSet getHierachicalSet() {
		if (hierachicalSetModel==null) {
			for (DataSet dataSet : getContentDao().getDataSets(getDomain())) {
				if (dataSet.isHierachical()) {
					hierachicalSetModel = new ObjectModel<DataSet>(dataSet);
					break;
				}
			}
		}
		return hierachicalSetModel.getObject();
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
	public void onInitialize() {
		super.onInitialize();
		setBrowserType("treelist");
		add(newTreeBrowser());
		add(new InvisiblePanel("audittrail-modal"));
		add(new InvisiblePanel("confirmation-modal"));
	}


	
	
	
	
	/**
	 * TREE 
	 * @return
	 */
	protected WebMarkupContainer newTreeBrowser() {
		
		return new AbstractTreeBrowser<Classificable, TreeNode<DataSetMember>>("browser", getConsoleKey(), getQuery()) {
			@Override
			protected TreeProvider<TreeNode<DataSetMember>> getTreeProvider() {
				return SearcherExplorerBrowser.this.getTreeProvider();
			}
			@Override
			public String getBrowserType() { 
				return SearcherExplorerBrowser.this.getBrowserType();
			}
			protected String getDefaultUserPreference(String key) {
				return null;
			}
			@Override
			public List<NavigationOrder> getOrders() {
				return SearcherExplorerBrowser.this.getOrders();
			}
			@Override
			public Searcher getSearcher() {
				return SearcherExplorerBrowser.this.getSearcher();
			}
			@Override
			protected String getContextKey() {
				return getConsoleKey();
			}
			@Override
			protected Panel getPanel(IModel<Classificable> model) {
				return SearcherExplorerBrowser.this.getPanel(model, 0, true);
			}
			@Override
			protected Panel getPanel(IModel<Classificable> model, List<String> snippets) {
				return SearcherExplorerBrowser.this.getPanel(model, 0, true);
			}
			@Override
			protected IModel<Classificable> getModel(Classificable object) {
				return new ObjectModel<Classificable>(object);
			}
			@Override
			protected Panel getMenu(IModel<Classificable> model) {
				return SearcherExplorerBrowser.this.getMenu(model);
			}
			@Override
			protected Panel getTopPanel() {
				if (SearcherExplorerBrowser.this.hasTopPanel()) {
					return SearcherExplorerBrowser.this.getTopPanel();
				}	
				return new InvisiblePanel("top");
			}
			@Override
			protected List<ToolbarItem> getToolbarItems() {
				List<ToolbarItem> items = new ArrayList<ToolbarItem>();
				items.addAll(super.getToolbarItems());
				items.addAll(SearcherExplorerBrowser.this.getToolbarItems(this));
				return items;
			}
			@Override
			protected boolean hasExpander() {
				return true;
			}
			@Override
			public boolean isMyListsEnabled() {
				return true;
			}
			@Override
			protected boolean isSavedQueriesEnabled() {
				return false;
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
			public Query getQuery() {
				return SearcherExplorerBrowser.this.getQuery();
			}
			@Override
			protected Panel getItemListPanel(IModel<Classificable> model, int index) {
				return SearcherExplorerBrowser.this.getItemListPanel(model, index);
			}
			@Override
			public boolean isRememberQuery() {
				return SearcherExplorerBrowser.this.isRememberQuery();
			}
			@Override
			protected void onUpdateQuery(AjaxRequestTarget target) {
				SearcherExplorerBrowser.this.onUpdateQuery(target);
			}
			@Override
			protected List<GridColumn<SearchResult, String>> getColumns() {
				return new ArrayList<>();
			}
			@Override
			protected boolean hasIcon(IModel<Classificable> model) {
				
				if (model.getObject() instanceof DataSetMember)
					return true;
				
				if (model.getObject() instanceof Content) {
					if (ishasACheckoutVersion(model)) {
						return true;
					}
					return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(((Content)model.getObject()));
				}
				return false;
			}
			
			@Override
			protected String getIcon(IModel<Classificable> model) {
				if (model.getObject() instanceof DataSetMember)
					return SearcherBrowser.FOLDER_CSS; // "far fa-folder";
				else if (model.getObject() instanceof Content) {
					if (ishasACheckoutVersion(model)) {
						return SearcherBrowser.LOCK_ICON_CSS;
					}
					else  {
						boolean writable = ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(((Content)model.getObject()));
						if (writable) {
							return SearcherBrowser.EDITABLE_ICON_CSS;
						}
					}
				}	
				return null;
			}
			@Override
			protected Modal newSaveQueryModal() {
				return new SaveQueryModal("save-filters", getConsoleKey(), getSiteModel());
			}
			@Override
			protected TreeNode<DataSetMember> getNode() {
				return SearcherExplorerBrowser.this.getNode();
			}
			@Override
			protected IModel<Site> getSiteModel() {
				return SearcherExplorerBrowser.this.getSiteModel();
			}
		};
	}

	protected Panel getItemListPanel(IModel<Classificable> model, int index) {
		
		if (model.getObject() instanceof Content) {
		
			IModel<Content> contentmodel = new ObjectModel<Content>((Content)model.getObject());
			return new ListContentItemMainPanel("item", contentmodel, getSiteModel(), index ,false) {
				@Override
				protected void onClick() {
					fireScanAll(new ClickEvent<Content>(null, getModel(), getIndex()));
				}
				@Override
				protected WebMarkupContainer getItemTags(IModel<Content> model) {
					return SearcherExplorerBrowser.this.getItemTags(model);
				}
				@Override
				protected WebMarkupContainer getMoreInfoPanel(IModel<Content> model) {
					return SearcherExplorerBrowser.this.getMoreInfoPanel(model);
				}
				@Override
				protected IModel<String> getItemLabel(IModel<Content> modelObject) {
					return SearcherExplorerBrowser.this.getItemLabel(modelObject);
				}
				@Override
				protected IModel<String> getItemLabelMeta(IModel<Content> model) {
					return SearcherExplorerBrowser.this.getItemLabelMeta(model);
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
				protected WebMarkupContainer getItemTags(IModel<DataSetMember> model) {
					return new InvisiblePanel("labels");
				}
				@Override
				protected WebMarkupContainer getMoreInfoPanel(IModel<DataSetMember> model) {
					return new InvisiblePanel("more-info-container");
				}
				@Override
				protected IModel<String> getItemLabel(IModel<DataSetMember> modelObject) {
					return  new Model<String>(modelObject.getObject().getDisplayName());
				}
				@Override
				protected IModel<String> getItemLabelMeta(IModel<DataSetMember> model) {
					return SearcherExplorerBrowser.this.getItemLabelFolderMeta(model);
				}
			};
		}
	}

	/**
	 * 
	 * 
	 * @param model
	 * @param index
	 * @param expanded
	 * @return
	 */
	
	protected Panel getPanel(IModel<Classificable> model, int index, boolean expanded) {
		Panel panel = null;
		if (model.getObject() instanceof Content) {
			IModel<Content> contentmodel = new ObjectModel<Content>((Content)model.getObject());
			Query query = getSearcher().getQuery();
			Object textfilter = query.getParameters().get("text");
			String textquery = textfilter instanceof Filter ? (String)((Filter)textfilter).getValue() : (textfilter!=null ? textfilter.toString() : null);
			panel = new SearcherContentViewPanel<Content>("editor", contentmodel, getSiteModel(), getSearcher(), textquery, index, expanded);
			((SearcherContentViewPanel<?>)panel).setContext(getConsoleKey());
		}
		else {
			if (model.getObject() instanceof DataSetMember) {
				IModel<DataSetMember> membermodel = new ObjectModel<DataSetMember>((DataSetMember)model.getObject());
				//panel = new SearcherMemberViewPanel<DataSetMember>("editor", membermodel);
				panel = new SearcherMemberExpandedViewPanel<DataSetMember>("editor", membermodel);
			}
		}
		return panel;
	}
	
	
	protected IModel<String> getItemLabelFolderMeta(IModel<DataSetMember> model) {
		
		@SuppressWarnings("unchecked")
		ListPanel<Classificable> panel = (ListPanel<Classificable>) getBrowser().getPanel(ListPanel.class);
		ListDisplayMode mode=panel.getListDisplayMode();
		
		if (mode.isCompact())
			return null;
		
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
	
	protected Panel getMenu(IModel<Classificable> model) {
		return model.getObject() instanceof DataSetMember
			? getMemberMenu(new ObjectModel<DataSetMember>((DataSetMember)model.getObject()))
			: getContentMenu(new ObjectModel<Content>((Content)model.getObject()));
	}
	
	protected Panel getMemberMenu(IModel<DataSetMember> model) {
		ContextMenuPanel<DataSetMember> menu = new ContextMenuPanel<>(model);
		
		
		menu.addItem(id ->
		new AjaxMenuItemPanelV5<DataSetMember>(id) {
			public void onClick(AjaxRequestTarget target) {
				String title = DisplayNameExtractor.get(getModelObject());
				Component browser = SearcherExplorerBrowser.this.get("browser");
				SiteTreeQuery query = new SiteTreeQuery(getSite(), getHierachicalSet(), getQueryIndex());
				query.setNode((DataSetMember)getModelObject());
				SaveQueryModal modal = ((SaveQueryModal) browser.get("save-filters"));
				modal.open(target, title, getBrowserType(), false, query.getParameters(), new Modal.Handler() {
					@Override
					public void onClick(AjaxRequestTarget target, Button button) {
					}
				});
			}
			@Override 
			public String getLabel() {
				return getLabelString("contextmenu.savequery");
			}
		});
		
		menu.addItem(id ->
			new MenuItemPanelV5<DataSetMember>(id) {
				public void onClick() {
					setResponsePage(new SecuredMemberAclPage(getModel()));
				}
				@Override 
				public String getTarget() {
					return "_blank";
				}
				@Override 
				public boolean isVisible() {
					return ServiceLocator
						.getService(ContentSystemSecurityService.class)
						.isWriteable((SecuredMember)getModelObject());
				}
				@Override 
				public String getLabel() {
					return getLabelString("contextmenu.acl");
				}
		});
		
		return menu;
	}
	
	protected List<NavigationOrder> getOrders() {
		List<NavigationOrder> orders = new ArrayList<NavigationOrder>();
		orders.add(new NavigationOrder(new Model<String>("Modified"), "modified", false));
		orders.add(new NavigationOrder(new Model<String>("Title"), "title_sort", true));
		orders.add(new NavigationOrder(new Model<String>("Relevance"), "relevance", false));
		return orders;
	}

	protected void addListeners() {
		
		
 
		
		
		
		add(new WicketEventListener<MyListsApplyUserListEvent>() {
			@Override
			public void onEvent(MyListsApplyUserListEvent event) {
				IModel<UserList> list = event.getUserList();
				logger.debug("Applying userlist filter to tree browser");
				Query query = getQuery();
				ValueFilter filter = new ValueFilter("userlist", String.valueOf(list.getObject().getId()), list.getObject().getDisplayName());
				query.getParameters().put("userlist", filter);
				setResponsePage(new SearcherResultsPage(getSiteModel(), query));
			}

			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof MyListsApplyUserListEvent;
			}
		});
		
		add(new WicketEventListener<MyListsUserListItemUpdateObjectEvent<Content>>() {
			@Override
			public void onEvent(MyListsUserListItemUpdateObjectEvent<Content> event) {
				FeedbackHelper.showInfoToast(event.getListModel().getObject().getName(),  event.getModel().getObject().getDisplayName());
				getBrowser().refresh(event.getRequestTarget());
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof MyListsUserListItemUpdateObjectEvent;
			}
		});
		
		add(new WicketEventListener<ClickEvent<?>>() {
			@Override
			public void onEvent(ClickEvent<?> event) {
				Object object = event.getModel().getObject();
				if (object instanceof DataSetMember) {
					DataSetNode node = (DataSetNode)getTreeProvider().getNode(object, 
							breadcrumb.getNode()!=null ? breadcrumb.getNode().getTreePath() : null);
					fireScanAll(new TreeNodeSelection<DataSetNode>(event.getRequestTarget(), new Model<DataSetNode>(node)));
				}
			}
		});
	}
	
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<Classificable> browser) {
		if (toolbarItems==null) {
			toolbarItems = new ArrayList<>();
			if (breadcrumb==null) {
				breadcrumb = new TreeBreadcrumbToolbarItem(browser, ToolbarItem.Align.TOP_LEFT, (DataSetNode)getNode()) {
		    		@Override
					public String getRootDisplayName() {
						return getSite().getDisplayName();
					}
		    		@Override
		        	public boolean isVisible() {
		        		return "tree".equals(getBrowserType()) || "treelist".equals(getBrowserType());
		        	}
		        };
			}
			toolbarItems.add(breadcrumb);
		}     
		return toolbarItems;
	}
	
	public boolean isGridBrowserSwitch() {
		return false;
	}
	
	protected boolean hasIcon(IModel<Content> model) {
		return false;
	}

	protected String getIcon(IModel<Content> model) {
		return null;
	}

	protected void onUpdateQuery(AjaxRequestTarget target) {
	}

	protected boolean isRememberQuery() {
		return false;
	}
	
	protected boolean hasTopPanel() {
		return false;
	}

	protected Panel getTopPanel() {
		return new InvisiblePanel("top");
	}
	
	protected boolean hasExpander() {
		return true;
	}

	protected boolean isMyListsEnabled() {
		return true;
	}

	protected boolean isMenuEnabled() {
		return true;
	}

	protected boolean isSelectionEnabled() {
		return true;
	}

	protected String getDefaultUserPreference(String key) {
		return null;
	}

	protected boolean isDefaultTopPanelVisible() {
		return false;
	}

	protected boolean isFiltersEnabled() {
		return true;
	}

	protected boolean isSavedQueriesEnabled() {
		return true;
	}

	protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
		return null;
	}

	protected TreeProvider<TreeNode<DataSetMember>> getTreeProvider() {
		if (treeProvider==null) {
			if (parameters!=null && "true".equals(parameters.get("writeables"))) {
				treeProvider = new DataSetTreeProvider(getHierachicalSet(), KbeePermission.WRITE);
			}	
			else {
				treeProvider = new DataSetTreeProvider(getHierachicalSet());
			}	
		}
		return treeProvider;
	}
	
	protected Navigator<Content> getNavigator(IModel<Content> model) {
		return new SolrSearcherNavigator<Content>(getSearcher(), getIndex(model.getObject()));
	}
	
	public long getIndex(Classificable object) {
		return getBrowser().getIndex(object);
	}
	
	@SuppressWarnings("unchecked")
	public BaseBrowser<Classificable> getBrowser() {
		return (BaseBrowser<Classificable>) get("browser");
	}
	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
}