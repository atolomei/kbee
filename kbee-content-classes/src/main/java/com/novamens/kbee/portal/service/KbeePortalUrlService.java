package com.novamens.kbee.portal.service;


import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.service.UrlService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.portal.service.PortalUrlService;
import com.novamens.portal6.model.PortalException;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteTemplate;
import com.novamens.portal6.model.SiteType;
import com.novamens.portal6.model.ViewDetailContent;
import com.novamens.service.ServiceLocator;

import kbee.util.PropertiesFactory;
			
public class KbeePortalUrlService implements PortalUrlService, EventListener {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePortalUrlService.class.getName());

	private Map<String, String> reserved_alias = new HashMap<String, String>();
	
	public KbeePortalUrlService() {

		reserved_alias.put("mytasks", "mytasks");		
		reserved_alias.put("monitor", "monitor");
		reserved_alias.put("library", "library");
		reserved_alias.put("dashbord", "dashbord");
		reserved_alias.put("users", "users");
		reserved_alias.put("api_dashbord", "api_dashbord");
		reserved_alias.put("api_reports", "api_reports");
		reserved_alias.put("domains", "domains");
	}
	
	@Override
	public boolean listen(Event event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onEvent(Event event) {
		// TODO Auto-generated method stub

	}

	@Override
	public Site getContentHomeSite(Content content) throws PortalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContentUrl(Content content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHomeSite(Domain domain) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUrl(ViewDetailContent view) {
		// TODO Auto-generated method stub
		return null;
	}


	
	protected Domain getDomain() {
		 return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	private String getServerUrl(Domain domain) {
		return domain.getService(UrlService.class).getServerUrl();
		//return vanity_server.replace("${domain}", domain.getName()) + (vanity_port.length()==0 || vanity_port.equals("80") ? "": (":"+vanity_port));
	}
	
	/**
	 * 
	 *  localhost:8080/ portal
	 *  localhost:8080/ searcher
	 *  
	 */
	@Override
	public String getSiteUrl(Site site) {
		
		if (site.isExternal())
			return site.getUrl();
		
		StringBuilder str = new StringBuilder();
		
		str.append(getServerUrl(getDomain()));
		
		if (site.getSiteType()==SiteType.LIBRARY  || 
			site.getSiteType()==SiteType.DEAL_ROOM || 
			site.getSiteType()==SiteType.KNOWLEDGE_BASE) {
			//str.append("/"+PortalUrlService.ROOT_SITE+"/"+PortalUrlService.SEARCHER+"/");
			str.append("/"+PortalUrlService.ROOT_SITE+"/");
			str.append(site.getUrl());
		} 
		
		logger.debug(str.toString());
		
		return str.toString();
	}


	@Override
	public String getRelativeSiteUrl(Site site) {
		
		if (site.isExternal())
			return site.getUrl();
		
		StringBuilder str = new StringBuilder();
		// str.append(getServerUrl(getDomain()));
		if (site.getSiteType()==SiteType.LIBRARY || 
			site.getSiteType()==SiteType.KNOWLEDGE_BASE ||
			site.getSiteType()==SiteType.DEAL_ROOM) {
			//str.append(PortalUrlService.ROOT_SITE+"/"+PortalUrlService.SEARCHER+"/");
			str.append(PortalUrlService.ROOT_SITE+"/");
			str.append(site.getUrl());
		}
		return str.toString();
	}

	/**
	 * 
	 * 
	 */

	@Override
	public String getDetailUrl(Content content, Site site) {
		String url= getSiteUrl(site);
		if (site.getSiteType()==SiteType.LIBRARY ||
			site.getSiteType()==SiteType.KNOWLEDGE_BASE ||
			site.getSiteType()==SiteType.DEAL_ROOM) {
			if (content.getContentTemplate().isVideo())	return  url + "/media/" + String.valueOf(content.getOId());
			if (content.getContentTemplate().isAudio())	return  url + "/media/" + String.valueOf(content.getOId());
			if (content.getContentTemplate().isImage())	return  url + "/media/" + String.valueOf(content.getOId());
			
			return url + "/doc/" + String.valueOf(content.getOId());
		}
		
		logger.error("url " + (site!=null?site.getTitle():""));
		
		return "undefined";
	}

	/**
	 * 
	 * 
	 */
	@Override
	public String getRelativeDetailUrl(Content content, Site site) {
		String url= getRelativeSiteUrl(site);
		if (site.getSiteType()==SiteType.LIBRARY  || 
				site.getSiteType()==SiteType.KNOWLEDGE_BASE ||
				site.getSiteType()==SiteType.DEAL_ROOM) {
			if (content.getContentTemplate().isVideo())	return  url + "/media/" + String.valueOf(content.getOId());
			if (content.getContentTemplate().isAudio())	return  url + "/media/" + String.valueOf(content.getOId());
			if (content.getContentTemplate().isImage())	return  url + "/media/" + String.valueOf(content.getOId());
			return url + "/doc/" + String.valueOf(content.getOId());
		}
		
		logger.error("url " + (site!=null?site.getTitle():""));
		
		return "undefined";
	}

	@Override
	public boolean isAvailableSiteUrl(String url, Domain domain) {
		Site site = getPortalDao().findSiteByURI(url, domain);
		if (site!=null)
			return false;
		return true;
	}
	

	
	@Override						
	public boolean isAvailableSiteAlias(String url, Domain domain) {
		Site site = getPortalDao().findSiteByAlias(url, domain);
		if (site!=null)
			return false;
		if (reserved_alias.containsKey(url))
			return false;
		return true;
	}
	
	protected PortalDao getPortalDao() {
		return (PortalDao)ServiceLocator.getService(BeansService.class).getBean("portalDao");
	}
}
