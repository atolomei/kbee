package com.novamens.content.web.workflow.markup.batch;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Page;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import com.novamens.content.base.Content;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.document.IDoc;

import com.novamens.content.model.UserSet;
import com.novamens.content.resource.KBFile;
import com.novamens.content.user.UserService;
import com.novamens.content.web.console.markup.DashboardPage;
import com.novamens.content.web.console.markup.ErrorPanel;
import com.novamens.content.web.console.markup.searchselector.AdvancedSearchButton;
import com.novamens.dom.DomainType;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.TextFilter;
import com.novamens.kbee.content.domain.provisioning.DomainModelBuilderService;
import com.novamens.kbee.wicket.markup.html.event.EventHandler;
import com.novamens.kbee.wicket.markup.html.event.EventListenerWicket;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketAjaxEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.BrandingService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorNotAuthorizedPanel;
import kbee.web.nav.HomeBC;
import kbee.web.nav.TasksDropDownMenuBC;
import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.query.ListModelQuery;


public class TaskBatchCreatePage<T extends Content> extends ApplicationPage<T> implements EventHandler {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TaskBatchCreatePage.class.getName());
	
	public final boolean IS_KBEE_VERSION =  ServiceLocator.getService(BrandingService.class).getProductKey().equals("kbee");
	
	protected static final ResourceReference BL = new CssResourceReference(Form.class, "build.css");
	protected static final ResourceReference BS = new CssResourceReference(Form.class, "bootstrap-select.css");
	protected static final ResourceReference BSJS = new JavaScriptResourceReference(Form.class, "bootstrap-select.js");
	
	final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	
	final boolean role_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	//final boolean bulk_create = role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.WORKSPACE_BULK_ACTIONS.getId());
	final boolean role_pending = role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.PENDING_TASKS.getId());

	/**
	 * 
	 */
	public TaskBatchCreatePage() {
	}
	
	@Override 
	public void onInitialize() {
		super.onInitialize();

		try {
			
			if (hasPermissions()) {
				getDomain().getService(DomainModelBuilderService.class).buildResourcesContentTemplate();
				addComponents();
				setLogVisit(true);
			}
			else {
				add((new WebMarkupContainer("navigation")).setVisible(false));
				// add( new ErrorPanel("console", new Model<String>("Not authorized"), 
				// new Model<String>("Please log in to access this page")));
				add(new ErrorNotAuthorizedPanel<>("console"));
				
			}
		} catch (Exception e) {
			logger.error(e);
			add((new WebMarkupContainer("navigation")).setVisible(false));
			setTopNavigation(getMainTopbar());  
			setMenu(getMainLaternalMenu());
			add( new ErrorPanel("console", new Model<String>(e.getClass().getName()), new Model<String>(e.getMessage())));
		}
	}


	protected String getConsoleName() {
		return getPageTitle().getObject();
	}
	
	
	
	protected Panel getContentHeaderPanelMenuPanel() {
		return new InvisiblePanel("menu-panel");
	}
	
	protected Panel getAdvancedSearchPanel() {
		return new AdvancedSearchButton<UserSet>("advancedsearch-panel", FileUploadAndCreateConsole.KEY);
	}
	
	protected Panel getContentHeaderPanelBreadcrumbPanel() {
		try {
			MenuBreadCrumbPanel bc =new MenuBreadCrumbPanel();
			bc.addElement( new HomeBC());
			bc.addElement(new TasksDropDownMenuBC());
			bc.addElement(new BCElement("taskbatchcreation"));
			return bc;
		} catch (Exception e) {
			logger.error(e, getSessionUser().getUserName());
			return new InvisiblePanel("breadcrumb");
		}
	}

	
	protected void addComponents() {
		try {
																										
			
			setPageTitle(new StringResourceModel("title", this, null));
			setPageDescription(getPageTitle());

			setTopNavigation(getMainTopbar());  
			setMenu(getMainLaternalMenu());
			
			PageContentHeaderPanel<Content> panel=new PageContentHeaderPanel<Content>(null);

			panel.setTitle(new StringResourceModel("bc.bulkupload", this, null));
			panel.setBreadcrumbPanel(getContentHeaderPanelBreadcrumbPanel());
			panel.setMenuPanel(getContentHeaderPanelMenuPanel());
			
			setSearchPlaceHolder(new StringResourceModel("search-in-bulk", TaskBatchCreatePage.this, null).getObject());
			setSuggester(false); 
			setSearchPanel(false); 
			setAdvancedSearch(false); 
			panel.setSearchPanel(getSearchPanel());
			setPageContentHeader(panel);
		 
		} catch (Exception e) {
			logger.error(e);
			add((new WebMarkupContainer("navigation")).setVisible(false));
			add(new InvisiblePanel("breadcrumb"));
		}
		 
		try {
			
			IDoc idoc = ServiceLocator.getService(UserService.class).getUploadAndCreateContainer();
			IModel<IDoc> idoc_model =  new ObjectModel<IDoc>(idoc);
			
			List<KBFile> list = idoc.getFiles();
			List<IModel<KBFile>> list_model = new ArrayList<IModel<KBFile>>();
			
			for (KBFile file: list) 
				list_model.add(new ObjectModel<KBFile>(file));
			
			ListModelQuery<KBFile> query = new ListModelQuery<KBFile>(list_model);
			
			add( new FileUploadAndCreateConsole(query, idoc_model) {
				private static final long serialVersionUID = 1L;
				@Override
				public Page getConsolePage(Query query, long index) {
					return new TaskBatchCreatePage<T>();
				}
			});
			
		} catch (ContentMgmtException e) {
			logger.error(e.getMessage());
			add( new ErrorPanel("console", new Model<String>("Files Container Error"), new Model<String>(e.getMessage())));
		}

	}
	
	
	@Override
	public void addListeners() {
		super.addListeners();

		/**
		add(new WicketEventListener<OnSearchEvent>() {
			@Override
			public void onEvent(OnSearchEvent event) {
				//Query q=  getQuery();
				//q.getParameters().put("text", new TextFilter(event.getText()));
				//q.getParameters().put("sort", "relevance");
				//setResponsePage(new DashboardPage(getDataSetModel(), q));
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof OnSearchEvent;
			}
		});
		**/
	}	
	
	@Override 
	protected boolean hasPermissions() {
		
		if (getDomain().getDomainType()==DomainType.EXPRESS)
			return is_root;

		return true;
	}

	
	/** 
	 * Falta ver si el evento debe ser tomado por la pagina ?
	 */
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
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.TASK;
	}
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(BL));
		response.render(CssHeaderItem.forReference(BS));
		response.render(JavaScriptHeaderItem.forReference(BSJS));
	}
	
	/**
	public Query newQuery() {
		try {
			List<KBFile> list = this.getContainerModel().getObject().getFiles();
			this.list_model.clear();
			for (KBFile file: list) 
				this.list_model.add(new ObjectModel<KBFile>(file));
			ListModelQuery<KBFile> query = new ListModelQuery<KBFile>(this.list_model);
			return setUserPreference(query);
			
		} catch (Exception e) {
			logger.error(e);
			return 	new ListModelQuery<KBFile>(this.list_model);
		}
	}*/
}
