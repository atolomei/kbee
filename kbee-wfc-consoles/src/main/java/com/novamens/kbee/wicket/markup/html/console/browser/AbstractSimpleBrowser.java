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
import com.novamens.kbee.wicket.markup.html.console.grid.GridConfigButton;
import com.novamens.kbee.wicket.markup.html.console.grid.GridPanel;
import com.novamens.kbee.wicket.markup.html.console.layout.TwoPanelsLayout;
import com.novamens.kbee.wicket.markup.html.console.panel.ConsoleSidePanel;
import com.novamens.kbee.wicket.markup.html.console.panel.FiltersPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.InvisibleConsoleSidePanel;
import com.novamens.kbee.wicket.markup.html.console.panel.SaveQueryModal;
import com.novamens.kbee.wicket.markup.html.console.panel.SavedQueriesSidePanel;
import com.novamens.kbee.wicket.markup.html.console.layout.AbstractLayout;
import com.novamens.kbee.wicket.markup.html.console.layout.LayoutPanel;

import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.modal.Modal.Button;
import com.novamens.wicket.markup.html.repeater.util.Searcher;

import kbee.web.console.Layout;



/**
 * 
 *  <p>Used by {@code AbstractSimpleConsole}</p>
 *  
 * @param <T>
 */
@SuppressWarnings("serial")
public abstract class AbstractSimpleBrowser<T> extends AbstractBrowser<T> {
						
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AbstractSimpleBrowser.class.getName());

	private List<ToolbarItem> items;
	private List<ToolbarItem> selection_actions_toolbaritems;
	
	
	
	public class MainPanel extends Fragment {
		public MainPanel(String id, Panel toolbar, Panel grid) {
			super(id, "main-fragment", AbstractSimpleBrowser.this);
			add(grid);
			add(toolbar);
			add(new AttributeModifier("style", new Model<String>() {
				public String getObject() {
					Panel sidepanel = AbstractSimpleBrowser.this.getPanel(AbstractLayout.SIDE_DISPOSITION);
					return sidepanel==null || !sidepanel.isVisible() ? "width:100%;overflow:auto;" : "overflow:auto;";
				}
			}));
			add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					Panel sidepanel = AbstractSimpleBrowser.this.getPanel(AbstractLayout.SIDE_DISPOSITION);
					return sidepanel==null || !sidepanel.isVisible() ? "left-panel col-md-8 col-lg-8 col-xs-12 ui-resizable layout-onepanel " : "left-panel col-md-8 col-lg-8 col-xs-12 ui-resizable layout-twopanels ";
				}
			}));
		}
	} 
	public AbstractSimpleBrowser(String id, String consoleName, Query query) {
		super(id, consoleName, query);
	}
	
	public Searcher getSearcher() {
		return null;
	}
	
	
	@Override
	public String getBrowserType() {
		return "grid";
	}
	
	
	@Override
	protected boolean isFiltersEnabled() {
		return false;
	}
	
	public SaveQueryModal getSaveQueryModal() {
		return (SaveQueryModal) get("save-filters");
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		this.items=null;
		this.selection_actions_toolbaritems=null;
	}

	
	protected void onClosePanel(Panel panel, AjaxRequestTarget target) {
		panel.setVisible(false);
		setPreference("sidepanel", "none");
		target.add(AbstractSimpleBrowser.this);
		fire(new SidePanelEvent(target));
		
	}
	
	
	protected Modal newSaveQueryModal() {
		return new SaveQueryModal("save-filters", getConsoleKey(), null);
	}
	

	protected void saveQuery(AjaxRequestTarget target, String title, Map<String, Object> parameters2) {
		((SaveQueryModal) get("save-filters")).open(target, title, false,  parameters2, new Modal.Handler() {
			@Override
			public void onClick(AjaxRequestTarget target, Button button) {
			}
		});
	}
	
	protected List<LayoutPanel> getPanels() {
	
		List<LayoutPanel> panels = new ArrayList<LayoutPanel>();
		
		GridPanel<T> grid = new GridPanel<T>("grid", getQuery(), getColumns()) {
		
			@Override
			protected String getDefaultUserPreference(String key) {
				return AbstractSimpleBrowser.this.getDefaultUserPreference(key);
			}
			
			@Override
			protected List<ToolbarItem> getSelectionToolbarItems() {
				return AbstractSimpleBrowser.this.getSelectionToolbarItems();
			}
			@Override
			public Searcher getSearcher() {
				return AbstractSimpleBrowser.this.getSearcher();	
			}
			@Override
			protected String getContextKey() {
				return AbstractSimpleBrowser.this.getContextKey()+super.getContextKey();
			}
			@Override
			protected IModel<T> getModel(T object) {
				return AbstractSimpleBrowser.this.getModel(object);
			}
			@Override
			protected Panel getPanel(IModel<T> model, List<String> snippets) {
				return AbstractSimpleBrowser.this.getPanel(model, snippets);
			}
			@Override
			protected Panel getMenu(IModel<T> model) {
				return AbstractSimpleBrowser.this.getMenu(model);
			}
			@Override
			protected void onSelectAll(AjaxRequestTarget target) {
				target.add(AbstractSimpleBrowser.this);
			}
			@Override
			protected boolean hasExpander() {
				return AbstractSimpleBrowser.this.hasExpander();
			}
			@Override
			protected boolean isSelectionEnabled() {
				return AbstractSimpleBrowser.this.isSelectionEnabled();
			}
			@Override
			protected boolean isMenuEnabled() {
				return AbstractSimpleBrowser.this.isMenuEnabled();
			}
			@Override
			protected String getConsoleKey() {
				return AbstractSimpleBrowser.this.getConsoleKey();
			}
			
			@Override
			protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
				return AbstractSimpleBrowser.this.getRowContainerCss(rowmodel);
			}

		};
		
		
		
		FiltersPanel filters = new FiltersPanel("side", getQuery()) {
			@Override
			public Searcher getSearcher() {
				return AbstractSimpleBrowser.this.getSearcher();	
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				grid.getQuery().setParameters(getParameters());
				target.add(AbstractSimpleBrowser.this);
			}
			@Override
			public void onClose(AjaxRequestTarget target) {
				onClosePanel(this, target);
			}
			
			@Override
			public void onFavorites(AjaxRequestTarget target) {
				AbstractSimpleBrowser.this.onFavorites(target);
			}
			@Override
			protected boolean isVisible(Facet facet) {
				return false;
			}
			
			protected void saveQuery(AjaxRequestTarget target, String title, Map<String, Object> parameters2) {
				AbstractSimpleBrowser.this.saveQuery(target,  title,  parameters2);
			}
		};
		
		filters.setConsoleName(AbstractSimpleBrowser.this.getConsoleKey());
		filters.setConsoleDisplayName(AbstractSimpleBrowser.this.getConsoleDisplayName());
		filters.setVisible(false);
		
		panels.add(new LayoutPanel (new MainPanel("main", getToolbar(), grid), TwoPanelsLayout.MAIN_DISPOSITION));
		panels.add(new LayoutPanel (filters, TwoPanelsLayout.SIDE_DISPOSITION));
		panels.add(new LayoutPanel (getTopPanel(), TwoPanelsLayout.TOP_DISPOSITION));
		
		ConsoleSidePanel panel = getRightPanel();
		
		if (panel==null) 
			panel = new InvisibleConsoleSidePanel("side");
		
		panel.setConsoleName(AbstractSimpleBrowser.this.getConsoleKey());
		panel.setConsoleDisplayName(AbstractSimpleBrowser.this.getConsoleDisplayName());

		panel.setVisible(false);
		panels.add(new LayoutPanel (panel, TwoPanelsLayout.SIDE_DISPOSITION));
		
		return panels;
	}	


	
	protected void onFavorites(AjaxRequestTarget target) {
		togglePanel(SavedQueriesSidePanel.class);
		target.add(this);
		fire(new SidePanelEvent(target));
	}
	
	@Override
	public <P extends WebMarkupContainer> void togglePanel(Class<P> panelclass) {
		super.togglePanel(panelclass);
		if (getPanel(panelclass)!=null && getPanel(panelclass).isVisible()) 
			setPreference("sidepanel", panelclass.getName());
		else 
			setPreference("sidepanel", "none");
	}
	
	
	@Override
	public void refresh(AjaxRequestTarget target) {
		super.refresh(target);
		getPanel(DataViewPanel.class).refresh(target);
		getPanel(FiltersPanel.class).reload(target);
	}
	
	
	@Override
	protected Layout newLayout() {
		TwoPanelsLayout layout = new TwoPanelsLayout("layout");
		layout.setPanels(getPanels());
		String preference = getPreference("sidepanel");
		if (preference!=null && !"none".equals(preference) && !"null".equals(preference) && getPanelClass(preference)!=null) {
			WebMarkupContainer panel = layout.getPanel(getPanelClass(preference));
			if (panel!=null) 
				panel.setVisible(true);
		}

		return layout;
	}

	

	
	@Override
	protected Toolbar getToolbar() {
		return new Toolbar("toolbar", getToolbarItems());
	}

	
	
	
	protected ConsoleSidePanel getRightPanel() {
		return new InvisibleConsoleSidePanel("side");
	}

	@Override
	protected boolean hasExpander() {
		return false;
	}

	@Override
	protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
		return null;
	}


	@Override
	public List<ToolbarItem> getSelectionToolbarItems() {

		if (selection_actions_toolbaritems!=null)
			return selection_actions_toolbaritems;
		
		selection_actions_toolbaritems = new ArrayList<ToolbarItem>();
		
		return selection_actions_toolbaritems; 
	}
	
	
	@Override
	protected List<ToolbarItem> getToolbarItems() {
		if (items!=null)
			return items;
		items = new ArrayList<ToolbarItem>();
		items.add(new NavigationLabel(this, ToolbarItem.Align.TOP_RIGHT));
		items.add(new PreviousButton(this, 	ToolbarItem.Align.TOP_RIGHT));
		items.add(new NextButton(this, 		ToolbarItem.Align.TOP_RIGHT));
		items.add(new RefreshButton(this, 	ToolbarItem.Align.TOP_RIGHT));
		items.add(new GridConfigButton(this, 	ToolbarItem.Align.TOP_RIGHT));
		if (isFiltersEnabled())
			items.add(new FiltersButton(this, 	ToolbarItem.Align.TOP_RIGHT));
		return items;
	}

	
	
	
	@SuppressWarnings("unchecked")
	private <P extends WebMarkupContainer> Class<P> getPanelClass(String classname) {
		try {
			return (Class<P>)Class.forName(classname);
		}
		catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	
	

	
	boolean default_top_panel_visible = false;
	
	protected void setDefaultTopPanelVisible(boolean b) {
		this.default_top_panel_visible=b;
	}

	protected boolean isDefaultTopPanelVisible() {
		return this.default_top_panel_visible;
	}
	
	
	
}
