package kbee.web.scheduler;


import java.math.RoundingMode;

import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.security.IQLRule;
import com.novamens.content.web.admin.markup.SystemInfoGeneralPage;
import com.novamens.datetime.DateTimeService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.content.base.KbeeSource;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.KbeePredicateGridColumn;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.SchedulerService;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BreadCrumb;


import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.console.ExpandedPanel;
import kbee.web.object.AuditTrailModal;
						
public  abstract class SchedulerCronJobsConsole extends AbstractFacetedConsole<AbstractCronJobRequest> {
	
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SchedulerCronJobsConsole.class.getName());

	private NumberFormat integer_nf = null;
	
	final boolean is_root 			= isDomainKbee() && ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_service_admin	= isDomainKbee() && (is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId()));
	final boolean is_factory_admin	= isDomainKbee() && (is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId()));
	final boolean is_api			= isDomainKbee() && (is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.API_DEVELOPER.getId()));
	final boolean is_domain_admin	= isDomainKbee() && (is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()));
	final boolean is_operations		= isDomainKbee() && (is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.OPERATIONS_ENGINEER.getId()));
	final boolean is_support		= isDomainKbee() && (ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId()));
 	
	private NumberFormat nf;
	
	private List<GridColumn<SearchResult,String>> columns;

	private List<ToolbarItem> items;
	
	long start = System.currentTimeMillis();

	
	boolean TRUNCATE = true;
	boolean DO_NOT_TRUNCATE = false;
	
	public SchedulerCronJobsConsole (String name, Query query) {
		super(name, query);
	}
	
	public SchedulerCronJobsConsole (Query query) {
		super("scheduler", query);
	}

	
	@Override
	protected String getIcon(IModel<AbstractCronJobRequest> model) {
		return null;
	}

	
	
	private String getParam(AbstractCronJobRequest o, boolean truncate) {
		
		if (o.getParameters()==null)
			return "";
		
		String r = o.getParameters().toString();
		
		
		if (truncate && r.length()>300)
			return r.substring(0, 300)+"<span class=\"ago\">...</span>";
					
		return r;
		
	}
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (this.columns!=null)
			return this.columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();
		
		try {

				{
					KbeePredicateGridColumn<AbstractCronJobRequest> idColumn = new KbeePredicateGridColumn<>("name", getLabel("title"), "title_sort", (obj) -> obj.getDisplayName());
					idColumn.setContextKey(this.getName() + idColumn.getContextKey());
					idColumn.setPreferred(true);
					columns.add(idColumn);
				}
				
				{
					KbeePredicateGridColumn<AbstractCronJobRequest> idColumn = new KbeePredicateGridColumn<>("cronexpression", new Model<String>("cronexpression"), (obj) -> 
					obj.getCronExpression()!=null?
							(obj.getCronExpression().toHTMLString("predicate")+ "  " + 
					        (obj.getTimeZone()!=null?obj.getTimeZone():"")
							
									) :
							"");
					
					idColumn.setContextKey(this.getName() + idColumn.getContextKey());
					idColumn.setPreferred(true);
					columns.add(idColumn);
				}
				
	
				{
					KbeePredicateGridColumn<AbstractCronJobRequest> idColumn = new KbeePredicateGridColumn<>("parameters", new Model<String>("parameters"), (obj) -> getParam(obj, TRUNCATE));
					idColumn.setContextKey(this.getName() + idColumn.getContextKey());
					idColumn.setHtmlValueResolver(obj -> getParam(obj, TRUNCATE));
					idColumn.setExpandedValueResolver(obj -> getParam(obj, DO_NOT_TRUNCATE));
					idColumn.setDefaultWidth(800);
					idColumn.setPreferred(true);
					columns.add(idColumn);
				}
	
				{
					KbeePredicateGridColumn<AbstractCronJobRequest> idColumn = new KbeePredicateGridColumn<>("description", new Model<String>("description"), (obj) -> obj.getDescription());
					idColumn.setContextKey(this.getName() + idColumn.getContextKey());							
					idColumn.setPreferred(true);
					idColumn.setDefaultWidth(800);
					columns.add(idColumn);
				}
	
				{
					KbeePredicateGridColumn<AbstractCronJobRequest> idColumn = new KbeePredicateGridColumn<>("applicationserverId", new Model<String>("applicationServerid"), (obj) -> obj.getApplicationServerId());
					idColumn.setContextKey(this.getName() + idColumn.getContextKey());							
					idColumn.setPreferred(true);
					columns.add(idColumn);
				}

				{
					KbeePredicateGridColumn<AbstractCronJobRequest> idColumn = new KbeePredicateGridColumn<>("serverhost", new Model<String>("serverhost"), (obj) -> obj.getServerHost());
					idColumn.setContextKey(this.getName() + idColumn.getContextKey());							
					idColumn.setPreferred(true);
					columns.add(idColumn);
				}
				

				{
					KbeePredicateGridColumn<AbstractCronJobRequest> idColumn = new KbeePredicateGridColumn<>("class", new Model<String>("class"), (obj) -> obj.getClass().getName());
					idColumn.setContextKey(this.getName() + idColumn.getContextKey());							
					idColumn.setPreferred(true);
					idColumn.setDefaultWidth(500);
					columns.add(idColumn);
				}


				{
					KbeePredicateGridColumn<AbstractCronJobRequest> idColumn = new KbeePredicateGridColumn<>("domainid", new Model<String>("domainid"), (obj) -> obj.getDomainId()!=null?obj.getDomainId().toString():"");
					idColumn.setContextKey(this.getName() + idColumn.getContextKey());							
					idColumn.setPreferred(true);
					idColumn.setDefaultWidth(500);
					columns.add(idColumn);
				}

				{
					KbeePredicateGridColumn<AbstractCronJobRequest> idColumn = new KbeePredicateGridColumn<>("next", new Model<String>("next"),	(obj) -> getNext(obj) ); 
					idColumn.setContextKey(this.getName() + idColumn.getContextKey());							
					idColumn.setPreferred(true);
					columns.add(idColumn);
				}
				
				{
					KbeePredicateGridColumn<AbstractCronJobRequest> idColumn = new KbeePredicateGridColumn<>("timezone", new Model<String>("timezone"), (obj) -> obj.getTimeZone()!=null?obj.getTimeZone().toString():"");
					idColumn.setContextKey(this.getName() + idColumn.getContextKey());							
					idColumn.setPreferred(true);
					columns.add(idColumn);
				}
				
				
				{
					KbeePredicateGridColumn<AbstractCronJobRequest> idColumn = new KbeePredicateGridColumn<>("enabled", getLabel("enabled"), (obj) -> obj.isEnabled()? "yes":"no");
					idColumn.setContextKey(this.getName() + idColumn.getContextKey());
					idColumn.setPreferred(true);
					columns.add(idColumn);
				}
	

				{
					KbeePredicateGridColumn<AbstractCronJobRequest> idColumn = new KbeePredicateGridColumn<>("isuser", new Model<String>("User/System"), (obj) -> obj.isUserRequest() ? "User":"System");
					idColumn.setContextKey(this.getName() + idColumn.getContextKey());
					idColumn.setPreferred(true);
					columns.add(idColumn);
				}

				
				
				this.columns.add(new GridColumn<SearchResult, String>("id", getLabel("id")) {
					private static final long serialVersionUID = 1L;
					@Override
					protected IModel<String> getLabelModel(SearchResult result) {
						if (result.getObject()==null) 
							return new Model<String>("err");
						try {
							return new Model<String>(((AbstractCronJobRequest)result.getObject()).getId().toString());
						} catch (Exception e) {
							logger.error(e);
							return new Model<String>(e.getClass().getSimpleName());
						}
					}
					@Override
					protected String getContextKey() {
						return SchedulerCronJobsConsole.this.getName() + super.getContextKey();
					}
				});
				
			
		} catch (Exception e) {
			logger.error(e);
		}
		return this.columns;
		
	}
	
	
	public void onAfterRender() {
		super.onAfterRender();
		if (logger.isDebugEnabled()) {
			long end = System.currentTimeMillis();
			logger.debug("Total time " + String.valueOf(end-start)+" ms");
		}
	}
	
	
	private String  getNext(AbstractCronJobRequest job) {
		
		try {
		
			if (job.getCronExpression()==null)
				return "";
			
			String tz=(job.getTimeZone()!=null? job.getTimeZone():TimeZone.getDefault().getID());
			
			ZonedDateTime zd = job.getCronExpression().nextTimeAfter(ZonedDateTime.now(ZoneId.of(tz)));
			
			
			ZoneId userZoneId = ZoneId.of(getSessionUser().getTimeZone());
			ZonedDateTime userDateTime = zd.withZoneSameInstant(userZoneId);
			String user_nextda=ServiceLocator.getService(DateTimeService.class).format(userDateTime);
			return user_nextda;
		}
		catch (Exception e) {
			logger.error(e);
			return e.getClass().getName();
		}
	}
	
	
	
	@Override
	protected boolean isFiltersEnabled() {
		return false;
	}
	
	@Override
	protected boolean isSelectionEnabled() {
		return false;
	}
	
	protected boolean isSavedQueriesEnabled() {
		return false;
	}
	
	
	protected IModel<AbstractCronJobRequest> getModel(AbstractCronJobRequest object) {
		return new Model<AbstractCronJobRequest>(object);
	}

	
 	@Override
	public IModel<String> getDisplayName() {
		return getLabel(getName());
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		this.nf = NumberFormat.getInstance(getSessionUser().getLocale());
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		nf.setRoundingMode(RoundingMode.HALF_UP);
 		
		this.integer_nf = NumberFormat.getInstance(getSessionUser().getLocale());
		integer_nf.setMinimumFractionDigits(0);
		integer_nf.setMaximumFractionDigits(0);
		integer_nf.setRoundingMode(RoundingMode.HALF_UP);
	}
	
	
	/**
	 * 
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		this.columns=null;
		this.items=null;
	}

	
	@Override
	public Query newQuery() {
		return setUserPreference(new CronJobListQuery());
	}
	
	public Page getConsolePage(Query query) {
		return getConsolePage(query, -1);
	}
	
	
	protected BreadCrumb getBreadCrumb() {
		return null;
	};
	
	
	@Override
	protected boolean hasExpander() {
		return true;
	}

	
	@Override
	protected Panel getMenu(IModel<AbstractCronJobRequest> model) {
		ContextMenuPanel<AbstractCronJobRequest> menu = new ContextMenuPanel<AbstractCronJobRequest>(model);
		
		menu.setOutputMarkupId(true);
		

		menu.addItem(new MenuItemFactory<AbstractCronJobRequest>() {
			@Override
			public AbstractMenuItemPanelV5<AbstractCronJobRequest> getItem(String id) {
				return new AjaxMenuItemPanelV5<AbstractCronJobRequest>(id) {
					@SuppressWarnings("unchecked")
					public void onClick(AjaxRequestTarget target) {
						AbstractCronJobRequest request = getModel().getObject();
						ServiceLocator.getService(SchedulerService.class).processCronJobById(request.getId());
						FeedbackHelper.showInfoToast("Sent to Scheduler");
					}
					@Override 
					public String getLabel() {				
						return SchedulerCronJobsConsole.this.getLabel("execute").getObject();
					}
					@Override
					public boolean isEnabled() {
						return true;
					}
				};
			}
		});
		return menu;
	}


	protected Panel getPanel(IModel<AbstractCronJobRequest> model) {
		return new ExpandedPanel<AbstractCronJobRequest>("editor", this, model, null);
	};
	

	protected Panel getPanel(IModel<AbstractCronJobRequest> model, List<String> list) {
		return new ExpandedPanel<AbstractCronJobRequest>("editor", this, model, list);
	};

	
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(SidePanelEvent event) {
			}
		});

		/**
		add(new WicketEventListener<ClickEvent<Domain>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(ClickEvent<Domain> event) {
				//setResponsePage(getDomainPage( event.getModel(), event.getIndex(), false, false));
			}
		});
		**/
	}

	/**
	 * Grid Toolbar
	 */
	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<AbstractCronJobRequest> browser) {
	
		
		if (this.items!=null)
			return this.items;
		
		this.items =super.getToolbarItems(browser);

				
		InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				// InfoDialog infoDialog = (InfoDialog) getInformationModal();
				// infoDialog.open(target,() -> {return DomainsConsole.this.getName();}, new Model<String>(DomainsConsole.this.getDescription()));
			}
			
			@Override
			public boolean isVisible() {
				return false;
			}
		};
		
		
		this.items.add(infoButton);
		return items;
  	}

	
	@Override
	protected void addModals() {
		super.addModals();
	}
	
	
	protected Component newIcon() {
		return new WebMarkupContainer("icon");
	}

	/**
	@Override
	protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
		try {
			if (((Domain) rowmodel.getObject().getObject()).getState()==ObjectState.ARCHIVED)				return "archived-state";
			if (((Domain) rowmodel.getObject().getObject()).getState()==ObjectState.DELETED)				return "deleted-state";
			return null;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	**/

	
/** 
	private DomainMetricsService getDomainMetricsServices() {
		return ServiceLocator.getService(DomainMetricsService.class);
	}
**/
	
	private NumberFormat getIntegerNumberFormat() {
		return this.integer_nf;
	}
	
	private NumberFormat getNumberFormat() {
		return this.nf;
	}

	 

}
