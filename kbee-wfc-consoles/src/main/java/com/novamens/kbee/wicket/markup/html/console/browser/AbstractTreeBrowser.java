package com.novamens.kbee.wicket.markup.html.console.browser;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.data.DataViewPanel;
import com.novamens.kbee.wicket.markup.html.console.event.SwitchPanelsEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridConfigButton;
import com.novamens.kbee.wicket.markup.html.console.grid.GridDisplayMode;
import com.novamens.kbee.wicket.markup.html.console.grid.GridPanel;
import com.novamens.kbee.wicket.markup.html.console.layout.TwoPanelsLayout;
import com.novamens.kbee.wicket.markup.html.console.list.ListConfigButton;
import com.novamens.kbee.wicket.markup.html.console.list.ListPanel;
import com.novamens.kbee.wicket.markup.html.console.layout.AbstractLayout;
import com.novamens.kbee.wicket.markup.html.console.layout.LayoutPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.ConsoleSidePanel;
import com.novamens.kbee.wicket.markup.html.console.panel.SaveQueryModal;
import com.novamens.kbee.wicket.markup.html.console.panel.SavedQueriesSidePanel;
import com.novamens.kbee.wicket.markup.html.console.tree.TreePanel;
import com.novamens.kbee.wicket.markup.html.console.tree.TreeProvider;
import com.novamens.kbee.wicket.markup.html.event.GeneralAjaxWicketEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.DisplayNameExtractor;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.security.Identifiable;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.actions.LinkMenuItemPanel;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.modal.Modal.Button;
import com.novamens.wicket.markup.html.repeater.util.Searcher;

import kbee.util.logging.Logger;
import kbee.web.console.Layout;

/**
 *
 * @param <T>
 */
@SuppressWarnings("serial")
public abstract class AbstractTreeBrowser<T, N extends com.novamens.kbee.wicket.markup.html.console.tree.TreeNode<?>> extends AbstractBrowser<T> {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(AbstractTreeBrowser.class.getName());

	private List<ToolbarItem> items;
	private List<ToolbarItem> selection_actions_toolbaritems;
	private Searcher searcher;
	
	private boolean include_list_browser_switcher = false;
	private boolean default_top_panel_visible = false;
	
	public class MainPanel extends Fragment {
		public MainPanel(String id, Panel toolbar, Panel grid) {
			super(id, "main-fragment", AbstractTreeBrowser.this);
			setOutputMarkupId(true);
			add(grid);
			add(toolbar);
			add(new AttributeModifier("style", new Model<String>() {
				public String getObject() {
					Panel sidepanel = AbstractTreeBrowser.this.getPanel(AbstractLayout.SIDE_DISPOSITION);
					return sidepanel==null || !sidepanel.isVisible() ? "width:100%;overflow:auto;" : "overflow:auto;";
				}
			}));
			add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					Panel sidepanel = AbstractTreeBrowser.this.getPanel(AbstractLayout.SIDE_DISPOSITION);
					return sidepanel==null || !sidepanel.isVisible() ? "primary-panel col-md-8 col-lg-8 col-xs-12 ui-resizable layout-onepanel" : "primary-panel col-md-8 col-lg-8 col-xs-12 ui-resizable layout-twopanels";
				}
			}));
		}
	} 
	
	public AbstractTreeBrowser(String id, String consoleName, Query query) {
		super(id,  consoleName, query);
		setOutputMarkupId(true);
	}

	public void refresh(AjaxRequestTarget target) {
		super.refresh(target);
		getPanel(DataViewPanel.class).refresh(target);
		target.add(getPanel(DataViewPanel.class));
	}
	
	@Override
	public List<ToolbarItem> getSelectionToolbarItems() {
		if (selection_actions_toolbaritems!=null)
			return selection_actions_toolbaritems;
		selection_actions_toolbaritems = new ArrayList<ToolbarItem>();
		return selection_actions_toolbaritems; 
	}
	
	public void setSearcher(Searcher searcher) {
		this.searcher=searcher;
	}
	
	public Searcher getSearcher() {
		return searcher;
	}

	public SaveQueryModal getSaveQueryModal() {
		return (SaveQueryModal) get("save-filters");
	}
	
	public String getBrowserType() { 
		return "tree";
	}
	
	/**
	 * Sample TOP:
	 * com.novamens.content.web.console.markup.searchselector.AdvancedSearchSelectorEditor
	 * com.novamens.kbee.wicket.markup.html.console.panel.FiltersPanel
	 * 
	 * 
	 * @see kbee.web.search.SuggesterSearchPanel
	 */
	@Override
	public <P extends WebMarkupContainer> void togglePanel(Class<P> panelclass) {
		super.togglePanel(panelclass);
		if (getPanel(panelclass)!=null) {
			String key = (getDisposition(panelclass)==Layout.SIDE_DISPOSITION) ? "sidepanel" : "toppanel";
			if (getPanel(panelclass).isVisible()) 
				setPreference(key, panelclass.getName());
			else 
				setPreference(key, "none");
		}
	}
	
	public boolean isListBrowserSwitch() {
		return include_list_browser_switcher;
	}
	
	public void setListBrowserSwitch(boolean b) {
		include_list_browser_switcher=b;
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		setSidePreference(getPreference("sideplace"));
	}

	@Override
	public void onDetach() {
		super.onDetach();
		
		if (this.searcher!=null)
			this.searcher.detach();
			
		this.items=null;
		this.selection_actions_toolbaritems=null;
	}
	
	protected N getNode() {
		return null;
	}
	
	protected Modal newSaveQueryModal() {
		return new SaveQueryModal("save-filters", getConsoleKey(), null);
	}
	
	protected void saveState(AjaxRequestTarget target, N node) {
		String title = DisplayNameExtractor.get(node);
		Map<String, Object> parameters = getQuery().getParameters();
		SaveQueryModal modal = ((SaveQueryModal) get("save-filters"));
		modal.open(target, title, getBrowserType(), false, parameters, new Modal.Handler() {
			@Override
			public void onClick(AjaxRequestTarget target, Button button) {
			}
		});
	}
	
	protected Panel getItemListPanel(IModel<T> model , int index) {
		LinkMenuItemPanel<T> link = new LinkMenuItemPanel<T>("item", model , index) {
			@Override
			public void onClick() throws Exception {
			}
			@Override
			public String getLabel() {
				if ((model.getObject() instanceof Identifiable)) {
					return ((Identifiable) model.getObject()).getDisplayName();	
				}
				return model.getObject().toString();
			}
		};
		link.setIndex(index);
		return link;
	}
	
	protected List<LayoutPanel> getPanels() {
		
		List<LayoutPanel> panels = new ArrayList<LayoutPanel>();
		
		GridPanel<T> grid = new GridPanel<T>("grid", getQuery(), getColumns()) {
			@Override
			public Searcher getSearcher() {
				return AbstractTreeBrowser.this.getSearcher();	
			}
			@Override
			protected String getContextKey() {
				return AbstractTreeBrowser.this.getContextKey()+super.getContextKey();
			}
			@Override
			protected IModel<T> getModel(T object) {
				return AbstractTreeBrowser.this.getModel(object);
			}
			@Override
			protected Panel getPanel(IModel<T> model, List<String> snippets) {
				return AbstractTreeBrowser.this.getPanel(model, snippets);
			}
			@Override
			protected Panel getMenu(IModel<T> model) {
				return AbstractTreeBrowser.this.getMenu(model);
			}
			@Override
			protected List<ToolbarItem> getSelectionToolbarItems() {
				return AbstractTreeBrowser.this.getSelectionToolbarItems();
			}
			@Override
			protected void onSelectAll(AjaxRequestTarget target) {
				target.add(AbstractTreeBrowser.this);
			}
			@Override
			protected boolean hasExpander() {
				return AbstractTreeBrowser.this.hasExpander();
			}
			@Override
			protected boolean isSelectionEnabled() {
				return AbstractTreeBrowser.this.isSelectionEnabled();
			}
			@Override
			protected boolean isMenuEnabled() {
				return AbstractTreeBrowser.this.isMenuEnabled();
			}
			@Override
			protected String getConsoleKey() {
				return AbstractTreeBrowser.this.getConsoleKey();
			}
			@Override
			protected String getConsoleDisplayName() {
				return AbstractTreeBrowser.this.getConsoleDisplayName();
			}
			@Override
			protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
				return AbstractTreeBrowser.this.getRowContainerCss(rowmodel);
			}
			@Override
			protected String getDefaultUserPreference(String key) {
				return AbstractTreeBrowser.this.getDefaultUserPreference(key);
			}
		};
		
		grid.setVisible(true);
		
		ListPanel<T> list = new ListPanel<T>("grid", getQuery()) {
			
			@Override
			public boolean isIconSupported() {
				return AbstractTreeBrowser.this.isIconSupported();
			}
			@Override
			public Searcher getSearcher() {
				return AbstractTreeBrowser.this.getSearcher();	
			}
			@Override
			protected String getContextKey() {
				return AbstractTreeBrowser.this.getContextKey()+super.getContextKey();
			}
			@Override
			protected IModel<T> getModel(T object) {
				return AbstractTreeBrowser.this.getModel(object);
			}
			@Override
			protected Panel getPanel(IModel<T> model, List<String> snippets) {
				return AbstractTreeBrowser.this.getPanel(model, snippets);
			}
			@Override
			protected Panel getPanel(IModel<T> model, int index, boolean expanded) {
				return AbstractTreeBrowser.this.getPanel(model);
			}
			@Override
			protected Panel getMenu(IModel<T> model) {
				return AbstractTreeBrowser.this.getMenu(model);
			}
			@Override
			protected List<ToolbarItem> getSelectionToolbarItems() {
				return AbstractTreeBrowser.this.getSelectionToolbarItems();
			}
			@Override
			protected boolean hasExpander() {
				return AbstractTreeBrowser.this.hasExpander();
			}
			@Override
			protected boolean isSelectionEnabled() {
				return AbstractTreeBrowser.this.isSelectionEnabled();
			}
			@Override
			protected boolean isMenuEnabled() {
				return AbstractTreeBrowser.this.isMenuEnabled();
			}
			@Override
			protected Panel getItemListPanel(IModel<T> model, int index) {
				return AbstractTreeBrowser.this.getItemListPanel(model, index);
			}
			@Override
			protected boolean hasIcon(IModel<T> model) {
				return AbstractTreeBrowser.this.hasIcon(model);
			}
			@Override
			protected String getIcon(IModel<T> model) {
				return AbstractTreeBrowser.this.getIcon(model);
			}
		};
		
		list.setVisible(true);
		
		TreePanel<N> tree = new TreePanel<N>("side", getTreeProvider(), getNode()) {
			@Override
			public void onSelect(AjaxRequestTarget target, N node) {
				Query query = AbstractTreeBrowser.this.getQuery();
				query.setParameter("node", node);
				AbstractTreeBrowser.this.onUpdateQuery(target);
				AbstractTreeBrowser.this.getPanel(DataViewPanel.class).refresh(target);
				target.add(AbstractTreeBrowser.this.getPanel(MainPanel.class));
			}
			@Override
			protected void saveState(AjaxRequestTarget target, N object) {
				AbstractTreeBrowser.this.saveState(target, object);
			}
		};
				
		String dm = getSessionUser().getService(PreferencesService.class).getValue(getConsoleKey() + "-" + "GridPanel", "displaymode",  GridDisplayMode.COMPACT_GRID_NO_BCK.getRsLabel());
		Toolbar toolbar = getToolbar();
		toolbar.setGlobalCss(dm);
		
		if ("tree".equals(getBrowserType())) {
			panels.add(new LayoutPanel (new MainPanel("main", toolbar, grid), TwoPanelsLayout.MAIN_DISPOSITION));
		}
		else {
			panels.add(new LayoutPanel (new MainPanel("main", toolbar, list), TwoPanelsLayout.MAIN_DISPOSITION));
		}	
		
		panels.add(new LayoutPanel (tree, TwoPanelsLayout.SIDE_DISPOSITION));
		
		tree.setVisible(true);
		
		
		LayoutPanel tp = new LayoutPanel (getTopPanel(), TwoPanelsLayout.TOP_DISPOSITION);
		String tpp = getPreference("toppanel");
		
		if (tpp.equals("null"))
			tp.setVisible(isDefaultTopPanelVisible());
		else
			tp.setVisible(!tpp.equals("none"));
		
		panels.add(tp);

		return panels;
	}

	abstract protected TreeProvider<N> getTreeProvider();

	protected abstract void onUpdateQuery(AjaxRequestTarget target);
	
	protected void setDefaultTopPanelVisible(boolean b) {
		this.default_top_panel_visible=b;
	}

	protected boolean isDefaultTopPanelVisible() {
		return this.default_top_panel_visible;
	}
	
	protected void onFavorites(AjaxRequestTarget target) {
		togglePanel(SavedQueriesSidePanel.class);
		target.add(this);
		fire(new SidePanelEvent(target));
	}
	
	@Override
	protected Layout newLayout() {
		TwoPanelsLayout layout = new TwoPanelsLayout("layout");
		layout.setPanels(getPanels());
		String preference = getPreference("sidepanel");
		if (preference!=null && !"none".equals(preference) && !"null".equals(preference) && getPanelClass(preference)!=null) {
			WebMarkupContainer panel = layout.getPanel(getPanelClass(preference));
			if (getQuery().getParameters().get("text")==null)
			if (panel!=null) 
				panel.setVisible(true);
		}
		String top_preference = getPreference("toppanel");
		if (top_preference!=null && !"none".equals(top_preference) && !"null".equals(top_preference) && getPanelClass(top_preference)!=null) {
			WebMarkupContainer top_panel = layout.getPanel(getPanelClass(top_preference));
			if ((top_panel!=null) && !(top_panel instanceof InvisiblePanel)) 
				top_panel.setVisible(true);
		}
		return layout;
	}
	
	@Override
	protected void addListeners() {
      add(new WicketEventListener<SwitchPanelsEvent>() {
    	  @Override
    	  public void onEvent(SwitchPanelsEvent event) {
    		  setSidePreference("left".equals(getPreference("sideplace")) ? "right" : "left");
    		  refresh(event.getRequestTarget());
    	  }
      });
	}
	
	@Override
	protected Toolbar getToolbar() {
		return new Toolbar("toolbar", getToolbarItems());
	}
	
	/***
	 *    Console general toolbar items.
	 *    IMPORTANT. The Semantics is: right actions apply to all items in the (filtered) grid 
	 */
	@Override
	protected List<ToolbarItem> getToolbarItems() {
		if (this.items!=null)
			return this.items;
		
		this.items = new ArrayList<ToolbarItem>();
		
		this.items.add(new NavigationLabel(this, 	ToolbarItem.Align.TOP_RIGHT));
		this.items.add(new PreviousButton(this, 	ToolbarItem.Align.TOP_RIGHT));
		this.items.add(new NextButton(this, 		ToolbarItem.Align.TOP_RIGHT));		
		this.items.add(new RefreshButton(this, 		ToolbarItem.Align.TOP_RIGHT));
		
		if (isSettingsEnabled()) {
			GridConfigButton gridconfig = new GridConfigButton(this, ToolbarItem.Align.TOP_RIGHT) {
				@Override
				public boolean isVisible() {
					return "tree".equals(getBrowserType());
				}
				@Override
				protected void onSetListView(AjaxRequestTarget target) {
					fireScanAll(new GeneralAjaxWicketEvent(target, "treelist-browser")); 
				}
			};
			gridconfig.setGridSwitcher(true);
			gridconfig.setRememberQuery(this.isRememberQuery());
			this.items.add(gridconfig);
			ListConfigButton listconfig = new ListConfigButton(this, ToolbarItem.Align.TOP_RIGHT) {
				@Override
				public boolean isVisible() {
					return "treelist".equals(getBrowserType());
				}
				@Override
				protected void onSetGridView(AjaxRequestTarget target) {
					fireScanAll(new GeneralAjaxWicketEvent(target, "tree-browser")); 
				}
			};
			listconfig.setGridSwitcher(false);
			this.items.add(listconfig);
		}
		
		if (isSavedQueriesEnabled())
			this.items.add(new SavedQueriesButton(this, getSiteModel(), ToolbarItem.Align.TOP_RIGHT) {
				@Override 
				protected String getConsoleKey() {
					return AbstractTreeBrowser.this.getConsoleKey();
				}
			});

		return this.items;
	}
	
	protected IModel<Site> getSiteModel() {
		return null;
	}

	protected void setSidePreference(String preference) {
		
		if ("left".equals(preference)) {
			Panel sidepanel = getPanel(AbstractLayout.SIDE_DISPOSITION);
			boolean hasSidepanel = (sidepanel!=null && sidepanel.isVisible());
			if (hasSidepanel) {			
				getPanel(TreePanel.class).add(new AttributeModifier("class","left-panel secondary-panel col-md-4 col-lg-4 col-xs-12"));
				getPanel(MainPanel.class).add(new AttributeModifier("class","right-panel primary-panel col-md-8 col-lg-8 col-xs-12 ui-resizable layout-twopanels"));
			}
			else {											
				getPanel(MainPanel.class).add(new AttributeModifier("class","right-panel primary-panel col-md-12 col-lg-12 col-xs-12 ui-resizable layout-twopanels"));
			}
		}
		else {
			Panel sidepanel = getPanel(AbstractLayout.SIDE_DISPOSITION);
			boolean hasSidepanel = (sidepanel!=null && sidepanel.isVisible());
			if (hasSidepanel) {			
				getPanel(TreePanel.class).add(new AttributeModifier("class","right-panel secondary-panel col-md-4 col-lg-4 col-xs-12"));
				getPanel(MainPanel.class).add(new AttributeModifier("class","left-panel primary-panel col-md-8 col-lg-8 col-xs-12 ui-resizable layout-twopanels"));
			}
			else {
				getPanel(MainPanel.class).add(new AttributeModifier("class","left-panel primary-panel col-md-12 col-lg-12 col-xs-12 ui-resizable layout-twopanels"));
			}
		}
		if (preference!=null) {
			setPreference("sideplace", preference);
		}
	}
	
	protected void onClosePanel(Panel panel, AjaxRequestTarget target) {
		panel.setVisible(false);
		if (panel instanceof ConsoleSidePanel) {
			setPreference("sidepanel", "none");
			fire(new SidePanelEvent(target));
		} 
		else {   
			setPreference("toppanel", "none");
		}	
		target.add(AbstractTreeBrowser.this);
	}
	
	@Override
	protected boolean hasExpander() {
		return false;
	}
	
	protected boolean isVisible(Facet facet) {
		return true;
	}

	@Override
	protected boolean isSavedQueriesEnabled() {
		return false;
	}

	@Override
	public boolean isMyListsEnabled() {
		return false;
	}

	@Override
	protected boolean isFiltersEnabled() {
		return false;
	}

	@Override
	protected boolean isSettingsEnabled() {
		return true;
	}
	
	protected boolean hasIcon(IModel<T> model) {
		return false;
	}
	
	protected String getIcon(IModel<T> model) {
		return null;
	}
	
	protected String getDefaultUserPreference(String key) {
		return null;
	}

	protected boolean isIconSupported() {
		return true;
	}
	
	
	@SuppressWarnings("unchecked")
	private <P extends WebMarkupContainer> Class<P> getPanelClass(String classname) {
		try {
			return (Class<P>)Class.forName(classname);
		}
		catch (Exception e) {
			logger.error( e,  classname);
			return null;
		}
	}
	
	
}
