package kbee.web.resource;

import java.io.File;

import org.apache.wicket.request.resource.IResource;
 

public class WebFileReference extends org.apache.wicket.request.resource.ResourceReference {
	private static final long serialVersionUID = 1L;
	private final URI uri;
	private int cacheDuration = 360;
	
	public static class FileId {
		private String id;
		public FileId(File file) {
			File root = new File(".");
			String rootpath = root.getAbsolutePath();
			rootpath = rootpath.substring(0, rootpath.length()-1);
			String filepath = file.getAbsolutePath();
			if (filepath.startsWith(rootpath)) {
				id = filepath.replace(rootpath, "");
				id = id.replace(File.separator, "/");
			}

			
		}
		@Override
		public String toString() {
			return id;
		}
	}

	public WebFileReference(URI uri, int cacheDuration) {
		super(WebFileResource.class, uri.getEscapedPath().substring(1));
		this.uri = uri;
		this.cacheDuration = cacheDuration;
	}
	
	public WebFileReference(File file) {
		super(WebFileResource.class, ("file/"+(new FileId(file)).toString()));
		this.uri = new URI(getName());
	}

	public WebFileReference(URI uri) {
		super(WebFileResource.class, uri.getEscapedPath().substring(1));
		this.uri = uri;
	}

	public IResource getResource() {
		WebFileResource resource = WebFileResource.get(this.getUri());
		resource.setCacheDuration(cacheDuration);
		return resource;
	}

	public URI getUri() {
		return this.uri;
	}
}
