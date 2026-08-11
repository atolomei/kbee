package com.novamens.portal.service;

import java.util.List;

import com.novamens.portal6.model.PortalService;
import com.novamens.portal6.model.Site;
import com.novamens.service.BusinessObjectService;
import com.novamens.util.KeyValue;


/**
 * TODO: Reset Cache via Events
 * kbee-content-classes
 * 
 */
public interface PortalUserService extends BusinessObjectService, PortalService {

	
	/** SiteFavotires */
	public void removeFavorite(Site site);
	public void addFavorite(Site site);
	public boolean isSiteInFavorites(Site site);

	
	// --------------------------------------------
	// public List<ViewBK> getRecentActivity();
	
	// public void resetCache();
	// public void evict();
	
	
	//
	// Ver como sacarlo de 
	// MySite  ContextualMenu2 
	//
	public List<KeyValue<String>> getListModel();
	
}
