package com.novamens.portal.service;

import com.novamens.content.base.Content;
import com.novamens.dom.Domain;
import com.novamens.portal6.model.PortalException;
import com.novamens.portal6.model.PortalService;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.ViewDetailContent;
import com.novamens.service.BusinessSystemService;

public interface PortalUrlService extends BusinessSystemService, PortalService {
	
	static public final String ROOT_SITE = "portal";
	static public final String SEARCHER = "lib";
	static public final String DIAGRAMMABLE = "site";
	
	public Site getContentHomeSite(Content content) throws PortalException;
	public String getContentUrl(Content content);
	public String getHomeSite(Domain domain);
	public String getUrl(ViewDetailContent view);
	public String getSiteUrl(Site site);
	public String getDetailUrl(Content content, Site site);
	public String getRelativeDetailUrl(Content content, Site site);
	public String getRelativeSiteUrl(Site site);
	public boolean isAvailableSiteUrl(String url, Domain domain);
	public boolean isAvailableSiteAlias(String url, Domain domain);
	
}
