package kbee.web.source;

import com.novamens.content.base.Source;
import com.novamens.content.security.Role;
import com.novamens.dom.DomainType;
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
import kbee.web.query.SourcesQuery;
import kbee.web.workflow.task.PageTaskToolbar;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class SourcesPage extends ConsolePage<Source> {
	private static final long serialVersionUID = 1L;

	final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean is_domain_admin			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_support				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());

	public SourcesPage(PageParameters parameters) {
		setPagePreferences();
	}

	public SourcesPage() {
		setPagePreferences();
	}

	
	public void onInitialize() {
		super.onInitialize();
		
		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());
		
		MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<>();
 		
		bc.addElement( new HomeBC());
		bc.addElement( new SettingsDropDownBC());
		bc.addElement(new BCElement("bc.sources"));
		setPageTitle(new StringResourceModel("bc.sources", this, null));
		PageContentHeaderPanel<?> panel=new PageContentHeaderPanel<>();
		panel.setTitle(new StringResourceModel("bc.sources", this, null));
		panel.setBreadcrumbPanel(bc);
		setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.sources", this, null).getObject()));
		setSearchPanel(true);
		setAdvancedSearch(true);
		setSuggester(false);
		// panel.setSearchPanel(getSearchPanel());
		
		List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
		List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
		r_list.add(getSearchPanel("panel"));
		PageTaskToolbar<Source> toolbar = new PageTaskToolbar<Source>("toolbar", getModel(), l_list, r_list);
		panel.setToolbarPanel(toolbar);
		setPageContentHeader(panel);

	}
	public SourcesPage(Query query) {
		super(query);
		setPagePreferences();
	}

	@Override
	public void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<OnSearchEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(OnSearchEvent event) {
				Query q=new SourcesQuery();
				q.getParameters().put("text", event.getText());
				q.getParameters().put("sort", "relevance");
				setResponsePage(new SourcesPage(q));
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
	public Console<Source> newConsole(Query query) {
		return new SourcesConsole(query) {
			@Override
			public Page getConsolePage(Query query, long index) {
				return SourcesPage.this.getConsolePage(query, index);
			}

			@Override
			protected boolean hasIcon(IModel<Source> model) {
				return false;
			}

			@Override
			protected String getIcon(IModel<Source> model) {
				return "";
			}
		};
	}

	@Override
	protected String getTipCategory() {
		return Tip.GENERAL;
	}
 
	@Override
	public Page getConsolePage(Query query, long index) {
		return new SourcesPage(query);
	}
	 
	@Override
	public boolean hasPermissions() {

		//if (getDomain().getDomainType()==DomainType.FREE)
		//	return is_root;

		return (isExpressVersion() && is_root) || (is_domain_admin || is_root  || is_support);  
	}
	
	private void setPagePreferences() {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null) 
			user.getService(PreferencesService.class).setValue( "settings", "security",  getClass().getSimpleName());
	}
}
