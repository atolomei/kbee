package kbee.web.util;

import org.apache.logging.log4j.LogManager;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.ResourceReference;


import com.novamens.content.base.Resource;
import com.novamens.content.resource.ExternalResource;

import kbee.web.resource.WebResourceReference;


public class ResourceUriHelper {
	
	private static ResourceUriHelper Instance;

	static final private org.apache.logging.log4j.Logger logger = LogManager.getLogger(ResourceUriHelper.class.getName());
	
	public static ResourceUriHelper getInstance() {
		if (Instance==null) 
			Instance = new ResourceUriHelper();
		return Instance;
	}
	
	/** ----------------------------------------------------------------------------------
	 * @param resource
	 * @return
	 */
	public String getHref(Resource resource) {

		if (resource instanceof ExternalResource) { 
			logger.debug("uri " + ((ExternalResource) resource).getUrl());
			return ((ExternalResource) resource).getUrl();
		}
		
		ResourceReference resourcereference;
		resourcereference = new WebResourceReference(resource);
		String filehref = RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(RequestCycle.get().urlFor(resourcereference, null)));
		
		logger.debug("uri " + filehref);
		return filehref;
	}
	

}
