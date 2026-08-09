package kbee.web.resource;

import java.util.List;
import java.util.StringTokenizer;

import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.IResource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.model.ContentId;
import com.novamens.content.model.ObjectId;
import com.novamens.thumbnail.ThumbnailSize;

@Component @Scope("prototype")
public class WebThumbnailReference extends WebResourceAbstractReference {
	private static final long serialVersionUID = 1L;
	
	//String key  = null;
	
	public WebThumbnailReference() {
	}

	public WebThumbnailReference(Resource resource, ResourceContainer container) {
		super(WebResource.class, ("thumbnail-large/"+new ObjectId(container)).toString() +"/" + resource.getName());
	}
	
	public WebThumbnailReference(Resource resource, Content content) {
		super(WebResource.class, ("thumbnail-large/"+new ContentId(content)).toString() +"/" + resource.getPath());
	}
	
	public WebThumbnailReference(Resource resource, Content content, ThumbnailSize size) {
		super(WebResource.class, "thumbnail"+"-"+ size.name().toLowerCase()+"/"+(new ContentId(content)).toString() +"/" + resource.getPath());
	}
	
	public WebThumbnailReference(Resource resource, ThumbnailSize size) {
		super(WebThumbnailResource.class, "thumbnail"+"-"+ size.name().toLowerCase()+"/" + resource.getId()+"/" + resource.getName());
//		if (resource instanceof ExternalResource) {
//			if (((ExternalResource)resource).getUrl()!=null) {
//				key = String.valueOf(((ExternalResource)resource).getUrl().hashCode()); 
//			}		
//		}
	}

	public WebThumbnailReference(URI uri) {
		super(WebResource.class, uri.getEscapedPath());
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
		
//		if (key!=null) {
//			List<QueryParameter> parameters = new ArrayList<>();
//			parameters.add(new QueryParameter("k", key));
//			url = new Url(segments, parameters);
//		}
		
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
		return new WebThumbnailReference(new URI(name.toString()));
	}
	
	public boolean handle(Url url) {
		if (url.getSegments().size()>2) {
			String segment1 = url.getSegments().get(1);
			if (segment1.startsWith("thumbnail")) {
				String sizesegments[] = segment1.split("-");
				if (sizesegments.length>1) {
					try {
						ThumbnailSize.valueOf(sizesegments[1].toUpperCase());
						return true;
					}
					catch (Exception e) {
					}
				}
				else if (sizesegments.length==1) {
					return true;
				}
			}
		}
		return false;
	}

	public IResource getResource() {
		WebResource resource = new WebThumbnailResource(this.getUri());
		resource.setCacheDuration(getCacheDuration());
		return resource;
	}
}