package com.novamens.kbee.content.webapi.resource;

import java.util.HashMap;
import java.util.Map;

public class FileUrl {
	
	private String url;
	private String path = null;
	private Map<String, String> parameters;
	
	public FileUrl(String url) {
		this.url = url;
	}
	
	public String getPath() {
		if (path==null) parseurl();
		return path;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getParameter(String name) {
		if (path==null) parseurl();
		return parameters.get(name);
	}
	
	private void parseurl() {
		int i = url.indexOf("?");
		
		if (i>0) {
			path = url.substring(0,i);
		}
		else {
			path = url;
			parameters = new HashMap<String, String>();
			return;
		}
		
		path = i>0 ? url.substring(0,i) : url;
		
		parameters = new HashMap<String, String>();
		
		int s = url.indexOf("&", i);
		if (s<0) s = url.length();
		while (s>0) {
			int e = url.indexOf("=", i);
			if (e>0) {
				String name = url.substring(i+1,e);
				String value = url.substring(e+1,s);
				parameters.put(name, value);
			}
			i = s;
			if (s<url.length()) {
				s = url.indexOf("&", s+1);
				if (s<0) s = url.length();
			}	
			else
				s = -1;
		}
	}
}
