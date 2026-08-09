package kbee.web.console;



import java.math.RoundingMode;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.wicket.markup.html.console.browser.GridMenu;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.Page;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.query.SavedQuery;
import com.novamens.content.service.domain.DomainPreferencesService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.ObjectState;
import com.novamens.event.Event;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Filter;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.query.KbeeSavedQuery;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.browser.RefreshClickEvent;

import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.browser.TopPanelEvent;
import com.novamens.kbee.wicket.markup.html.console.event.QueryChangeEvent;
import com.novamens.kbee.wicket.markup.html.console.event.SwitchPanelsEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.panel.DownloadMenuItemPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.FiltersPanel;
import com.novamens.kbee.wicket.markup.html.event.CloseConsoleTopPanelEvent;
import com.novamens.kbee.wicket.markup.html.event.EmailSentEvent;
import com.novamens.kbee.wicket.markup.html.event.FilterSelectorClearAllEvent;
import com.novamens.kbee.wicket.markup.html.event.FilterSelectorEvent;
import com.novamens.kbee.wicket.markup.html.event.GeneralAjaxWicketEvent;
import com.novamens.kbee.wicket.markup.html.event.GeneralWicketEvent;
import com.novamens.kbee.wicket.markup.html.event.InfoEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.kbee.wicket.util.InvisiblePanel;

import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.security.Identifiable;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.EmptyDialog;

import com.novamens.wicket.markup.html.modal.ErrorDialog;
import com.novamens.wicket.markup.html.modal.ExecutorDialog;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.repeater.util.NavigationOrder;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.DummyBlockPanel;

import kbee.web.alert.BillboardPanel;
import kbee.web.console.tools.*;

import kbee.web.event.wicket.ErrorEvent;

/**
 *  <p>grid and list consoles</p>
 *
 * @param <T>
 */
@SuppressWarnings("serial")
public abstract class AbstractConsole<T> extends Console<T> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AbstractConsole.class.getName());

	protected final boolean root		   = ServiceLocator.getService(SecurityService.class).isRoot();
	protected final boolean role_admin     = root || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	private Boolean is_domain_kbee = null;
	private IModel<User> model_session_user = null;
	private List<ToolbarItem> items = new ArrayList<>();
	private NumberFormat nf, integer_nf;
	private boolean default_top_panel_visible = false;
	private boolean is_infopanel=false;
	private boolean isRememberQuery = false;
	
	// default Grid YES, List NO
	//
	private boolean is_grid_browser = true;
	private boolean is_list_browser = false;
	private boolean is_tree_browser = false;
	private String browser_type= "grid";

	
	/**
	 * @param id
	 * @param name
	 * @param query
	 */
	public AbstractConsole(String id, String name, Query query) {
		super(id, name, query);
		addModals();
	}

	public AbstractConsole(String name, Query query) {
		super(name, query);
		addModals();
	}
	
	public long getIndex(T object) {
		return getBrowser().getIndex(object);
	}

	protected boolean isFiltersEnabled() {
		return false;
	}
	
	public boolean hasBillboardPanel() {
		return ServiceLocator.getService(com.novamens.content.notification.NotificationService.class).getTotalBillboardNotifications(getSessionUser())>0;
	}

	public boolean hasInfoPanel() {
		return is_infopanel;
	}

	public void setConsoleTopPanel(Panel panel) {
		
		if (panel==null)
			throw new IllegalArgumentException("error-panel can not be null");
		
		if (!panel.getId().contentEquals("error-panel"))
			throw new IllegalArgumentException("Error Panel must have id error-panel");
		
		is_infopanel = panel.isVisible();
		
		WebMarkupContainer c = (WebMarkupContainer) get("error-panel-container");
		c.addOrReplace(panel);
	}

	@SuppressWarnings("unchecked")
	public BaseBrowser<T> getBrowser() {
		return (BaseBrowser<T>) get("browser");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		try {
			if (this.model_session_user != null)
				this.model_session_user.detach();
			items = null;
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}

	public void refresh(AjaxRequestTarget target) {
		onBeforeRefresh(target);
		getSearcher().refresh();
		getBrowser().refresh(target);
	}

	public String getBrowserType() {return this.browser_type;}
	
	public boolean isListBrowser() {return is_list_browser;}
	public void setListBrowser(boolean b) {is_list_browser=b;}
					
	public boolean isGridBrowser() {return is_grid_browser;}
	public void setGridBrowser(boolean b) {is_grid_browser=b;}
	
	public boolean isTreeBrowser() {return is_tree_browser;}
	public void setTreeBrowser(boolean b) {is_tree_browser=b;}

	public boolean isRememberQuery() {return this.isRememberQuery;}
	
	public void setBrowserType(String s) {this.browser_type=s;}
	protected void setRememberQuery(boolean b) {isRememberQuery=b;}

	protected void onBeforeRefresh(AjaxRequestTarget target) {
	}

	public void resetSelection() {
		getBrowser().resetSelection();
	}
	
	@SuppressWarnings("unchecked")
	public void fireScanAll(Event event) {
		
		logger.debug("Fire Scan All -> " + event.getClass().getSimpleName());
		
		for (WicketEventListener<Event> listener : getPage().getBehaviors(WicketEventListener.class)) {
			if (listener.handle(event)) {
				listener.onEvent(event);
			}
		}
		
		fire(event, getPage().iterator(), false);
	}

	protected void onUpdateQuery(AjaxRequestTarget target) {
		if (isRememberQuery()) {
			saveQuery();
		}
	}

	protected void saveQuery() {
		KbeeSavedQuery sq;
		sq = new KbeeSavedQuery (getSessionUser(), "rememberQuery", getName(), null, getQuery().getParameters());
		String sta=sq.getStatement();
		setUserPreference("lastQuery", sta);
		sq=null;
	}

	protected void loadLastQuery() {
		String lq=getUserPreference("lastQuery");
		if (lq==null)
			return;
		KbeeSavedQuery sq;
		sq = new KbeeSavedQuery (getSessionUser(), getName());
		sq.setStatement(lq);
		getQuery().setParameters(sq.getParameters());
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (get("browser") == null) {
			
			BaseBrowser<T> browser = null;
			
			String rq = getSessionUser().getService(PreferencesService.class).getValue(getName(), "rememberQuery",  "no");
			setRememberQuery(rq.equals("yes"));
			
			// logger.debug("rememberQuery -> " + (isRememberQuery() ? "yes":"no"));
			
			String bt = getSessionUser().getService(PreferencesService.class).getValue(getName() + "-" + "console", "browserType",  "grid");
			setBrowserType(bt);
			
			if (getQuery() == null) {
				setQuery(newQuery());
				if (isRememberQuery())
					loadLastQuery();
			}
			else {
				if (isRememberQuery())
					saveQuery();
			}
			
			if (getBrowserType()!=null && getBrowserType().equals("list"))
				browser = newListBrowser();
			else
				if (getBrowserType()!=null && (getBrowserType().equals("tree") || getBrowserType().equals("treelist")))
					browser = newTreeBrowser();
				else
					browser = newGridBrowser();
			if (browser==null)
				browser = newGridBrowser();
			add(browser);
			
			if (this.hasBillboardPanel())		
				setBillboardPanel(new BillboardPanel());
			else								
				add(new InvisiblePanel("billboard"));

			if (this.hasInfoPanel()) 			
				setConsoleTopPanel(new DummyBlockPanel("error-panel"));
			else								
			{
				WebMarkupContainer c =new WebMarkupContainer("error-panel-container");
				c.add(new InvisiblePanel("error-panel"));
				c.setOutputMarkupId(true);
				c.setVisible(true);
				add(c);
			}
			
			add(new InvisiblePanel("advancedsearch"));
			
		}
	}

	public List<Panel> getRightPanels() {
		return null;
	}

	/**
	 * Useb by Modal Windows in the title, title in the html page header, etc.
	 */
	@Override
	public IModel<String> getDisplayName() {
		return getLabel("console.name");
	}
	
	public abstract List<GridColumn<SearchResult, String>> getColumns();
	
	public abstract Page getConsolePage(Query query, long index);
	
	public abstract Query newQuery();
	
	public String getDownloadFileName(){
		String dname = ( getDomain()!=null? (getDomain().getName().toLowerCase().replaceAll("[ |\\t|\\s|(|)]", "-") +"-" ):"");
		return  dname + this.getDisplayName().getObject().replaceAll("[ |\\t|\\s|(|)]", "-").toLowerCase()+"-"+(new SimpleDateFormat("YYYY-MM-dd").format(new Date()));
	}
	
	protected abstract Panel getMenu(IModel<T> model);
	
	protected abstract BaseBrowser<T> newGridBrowser();

	protected IModel<String> getLabel(String key) {
		return new StringResourceModel(key, AbstractConsole.this, null);
	}

	protected IModel<String> getConsoleLabel(String key) {
		return new StringResourceModel(key, AbstractConsole.this, null);
	}

	protected IModel<String> getConsoleLabel(String key, String... parameter) {
		StringResourceModel model = new StringResourceModel(key, this);
		model.setParameters((Object[]) parameter);
		return model;
	}
	
	@Override
	protected String getDefaultUserPreference(String key) {
		Domain domain = getDomain();
		if (domain==null || key==null)
			return null;
		DomainPreferencesService service = domain.getService(DomainPreferencesService.class);
		if (service!=null) 
			return service.getValue(getName(), key);
		return null;
	}
	
	@Override
	protected void addListeners() {
		super.addListeners();

		add(new WicketEventListener<EmailSentEvent<T>>() {
			@Override
			public void onEvent(EmailSentEvent<T> event) {
				if ((event.getModel() != null) && (event.getModel().getObject() instanceof Identifiable)) {
					FeedbackHelper.showInfoToast(event.getClass().getSimpleName(), ((Identifiable) event.getModel().getObject()).getDisplayName());
				}
				else {
					FeedbackHelper.showInfoToast(event.getClass().getSimpleName());
								
				}
				AbstractConsole.this.refresh(event.getRequestTarget());
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				if (event instanceof EmailSentEvent)
					return true;
				return false;
			}
		});

		
		
		add(new WicketEventListener<GeneralWicketEvent>() {
			@Override
			public void onEvent(GeneralWicketEvent event) {
					setRememberQuery( event.getMap().get("rememberQuery").equals("yes"));
					saveQuery();
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				if (event instanceof GeneralWicketEvent)
					((GeneralWicketEvent) event).getName().equals("rememberQuery");
				return false;
			}
		});
		
		add(new WicketEventListener<GeneralAjaxWicketEvent>() {
			@Override
			public boolean handle(com.novamens.event.Event event) {
				if (event instanceof GeneralAjaxWicketEvent) {
					String browser = ((GeneralAjaxWicketEvent) event).getName();
					return browser.equals("grid-browser") || 
						browser.equals("list-browser") || 
						browser.equals("tree-browser") ||
						browser.equals("treelist-browser");
				}
				return false;
			}
			@Override
			public void onEvent(GeneralAjaxWicketEvent event) {
				AbstractConsole.this.handle(event);
			}
		});

		add(new WicketEventListener< CloseConsoleTopPanelEvent>() {
			@Override
			public void onEvent( CloseConsoleTopPanelEvent event) {
				AbstractConsole.this.setConsoleTopPanel(new InvisiblePanel("error-panel"));
				AbstractConsole.this.refreshInfoArea(event.getRequestTarget());
			}
		});
		
		add(new WicketEventListener<ErrorEvent<?>>() {
			@Override
			public void onEvent(ErrorEvent<?> event) {
				AbstractConsole.this.setConsoleTopPanel(new ConsoleErrorPanel("error-panel", event.getThrowable()));
				AbstractConsole.this.refreshInfoArea(event.getRequestTarget());
			}
		});
		
		add(new WicketEventListener<InfoEvent>() {
			@Override
			public void onEvent(InfoEvent event) {
				IModel<String> title= event.getTitle();
				IModel<String> text= event.getText();
				String css = event.getCss();
				AbstractConsole.this.setConsoleTopPanel(new ConsoleInfoPanel("error-panel", title, text, css));
				AbstractConsole.this.refreshInfoArea(event.getRequestTarget());
			}
		});

		add(new WicketEventListener<TopPanelEvent>() {
			@Override
			public void onEvent(TopPanelEvent event) {
				AbstractConsole.this.refresh(event.getRequestTarget());
				AbstractConsole.this.getBrowser().togglePanel(AdvancedSearchSelectorEditor.class);
				event.getRequestTarget().add(getBrowser());
			}
		});
		
		add(new WicketEventListener<QueryChangeEvent>() {
			@Override
			public void onEvent(QueryChangeEvent event) {
				updateUserPreference(event.getQuery());
				
			}
		});

		add(new WicketEventListener<RefreshClickEvent>() {
			@Override
			public void onEvent(RefreshClickEvent event) {
				AbstractConsole.this.refresh(event.getRequestTarget());
			}
		});
		
		add(new WicketEventListener<FilterSelectorClearAllEvent>() {
			@Override
			public void onEvent(FilterSelectorClearAllEvent event) {
				logger.debug(event.getClass().getName());
				Query query  = newQuery();
				setResponsePage(AbstractConsole.this.getConsolePage(query, -1));
				
			}
		});

		/**
		 * Advanced Search
		 */
		add(new WicketEventListener<FilterSelectorEvent>() {
			@Override
			public void onEvent(FilterSelectorEvent event) {
				logger.debug(event.getClass().getName());
				try {
					setFilters(getQuery(), event.getFilters());
					FiltersPanel panel = getBrowser().getPanel(FiltersPanel.class);
					if (panel != null)
						panel.setParameters(getQuery().getParameters());
					AbstractConsole.this.refresh(event.getRequestTarget());
				} 
				catch (Exception e) {
					logger.error(e);
				}
			}
			private void setFilters(Query query, Map<String, Object> filters) {
				List<String> parameters = new ArrayList<String>();
				parameters.addAll(query.getParameters().keySet());
				for (String parameter : parameters) {
					if (query.getParameters().get(parameter) instanceof Filter) {
						synchronized (query) {
							query.getParameters().remove(parameter);
						}
					}
				}
				for (String filter: filters.keySet()) {
					query.getParameters().put(filter, filters.get(filter));
				}
				logger.debug("aca guardo los parametros de la Query ?");
			}
		});
	}
	
	protected void handle(GeneralAjaxWicketEvent event) {
		try {
			if (event.getName().equals("grid-browser")) {
				//if ( ((BaseBrowser<T>) get("browser")).getBrowserType().equals("grid"))
				//	return;
				BaseBrowser<T> browser = null;
				setBrowserType("grid");
				getSessionUser().getService(PreferencesService.class).setValue(getName() + "-" + "console", "browserType", "grid");
				browser = newGridBrowser();
				addOrReplace(browser);
				AbstractConsole.this.refresh(event.getRequestTarget());
			}
			else if (event.getName().equals("list-browser")) {
				//if ( ((BaseBrowser<T>) get("browser")).getBrowserType().equals("list"))
				//	return;
				BaseBrowser<T> browser = null;
				setBrowserType("list");
				getSessionUser().getService(PreferencesService.class).setValue(getName() + "-" + "console", "browserType", "list");
				browser = newListBrowser();
				addOrReplace(browser);
				AbstractConsole.this.refresh(event.getRequestTarget());
			}
			else if (event.getName().equals("tree-browser")) {
				//if ( ((BaseBrowser<T>) get("browser")).getBrowserType().equals("tree"))
				//	return;
				setBrowserType("tree");
				getSessionUser().getService(PreferencesService.class).setValue(getName() + "-" + "console", "browserType", "tree");
				BaseBrowser<T> browser = newTreeBrowser();
				addOrReplace(browser);
				AbstractConsole.this.refresh(event.getRequestTarget());
			}
			else if (event.getName().equals("treelist-browser")) {
				//if ( ((BaseBrowser<T>) get("browser")).getBrowserType().equals("treelist"))
				//	return;
				setBrowserType("treelist");
				getSessionUser().getService(PreferencesService.class).setValue(getName() + "-" + "console", "browserType", "treelist");
				BaseBrowser<T> browser = newTreeBrowser();
				addOrReplace(browser);
				AbstractConsole.this.refresh(event.getRequestTarget());
			}
		} 
		catch (Exception e) {
			logger.error(e);
			fire (new ErrorEvent<>(event.getRequestTarget(), e));
		}
	}

	protected void addModals() {
		add(new EmptyDialog("audit-trail-modal"));
		add(new EmptyDialog("send-email-modal"));
		add(new EmptyDialog("labels-modal"));
		add(new ErrorDialog("error-dialog"));
		add(new ConfirmationDialog("confirmation-dialog"));
		add(new ExecutorDialog("executor-dialog"));
		add(new InfoDialog("information-modal"));
	}

	protected Dialog getInformationModal() {
		return (Dialog) get("information-modal");
	}
	
	protected Modal getAuditTrailModal() {
		return (Modal) get("audit-trail-modal");
	}

	protected Modal getSendByEmailModal() {
		return (Modal) get("send-email-modal");
	}

	protected ErrorDialog getErrorDialog() {
		return (ErrorDialog) get("error-dialog");
	}

	protected Modal getLabelsModal() {
		return (Modal) get("labels-modal");
	}

	protected ConfirmationDialog getConfirmationDialog() {
		return (ConfirmationDialog) get("confirmation-dialog");
	}

	protected ExecutorDialog getExecutorDialog() {
		return (ExecutorDialog) get("executor-dialog");
	}
	
	protected BaseBrowser<T> newListBrowser() {
		return null;
	}
	
	protected BaseBrowser<T> newTreeBrowser() {
		return null;
	}

	/**
	 * <p>
	 * This Method must be overriden by the {@code AbstractConsole} when {@code T}
	 * is not a Hibernate Object
	 * </p>
	 * 
	 * @see {@link CommandsConsoleDELETE} for example.
	 * 
	 * @param object
	 * @return
	 */
	protected IModel<T> getModel(T object) {
		return new ObjectModel<T>(object, true);
	}

	/**
	 * @param snippets
	 *            Expanded Hit Panel in Grid
	 */
	protected Panel getPanel(IModel<T> model, List<String> snippets) {
		return null;
	}
	
	/**
	 * @param snippets
	 *            Expanded Hit Panel in Grid
	 */
	protected Panel getPanel(IModel<T> model) {
		return null;
	}

	protected Panel getItemListPanel(IModel<T> model, int index) {
		if (model==null || model.getObject()==null)
			return null;
		if (model.getObject() instanceof Identifiable)
			return new  kbee.web.dashboard.LabelPanel("item", new Label("label", ((Identifiable) model.getObject()).getDisplayName()));
		logger.error("No displayName available -> " + model.getObject().getClass().getName() );
		return new  kbee.web.dashboard.LabelPanel("item", new Label("label", model.getObject().toString() ));
	}
	
	/**
	 * Row Expander enabled
	 */
	protected boolean hasExpander() {
		return false;
	}

	protected boolean isSelectionEnabled() {
		return true;
	}

	protected List<NavigationOrder> getOrders() {
		return new ArrayList<NavigationOrder>();
	}

	protected List<ToolbarItem> getToolbarItems(BaseBrowser<T> browser) {
		if (items==null)
			items = new ArrayList<>();
		return items;
	}
	
	protected DownloadMenuItemPanel<SavedQuery> getGridExportSavedQueryMenuItem(String id, IModel<SavedQuery> model) {
		 GridExportMenuItem<SavedQuery> m= new GridExportMenuItem<SavedQuery>(id, getLabel("tools.grid.export.xls").getObject(), model) {
			 @Override
			public AbstractConsole<T> getConsole() {
				return AbstractConsole.this;
			}
			@Override
			public GridExport getGridExport() {
				return new GridExportQueryExcel(AbstractConsole.this.getCustomExcelTemplate());
			}
			@Override
			public InfoDialog getInfoDialog() {
				return (InfoDialog) AbstractConsole.this.getInformationModal();
			}
		};
		 
		return m;
	}
	
	protected GridMenu getGridToolbarMenuItem() {
		
		GridMenu gridMenu = new GridMenu(this.getBrowser());

		gridMenu.addItem(itemId ->
			new GridExportMenuItem<Void>(itemId, getLabel("tools.grid.export.xls").getObject()){
				@Override
				public AbstractConsole<T> getConsole() {
					return AbstractConsole.this;
				}
				@Override
				public GridExport getGridExport() {
					return new GridExportQueryExcel(AbstractConsole.this.getCustomExcelTemplate());
				}
				@Override
				public InfoDialog getInfoDialog() {
					return (InfoDialog) AbstractConsole.this.getInformationModal();
				}
			}
		);
		
		gridMenu.addItem(itemId ->
			new GridExportMenuItem<Void>(itemId, getLabel("tools.grid.export.csv").getObject()) {
				@Override
				public AbstractConsole<T> getConsole() {
					return AbstractConsole.this;
				}
				@Override
				public GridExport getGridExport() {
					return new GridExportQueryCSV();
				}
				@Override
				public InfoDialog getInfoDialog() {
					return (InfoDialog) AbstractConsole.this.getInformationModal();
				}
			}
		);
		
		
		gridMenu.addItem(itemId ->
		 new SeparatorMenuItemPanelV5<Void>(itemId) {
			private static final long serialVersionUID = 1L;
			@Override
			public String getCssClass() {
				return "divider";
			}
			@Override
			public boolean isVisible() {
				return true;
			}
		});
		
		// --
		gridMenu.addItem(itemId ->
		new AjaxMenuItemPanelV5<Void>(itemId) {
			@Override
			public void onClick(AjaxRequestTarget target) {
	            fireScanAll(new SwitchPanelsEvent(target));
			}	
			@Override
			public String getLabel() {	
				return getLabelString("switch-sides");
			}
		});
		
		
		
		return gridMenu;
	}

	protected List<ToolbarItem> getSelectionToolbarItems(BaseBrowser<T> browser) {
		return new ArrayList<ToolbarItem>();
	}

	protected Query setUserPreference(Query query) {
		String order = this.getUserPreference("sort");
		if (order != null)
			query.getParameters().put("sort", order);
		String ascending = this.getUserPreference("ascending");
		if (ascending != null)
			query.getParameters().put("ascending", ascending);
		return query;
	}

	protected void updateUserPreference(Query query) {
		String order = (String) query.getParameters().get("sort");
		setUserPreference("sort", order);
		String ascending = (String) query.getParameters().get("ascending");
		setUserPreference("ascending", ascending);
		if (isRememberQuery())
			saveQuery();
	}

	protected String getUserPreference(String key, String default_value) {
		String s = getUserPreference(key);
		if (s != null)
			return s;
		return default_value;
	}

	protected String getUserPreference(String key) {
		KbeeUser user = getSessionUser();
		if (user != null)
			return user.getService(PreferencesService.class).getValue(getName(), key);
		return null;
	}

	protected void setUserPreference(String key, String value) {
		KbeeUser user = getSessionUser();
		if (user != null)
			user.getService(PreferencesService.class).setValue(getName(), key, value);
	}

	protected boolean isSupport() {
		return !ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot() && ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}

	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	/**
	 * For Advanced Search. If true the Console must provide a panel with id
	 * "advancedsearch"
	 * 
	 * @return
	 */
	protected boolean hasTopPanel() {
		return false;
	}

	protected void setBillboardPanel(Panel panel) {
		if (panel==null)
			throw new IllegalArgumentException("billboard-panel can not be null");
		if (!panel.getId().contentEquals("billboard"))
			throw new IllegalArgumentException("billboard Panel must have id billboard");
		addOrReplace(panel);
	}
	
	protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
		try {		
			if (rowmodel.getObject().getObject() instanceof com.novamens.dom.Object) {
				com.novamens.dom.Object object = (com.novamens.dom.Object) rowmodel.getObject().getObject();
				if (object.getState()==ObjectState.ARCHIVED)	return "archived-state";
				if (object.getState()==ObjectState.DELETED)		return "deleted-state";	
			}
			return null;
		} 
		catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	public String getGridExportTitle() {
		String str=ServiceLocator.getService(DateTimeService.class).formatTZ( OffsetDateTime.now(), getSessionUser().getTimeZone(), getSessionUser().getLocale(), DateTimeService.Dow_Month_Day_year);
		return (getName()!=null ? getName().toLowerCase().trim().replace(" ", "-") :"")+"-"+str;
	}
	
	protected String format(double val) {
		if (this.nf==null) {
			this.nf = NumberFormat.getInstance(getSessionUser().getLocale());
			nf.setMinimumFractionDigits(2);
			nf.setMaximumFractionDigits(2);
			nf.setRoundingMode(RoundingMode.HALF_UP);
		}
		return nf.format(val);
	}
	
	protected String format(int val) {
		if (this.integer_nf==null) {
			this.integer_nf = NumberFormat.getInstance(getSessionUser().getLocale());
			integer_nf.setMinimumFractionDigits(0);
			integer_nf.setMaximumFractionDigits(0);
			integer_nf.setRoundingMode(RoundingMode.HALF_UP);
		}
		return integer_nf.format(val);
	}
	
	protected URL getCustomExcelTemplate(){
		return null;
	}
	
	protected String convertWithStream(Map<String, Object> map) {
		String mapAsString = map.keySet().stream()
	      .map(key -> key + "=" + map.get(key))
	      .collect(Collectors.joining(", ", "{", "}"));
	    return mapAsString;
	}
	
	protected Map<String, Object> convertWithStream(String mapAsString) {
	    Map<String, Object> map = Arrays.stream(mapAsString.split(","))
	      .map(entry -> entry.split("="))
	      .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]));
	    return map;
	}
	
	protected String getDescription() {
		StringBuilder str = new StringBuilder();
			str.append("<section>");
			str.append("<h3>"+ getDisplayName().getObject() + "</h3>");
			str.append(new StringResourceModel("console-description", this, null).getString());
			str.append("</section>");
			return str.toString();
	}
	
	protected boolean isMyListsEnabled() {
		return false;
	}
	
	protected void refreshInfoArea(AjaxRequestTarget requestTarget) {
		requestTarget.add(get("error-panel-container"));
	}
	
	protected void setDefaultTopPanelVisible(boolean b) {
		this.default_top_panel_visible=b;
	}

	protected boolean isDefaultTopPanelVisible() {
		return this.default_top_panel_visible;
	}
	
	protected UserProfile getSessionUserProfile() {
		return getContentDao().findUserProfileByUser(getSessionUser());
	}
	
	protected String getSessionUserName() {
		if (getSessionUser()==null)
			return "null";
		return getSessionUser().getUserName();
	}
	
	protected KbeeUser getSessionUser() {
		try {
			if (model_session_user != null && model_session_user.getObject() != null)
				return (KbeeUser) model_session_user.getObject();

			User session_user = ServiceLocator.getService(SecurityService.class).getSessionUser();
			model_session_user = new ObjectModel<User>(session_user);
			return (KbeeUser) model_session_user.getObject();
		} catch (Exception e) {
				logger.error(e);
			return null;
		}
	}
	
	protected boolean isRoot() {
		return  this.root;
	}

	protected boolean isAdmin() {
		return this.role_admin;
	}

	protected boolean isDomainKbee() {
		if (this.is_domain_kbee == null) {
			try {
				this.is_domain_kbee = Boolean.valueOf(
						getPerson().getDomain().getName().toLowerCase().trim().equals("kbee"));
			} catch (Exception e) {
				logger.error(e);
				this.is_domain_kbee = Boolean.valueOf(false);
			}
		}
		return this.is_domain_kbee.booleanValue();
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	protected boolean isFreeVersion() {
		try {
			return getDomain().getDomainType()==DomainType.EXPRESS;
		}
		catch (Exception e) {
			logger.error(e);
			return false;
		}
	}
	
	protected <R> DomRepository<R> getRepository(Class<R> objectclass) {
		DomRepository<R> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
	}

	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	protected Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}
	
	protected abstract String getIcon(IModel<T> model);
	
}
 