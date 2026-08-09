package kbee.web.resource;

import java.util.List;
import java.util.StringTokenizer;

import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.IResource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.novamens.content.base.Resource;
import com.novamens.content.service.TokenService;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailSize;

@Component @Scope("prototype")
public class WebThumbnailSharedReference extends WebResourceAbstractReference {
	private static final long serialVersionUID = 1L;
	
	public WebThumbnailSharedReference() {
	}
	
	public WebThumbnailSharedReference(Resource resource, ThumbnailSize size) {
		super(WebResource.class, "shared-thumbnail/" + getToken(resource, size));
	}
	
	public WebThumbnailSharedReference(URI uri) {
		super(WebResource.class, uri.getEscapedPath());
	}
	
	private static String getToken(Resource resource, ThumbnailSize size) {
		KbeeJson data = new KbeeJson();
		data.put("resource", String.valueOf(resource.getId()));
		data.put("size", size.name());
		return ServiceLocator.getService(TokenService.class).getToken(data);
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
		return new WebThumbnailSharedReference(new URI(name.toString()));
	}
	
	public boolean handle(Url url) {
		return url.getSegments().size()==3 && "shared-thumbnail".equals(url.getSegments().get(1));
	}

	public IResource getResource() {
		WebResource resource = new WebThumbnailSharedResource(this.getUri());
		resource.setCacheDuration(getCacheDuration());
		return resource;
	}
}