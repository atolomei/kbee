package kbee.web.portal6;


import java.io.Serializable;
import java.util.Map;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.user.UserProfile;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.PageSection;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.PortalViewMode;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteType;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.dashboard.DashboardFactoryHomePage;
import kbee.web.dashboard.DashboardHomePage;
import kbee.web.dashboard.DashboardPortalLibraryContentsPanel;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.library.LibraryEditor;
import kbee.web.portal6.event.PortalAjaxEvent;
import kbee.web.portal6.panel.AreaInternalPanel;
import kbee.web.portal6.panel.PageSectionInternalPanel;
import kbee.web.portal6.panel.PortalDummyBlockPanel;
import kbee.web.portal6.panel.PortalErrorPanel;
import kbee.web.portal6.panel.PortalTitlePanel;
import kbee.web.searcher.editor.SearcherSiteEditorPage;
import kbee.web.searcher.page.SearcherHomePage;
import kbee.web.security.user.UsersPage;
import kbee.web.service.PortalPanelService;

/**
 *
 */
public class KbeePortalPanelService implements PortalPanelService {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePortalPanelService.class.getName());

	// private Map<String, String> block_panels = new ConcurrentHashMap<String, String>(16, 0.9f, 1); 
	

	/** PageSection  ------------------------------------------------------------------*/
	
	@Override
	public Panel getInternalHeaderPanel(String id, PageSection ps) {
		return getInternalHeaderPanel(id, ps, PortalViewMode.PRODUCTION, null); 	
	}
	
	@Override
	public Panel getInternalHeaderPanel(String id, PageSection ps, PortalViewMode viewmode, Map<String, String> parameters) {

		if (isPayloadRender(ps, viewmode, parameters)) {
			return new PortalTitlePanel<PageSection>(id, new ObjectModel< PageSection>(ps));
		}
		else
			return new PortalDummyBlockPanel<PageSection>(id, new ObjectModel< PageSection>(ps));
	}

	@Override
	public Panel getInternalBodyPanel(String id, PageSection ps) {
		return getInternalBodyPanel(id,ps, PortalViewMode.PRODUCTION, null);
	}
	
	@Override
	public Panel getInternalBodyPanel(String id, PageSection ps, PortalViewMode viewmode) {
		return getInternalBodyPanel(id, ps, viewmode, null);
	}
	
	
	@Override
	public Panel getInternalBodyPanel(String id, PageSection ps, PortalViewMode viewmode, Map<String, String> parameters) {
		try {
			
			if (ps==null)
				throw new IllegalArgumentException("PageSection can not be null");
			
				String key=ps.getKey();
				if (key==null || key.equals("pagesection")) {
					return new PageSectionInternalPanel(id, new ObjectModel<PageSection>(ps),  viewmode, parameters);
				}
				
				if (isPayloadRender(ps, viewmode, parameters))
					return  ServiceLocator.getService(PortalMVCService.class).getViewer(key, id, ps);
				
				return new PortalDummyBlockPanel<PageSection>(id, new ObjectModel<PageSection>(ps), "pagesection-internal-panel-body");
				
		} catch (Exception e) {
			logger.error(e);					
			return new PortalErrorPanel<PageSection>(id, new ObjectModel<PageSection>(ps), e);
		}
	}

	/** Area  ------------------------------------------------------------------*/
	
	@Override
	public Panel getInternalHeaderPanel(String id, Area area) {
		return getInternalHeaderPanel(id, area, PortalViewMode.PRODUCTION, null);
	}

	@Override
	public Panel getInternalHeaderPanel(String id, Area area, PortalViewMode viewmode,  Map<String, String> parameters) {
		if (isPayloadRender(area, viewmode, parameters))
			return new PortalTitlePanel<Area>(id, new ObjectModel<Area>(area));
		else 
			return new PortalDummyBlockPanel<Area>(id, new ObjectModel<Area>(area));
		
	}


	@Override
	public Panel getInternalBodyPanel(String id, Area block) {
			return getInternalBodyPanel(id, block, PortalViewMode.PRODUCTION);
	}
	
	
	public Panel getInternalBodyPanel(String id, Area area, PortalViewMode viewmode) {
		return getInternalBodyPanel(id,  area, viewmode, null);
	}
	
	@Override
	public Panel getInternalBodyPanel(String id, Area area, PortalViewMode viewmode, Map<String, String> parameters) {
		try {
			
			if (area==null)
				throw new IllegalArgumentException("area can not be null");
			
				String key=area.getKey();
				
				if (key==null || key.equals("area"))
					return new AreaInternalPanel(id, new ObjectModel<Area>(area), viewmode, parameters);

				if (isPayloadRender(area, viewmode, parameters))
					 return ServiceLocator.getService(PortalMVCService.class).getViewer(key, id, area);
				
				
			return new PortalDummyBlockPanel<Area>(id, new ObjectModel<Area>(area), "area-internal-panel-body");

		} catch (Exception e) {
			logger.error(e);					
			return new PortalErrorPanel<Area>(id, new ObjectModel<Area>(area), e);
		}
	}

	
	/** Block  ------------------------------------------------------------------*/
	
	@Override
	public Panel getInternalHeadPanel(String id, Block block) {
		return getInternalHeaderPanel(id, block, PortalViewMode.PRODUCTION, null);
	}
	
	@Override
	public Panel getInternalHeaderPanel(String id, Block block, PortalViewMode viewmode,  Map<String, String> parameters) {
		if (isPayloadRender(block, viewmode, parameters)) 
			return new PortalTitlePanel<Block>(id, new ObjectModel<Block>(block));
		
		return new PortalDummyBlockPanel<Block>(id, new ObjectModel<Block>(block));
	}
	
	@Override
	public Panel getInternalBodyPanel(String id, Block block) {
			return getInternalBodyPanel(id, block, PortalViewMode.PRODUCTION);
	}
	
	public Panel getInternalBodyPanel(String id, Block block, PortalViewMode viewmode) {
					return  getInternalBodyPanel(id, block, viewmode, null);
	}
	
	@Override
	public Panel getInternalBodyPanel(String id, Block block, PortalViewMode viewmode, Map<String, String> parameters) {
		try {
			
			if (block==null)
				throw new IllegalArgumentException("block can not be null");

				String key=block.getKey();
				
				//key="block"; 
				
				if (key==null || key.equals("block"))
					return new PortalDummyBlockPanel<Block>(id, new ObjectModel<Block>(block), "block-internal-panel-body", new Model<String>( block.getKey() ));
				
				if (isPayloadRender(block, viewmode, parameters)) {
				
					Panel panel = ServiceLocator.getService(PortalMVCService.class).getViewer(key, id, block);
					
					if (panel instanceof DashboardPortalLibraryContentsPanel) {
						
						logger.debug(panel.getClass().getName());
						
					}
					
					logger.debug(panel.getClass().getName());
					return panel;
				}
				
			return new PortalDummyBlockPanel<Block>(id, new ObjectModel<Block>(block), "block-internal-panel-body po_wrapper", null);
	 
		} catch (Exception e) {
			logger.error(e);					
			return new PortalErrorPanel<Block>(id, new ObjectModel<Block>(block), e);
		}
	}

	//@Override
	//public void registerClassPanel(String key, String classname) {
	//	block_panels.put(key, classname);
	//}
	
	@Override
	public Panel getGlobalFooterPanel() {
		return new InvisiblePanel("footer");
	}

	@Override
	public Panel getGlobalHeaderPanel(Site site) {
		return new InvisiblePanel("header");
	}

	/**
	 * 
	 */
	@Override
	public WebPage getStartPage(UserProfile profile) {
		
			String start = profile.getStartPage();

			boolean admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
			boolean role_workspace = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.WORKSPACE.getId())  || admin;
		
			
			 
			//Site xsite = getPortalDao().findSiteByURI("bcv", profile.getDomain());
			//if (xsite!=null) 
			//	return (new SearcherHomePage(new ObjectModel<Site>(xsite)));
			  
			// start="bcv";
			
			
			if (start.equals("home")) {
				if (profile.getDomain().getName().equals("kbee"))
						return new DashboardFactoryHomePage();
				else
					return new DashboardHomePage();
			}

			if (start.equals("mytasks") && (role_workspace))
				return new kbee.web.content.console.WorkspacePage();


			boolean role_monitor = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.MONITOR_AUDIT.getId()) || admin;
			if (start.equals("monitor") && (role_monitor))
				return new kbee.web.content.console.MonitorPage();
					
			
			boolean role_pending = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.PENDING_TASKS.getId()) || admin;
			
			if (start.equals("pending") && (role_pending))
				return new kbee.web.content.console.PendingTasksPage();
			
			
			if (start.equals("library"))
				return  new kbee.web.content.console.ContentBasePage();
			
			
			boolean role_security = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId())  || admin;
			
			
			if (start.equals("users") && role_security) 
				return new UsersPage();
				
			for (Site site: getPortalDao().getSites(profile.getDomain())){
				if (site.getKey()!=null && site.getKey().equals(start)) {
					return getWebPage(site);
				}
			}
			
			return  new kbee.web.content.console.ContentBasePage();
		}

	public String getSiteUrl(Site site) {
		if (site.isExternal())			
			return site.getUrl();
		return site.getUrl();
	}
	
	
	@Override
	public WebPage getWebPage(Serializable siteid) {
		Site site=getPortalDao().findSiteById(siteid);
		return getWebPage(site);
		
	}
	public WebPage getWebPage(Site site) {

		if (site==null)
			throw new IllegalArgumentException("site is null");
		
		if (site.isExternal()) {	
			logger.debug( "redirect ->" + site.getUrl());
			
			if (site.getUrl()==null)
				return new ApplicationErrorPage<>(new Model<String>("Site url is null"));
			
			return new RedirectPage(site.getUrl());
		}
		
		else if (site.getSiteType()==SiteType.GENERAL_DASHBOARD) return new SearcherHomePage( new ObjectModel<Site>(site));
		else if (site.getSiteType()==SiteType.LIBRARY) 			 return new SearcherHomePage( new ObjectModel<Site>(site));
		else if (site.getSiteType()==SiteType.DEAL_ROOM)		 return new SearcherHomePage( new ObjectModel<Site>(site));
		else if (site.getSiteType()==SiteType.KNOWLEDGE_BASE)	 return new SearcherHomePage( new ObjectModel<Site>(site));
		
		logger.debug( "redirect ->" + site.getUrl());
		
		if (site.getUrl()==null)
			return new ApplicationErrorPage<>(new Model<String>("Site url is null"));
		
		return new RedirectPage(site.getUrl());
	}

	@Override
	public WebPage getEditorWebPage(Site site) {
		 
		if (site.getSiteType()==SiteType.LIBRARY)			 return new SearcherSiteEditorPage(new ObjectModel<Site>(site));
		 if (site.getSiteType()==SiteType.DEAL_ROOM)		 return new SearcherSiteEditorPage(new ObjectModel<Site>(site));
	 	 if (site.getSiteType()==SiteType.KNOWLEDGE_BASE)	 return new SearcherSiteEditorPage(new ObjectModel<Site>(site));

	 	 if (site.isExternal()) 
				return new ExternalSiteEditorPage(new ObjectModel<Site>(site));
			
	 	 return null;
	}
	
	protected PortalDao getPortalDao() {
		return (PortalDao)ServiceLocator.getService(BeansService.class).getBean("portalDao");
	}


	private boolean isPayloadRender(PortalObject po, PortalViewMode viewmode, Map<String, String> parameters) {
		
		if (viewmode==PortalViewMode.PRODUCTION)
			return true;
		
		if (parameters==null)
			return true;

		String key="payload-visible";
		String show_payload  = getPreference( po, key, "no");
		
		if (show_payload.equals("yes"))
			return true;
		
		if (parameters.get(PortalAjaxEvent.PAYLOAD_VISIBLE)!=null) 
			return parameters.get(PortalAjaxEvent.PAYLOAD_VISIBLE).equals(String.valueOf(PortalAjaxEvent.SHOW_PAYLOAD_YES));	 
		
		return false;

	}

	
	protected User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
		} catch (Exception e) {
			return null;
		}
	}
	
	//public void setPreference(String key, String value) {
	//	((com.novamens.kbee.security.KbeeUser) getSessionUser()).getService(PreferencesService.class).setValue("portal-page-"+getModel().getObject().getId().toString(), key, value);
	//}
	
	
	public String getPreference(PortalObject po, String key, String defaultValue) {
		return ((com.novamens.kbee.security.KbeeUser) getSessionUser()).getService(PreferencesService.class).getValue("portal-"+po.getClassKey()+"-"+po.getId().toString(), key, defaultValue);
	}

	
}


