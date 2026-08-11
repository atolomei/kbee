package com.novamens.kbee.content.service;

import java.util.Properties;

import com.novamens.content.service.UrlService;
import com.novamens.dom.Domain;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.ServiceLocator;
import com.novamens.system.parameters.SystemParameterService;
import com.novamens.thumbnail.ThumbnailSize;

import kbee.util.PropertiesFactory;

public abstract class KbeeAbstractUrlService implements UrlService {
			
	static Properties properties = PropertiesFactory.getInstance("kbee").getProperties();
	static String servername;
	static String vanity_server;
	static String webprotocol;
	
	static  {

		webprotocol=ServiceLocator.getService(SystemParameterService.class).getParameter("webprotocol", "http");
		servername = properties.getProperty("server", webprotocol.trim()+"://localhost").trim();
		vanity_server = properties.getProperty("vanity-server", servername).trim();
		
	}
	public KbeeAbstractUrlService() {
	}
	
	public String getServerUrl() {
		return getServerUrl(getDomain());
	}
	
	public String getThumbnailUrl(ThumbnailSize size) {
		return null;
	}
	
	public String getThumbnailPublicUrl(ThumbnailSize size) {
		return null;
	}
	
	public String getPublicTaskUrl() {
		return null;
	}
	
	public String getTaskUrl() {
		return null;
	}
	
	protected abstract Domain getDomain();
	
	
	
	/** 
	 * NOTE. We can not use this here -> domain.getService(UrlService.class).getServerUrl();
	 * 
	 * @param domain
	 * @return
	 */
	protected String getServerUrl(Domain domain) {
		String port=ServiceLocator.getService(ApplicationServerService.class).getJettyPort();
		String vanity_port = properties.getProperty("vanity-port", port).trim();
		if (domain.getName().equals("kbee"))
			return servername + (vanity_port.length()==0 || vanity_port.equals("80") ? "": (":"+vanity_port));
		return vanity_server.trim().replace("${domain}", domain.getName()) + (vanity_port.length()==0 || vanity_port.equals("80") ? "": (":"+vanity_port));
	}
}