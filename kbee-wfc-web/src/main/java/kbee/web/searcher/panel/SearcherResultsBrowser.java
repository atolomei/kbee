package kbee.web.searcher.panel;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.query.SavedQuery;
import com.novamens.content.userlist.UserList;
 
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Filter;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.query.ValueFilter;
import com.novamens.kbee.wicket.markup.html.console.browser.AbstractFacetedBrowser;
import com.novamens.kbee.wicket.markup.html.console.browser.AbstractListBrowser;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.layout.AbstractLayout;
import com.novamens.kbee.wicket.markup.html.console.layout.LayoutPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.ApplySavedQueryEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.DownloadMenuItemPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.FiltersPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListsApplyUserListEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListsUserListItemUpdateObjectEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.ParametersPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.SaveQueryModal;
import com.novamens.kbee.wicket.markup.html.event.FilterSelectorClearAllEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.repeater.util.NavigationOrder;
import com.novamens.wicket.markup.html.repeater.util.Searcher;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.console.BaseBrowser;
import kbee.web.panel.ListContentItemMainPanel;

@SuppressWarnings("serial")
public class SearcherResultsBrowser extends SearcherBrowser {
	private static final long serialVersionUID = 1L;
	
	public SearcherResultsBrowser(String id, IModel<Site> siteModel) {
		super(id, siteModel);
		addListeners();
	}

	public SearcherResultsBrowser(String id, Query query, IModel<Site> siteModel) {
		super(id, query, siteModel);
		addListeners();
	}
	 
	public boolean isIconSupported() {
		return true;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		add(newListBrowser());
		add(new InvisiblePanel("audittrail-modal"));
		add(new InvisiblePanel("confirmation-modal"));
	}
	
	protected WebMarkupContainer newListBrowser() {
		
		return new AbstractListBrowser<Content>("browser", getConsoleKey(), getQuery()) {
			@Override 
			public boolean isIconSupported() {
				return SearcherResultsBrowser.this.isIconSupported();
			}
			@Override 
			public boolean isGridBrowserSwitch() {
				return SearcherResultsBrowser.this.isGridBrowserSwitch();
			}
			@Override
			protected Modal newSaveQueryModal() {
				return new SaveQueryModal("save-filters", getConsoleKey(), getSiteModel());
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
				return SearcherResultsBrowser.this.getOrders();
			}
			@Override
			public Searcher getSearcher() {
				return SearcherResultsBrowser.this.getSearcher();
			}
			@Override
			protected String getContextKey() {
				return getConsoleKey();
			}
			@Override
			protected IModel<Content> getModel(Content content) {
				return new ObjectModel<Content>(content);
			}
			@Override
			protected Panel getPanel(IModel<Content> model) {
				return SearcherResultsBrowser.this.getHitPanel(model, 0, false);
			}
			@Override
			protected Panel getPanel(IModel<Content> model, List<String> snippets) {
				return SearcherResultsBrowser.this.getHitPanel(model, 0, false);
			}
			protected Panel getPanel(IModel<Content> model, int index, boolean expanded) {
				return SearcherResultsBrowser.this.getHitPanel(model, index, expanded);
			}
			@Override
			protected Panel getMenu(IModel<Content> model) {
				return SearcherResultsBrowser.this.getMenu(model);
			}
			@Override
			protected Panel getTopPanel() {
				if (SearcherResultsBrowser.this.hasTopPanel())
					return SearcherResultsBrowser.this.getTopPanel();
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
				return SearcherResultsBrowser.this.isVisible(facet);
			}
			@Override
			public Query getQuery() {
				return SearcherResultsBrowser.this.getQuery();
			}

			@Override
			protected Panel getItemListPanel(IModel<Content> model, int index) {
				return SearcherResultsBrowser.this.getItemListPanel(model, index);
			}
			@Override
			public boolean isRememberQuery() {
				return SearcherResultsBrowser.this.isRememberQuery();
			}
			@Override
			protected void onUpdateQuery(AjaxRequestTarget target) {
				SearcherResultsBrowser.this.onUpdateQuery(target);
			}

			@Override
			protected String getIcon(IModel<Content> model) {
				return SearcherResultsBrowser.this. getIcon(model);
			}

			@Override
			protected boolean hasIcon(IModel<Content> model) {
				return SearcherResultsBrowser.this. hasIcon(model);
			}
		};
	}

	/**
 
	 * @return
	 */
	protected BaseBrowser<Content> newGridBrowser() {
		
		AbstractFacetedBrowser<Content> br = new AbstractFacetedBrowser<Content>("browser", getName(), getQuery()) {
			@Override
			protected boolean isSavedQueriesEnabled() {
				return SearcherResultsBrowser.this.isSavedQueriesEnabled();
			}
			@Override
			protected boolean isFiltersEnabled() {
				return SearcherResultsBrowser.this.isFiltersEnabled();
				
			}
			@Override
			protected boolean isDefaultTopPanelVisible() {
				return SearcherResultsBrowser.this.isDefaultTopPanelVisible();
			}
			protected String getDefaultUserPreference(String key) {
				return SearcherResultsBrowser.this.getDefaultUserPreference(key);
			}
			@Override
			public List<NavigationOrder> getOrders() {
				return SearcherResultsBrowser.this.getOrders();
			}
			@Override
			public Searcher getSearcher() {
				return SearcherResultsBrowser.this.getSearcher();
			}
			@Override
			protected String getContextKey() {
				return SearcherResultsBrowser.this.getName() + super.getContextKey();
			}
			@Override
			protected IModel<Content> getModel(Content object) {
				return new ObjectModel<Content>(object);
				//return SearcherResultsBrowser.this.getModel(object);
			}
			@Override
			protected Panel getPanel(IModel<Content> model) {
				return null;
				//return SearcherResultsBrowser.this.getPanel(model);
			}
			@Override
			protected Panel getPanel(IModel<Content> model, List<String> snippets) {
				return null;
			}
			@Override
			protected List<GridColumn<SearchResult, String>> getColumns() {
				return null;
			}
			@Override
			protected Panel getMenu(IModel<Content> model) {
				return SearcherResultsBrowser.this.getMenu(model);
			}
			@Override
			public DownloadMenuItemPanel<SavedQuery> getGridExportSavedQueryMenuItem(String id, IModel<SavedQuery> model) {
				return null;
			}
			@Override
			protected Panel getTopPanel() {
				if (SearcherResultsBrowser.this.hasTopPanel())
					return SearcherResultsBrowser.this.getTopPanel();
				return new InvisiblePanel("top");
			}
			@Override
			protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
				return SearcherResultsBrowser.this.getRowContainerCss(rowmodel);
			}
			@Override
			protected List<ToolbarItem> getToolbarItems() {
				List<ToolbarItem> items = new ArrayList<ToolbarItem>();
				return items;
			}
			@Override
			public List<ToolbarItem> getSelectionToolbarItems() {
				List<ToolbarItem> items = super.getSelectionToolbarItems();
				List<ToolbarItem> items2 = new ArrayList<ToolbarItem>();
				items2.addAll(items);
				return items2;
			}
			@Override
			protected boolean hasExpander() {
				return SearcherResultsBrowser.this.hasExpander();
			}
			@Override
			public boolean isMyListsEnabled() {
				return SearcherResultsBrowser.this.isMyListsEnabled();
			}
			@Override
			protected List<LayoutPanel> getPanels() {

				List<LayoutPanel> panels = super.getPanels();

				panels.add(new LayoutPanel("side", AbstractLayout.SIDE_DISPOSITION) {
					protected WebMarkupContainer getPanel(String id) {
						return null;
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
				return SearcherResultsBrowser.this.isSelectionEnabled();
			}
			@Override
			protected boolean isMenuEnabled() {
				return SearcherResultsBrowser.this.isMenuEnabled();
			}
			@Override
			protected boolean isVisible(Facet facet) {
				return SearcherResultsBrowser.this.isVisible(facet);
			}
			@Override
			public Query getQuery() {
				return SearcherResultsBrowser.this.getQuery();
			}
			@Override
			public boolean isRememberQuery() {
				return SearcherResultsBrowser.this.isRememberQuery();
			}
			@Override
			protected void onUpdateQuery(AjaxRequestTarget target) {
				SearcherResultsBrowser.this.onUpdateQuery(target);
			}
		};
		
		br.setListBrowserSwitch(true);
		return br;
	}


	protected Panel getItemListPanel(IModel<Content> model, int index) {
		return new ListContentItemMainPanel("item", model, getSiteModel(), index ,false) {
			@Override
			protected void onClick() {
				fireScanAll(new ClickEvent<Content>(null, getModel(), getIndex()));
			}
			@Override
			protected WebMarkupContainer getItemTags(IModel<Content> modelObject) {
				return SearcherResultsBrowser.this.getItemTags(modelObject);
			}
			@Override
			protected WebMarkupContainer getMoreInfoPanel(IModel<Content> modelObject) {
				return SearcherResultsBrowser.this.getMoreInfoPanel(modelObject);
			}
			@Override
			protected IModel<String> getItemLabel(IModel<Content> modelObject) {
				return SearcherResultsBrowser.this.getItemLabel(modelObject);
			}
			@Override
			protected IModel<String> getItemLabelMeta(IModel<Content> modelObject) {
				return SearcherResultsBrowser.this.getItemLabelMeta(modelObject);
			}
		};
	}


	/**
	 * 
	 * 
	 * @param model	
	 * @param index
	 * @param expanded
	 * @return
	 */
	protected Panel getHitPanel(IModel<Content> model, int index, boolean expanded) {
		
		Query query = getSearcher().getQuery();
		
		Object textfilter = query.getParameters().get("text");
		String textquery = textfilter instanceof Filter ? (String)((Filter)textfilter).getValue() : (textfilter!=null ? textfilter.toString() : null);
				
		SearcherContentViewPanel<Content> panel = new SearcherContentViewPanel<Content>("editor", model, getSiteModel(), getSearcher(), textquery, index, expanded);
		panel.setContext(getConsoleKey());
		
		return panel;
	}

	
	protected Panel getMenu(IModel<Content> model) {
		
		Panel menu = getContentMenu(model);
		return menu;
	}
	

	
	protected List<NavigationOrder> getOrders() {
		List<NavigationOrder> orders = new ArrayList<NavigationOrder>();
		orders.add(new NavigationOrder(getLabel("modified"), "modified", false));
		orders.add(new NavigationOrder(getLabel("title"), "title_sort", true));
		orders.add(new NavigationOrder(getLabel("relevance"), "relevance", false));
		
		return orders;
	}

	protected void addListeners() {
		
		
		add(new WicketEventListener<FilterSelectorClearAllEvent>() {
			@Override
			public void onEvent(FilterSelectorClearAllEvent event) {
				 
				FeedbackHelper.showInfoToast( "FilterSelectorClearAllEvent" ,   "FilterSelectorClearAllEvent" );
				getBrowser().refresh(event.getRequestTarget());
				 
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof FilterSelectorClearAllEvent;
			}
		});

		
		 
		 
		 
		
		add(new WicketEventListener<MyListsApplyUserListEvent>() {
			@Override
			public void onEvent(MyListsApplyUserListEvent event) {
				IModel<UserList> list = event.getUserList();
				ParametersPanel parameterspanel = getBrowser().getPanel(ParametersPanel.class);
				ValueFilter filter = new ValueFilter("userlist", String.valueOf(list.getObject().getId()), list.getObject().getDisplayName());
				parameterspanel.setParameter("userlist", filter);
				getQuery().getParameters().put("userlist", filter);
				getBrowser().refresh(event.getRequestTarget());
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
				FeedbackHelper.showInfoToast(event.getListModel().getObject().getName(),  event.getModel().getObject().getDisplayName());
				getBrowser().refresh(event.getRequestTarget());
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof MyListsUserListItemUpdateObjectEvent;
			}
		});
	}
	
	public boolean isGridBrowserSwitch() {
		return false;
	}
	
	protected boolean hasIcon(IModel<Content> model) {
		return true;
	}

	protected String getIcon(IModel<Content> model) {
		
		
		if ( model.getObject().isLocked()) {
			return SearcherBrowser.LOCK_ICON_CSS;
		}
		
		if (isWriteable(model))  { 
			return SearcherBrowser.EDITABLE_ICON_CSS;
		}
		return null;
		 
		/**
		StringBuilder iconStr = new StringBuilder();
		try {
			if ( model.getObject().isLocked()) {
				StringBuilder str = new StringBuilder();
				try {
					Long oid = model.getObject().getOId();
					Content content=getContentDao().findWorkspaceCopyContentByOId(oid);
					if (content!=null) {
						String name = getContentDao().findUserProfileByUserId(content.getWorkspace()).getPersonFirstLastName();
						str.append(name);
					}
				}
				catch (Exception e) {
					return getClass().getSimpleName();
				}
				iconStr.append(" <i class=\"ml-2 small fa-duotone fa-solid fa-lock\" title=\"" + str.toString() +"\"></i>" );
			}
			else {	
				if (isWriteable(model))  { 
					String is=SearcherBrowser.EDITABLE_ICON;
					iconStr.append(is);
				}
			}
		} 
		catch (Exception e) {
			iconStr.append(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
		
		return iconStr.toString();
		**/
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
	@SuppressWarnings("unchecked")
	public BaseBrowser<Content> getBrowser() {
		return (BaseBrowser<Content>) get("browser");
	}
}
