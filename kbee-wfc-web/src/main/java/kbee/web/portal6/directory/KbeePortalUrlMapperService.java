package kbee.web.portal6.directory;

import org.apache.wicket.protocol.http.WebApplication;

import com.novamens.event.Event;
import com.novamens.portal.service.PortalUrlService;

import kbee.web.searcher.page.SearcherAboutPage;
import kbee.web.searcher.page.SearcherContactPage;
import kbee.web.searcher.page.SearcherDetailDocumentPage;
import kbee.web.searcher.page.SearcherDetailVideoPage;
import kbee.web.searcher.page.SearcherExplorerPage;
import kbee.web.searcher.page.SearcherHomePage;
import kbee.web.searcher.page.SearcherNotificationsPage;
import kbee.web.searcher.page.SearcherResultsPage;
import kbee.web.searcher.page.SearcherUserNotesPage;
import kbee.web.service.PortalUrlMapperService;


public class KbeePortalUrlMapperService implements PortalUrlMapperService {
							
	static public final String ROOT_SITE = PortalUrlService.ROOT_SITE;
	static public final String SEARCHER = PortalUrlService.SEARCHER;
	static public final String DIAGRAMMABLE = PortalUrlService.DIAGRAMMABLE;
	
	@Override
	public void map(WebApplication webapp) {
		
		webapp.mountPage("/"+ROOT_SITE+"/${siteurl}/mynotepad", SearcherUserNotesPage.class);
		webapp.mountPage("/"+ROOT_SITE+"/${siteurl}/notifications", SearcherNotificationsPage.class);
		
		// Searcher
		//
		webapp.mountPage("/"+ROOT_SITE+"/${siteurl}", SearcherHomePage.class);
		webapp.mountPage("/"+ROOT_SITE+"/${siteurl}/results", SearcherResultsPage.class);
		webapp.mountPage("/"+ROOT_SITE+"/${siteurl}/explorer", SearcherExplorerPage.class);
		webapp.mountPage("/"+ROOT_SITE+"/${siteurl}/media/${oid}", SearcherDetailVideoPage.class);
		webapp.mountPage("/"+ROOT_SITE+"/${siteurl}/doc/${oid}", SearcherDetailDocumentPage.class);
		
		//
		//
		// webapp.mountPage("/"+ROOT_SITE+"/"+SEARCHER+"/${siteurl}/settings", SearcherSettingsPage.class);
		webapp.mountPage("/"+ROOT_SITE+"/"+SEARCHER+"/${siteurl}/about", SearcherAboutPage.class);
		webapp.mountPage("/"+ROOT_SITE+"/"+SEARCHER+"/${siteurl}/contact", SearcherContactPage.class);

		//
		// Diagramable site
		//
		
		
		
		
	}
	
	
	@Override
	public boolean listen(Event event) {
		return false;
	}

	@Override
	public void onEvent(Event event) {
		// TODO Auto-generated method stub
		
	}
}
