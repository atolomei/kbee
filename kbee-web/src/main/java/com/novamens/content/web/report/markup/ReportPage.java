package com.novamens.content.web.report.markup;

import java.util.List;

import org.apache.wicket.Page;

import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.content.user.UserService;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.Query;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.report.ReportConsole;
import kbee.web.report.ReportFactory;
import kbee.web.report.Row;
import kbee.web.service.ReportsLibraryService;

import org.apache.wicket.request.mapper.parameter.PageParameters;


public class ReportPage extends ConsolePage<Row> {
			
	private static final long serialVersionUID = 1L;
							
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReportPage.class.getName());
	
	private ReportFactory factory = null;

	
	public ReportPage() {
		this(new PageParameters());
	}

	
	public ReportPage(PageParameters pageParameters) {
		super(null);

		String reportGroup = pageParameters.get("reportGroup").toOptionalString();
		
		// aca extraer el id de parameters y buscar el reporte
		
		List<ReportFactory> factories = ServiceLocator.getService(ReportsLibraryService.class).getReports(getDomain());
		
		if (!factories.isEmpty()) {
			
			if(reportGroup != null) {
				
				String last = pageParameters.get("reportKey").toOptionalString();
				
				if (last==null)
						last=getUserPreference("report-" + reportGroup);
				
				if (last==null) {
					factory = getFirstAllowedFactory(reportGroup, factories);
				}
				else {
					for (ReportFactory rf: factories) {
						if (rf.getReport().getReportGroup().equals(reportGroup)) {
							if (rf.getKey().equals(last)) {
								factory = rf;
								break;
							}
						}
					}
					if(factory == null || !factory.getReport().isReadable())
						factory = getFirstAllowedFactory(reportGroup, factories);
				}
			}
			
			if(factory == null)
				factory = factories.get(0);
			
			logger.debug(factory.getKey());
			
			setUserPreference("report-" + reportGroup, factory.getKey());
			
			setQuery(factory.getReport().newQuery());
			
		}
		else {
			logger.error("No reports found for group: " + reportGroup);
		}
			
	}

	protected ReportFactory getFirstAllowedFactory(String reportGroup, List<ReportFactory> factories) {
		return factories.stream().filter(rep -> rep.getReport().getReportGroup().equals(reportGroup)).filter(f->f.getReport().isReadable()).findFirst().orElse(null);
	}

	public ReportPage(ReportFactory factory) {
		super(null);
		this.factory = factory;
		
		// extraer el id del report factory y ponerlo en el pageparameters
		
		setUserPreference("report-" + factory.getReport().getReportGroup(), factory.getKey());
		setQuery(factory.getReport().newQuery());
	}
	
	
	
	@Override
	public Console<Row> newConsole(Query query) {
		ReportConsole report = factory.getReport();
		report.setQuery(query);
		return report;
	}
	
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.REPORTS;
	}

	
	@Override
	public boolean hasPermissions() {
		
		boolean role_portal_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.PORTAL_ADMIN.getId());
		boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		boolean role_support = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
		boolean role_reports = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.REPORTS.getId());
		
		if (role_support || role_admin || role_portal_admin || role_reports) 
			return true;
		
		if (ServiceLocator.getService(com.novamens.service.BrandingService.class).isPortalEnabled())
			return true;
		
		return false;
		
	}
	
	@Override
	protected Panel newNavigationPanel() {
		 return  new GlobalNavigationBar<Row>("navigation", null, false);
	}
	
	@Override
	public Page getConsolePage(Query query, long index) {
		return new ReportPage();
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
