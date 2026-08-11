package com.novamens.content.base;

import java.util.ArrayList;
import java.util.List;

public class ResourceURI {
	
	private String[] path;
	
	public ResourceURI(String uri) {
		path = uri.split("/");
	}
	
	public List<String> getPath() {
		List<String> path = new ArrayList<>();
		for (int f=0; f<this.path.length-1; f++) {
			path.add(this.path[f]);
		}
		return path;
 	}
	
	public String getName() {
		return path[path.length-1];
	}
	
	@Override
	public String toString() {
		String string = "";
		for (String fragment : getPath()) {
			if (!"".equals(string)) string = "/" + string;
			string = fragment + string;
		}
		return string;
	}
}
