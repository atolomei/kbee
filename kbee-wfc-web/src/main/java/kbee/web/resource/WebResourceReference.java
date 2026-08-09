package kbee.web.resource;

import org.apache.wicket.request.resource.IResource;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.document.TreeFileKBFile;
import com.novamens.content.model.ContentId;

public class WebResourceReference extends org.apache.wicket.request.resource.ResourceReference {
	private static final long serialVersionUID = 1L;
	private final URI uri;
	private int cacheDuration = 3600;

	public WebResourceReference(URI uri, int cacheDuration) {
		super(WebResource.class, uri.getEscapedPath().substring(1));
		this.uri = uri;
		this.cacheDuration = cacheDuration;
	}
	
	public WebResourceReference(Resource resource, String path, Content content) {
		super(WebResource.class, (new ContentId(content)).toString() 
				+ "/" 
				+ (path!=null?path+"/":"") 
				+ resource.getPath());
		this.uri = new URI(getName());
	}

	public WebResourceReference(Resource resource, Content content) {
		super(WebResource.class, (new ContentId(content)).toString() +"/" + resource.getPath());
		this.uri = new URI(getName());
	}
	
	public WebResourceReference(Resource resource) {
		super(WebResource.class, resource.getId() +"/" + resource.getPath());
		this.uri = new URI(getName());
	}
	
	public WebResourceReference(TreeFileKBFile resource) {
		super(WebResource.class, resource.getPath());
		this.uri = new URI(getName());
	}
	
	public WebResourceReference(URI uri) {
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
