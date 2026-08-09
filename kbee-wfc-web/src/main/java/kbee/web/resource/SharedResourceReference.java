package kbee.web.resource;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.document.TreeFileKBFile;
import com.novamens.kbee.content.resource.SharedResourceToken;

public class SharedResourceReference extends ResourceReference {
	private static final long serialVersionUID = 1L;
	private final URI uri;
	private int cacheDuration = 3600;

	public SharedResourceReference(URI uri, int cacheDuration) {
		super(WebResource.class, uri.getEscapedPath().substring(1));
		this.uri = uri;
		this.cacheDuration = cacheDuration;
	}

	public SharedResourceReference(Resource resource, Content content) {
		super(WebResource.class, (new SharedResourceToken(resource, content)).toString());
		//super(WebResource.class, (new ContentId(content)).toString() +"/" + resource.getPath());
		this.uri = new URI(getName());
	}
	
	public SharedResourceReference(Resource resource) {
		super(WebResource.class, (new SharedResourceToken(resource)).toString());
		this.uri = new URI(getName());
	}
	
	public SharedResourceReference(TreeFileKBFile resource) {
		super(WebResource.class, resource.getPath());
		this.uri = new URI(getName());
	}
	
	public SharedResourceReference(URI uri) {
		super(WebResource.class, uri.getEscapedPath().substring(1));
		this.uri = uri;
	}

	public IResource getResource() {
		WebResource resource = WebResource.get(this.getUri());
		resource.setCacheDuration(cacheDuration);
		return resource;
	}

	public URI getUri() {
		return this.uri;
	}
}