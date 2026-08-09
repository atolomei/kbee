package kbee.web.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.caching.IStaticCacheableResource;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.springframework.util.StreamUtils;


import kbee.web.resource.WebResourceResponse.WebRange;

public class WebResource extends AbstractResource {
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(WebResource.class.getName());
	
	private static final long serialVersionUID = -1L;
	private final URI uri;
	private int cacheDuration = 3600;
	private transient Instant lastModifiedTime = null;
	private transient long lastModifiedTimeUpdate = 0;

	WebResource(URI uri) {
		this.uri = uri;
	}

	public static WebResource get(URI uri) {
		return new WebResource(uri);
	}
	
	public URI getUri() {
		return uri;
	}

	@Override
	protected ResourceResponse newResourceResponse(final Attributes attributes)	{
		//final ResourceResponse response = newResourceResponse();
		final AbstractResourceResponse response = newResourceResponse();
		
		if (response.dataNeedsToBeWritten(attributes)) {
			response.setContentDisposition(ContentDisposition.INLINE);

			if (response.getFileName() != null) {
				response.setFileName(response.getFileName());
			}
			
			response.setWriteCallback(new WriteCallback() {
				@Override
				public void writeData(final Attributes attributes) {
					try {
						if (response.getAcceptRange()!=null && response.getAcceptRange().equals(ContentRangeType.BYTES)) {
							copyRange(attributes, response.getInputStream(), response.getRangeString());
						}
						else {
							writeStream(attributes, response.getInputStream());
						}
					}
					catch (IOException e) {
						logger.error(e);
					}
					catch (ResourceStreamNotFoundException e) {
						throw new ResourceException(e);
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
	
	@Override
	public void respond(final Attributes attributes) {
		// Sets the request attributes
		setRequestMetaData(attributes);

		// Get a "new" ResourceResponse to write a response
		ResourceResponse data = newResourceResponse(attributes);

		// is resource supposed to be cached?
		if (this instanceof IStaticCacheableResource)
		{
			final IStaticCacheableResource cacheable = (IStaticCacheableResource)this;

			// is caching enabled?
			if (cacheable.isCachingEnabled())
			{
				// apply caching strategy to response
				getCachingStrategy().decorateResponse(data, cacheable);
			}
		}
		// set response header
		setResponseHeaders(data, attributes);

		if (!data.dataNeedsToBeWritten(attributes) || data.getErrorCode() != null ||
			needsBody(data.getStatusCode()) == false)
		{
			return;
		}

		if (data.getWriteCallback() == null)
		{
			//return;
			throw new IllegalStateException("ResourceResponse#setWriteCallback() must be set.");
		}

		try
		{
			data.getWriteCallback().writeData(attributes);
		}
		catch (IOException iox)
		{
			throw new WicketRuntimeException(iox);
		}
	}
	
	protected AbstractResourceResponse newResourceResponse()	{
		return new WebResourceResponse(uri);
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
		if (data instanceof WebResourceResponse) {
			WebResourceResponse webresponse = (WebResourceResponse)data;
 			String value = ((ServletWebRequest)attributes.getRequest()).getHeader("Range");
 			//this.data = !value.equals("bytes=0-");
			if (webresponse.getContentLength()>10000000 && value!=null) {
				webresponse.setContentRange(value);
				WebRange range = webresponse.getRange();
				String responserange = String.format("bytes %d-%d/%d", range.getFrom(), range.getTo(), webresponse.getContentLength());
				webresponse.setAcceptRange(ContentRangeType.BYTES);
				webresponse.setContentRange(responserange);
				//if (10000000<webresponse.getContentLength()) {
					webresponse.setContentLength(range.getLenght());
					webresponse.setStatusCode(HttpServletResponse.SC_PARTIAL_CONTENT);
				//}
			}
		}
		super.setResponseHeaders(data, attributes);
	}
	
	private boolean needsBody(Integer statusCode) {
		return statusCode == null ||
			(statusCode < 300 &&
			statusCode != HttpServletResponse.SC_NO_CONTENT &&
		statusCode != HttpServletResponse.SC_RESET_CONTENT);
	}
	
	protected final void copyRange(Attributes attributes, InputStream stream, String range) throws IOException
	{
		final Response response = attributes.getResponse();
		String[] ranges = range.split("-");
		String fromstr = ranges[0];
		fromstr = fromstr.replace("bytes","");
		long from = Integer.parseInt(fromstr.trim());
		long to = 0;
		if (ranges.length == 2) {
			String tostr = ranges[1];
			int s = tostr.indexOf("/");
			tostr = tostr.substring(0,s);
			to = Integer.parseInt(tostr);
		}
		copyRange(stream, response.getOutputStream(), from, to);
		to =0;
	}
	
	private void copyRange(InputStream in, OutputStream out, long start, long end) throws IOException {
		try {
			long skipped = in.skip(start);
	
			if (skipped < start) {
				throw new IOException("Skipped only " + skipped + " bytes out of " + start + " required.");
			}
	
			long bytesToCopy = end - start + 1;
			//long copied = 0;
	
			byte buffer[] = new byte[StreamUtils.BUFFER_SIZE];
			while (bytesToCopy > 0) {
				int bytesRead = in.read(buffer);
				if (bytesRead <= bytesToCopy) {
					out.write(buffer, 0, bytesRead);
					bytesToCopy -= bytesRead;
				}
				else {
					out.write(buffer, 0, (int) bytesToCopy);
					bytesToCopy = 0;
				}
				if (bytesRead < buffer.length) {
					break;
				}
			}
		}
		catch (IOException e) {
			throw e;
		}
		catch (Exception e) {
			//throw e;
		}
	}

}
