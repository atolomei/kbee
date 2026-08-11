package com.novamens.kbee.dom;

import java.util.Map;

import com.novamens.dom.Url;

public class KbeeUrl implements Url {
	
	String path;
	Map<String, String> parameters;
	
	public KbeeUrl(String path, Map<String, String> parameters) {
		this.path = path;
		this.parameters = parameters;
	}
	
	public String getPath() {
		return path;
	}
	
	public Map<String, String> getParameters() {
		return parameters;
	}
}