package kbee.web.report;

import org.apache.wicket.Page;
 
import org.apache.wicket.markup.html.panel.Panel;
import org.springframework.beans.factory.BeanNameAware;

import com.novamens.beans.BeansService;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.indexer.query.Query;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.report.Row;

/**
 * Is this deprecated ??
 * @author atolo
 *
 */
@Deprecated
public class ReportPage extends ConsolePage<Row> implements BeanNameAware {

	private static final long serialVersionUID = 1L;
	
	private String beanName;
	private ReportConsole console;

	public ReportPage(Query query) {
		super(query);
	}
	
	@Override
	public Console<Row> newConsole(Query query) {
		return console.clone(query);
	}
	
	public void setConsole(ReportConsole console) {
		this.console = console;
	}

	@Override
	public Page getConsolePage(Query query, long index) {
		return (Page)ServiceLocator.getService(BeansService.class).getBean(beanName);
	}
	
	@Override
	public boolean hasPermissions() {
		boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		boolean role_support = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
		if (role_support || role_admin) 
			return true;
		return false;
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.REPORTS;
	}
	
    @Override
    public void setBeanName(String beanName) {
    	this.beanName = beanName;
    }

	@Override
	protected Panel newNavigationPanel() {
		return new GlobalNavigationBar<Row>("navigation");
	}
}
