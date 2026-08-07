package com.novamens.content.web.security.markup;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.security.IQLRule;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.content.support.Tip;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.HomeBC;
import kbee.web.nav.RolesBC;
import kbee.web.nav.RulesBC2;
import kbee.web.nav.SecurityBC;
import kbee.web.nav.UsersBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;

@SuppressWarnings("serial")
public class RulesPage extends ConsolePage<IQLRule> {
	private static final long serialVersionUID = 1L;

	final boolean is_root			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_sec			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
	final boolean is_support		= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
				
	public RulesPage(PageParameters parameters) {
		//setPagePreferences();
	}
	
	public RulesPage() {
		//setPagePreferences();


		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());

		
		
	}
	
	
	public void onInitialize() {
		super.onInitialize();
		
		setPageTitle(new StringResourceModel("bc.rules", this, null));

		PageContentHeaderPanel<IQLRule> panel=new PageContentHeaderPanel<IQLRule>(getModel());
		panel.setTitle(new StringResourceModel("bc.rules", this, null));
		panel.setBreadcrumbPanel(getContentHeaderPanelBreadcrumbPanel());
		
		
		setSearchPlaceHolder(new StringResourceModel("bc.rules", this, null).getObject());
		setSearchPanel(true);
		setAdvancedSearch(false);
		setSuggester(false);
		panel.setSearchPanel(getSearchPanel());
		
		setPageContentHeader(panel);
	}
	
	protected Panel getContentHeaderPanelBreadcrumbPanel() {
		MenuBreadCrumbPanel<?>  bc =new MenuBreadCrumbPanel<Void>("breadcrumb");
		bc.addElement( new HomeBC());
		DropDownMenuBC<?> dd = new DropDownMenuBC<Void>();
		dd.addElement(new SecurityBC(), true);
		dd.addElement(new UsersBC());
		dd.addElement(new RolesBC());
		dd.addElement(new RulesBC2());
		bc.addElement(dd);
		bc.addElement(new BCElement("bc.users"));
		return bc;
	}
	
	public RulesPage(Query query) {
		super(query);
		//setPagePreferences();
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SETTINGS;
	}

	@Override
	public Console<IQLRule> newConsole(Query query) {
		return new RulesConsole(query) {
			@Override
			public Page getConsolePage(Query query, long index) {
				return RulesPage.this.getConsolePage(query, index);
			}
		};
	}

	@Override
	protected String getTipCategory() {
		return Tip.SECURITY;
	}
 
	@Override
	public Page getConsolePage(Query query, long index) {
		return new RulesPage(query);
	}
	 
	@Override
	public boolean hasPermissions() {
		return (isExpressVersion() && is_root) || (is_domain_admin || is_root || is_sec || is_support);  
	}
}
