package kbee.web.library;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.email.EmailTemplate;
import com.novamens.content.library.Library;
import com.novamens.content.security.Role;
import com.novamens.dom.DomainType;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.content.support.Tip;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.nav.HomeBC;
import kbee.web.nav.SettingsDropDownBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.query.LibrariesQuery;
import kbee.web.workflow.task.PageTaskToolbar;

@SuppressWarnings("serial")
public class LibrariesPage extends ConsolePage<Library> {
				
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(LibrariesPage.class.getName());
	
	final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_support				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
				
	public LibrariesPage(PageParameters parameters) {
	}
	
	public LibrariesPage() {
	}
	
	
	public LibrariesPage(Query query) {
		super(query);

	}
	
	@Override
	public void onInitialize() {
		long start = System.currentTimeMillis();
		try {
			super.onInitialize();
			setTopNavigation(getMainTopbar());  
			setMenu(getMainLaternalMenu());  
			
			setLogVisit(true);

			
			PageContentHeaderPanel<EmailTemplate> panel=new PageContentHeaderPanel<EmailTemplate>();
			MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<Void>();
			bc.addElement( new HomeBC());
			
			bc.addElement( new SettingsDropDownBC());
			bc.addElement(new BCElement("bc.libraries"));
	
			panel.setBreadcrumbPanel(bc);
			setPageTitle(new StringResourceModel("bc.libraries", this, null));
			panel.setTitle(new StringResourceModel("bc.libraries", this, null));
			setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.libraries", this, null).getObject()));
			setSearchPanel(true);
			setAdvancedSearch(false);
			setSuggester(false);
			// panel.setSearchPanel(getSearchPanel());
			
			List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
			List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
			r_list.add(getSearchPanel("panel"));
			PageTaskToolbar<Library> toolbar = new PageTaskToolbar<Library>("toolbar", getModel(), l_list, r_list);
			panel.setToolbarPanel(toolbar);
			
			
			setPageContentHeader(panel);
		} finally {
			logger.debug("initialize: " + String.valueOf(System.currentTimeMillis()-start) + " ms ");
		}
	}
	
	@Override
	public void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<OnSearchEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(OnSearchEvent event) {
				Query q=new LibrariesQuery();
				q.getParameters().put("text", event.getText());
				q.getParameters().put("sort", "relevance");
				setResponsePage(new LibrariesPage(q));
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof OnSearchEvent;
			}
		});
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SETTINGS;
	}

	@Override
	public Console<Library> newConsole(Query query) {
		return new LibrariesConsole(query) {
			@Override
			public Page getConsolePage(Query query, long index) {
				return LibrariesPage.this.getConsolePage(query, index);
			}
		};
	}

	@Override
	protected String getTipCategory() {
		return Tip.GENERAL;
	}
 
	@Override
	public Page getConsolePage(Query query, long index) {
		return new LibrariesPage(query);
	}
	 
	@Override
	public boolean hasPermissions() {

		if (getDomain().getDomainType()==DomainType.EXPRESS)
			return is_root;

		return (isExpressVersion() && is_root) || (is_domain_admin || is_root  || is_support);  
	}

	/**
	private void setPagePreferences() {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null) 
			user.getService(PreferencesService.class).setValue( "settings", "security",  getClass().getSimpleName());
	}**/
}
