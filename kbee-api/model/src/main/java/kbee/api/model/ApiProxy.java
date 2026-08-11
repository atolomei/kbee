package kbee.api.model;

import java.io.Serializable;

public class ApiProxy implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String id;
	private String href;
	private String name;
	private String rel;
	
	public ApiProxy() {
	}
	
	public ApiProxy(String uri) {
		this.href = uri;
	}
	
	public ApiProxy(String name, String uri) {
		this.name = name;
		this.href = uri;
	}
	
	public ApiProxy(String id, String name, String uri, String rel) {
		this.id = id;
		this.name = name;
		this.href = uri;
		this.rel = rel;
	}
	
	public ApiProxy(String name, String uri, String rel) {
		this.name = name;
		this.href = uri;
		this.rel = rel;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String name) {
		this.id = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getHRef() {
		return href;
	}
	
	public void setHRef(String href) {
		this.href = href;
	}
	
	public String getRel() {
		return rel;
	}
	
	public void setRel(String rel) {
		this.rel = rel;
	}
}
