package kbee.web.console;



import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import com.novamens.content.query.SavedQuery;

import com.novamens.datetime.DateTimeService;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.browser.AbstractFacetedBrowser;
import com.novamens.kbee.wicket.markup.html.console.browser.AbstractListBrowser;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.layout.AbstractLayout;
import com.novamens.kbee.wicket.markup.html.console.layout.LayoutPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.ApplySavedQueryEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.ConsoleSidePanel;
import com.novamens.kbee.wicket.markup.html.console.panel.DownloadMenuItemPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.FiltersPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.SaveQueryModal;
import com.novamens.kbee.wicket.markup.html.console.panel.SavedQueriesSidePanel;
import com.novamens.kbee.wicket.markup.html.console.tree.TreeNode;
import com.novamens.kbee.wicket.markup.html.console.tree.TreeProvider;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.Identifiable;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.LinkMenuItemPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.repeater.util.NavigationOrder;
import com.novamens.wicket.markup.html.repeater.util.Searcher;

import kbee.web.panel.ClickItemEvent;

 
public abstract class AbstractFacetedConsole<T> extends AbstractConsole<T> {
	private static final long serialVersionUID = 1L;

	static protected final int MAX = 280;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AbstractFacetedConsole.class.getName());

	public AbstractFacetedConsole(String id, String name, Query query) {
		super(id, name, query);
	}

	public AbstractFacetedConsole(String name, Query query) {
		super(name, query);
	}
	
	protected void addModals() {
		super.addModals();
	}

	@Override
	protected boolean isFiltersEnabled() {
		return true;
	}

	protected Page getPageV6(IModel<T> model) {
		return null;
	}

	@Override
	protected Panel getItemListPanel(IModel<T> model , int index) {
		LinkMenuItemPanel<T> link = new LinkMenuItemPanel<T>("item", model , index) {
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
	
	protected BaseBrowser<T> newListBrowser() {
		
		AbstractListBrowser<T> br = new AbstractListBrowser<T>("browser", getName(), getQuery()) {

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
				return  AbstractFacetedConsole.this.getOrders();
			}
			@Override
			public Searcher getSearcher() {
				return AbstractFacetedConsole.this.getSearcher();
			}
			@Override
			protected String getContextKey() {
				return getConsoleKey();
			}
			@Override
			protected IModel<T> getModel(T c) {
				return  AbstractFacetedConsole.this.getModel(c);
			}
			/** Item Panel -> Title  */
			@Override
			protected Panel getItemListPanel(IModel<T> model, int index) {
				return AbstractFacetedConsole.this.getItemListPanel(model, index);
			}
			/** Expanded Hit Panel */
			@Override
			protected Panel getPanel(IModel<T> model) {
				return AbstractFacetedConsole.this.getPanel(model);
			}
			/** Expanded Hit Panel */
			@Override
			protected Panel getPanel(IModel<T> model, List<String> snippets) {
				return AbstractFacetedConsole.this.getPanel(model, snippets);
			}
			/** Expanded Hit Panel */
			@Override
			protected Panel getPanel(IModel<T> model, int index, boolean expanded) {
				return AbstractFacetedConsole.this.getPanel(model);
			}
			/** Menu */
			@Override
			protected Panel getMenu(IModel<T> model) {
				return AbstractFacetedConsole.this.getMenu(model);
			}
			
			@Override
			protected Panel getTopPanel() {
				if (AbstractFacetedConsole.this.hasTopPanel())
					return AbstractFacetedConsole.this.getTopPanel();
				return new InvisiblePanel("top");
			}
			
			@Override
			protected List<ToolbarItem> getToolbarItems() {
				List<ToolbarItem> items = new ArrayList<ToolbarItem>();
				List<ToolbarItem> items_console = AbstractFacetedConsole.this.getToolbarItems(this);
				List<ToolbarItem> items_super = super.getToolbarItems();
				items.addAll(items_super);
				items_console.forEach(v -> { if (v.getJustify() == ToolbarItem.JUSTIFY_LEFT) items.add(v);});
				items_console.forEach(v -> { if (v.getJustify() == ToolbarItem.JUSTIFY_RIGHT) items.add(v);}); 
				items.add(AbstractFacetedConsole.this.getGridToolbarMenuItem());
				return items;
			}
			@Override
			public List<ToolbarItem> getSelectionToolbarItems() {
				List<ToolbarItem> items = super.getSelectionToolbarItems();
				return items;
			}
			@Override
			protected boolean hasExpander() {
				return AbstractFacetedConsole.this.hasExpander();
			}
			@Override
			public boolean isMyListsEnabled() {
				return AbstractFacetedConsole.this.isMyListsEnabled();
			}
			@Override
			protected List<LayoutPanel> getPanels() {
				List<LayoutPanel> panels = super.getPanels();
				return panels;
			}
			@Override
			protected boolean isSelectionEnabled() {
				 return AbstractFacetedConsole.this.isSelectionEnabled();
			}
			@Override
			protected boolean isMenuEnabled() {
				return AbstractFacetedConsole.this.isMenuEnabled();
			}
			@Override
			protected boolean isVisible(Facet facet) {
				return AbstractFacetedConsole.this.isVisible(facet);
			}
			@Override
			public Query getQuery() {
				return AbstractFacetedConsole.this.getQuery();
			}
			@Override
			public boolean isRememberQuery() {
				return AbstractFacetedConsole.this.isRememberQuery();
			}
			@Override
			protected void onUpdateQuery(AjaxRequestTarget target) {
					AbstractFacetedConsole.this.onUpdateQuery(target);
			}
			@Override
			protected String getIcon(IModel<T> model) {
				return AbstractFacetedConsole.this.getIcon(model);
			}
			@Override
			protected boolean hasIcon(IModel<T> model) {
				return AbstractFacetedConsole.this.hasIcon(model);
			}
		};
		br.setGridBrowserSwitch(isGridBrowser());
		//br.setTreeBrowserSwitch(isTreeBrowser());
		return br;
	}




	protected BaseBrowser<T> newGridBrowser() {
		
		AbstractFacetedBrowser<T> br = new AbstractFacetedBrowser<T>("browser", getName(), getQuery()) {
			@Override
			protected boolean isSavedQueriesEnabled() {
				return AbstractFacetedConsole.this.isSavedQueriesEnabled();
			}
			@Override
			protected boolean isFiltersEnabled() {
				return AbstractFacetedConsole.this.isFiltersEnabled();
				
			}
			@Override
			protected boolean isDefaultTopPanelVisible() {
				return AbstractFacetedConsole.this.isDefaultTopPanelVisible();
			}
			protected String getDefaultUserPreference(String key) {
				return AbstractFacetedConsole.this.getDefaultUserPreference(key);
			}
			@Override
			public List<NavigationOrder> getOrders() {
				return AbstractFacetedConsole.this.getOrders();
			}
			@Override
			public Searcher getSearcher() {
				return AbstractFacetedConsole.this.getSearcher();
			}
			@Override
			protected String getContextKey() {
				return AbstractFacetedConsole.this.getName() + super.getContextKey();
			}
			@Override
			protected IModel<T> getModel(T object) {
				return AbstractFacetedConsole.this.getModel(object);
			}
			@Override
			protected Panel getPanel(IModel<T> model) {
				return AbstractFacetedConsole.this.getPanel(model);
			}
			@Override
			protected Panel getPanel(IModel<T> model, List<String> snippets) {
				return AbstractFacetedConsole.this.getPanel(model, snippets);
			}
			@Override
			protected List<GridColumn<SearchResult, String>> getColumns() {
				return AbstractFacetedConsole.this.getColumns();
			}
			@Override
			protected Panel getMenu(IModel<T> model) {
				return AbstractFacetedConsole.this.getMenu(model);
			}
			@Override
			public DownloadMenuItemPanel<SavedQuery> getGridExportSavedQueryMenuItem(String id, IModel<SavedQuery> model) {
				return AbstractFacetedConsole.this.getGridExportSavedQueryMenuItem(id, model);
			}
			@Override
			protected Panel getTopPanel() {
				if (AbstractFacetedConsole.this.hasTopPanel())
					return AbstractFacetedConsole.this.getTopPanel();
				return new InvisiblePanel("top");
			}
			@Override
			protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
				return AbstractFacetedConsole.this.getRowContainerCss(rowmodel);
			}
			@Override
			protected List<ToolbarItem> getToolbarItems() {
				List<ToolbarItem> items = new ArrayList<ToolbarItem>();
				List<ToolbarItem> items_console = AbstractFacetedConsole.this.getToolbarItems(this);
				List<ToolbarItem> items_super = super.getToolbarItems();
				items.addAll(items_super);
				items_console.forEach(v -> { if (v.getJustify() == ToolbarItem.JUSTIFY_LEFT) items.add(v);});
				items_console.forEach(v -> { if (v.getJustify() == ToolbarItem.JUSTIFY_RIGHT) items.add(v);}); 
				items.add(AbstractFacetedConsole.this.getGridToolbarMenuItem());
				return items;
			}
			@Override
			public List<ToolbarItem> getSelectionToolbarItems() {
				List<ToolbarItem> items = super.getSelectionToolbarItems();
				List<ToolbarItem> items2 = new ArrayList<ToolbarItem>();
				items2.addAll(items);
				items2.addAll(AbstractFacetedConsole.this.getSelectionToolbarItems(this));
				return items2;
			}
			@Override
			protected boolean hasExpander() {
				return AbstractFacetedConsole.this.hasExpander();
			}
			@Override
			public boolean isMyListsEnabled() {
				return AbstractFacetedConsole.this.isMyListsEnabled();
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
								AbstractFacetedConsole.this.onFilters(target);
							}
						};
						return sq;
					}
				});

				add(new WicketEventListener<ApplySavedQueryEvent>() {
					@Override
					public void onEvent(ApplySavedQueryEvent event) {
						FiltersPanel panel = getBrowser().getPanel(FiltersPanel.class);
						if (panel != null) {
							panel.setParameters(event.getQuery().getParameters());
							getBrowser().getQuery().setParameters(event.getQuery().getParameters());
							getBrowser().refresh(event.getRequestTarget());
						}
					}
				});

				return panels;
			}
			@Override
			protected boolean isSelectionEnabled() {
				return AbstractFacetedConsole.this.isSelectionEnabled();
			}
			@Override
			protected boolean isMenuEnabled() {
				return AbstractFacetedConsole.this.isMenuEnabled();
			}
			@Override
			protected boolean isVisible(Facet facet) {
				return AbstractFacetedConsole.this.isVisible(facet);
			}
			@Override
			public Query getQuery() {
				return AbstractFacetedConsole.this.getQuery();
			}
			@Override
			public boolean isRememberQuery() {
				return AbstractFacetedConsole.this.isRememberQuery();
			}
			@Override
			protected void onUpdateQuery(AjaxRequestTarget target) {
				AbstractFacetedConsole.this.onUpdateQuery(target);
			}
		};
		
		br.setListBrowserSwitch(isListBrowser());
		//br.setTreeBrowserSwitch(isTreeBrowser());
		return br;
	}

	protected boolean isSavedQueriesEnabled() {
		return true;
	}

	@Override
	protected boolean isMyListsEnabled() {
		return false;
	}

	protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
		return super.getRowContainerCss(rowmodel);
	}

	protected Panel getTopPanel() {
		return new InvisiblePanel("top");
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
	
	protected boolean isSelectionEnabled() {
		return true;
	}

	protected boolean isMenuEnabled() {
		return true;
	}

	protected boolean isVisible(Facet facet) {
		return true;
	}

	protected List<MenuItemFactory<T>> getSelectionActionsMenu(BaseBrowser<T> browser) {
		return null;
	}

	protected IModel<String> getStringDateModel(OffsetDateTime dt) {
		return getStringDateModel(dt, true);
	}

	protected IModel<String> getStringDateModel(OffsetDateTime dt, boolean allowHTML) {
		if (dt==null)
			throw new IllegalArgumentException("OffsetDateTime is null");
		try {
			DateTimeService service = ServiceLocator.getService(DateTimeService.class);
			ZoneId user_zoneid = ZoneId.of(getSessionUser().getTimeZone());
			if (user_zoneid==null)
				user_zoneid=ZoneId.systemDefault();
			ZonedDateTime zd = ZonedDateTime.ofInstant(dt.toInstant(), user_zoneid);
			int format = allowHTML ? DateTimeService.DATE_COLlOQUIAL_AGO : DateTimeService.DATE_COLlOQUIAL;
			return new Model<String>(service.timeElapsed(zd, user_zoneid, getSessionUser().getLocale(), format, "ago"));
		} 
		catch (Exception e) {
			logger.error(e);
			return new Model<String>(e.getClass()+" " + e.getMessage());
		}
	}
	
	protected IModel<String> getSnippet(String text) {
	
		if (text==null || text.isEmpty())
			return new Model<String>();
		
		String s = null;
		if (text.length()>MAX)
			s = text.substring(0, MAX)+"...";
		else
			s=text;
		Safelist list = Safelist.basic();
		list.removeTags("p");
		
		String cleaned = Jsoup.clean(s, list);
		String t1 = cleaned;
		return  new Model<String>(t1);
	}

	protected List<NavigationOrder> getOrders() {
		List<NavigationOrder> orders = new ArrayList<NavigationOrder>();
		orders.add(new NavigationOrder(getLabel("modified-desc"), "modified", false));
		orders.add(new NavigationOrder(getLabel("modified-asc"), "modified", true));
		orders.add(new NavigationOrder(getLabel("title"), "title_sort", true));
		orders.add(new NavigationOrder(getLabel("title-desc"), "title_sort", false));
		orders.add(new NavigationOrder(getLabel("relevance"), "relevance", false));
		
		return orders;
	}
	
	protected TreeProvider<TreeNode<T>> getTreeProvider() {
		return null;
	}


	protected boolean hasIcon(IModel<T> model) {
		return (getIcon( model) != null);
	}
	
	
	

	
	
}
