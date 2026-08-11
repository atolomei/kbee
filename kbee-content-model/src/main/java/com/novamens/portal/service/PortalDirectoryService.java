package com.novamens.portal.service;

import java.util.List;

import com.novamens.dom.Domain;
import com.novamens.portal6.model.PortalException;
import com.novamens.portal6.model.PortalService;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteType;
import com.novamens.security.User;
import com.novamens.service.BusinessSystemService;


/**
 * 
 * <h3>Directory of Sites</h3>
 * <p>Example -> Dashboard Site for a User (for DashboardPage)</p>
 * 
 * 
 */
public interface PortalDirectoryService extends BusinessSystemService, PortalService {

	public List<Site> getSites(Domain domain) throws PortalException;
	public List<Site> getSitesPublic(Domain domain) throws PortalException;
	public List<Site> getSites(Domain domain, SiteType site_type) throws PortalException;

	public Site findSiteByURI(String uri, Domain domain) throws PortalException;
	public Site findSiteByURI(String uri, Domain domain, SiteType site_type) throws PortalException;
	public Site findSiteById(Long id) throws PortalException;
	public Site findSiteByOId(Long oid) throws PortalException;
	
	public Site getHomeSite(Domain domain);
	
	public boolean existsSiteUrl(String relative_url, Domain domain);
	
	public Site findDashboardSite(User user) throws PortalException;
	public Site findSiteByUserKey(User sessionUser, String string);
	

	// -------
	// public String getContentUrl(Content content);
	// public void deleteAllSitesExternal(Domain domain);
	// public void deleteAllSites(Domain domain, SiteType site_type, boolean only_internal) throws PortalException, ContentMgmtException, ContentCreationException;
	// public void deleteAllViewDetailContent(Domain domain) throws PortalException;
	// public DiagrammableSite getGlobalHomeSite(Domain domain) 	throws PortalException;
	// public DiagrammableSite createHome(Domain domain) 			throws PortalException;
	// public DiagrammableSite getContentHomeSite(Content content)  throws PortalException; 
	// public ResourceReference getLogo();
	// public ResourceReference getLogo(Domain domain);
	// public Panel getGlobalFooterPanel();
	// public Panel getGlobalHeaderPanel(IModel<Site> callerSiteModel);
	// public Map<String, String> getIcons();
	// public void setSessionFactory(SessionFactory sessionFactory);
	// public void addIconsIfNotPresent(Domain domain);
	// -------

}
