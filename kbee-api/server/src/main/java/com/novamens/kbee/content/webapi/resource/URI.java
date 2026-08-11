package com.novamens.kbee.content.webapi.resource;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class URI implements Serializable {
	private static final long serialVersionUID = 1L;
	private String path;
	private Map<String, String> parameters;
	private List<String> segments;
	
	public URI(String name, Map<String, String> parameters) {
		this.path = name;
		this.parameters = parameters;
		segments = Arrays.asList(path.split("/"));
	}
	
	public String getEscapedPath() {
		return path;
	}
	
	public String getName() {
		return path;
	}
	
	public Map<String, String> getParameters() {
		return parameters;
	}
	
	public List<String> getSegments() {
		return segments;
	}
}
