package com.novamens.kbee.content.webapi.resource;

import org.apache.wicket.request.resource.IResource;

public class ApiResourceReference extends org.apache.wicket.request.resource.ResourceReference {
	private static final long serialVersionUID = 1L;
	private final URI uri;
	private int cacheDuration = 3600;

	
	public ApiResourceReference(URI uri) {
		super(ApiResource.class, uri.getEscapedPath().substring(1));
		this.uri = uri;
	}

	public IResource getResource() {
		ApiResource resource = ApiResource.get(this.getUri());
		resource.setCacheDuration(cacheDuration);
		return resource;
	}
	
	public URI getUri() {
		return this.uri;
	}
}
