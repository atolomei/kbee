package com.novamens.kbee.content.webapi.resource;

import java.io.IOException;
import java.time.Instant;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;

public class ApiResource extends AbstractResource {
	private static final long serialVersionUID = -1L;
	private URI uri;
	private int cacheDuration = 3600;
	private transient Instant lastModifiedTime = null;
	private transient long lastModifiedTimeUpdate = 0;

	ApiResource(URI uri) {
		this.uri = uri;
	}

	public static ApiResource get(URI uri) {
		return new ApiResource(uri);
	}

	@Override
	protected ResourceResponse newResourceResponse(final Attributes attributes)	{
		final ApiResourceResponse response = new ApiResourceResponse(uri);

		if (response.dataNeedsToBeWritten(attributes)) {
			response.setContentDisposition(ContentDisposition.INLINE);

			if (response.getFileName() != null) {
				response.setFileName(response.getFileName());
			}
			
			response.setWriteCallback(new WriteCallback() {
				@Override
				public void writeData(final Attributes attributes) {
					try {
						writeStream(attributes, response.getInputStream());
					}
					catch (IOException e) {
					}
					catch (ResourceStreamNotFoundException e) {
					}
					finally {
						try {
							response.close();
						}
						catch (IOException e) {
						}
					}
				}
			});
		}

		return response;
	}

	public String getResourceName() {
		return this.uri.getName();
	}

	/**
	 * Returns the last modified time of resource
	 * 
	 * @return last modified time or null if the time can not be determined
	 */
	public Instant lastModifiedTime() {
		if (lastModifiedTimeUpdate == 0
				|| lastModifiedTimeUpdate < System.currentTimeMillis() - 5
						* (1000 * 60)) {
			lastModifiedTime = newResourceResponse(null).getLastModified();
			lastModifiedTimeUpdate = System.currentTimeMillis();
		}
		return lastModifiedTime;
	}
	
	public void setCacheDuration(int time) {
		this.cacheDuration = time;
	}
	
	protected int getCacheDuration() {
		return cacheDuration;
	}
	
	protected void configureCache(final ResourceResponse data, final Attributes attributes)	{
		super.configureCache(data, attributes);
		Response response = attributes.getResponse();
		if (response instanceof WebResponse) {
			WebResponse webResponse = (WebResponse)response;
			webResponse.setHeader("Cache-Control", "public, max-age=558572011, must-revalidate, post-check=0, pre-check=0");
		}
	}
	
	@Override
	protected void setResponseHeaders(final ResourceResponse data, final Attributes attributes) {
		Request request = RequestCycle.get().getRequest();
		Object cr = request.getContainerRequest();
		((HttpServletRequest)cr).getHeaderNames();
		String h = ((HttpServletRequest)cr).getHeader("Range");
		if (h!=null) {
	        final String responseRange = String.format("bytes %d-%d/%d", 0, data.getContentLength()-1, data.getContentLength());
			data.getHeaders().addHeader("Accept-Ranges", "bytes");
			data.getHeaders().addHeader("Content-Range", responseRange);
		}
		super.setResponseHeaders(data, attributes);
	}
}
