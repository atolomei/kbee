package com.novamens.kbee.content.webapi.resource;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpStatus;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.http.WebResponse.CacheScope;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.AbstractResource.ResourceResponse;
import org.apache.wicket.request.resource.IResource.Attributes;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ContentId;
import com.novamens.content.resource.ExternalResource;
import com.novamens.content.resource.KBFile;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.FileService;
import com.novamens.content.service.kbfs.KBFSResourceService;
import com.novamens.kbee.content.resource.KbeeExternalResource;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailService;
import com.novamens.thumbnail.ThumbnailSize;

import kbee.util.FSUtils;

public class ApiResourceResponse extends ResourceResponse {

	private transient InputStream inputStream;
	private transient Resource resource;

	private final URI uri;
	
	public class WebRange {
		long from, to;
		public WebRange(long from, long to) {
			this.to = to;
			this.from = from;
		}
		public long getTo() {
			return to;
		}
		public long getFrom() {
			return from;
		}
		public long getLenght() {
			return to-from+1;
		}
	}

	public ApiResourceResponse(URI uri) {
		this.uri = uri;
		setFileName(uri.getSegments().get(uri.getSegments().size()-1));
		//setTextEncoding("UTF8");
	}

	public URI getURI() {
		return uri;
	}

	public void close() throws IOException {
		if (inputStream != null) {
			inputStream.close();
			inputStream = null;
		}
	}

	/**
	 * @return
	 * @throws ResourceStreamNotFoundException
	 */
	public InputStream getInputStream() throws ResourceStreamNotFoundException {
		if (inputStream == null) {
			try {
				Resource resource = getResource();
				if (resource!=null && resource instanceof ExternalResource) {
					inputStream = getThumbnail((KbeeExternalResource) resource, ThumbnailSize.LARGE);
				} 
				else if (resource!=null && resource instanceof KBFile) {
					KBFile file = (KBFile)resource;
					if (!isThumbnail()) {
						inputStream  = file.getService(KBFSResourceService.class).getObject();
					}
					else  {
						inputStream  = file.getService(FileService.class).getThumbnail(getThumbnailSize());
					}
				}	
				else {
					setError(HttpStatus.SC_NOT_FOUND);
					throw new ResourceStreamNotFoundException("Resource " + uri	+ " not found");
				}	
			}
			catch (IOException e) {
				throw new ResourceStreamNotFoundException("Resource " + uri
					+ " could not be opened", e);
			}
			catch (Exception e) {
				throw new ResourceStreamNotFoundException("Resource " + uri
					+ " could not be opened", e);
			}
		}
		
		return inputStream;
	}

	/** ----------------------------------------------------------------------------------
	 */
	@Override
	public String getContentType() {
		try {
			Resource resource = getResource();
			return getMimeType(resource);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Instant getLastModified() {
		try {
			Resource resource = getResource();
			if (resource!=null) {
				return OffsetDateTime.now().toInstant();
				//return resource.getLastModifiedOffsetDateTime().toInstant();
			}
			else
				return null;
		}
		catch (SecurityException e) {
			return Instant.ofEpochMilli(System.currentTimeMillis());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return uri.toString();
	}
	

	@Override
	public ContentDisposition getContentDisposition() {
		String disposition = uri.getParameters()!=null ? uri.getParameters().get("disposition") : null;
		return disposition!=null && disposition.equals("attachment") ? ContentDisposition.ATTACHMENT : ContentDisposition.INLINE;
	}
	
	@Override
	public long getContentLength() {
		try {
			if (super.getContentLength()>0)
				return super.getContentLength();
			Resource resource = getResource();
			if (resource!=null) {
				if (isThumbnail()) {
					File file = ((KBFile)resource).getService(FileService.class).getThumbnailFile(getThumbnailSize());
					long size = file.length();
					return size;
				}
				else {
					long size = 0;
					size = ((KBFile)resource).getSize();
					if (size==0) {
						File file = ((KBFile)resource).getFile();
						size = file.length();
					}
					setContentLength(size);
					return size;
				}
			}	
			else
				return 0;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public InputStream getThumbnail(KbeeExternalResource resource, ThumbnailSize size) {
		try {
			String domain_name=resource.getDomain().getName();
			File file = ServiceLocator.getService(ThumbnailService.class).getThumbnailFile(resource.getId().toString(), domain_name, resource.getUrl(), size);
			return new FileInputStream(file);
		} 
		catch (IOException e) {
			throw new RuntimeException(e);
		}	
	}

	public Resource getResource() throws IOException {
		if (resource==null) {
			if (getURI().getSegments().size()==5) {
				ContentId contentId = getContentId();
				if (contentId!=null) {
					Content content = getContentDao().findContentById(contentId);
					if (content instanceof ResourceContainer && ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(content)) {
						String resourcename = getResourceName();
						resource =  ((ResourceContainer)content).getResource(resourcename);
					}
				}
			}
			else 
			if (getURI().getSegments().size()==7) {
				String source = getSource();
				if (source!=null) {
					String externalId = getExternalId();
					if (externalId!=null) {
						Content content = getContentDao().findContentByExternalId(source, externalId);
						if (content instanceof ResourceContainer && ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(content)) {
							String resourcename = getResourceName();
							resource =  ((ResourceContainer)content).getResource(resourcename);
						}
					}
				}
			}
			else
			if (getURI().getSegments().size()==4) {
				Long resourceId = getResourceId();
				if (resourceId!=null) {
					resource = getContentDao().findResourceById(KBFile.class, resourceId);
				}
				if ("resourceref".equals(getURI().getSegments().get(1)) && resource!=null) {
					if (!validSecurityToken(resource)) {
						setError(HttpStatus.SC_FORBIDDEN);
						throw new SecurityException();
					}
				}
			}
			else
			if (getURI().getSegments().size()==3) {
				Long resourceId = getResourceId();
				if (resourceId!=null) {
					resource = getContentDao().findResourceById(KBFile.class, resourceId);
				}
			}
		}
		return resource;
	}
	
	public ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}	

	public String getExternalId() {
		List<String> segments = getURI().getSegments();
		if (segments.size()<7) return null;
		String externalId = segments.get(4);
		return externalId;
	}
	
	public ContentId getContentId() {
		List<String> segments = getURI().getSegments();
		if (segments.size()<5) return null;
		String id = "resource".equals(segments.get(1)) ? segments.get(3) : segments.get(2);
		String arr[] = id.split("-");
		if (arr.length>2)
			return new ContentId(arr[1], arr[2]);
		else	
			return null;
	}
	
	public String getSource() {
		List<String> segments = getURI().getSegments();
		if (segments.size()<7) return null;
		String source = segments.get(2);
		return source;
	}
	
	public String getResourceName() {
		List<String> segments = getURI().getSegments();
		String resourceName = segments.get(segments.size()-1);
		return resourceName;
	}
	
	public Long getResourceId() {
		List<String> segments = getURI().getSegments();
		if (segments.size()<3) return null;
		try {
			return Long.valueOf(segments.get(2));
		}
		catch (NumberFormatException e) {
		}
		return null;
	}
	
	public boolean dataNeedsToBeWritten(Attributes attributes) {
		WebRequest request = (WebRequest)attributes.getRequest();
		Instant ifModifiedSince = request.getIfModifiedSinceHeader();

		if (getCacheDuration()!= Duration.ZERO && ifModifiedSince != null && getLastModified() != null)	{
			// [Last-Modified] headers have a maximum precision of one second
			// so we have to truncate the milliseconds part for a proper compare.
			// that's stupid, since changes within one second will not be reliably
			// detected by the client ... any hint or clarification to improve this
			// situation will be appreciated...
			Instant roundedLastModified = Instant.ofEpochMilli(getLastModified().toEpochMilli() / 1000 * 1000);

			return ifModifiedSince.isBefore(roundedLastModified);
		}
		else {
			return true;
		}
	}
	
	public Duration getCacheDuration()	{
		return Duration.ofHours(1);
	}

	/**
	 * returns what kind of caches are allowed to cache the resource response
	 * <p/>
	 * resources are only cached at all if caching is enabled by setting a cache duration.
	 * 
	 * @return cache scope
	 * 
	 */
	public WebResponse.CacheScope getCacheScope() {
		return CacheScope.PUBLIC;
	}
	
	public boolean acceptRanges() {
		return getRangeString()!=null;
	}
	
	public WebRange getRange() {
			String range = getRangeString();
			if (range!=null) {
				String[] ranges = range.split("=")[1].split("-");
				long from, to;
				from = Integer.parseInt(ranges[0]);
				if (ranges.length == 2) {
					to = Integer.parseInt(ranges[1]);
					if (to==from) {
						from = 0;
						to = getContentLength()-1;
					}
				}
				else
					to = getContentLength()-1;
				return new WebRange(from, to);
			}
			else  {
				return null;
			}
	}
	
	public String getRangeString() {
		Request request = RequestCycle.get().getRequest();
		Object containerrequest = request.getContainerRequest();
		if (containerrequest instanceof HttpServletRequest) {
			return ((HttpServletRequest)containerrequest).getHeader("Range");
		}
		else {
			return null;
		}
	}
	
	public boolean isThumbnail() {
		return getThumbnailSize()!=null;
	}
	
	public ThumbnailSize getThumbnailSize() {
		String thumbnailParameter = getURI().getParameters()!=null ? getURI().getParameters().get("thumbnail-size") : null;
		ThumbnailSize thumbnailSize = thumbnailParameter!=null ? ThumbnailSize.valueOf(thumbnailParameter) : null;
		return thumbnailSize;
	}
	
	private String getMimeType(Resource resource) {
		if (resource!=null) {
			if (isThumbnail())  {
				if (!FSUtils.isImage(resource.getName()))  {
					return "image/jpeg"; 
				}
			}
			if (FSUtils.isPdf(resource.getName()))
				return "application/pdf";
			if (FSUtils.isImage(resource.getName()))  {
				String str = FilenameUtils.getExtension(resource.getName());
				if (str.equals("jpg"))
					return "image/jpeg"; 
				return "image/"+str;
			}
			if (FSUtils.isVideo(resource.getName())) {
				return "video/"+FilenameUtils.getExtension(resource.getName());
			}
			if (FSUtils.isAudio(resource.getName()))
				return "audio/"+FilenameUtils.getExtension(resource.getName());
			if (resource instanceof KBFile)
				return ((KBFile) resource).getContentType();
			return null;
		}	
		else
			return null;
	}
	
	private boolean validSecurityToken(Resource resource) {
		if (getURI().getParameters()==null) return false;
		String token = getURI().getParameters().get("token");
		if (token==null || !ServiceLocator.getService(SecurityService.class).isValid(token)) {
			return false;
		};
		Serializable tokenid = ServiceLocator.getService(SecurityService.class).getId(token);
		if (!resource.getId().equals(tokenid)) {
			return false;
		}
		return true;
	}
}
