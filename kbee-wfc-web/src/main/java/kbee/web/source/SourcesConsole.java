package kbee.web.source;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.Source;
import com.novamens.content.library.Library;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.DOMObjectService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.content.base.KbeeSource;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.*;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.portal6.model.Site;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.LanguageService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.actions.*;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.util.BreadCrumb;

import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.console.ExpandedPanel;
import kbee.web.console.grid.LinkPredicateKbeeGridColumn;

import kbee.web.model.object.ObjectAuditModal;
import kbee.web.nav.SecurityBC;
import kbee.web.query.LibraryQuery;
import kbee.web.query.SourcesQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.danekja.java.util.function.serializable.SerializableSupplier;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("serial")
public abstract class SourcesConsole extends AbstractFacetedConsole<Source> {
	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger = new kbee.util.logging.Logger(LogManager.getLogger(SourcesConsole.class.getName()));

	final boolean is_support				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();

	private Locale user_locale;
	private  ZoneId user_zoneid;

	private List<GridColumn<SearchResult,String>> columns;
	private NumberFormat nf;
	private NumberFormat integer_nf = null;
	
	

	public SourcesConsole(Query query) {
		super("source", query);

		add(new WicketEventListener<SidePanelEvent>() {
			@Override
			public void onEvent(SidePanelEvent event) {
				// event.getRequestTarget().add(get("header"));
			}
		});
	}
	
	
	@Override
	protected String getIcon(IModel<Source> model) {
		return null;
	}
	
	@Override
	 protected  IModel<Source> getModel(Source object) {
			return new ObjectModel<Source>(object, true);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		
		
		
		
		user_zoneid = ZoneId.of(getSessionUser().getTimeZone());
		if (user_zoneid==null) 
			user_zoneid=ZoneId.systemDefault();
		user_locale = getSessionUser().getLocale();
		
		this.nf = NumberFormat.getInstance(getSessionUser().getLocale());
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		nf.setRoundingMode(RoundingMode.HALF_UP);
 		
		this.integer_nf = NumberFormat.getInstance(getSessionUser().getLocale());
		integer_nf.setMinimumFractionDigits(0);
		integer_nf.setMaximumFractionDigits(0);
		integer_nf.setRoundingMode(RoundingMode.HALF_UP);

		
	}
	

	@Override
	public boolean isSelectionEnabled() {
		return false;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		this.columns=null;
	}
	
	@Override
	public Query newQuery() {
		return setUserPreference(new SourcesQuery());
	}
	
	public Page getConsolePage(Query query) {
		return getConsolePage(query, -1);
	}

	// protected abstract Page getConsolePage(Query query, long index);

	protected BreadCrumb getBreadCrumb() {
		return new BreadCrumb(new SecurityBC());
	};

	@Override
	protected Panel getMenu(IModel<Source> model) {
		
		ContextMenuPanel<Source> menu = new ContextMenuPanel<>(model);
		
		menu.setOutputMarkupId(true);
		
		menu.addItem((MenuItemFactory<Source>) id -> new AjaxMenuItemPanelV5<Source>(id) {
			public void onClick(AjaxRequestTarget target) {
				setResponsePage(SourcesConsole.this.getPage(getModel(), SourcesConsole.this.getIndex(getModel().getObject()), false));
			}
			@Override
			public String getLabel() {
				return SourcesConsole.this.getLabel("sourcesconsole.contextmenu.open").getObject();
			}
		});
		
		menu.addItem((MenuItemFactory<Source>) id -> new AjaxMenuItemPanelV5<Source>(id) {
			@SuppressWarnings("unchecked")
			public void onClick(AjaxRequestTarget target) {
				Modal modal = SourcesConsole.this.getAuditTrailModal();
				((ObjectAuditModal<Source>)modal).open(target, getModel(), true);
			}
			@Override
			public String getLabel() {
				return getConsoleLabel("sourcesconsole.contextmenu.audittrail").getObject();
			}
		});
		
		menu.addItem(new MenuItemFactory<Source>() {
			@Override
			public AbstractMenuItemPanelV5<Source> getItem(String id) {
				return new SeparatorMenuItemPanelV5<Source>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
					@Override
					public boolean isVisible() {
						return  true;
					}
				};
			}
		});
		
		menu.addItem(new MenuItemFactory<Source>() {
			@Override
			public AbstractMenuItemPanelV5<Source> getItem(String id) {
				return new AjaxMenuItemPanelV5<Source>(id) {
					public void onClick(AjaxRequestTarget target) {
							getConfirmationDialog().open(target, getConsoleLabel("deleteconfirmation.message", getModel().getObject().getDisplayName()), Dialog.Delete, new Dialog.Handler() {
								@Override
								public void onClick(AjaxRequestTarget target, Button button) {
									if (button.key().equals(Dialog.Delete.key())) {
										try {
											((KbeeSource)getModelObject()).getService(DOMObjectService.class).delete();
										}
										catch (DataIntegrityViolationException e) {
											logger.error(e);
											getErrorDialog().open(target, getConsoleLabel("error.constraint"));
										}
										catch (Exception e) {
											logger.error(e);
											getErrorDialog().open(target, new Model<String>(e.getMessage()));
										}
										SourcesConsole.this.refresh(target);
									}
								}
							});
						refresh(target);
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("contextmenu.delete").getObject();
					}
					
					@Override
					public boolean isEnabled() {
						if (is_support && !is_root)
							return false;
						return true;
					}
				};
			}
		});
		
		return menu;
	}

	/**
	 * 
	 * Note that the Query is Hibernate Query
	 * 
	 */
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (this.columns!=null)
			return this.columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();
		

		{
			LinkPredicateKbeeGridColumn<Source> titleColumn =
					new LinkPredicateKbeeGridColumn<>("title", getLabel("sourcesconsole.column.name"), "title_sort", obj -> obj.getName(), obj -> getModel(obj));
			titleColumn.setContextKey(this.getName() + titleColumn.getContextKey());
			columns.add(titleColumn);
		}

		{
			KbeePredicateGridColumn<KbeeSource> displayNameColumn =
					new KbeePredicateGridColumn<>("displayname", getLabel("sourcesconsole.column.displayname"), (obj) -> obj.getDisplayName());
			displayNameColumn.setContextKey(this.getName() + displayNameColumn.getContextKey());
			displayNameColumn.setPreferred(false);
			columns.add(displayNameColumn);
		}

		{
			KbeePredicateGridColumn<KbeeSource> statusColumn = new KbeePredicateGridColumn<>("status", getLabel("sourcesconsole.column.status"), obj ->  obj.getState() != null ? obj.getState().getLabel(getUser().getLocale()) : "err"   );
			statusColumn.setHtmlValueResolver(obj -> obj.getState() != null ? obj.getState().getHTMLLabel(getUser().getLocale()) : "err");
			statusColumn.setContextKey(this.getName() + statusColumn.getContextKey());
			this.columns.add(statusColumn);
		}

		{
			SerializableSupplier<String> formatSupplier = () -> this.getBrowser().getPanel(GridPanel.class).getDateFormat();
			DateKbeeColumn<KbeeSource> createdColumn = new DateKbeeColumn<>("created", getLabel("sourcesconsole.column.created"), (obj) -> obj.getCreationOffsetDateTime(), formatSupplier);
			createdColumn.setContextKey(this.getName() + createdColumn.getContextKey());
			columns.add(createdColumn);
		}

		{
			SerializableSupplier<String> formatSupplier = () -> this.getBrowser().getPanel(GridPanel.class).getDateFormat();
			DateKbeeColumn<KbeeSource> createdColumn = new DateKbeeColumn<>("modified", getLabel("sourcesconsole.column.modified"), (obj) -> obj.getLastModifiedOffsetDateTime(), formatSupplier);
			createdColumn.setContextKey(this.getName() + createdColumn.getContextKey());
			columns.add(createdColumn);
		}

		{
			KbeePredicateGridColumn<KbeeSource> idColumn = new KbeePredicateGridColumn<>("modifiedby", getLabel("sourcesconsole.column.modifiedby"),
					(obj) -> obj.getLastModifiedUser() != null ? obj.getLastModifiedUser().getFirstLastName() : "");
			idColumn.setContextKey(this.getName() + idColumn.getContextKey());
			idColumn.setPreferred(false);
			columns.add(idColumn);
		}

		{
			KbeePredicateGridColumn<KbeeSource> idColumn = new KbeePredicateGridColumn<>("id", getLabel("sourcesconsole.column.id"), (obj) -> String.valueOf(obj.getId()));
			idColumn.setContextKey(this.getName() + idColumn.getContextKey());
			idColumn.setPreferred(false);
			columns.add(idColumn);
		}

		return this.columns;
	}

	
	
	@SuppressWarnings("unused")
	private NumberFormat getIntegerNumberFormat() {
		return this.integer_nf;
	}
	
	@SuppressWarnings("unused")
	private NumberFormat getNumberFormat() {
		return this.nf;
	}

	
	
	
	protected int getTotalContent(Library lib) {
		LibraryQuery q=new LibraryQuery(getQueryIndex(),lib);
		q.setIncludeFacets(false);
		return q.execute().size();
	}

	
	
	
	@Override
	protected void addModals () {
		super.addModals();
		addOrReplace(new ObjectAuditModal<Library>("audit-trail-modal"));
	}
	
	protected Page getPage(IModel<Source> model, long index, boolean isnew) {
		Page page = new SourcePage(model, isnew);
		return page;

	}
	
	@Override
	protected void addListeners() {
		super.addListeners();
		add(new WicketEventListener<ClickEvent<Source>>() {
			@Override
			public void onEvent(ClickEvent<Source> event) {
				setResponsePage(SourcesConsole.this.getPage(event.getModel(), getIndex(event.getModel().getObject()), false));
			}
		});
	}
	
	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<Source> browser) {
		List<ToolbarItem> items = new ArrayList<ToolbarItem>();
		items.add(new ToolBarButton(browser, ToolbarItem.Align.TOP_LEFT, new StringResourceModel("new", SourcesConsole.this, null).getObject()) {
			@Override
			protected void onClick() {
				try {
					
					String s = "Source";
					try {
						s=ServiceLocator.getService(LanguageService.class).getString("source", getDomain().getLocale(), "source");
					} catch (Exception e) {
						logger.error(e);
						s="source";
					}
					Object library = ServiceLocator.getService(ContentFactoryService.class).createSource(s, s);
					Page page = SourcesConsole.this.getPage(SourcesConsole.this.getModel((Source)library), 0, true);
					setResponsePage(page);
				}
				catch (ContentCreationException e) {
					logger.error(e);
					throw new KbeeRuntimeException(e);
				}
			}
		});
		
		InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				InfoDialog infoDialog = (InfoDialog) getInformationModal();
				infoDialog.open(target,() -> {return SourcesConsole.this.getName();}, new Model<String>(SourcesConsole.this.getDescription()));
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
		};

		items.add(infoButton);

		
		
		return items;
	}
	
	@Override
	protected boolean hasExpander() {
		return true;
	} 
	
	
	@Override
	protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
		try {
			if (((KbeeSource) rowmodel.getObject().getObject()).getState()==ObjectState.ARCHIVED) return "archived-state";
			if (((KbeeSource) rowmodel.getObject().getObject()).getState()==ObjectState.DELETED)  return "deleted-state";
			return null;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

		
	protected IModel<String> getStringDateModel(OffsetDateTime dt) {
		if (dt==null)
			return new Model<String>("err");
		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		ZonedDateTime zd = ZonedDateTime.ofInstant(dt.toInstant(), user_zoneid);
		return new Model<String>(service.timeElapsed(zd, user_zoneid, user_locale, DateTimeService.DATE_COLlOQUIAL_AGO, "ago"));
	}
	
	@Override
	protected Panel getPanel(IModel<Source> model) {
		return new ExpandedPanel<Source>("editor", this, model);
	}
	
	@Override
	protected Panel getPanel(IModel<Source> model, List<String> snippets) {
		return new ExpandedPanel<Source>("editor", this, model, snippets);
	}

	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

}
   