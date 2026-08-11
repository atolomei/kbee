package kbee.web.rule;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.rule.ActionRule;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.content.support.Tip;
import com.novamens.kbee.security.KbeeUser;
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
import kbee.web.workflow.task.PageTaskToolbar;

@SuppressWarnings("serial")
public class ActionRulesPage extends ConsolePage<ActionRule> {
	private static final long serialVersionUID = 1L;

	final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_support				= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
				
	public ActionRulesPage(PageParameters parameters) {
		setPagePreferences();
	}
	
	public ActionRulesPage() {
		setPagePreferences();
		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());

	}
	
	public ActionRulesPage(Query query) {
		super(query);
		setPagePreferences();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setPageTitle(new StringResourceModel("bc.actionrules", this, null));

		PageContentHeaderPanel<ActionRule> panel=new PageContentHeaderPanel<ActionRule>();
		panel.setTitle(new StringResourceModel("bc.actionrules", this, null));
		
		
		// panel.setBreadcrumbPanel(new DataManagementPanelBC("bc.actionrules"));
		MenuBreadCrumbPanel<ActionRule>  bc = new MenuBreadCrumbPanel<ActionRule>();
		
		bc.addElement(new HomeBC());
		bc.addElement( new AlertManagementDropDownBC());
		bc.addElement(new BCElement("time-based-alerts"));
		panel.setBreadcrumbPanel(bc);

		
		setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject().replace("{0}", new StringResourceModel("bc.actionrules", this, null).getObject()));
		
		setSearchPanel(true);
		setAdvancedSearch(false);
		setSuggester(false);
		// panel.setSearchPanel(getSearchPanel());
		
		List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
		List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
		r_list.add(getSearchPanel("panel"));
		PageTaskToolbar<ActionRule> toolbar = new PageTaskToolbar<ActionRule>("toolbar", getModel(), l_list, r_list);
		panel.setToolbarPanel(toolbar);

		
		setPageContentHeader(panel);
	}
	
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.ALERT_SETTINGS;
	}

	@Override
	public Console<ActionRule> newConsole(Query query) {
		return new ActionRulesConsole(query) {
			@Override
			public Page getConsolePage(Query query, long index) {
				return ActionRulesPage.this.getConsolePage(query, index);
			}
		};
	}

	@Override
	protected String getTipCategory() {
		return Tip.GENERAL;
	}
 
	@Override
	public Page getConsolePage(Query query, long index) {
		return new ActionRulesPage(query);
	}
	
	@Override
	public Page getConsolePage() {
		return new ActionRulesPage();
	}
	
	@Override
	public boolean hasPermissions() {
		return (isExpressVersion() && is_root) || (is_domain_admin || is_root  || is_support);  
	}
	
	private void setPagePreferences() {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null) 
			user.getService(PreferencesService.class).setValue( "settings", "security",  getClass().getSimpleName());
	}
}
