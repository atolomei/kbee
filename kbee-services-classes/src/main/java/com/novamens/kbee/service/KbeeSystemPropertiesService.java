package com.novamens.kbee.service;

import java.util.Properties;

import com.novamens.dom.KBFSStorageType;
import com.novamens.system.properties.SystemPropertiesService;

import kbee.util.PropertiesFactory;

public class KbeeSystemPropertiesService implements SystemPropertiesService {

	private Properties properties = PropertiesFactory.getInstance("kbee").getProperties();
	
	private String server_id_prefix = null;
	
	
	@Override
	public String getDefaultKBFSService() {
		return  properties.getProperty("kbfs.storage.default",  KBFSStorageType.Odilon.getKey()).trim();
	}
	
	@Override
	public String getServerId() {
		return  properties.getProperty("kbfs.server.id", "dev").trim();
	}
	
	@Override
	public String getServerIdPrefix() {
	 		
		if (server_id_prefix!=null)
			return server_id_prefix;
		
		if(getServerId()==null)
				server_id_prefix="";
		
	 	else if (getServerId().length()<1 || getServerId().endsWith("-"))
	 		server_id_prefix = getServerId();
	 		else
	 			server_id_prefix = getServerId() + "-";
		return server_id_prefix;
	}
	
	
	@Override
	public String getProperty(String key) {
		String s= properties.getProperty(key,  null);
		if (s!=null)
			return s.trim();
		return null;
	}
	

}
