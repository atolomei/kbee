package kbee.web.console;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.model.Classificable;
import com.novamens.dom.Versionable;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.browser.AbstractListBrowser;
import com.novamens.kbee.wicket.markup.html.console.browser.AbstractSimpleBrowser;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.layout.AbstractLayout;
import com.novamens.kbee.wicket.markup.html.console.layout.LayoutPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.ConsoleSidePanel;
import com.novamens.kbee.wicket.markup.html.console.panel.FiltersPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.SaveQueryModal;
import com.novamens.kbee.wicket.markup.html.console.panel.SavedQueriesSidePanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.Identifiable;
import com.novamens.wicket.markup.html.actions.LinkMenuItemPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.repeater.util.NavigationOrder;
import com.novamens.wicket.markup.html.repeater.util.Searcher;

import kbee.web.panel.ClickItemEvent;

public abstract class AbstractSimpleConsole<T> extends AbstractConsole<T> {
	private static final long serialVersionUID = 1L;
					
	//private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AbstractSimpleConsole.class.getName());

 	private String browser_type= "grid";
 	
	public AbstractSimpleConsole(String id, String name, Query query) {
		super(id, name, query); 		
	}
	
	public AbstractSimpleConsole(String name, Query query) {
		super(name, query); 		
	}

	public void setBrowserType(String s) {
		this.browser_type=s;
	}
	
	protected BaseBrowser<T> newGridBrowser() {

		if (browser_type!=null && browser_type.equals("list"))
			return newListBrowser(); 
		

		return new AbstractSimpleBrowser<T>("browser", getName(), getQuery()) {
			
			/**
			 * @return
			 */
			protected String getDefaultUserPreference(String key) {
				return AbstractSimpleConsole.this.getDefaultUserPreference(key);
			}

			@Override
			public List<NavigationOrder> getOrders() {
				return AbstractSimpleConsole.this.getOrders();
			}
			@Override
			public Searcher getSearcher() {
				return AbstractSimpleConsole.this.getSearcher();
			}
			
			@Override
			public List<ToolbarItem> getSelectionToolbarItems() {
				List<ToolbarItem> items = super.getSelectionToolbarItems();
				items.addAll(AbstractSimpleConsole.this.getSelectionToolbarItems(this));
				return items;
			}
			
			@Override
			protected String getContextKey() {
				return AbstractSimpleConsole.this.getName() + super.getContextKey();
			}
			
			@Override
			protected IModel<T> getModel(T object) {
				return AbstractSimpleConsole.this.getModel(object);
			}
			@Override
			protected Panel getPanel(IModel<T> model) {
				return AbstractSimpleConsole.this.getPanel(model);
			}
			
			@Override
			protected Panel getPanel(IModel<T> model, List<String> snippets) {
				return AbstractSimpleConsole.this.getPanel(model, snippets);
			}
			@Override
			protected List<GridColumn<SearchResult, String>> getColumns() {
				return AbstractSimpleConsole.this.getColumns();
			}
			@Override
			protected Panel getMenu(IModel<T> model) {
				return AbstractSimpleConsole.this.getMenu(model);
			}
			@Override
			protected List<ToolbarItem> getToolbarItems() {
				List<ToolbarItem> items = new ArrayList<ToolbarItem>();
				List<ToolbarItem> items_console = AbstractSimpleConsole.this.getToolbarItems(this);
				List<ToolbarItem> items_super = super.getToolbarItems();
				items.addAll(items_super);
				items_console.forEach(v -> { if (v.getJustify() == ToolbarItem.JUSTIFY_LEFT) items.add(v);});
				items_console.forEach(v -> { if (v.getJustify() == ToolbarItem.JUSTIFY_RIGHT) items.add(v);}); 
				items.add(AbstractSimpleConsole.this.getGridToolbarMenuItem());
				return items;
			}
			
			@Override
			protected Panel getTopPanel() {
				if (AbstractSimpleConsole.this.hasTopPanel())
					return AbstractSimpleConsole.this.getTopPanel();
				return new InvisiblePanel("top");
			}

			
			@Override
			protected boolean hasExpander() {
				return AbstractSimpleConsole.this.hasExpander();
			}
			@Override
			protected boolean isMenuEnabled() {
				return AbstractSimpleConsole.this.isMenuEnabled();
			}
			@Override
			protected ConsoleSidePanel getRightPanel() {
				return AbstractSimpleConsole.this.getRightPanel();
			}
			
			@Override
			protected boolean isFiltersEnabled() {
				return AbstractSimpleConsole.this.isFiltersEnabled();
			}
	
			
			@Override
			protected List<LayoutPanel> getPanels() {
				
				List<LayoutPanel> panels = super.getPanels();
				
				panels.add(new LayoutPanel ("side", AbstractLayout.SIDE_DISPOSITION) {
					protected WebMarkupContainer getPanel(String id) {
						SavedQueriesSidePanel sq= new SavedQueriesSidePanel(id, getBrowser()) {
							@Override
							public void onClose(AjaxRequestTarget target) {
								onClosePanel(this, target);
							}
							@Override
							public void onFilters(AjaxRequestTarget target) {
								AbstractSimpleConsole.this.onFilters(target);
							}
						};
						return sq;
					}
				});

				
				return panels;
			}

			/** 
			 * Check column for multiple selection
			 */
			@Override
			protected boolean isSelectionEnabled() {
				return AbstractSimpleConsole.this.isSelectionEnabled();
			}

			@Override
			public boolean isRememberQuery() {
				return AbstractSimpleConsole.this.isRememberQuery();
			}

			
		};
 	}
	

	protected BaseBrowser<T> newListBrowser() {
		
		return new AbstractListBrowser<T>("browser", getName(), getQuery()) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected Modal newSaveQueryModal() {
				return new SaveQueryModal("save-filters", getConsoleKey(), null);
			}
			
			@Override
			protected boolean isFiltersEnabled() {
				return AbstractSimpleConsole.this.isFiltersEnabled();
			}
			protected String getDefaultUserPreference(String key) {
				return null;
			}
			@Override
			public List<NavigationOrder> getOrders() {
				return  AbstractSimpleConsole.this.getOrders();
			}
			@Override
			public Searcher getSearcher() {
				return AbstractSimpleConsole.this.getSearcher();
			}
			@Override
			protected String getContextKey() {
				return getConsoleKey();
			}
			@Override
			protected IModel<T> getModel(T c) {
				return  AbstractSimpleConsole.this.getModel(c);
			}
			
			@Override
			protected Panel getPanel(IModel<T> model) {
				//return AbstractFacetedConsole.this.getPanel(model, 0, false);
				//return AbstractFacetedConsole.this.getPanel(model);
				return null;
			}
			
			@Override
			protected Panel getPanel(IModel<T> model, List<String> snippets) {
				//return AbstractFacetedConsole.this.getPanel(model, 0, false);
				//return AbstractFacetedConsole.this.getPanel(model);
				return null;
			}
			protected Panel getPanel(IModel<T> model, int index, boolean expanded) {
				//return AbstractFacetedConsole.this.getPanel(model, index, expanded);
				//return AbstractFacetedConsole.this.getPanel(model);
				return null;
			}
			@Override
			protected Panel getMenu(IModel<T> model) {
				return AbstractSimpleConsole.this.getMenu(model);
			}
			@Override
			protected Panel getTopPanel() {
				return new InvisiblePanel("top");
			}
			@Override
			protected List<ToolbarItem> getToolbarItems() {
				List<ToolbarItem> items = new ArrayList<ToolbarItem>();
				items.addAll(super.getToolbarItems());
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
				return true;
			}
			@Override
			protected List<LayoutPanel> getPanels() {
				List<LayoutPanel> panels = super.getPanels();
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
				return false;
			}
			@Override
			public Query getQuery() {
				return AbstractSimpleConsole.this.getQuery();
			}

			@Override
			protected Panel getItemListPanel(IModel<T> model, int index) {
				return AbstractSimpleConsole.this.getItemListPanel(model, index);
			}

			@Override
			public boolean isRememberQuery() {
				return AbstractSimpleConsole.this.isRememberQuery();
			}

			@Override
			protected void onUpdateQuery(AjaxRequestTarget target) {
				AbstractSimpleConsole.this.onUpdateQuery(target);
				
			}

			@Override
			protected String getIcon(IModel<T> model) {
				return AbstractSimpleConsole.this.getIcon(model);
			}

			@Override
			protected boolean hasIcon(IModel<T> model) {
				return AbstractSimpleConsole.this.hasIcon(model);
			}
			
			
			
		};
		
	}

	
	@Override
	protected Panel getItemListPanel(IModel<T> model , int index) {
		LinkMenuItemPanel<T> link = new LinkMenuItemPanel<T>("item", model , index) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() throws Exception {
					fire( new ClickItemEvent<T>( getModel(), getIndex()));
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


	protected String getIcon(IModel<T> model) {
		if (isCheckout(model))
			return "fal fa-lock";
		
		if (isFolder(model))
			return "fa-light fa-folder";
		
		return "";
	}
	
	protected boolean isFolder(IModel<T> model) {
		// TOD AT
		return false;

	}

	protected boolean hasIcon(IModel<T> model) {
		return isCheckout(model) || isFolder(model);
	}

	protected boolean isCheckout(IModel<T> model) {
		
		if (! (model.getObject() instanceof Versionable))
			return false;
		
		@SuppressWarnings("unchecked")
		Versionable<Classificable> v = (Versionable<Classificable>) model.getObject();
		if ((!v.isHeadVersion()) && (v.getVersion()>0))
			return true;
		
		return false;
		
	}


	

	protected void onFilters(AjaxRequestTarget target) {
		getBrowser().togglePanel(FiltersPanel.class);
		target.add(getBrowser());
		fire(new SidePanelEvent(target));
	}

	
	@Override
	protected void addListeners() {
		 super.addListeners();
	}

	protected ConsoleSidePanel getRightPanel() {
		return null;
	}
	
	protected boolean isMenuEnabled() {
		return true;
	}
	
	 
	
	protected List<MenuItemFactory<T>> getSelectionActionsMenu() {
		return null;
	}

	protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
		return super.getRowContainerCss(rowmodel);
	}
	
	/**
	 * Not sure if this works . please test !!!
	 * @return
	 */
	protected Panel getTopPanel() {
		return new InvisiblePanel("top");
	}
	
	
	protected List<NavigationOrder> getOrders() {
		List<NavigationOrder> orders = new ArrayList<NavigationOrder>();
		orders.add(new NavigationOrder(getLabel("modified-desc"), "modified", false));
		orders.add(new NavigationOrder(getLabel("modified-asc"), "modified", true));
		orders.add(new NavigationOrder(getLabel("title"), "title_sort", true));
		orders.add(new NavigationOrder(getLabel("relevance"), "relevance", false));
		return orders;
	}
}
