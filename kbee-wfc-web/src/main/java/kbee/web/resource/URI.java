package kbee.web.resource;

import java.io.Serializable;

public class URI implements Serializable {
	private static final long serialVersionUID = 1L;
	private String path;
	
	public URI(String name) {
		this.path = name;
	}
	
	public String getEscapedPath() {
		return path;
	}
	
	public String getName() {
		return path;
	}
}
