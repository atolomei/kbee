package kbee.web.resource;

import java.io.IOException;
import java.io.InputStream;

import org.apache.wicket.request.resource.AbstractResource.ResourceResponse;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Resource;
import com.novamens.content.dao.ContentDao;
import com.novamens.service.ServiceLocator;

import kbee.util.logging.Logger;

public abstract class AbstractResourceResponse extends ResourceResponse {
	
	protected static Logger logger = Logger.getLogger(AbstractResourceResponse.class.getName());
	
	private URI uri;
	
	private transient InputStream inputStream;
	private transient Resource resource;
	
	public AbstractResourceResponse(URI uri) {
		this.uri = uri;
	}

	public URI getURI() {
		return uri;
	}
	
	public InputStream getInputStream() throws ResourceStreamNotFoundException {
		if (inputStream == null) {
			try {
				inputStream = openStream();
			}
			catch (Exception e) {
				logger.error(e.getClass().getName() + " | " + ("Resource " + uri.getName()	+ " could not be opened") + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
				throw new ResourceStreamNotFoundException("Resource " + uri	+ " could not be opened", e);
			}
		}
		return inputStream;		
	}
	
	public void close() throws IOException {
	}
	
	public String getRangeString() {
		return null;
	}
	
	public Resource getResource() throws IOException {
		if (resource==null) {
			resource = loadResource();
		}
		return resource;
	}	

	protected abstract InputStream openStream() throws IOException;
	
	protected abstract Resource loadResource() throws IOException;
			
	protected Resource getResource(String resourceId) {
		Resource resource = null;
		try {
			resource = getContentDao().findResourceById(Resource.class, Long.valueOf(resourceId));
		}
		catch (NumberFormatException e) {
			logger.error(e);
		}
		return resource;
	}	
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}	
}