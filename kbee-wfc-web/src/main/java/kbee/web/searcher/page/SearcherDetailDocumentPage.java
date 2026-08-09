package kbee.web.searcher.page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import com.novamens.content.base.Content;
import com.novamens.content.document.IDoc;
import com.novamens.content.form.EFormAccessLevel;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.form.EForm;
import com.novamens.dom.Json;
import com.novamens.indexer.query.Cursor;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.kbee.portal.model.SearcherSiteQuery;
import com.novamens.kbee.wicket.markup.html.console.panel.SolrCursorModel;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.event.ClickBackEvent;
import com.novamens.kbee.wicket.markup.html.event.EditableListEvent;
import com.novamens.kbee.wicket.markup.html.event.EventHandler;
import com.novamens.kbee.wicket.markup.html.event.EventListenerWicket;
import com.novamens.kbee.wicket.markup.html.event.ExplorerOpenEvent;
import com.novamens.kbee.wicket.markup.html.event.ShareContentEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketAjaxEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrCursor;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.repeater.util.Searcher;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.util.logging.Logger;
import kbee.web.content.eform.ContentFormViewer;
import kbee.web.content.panel.ShareModal;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPanel;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.nav.CursorNavigationEvent;
import kbee.web.nav.NavigablePage;
import kbee.web.nav.Navigator;
import kbee.web.nav.NavigatorPanelV6;
import kbee.web.panel.ClickItemEvent;
import kbee.web.portal6.panel.PortalErrorPanel;
import kbee.web.searcher.PortalBC;
import kbee.web.searcher.panel.SearcherDetailHeaderPanel;
import kbee.web.searcher.panel.SearcherDetailMainPanel;
import kbee.web.searcher.panel.SearcherDetailToolsPanel;
import kbee.web.util.PanelBeanResolver;

@SuppressWarnings("serial")
public class SearcherDetailDocumentPage<T extends Content> extends SearcherDetailPage<T> implements EventHandler, NavigablePage<T> {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(SearcherDetailDocumentPage.class.getName());

	private WebMarkupContainer mc;

	private IModel<Cursor> cursor_model;
	private Searcher searcher;

	private int index = 0;
	private boolean isConsole = false;
	private Navigator<T> navigator;

	private boolean isStandAlonePage;
	private boolean has_breadcrumb = true;
	private boolean isAudit;

	private ShareModal<T> share_modal = null;
	private VerticalLayout<ITab> panel;
	private List<ITab> tabs;

	//private Map<String, String> map = new HashMap<String, String>();

	/**
	 * @param parameters
	 */
	public SearcherDetailDocumentPage(PageParameters parameters) {
		super(parameters);
		this.searcher = null;
		this.mc = new WebMarkupContainer("container");
		this.mc.setOutputMarkupId(true);
		add(this.mc);
	}

	public SearcherDetailDocumentPage(IModel<T> model, IModel<Site> site_model) {
		super(model, site_model);
		this.mc = new WebMarkupContainer("container");
		this.mc.setOutputMarkupId(true);
		add(this.mc);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (this.cursor_model != null)
			this.cursor_model.detach();
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		if (getModel() == null || getModel().getObject() == null) {
			add(new ErrorPanel("tabs", getLabel("error.title"), getLabel("error.no-document")));
			return;
		}

		if (this.mc == null) {
			setResponsePage(new SearcherDetailDocumentPage<T>(getModel(), getSiteModel()));
		}

		this.tabs = new ArrayList<ITab>();

		Json json = getSiteModel().getObject().getCustomValuesJson();

		if (json != null) {
			this.isAudit = ((json.get("audit") == null) || json.get("audit").equals("true"));
		}

		this.mc.addOrReplace(new ShareModal<T>("send-email-modal"));
		
		addEForms();
		addGeneralPanels();
		
		this.panel = new VerticalLayout<ITab>("tabs", "portal", this.tabs, VerticalLayout.VERTICAL, false);
		this.panel.setSections(VerticalLayout.COLS_9X3);
		this.panel.setTitle(getLabel("sections"));
		this.panel.setMenuItemFactory(getMenuItems());
		this.mc.add(this.panel);

		addBreadcrumbPanelPanel();
		addToolsPanel();
		addNavigatorPanel();
		
		addTitlePanel();
	}
	
	private void addGeneralPanels() {
		if (isWriteable()) {
			tabs.add(new AbstractTab(getLabel("tab.versioncontrol")) {
				@Override
				public Panel getPanel(String panelId) {
					return (new PanelBeanResolver(
						"searcher-detail-history-panel", 
						panelId,
						getModel(), 
						getSiteModel(), 
						isConsole())).getPanel();
				}
			});
		}
	}

	private void addEForms() {

		boolean has_eforms = false;

		for (EForm eform : getForms()) {
			try {
				tabs.add(new AbstractTabKB(new Model<String>(eform.getDisplayName()), eform.getName()) {
					@Override
					public Panel getPanel(String panelId) {
						try {
							return new ContentFormViewer<T>(panelId, getModel(), eform, getSiteModel());
						} catch (Exception e) {
							logger.error(e);
							return new PortalErrorPanel<>(panelId, e);
						}
					}
				});

				has_eforms = true;
			} catch (Throwable e) {
				logger.error(e);
				IModel<String> title = eform != null ? new Model<String>(eform.getDisplayName()) : new Model<String>("null");
				tabs.add(new AbstractTabKB(title, title.getObject()) {
					@Override
					public Panel getPanel(String panelId) {
						String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
						return new ErrorPanel(panelId, new Model<String>("Form Error"), new Model<String>(message));
					}
				});
			}
		}

		// ----------------

		if (!has_eforms) {
			tabs.add(new AbstractTab(getLabel("tab.main")) {
				@Override
				public Panel getPanel(String panelId) {
					try {
						return new SearcherDetailMainPanel<T>(panelId, getModel(), getSiteModel());
					} catch (Exception e) {
						logger.error(e);
						return new PortalErrorPanel<>(panelId, e);
					}
				}
			});
		}

	
	}


	private void addTitlePanel() {
		try {
			SearcherDetailHeaderPanel<T> pa;
			if (getNavigator() != null) {
				NavigatorPanelV6<T> na = new NavigatorPanelV6<T>("panel", getNavigator());
				pa = new SearcherDetailHeaderPanel<T>("content-top-panel", getModel(), getSiteModel(), na, isConsole);
			} else
				pa = new SearcherDetailHeaderPanel<T>("content-top-panel", getModel(), getSiteModel(), null, isConsole);
			pa.setHasToolbar(false);
			pa.setHasBreadcrumb(false);
			panel.setContentTopPanel(pa);

		} catch (Exception e) {
			logger.error(e);
			SearcherDetailHeaderPanel<T> hpanel = new SearcherDetailHeaderPanel<T>("page-content-header", getModel(), getSiteModel(), null, true);
			hpanel.add(new AttributeModifier("class", "page-header"));
			panel.setContentTopPanel(hpanel);
			mc.addOrReplace(panel);
		}
	}



	protected void addEFormLayout() {

	}

	protected boolean isAudit() {
		return isAudit && is_admin;
	}

	protected boolean isHistory() {
		return isAudit;
	}

	protected boolean isVersionControl() {
		return isAudit;
	}

	protected boolean isConsole() {
		return false;
	}

	@Override
	public boolean isFooterRequired() {
		return true;
	}

	public Searcher getSearcher() {
		return searcher;
	}

	public IModel<Cursor> getCursor() {

		if (cursor_model == null) {
			if (getSearcher() != null) {
				cursor_model = new SolrCursorModel((SolrCursor) getSearcher().getResultSet().getCursor());
				cursor_model.getObject().setIndex(index);
			}
		}
		return cursor_model;
	}

	protected List<EForm> getForms() {

		List<EForm> forms = new ArrayList<EForm>();

		if (getModelObject().getContentTemplate().getForms() == null)
			return forms;

		for (EForm form : getModelObject().getContentTemplate().getForms()) {
			if (form.getFormAccessLevel().equals(EFormAccessLevel.GENERAL) || form.getFormAccessLevel().equals(EFormAccessLevel.GENERAL_PORTAL)) {
				forms.add(new KbeeTaskForm(form));
			}
		}
		return forms;
	}


	/**
	 * Library Archive RecycleBin
	 * 
	 * @return
	 */
	private List<MenuItemFactory<Panel>> getMenuItems() {

		List<MenuItemFactory<Panel>> list = new ArrayList<MenuItemFactory<Panel>>();

		/**
		list.add(new MenuItemFactory<Panel>() {
			@Override
			public AbstractMenuItemPanelV5<Panel> getItem(String id) {
				return new AjaxMenuItemPanelV5<Panel>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						fireScanAll(new ClickSendByEmailEvent<T>(target, SearcherDetailDocumentPage.this.getModel()));
					}

					@Override
					public String getLabel() {
						return SearcherDetailDocumentPage.this.getLabel("modal.sendbyemail.title").getObject();
					}
				};
			}
		});
**/
		return list;
	}

	@Override
	public void addListeners() {
		super.addListeners();

		add(new WicketEventListener<EditableListEvent<Site>>() {
			public void onEvent(EditableListEvent<Site> event) {
				try {
					Map<String, Object> parameters = new HashMap<>();
					parameters.put("writeables", isEditableOn() ? "true" : "false");
					SearcherSiteQuery sq = new SearcherSiteQuery(getSiteModel().getObject(), getIndex(), parameters);
					SearcherResultsPage s= new SearcherResultsPage(getSiteModel(), sq);
					setResponsePage(s);
				} catch (Exception e) {
					setResponsePage(new ApplicationErrorPage<>(e));
					logger.error(e);
				}
			}
		});

		add(new WicketEventListener<ExplorerOpenEvent<Site>>() {
			public void onEvent(ExplorerOpenEvent<Site> event) {
				try {
					setResponsePage(new SearcherExplorerPage(event.getModel()));
				} catch (Exception e) {
					setResponsePage(new ApplicationErrorPage<>(e));
					logger.error(e);
				}
			}
		});

		add(new WicketEventListener<ClickBackEvent<Content>>() {
			@Override
			public void onEvent(ClickBackEvent<Content> event) {
				setResponsePage(new SearcherResultsPage(getSiteModel(), null));
			}
		});

		add(new WicketEventListener<ClickItemEvent<T>>() {
			@Override
			public void onEvent(ClickItemEvent<T> event) {
				try {
					logger.debug(event.toString());
					Content content = event.getModel().getObject();
					if (!(content instanceof IDoc)) {
						logger.error("TBA ASSUMES CONTENT IS IDOC");
						return;
					}
					SearcherDetailDocumentPage<T> page = new SearcherDetailDocumentPage<T>(new ObjectModel<T>((T) content), getSiteModel());
					Navigator<T> navigator = getNavigator();
					navigator.getCursor().setIndex(event.getIndex());
					page.setNavigator(navigator);
					setResponsePage(page);
					getPage().detach();
					return;
				} catch (Exception e) {
					logger.error(e);
					setResponsePage(new ApplicationErrorPage<T>(e));
				}
			}
		});

		add(new WicketEventListener<CursorNavigationEvent<T>>() {
			public void onEvent(CursorNavigationEvent<T> event) {
				SearcherDetailDocumentPage.this.onNavigate((T) event.getModelObject());
				event.detach();
			}
		});

		add(new WicketEventListener<ErrorEvent<?>>() {
			@Override
			public void onEvent(ErrorEvent<?> event) {
				FeedbackHelper.showErrorToast(event.getThrowable() != null ? event.getThrowable().getClass().getName() : (event.getModel() != null ? event.getModel().getObject().toString() : "Error"),
						event.getThrowable() != null ? event.getThrowable().getMessage() : (event.getModel() != null ? event.getModel().getObject().toString() : "Error"));
			}
		});

		add(new WicketEventListener<ShareContentEvent<T>>() {
			@Override
			public void onEvent(ShareContentEvent<T> event) {
				getShareModal().open(event.getRequestTarget(), event.getModel());
				event.getRequestTarget().add(SearcherDetailDocumentPage.this.mc);
			}
		});
	}

	public ShareModal<T> getShareModal() {
		if (share_modal == null) {
			share_modal = new ShareModal<T>("send-email-modal");
			mc.addOrReplace(share_modal);
		}
		return this.share_modal;
	}

	@Override
	public Navigator<T> getNavigator() {
		return navigator;
	}

	@Override
	public void setNavigator(Navigator<T> navigator) {
		this.navigator = navigator;

	}

	@Override
	public void handle(final WicketAjaxEvent event) {
		visitChildren(new IVisitor<Component, Void>() {
			@Override
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public void component(Component component, IVisit<Void> visit) {
				List<EventListenerWicket> listeners = component.getBehaviors(EventListenerWicket.class);
				for (EventListenerWicket listener : listeners) {
					if (listener.handle(event))
						listener.onEvent(event);
				}
			}
		});
		visitChildren(new IVisitor<Component, Void>() {
			@Override
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public void component(Component component, IVisit<Void> visit) {
				List<WicketEventListener> listeners = component.getBehaviors(WicketEventListener.class);
				for (WicketEventListener listener : listeners) {
					if (listener.handle(event))
						listener.onEvent(event);
				}
			}
		});
	}

	private void addNavigatorPanel() {

		if (getNavigator() == null) {
			add(new InvisiblePanel("navigator"));
			return;

		}
		try {
			NavigatorPanelV6<T> na = new NavigatorPanelV6<T>("navigator", getNavigator());
			na.setResultsPanel(true);
			add(na);
		} catch (Exception e) {
			logger.error(e);
			add(new ErrorPanel("navigator", e));
		}

	}

	private void addToolsPanel() {
		SearcherDetailToolsPanel<T> tools = new SearcherDetailToolsPanel<T>("tools", getModel(), getSiteModel());
		add(tools);
	}

	private void addBreadcrumbPanelPanel() {

		try {

			if (getSessionUser() == null) {
				add(new InvisiblePanel("breadcrumb"));
				return;
			}

			if (isStandAlonePage()) {
				add(new InvisiblePanel("breadcrumb"));
				return;
			}

			if (!hasBreadcrumb()) {
				addOrReplace(new InvisiblePanel("breadcrumb"));
				return;
			}

			MenuBreadCrumbPanel<?> bc = new MenuBreadCrumbPanel<>();

			bc.addElement(new PortalBC(getSiteModel()));
			add(bc);

		} catch (Exception e) {
			logger.error(e);
			addOrReplace(new ErrorPanel("breadcrumb", e));
		}
	}

	public boolean isStandAlonePage() {
		return isStandAlonePage;
	}

	public void setStandAlonePage(boolean tre) {
		this.isStandAlonePage = tre;
	}

	public void setHasBreadcrumb(boolean b) {
		this.has_breadcrumb = b;
	}

	public boolean hasBreadcrumb() {
		return has_breadcrumb;
	}

	public void onNavigate(T content) {
		try {
			SearcherDetailDocumentPage<T> page = new SearcherDetailDocumentPage<T>(new ObjectModel<T>(content), getSiteModel());
			if (getNavigator() != null)
				page.setNavigator(getNavigator());
			setResponsePage(page);
		} 
		catch (Exception e) {
			logger.error(e);
			setResponsePage(new ApplicationErrorPage<>(e));
		}
	}
	
	protected boolean isWriteable() {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(getModelObject());
	}

	@Override
	protected void addModals() {
		super.addModals();
	}
}
