package kbee.web.enoti;



import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.base.Content;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.enoti.ENotiRule;
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
import kbee.web.nav.AlertManagementDropDownBC;
import kbee.web.nav.HomeBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.query.ENotiRulesQuery;
import kbee.web.workflow.task.PageTaskToolbar;

public class ENotiRulesPage extends ConsolePage<ENotiRule> {
				
	private static final long serialVersionUID = 1L;

	final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_support				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
		
	
	public ENotiRulesPage(PageParameters parameters) {
	}
	
	public ENotiRulesPage() {
	}

	
	public ENotiRulesPage(Query query) {
		super(query);
	}

	
	@Override
	protected String getConsoleName() {
		return "mainmenu.enotirules";
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setPageTitle(getLabel("mainmenu.enotirules"));
		
		setPagePreferences();
	 
		setTopNavigation(getMainTopbar()); 
		setMenu(getMainLaternalMenu());
	
		PageContentHeaderPanel<EmailTemplate> panel=new PageContentHeaderPanel<EmailTemplate>();
		MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<>();
		bc.addElement( new HomeBC());
		bc.addElement( new AlertManagementDropDownBC());
		bc.addElement(new BCElement("bc.enoti"));
		panel.setBreadcrumbPanel(bc);
		
		setPageTitle(new StringResourceModel("bc.enoti", this, null));
		panel.setTitle(new StringResourceModel("bc.enoti", this, null));
		setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.enoti", this, null).getObject()));
		setSearchPanel(true);
		setAdvancedSearch(false);
		setSuggester(false);
		// panel.setSearchPanel(getSearchPanel());
		
		
		List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
		List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
		r_list.add(getSearchPanel("panel"));
		PageTaskToolbar<ENotiRule> toolbar = new PageTaskToolbar<ENotiRule>("toolbar", getModel(), l_list, r_list);
		panel.setToolbarPanel(toolbar);

		
		setPageContentHeader(panel);
		
	}
	
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.ALERT_SETTINGS;
	}

	@Override
	public Console<ENotiRule> newConsole(Query query) {
		return new ENotiRuleConsole(query) {
			private static final long serialVersionUID = 1L;
			@Override
			public Page getConsolePage(Query query, long index) {
				return ENotiRulesPage.this.getConsolePage(query, index);
			}
			
		};
	}
	

	@Override
	public void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<OnSearchEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(OnSearchEvent event) {
				Query q=new ENotiRulesQuery();
				q.getParameters().put("text", event.getText());
				q.getParameters().put("sort", "relevance");
				setResponsePage(new ENotiRulesPage(q));
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof OnSearchEvent;
			}
		});
	}

	
	@Override
	protected String getTipCategory() {
		return Tip.GENERAL;
	}
 
	@Override
	public Page getConsolePage(Query query, long index) {
		return new ENotiRulesPage(query);
	}
	 
	@Override
	public boolean hasPermissions() {

		//if (getDomain().getDomainType()==DomainType.FREE)
		//	return is_root;

		return is_root || (is_domain_admin || is_support);  
	}
	
	private void setPagePreferences() {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null) 
			user.getService(PreferencesService.class).setValue( "settings", "enotirules",  getClass().getSimpleName());
	}

}
