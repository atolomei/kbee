package kbee.web.multidimensional;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.security.Role;
import com.novamens.dom.DomainType;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.content.support.Tip;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.nav.HomeBC;
import kbee.web.nav.SettingsDropDownBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.query.FacetsQuery;
import kbee.web.workflow.task.PageTaskToolbar;

@SuppressWarnings("serial")
public class FacetsPage extends ConsolePage<Facet> {
	
	private static final long serialVersionUID = 1L;

	final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_support				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
				
	public FacetsPage(PageParameters parameters) {
	}
	
	public FacetsPage() {
	}

	
	public FacetsPage(Query query) {
		super(query);
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
					
		setPageTitle(getLabel("bc.facets"));

		MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<Void>();
		bc.addElement( new HomeBC());
		bc.addElement( new SettingsDropDownBC());
		bc.addElement(new BCElement("bc.facets"));

		PageContentHeaderPanel<?> panel=new PageContentHeaderPanel<>();
		panel.setBreadcrumbPanel(bc);
		
		setPageTitle(new StringResourceModel("bc.facets", this, null));
		panel.setTitle(new StringResourceModel("bc.facets", this, null));
		setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.facets", this, null).getObject()));
		setSearchPanel(true);
		setAdvancedSearch(false);
		setSuggester(false);
		//panel.setSearchPanel(getSearchPanel());
		
		
		List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
		List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
		r_list.add(getSearchPanel("panel"));
		PageTaskToolbar<Facet> toolbar = new PageTaskToolbar<Facet>("toolbar", getModel(), l_list, r_list);
		panel.setToolbarPanel(toolbar);
		
		
		setPageContentHeader(panel);

		
		

		
		
		
		
		setPagePreferences();

		/**
		GlobalNavigationBar<Facet> nav = new GlobalNavigationBar<Facet>("navigation", getPageTitle().getObject(), false) {
			@Override
			protected void onSearch(AjaxRequestTarget target, String text) {
				getQuery().getParameters().put("text", text);
				getQuery().getParameters().put("sort", "relevance");
				setResponsePage(getConsolePage(getQuery(), 0));
			}
			@Override
			public void onDetach() {
				super.onDetach();
				FacetsPage.this.onDetach();
			}
		};**/
		// setMenu(new NavBarLateralMenu("menu", getApplicationMenuSection().getKey()));
		//setNavigation(nav);
		
		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());  

		
		
		
		
		
		
		
		
		
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SETTINGS;
	}

	@Override
	public void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<OnSearchEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(OnSearchEvent event) {
				Query q=new FacetsQuery();
				q.getParameters().put("text", event.getText());
				q.getParameters().put("sort", "relevance");
				setResponsePage(new FacetsPage(q));
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof OnSearchEvent;
			}
		});
	}

	
	@Override
	public Console<Facet> newConsole(Query query) {
		return new FacetsConsole(query) {
			@Override
			public Page getConsolePage(Query query, long index) {
				return FacetsPage.this.getConsolePage(query, index);
			}

			
		};
	}

	@Override
	protected String getTipCategory() {
		return Tip.GENERAL;
	}
 
	@Override
	public Page getConsolePage(Query query, long index) {
		return new FacetsPage(query);
	}
	 
	@Override
	public boolean hasPermissions() {

		if (getDomain().getDomainType()==DomainType.EXPRESS)
			return is_root;

		return is_root || (is_domain_admin || is_support);  
	}
	
	private void setPagePreferences() {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null) 
			user.getService(PreferencesService.class).setValue( "settings", "security",  getClass().getSimpleName());
	}
}
