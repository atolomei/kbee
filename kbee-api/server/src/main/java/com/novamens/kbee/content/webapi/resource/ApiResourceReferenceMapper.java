 package com.novamens.kbee.content.webapi.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.wicket.core.request.mapper.AbstractResourceReferenceMapper;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.Url.QueryParameter;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.resource.ResourceReference;

public class ApiResourceReferenceMapper extends AbstractResourceReferenceMapper {

	public ApiResourceReferenceMapper()	{
	}

	public IRequestHandler mapRequest(Request request) {
		Url url = request.getUrl();
		int segments = url.getSegments().size();
		boolean mapped = false;
		Map<String, String> parameters = null;
		
		// /api/content/[content id]/resource/[resource name]
		if (segments==5 && 
				url.getSegments().get(0).equals("api") && 
				url.getSegments().get(3).equals("resource") && 
				isFile(url.getSegments().get(4))) { 
			mapped = true;
		}
		
		// /api/resource/content/[content id]/[resource name]
		if (segments==5 && 
				url.getSegments().get(0).equals("api") && 
				url.getSegments().get(1).equals("resource") && 
				isFile(url.getSegments().get(4))) { 
			mapped = true;
		}
		
		// /api/file/[source]/[domain]/[file id]/resource/[resource name]
		if (segments==7 && 
				url.getSegments().get(0).equals("api") && 
				url.getSegments().get(5).equals("resource") && 
				isFile(url.getSegments().get(6))) {
			mapped = true;
		}
		
		// /api/resource/[resource id]
		if (segments==3 && 
				url.getSegments().get(0).equals("api") && 
				url.getSegments().get(1).equals("resource")) {
			mapped = true;
		}
		
		// /api/resourceref/[resource id]/[resource name]
		if (segments==4 && 
				url.getSegments().get(0).equals("api") && 
				url.getSegments().get(1).equals("resourceref")) {
			mapped = true;
			parameters = new HashMap<String, String>();
			for (QueryParameter parameter : url.getQueryParameters()) {
				parameters.put(parameter.getName(), parameter.getValue());
			}
		}
		
		if (mapped) {
			StringBuilder name = new StringBuilder();
			for (int i = 0; i < segments; ++i)	{
				String segment = url.getSegments().get(i);
				if (name.length() > 0)	{
					name.append("/");
				}
				name.append(segment);
			}
			ResourceReference reference;
			reference = new ApiResourceReference(new URI(name.toString(), parameters));
			return new ResourceReferenceRequestHandler(reference);
		}
		return null;
	}

	protected Class<?> resolveClass(String name) {
		return WicketObjects.resolveClass(name);
	}

	protected String getClassName(Class<?> scope) {
		return scope.getName();
	}

	public Url mapHandler(IRequestHandler requestHandler) {
		
		if (!(requestHandler instanceof ResourceReferenceRequestHandler))
			return null;

		ResourceReferenceRequestHandler referenceRequestHandler = (ResourceReferenceRequestHandler)requestHandler;
		org.apache.wicket.request.resource.ResourceReference reference = referenceRequestHandler.getResourceReference();

		if (!(reference instanceof ApiResourceReference))
			return null;

		Url url = new Url();

		List<String> segments = url.getSegments();
		
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
		if (url.getSegments().size() >= 4 &&
			urlStartsWith(url, getContext().getNamespace(), getContext().getResourceIdentifier()))	{
			score = 1;
		}

		return score;
	}
	
	protected boolean isFile(String name) {
		return name.toLowerCase().endsWith("jpg") ||
				name.toLowerCase().endsWith("pdf") ||
		name.toLowerCase().endsWith("png");
	}
}
