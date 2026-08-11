package com.novamens.content.web.admin.api;


import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.apache.wicket.model.ResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.content.web.sql.markup.SQLFiltersPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.error.ErrorPanel;
import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;

public class APIStatsReportPage extends ApplicationPage<Person> {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger( APIStatsReportPage .class.getName());

	
	private final boolean admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	private final boolean role_service_admin   = admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
	private final boolean role_api_developer   = admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.API_DEVELOPER.getId());
	private final boolean role_factory_manager = admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId());
	private final boolean operations   = admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.OPERATIONS_ENGINEER.getId());
						
	
	public APIStatsReportPage() {
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
			return ApplicationMenuSection.API;
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
	
	@Override
	protected boolean hasPermissions() {
		
		if (getSecurityService().isRoot())
			return true;
		
		if (isDomainKbee()) 
			return (role_service_admin || role_api_developer || role_factory_manager ||  operations);
		
		return admin;
	}

	@Override
	protected boolean isFooterRequired() {
		return false;
	}
	
	private com.novamens.service.SecurityService getSecurityService() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class);
	}
	
	private void addBreadcrumb() {
		MenuBreadCrumbPanel<?>  bc =new MenuBreadCrumbPanel<>();
		bc.addElement(new APIReportsBC());
		add(bc);
	}
	
	private void addComponents() {
		addBreadcrumb();
		if (hasPermissions()) {
			
			String sql = "select ts \"Date (day + hour)\", total \"Total\",  mean_time_total \"mean time (ms)\", total_post \"POST\",  mean_time_post \"POST mean time (ms)\",  totdel \"DEL\", meantimedel \"DEL mean time (ms)\",  total_bounced \"Total Bounced\" from kb_api_usage_stat order by ts desc limit 720";
			SQLFiltersPanel sqlpanel = new SQLFiltersPanel("panel", sql);
			sqlpanel.setWide(false);
			add(sqlpanel);
			
			// 120 days totals
			String start_date = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now().minusDays(120).truncatedTo(ChronoUnit.DAYS));   //;" 00:00:00.000";
			String end_date = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS));   //;" 00:00:00.000";
			String d_sql="select date(event_time) \"Date (day)\", "
					
					
					+ " to_char(count(*), '99,999,999' ) \"TOTAL\",  "
					+ " to_char(avg(event_processing_time), '99,999' ) \"AVG TIME (ms)\",  "
					
					+ " to_char(count(event_request='POST'),   '99,999,999' ) \"TOTAL POST\",  "
					+ " to_char(count(event_request like 'DEL%'),   '99,999,999' ) \"TOTAL DEL\"  "
					
					+ " from api_logevent where (event_time >= '"+start_date+"' and event_time < '"+end_date+"') group by date(event_time) order by date(event_time) desc limit 120 ";
			
			logger.debug(d_sql);
			
			SQLFiltersPanel d_sqlpanel = new SQLFiltersPanel("day-panel", d_sql);
			d_sqlpanel.setWide(false);
			add(d_sqlpanel);

			
			
			
		}
		else {
			add(new ErrorPanel("panel", "Not authorized", ""));
			add(new InvisiblePanel("day-panel"));
		}
		
		
	}
}
