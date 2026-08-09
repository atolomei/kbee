package kbee.web.support;

import java.util.Map;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.entity.Person;
import com.novamens.content.notes.Billboard;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;

import kbee.content.support.SupportTicket;
import kbee.web.error.ErrorPanel;
import kbee.web.nav.ErrorNavigationBar;
import kbee.web.nav.TabNavigationBar;

import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ApplicationPage;

import kbee.web.portal6.editor.PortalSiteEditorPage;
import kbee.web.portal6.sitemanager.PortalNavigationBar;

public class ReportIssuePage extends AbstractApplicationPage<Person> {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReportIssuePage.class.getName());
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	
	
	public ReportIssuePage() {
		
	}

	
	public ReportIssuePage(PageParameters parameters) {
	}

	
	public void onInitialize()  {
		super.onInitialize();
		setMenu(new InvisiblePanel("menu"));
		//setTopNavigation(new ErrorNavigationBar<Void>("navigation"));
		setTopNavigation(new TabNavigationBar<SupportTicket>("navigation"));
		setLogVisit(true);
		

		add(new ReportIssueEditor("panel"));
		
		
		setPageTitle(new Model<String>("Report issue"));
		setPageDescription(getPageTitle());

		
		
		

		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
