package com.novamens.kbee.wicket.markup.html.console.browser;


import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.GridDisplayMode;
import com.novamens.kbee.wicket.markup.html.console.layout.TwoPanelsLayout;
import com.novamens.kbee.wicket.markup.html.console.list.ListConfigButton;
import com.novamens.kbee.wicket.markup.html.console.list.ListPanel;
import com.novamens.kbee.wicket.markup.html.console.layout.AbstractLayout;
import com.novamens.kbee.wicket.markup.html.console.layout.LayoutPanel;
import com.novamens.kbee.wicket.markup.html.console.layout.OnePanelLayout;
import com.novamens.kbee.wicket.markup.html.console.panel.FiltersPanel;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.repeater.util.Searcher;

import kbee.web.console.Layout;

 
public abstract class AbstractListBrowser<T> extends AbstractFacetedBrowser<T> {
	
	private static final long serialVersionUID = 1L;

	private List<ToolbarItem> items;

	private boolean include_grid_browser_switcher = true;

	/***
	 * @param id
	 * @param consoleName
	 * @param query
	 */
	public AbstractListBrowser(String id, String consoleName, Query query) {
		super(id, consoleName, query);
		setOutputMarkupId(true);
	}
	
	public String getBrowserType() { return "list";}
	
	public boolean isGridBrowserSwitch() {
		return include_grid_browser_switcher;
	}
	
	public void setGridBrowserSwitch(boolean b) {
		include_grid_browser_switcher=b;
	}
	

	@Override
	protected void setSidePreference(String preference) {
        
        // AA preference = "left";
        
        if ("left".equals(preference)) {
            Panel sidepanel = AbstractListBrowser.this.getPanel(AbstractLayout.SIDE_DISPOSITION);
            boolean hasSidepanel = (sidepanel!=null && sidepanel.isVisible());
            if (hasSidepanel) {         
                getPanel(FiltersPanel.class).add(new AttributeModifier("class","left-panel secondary-panel col-md-4 col-lg-4 col-xs-12"));
                getPanel(MainPanel.class).add(new AttributeModifier("class","right-panel primary-panel col-md-8 col-lg-8 col-xs-12 ui-resizable layout-twopanels"));
                
                // AA FiltersPanel panel = getPanel(FiltersPanel.class);
                // AA panel.add(new AttributeModifier("style", "width: calc(21.5% + 3px) !important; float:left; min-height: 93vh; border-right: none; padding-left: 60px;"));
                // AA MainPanel main = getPanel(MainPanel.class);
                // AA main.add(new AttributeModifier("style", "width: calc(78.5% - 2px) !important; float:right;" ));
                
                
            }
            else {                                          
                getPanel(MainPanel.class).add(new AttributeModifier("class","right-panel primary-panel col-md-12 col-lg-12 col-xs-12 ui-resizable layout-twopanels"));
                //getPanel(MainPanel.class).add(new AttributeModifier("style","width:100%;"));
            }
            
            // getPanel(MainPanel.class).add(new AttributeModifier("style","width: calc(78.5% - 2px); float:right;"));
        }
        else {
            Panel sidepanel = AbstractListBrowser.this.getPanel(AbstractLayout.SIDE_DISPOSITION);
            boolean hasSidepanel = (sidepanel!=null && sidepanel.isVisible());
            if (hasSidepanel) {         
                getPanel(FiltersPanel.class).add(new AttributeModifier("class","right-panel secondary-panel col-md-4 col-lg-4 col-xs-12"));
                getPanel(MainPanel.class).add(new AttributeModifier("class","left-panel primary-panel col-md-8 col-lg-8 col-xs-12 ui-resizable layout-twopanels"));
            }
            else {
                getPanel(MainPanel.class).add(new AttributeModifier("class","left-panel primary-panel col-md-12 col-lg-12 col-xs-12 ui-resizable layout-twopanels"));
                //getPanel(MainPanel.class).add(new AttributeModifier("style","width:100%;"));
            }
        }
        //getPanel(FiltersPanel.class).add(new AttributeModifier("style","width: calc( 21.5% + 3px );"));
        
        if (preference!=null) {
            setPreference("sideplace", preference);
        }
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
	
	
	
	protected List<LayoutPanel> getPanels() {
		
		String dm = getSessionUser().getService(PreferencesService.class).getValue(getConsoleKey() + "-" + "GridPanel", "displaymode",  GridDisplayMode.COMFORTABLE_GRID_NO_BCK.getCss());
		Toolbar toolbar = getToolbar();
		toolbar.setGlobalCss(dm);
		
		List<LayoutPanel> panels = new ArrayList<LayoutPanel>();
		
		ListPanel<T> list = new ListPanel<T>("grid", getQuery()) {
			
			@Override
			public boolean isIconSupported() {
				return AbstractListBrowser.this.isIconSupported();
			}
			
			@Override
			public Searcher getSearcher() {
				return AbstractListBrowser.this.getSearcher();	
			}
			@Override
			protected String getContextKey() {
				return AbstractListBrowser.this.getContextKey();
			}
			@Override
			protected IModel<T> getModel(T object) {
				return AbstractListBrowser.this.getModel(object);
			}
			@Override
			protected Panel getPanel(IModel<T> model, List<String> snippets) {
				return AbstractListBrowser.this.getPanel(model, snippets);
			}
			@Override
			protected Panel getPanel(IModel<T> model, int index, boolean expanded) {
				return AbstractListBrowser.this.getPanel(model, index, expanded);
			}
			@Override
			protected Panel getMenu(IModel<T> model) {
				return AbstractListBrowser.this.getMenu(model);
			}
			
			@Override
			protected boolean hasIcon(IModel<T> model) {
				return AbstractListBrowser.this.hasIcon(model);
			}
			
			@Override
			protected String getIcon(IModel<T> model) {
				return AbstractListBrowser.this.getIcon(model);
			}
			
			@Override
			protected List<ToolbarItem> getSelectionToolbarItems() {
				return AbstractListBrowser.this.getSelectionToolbarItems();
			}
			@Override
			protected boolean hasExpander() {
				return AbstractListBrowser.this.hasExpander();
			}
			@Override
			protected boolean isSelectionEnabled() {
				return AbstractListBrowser.this.isSelectionEnabled();
			}
			@Override
			protected boolean isMenuEnabled() {
				return AbstractListBrowser.this.isMenuEnabled();
			}
			
			
			
			
			@Override
			protected Panel getItemListPanel(IModel<T> model, int index) {
				return AbstractListBrowser.this.getItemListPanel(model, index);
			}
		};
		
		
		// list.setListDisplayMode(ListDisplayMode.COMPACT_LIST_NO_BCK);
		
		
		if (isFiltersEnabled()) {
			
				FiltersPanel filters = new FiltersPanel("side", getQuery()) {
					@Override
					public Searcher getSearcher() {
						return AbstractListBrowser.this.getSearcher();	
					}
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						list.getQuery().setParameters(getParameters());
						refresh(target);
					}
					@Override
					public void onClose(AjaxRequestTarget target) {
						onClosePanel(this, target);
					}
					@Override
					public void onFavorites(AjaxRequestTarget target) {
						AbstractListBrowser.this.onFavorites(target);
					}
					@Override
					protected boolean isVisible(Facet facet) {
						return AbstractListBrowser.this.isVisible(facet);
					}
					
					@Override
					protected void saveQuery(AjaxRequestTarget target, String title, Map<String, Object> parameters2) {
						AbstractListBrowser.this.saveQuery(target,  title,  parameters2);
					}
					
					@Override
					protected void saveDashboardQuery(AjaxRequestTarget target, String title, Map<String, Object> parameters2) {
						AbstractListBrowser.this.saveDashboardQuery(target,  title,  parameters2);
					}
				};
				
				filters.setConsoleName(getConsoleKey());
				filters.setConsoleDisplayName(getConsoleDisplayName());
				filters.setVisible(true);
				
				panels.add(new LayoutPanel (new MainPanel("main", toolbar, list), TwoPanelsLayout.MAIN_DISPOSITION));
				panels.add(new LayoutPanel (filters, TwoPanelsLayout.SIDE_DISPOSITION));
				LayoutPanel tp=new LayoutPanel (getTopPanel(), TwoPanelsLayout.TOP_DISPOSITION);

				String tpp=getPreference("toppanel");
				if (tpp.equals("null"))
					tp.setVisible(isDefaultTopPanelVisible());
				else
					tp.setVisible(!tpp.equals("none"));
				
				panels.add(tp);
				
		}
		else {
			panels.add(new LayoutPanel (new MainPanel("main", toolbar, list), OnePanelLayout.MAIN_DISPOSITION));
			LayoutPanel tp=new LayoutPanel (getTopPanel(), OnePanelLayout.TOP_DISPOSITION);

			String tpp=getPreference("toppanel");
			if (tpp.equals("null"))
				tp.setVisible(isDefaultTopPanelVisible());
			else
				tp.setVisible(!tpp.equals("none"));

			panels.add(tp);
		}
				
		
		
		
		
		

		return panels;
	}
	

	protected boolean isIconSupported() {
		return true;
	}

	protected abstract String getIcon(IModel<T> model);
	protected abstract boolean hasIcon(IModel<T> model);
	protected abstract Panel getItemListPanel(IModel<T> model, int index);

	@Override
	protected List<ToolbarItem> getToolbarItems() {
		if (this.items!=null)
			return this.items;
		
		this.items = new ArrayList<ToolbarItem>();
		
		if (getOrders()!=null && getOrders().size()>0)
			this.items.add(new OrderSelector(this, 		ToolbarItem.Align.TOP_RIGHT));
		
		this.items.add(new NavigationLabel(this, 	ToolbarItem.Align.TOP_RIGHT));
		this.items.add(new PreviousButton(this, 	ToolbarItem.Align.TOP_RIGHT));
		this.items.add(new NextButton(this, 		ToolbarItem.Align.TOP_RIGHT));		
		this.items.add(new RefreshButton(this, 		ToolbarItem.Align.TOP_RIGHT));
		
		if (isSettingsEnabled()) {
			ListConfigButton c=new ListConfigButton(this, 	ToolbarItem.Align.TOP_RIGHT);
			
			
			c.setGridSwitcher(isGridBrowserSwitch());
			this.items.add(c);
		}
		
		if (isFiltersEnabled())
			this.items.add(new FiltersButton(this, 	ToolbarItem.Align.TOP_RIGHT));

		return this.items;
	}
	
	protected Panel getPanel(IModel<T> model, int index, boolean expanded) {
		return null;
	}

	protected List<GridColumn<SearchResult, String>> getColumns() {
		return null;
	}
}
