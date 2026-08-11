package kbee.web.security.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.content.model.UserSet;
import com.novamens.content.user.UserService;
import com.novamens.content.web.console.markup.ErrorPanel;
import com.novamens.content.web.console.markup.searchselector.AdvancedSearchButton;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.content.support.Tip;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.nav.HomeBC;
import kbee.web.nav.SecurityDropDownMenuBC;

import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.workflow.task.PageTaskToolbar;

@SuppressWarnings("serial")
public class UsersPage extends ConsolePage<Person> {
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UsersPage.class.getName());
	private IModel<UserSet> datasetmodel;
	
	final boolean is_support		= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_root			= ServiceLocator.getService(SecurityService.class).isRoot(); 
	final boolean is_domain_admin	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_security		= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
	final boolean is_useradmin		= ServiceLocator.getService(UserService.class).isUserAdmin();
	
	public UsersPage() {
		setDataSetModel(new ObjectModel<UserSet>(getUserSet()));
	}
	
	public UsersPage(IModel<UserSet> model) {
		setDataSetModel(model);
	}
	
	public UsersPage(IModel<UserSet> model, Query query) {
		super(query);
		setDataSetModel(model);
	}

	protected Panel getContentHeaderPanelMenuPanel() {
		return new InvisiblePanel("menu-panel");
	}
	
	protected Panel getAdvancedSearchPanel() {
		return new AdvancedSearchButton<UserSet>("advancedsearch-panel", UsersConsole.KEY);
	}
	
	protected Panel getContentHeaderPanelBreadcrumbPanel() {
		
		MenuBreadCrumbPanel<?>  bc =new MenuBreadCrumbPanel<Void>("breadcrumb");
		
		bc.addElement( new HomeBC());
		bc.addElement(new SecurityDropDownMenuBC());
		
		bc.addElement(new BCElement("bc.users"));
		return bc;
	}
	
	
	/**
	 * 
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();

		try {
			setPageTitle(new StringResourceModel("bc.users", this, null));

			PageContentHeaderPanel<UserSet> panel=new PageContentHeaderPanel<UserSet>(datasetmodel);
			panel.setTitle(new StringResourceModel("bc.users", this, null));
			panel.setBreadcrumbPanel(getContentHeaderPanelBreadcrumbPanel());
			setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.users", this, null).getObject()));
			
			setSearchPanel(true);
			setAdvancedSearch(true);
			setSuggester(false);
			
			
			// panel.setSearchPanel(getSearchPanel());
			
			
			List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
			List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
			Panel s=getSearchPanel("panel");
			r_list.add(s);
			PageTaskToolbar<Person> toolbar = new PageTaskToolbar<Person>("toolbar", getModel(), l_list, r_list);
			panel.setToolbarPanel(toolbar);
			
			setPageContentHeader(panel);
			
			
			
		} 
		catch (Exception e) {
			logger.error(e);
			addOrReplace( new ErrorPanel("console", new Model<String>(e.getClass().getName()), 
					new Model<String>( this.getClass().getName() + " | " + e.getMessage() + " | " + e.getCause())));
			setTopNavigation(new InvisiblePanel("navigation"));
		}
	}
	
	@Override
	public void addListeners() {
		super.addListeners();
		add(new WicketEventListener<OnSearchEvent>() {
			@Override
			public void onEvent(OnSearchEvent event) {
					getQuery().getParameters().put("text", event.getText());
					getQuery().getParameters().put("sort", "relevance");
					setResponsePage(getConsolePage(getQuery(), 0));
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof OnSearchEvent;
			}
		});
	}

	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SECURITY;
	}

	public void setDataSetModel(IModel<UserSet> model) {
		this.datasetmodel = model;
	}
	
	public IModel<UserSet> getDataSetModel() {
		return datasetmodel;
	}

	@Override
	public Console<Person> newConsole(Query query) {
		return new UsersConsole(getDataSetModel(), query) {
			@Override
			public Page getConsolePage(Query query, long index) {
				return UsersPage.this.getConsolePage(query, index);
			}
		};
	}
	
	public UserSet getUserSet() {
		UserSet userset = getContentDao().getUserSet();
		if (userset==null)
			logger.error("UserSet is null for domain " + getDomain().getName());
		return userset;
	}
	
	@Override
	public boolean hasPermissions() {
		return is_domain_admin || is_root || is_security || is_support || is_useradmin; 
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (datasetmodel!=null)
			datasetmodel.detach();
	}

	@Override
	protected String getTipCategory() {
		return Tip.SECURITY;
	}
	
	@Override
	public Page getConsolePage(Query query, long index) {
		return new UsersPage(getDataSetModel(), query);
	}
}
