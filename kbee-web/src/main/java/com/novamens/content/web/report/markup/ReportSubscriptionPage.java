package com.novamens.content.web.report.markup;



import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.report.ReportsDropdownBC;

public class ReportSubscriptionPage extends ApplicationPage<Person> {
																							
	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReportSubscriptionPage.class.getName());
	
	private static final long serialVersionUID = 1L;

	private boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	private boolean role_support = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());

	/**
	 * 
	 */
	public ReportSubscriptionPage() {
        super();
        setLogVisit(true);
        if (getPerson()!=null)
        	setModel(new ObjectModel<Person>(getPerson()));
    }

    @Override
    public void onInitialize() {
        super.onInitialize();
        
		setTopNavigation(getMainTopbar()); 	
		setMenu(getMainLaternalMenu()); 	
		
		PageContentHeaderPanel<Void> panel=new PageContentHeaderPanel<Void>(null);
		panel.setTitle(new StringResourceModel("bc.reportsubscription", this, null));
		panel.setBreadcrumbPanel(getHeaderPanelBreadcrumbPanel());
		setSearchPanel(false);
		setClearAllSearch(false);
		setAdvancedSearch(false);
		setSuggester(false);
		setPageContentHeader(panel);
		
        if (hasPermissions()) 
        	add(new ReportSubscriptionMainPanel(null));
        else
        	add(new InvisiblePanel("editor"));
    }

    
    protected Panel getHeaderPanelBreadcrumbPanel() {
		try {
			MenuBreadCrumbPanel<?>  bc =new MenuBreadCrumbPanel<>();
			bc.addElement(new ReportsDropdownBC());
			bc.addElement(new BCElement(new StringResourceModel("bc.reportsubscription", this, null)));
			return bc;
		} catch (Exception e) {
			return new InvisiblePanel("breadcrumb");
		}
	}

    protected Person getPerson() {
        return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
    }
    
	@Override
	public boolean hasPermissions() {
		if (this.role_support || this.role_admin) 
			return true;
		boolean role_reports = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.REPORTS.getId());
		return role_reports;
	}

}
