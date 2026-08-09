package kbee.web.report;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.novamens.security.Identifiable;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.LanguageService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.system.parameters.SystemParameterService;


import kbee.web.console.AbstractSimpleConsole;
import kbee.web.console.BaseBrowser;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.resource.KBFile;
import com.novamens.content.user.UserService;

import com.novamens.dom.Domain;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.browser.FormButton;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.panel.ConsoleSidePanel;
import com.novamens.kbee.wicket.markup.html.console.panel.InvisibleConsoleSidePanel;
import com.novamens.user.PreferencesService;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.modal.InfoDialog;

import org.apache.wicket.model.StringResourceModel;

/**
 * 
 * 
 */
@SuppressWarnings("serial")
public abstract class ReportConsole extends AbstractSimpleConsole<Row> {

	private static final long serialVersionUID = 1L;
	
	public final static String REPORT = "reports";
	public final static String ACCESS = "access";

	/**
	 * Acuse de Recibo
	 * 
	 * Users by Content
	 * Visits by User
	 * 
	 */
	public final static String AUDIT  = "audit";
	
	public final static String SYSTEMAUDIT = "system-audit";
	
	static DateTimeFormatter  date_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReportConsole.class.getName());
	
	private List<ToolbarItem> items = null;
	private String key;
	private String report_group;
	
	
	public class RowModel implements IModel<Row> {
		Row row;
		public RowModel(Row r) {
			this.row=r;
		}
		@Override
		public Row getObject() {
			return row;
		}
	}
		
	protected String getStringFromTo() {
		OffsetDateTime to = (OffsetDateTime)getQuery().getParameters().get("to");
		if (to==null) to = OffsetDateTime.now();
		String tostr =  date_formatter.format(to);
		OffsetDateTime from = (OffsetDateTime)getQuery().getParameters().get("from");
		if (from==null) from = OffsetDateTime.now();
		String fromstr = date_formatter.format(from);
		return fromstr+"-to-"+tostr;
	}

	protected class InnerStringColumn extends ReportColumn {
		
		public InnerStringColumn(String id, IModel<String> displayModel, String sortProperty) {
			super(id, displayModel, sortProperty);
		}
		public InnerStringColumn(String id, IModel<String> displayModel) {
			super(id, displayModel, null);
		}
		
		
		protected IModel<String> getLabelModel(SearchResult result) {
			if (result.getObject()==null) 
				return new Model<String>("err");
			Row row = (Row)result.getObject();
			return  getLabelModel(row);
		}
		protected IModel<String> getLabelModel(Row row) {
			return  new Model<String>(row.get(getId()));
		}
		@Override
		protected String getContextKey() {
			return ReportConsole.this.getName() + super.getContextKey();
		}
	};
	
	
	protected class InnerNumberColumn extends ReportColumn {
		private boolean preferred=true;
		private boolean sparse_highligter = false;
		private String highlightCss = "info";
		
		public InnerNumberColumn(String id, IModel<String> displayModel, String sortProperty) {
			super(id, displayModel, sortProperty);
		}
		
		public InnerNumberColumn(String id, IModel<String> displayModel, String sortProperty, String label_css ) {
				this(id, displayModel, sortProperty, label_css, false);
		}
		
		public InnerNumberColumn(String id, IModel<String> displayModel, String sortProperty, String label_css, boolean highlightNonZero) {
				this(id, displayModel,sortProperty,label_css, highlightNonZero, " info");
		}
		
		public InnerNumberColumn(String id, IModel<String> displayModel, String sortProperty, String label_css, boolean highlightNonZero, String highlightCss) {
			super(id, displayModel, sortProperty);
			
			setLabelCss(label_css);
			this.sparse_highligter=highlightNonZero;
			this.highlightCss=highlightCss;
		}
		
		public IModel<String> getCellAsString(SearchResult result) {
			return getLabelModel(result);
		}

		protected IModel<String> getLabelModel(SearchResult result) {
			if (result.getObject()==null) 
				return new Model<String>("err");
			Row row = (Row)result.getObject();
			return  new Model<String>(row.get(getId()));
		}

		public void setHighlightNonZero(boolean b) {
			this.sparse_highligter =b;
		}
		
		public boolean ishighlightNonZero() {
			return this.sparse_highligter;
		}
		
		@Override
		protected String getContextKey() {
			return ReportConsole.this.getName() + super.getContextKey();
		}

		protected String getHighLightCss() {
			return highlightCss;
		}
		
		@Override
		protected String getLabelCss(IModel<SearchResult> model) {
			if (ishighlightNonZero()) {
				Row row = (Row) model.getObject().getObject();
				String str=row.get(getId());
				return getLabelCss() + (str!=null&& str.equals("0")? "" : (" " + getHighLightCss()));
			}
			else
				return getLabelCss();
		}
		@Override
		public String getCssClass() {
			return "col col-xs-1 col-md-1 col-lg-1 ui-resizable centered";
		}
		@Override
		public boolean isPreferred() {
			return this.preferred;
		}
		public void setPreferred(boolean b) {
			this.preferred=b;
		}
	};
	
	public class InnerIntegerColumn extends IntegerColumn {
		
		public InnerIntegerColumn(String id, IModel<String> displayModel) {
				this (id, displayModel, null);
		}
		
		public InnerIntegerColumn(String id, IModel<String> displayModel, String sortProperty) {
			super(id, displayModel, sortProperty);
		}
		
		public InnerIntegerColumn(String id, IModel<String> displayModel, String sortProperty, String label_css, boolean highlightNonZero) {
			super(id, displayModel, sortProperty);
			super.setLabelCss(label_css);
			super.setHighlightNonZero(highlightNonZero);	
		}
		
		@Override
		protected String getContextKey() {
			return ReportConsole.this.getName() + super.getContextKey();
		}
	}
	
	protected class InnerFloatColumn extends FloatColumn {

		public InnerFloatColumn(String id, IModel<String> displayModel, String sortProperty) {
			super(id, displayModel, sortProperty);
			setMaximumFractionDigits(2);
		}
		
		public InnerFloatColumn(String id, IModel<String> displayModel, String sortProperty, String label_css, boolean highlightNonZero) {
			super(id, displayModel, sortProperty);
			setMaximumFractionDigits(2);
			super.setLabelCss(label_css);
			super.setHighlightNonZero(highlightNonZero);	
		}
		
		@Override
		protected String getContextKey() {
			return ReportConsole.this.getName() + super.getContextKey();
		}
	};

	/**
	 * 
	 */
	public ReportConsole(String id) {
		this(id, null, id);
	}
	
	public ReportConsole(String id, Query query, String key) {
		super(id, query);
		setKey(key);
	}
	
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		String side = getUserPreference("sidepanel");
		if (side==null) 
			setUserPreference("sidepanel", ReportBaseParameterPanel.class.getName());
	}
	
	public void setKey( String key) {
		this.key=key;
	}
	
	public String getKey() {
		return key;
	}
	
	
	@Override
	public Page getConsolePage(Query query, long index) {
		throw new KbeeRuntimeException("not done");
	}
	
	
	protected Panel getItemListPanel(IModel<Row> model, int index) {
		return new  kbee.web.dashboard.LabelPanel("item", new Label("label",model.getObject().toString()));
	}
	
	public IModel<String> getDisplayName() {															
		return new Model<String>(ServiceLocator.getService(LanguageService.class).getString( getKey(), getSessionUser().getLocale()));
	}
	
	@Override
	public Panel getMenu(IModel<Row> row) {
		return null;
	}
	
	@Override
	public Query newQuery() {
		return null;
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}
	

	
	public Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	public ReportConsole clone(Query query) {
		return null;
	}

	public boolean isReadable() {
		SecurityService service = ServiceLocator.getService(SecurityService.class);
		boolean role_admin = service.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		boolean role_support = service.isMember(KbeeGlobalRole.SUPPORT.getId());
		return role_admin || role_support;
	}
	
	
	public void setReportGroup(String s) {
		this.report_group=s;
	}
	
	public String getReportGroup() {
		return this.report_group!=null?this.report_group: REPORT;
	}
	
	public String getReportGroupDisplayName() {
		try {
			String ret = ServiceLocator.getService(LanguageService.class).getString(getReportGroup(), getSessionUser().getLocale());
			if (ret!=null)
				return ret;
			return getReportGroup();
		} catch (Exception e ) {
			  logger.error(e);
			  return getReportGroup();
		 }
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	}
	
	@Override
	protected ConsoleSidePanel getRightPanel() {
		return new InvisibleConsoleSidePanel("side");
	}
	
	/**
	 * 
	 */
	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<Row> browser) {
		
		if (this.items!=null) 
			return this.items;
			
		this.items = super.getToolbarItems(browser);
		
		
		this.items.add(new FormButton(browser, ToolbarItem.Align.TOP_RIGHT) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					ReportConsole.this.getBrowser().togglePanel(ReportBaseParameterPanel.class);
					target.add(ReportConsole.this.getBrowser());
					fire(new SidePanelEvent(target));
				}
			});

		InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				InfoDialog infoDialog = (InfoDialog) getInformationModal();
				infoDialog.open(target,() -> {return "About";}, ReportConsole.this.getReportDescription());
			}
			
			@Override
			public boolean isVisible() {
				return (getReportDescription() != null);
			}
		};
		this.items.add(infoButton);
		
		return this.items;
	}


	
	
	/**
	 * 
	 * @return
	 */
	@Override
	public String getDownloadFileName() {
		DateTimeFormatter day_df = DateTimeFormatter.ofPattern("YYYY-MM-dd", getSessionUser().getLocale());
		return getDisplayName().getObject().toLowerCase().trim().replace(" ", "-") +  day_df.format(OffsetDateTime.now());
	}

	
	public String getReportTitle() {
		return getDisplayName().getObject();
	}

	public IModel<String> getReportDescription() {
		
		//String abs= ServiceLocator.getService(LanguageService.class).getString(getKey()+"-description", getSessionUser().getLocale());
		//if (abs!=null)
		//	return new Model<String>(abs);

		
		String paramName = "report." + (this.getName()!=null?this.getName().toLowerCase().trim(): this.getClass().getSimpleName().toLowerCase()) +".description";
		String s=ServiceLocator.getService(SystemParameterService.class).getParameter(paramName, null);
		if (s!=null)
			return new Model<String>(s);
		return new StringResourceModel("report.description", this, null);
	}

	public IModel<String> getReportAbstract() {
		
		String abs= ServiceLocator.getService(LanguageService.class).getString(getKey()+"-abstract", getSessionUser().getLocale());
		if (abs!=null)
			return new Model<String>(abs);
		
		
		String paramName = "report." + (this.getName()!=null?this.getName().toLowerCase().trim(): this.getClass().getSimpleName().toLowerCase()) +".abstract";
		String s=ServiceLocator.getService(SystemParameterService.class).getParameter(paramName, null);
		if (s!=null)
			return new Model<String>(s);
		return new StringResourceModel("report.abstract", this, null);
	}

	
	
	protected void addHeader() {
	}
	
	@Override
	protected IModel<Row> getModel(Row row) {
		return new Model<Row>(row);
	}
	
	@Override
	protected boolean isSelectionEnabled() {
		return false;
	}
	
	@Override
	protected boolean isMenuEnabled() {
		return false;
	}
	
	@Override
	protected boolean isFiltersEnabled() {
	 	return false;
	}

	@Override
	protected boolean hasExpander() {
		return false;
	}
	
	protected String getPreference(String name) {
		String value = getSessionUser().getService(PreferencesService.class).getValue(getName() + "-browser", name);
		return value;
	}
	
	protected  void setPreference(String name, String value) {
		getSessionUser().getService(PreferencesService.class).setValue(getName() + "-browser", name, value);
	}

 
	@Override
	protected String getDefaultUserPreference(String key) {
		String ret = super.getDefaultUserPreference(key);
			if (ret!=null)
				return ret;
			// harcoded defaults
			if (key.toLowerCase().contentEquals("sidepanel"))
				return ReportBaseParameterPanel.class.getName();
		return null;
	}

	

}