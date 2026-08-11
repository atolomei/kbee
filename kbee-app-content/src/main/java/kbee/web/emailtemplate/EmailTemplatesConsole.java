package kbee.web.emailtemplate;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.danekja.java.util.function.serializable.SerializableSupplier;

import com.novamens.content.email.EmailTemplate;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.email.EmailService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.DateKbeeColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.GridDisplayMode;
import com.novamens.kbee.wicket.markup.html.console.grid.GridPanel;
import com.novamens.kbee.wicket.markup.html.console.grid.KbeePredicateGridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.LastModifiedColumn;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BreadCrumb;

import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.console.ExpandedPanel;
import kbee.web.console.grid.LinkPredicateKbeeGridColumn;
import kbee.web.model.object.ObjectAuditModal;
import kbee.web.nav.SettingsBC;
import kbee.web.query.EmailTemplatesQuery;

public abstract class EmailTemplatesConsole extends AbstractFacetedConsole<EmailTemplate> {
												
	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger = new kbee.util.logging.Logger(LogManager.getLogger(EmailTemplatesConsole.class.getName()));
							
	final boolean is_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_root	 = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 

	private Locale user_locale;
	private ZoneId user_zoneid;

	private List<GridColumn<SearchResult,String>> columns;
	
	public EmailTemplatesConsole(Query query) {
		super("templates", query);
	}

	
	@Override
	 protected  IModel<EmailTemplate> getModel(EmailTemplate object) {
			return new ObjectModel<EmailTemplate>(object, true);
	}
	
	 
	@Override
	protected String getIcon(IModel<EmailTemplate> model) {
		return null;
	}	

	@Override
	protected boolean isFiltersEnabled() {
		return true;
	}
	
	
	@Override
	protected boolean isSelectionEnabled() {
		return false;
	}

	@Override
	protected boolean isMenuEnabled() {
		return true;
	}

	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		//if (!ServiceLocator.getService(EmailService.class).hasEmailTemplate(getDomain())) 
			ServiceLocator.getService(EmailService.class).setUpTemplates(getDomain());
		
		this.user_zoneid = ZoneId.of(getSessionUser().getTimeZone());
		if (this.user_zoneid==null) 
			this.user_zoneid=ZoneId.systemDefault();
		this.user_locale = getSessionUser().getLocale();
		
		
		GridPanel<?> panel = (GridPanel<?>) getBrowser().getPanel(GridPanel.class);
		if (panel!=null)
			panel.setDefaultGridDisplayMode(GridDisplayMode.COMFORTABLE_NO_BCK);
		
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.columns=null;
	}
	
						
	
	@Override
	public Query newQuery() {
		return setUserPreference(new EmailTemplatesQuery());
	}
	
	public Page getConsolePage(Query query) {
		return getConsolePage(query, -1);
	}

	protected BreadCrumb getBreadCrumb() {
		return new BreadCrumb(new SettingsBC());
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("serial")
	@Override
	protected Panel getMenu(IModel<EmailTemplate> model) {
		
		ContextMenuPanel<EmailTemplate> menu = new ContextMenuPanel<EmailTemplate>(model);
		
		menu.setOutputMarkupId(true);
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<EmailTemplate>(id) {
				public void onClick(AjaxRequestTarget target) {
					setResponsePage(EmailTemplatesConsole.this.getPage(getModel(), EmailTemplatesConsole.this.getIndex(getModel().getObject()), false));
				}
				@Override 
				public String getLabel() {						
					return EmailTemplatesConsole.this.getLabel("contextmenu.open").getObject();
				}
			}
		);
		
		return menu;
	}

	
	/**
	 * Note that the Query is Hibernate Query
	 */
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (this.columns!=null)
			return this.columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();

		LinkPredicateKbeeGridColumn<EmailTemplate> titleColumn = new LinkPredicateKbeeGridColumn<EmailTemplate>("title",getLabel("name"),"title_sort", col->col.getTitle(), col->getModel(col)); 
		titleColumn.setContextKey(this.getName() + titleColumn.getContextKey());
		columns.add(titleColumn);

		KbeePredicateGridColumn<EmailTemplate> subjectClassColumn = new KbeePredicateGridColumn<>("subject", getLabel("subject"), "subject", (obj) -> obj.getSubjectHTML());
		subjectClassColumn.setContextKey(this.getName() + subjectClassColumn.getContextKey());
		columns.add(subjectClassColumn);

		KbeePredicateGridColumn<EmailTemplate> textClassColumn = new KbeePredicateGridColumn<>("text", getLabel("text"), (obj) -> obj.getTextHTML());
		textClassColumn.setDefaultWidth(GridColumn.DEFAULT_COLUMN_TEXT_WIDTH);
		textClassColumn.setContextKey(this.getName() + textClassColumn.getContextKey());
		columns.add(textClassColumn);

		KbeePredicateGridColumn<EmailTemplate> langClassColumn = new KbeePredicateGridColumn<>("lang", getLabel("lang"), "lang", (obj) -> obj.getLanguage());
		langClassColumn.setContextKey(this.getName() + langClassColumn.getContextKey());
		columns.add(langClassColumn);

		this.columns.add(new GridColumn<SearchResult, String>("status", getLabel("status"), "state") {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				try {
					if (result.getObject()==null) 
						return new Model<String>("err");
					ObjectState state = ((EmailTemplate) result.getObject()).getState();
					if (state==null)
						return new Model<String>("err");
					return new Model<String>(state.getHTMLLabel(getUser().getLocale()));
				} 
				catch (Exception e) {
					logger.error(e,  (getSessionUser()!=null?getSessionUser().getUserName():"null"));
					return new Model<String>(e.getClass().getName() + " | " + e.getMessage());
				}
			}

			@Override
			public IModel<String> getCellAsString(SearchResult result) {
				try {
					if (result.getObject()==null)
						return new Model<String>("err");
					ObjectState state = ((EmailTemplate) result.getObject()).getState();
					if (state==null)
						return new Model<String>("err");
					return new Model<String>(state.getLabel(getUser().getLocale()));
				} catch (Exception e) {
					logger.error(e,  (getSessionUser()!=null?getSessionUser().getUserName():"null"));
					return new Model<String>(e.getClass().getName() + " | " + e.getMessage());
				}
			}

			@Override
			protected String getContextKey() {
				return EmailTemplatesConsole.this.getName() + super.getContextKey();
			}
			@Override
			public boolean isPreferred() {
				return true;
			}
		});

		KbeePredicateGridColumn<EmailTemplate> keyClassColumn = new KbeePredicateGridColumn<>("key", getLabel("key"), "key", (obj) -> obj.getKey());
		keyClassColumn.setContextKey(this.getName() + keyClassColumn.getContextKey());
		columns.add(keyClassColumn);

		this.columns.add(new LastModifiedColumn<EmailTemplate>("modified", getLabel("modified"), "modified") {
			private static final long serialVersionUID = 1L;
			@Override
			protected String getContextKey() {
				return EmailTemplatesConsole.this.getName() + super.getContextKey();
			}
		});


		SerializableSupplier<String> formatSupplier = () -> this.getBrowser().getPanel(GridPanel.class).getDateFormat();
		DateKbeeColumn<EmailTemplate> createdColumn = new DateKbeeColumn<EmailTemplate>("created", getLabel("created"), "created", (obj)-> obj.getCreationOffsetDateTime(), formatSupplier);
		createdColumn.setContextKey(this.getName() + createdColumn.getContextKey());
		columns.add(createdColumn);

		KbeePredicateGridColumn<EmailTemplate> idColumn = new KbeePredicateGridColumn<>("id", getLabel("id"),  "id", (obj) -> String.valueOf(obj.getId()));
		idColumn.setContextKey(this.getName() + idColumn.getContextKey());
		idColumn.setPreferred(false);
		columns.add(idColumn);

		return this.columns;

	}

	
	/**
	 * 
	 * 
	 */
	@Override
	protected void addModals () {
		super.addModals();
		replace(new ObjectAuditModal<EmailTemplate>("audit-trail-modal"));
	}
	
	protected Page getPage(IModel<EmailTemplate> model, long index, boolean isnew) {
		return new EmailTemplatePage(model, isnew);
	}
	
	@Override
	protected void addListeners() {
		super.addListeners();

		add(new WicketEventListener<ClickEvent<EmailTemplate>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(ClickEvent<EmailTemplate> event) {
				setResponsePage(EmailTemplatesConsole.this.getPage(event.getModel(), getIndex(event.getModel().getObject()), false));
			}
		});
	}
	
	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<EmailTemplate> browser) {
		List<ToolbarItem> items = new ArrayList<ToolbarItem>();
		
		InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				InfoDialog infoDialog = (InfoDialog) getInformationModal();
				infoDialog.open(target,() -> {return EmailTemplatesConsole.this.getName();}, new Model<String>(EmailTemplatesConsole.this.getDescription()));
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
		};
		
		items.add(infoButton);
		
		return items;
	}
	
	protected String getDescription() {
		StringBuilder str = new StringBuilder();
			str.append("<section>");
			str.append("<h3>"+ getDisplayName().getObject() + "</h3>");
			str.append(new StringResourceModel("console-description", this, null).getString());
			str.append("</section>");
			return str.toString();
	}
		
	
	/**
	 * 
	 * 
	 */
	@Override
	protected boolean hasExpander() {
		return true;
	} 
	

	/**
	 */	  
	 
	@Override
	protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
		try {
			if (((EmailTemplate) rowmodel.getObject().getObject()).getState()==ObjectState.ARCHIVED) return "archived-state";
			if (((EmailTemplate) rowmodel.getObject().getObject()).getState()==ObjectState.DELETED)  return "deleted-state";
			return null;
		} 
		catch (Exception e) {
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
	protected Panel getPanel(IModel<EmailTemplate> model) {
		return new ExpandedPanel<EmailTemplate>("editor", this, model);
	}
	
	@Override
	protected Panel getPanel(IModel<EmailTemplate> model, List<String> snippets) {
		return new ExpandedPanel<EmailTemplate>("editor", this, model, snippets);
	}


	protected String getSectionDisplayName(String key) {
		return new StringResourceModel(key, this, null).getString();
	}

	
}
