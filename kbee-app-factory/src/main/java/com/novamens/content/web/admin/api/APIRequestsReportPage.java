package com.novamens.content.web.admin.api;



import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.ResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.command.Command;
import com.novamens.content.entity.Person;
import com.novamens.content.web.admin.markup.datamanagement.CommandExecutionPage;
import com.novamens.content.web.sql.markup.SQLFiltersPanel;
import com.novamens.kbee.wicket.markup.html.console.browser.AjaxToolbarButton;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorPanel;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;
import kbee.web.page.FactoryPage;

/**
 * Page Header
 * Toolbar [Query Selector] 
 * SQLDataPanel
 */
@SuppressWarnings("serial")
public class APIRequestsReportPage extends ApplicationPage<Person> implements FactoryPage {
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unused")
	static private Logger logger = LogManager.getLogger( APIRequestsReportPage.class.getName());
	
	private final boolean admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	private final boolean role_service_admin   = admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
	private final boolean role_auditor		   = admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.AUDITOR.getId());
	private final boolean role_api_developer   = admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.API_DEVELOPER.getId());
	private final boolean role_factory_manager = admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId());
	private final boolean role_operations      = admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.OPERATIONS_ENGINEER.getId());
							
	
	
	public APIRequestsReportPage() {
		setPageTitle(new ResourceModel("mainmenu.domains.api"));
		Person person = getPerson();
		if (person!=null) {
			setTopNavigation(getMainTopbar());   
			setMenu(getMainLaternalMenu());      
			
			setModel(new ObjectModel<Person>(person));
			addComponents(); 
		}
		else {
			add(new ErrorPanel("info-panel", "Not authorized", ""));
		}
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		if (isDomainKbee())
			return ApplicationMenuSection.API;
		return ApplicationMenuSection.LOGS;
	}
	
	@Override
	public String hasPermissionsReason() {
		StringBuilder str = new StringBuilder();
		if (isDomainKbee()) {
			str.append("<p><b>Service Admin</b> <b>API Developer</b>can access this Page. ");
			str.append("You need <b>Service Admin</b> enabled in your <b><a class=\"btn-link\" href=\"/myaccount\" target=\"_blank\">Rights</a></b>.");
		} else {
			str.append("You need <b>Domain Admin</b> enabled in your <b><a class=\"btn-link\" href=\"/myaccount\" target=\"_blank\">Rights</a></b>.");
		}
		return str.toString();
	}
	
	/**
	 * Service Admin
	 */
	@Override
	protected boolean hasPermissions() {
		
		if (getSecurityService().isRoot())
			return true;
		
		if (isDomainKbee()) 
			return (role_service_admin || role_api_developer || role_factory_manager || role_operations || role_auditor);
		
		return admin || role_auditor;
	}

	@Override
	protected boolean isFooterRequired() {
		return false;
	}
	
	private com.novamens.service.SecurityService getSecurityService() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class);
	}
	
	private void addBreadcrumb() {
		MenuBreadCrumbPanel<?>  bc =new MenuBreadCrumbPanel<Void>();
		bc.addElement(new APIReportsBC());
		add(bc);
	}
	
	private void addComponents() {
		addBreadcrumb();
		if (hasPermissions()) {
			final APIReportSelectorPanel filters = new APIReportSelectorPanel("filters");
			SQLFiltersPanel sqlpanel = new SQLFiltersPanel("panel", filters.getQuery());
			sqlpanel.setWide(true);
			sqlpanel.addFiltersSelectorPanel(filters);
			
			List<ToolbarItem> toolbarItems = new ArrayList<ToolbarItem>();
			toolbarItems.add(new AjaxToolbarButton( null, ToolbarItem.Align.TOP_NONE, true) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					launchReprocess(filters.getQuery());
				}
				@Override
				public boolean isVisible() {
					return isDomainKbee() && getSecurityService().isRoot() || admin || role_service_admin;
				}
				
				@Override
				public boolean isEnabled() {
					return (getSecurityService().isRoot() || admin || role_service_admin);
				}
				@Override
				protected String getAnchorTitle() {
					return "Reprocess Requests";
				}
				@Override
				protected String getIcon() {
					return "far fa-forklift"; 
				}
			});
			
			toolbarItems.add(new AjaxToolbarButton( null, ToolbarItem.Align.TOP_NONE, true) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					launchClose(filters.getQuery());
				}
				@Override
				public boolean isVisible() {
					return isDomainKbee() && getSecurityService().isRoot() || admin || role_service_admin;
				}
				
				@Override
				public boolean isEnabled() {
					return (getSecurityService().isRoot() || admin || role_service_admin);
				}
				@Override
				protected String getAnchorTitle() {
					return "Mark as Closed";
				}
				@Override
				protected String getIcon() {
					return "far fa-highlighter"; 
				}
			});
			
			sqlpanel.setToolbarItems(toolbarItems);
			
			add(sqlpanel);
		}
		else
			add(new ErrorPanel("panel", "Not authorized", ""));
	}
	
	private void launchReprocess(String statement) {
		Command reprocesscommand = (Command)ServiceLocator.getService(BeansService.class).getBean("ApiReprocessCommand");
		if (reprocesscommand==null) 
			return;
		int criteriaindex = statement.indexOf("from");
		statement = "SELECT * " + statement.substring(criteriaindex);
		reprocesscommand.setParameter("statement", statement);
		setResponsePage(new CommandExecutionPage(reprocesscommand));
	}
	
	private void launchClose(String statement) {
		Command closecommand = (Command)ServiceLocator.getService(BeansService.class).getBean("ApiCloseEventsCommand");
		if (closecommand==null) 
			return;
		int criteriaindex = statement.indexOf("where");
		String criteria = statement.substring(criteriaindex+6);
		int orderindex = criteria.indexOf("order");
		criteria = criteria.substring(0, orderindex).trim();
		closecommand.setParameter("criteria", criteria);
		closecommand.setParameter("value", "true");
		setResponsePage(new CommandExecutionPage(closecommand));
	}

}
