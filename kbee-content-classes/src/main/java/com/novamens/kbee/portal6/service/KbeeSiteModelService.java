package com.novamens.kbee.portal6.service;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.pages.RedirectPage;

import com.novamens.event.Event;
import com.novamens.kbee.portal.service.KbeeSiteService;
import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.SimplePage;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteTemplate;
import com.novamens.portal6.model.SiteType;
import com.novamens.portal6.service.SiteModelService;
import com.novamens.wicket.model.ObjectModel;

public class KbeeSiteModelService implements SiteModelService {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeSiteModelService.class.getName());
	
	private Site site;
	
	
	public KbeeSiteModelService() {
		
	}
	
	public KbeeSiteModelService( Site site) {
		 this.site=site;
	}
	
	public void setSite(Site site) {
		this.site=site;
	}
	
	public Site getSite() {
		return this.site;
				
	}
	@Override
	public boolean listen(Event event) {
		return false;
	}

	@Override
	public void onEvent(Event event) {
	}

	/**
	 * Mediante Spring poner todo
	 */
	@Override
	public List<Page> getSimplePages() {
		
		List<Page> list = new ArrayList<Page>();
		
		if (site.getSiteType()==SiteType.LIBRARY) {
			
			list.add( new SimplePage(getSite(), "home")    );
			list.add( new SimplePage(getSite(), "results") );
			list.add( new SimplePage(getSite(), "detail")  );
			list.add( new SimplePage(getSite(), "about")   );
			list.add( new SimplePage(getSite(), "notifications"));

		}
		if (site.getSiteType()==SiteType.DEAL_ROOM)	{
			
			list.add( new SimplePage(getSite(), "home")    );
			list.add( new SimplePage(getSite(), "results") );
			list.add( new SimplePage(getSite(), "detail")  );
			list.add( new SimplePage(getSite(), "about")   );
			list.add( new SimplePage(getSite(), "notifications"));
			
		}
		if (site.getSiteType()==SiteType.KNOWLEDGE_BASE) {

			list.add( new SimplePage(getSite(), "home")    );
			list.add( new SimplePage(getSite(), "results") );
			list.add( new SimplePage(getSite(), "detail")  );
			list.add( new SimplePage(getSite(), "about")   );
			
		}

		return list;
	}

	@Override
	public List<Area> getSimplePages(Page page) {
		return null;
	}

	@Override
	public List<Block> getSimplePages(Area area) {
		return null;
	}

	@Override
	public Page getPage(String key) {
		
		
		return null;
	}

}
