package kbee.web.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.StringTokenizer;

import org.apache.commons.io.FilenameUtils;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.http.WebResponse.CacheScope;
import org.apache.wicket.request.resource.AbstractResource.ContentRangeType;
import org.apache.wicket.request.resource.IResource.Attributes;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.hibernate.proxy.HibernateProxy;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.base.ResourceURI;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.document.TreeFile;
import com.novamens.content.model.ContentId;
import com.novamens.content.resource.ExternalResource;
import com.novamens.content.resource.KBFile;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.FileService;
import com.novamens.content.service.TokenService;
import com.novamens.content.service.kbfs.KBFSResourceService;
import com.novamens.dom.Json;
import com.novamens.kbee.content.document.KbeeTreeFile;
import com.novamens.kbee.content.document.KbeeTreeFileKBFile;
import com.novamens.kbee.content.resource.KbeeExternalResource;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailService;
import com.novamens.thumbnail.ThumbnailSize;

import kbee.util.FSUtils;

public class WebResourceResponse extends AbstractResourceResponse {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(WebResourceResponse.class.getName());
	
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

	public WebResourceResponse(URI uri) {
		super(uri);
		this.uri = uri;
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
	 * 
	 * @throws ResourceStreamNotFoundException
	 */
	public InputStream getInputStream() throws ResourceStreamNotFoundException {
		if (inputStream == null) {
			
			try {
				
				Resource resource = getResource();
				
				if (resource instanceof HibernateProxy) {
					resource = (Resource)getContentDao().reload(resource);
				}
				
				if (resource!=null && resource instanceof ExternalResource) {
					inputStream = getThumbnailYoutube((KbeeExternalResource) resource, ThumbnailSize.LARGE);
				} 
				else if (resource!=null && resource instanceof KBFile) {

					KBFile file = (KBFile)resource;
					
					if (getURI().getEscapedPath().contains("thumbnail-large")) {
						inputStream = file.getService(FileService.class).getThumbnail(ThumbnailSize.LARGE);
					}
					else if (getURI().getEscapedPath().contains("thumbnail-small")) {
						inputStream = file.getService(FileService.class).getThumbnail(ThumbnailSize.SMALL);
					}
					else if (getURI().getEscapedPath().contains("thumbnail-mini")) {
						inputStream = file.getService(FileService.class).getThumbnail(ThumbnailSize.MINI);
					}
					else if (getURI().getEscapedPath().contains("thumbnail-avatar_status")) {
						inputStream = file.getService(FileService.class).getThumbnail(ThumbnailSize.AVATAR_STATUS);
					}
					else if (getURI().getEscapedPath().contains("thumbnail-W980")) {
						inputStream = file.getService(FileService.class).getThumbnail(ThumbnailSize.W980);
					}
					else if (getURI().getEscapedPath().contains("thumbnail")) {
						inputStream = file.getService(FileService.class).getThumbnail(ThumbnailSize.MEDIUM);
					}
					else
						inputStream  = file.getInputStream();
				}	
				else {
					logger.error(("Resource " + uri.getName() + " not found") + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
					throw new ResourceStreamNotFoundException("Resource " + uri.getName()	+ " not found");
				}
			}
			catch (IOException e) {
				logger.error(e.getClass().getName() + " | " + ("Resource " + uri.getName()	+ " could not be opened") + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
				throw new ResourceStreamNotFoundException("Resource " + uri.toString() + " could not be opened", e);
			}
			catch (Exception e) {
				logger.error(e.getClass().getName() + " | " + ("Resource " + uri.getName()	+ " could not be opened") + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
				throw new ResourceStreamNotFoundException("Resource " + uri	+ " could not be opened", e);
			}
		}
		
		return inputStream;
	}
	
	public InputStream openStream() throws IOException {
		return null;
	}

	/**
	 * 
	 * 
	 * 
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
	public String getFileName() {
		try {
			Resource resource = getResource();
			if (resource!=null) {
				return resource.getName();
			}
			else
				return null;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * 
	 * 
	 */

	@Override
	public Instant getLastModified() {
		try {
			Resource resource = getResource();
			if (resource!=null) {
				return resource.getLastModifiedOffsetDateTime().toInstant();
			}
			else
				return null;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * 
	 * 
	 */

	@Override
	public String toString() {
		return uri.toString();
	}

	/**
	 * 
	 * 
	 * 
	 */

	@Override
	public long getContentLength() {
		try {
			if (super.getContentLength()>0)
				return super.getContentLength();
			Resource resource = getResource();
			if (resource!=null) {
				long size = 0;
				if (resource instanceof ExternalResource && resource.getUrl()!=null && resource.getUrl().contains("youtube.com")) {
					File iofile = getThumbnailYoutubeFile((KbeeExternalResource) resource, ThumbnailSize.LARGE);
					size = iofile.length();
				} 
				else if (getURI().getEscapedPath().contains("thumbnail-large")) {
					KBFile file = (KBFile)resource;
					File iofile = file.getService(FileService.class).getThumbnailFile(ThumbnailSize.LARGE);
					size = iofile.length();
				} 
				else if (getURI().getEscapedPath().contains("thumbnail-small")) {
					KBFile file = (KBFile)resource;
					File iofile = file.getService(FileService.class).getThumbnailFile(ThumbnailSize.SMALL);
					size = iofile.length();
				}
				else if (getURI().getEscapedPath().contains("thumbnail-mini")) {
					KBFile file = (KBFile)resource;
					File iofile = file.getService(FileService.class).getThumbnailFile(ThumbnailSize.MINI);
					size = iofile.length();
				}
				else if (getURI().getEscapedPath().contains("thumbnail-medium")) {
					KBFile file = (KBFile)resource;
					File iofile = file.getService(FileService.class).getThumbnailFile(ThumbnailSize.MEDIUM);
					size = iofile.length();
				}
				else if (getURI().getEscapedPath().contains("thumbnail-avatar_status")) {
					KBFile file = (KBFile)resource;
					File iofile = file.getService(FileService.class).getThumbnailFile(ThumbnailSize.AVATAR_STATUS);
					size = iofile.length();
				} 
				else if (getURI().getEscapedPath().contains("thumbnail-W980")) {
					KBFile file = (KBFile)resource;
					File iofile = file.getService(FileService.class).getThumbnailFile(ThumbnailSize.W980);
					size = iofile.length();
				} 
				else if (getURI().getEscapedPath().contains("thumbnail")) {
					KBFile file = (KBFile)resource;
					File iofile = file.getService(FileService.class).getThumbnailFile(ThumbnailSize.LARGE);
					size = iofile.length();
				}
				else {
					//size = ((KBFile)resource).getSize();
					size = resource.getSize();
					if (size==0) {
						File file = ((KBFile)resource).getFile();
						size = file!=null ? file.length() : 0;
					}
				}
				setContentLength(size);
				return size;
			}	
			else
				return 0;
		}
		catch (IOException e) {
			if (e.getMessage().contains("File System. File not in disk"))
				logger.debug(e);
			else
				logger.error(e);
			throw new ResourceException(e);
		}
	}

	
	/**
	 * 
	 * 
	 * 
	 */
	public InputStream getThumbnailYoutube(KbeeExternalResource resource, ThumbnailSize size) {
		try {
			String domain_name=resource.getDomain().getName();
			File file = ServiceLocator.getService(ThumbnailService.class).getThumbnailFile(resource.getId().toString(), domain_name, resource.getUrl(), size);
			return new FileInputStream(file);
		} 
		catch (IOException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}	
	}
	
	/**
	 * 
	 * 
	 */
	public Resource getResource() throws IOException {
		if (resource==null) {
			ContentId contentId = getContentId();
			if (contentId!=null) {
				Content content = getContentDao().findContentById(contentId);
				if (content instanceof ResourceContainer && (ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(content)||getURI().getName().startsWith("shared/resource"))) {
					String resourcename = getResourceName();
					if (resourcename!=null && resourcename.startsWith("fs")) {
						int s = resourcename.indexOf("/");
						if (s>0)
						resource = getTreeNode(resourcename.substring(0,s));
					}
					else {
						if (resourcename.contains("/")) {
							StringTokenizer tokenizer = new StringTokenizer(resourcename, "/");
							String normalized = "";
							while (tokenizer.hasMoreTokens()) {
								String fragment = tokenizer.nextToken();
								//fragment = content.getService(KBFSResourceService.class).normalize(fragment);
								if (!"".equals(normalized)) {
									normalized += "/";
								}
								normalized += fragment;
							}
							resource = ((ResourceContainer)content).getResource(new ResourceURI(normalized));
							if (resource==null) {
								StringTokenizer tokenizer2 = new StringTokenizer(resourcename, "/");
								String normalized2 = "";
								while (tokenizer2.hasMoreTokens()) {
									String fragment2 = tokenizer2.nextToken();
									fragment2 = content.getService(KBFSResourceService.class).normalize(fragment2);
									if (!"".equals(normalized2)) {
										normalized2 += "/";
									}
									normalized2+= fragment2;
								}
								resource = ((ResourceContainer)content).getResource(new ResourceURI(normalized2));
							}
						}
						else {
							String normalized = content.getService(KBFSResourceService.class).normalize(resourcename);
							resource = ((ResourceContainer)content).getResource(normalized);
							if (resource==null) {
								resource = ((ResourceContainer)content).getResource(resourcename);
							}
						}
					}
				}
			}
			else {
				String resourceId = getResourceId();
				if (resourceId!=null) {
					if (resourceId.startsWith("fs")) {
						resource = getTreeNode(resourceId);
					}
					else {
						resource = getResource(resourceId);
					}
				}
			}
		}
		return resource;
	}
	
	public Resource loadResource() throws IOException {
		if (resource==null) {
			ContentId contentId = getContentId();
			if (contentId!=null) {
				Content content = getContentDao().findContentById(contentId);
				if (content instanceof ResourceContainer && (ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(content)||getURI().getName().startsWith("shared/resource"))) {
					String resourcename = getResourceName();
					if (resourcename!=null && resourcename.startsWith("fs")) {
						int s = resourcename.indexOf("/");
						if (s>0)
						resource = getTreeNode(resourcename.substring(0,s));
					}
					else {
						resource = ((ResourceContainer)content).getResource(resourcename);
					}
				}
			}
			else {
				String resourceId = getResourceId();
				if (resourceId!=null) {
					if (resourceId.startsWith("fs")) {
						resource = getTreeNode(resourceId);
					}
					else {
						resource = getResource(resourceId);
					}
				}
			}
		}
		return resource;
	}


	public ContentId getContentId() {
		String name = getURI().getName();
		int s;
		if (name.startsWith("thumbnail-W980"))
			s = 15;
		else if (name.startsWith("thumbnail-large"))
			s = 16;
		else if (name.startsWith("thumbnail-small"))
			s = 16;
		else if (name.startsWith("thumbnail-mini"))
			s = 15;
		else if (name.startsWith("thumbnail-medium"))
			s = 17;
		else if (name.startsWith("thumbnail-avatar_status"))
			s = 24;
		else if (name.startsWith("thumbnail"))
			s = 10;
		else if (name.startsWith("shared/resource") && name.length()>20) {
			Json data = ServiceLocator.getService(TokenService.class).decode(name.substring(16));
			String id = (String)data.get("content");
			if (id==null) return null;
			return new ContentId(id);
		}
		else
			s = 0;	
		int i = name.indexOf("/", s);
		String id = name.substring(s, i);
		String arr[] = id.split("-");
		if (arr.length>2)
			return new ContentId(arr[1], arr[2]);
		else	
			return null;
	}
	
	public String getResourceName() {
		String name = getURI().getName();
		int s;
		if (name.startsWith("thumbnail-W980"))
			s = 15;
		else if (name.startsWith("thumbnail-large"))
			s = 16;
		else if (name.startsWith("thumbnail-small"))
			s = 16;
		else if (name.startsWith("thumbnail-avatar_status"))
			s = 24;
		else if (name.startsWith("thumbnail-mini"))
			s = 15;
		else if (name.startsWith("thumbnail-medium"))
			s = 17;
		else if (name.startsWith("thumbnail"))
			s = 10;
		else if (name.startsWith("shared/resource") && name.length()>20) {
			Json data = ServiceLocator.getService(TokenService.class).decode(name.substring(16));
			String resourcename = (String)data.get("name");
			return resourcename;
		}
		else
			s = 0;	
		int i = name.indexOf("/", s);
		String id = name.substring(i+1);
		return id;
	}
	
	public String getResourceId() {
		String name = getURI().getName();
		String id = null;
		
		if (name.startsWith("shared/resource") && name.length()>20) {
			Json data = ServiceLocator.getService(TokenService.class).decode(name.substring(16));
			id = (String)data.get("id");
		}
		else {
			String segments[] = name.split("/");
			if (segments.length>0 && segments[0]!=null && segments[0].toLowerCase().contains("thumbnail")) {
				id = segments[1];
			}
			else {
				if (segments.length>1 && segments[1]!=null && segments[1].startsWith("fs")) {
					id = segments[1];
				}
				else {
					id = segments.length > 0 ? segments[0] : null;
				}
			}
		}
		
		return id;
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
		return getAcceptRange().equals(ContentRangeType.BYTES);
	}
	
	public WebRange getRange() {
		String range = getContentRange();
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
		return getContentRange();
	}
	
	protected Resource getResource(String resourceId) {
		Resource resource = null;
		try {
			resource = getContentDao().findResourceById(KBFile.class, Long.valueOf(resourceId));
		}
		catch (NumberFormatException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return resource;
	}
	
	private Resource getTreeNode(String resourceId) {
		if (resourceId==null || !resourceId.startsWith("fs") || resourceId.length()<=2) return null;
		try {
			Resource resource = null;
			TreeFile tree = (TreeFile)getContentDao().findResourceById(KbeeTreeFile.class, Long.valueOf(resourceId.substring(2)));
			if (tree!=null) {
				String segments[] = getURI().getName().split("/");
				if (segments.length>1) {
					int s=0;
					for (;s<segments.length; s++) {
						if (resourceId.equals(segments[s])) {
							s++;
							break;
						}
					}
					for (;s<segments.length; s++) {
						String segment = segments[s];
						TreeFile treechild = null;
						for (TreeFile child : tree.getChildren()) {
							if (child.getName().equals(segment)) {
								treechild = child;
								break;
							}
						}
						if (treechild!=null) {
							if (treechild.isBinaryFile() && s+1==segments.length) {
								resource = ((KbeeTreeFileKBFile)treechild).getFile();
							}
							else {
								tree = treechild;
							}
						}
						else {
							break;
						}
					}
				}
			}
			return resource;
		}
		catch (Exception e) {
			logger.error(e);
		}
		return null;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}	
	
	private File getThumbnailYoutubeFile(KbeeExternalResource resource, ThumbnailSize large) throws IOException {
		ThumbnailService ths = ServiceLocator.getService(ThumbnailService.class);
		String domain_name=resource.getDomain().getName();
		File file = ths.getThumbnailFile(resource.getId().toString(), domain_name, resource.getUrl(), large);
		return file;
	}
	
	private String getMimeType(Resource resource) {
		
		if (resource!=null) {
			
			if (resource instanceof KBFile) {

				KBFile file = (KBFile) resource;
				
				String src = file.getFileName();
				
				if (src==null)
					src=file.getName();
				
				if (src==null)
					return null;
				
				if (FSUtils.isPdf(src))
					return "application/pdf";
				
				if (FSUtils.isImage(src))  {
					String str = FilenameUtils.getExtension(src);
					if (str!=null && (str.toLowerCase().equals("jpg") ||  str.toLowerCase().equals("jpeg")))
						return "image/jpeg"; 
					return "image/"+str;
				}
				if (FSUtils.isVideo(src)) {
					return "video/"+FilenameUtils.getExtension(src);
				}
				
				if (FSUtils.isAudio(src))
					return "audio/"+FilenameUtils.getExtension(src);
				
				if (FSUtils.isJScript(src))
					return "application/javascript";
				
				return  file.getContentType();
				
			}
			else {

				if (FSUtils.isPdf(resource.getName()))
					return "application/pdf";
				
				if (FSUtils.isImage(resource.getName()))  {
					String str = FilenameUtils.getExtension(resource.getName());
					if (str!=null && (str.toLowerCase().equals("jpg") ||  str.toLowerCase().equals("jpeg")))
						return "image/jpeg"; 
					return "image/"+str;
				}
				if (FSUtils.isVideo(resource.getName())) {
					return "video/"+FilenameUtils.getExtension(resource.getName());
				}
				
				if (FSUtils.isAudio(resource.getName()))
					return "audio/"+FilenameUtils.getExtension(resource.getName());
				
				return null;
				
			}
		}	
		else
			return null;
	}
}
