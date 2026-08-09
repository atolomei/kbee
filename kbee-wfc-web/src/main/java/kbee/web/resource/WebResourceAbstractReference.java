package kbee.web.resource;

import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.ResourceReference;

public abstract class WebResourceAbstractReference extends ResourceReference {
	private static final long serialVersionUID = 1L;
	
	private URI uri;
	private int cacheDuration = 3600;
	
	public WebResourceAbstractReference() {
		super("");
	}
	
	public WebResourceAbstractReference(Class<?> scope, String name) {
		super(scope, name);
		this.uri = new URI(getName());
	}
	
	public boolean handle(Url url) {
		return false;
	}
	
	public abstract WebResourceAbstractReference getReference(Url url);
	
	public abstract Url getUrl();
	
	public URI getUri() {
		return this.uri;
	}
	
	public int getCacheDuration() {
		return cacheDuration;
	}
}