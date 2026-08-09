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
import com.novamens.kbee.wicket.markup.html.console.layout.AbstractLayout;
import com.novamens.kbee.wicket.markup.html.console.layout.LayoutPanel;
import com.novamens.kbee.wicket.markup.html.console.layout.OnePanelLayout;
import com.novamens.kbee.wicket.markup.html.console.panel.ConsoleSidePanel;
import com.novamens.kbee.wicket.markup.html.console.panel.FiltersPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.SaveQueryModal;
import com.novamens.kbee.wicket.markup.html.console.panel.SavedQueriesSidePanel;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.modal.Modal.Button;
import com.novamens.wicket.markup.html.repeater.util.Searcher;

import kbee.web.console.Layout;

/**
 *
 * @param <T>
 */
@SuppressWarnings("serial")
public abstract class AbstractFacetedBrowser<T> extends AbstractBrowser<T> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AbstractFacetedBrowser.class.getName());

	private List<ToolbarItem> items;
	private List<ToolbarItem> selection_actions_toolbaritems;
	private Searcher searcher;
	
	private boolean include_list_browser_switcher = false;
	private boolean include_tree_browser_switcher = false;
	private boolean default_top_panel_visible = false;
	
	public class MainPanel extends Fragment {
		public MainPanel(String id, Panel toolbar, Panel grid) {
			super(id, "main-fragment", AbstractFacetedBrowser.this);
			add(grid);
			add(toolbar);
			add(new AttributeModifier("style", new Model<String>() {
				public String getObject() {
					Panel sidepanel = AbstractFacetedBrowser.this.getPanel(AbstractLayout.SIDE_DISPOSITION);
					return sidepanel==null || !sidepanel.isVisible() ? "width:100%; overflow:auto;" : "overflow:auto;";
				}
			}));
			add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					Panel sidepanel = AbstractFacetedBrowser.this.getPanel(AbstractLayout.SIDE_DISPOSITION);
					return sidepanel==null || !sidepanel.isVisible() ? "primary-panel col-md-8 col-lg-8 col-xs-12 ui-resizable layout-onepanel" : "primary-panel col-md-8 col-lg-8 col-xs-12 ui-resizable layout-twopanels";
				}
			}));
		}
	} 
	
	/***
	 * 
	 * @param id
	 * @param consoleName
	 * @param query
	 */
	public AbstractFacetedBrowser(String id, String consoleName, Query query) {
		super(id,  consoleName, query);
		setOutputMarkupId(true);
	}

	public void refresh(AjaxRequestTarget target) {
		super.refresh(target);
		getPanel(DataViewPanel.class).refresh(target);
		getPanel(FiltersPanel.class).reload(target);
	}
	
	public void setSearcher(Searcher searcher) {
		this.searcher=searcher;
	}
	
	public Searcher getSearcher() {
		return searcher;
	}
	
	public boolean isListBrowserSwitch() {
		return include_list_browser_switcher;
	}
	
	public void setListBrowserSwitch(boolean b) {
		include_list_browser_switcher=b;
	}
	
	public boolean isTreeBrowserSwitch() {
		return include_tree_browser_switcher;
	}
	
	public void setTreeBrowserSwitch(boolean b) {
		include_tree_browser_switcher=b;
	}
	
	@Override
	public boolean isMyListsEnabled() {
		return false;
	}

	public SaveQueryModal getSaveQueryModal() {
		return (SaveQueryModal) get("save-filters");
	}
		
	public String getBrowserType() { 
		return "grid";
	}
	
	@Override
	public List<ToolbarItem> getSelectionToolbarItems() {
		if (selection_actions_toolbaritems!=null)
			return selection_actions_toolbaritems;
		selection_actions_toolbaritems = new ArrayList<ToolbarItem>();
		return selection_actions_toolbaritems; 
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
	
	protected Modal newSaveQueryModal() {
		return new SaveQueryModal("save-filters", getConsoleKey(), null);
	}
	
	protected void saveQuery(AjaxRequestTarget target, String title, Map<String, Object> parameters2) {
		((SaveQueryModal) get("save-filters")).open(target, title, false, parameters2, new Modal.Handler() {
			@Override
			public void onClick(AjaxRequestTarget target, Button button) {
			}
		});
	}
	
	protected void saveDashboardQuery(AjaxRequestTarget target, String title, Map<String, Object> parameters2) {
		((SaveQueryModal) get("save-filters")).open(target, title, true, parameters2, new Modal.Handler() {
			@Override
			public void onClick(AjaxRequestTarget target, Button button) {
			}
		});
	}
	
	protected List<LayoutPanel> getPanels() {
		
		List<LayoutPanel> panels = new ArrayList<LayoutPanel>();
		
		GridPanel<T> grid = new GridPanel<T>("grid", getQuery(), getColumns()) {
			
			@Override
			public Searcher getSearcher() {
				return AbstractFacetedBrowser.this.getSearcher();	
			}
			
			@Override
			protected String getContextKey() {
				return AbstractFacetedBrowser.this.getContextKey()+super.getContextKey();
			}
			
			@Override
			protected IModel<T> getModel(T object) {
				return AbstractFacetedBrowser.this.getModel(object);
			}
			
			@Override
			protected Panel getPanel(IModel<T> model, List<String> snippets) {
				return AbstractFacetedBrowser.this.getPanel(model, snippets);
			}
			
			@Override
			protected Panel getMenu(IModel<T> model) {
				return AbstractFacetedBrowser.this.getMenu(model);
			}
			
			@Override
			protected List<ToolbarItem> getSelectionToolbarItems() {
				return AbstractFacetedBrowser.this.getSelectionToolbarItems();
			}
			
			@Override
			protected void onSelectAll(AjaxRequestTarget target) {
				target.add(AbstractFacetedBrowser.this);
			}
			
			@Override
			protected boolean hasExpander() {
				return AbstractFacetedBrowser.this.hasExpander();
			}
			
			@Override
			protected boolean isSelectionEnabled() {
				return AbstractFacetedBrowser.this.isSelectionEnabled();
			}
			
			@Override
			protected boolean isMenuEnabled() {
				return AbstractFacetedBrowser.this.isMenuEnabled();
			}
			
			@Override
			protected String getConsoleKey() {
				return AbstractFacetedBrowser.this.getConsoleKey();
			}
			
			@Override
			protected String getConsoleDisplayName() {
				return AbstractFacetedBrowser.this.getConsoleDisplayName();
			}
			
			@Override
			protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
				return AbstractFacetedBrowser.this.getRowContainerCss(rowmodel);
			}
			
			@Override
			protected String getDefaultUserPreference(String key) {
				return AbstractFacetedBrowser.this.getDefaultUserPreference(key);
			}
		};
		
		
		FiltersPanel filters = new FiltersPanel("side", getQuery()) {
			@Override
			public Searcher getSearcher() {
				return AbstractFacetedBrowser.this.getSearcher();	
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				Query query = AbstractFacetedBrowser.this.getQuery();
				query.setParameters(getParameters());
				AbstractFacetedBrowser.this.onUpdateQuery(target);
				refresh(target);
			}
			@Override
 			public void onClose(AjaxRequestTarget target) {
				onClosePanel(this, target);
			}
			@Override
			public void onFavorites(AjaxRequestTarget target) {
				AbstractFacetedBrowser.this.onFavorites(target);
			}
			@Override
			protected boolean isVisible(Facet facet) {
				return AbstractFacetedBrowser.this.isVisible(facet);
			}
			@Override
			protected void saveQuery(AjaxRequestTarget target, String title, Map<String, Object> parameters2) {
				AbstractFacetedBrowser.this.saveQuery(target,  title,  parameters2);
			}
			@Override
			protected void saveDashboardQuery(AjaxRequestTarget target, String title, Map<String, Object> parameters2) {
				AbstractFacetedBrowser.this.saveDashboardQuery(target,  title,  parameters2);
			}
		};
		
		filters.setConsoleName(AbstractFacetedBrowser.this.getConsoleKey());
		filters.setConsoleDisplayName(AbstractFacetedBrowser.this.getConsoleDisplayName());
		filters.setVisible(false);
				
		String dm = getSessionUser().getService(PreferencesService.class).getValue(getConsoleKey() + "-" + "GridPanel", "displaymode",  GridDisplayMode.COMPACT_GRID_NO_BCK.getRsLabel());
		Toolbar toolbar = getToolbar();
		toolbar.setGlobalCss(dm);
		
		panels.add(new LayoutPanel (new MainPanel("main", toolbar, grid), TwoPanelsLayout.MAIN_DISPOSITION));
		panels.add(new LayoutPanel (filters, TwoPanelsLayout.SIDE_DISPOSITION));
		
		LayoutPanel tp=new LayoutPanel (getTopPanel(), TwoPanelsLayout.TOP_DISPOSITION);
		String tpp=getPreference("toppanel");
		
		if (tpp.equals("null"))
			tp.setVisible(isDefaultTopPanelVisible());
		else
			tp.setVisible(!tpp.equals("none"));
		
		panels.add(tp);

		return panels;
	}

	protected abstract void onUpdateQuery(AjaxRequestTarget target);
	
	protected void setDefaultTopPanelVisible(boolean b) {
		this.default_top_panel_visible=b;
	}

	protected boolean isDefaultTopPanelVisible() {
		return this.default_top_panel_visible;
	}

	protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
		return null;
	}
	
	protected void onFavorites(AjaxRequestTarget target) {
		togglePanel(SavedQueriesSidePanel.class);
		target.add(this);
		fire(new SidePanelEvent(target));
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
	protected Layout newLayout() {
		if (isFiltersEnabled()) {
			TwoPanelsLayout layout = new TwoPanelsLayout("layout");
			layout.setPanels(getPanels());
			String preference = getPreference("sidepanel");
			if (preference!=null && !"none".equals(preference) && !"null".equals(preference) && getPanelClass(preference)!=null) {
				WebMarkupContainer panel = layout.getPanel(getPanelClass(preference));
				if (panel!=null) 
					panel.setVisible(true);
			}
			String top_preference = getPreference("toppanel");
			if (top_preference!=null && !"none".equals(top_preference) && !"null".equals(top_preference) && getPanelClass(top_preference)!=null) {
				WebMarkupContainer top_panel = layout.getPanel(getPanelClass(top_preference));
				if ((top_panel!=null) && !(top_panel instanceof InvisiblePanel)) 
					top_panel.setVisible(true);
			}
			//setSidePreference(getPreference("sideplace"));
			return layout;
		}
		else {
			OnePanelLayout layout = new OnePanelLayout("layout");
			layout.setPanels(getPanels());
			String preference = getPreference("sidepanel");
			if (preference!=null && !"none".equals(preference) && !"null".equals(preference) && getPanelClass(preference)!=null) {
				WebMarkupContainer panel = layout.getPanel(getPanelClass(preference));
				if (panel!=null) 
					panel.setVisible(true);
			}
			/**
			 * {@link EmailAdvancedSearchSelectorPanel}
			 * {@link com.novamens.content.web.console.markup.searchselector.AdvancedSearchSelectorEditor}
			 */
			String top_preference = getPreference("toppanel");
			if (top_preference!=null && !"none".equals(top_preference) && !"null".equals(top_preference) && getPanelClass(top_preference)!=null) {
				WebMarkupContainer top_panel = layout.getPanel(getPanelClass(top_preference));
				if ((top_panel!=null) && !(top_panel instanceof InvisiblePanel)) 
					top_panel.setVisible(true);
			}
			return layout;
		}
	}
	
	/***
	 *    Console general toolbar items.
	 *    IMPORTANT. The Semantics is: right actions apply to all items in the (filtered) grid 
	 *    
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
			GridConfigButton c = new GridConfigButton(this, 	ToolbarItem.Align.TOP_RIGHT);
			c.setGridSwitcher(this.isListBrowserSwitch());
			c.setTreeSwitcher(isTreeBrowserSwitch());
			c.setRememberQuery(this.isRememberQuery());
			this.items.add(c);
		}
		
		if (isSavedQueriesEnabled())
			this.items.add(new SavedQueriesButton(this,  ToolbarItem.Align.TOP_RIGHT));
		
		if (isFiltersEnabled())
			this.items.add(new FiltersButton(this, 	ToolbarItem.Align.TOP_RIGHT));

		return this.items;
	}
	
	@Override
	protected Toolbar getToolbar() {
		return new Toolbar("toolbar", getToolbarItems());
	}

	@Override
	protected boolean hasExpander() {
		return false;
	}	
	
	protected boolean isVisible(Facet facet) {
		return true;
	}

	@Override
	protected boolean isFiltersEnabled() {
		return true;
	}
	
	@Override
	protected boolean isSettingsEnabled() {
		return true;
	}
	
	@Override
	protected boolean isSavedQueriesEnabled() {
		return false;
	}
	
	protected void setSidePreference(String preference) {
		
		if ("left".equals(preference)) {
			Panel sidepanel = AbstractFacetedBrowser.this.getPanel(AbstractLayout.SIDE_DISPOSITION);
			boolean hasSidepanel = (sidepanel!=null && sidepanel.isVisible());
			if (hasSidepanel) {			
			    getPanel(FiltersPanel.class).add(new AttributeModifier("class","left-panel secondary-panel col-md-4 col-lg-4 col-xs-12"));
				getPanel(MainPanel.class).add(new AttributeModifier("class","right-panel primary-panel col-md-8 col-lg-8 col-xs-12 ui-resizable layout-twopanels"));
			}
			else {											
				getPanel(MainPanel.class).add(new AttributeModifier("class","right-panel primary-panel col-md-12 col-lg-12 col-xs-12 ui-resizable layout-twopanels"));
			}
		}
		else {
			Panel sidepanel = AbstractFacetedBrowser.this.getPanel(AbstractLayout.SIDE_DISPOSITION);
			boolean hasSidepanel = (sidepanel!=null && sidepanel.isVisible());
			if (hasSidepanel) {			
				getPanel(FiltersPanel.class).add(new AttributeModifier("class","right-panel secondary-panel col-md-4 col-lg-4 col-xs-12"));
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
		} else   
			setPreference("toppanel", "none");
		target.add(AbstractFacetedBrowser.this);
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
