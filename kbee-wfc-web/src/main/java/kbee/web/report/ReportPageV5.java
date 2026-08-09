package kbee.web.report;

import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.error.ErrorNotAuthorizedPanel;
import kbee.web.error.ErrorPanel;
import kbee.web.nav.HomeBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.service.ReportsLibraryService;

public class ReportPageV5 extends  ConsolePage<Row> {

	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReportPageV5.class.getName());
	
	private ReportFactory factory = null;

	public ReportPageV5() {
		this(new PageParameters());
	}

	public ReportPageV5(PageParameters pageParameters) {
		super(null);
		this.factory = getFactory(pageParameters);
	}

	/**
	 * @param parameters
	 * @return
	 */
	protected ReportFactory getFactory(PageParameters parameters) {
		
		ReportFactory fa = null;
		
		try {
			StringValue id = parameters.get("key");
			if (!id.isNull() && !id.isEmpty()) {
				List<ReportFactory> factories = ServiceLocator.getService(ReportsLibraryService.class).getReports(getDomain());
				for (ReportFactory rf: factories) {
					logger.debug(rf.getDisplayName()+ " -> " + rf.getKey());
					if (rf.getKey().equals(id.toString())) {
						fa = rf;
						break;
					}
				}
			}
		} catch (Exception e ) {
			logger.error(e);
		}
		return fa;
	}


	public ReportPageV5(ReportFactory factory) {
		super(null);
		this.factory = factory;
		setQuery(factory.getReport().newQuery());
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setTopNavigation(getMainTopbar());
		setMenu(getMainLaternalMenu());
		
		if (this.factory!=null) {
			if (hasPermissions()) {
				getPageParameters().set("reportgroup", factory.getReportGroup());
				getPageParameters().set("key", factory.getKey());
				PageContentHeaderPanel<ReportFactory> panel=new PageContentHeaderPanel<ReportFactory>(null);
				panel.setTitle(factory.getDisplayName());
				panel.setBreadcrumbPanel(getHeaderPanelBreadcrumbPanel());
				setSearchPanel(false);
				setClearAllSearch(false);
				setAdvancedSearch(false);
				setSuggester(false);
				setPageContentHeader(panel);
			}
			else
				add(new ErrorNotAuthorizedPanel<>("editor"));
		}
		else {
			add(new ErrorPanel("editor"));
		}
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

	/**
	 * @return
	 */
	protected Panel getHeaderPanelBreadcrumbPanel() {
		try {
			MenuBreadCrumbPanel<?>  bc =new MenuBreadCrumbPanel<>();
			bc.addElement( new HomeBC());
			bc.addElement(new ReportsDropdownBC());
			bc.addElement(new BCElement( new Model<String>(factory.getDisplayName())));
			return bc;
		} catch (Exception e) {
			logger.error(e, getSessionUser().getUserName());
			return new InvisiblePanel("breadcrumb");
		}
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
	protected ReportFactory getFirstAllowedFactory(String reportGroup, List<ReportFactory> factories) {
		return factories.stream().filter(rep -> rep.getReport().getReportGroup().equals(reportGroup)).filter(f->f.getReport().isReadable()).findFirst().orElse(null);
	}

	@Override
	public Page getConsolePage(Query query, long index) {
		return new ReportPageV5();
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
