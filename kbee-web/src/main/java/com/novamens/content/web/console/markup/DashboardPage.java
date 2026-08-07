package com.novamens.content.web.console.markup;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.document.IDoc;
import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.UserSet;
import com.novamens.content.model.UserSubset;
import com.novamens.content.user.UserService;
import com.novamens.content.web.console.markup.searchselector.AdvancedSearchButton;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.TextFilter;
import com.novamens.kbee.content.command.CommandListQuery;
import com.novamens.kbee.content.support.Tip;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.event.EmailSentEvent;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.Identifiable;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.nav.HomeBC;
import kbee.web.nav.TasksDropDownMenuBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.workflow.task.PageTaskToolbar;


@Deprecated
public class DashboardPage extends ConsolePage<Person> {
																								
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DashboardPage .class.getName());

	private static final long serialVersionUID = 1L;

	private IModel<UserSet> datasetmodel;
	
	final boolean is_support				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_security				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());

	public DashboardPage() {
		UserSet userset = getUserSet();
		IModel<UserSet> model = new ObjectModel<UserSet>(userset);
		setPageTitle( new Model<String>(model.getObject().getName()));
		setDataSetModel(model);
																			
		setTopNavigation(new GlobalNavigationBar<DataSetMember>("navigation",  getPageTitle().getObject()) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void onSearch(AjaxRequestTarget target, String text) {
				getQuery().getParameters().put("text", text);
				getQuery().getParameters().put("sort", "relevance");
				setResponsePage(getConsolePage(getQuery(), 0));
			}
			@Override
			public void onDetach() {
				super.onDetach();
				DashboardPage.this.onDetach();
			}
		});
	}

	public DashboardPage(IModel<UserSet> model) {

		setPageTitle(new Model<String>(model.getObject().getName()));
		setDataSetModel(model);
																				
		setTopNavigation(new GlobalNavigationBar<DataSetMember>("navigation",  getPageTitle().getObject()) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void onSearch(AjaxRequestTarget target, String text) {
				getQuery().getParameters().put("text", text);
				getQuery().getParameters().put("sort", "relevance");
				setResponsePage(getConsolePage(getQuery(), 0));
			}
			/**
			 * Because it's inline class
			 */
			@Override
			public void onDetach() {
				super.onDetach();
				DashboardPage.this.onDetach();
			}
		});
	}
	
	public DashboardPage(IModel<UserSet> model, Query query) {
		super(query);
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null)											 
			user.getService(PreferencesService.class).setValue( "dashboard", "page",  DashboardPage.class.getName());
		setDataSetModel(model);
	}

	public void onInitialize() {
		super.onInitialize();
		
		PageContentHeaderPanel<Content> panel=new PageContentHeaderPanel<Content>(null);
		
		panel.setTitle( new StringResourceModel("dashboard", this, null));
		panel.setBreadcrumbPanel(getContentHeaderPanelBreadcrumbPanel());
		
		panel.setMenuPanel(getContentHeaderPanelMenuPanel());

		setSearchPlaceHolder( new StringResourceModel("bc.dashboard", this, null).getObject());
		
		setSuggester(false); // Search supports suggester
		setSearchPanel(true); // include Search
		setAdvancedSearch(false); // button advanced search
		// panel.setSearchPanel(getSearchPanel());
		
		

		List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
		List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
		r_list.add(getSearchPanel("panel"));
		PageTaskToolbar<Person> toolbar = new PageTaskToolbar<Person>("toolbar", getModel(), l_list, r_list);
		panel.setToolbarPanel(toolbar);

		
		setPageContentHeader(panel);
		setLogVisit(true);		
		
		
		
		
		
		
		
		
		
		
	}
	
	
	
	
	@Override
	public void addListeners() {
		super.addListeners();
		
		
		
		add(new WicketEventListener<EmailSentEvent<IDoc>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(EmailSentEvent<IDoc> event) {
				if ((event.getModel() != null) && (event.getModel().getObject() instanceof Identifiable)) {
					FeedbackHelper.showInfoToast( event.getClass().getSimpleName(), ((Identifiable) event.getModel().getObject()).getDisplayName());
				}
				else {
					FeedbackHelper.showInfoToast(event.getClass().getSimpleName());
				}
				event.getRequestTarget().add(DashboardPage.this);
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				if (event instanceof EmailSentEvent)
					return true;
				return false;
			}
		});

		
		
		add(new WicketEventListener<OnSearchEvent>() {
			@Override
			public void onEvent(OnSearchEvent event) {
				Query q= getQuery();
				//q.getParameters().put("text", new TextFilter(event.getText()));
				q.getParameters().put("text", event.getText());
				q.getParameters().put("sort", "relevance");
				setResponsePage(new DashboardPage(getDataSetModel(), q));
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof OnSearchEvent;
			}
		});
	}	
	
	
	protected Panel getContentHeaderPanelMenuPanel() {
		return new InvisiblePanel("menu-panel");
	}
	
	protected Panel getAdvancedSearchPanel() {
		return new AdvancedSearchButton<UserSet>("advancedsearch-panel", DashboardConsole.KEY);
	}
	
	protected Panel getContentHeaderPanelBreadcrumbPanel() {
		try {
			MenuBreadCrumbPanel<?> bc =new MenuBreadCrumbPanel<Void>("breadcrumb");
 			
			bc.addElement( new HomeBC());
			
			bc.addElement(new TasksDropDownMenuBC());
 			bc.addElement(new BCElement("dashboard"));
			return bc;
		} catch (Exception e) {
			
			logger.error(e, getSessionUser().getUserName());
			return new InvisiblePanel("breadcrumb");
		}
	}

	
	
	public void setDataSetModel(IModel<UserSet> model) {
		this.datasetmodel = model;
	}
	
	public IModel<UserSet> getDataSetModel() {
		return datasetmodel;
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.TASK;
	}
	
	

	@Override
	public Console<Person> newConsole(Query query) {
		return new DashboardConsole(getDataSetModel(), query) {
			private static final long serialVersionUID = 1L;
			@Override
			public Page getConsolePage(Query query, long index) {
				return DashboardPage.this.getConsolePage(query, index);
			}
		};
	}
	
	public UserSet getUserSet() {
		UserSet userset= null;
		for (DataSet dataset : getDataSets()) {
			if (dataset instanceof UserSet && !(dataset instanceof UserSubset)) {
				userset = (UserSet)dataset;
				break;
			}
		}
		Assert.isTrue(userset!=null, "user set not found!");
		return userset;
	}



	@Override
	public boolean hasPermissions() {
		return is_domain_admin || is_root || is_support; 
	}
	
	
	/*
	@Override
	public String hasPermissionsReason() {
		StringBuilder str = new StringBuilder ();
		str.append("<p><b>Domain Admin</b> and <b>Support users</b> can access this Page. ");
		str.append("Check your <b><a class=\"btn-link\" href=\"/myaccount\" target=\"_blank\">Rights</a></b>.");
		return str.toString();
	}*/
	
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (datasetmodel!=null)
			datasetmodel.detach();
	}
	
	
	@Override
	protected String getTipCategory() {
		return Tip.GENERAL;
	}

	
	@Override
	public Page getConsolePage(Query query, long index) {
		return new DashboardPage(getDataSetModel(), query);
	}

	protected List<DataSet> getDataSets() {
		return getContentDao().getDataSets(ServiceLocator.getService(UserService.class).getDomain());
	}

}
