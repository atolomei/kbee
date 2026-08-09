 package kbee.web.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.wicket.core.request.mapper.AbstractResourceReferenceMapper;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.service.ServiceLocator;
import com.novamens.spring.service.SpringServiceLocator;


public class WebResourceReferenceMapper extends AbstractResourceReferenceMapper {
	
	private List<WebResourceAbstractReference> referencePrototypes = null;

	public WebResourceReferenceMapper()	{
	}

	public IRequestHandler mapRequest(Request request) {
		Url url = request.getUrl();
		
		ResourceReference reference = getReference(url);
		if (reference!=null) {
			return new ResourceReferenceRequestHandler(reference);
		}
		
		if (url.getSegments().size()>2 && 
				url.getSegments().get(0).equals("resource") && 
				(url.getSegments().get(1).equals("content")
					|| url.getSegments().get(1).equals("thumbnail")
					|| url.getSegments().get(1).equals("thumbnail-large")
					|| url.getSegments().get(1).equals("thumbnail-small")
					|| url.getSegments().get(1).equals("thumbnail-mini")
					|| url.getSegments().get(1).equals("thumbnail-medium")
					|| url.getSegments().get(1).equals("thumbnail-avatar_status")
					|| url.getSegments().get(1).equals("file")
					|| url.getSegments().get(1).equals("kbfile")
					|| url.getSegments().get(1).equals("thumbnail-W980")
			)) {
			
			StringBuilder name = new StringBuilder();
			int segmentsSize = url.getSegments().size();
			for (int i = 2; i < segmentsSize; ++i)	{
				String segment = url.getSegments().get(i);
				if (name.length() > 0)	{
					name.append("/");
				}
				name.append(segment);
			}
			if (url.getSegments().get(1).equals("thumbnail"))
				reference = new WebThumbnailReference(new URI("thumbnail/"+name.toString()));
			else if (url.getSegments().get(1).equals("thumbnail-large"))
				reference = new WebThumbnailReference(new URI("thumbnail-large/"+name.toString()));
			else if (url.getSegments().get(1).equals("thumbnail-small"))
				reference = new WebThumbnailReference(new URI("thumbnail-small/"+name.toString()));
			else if (url.getSegments().get(1).equals("thumbnail-W980"))
				reference = new WebThumbnailReference(new URI("thumbnail-W980/"+name.toString()));
			else if (url.getSegments().get(1).equals("thumbnail-mini"))	
				reference = new WebThumbnailReference(new URI("thumbnail-mini/"+name.toString()));
			else if (url.getSegments().get(1).equals("thumbnail-medium"))	
				reference = new WebThumbnailReference(new URI("thumbnail-medium/"+name.toString()));
			else if (url.getSegments().get(1).equals("thumbnail-avatar_status"))
				reference = new WebThumbnailReference(new URI("thumbnail-avatar_status/"+name.toString()));
			else if (url.getSegments().get(1).equals("kbfile"))
				reference = new WebResourceReference(new URI(name.toString()));
			else if (url.getSegments().get(1).equals("file"))
				reference = new WebFileReference(new URI("file/"+name.toString()));
			else
				reference = new WebResourceReference(new URI(name.toString()));
			return new ResourceReferenceRequestHandler(reference);
			
		}
		
		if (url.getSegments().size()==3 && url.getSegments().get(1).equals("shared")) {
			reference = new  SharedResourceReference(new URI("shared/resource/"+url.getSegments().get(2)));
			return new ResourceReferenceRequestHandler(reference);
		}

		return null;
	}



	public Url mapHandler(IRequestHandler requestHandler) {
		
		if (!(requestHandler instanceof ResourceReferenceRequestHandler))
			return null;

		ResourceReferenceRequestHandler referenceRequestHandler = (ResourceReferenceRequestHandler)requestHandler;
		org.apache.wicket.request.resource.ResourceReference reference = referenceRequestHandler.getResourceReference();
	
		
		if (reference instanceof WebResourceAbstractReference) {
			return ((WebResourceAbstractReference)reference).getUrl();
		}

		if (!(reference instanceof WebResourceReference || 
			reference instanceof WebThumbnailReference || 
			reference instanceof SharedResourceReference || 
			reference instanceof WebFileReference))
			return null;

		Url url = new Url();

		List<String> segments = url.getSegments();
		segments.add(getContext().getResourceIdentifier());

		if (reference instanceof WebResourceReference) {
			segments.add("content");
		}
		
		if (reference instanceof SharedResourceReference) {
			segments.add("shared");
		}

		StringTokenizer tokens = new StringTokenizer(reference.getName(), "/");

		while (tokens.hasMoreTokens())	{
			String token = tokens.nextToken();
			segments.add(token);
		}
 
		return url;
	}

	public int getCompatibilityScore(Request request) {
		Url url = request.getUrl();

		int score = -1;
		if (url.getSegments().size() >= 3 &&
			urlStartsWith(url, getContext().getNamespace(), getContext().getResourceIdentifier()))	{
			score = 1;
		}

		return score;
	}
	
	protected ResourceReference getReference(Url url) {
		for (WebResourceAbstractReference prototype : getReferencePrototypes()) {
			if (prototype.handle(url)) {
				return prototype.getReference(url);
			}
		}
		return null;
	}
	
	protected List<WebResourceAbstractReference> getReferencePrototypes() {
		if (referencePrototypes==null) {
			referencePrototypes = new ArrayList<WebResourceAbstractReference>();
			SpringServiceLocator serviceLocator = (SpringServiceLocator)ServiceLocator.getInstance();
			Map<String, WebResourceAbstractReference> beans = serviceLocator.getContext().getBeansOfType(WebResourceAbstractReference.class);
			for (String bean : beans.keySet()) {
				WebResourceAbstractReference reference = (WebResourceAbstractReference)serviceLocator.getContext().getBean(bean);
				referencePrototypes.add(reference);
			}
		}
		return referencePrototypes;
	}
	
	protected Class<?> resolveClass(String name) {
		return WicketObjects.resolveClass(name);
	}

	protected String getClassName(Class<?> scope) {
		return scope.getName();
	}
}