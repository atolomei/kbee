package kbee.web.content.console;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.resource.KBFile;
import com.novamens.content.security.Role;
import com.novamens.content.web.console.audit.markup.AuditContentConsole;
import com.novamens.indexer.query.Query;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.util.logging.Logger;
import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.nav.AuditDropDownBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.workflow.task.PageTaskToolbar;

public class AuditResourcesPage extends ConsolePage<KBFile> {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(AuditResourcesPage.class.getName());

	
	final boolean is_root			= ServiceLocator.getService(SecurityService.class).isRoot(); 
	final boolean is_auditor		= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.AUDITOR.getId());
	final boolean is_domain_admin	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	public AuditResourcesPage() {
		super(null);
	}
	
	public AuditResourcesPage(Query query) {
		super(query);
	}
		@Override
	public Console<KBFile> newConsole(Query query) {
		return new AuditResourcesConsole(query) {
			private static final long serialVersionUID = 1L;
			@Override
			public Page getConsolePage(Query query, long index) {
				return AuditResourcesPage.this.getConsolePage(query, index);
			}
		};
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		PageContentHeaderPanel<Content> panel=new PageContentHeaderPanel<Content>(null);
		
		MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<>();
		bc.addElement(new AuditDropDownBC());
		bc.addElement(new BCElement("audit.resources"));
		panel.setBreadcrumbPanel(bc);
		panel.setTitle( new StringResourceModel("audit.resources", this, null));

		
		//panel.setMenuPanel(getContentHeaderPanelMenuPanel());

		setSearchPlaceHolder(new StringResourceModel("audit.resources", this, null).getObject());
		
		setSuggester(false); // Search supports suggester
		setSearchPanel(true); // include Search
		setAdvancedSearch(true); // button advanced search
		//panel.setSearchPanel(getSearchPanel());
		
		
		List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
		List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
		r_list.add(getSearchPanel("panel"));
		PageTaskToolbar<KBFile> toolbar = new PageTaskToolbar<KBFile>("toolbar", getModel(), l_list, r_list);
		panel.setToolbarPanel(toolbar);
		
		setPageContentHeader(panel);
		

	}

	
	
	@Override
	public Page getConsolePage(Query query, long index) {
		return new AuditResourcesPage(query);
	}

	@Override
	public Page getConsolePage() {
		return new AuditResourcesPage();
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.LOGS;
	}
	
	@Override
	public boolean hasPermissions() {
		return is_domain_admin || is_root || is_auditor; 
	}
}
