package kbee.web.portal6;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.TextFilter;
import com.novamens.kbee.wicket.markup.html.event.OnSearchEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.portal.model.diagrammablesite.PortalPage;
import com.novamens.portal.service.PortalDirectoryService;
import com.novamens.portal.service.SiteFactoryService;
import com.novamens.portal6.model.PortalException;
import com.novamens.portal6.model.Site;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.content.console.WorkspacePage;
import kbee.web.nav.HomeBC;
import kbee.web.nav.SitesBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.service.PortalPanelService;

@SuppressWarnings("serial")
public class SitesPage extends ConsolePage<Site> implements PortalPage {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SitesPage.class.getName());

	private static final long serialVersionUID = 1L;

	final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean is_domain_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_archive = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.ARCHIVE.getId());
	final boolean is_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_portal_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.PORTAL_ADMIN.getId());

	public SitesPage() {
		super(null);
	}

	public SitesPage(Query query) {
		super(query);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		
		//ServiceLocator.getService(PortalPanelService.class).registerClassPanel("block-billboard", "kbee.web.alert.BillboardPanel");
        //ServiceLocator.getService(PortalPanelService.class).registerClassPanel("block-dummy", kbee.web.alert.BillboardPanel.class.getName());
        //ServiceLocator.getService(PortalPanelService.class).registerClassPanel("area-billboard", "kbee.web.alert.BillboardPanel");
        //ServiceLocator.getService(PortalPanelService.class).registerClassPanel("area-dummy", "kbee.web.alert.BillboardPanel");
        
		PageContentHeaderPanel<Content> panel=new PageContentHeaderPanel<Content>(null);
		
		panel.setTitle( new StringResourceModel("sites", this, null));
		
		MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<Void>();
		
		bc.addElement( new HomeBC());
		
		bc.addElement(new SitesBC());
		panel.setBreadcrumbPanel(bc);
		
		setSearchPlaceHolder(new StringResourceModel("search-in", this, null).getObject());
		setSuggester(false); // Search supports suggester
		setSearchPanel(true); // include Search
		setAdvancedSearch(false); // button advanced search
		panel.setSearchPanel(getSearchPanel());
		setLogVisit(true);
		
		super.setPageContentHeader(panel);
		
		Site site;
		
		/**
		try {
				site = ServiceLocator.getService(PortalDirectoryService.class).findDashboardSite(getSessionUser());
				 if (site==null)
					 site=ServiceLocator.getService(SiteFactoryService.class).createMainDashboardSite(getPerson());
		
		} catch (PortalException e) {
				logger.error(e);
		}**/

		/**
		try {
			site = ServiceLocator.getService(PortalDirectoryService.class).findSiteByUserKey(getSessionUser(), "test");
			 if (site==null)
				 site=ServiceLocator.getService(SiteFactoryService.class).createTestSite(getPerson());
	
		} catch (Exception e) {
			logger.error(e);
		}**/
		
		
	}
	
	@Override
	public Console<Site> newConsole(Query query) {
		return new SitesConsole(query) {
			private static final long serialVersionUID = 1L;

			@Override
			public Page getConsolePage(Query query, long index) {
				return new SitesPage(query);
			}
		};
	}

	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SITES;
	}

	@Override
	public Page getConsolePage(Query query, long index) {
		return new SitesPage(query);
	}

	@Override
	public void addListeners() {
		super.addListeners();
		add(new WicketEventListener<OnSearchEvent>() {
			@Override
			public void onEvent(OnSearchEvent event) {
					getQuery().getParameters().put("text", new TextFilter(event.getText()));
					getQuery().getParameters().put("sort", "relevance");
					setResponsePage(getConsolePage(getQuery(), 0));
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof OnSearchEvent;
			}
		});
	}

	
	@Override
	public boolean hasPermissions() {
		return true;
		// return this.is_domain_admin || this.is_root || this.is_archive ||
		// this.is_support || this.is_portal_admin;
	}

	
	@Override
	public void onSiteAdmin(IModel<Site> model, AjaxRequestTarget target) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSiteAdmin(IModel<Site> model, AjaxRequestTarget target, int site_mode) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSiteAdmin(AjaxRequestTarget target) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSiteAdmin(AjaxRequestTarget target, int site_mode) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPageAdmin(AjaxRequestTarget target) {
		// TODO Auto-generated method stub
	}

}
