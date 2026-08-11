package kbee.web.library;


import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import java.util.List;
import java.util.Locale;

import com.novamens.kbee.wicket.markup.html.console.grid.*;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.danekja.java.util.function.serializable.SerializableSupplier;
import org.springframework.dao.DataIntegrityViolationException;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.entity.Person;
import com.novamens.content.library.Library;
import com.novamens.content.model.LauncherGroup;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.DomService;
import com.novamens.content.web.security.markup.AclModal;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.content.library.KbeeLibrary;
import com.novamens.kbee.wicket.markup.html.console.browser.AjaxToolbarButton;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
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
import kbee.web.error.ApplicationErrorPage;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.model.object.ObjectAuditModal;
import kbee.web.nav.SecurityBC;
import kbee.web.object.ObjectStatusColumn;
import kbee.web.query.LibrariesQuery;
import kbee.web.query.LibraryQuery;

@SuppressWarnings("serial")
public abstract class LibrariesConsole extends AbstractFacetedConsole<Library> {
				
	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(LibrariesConsole.class.getName());
							
	final boolean is_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_root	 = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 

	private Locale user_locale;
	private ZoneId user_zoneid;

	private List<GridColumn<SearchResult,String>> columns;
	
	private NumberFormat nf;
	private NumberFormat integer_nf = null;

	private List<ToolbarItem> selection_toolbar;
	
	/**
	 * 
	 * 
	 * @param query
	 */
	public LibrariesConsole(Query query) {
		super("cabinets", query);
	}
	
	
	@Override
	protected String getIcon(IModel<Library> model) {
		return null;
	}	

	@Override
	 protected  IModel<Library> getModel(Library object) {
			return new ObjectModel<Library>(object, true);
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
		this.selection_toolbar=null;
		this.columns=null;
	}
	
	@Override
	public Query newQuery() {
		return setUserPreference(new LibrariesQuery());
	}
	
	public Page getConsolePage(Query query) {
		return getConsolePage(query, -1);
	}


	/**
	 * Note that the Query is Hibernate Query
	 */
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (this.columns!=null)
			return this.columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();

		this.columns.add(new ObjectStatusColumn<Person>("iconstatus", getName(), new Model<String>("St")));

		
		LinkPredicateKbeeGridColumn<Library> titleColumn =
				new LinkPredicateKbeeGridColumn<Library>("title",getLabel("librariesconsole.column.name"),"title_sort", col->col.getName(), col->getModel(col));
		titleColumn.setContextKey(this.getName() + titleColumn.getContextKey());
		columns.add(titleColumn);


		this.columns.add(new LastModifiedColumn<Library>("modified", getLabel("librariesconsole.column.modified"), "modified"){
			@Override
			protected String getContextKey() {
				return LibrariesConsole.this.getName() + super.getContextKey();
			}
		});

		this.columns.add(new GridColumn<SearchResult, String>("status", getLabel("status"), "state") {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getLabelModel(SearchResult result) {
				try {
					if (result.getObject()==null) 
						return new Model<String>("err");
					ObjectState state = ((KbeeLibrary)result.getObject()).getState();
					
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
					ObjectState state = ((KbeeLibrary)result.getObject()).getState();

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
				return LibrariesConsole.this.getName() + super.getContextKey();
			}
			@Override
			public boolean isPreferred() {
				return true;
			}
		});

		
		
		{
			KbeePredicateGridColumn<Library> portal = new KbeePredicateGridColumn<>("portal", getLabel("portal"), "library",	obj ->  obj.getDisplayName());
			 
			portal.setHtmlValueResolver( obj -> getPortalLink(obj) );
			portal.setTextValueResolver( obj -> obj.getDisplayName()  	);
			portal.setContextKey(this.getName() + portal.getContextKey());
			this.columns.add(portal);
		}

		
		KbeePredicateGridColumn<Library> totalColumn = new KbeePredicateGridColumn<Library>( "totalcontents", getLabel("totalcontents"), null, (obj) -> getIntegerNumberFormat().format(getTotalContent(obj)));
		totalColumn.setCssClass("col col-xs-1 col-md-1 col-lg-1 ui-resizable centered");
		totalColumn.setLabelCss("number-mdx");
		totalColumn.setCssValueResolver(obj ->   getNumberClass(obj));
		totalColumn.setPreferred(false);
		totalColumn.setContextKey(this.getName() + totalColumn.getContextKey());
		columns.add(totalColumn);


		KbeePredicateGridColumn<Library> keyClassColumn = new KbeePredicateGridColumn<>("key", getLabel("librariesconsole.column.key"), (obj) -> obj.getKey());
		keyClassColumn.setContextKey(this.getName() + keyClassColumn.getContextKey());
		columns.add(keyClassColumn);

		
		SerializableSupplier<String> formatSupplier = () -> this.getBrowser().getPanel(GridPanel.class).getDateFormat();
		DateKbeeColumn<Library> createdColumn = new DateKbeeColumn<Library>("created", getLabel("created"), (obj)-> obj.getCreationOffsetDateTime(), formatSupplier);
		createdColumn.setContextKey(this.getName() + createdColumn.getContextKey());
		columns.add(createdColumn);

		KbeePredicateGridColumn<Library> ordersColumn = new KbeePredicateGridColumn<>("order", getLabel("librariesconsole.column.order"), "order", (obj) -> String.valueOf(obj.getOrder()));
		ordersColumn.setContextKey(this.getName() + ordersColumn.getContextKey());
		ordersColumn.setPreferred(false);
		columns.add(ordersColumn);

		KbeePredicateGridColumn<Library> conditionColumn = new KbeePredicateGridColumn<>("condition", getLabel("librariesconsole.column.criteria"),  (obj) -> ((KbeeLibrary)obj).getStatement());
		conditionColumn.setContextKey(this.getName() + conditionColumn.getContextKey());
		columns.add(conditionColumn);

		KbeePredicateGridColumn<Library> idColumn = new KbeePredicateGridColumn<>("id", getLabel("librariesconsole.column.id"),  (obj) -> String.valueOf(obj.getId()));
		idColumn.setContextKey(this.getName() + idColumn.getContextKey());
		idColumn.setPreferred(false);
		columns.add(idColumn);

		return this.columns;
	}
	

	
	protected String getPortalLink(Library obj) {
		Site site = getPortalDao().getLibrarySite(obj);
		if (site!=null) 
			return "<a  target=\"_blank\" class=\"btn-link\" href=\""+getServerUrl()+"/lib/"+obj.getKey()+"\">" + obj.getDisplayName()+ "</a>";
		else
			return "<a  target=\"_blank\" class=\"btn-link\" href=\"#\"" + obj.getDisplayName() + " [not found]</a>";
	}

	protected int getTotalContent(Library lib) {
		LibraryQuery q = new LibraryQuery(getQueryIndex(),lib) {
			@Override
			public boolean includeFacets() {
				return false;
			}
		};
		return q.execute().size();
	}
	
	
	@Override
	protected void addModals() {
		super.addModals();
		add(new InvisiblePanel("acl-modal"));
		addOrReplace(new ObjectAuditModal<Library>("audit-trail-modal"));
	}
	
	
	protected Page getPage(IModel<Library> model, long index, boolean isnew) {
		return new LibraryPage(model, isnew);
	}
	
	@Override
	protected void addListeners() {
		super.addListeners();
		add(new WicketEventListener<ClickEvent<Library>>() {
			@Override
			public void onEvent(ClickEvent<Library> event) {
				setResponsePage(LibrariesConsole.this.getPage(event.getModel(), getIndex(event.getModel().getObject()), false));
			}
		});
	}
	
	
	/**
	 * 
	 * Main Toolbar
	 * 
	 * 
	 */
	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<Library> browser) {
		List<ToolbarItem> items = new ArrayList<ToolbarItem>();
		items.add(new NewLibraryButton(browser, ToolbarItem.Align.TOP_LEFT) {
			@Override
			protected void onClick() {
				try {
					String name=new StringResourceModel("library", LibrariesConsole.this, null).getObject()+" "+String.valueOf(getContentDao().getLibraries(getDomain()).size()+1);
					Object library = ServiceLocator.getService(ContentFactoryService.class).createLibrary(name);
					Page page = LibrariesConsole.this.getPage(LibrariesConsole.this.getModel((Library)library), 0, true);
					setResponsePage(page);
				}
				catch (ContentCreationException e) {
					logger.error(e);
					setResponsePage( new ApplicationErrorPage<>(e));
		}
			}
		});
		
		
		
		InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				InfoDialog infoDialog = (InfoDialog) getInformationModal();
				infoDialog.open(target,() -> {return  new StringResourceModel("about", LibrariesConsole.this, null).getObject();}, new Model<String>(LibrariesConsole.this.getDescription()));
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
		};
		
		items.add(infoButton);
		
		return items;
	}
	
	
	
	
	
	/**
	 * 
	 *  
	 *  
	 *  
	 *  
	 *  
	 * Selection toolbar
	 */
	@Override					
	protected List<ToolbarItem> getSelectionToolbarItems(BaseBrowser<Library> browser) {
		
		if (this.selection_toolbar!=null)
			return this.selection_toolbar;
		this.selection_toolbar = new ArrayList<ToolbarItem>();
		
	
		this.selection_toolbar.add(new AjaxToolbarButton(browser, ToolbarItem.Align.TOP_LEFT) {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		@Override
		public boolean isEnabled() {
			return true;
		}
		
		protected String getIcon() {
			return "";
		}
		
		@Override
		public boolean isVisible() {
			return true;
		}
		
		protected String getLabelStr() {
			 return new StringResourceModel("delete", LibrariesConsole.this).getObject();
		 }
		@Override
		public void onClick(AjaxRequestTarget target) {
			try {
				String result=LibrariesConsole.this.delete(getBrowser().getSelection());
				if (result!=null)
					getErrorDialog().open(target, new Model<String>("Error") ,new Model<String>(result));
				LibrariesConsole.this.resetSelection();
				LibrariesConsole.this.refresh(target);
				
			} catch (Exception e) {
				logger.error(e);
				getErrorDialog().open(target, new Model<String>(e.getClass().getSimpleName()) ,new Model<String>(e.getMessage()));
				LibrariesConsole.this.refresh(target);
			}
		}
	});

	return this.selection_toolbar;
	}
	
	@Override
	protected boolean hasExpander() {
		return true;
	} 

	
	protected BreadCrumb getBreadCrumb() {
		return new BreadCrumb(new SecurityBC());
	};

	@Override
	protected Panel getMenu(IModel<Library> model) {
		
		ContextMenuPanel<Library> menu = new ContextMenuPanel<Library>(model);
		
		menu.setOutputMarkupId(true);
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Library>(id) {
				public void onClick(AjaxRequestTarget target) {
					setResponsePage(LibrariesConsole.this.getPage(getModel(), LibrariesConsole.this.getIndex(getModel().getObject()), false));
				}
				@Override 
				public String getLabel() {
					return LibrariesConsole.this.getLabel("librariesconsole.contextmenu.open").getObject();
				}
			}
		);
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Library>(id) {
				@SuppressWarnings("unchecked")
				public void onClick(AjaxRequestTarget target) {
					Modal modal = LibrariesConsole.this.getAuditTrailModal();
					((ObjectAuditModal<Library>)modal).open(target, getModel(), true);
				}
				@Override 
				public String getLabel() {
					return getConsoleLabel("librariesconsole.contextmenu.audittrail").getObject();
				}
			}
		);
		
		menu.addItem(id ->
			new SeparatorMenuItemPanelV5<Library>(id) {
				@Override
				public String getCssClass() {
					return "divider";
				}
				@Override
				public boolean isVisible() {
					return  true;
				}
			}
		);
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Library>(id) {
				public void onClick(AjaxRequestTarget target) {
					
					/**
					getConfirmationDialog().open(target, getConsoleLabel("deleteconfirmation.message", getModel().getObject().getDisplayName()), Dialog.Delete, new Dialog.Handler() {
						@Override
						public void onClick(AjaxRequestTarget target, Button button) {
							if (button.key().equals(Dialog.Delete.key())) {
								try {
									((KbeeLibrary)getModelObject()).getService(DomService.class).delete();
								}
								catch (DataIntegrityViolationException e) {
									logger.error(e);
									getErrorDialog().open(target, getConsoleLabel("error.constraint"));
								}
								catch (Exception e) {
									logger.error(e);
									getErrorDialog().open(target, new Model<String>(e.getMessage()));
								}
								LibrariesConsole.this.refresh(target);
							}
						}
					});**/
					
					try {
						((KbeeLibrary)getModelObject()).getService(DomService.class).delete();
						FeedbackHelper.showInfoToast("ok");
					}
					catch (DataIntegrityViolationException e) {
						logger.error(e);
						FeedbackHelper.showErrorToast(e.getClass().getName() + " " + e.getMessage());
					}
					catch (Exception e) {
						fire (new ErrorEvent<>(target, e));
						FeedbackHelper.showErrorToast(getConsoleLabel("error.constraint").getObject());
					}
					refresh(target);
					
				}
				@Override 
				public String getLabel() {
					return getConsoleLabel("contextmenu.delete").getObject();
				}
				@Override
				public boolean isEnabled() {
					
					if (getModel().getObject().isCanonical())
						return false;
					
					if (is_support && !is_root)
						return false;
					
					return true;
				}
			}
		);
		
		return menu;
	}

	
	
	@Override
	protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
		try {
			if (((Library) rowmodel.getObject().getObject()).getState()==ObjectState.ARCHIVED) return "archived-state";
			if (((Library) rowmodel.getObject().getObject()).getState()==ObjectState.DELETED)  return "deleted-state";
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
	protected Panel getPanel(IModel<Library> model) {
		return new ExpandedPanel<Library>("editor", this, model);
	}
	
	@Override
	protected Panel getPanel(IModel<Library> model, List<String> snippets) {
		return new ExpandedPanel<Library>("editor", this, model, snippets);
	}
	
	private NumberFormat getIntegerNumberFormat() {
		return this.integer_nf;
	}
	
	@SuppressWarnings("unused")
	private NumberFormat getNumberFormat() {
		return this.nf;
	}
	
	protected String getNumberClass(Library obj) {
		try {
		int ref=  getTotalContent(obj);
		return ref>0?"col number-mdx info" : "col number-mdx";
		} catch (Exception e) {
			logger.error(e);
			return "number-mdx";
		}
	}
	
	
	/**
	 * 
	 * @param selection
	 * @return
	 */
	protected String delete(List<?> selection) {
	
		StringBuilder str = new StringBuilder();
		
		@SuppressWarnings("unchecked")
		List<IModel<Library>> list = (List<IModel<Library>>)  selection;

		for (IModel<Library> c:list) {
			try {
				logger.debug(" deleteing " + c.getObject().getName());
				((KbeeLibrary) c).getService(DomService.class).delete();

			} catch (Exception e) {
				logger.error(e);
				str.append(c.getObject().getName()+" -> " + e.getMessage());
			}
		}
		if (str.length()==0)
			return null;
		return str.toString();


	}

	protected PortalDao getPortalDao() {
		return (PortalDao)ServiceLocator.getService(BeansService.class).getBean("portalDao");
	}

}
