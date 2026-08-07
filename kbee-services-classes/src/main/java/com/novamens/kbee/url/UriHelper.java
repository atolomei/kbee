package com.novamens.kbee.url;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.novamens.content.base.Content;
import com.novamens.content.base.ResourceContainer;
import com.novamens.dom.Domain;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.ServiceLocator;

import kbee.util.PropertiesFactory;
import kbee.util.Tuple;

public class UriHelper {
	
	private static UriHelper Instance;

	 

	
	public static UriHelper getInstance() {
		if (Instance==null) 
			Instance = new UriHelper();
		return Instance;
	}

	


	/**
	public String getServerURLAndPort(String domain_name) {
		String port=ServiceLocator.getService(ApplicationServerService.class).getJettyPort();
		String vanity_port = properties.getProperty("vanity-port", port).trim();
		if (domain_name.equals("kbee"))
			return servername + (vanity_port.length()==0 || vanity_port.equals("80") ? "": (":"+vanity_port));
		return vanity_server.trim().replace("${domain}", domain_name) + (vanity_port.length()==0 || vanity_port.equals("80") ? "": (":"+vanity_port));
			//return vanity_server.replace("${domain}", domain_name) + ((vanity_port!=null && !vanity_port.equals("80"))?":"+vanity_port:"");
	}
	**/
	
	public String getUri(Object object) {
		return null;
	}
	
	public String getPrintUri(Object object) {
		return null;
	}
	
	public String getTitleName(String name) {
		return get_title(name);
	}

	public String getName(Content content) {
		StringBuilder str = new StringBuilder();
		str.append((content.getOId()!=null ? content.getOId() : content.getId()) + "-" + content.getId());
		if (content.getTitle()!=null) 
			str.append("-" + getTitle(content));
		return str.toString();
	}
	
	public String getName(ResourceContainer container) {
		StringBuilder str = new StringBuilder();
		str.append((container.getId()!=null ? container.getId() : container.getId()) + "-" + container.getId());
		if (container.getTitle()!=null) 
			str.append("-" + getTitle(container));
		return str.toString();
	}

	public String getId(String name) {
		String segments[] = name.split("-");
		if (segments.length==2 || segments.length==3) {
			String id = segments[1];
			if (isNumeric(id)) {
				return id;
			}
		}
		return null;
	}
	
	public String getTitle(ResourceContainer container) {
		String title = container.getTitle();
		return get_title(title);
	}
	
	public String getTitle(String title) {
		return get_title(title);
	}
	
	public String getTitle(Content content) {
		String title = content.getTitle();
		return get_title(title);
	}
	
	private String get_title(String title) {
		StringBuilder text = new StringBuilder();
		if(title==null)
			return "";
		title = title.toLowerCase();
		title = title.replace("á", "a");
		title = title.replace("é", "e");
		title = title.replace("í", "i");
		title = title.replace("ó", "o");
		title = title.replace("ú", "u");
		title = title.replace("ñ", "n");
		title = title.replace("Ñ", "u");
		
		title = title.replace("\"", "");
		title = title.replace("/", "");
		title = title.replace(",", "");
		title = title.replace(":", "");
		title = title.replace(".", "");
		title = title.replace("?", "");
		title = title.replace("-", "");
		title = title.replace("<br>", "");
		title = title.replaceAll("[°,¡!?¿:\\/\"-().]|<br>", "");
		

		
		// Se pasa dos veces para los casos donde estan consecutivos dos terminos a eliminar: ej. " el que "
		//
		//
		title = title.replaceAll(" el | que | para | se | de | en | una | sin | las | del | por | con | la | lo | los | si ", " ");
		title = title.replaceAll(" el | que | para | se | de | en | una | sin | las | del | por | con | la | lo | los | si ", " ");
		
		String words[] = title.split("\\s+");
		
		boolean end = false;
		for (int w=0; w<words.length && !end; w++) {
			if (text.length()>0) 
				text.append("_");
			String word = words[w];
			text.append(word);
			if (w>=4)
				end = true;
			
		}
		return text.toString();
	}
	
	public static boolean isNumeric(String str) {  
		try	{Long.parseLong(str);}
		catch(NumberFormatException nfe) {  
			return false;  
		} 
		return true;  
	}

	
	String port;
		
	public String getJettyPort() {
		
		if (port!=null)
			return port;
		
		for (Tuple t:systemEnv()) {
			if (t.getLabel().equals("jetty.port")) {
					port=t.getValue();
					return port;
			}
		}
		port =PropertiesFactory.getInstance("kbee").getProperties().getProperty("port", "").trim();
		return port;
	}
	
	
	private List<Tuple> systemEnv() {
		return dumpVars(System.getenv());
	}


	/***
	 * 
	 * 
	 */

	private List<Tuple> dumpVars(Map<String, ?> m) {
		List<Tuple> list = new ArrayList<Tuple>(m.size());
		List<String> keys = new ArrayList<String>(m.keySet());
		  for (String k : keys) {
			  list.add(new Tuple(k,m.get(k).toString()));
		  }
		return list;
	}

}
