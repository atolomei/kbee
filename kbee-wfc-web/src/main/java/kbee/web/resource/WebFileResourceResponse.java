package kbee.web.resource;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.http.WebResponse.CacheScope;
import org.apache.wicket.request.resource.AbstractResource.ResourceResponse;
import org.apache.wicket.request.resource.IResource.Attributes;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;

import com.novamens.util.KbeeRuntimeException;

import kbee.util.FSUtils;

public class WebFileResourceResponse extends ResourceResponse {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(WebFileResourceResponse.class.getName());

	
	private transient InputStream inputStream;
	private transient File file;

	private final URI uri;

	public WebFileResourceResponse(URI uri) {
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

	public InputStream getInputStream() throws ResourceStreamNotFoundException {
		if (inputStream == null) {
			try {
				//Assert.isTrue(isRootUser(), "no root user");
				File file = getFile();
				return new FileInputStream(file);
			}
			catch (IOException e) {
				throw new ResourceStreamNotFoundException("File " + uri
						+ " could not be opened", e);
			}
			catch (Exception e) {
				throw new ResourceStreamNotFoundException("File " + uri
						+ " could not be opened", e);
			}
		}
		
		return inputStream;
	}

	@Override
	public String getContentType() {
		try {
			return getMimeType(getFile());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Instant getLastModified() {
		try {
			File file = getFile();
			if (file!=null)
				return Instant.ofEpochMilli(file.lastModified());
			else
				return null;
		}
		catch (IOException e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return uri.toString();
	}
	
	@Override
	public long getContentLength() {
		try {
			File file = getFile();
			if (file!=null) {
				return file.length();
			}	
			else
				return 0;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public File getFile() throws IOException {
		if (this.file==null) {
			this.file = new File(getFilePath());
		}
		return this.file;
	}
	
	public String getFilePath() {
		File root = new File(".");
		String rootpath = root.getAbsolutePath();
		String name = getURI().getName();
		name = name.substring(5);
		name = name.replace("/", File.separator);
		String path = rootpath + File.separator + name;
		return path;
	}
	
	public boolean dataNeedsToBeWritten(Attributes attributes) {
		WebRequest request = (WebRequest)attributes.getRequest();
		Instant ifModifiedSince = request.getIfModifiedSinceHeader();

		if (getCacheDuration()!= Duration.ZERO && ifModifiedSince != null && getLastModified() != null)	{
			Instant roundedLastModified = Instant.ofEpochMilli(getLastModified().toEpochMilli() / 1000 * 1000);
			return ifModifiedSince.isBefore(roundedLastModified);
		}
		else {
			return true;
		}
	}
	
	public Duration getCacheDuration()	{
		return Duration.ZERO;
	}

	public WebResponse.CacheScope getCacheScope() {
		return CacheScope.PUBLIC;
	}
	
//	private boolean isRootUser() {
//		return ServiceLocator.getService(SecurityService.class).isRoot();
//	}
	
	private String getMimeType(File file) { 
		if (file!=null) {
			if (FSUtils.isPdf(file.getName()))
				return "application/pdf";
			if (FSUtils.isImage(file.getName()))  {
				String str = FilenameUtils.getExtension(file.getName());
				if (str.equals("jpg"))
					return "image/jpeg"; 
				return "image/"+str;
			}
			if (FSUtils.isVideo(file.getName())) {
				return "video/"+FilenameUtils.getExtension(file.getName());
			}
			if (FSUtils.isAudio(file.getName())) {
				return "audio/"+FilenameUtils.getExtension(file.getName());
			}	
			if (FSUtils.isHTML(file.getName())) {
				return "text/html";
			}	
			if (file.getName().endsWith(".css")) {
				return "text/css";
			}
			if (file.getName().endsWith(".svg")) {
				return "image/svg+xml";
			}
			MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
			return 	mimeTypesMap.getContentType(file.getName());
		}	
		else
			return null;
	}
}
