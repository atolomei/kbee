package kbee.web.resource;

import java.util.List;
import java.util.StringTokenizer;

import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.IResource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component @Scope("prototype")
public class WebPdfFileReference extends WebResourceAbstractReference {
	private static final long serialVersionUID = 1L;

	public WebPdfFileReference() {
	}
	
	public WebPdfFileReference(URI uri, int cacheDuration) {
		super(WebFileResource.class, uri.getEscapedPath());
	}

	public WebPdfFileReference(URI uri) {
		super(WebFileResource.class, uri.getEscapedPath());
	}

	public IResource getResource() {
		WebPdfFileResource resource = WebPdfFileResource.get(this.getUri());
		return resource;
	}

	
	public WebResourceAbstractReference getReference(Url url) {
		StringBuilder name = new StringBuilder();
		int segmentsSize = url.getSegments().size();
		for (int i = 1; i < segmentsSize; ++i)	{
			String segment = url.getSegments().get(i);
			if (name.length() > 0)	{
				name.append("/");
			}
			name.append(segment);
		}
		return new WebPdfFileReference(new URI(name.toString()));
	}
	
	public Url getUrl() {
		Url url = new Url();

		List<String> segments = url.getSegments();
		segments.add("resource");

		StringTokenizer tokens = new StringTokenizer(getName(), "/");

		while (tokens.hasMoreTokens())	{
			String token = tokens.nextToken();
			segments.add(token);
		}
		
		return url;
	}
	
	public boolean handle(Url url) {
		if (url.getSegments().size()>2) {
			String segment0 = url.getSegments().get(0);
			if (segment0.startsWith("pdfserver")) {
				return true;
			}
		}
		return false;
	}
}