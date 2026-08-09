package kbee.wicket.froala;

import java.util.List;

import org.apache.wicket.markup.MarkupType;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.handler.TextRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.UrlService;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailSize;

import kbee.util.FSUtils;

public class FroalaImagesPage extends WebPage {
	private static final long serialVersionUID = 1L;

	static private ObjectMapper mapper = new ObjectMapper();
	
	
	public FroalaImagesPage(final PageParameters parameters) {
	        super(parameters);
	        getRequestCycle().scheduleRequestHandlerAfterCurrent(new TextRequestHandler("application/json", "UTF-8", sendResponse(parameters)));                
	    }
	     
	    @Override
	    public MarkupType getMarkupType() {
	        return new MarkupType("html","application/json");
	    }
	 
	    protected String sendResponse(final PageParameters parameters) {
	    	ArrayNode images = mapper.createArrayNode();
	    	for (Resource resource : getResources( parameters)) {
	    		if (isImage(resource)) {
	    			images.add(getJsonImage(resource));
	    		}
	    	}
	    	String response = images.toString();
	        return response;   
	    }
	    
	    protected List<Resource> getResources(PageParameters parameters) {
			StringValue tag = parameters.get("tag");
			if (tag.isNull()) return null; 
			ResourceContainer content = (ResourceContainer)getContent(parameters);
			if (content==null) return null;
	    	List<Resource> resources = content.getResources(tag.toString());
	    	return resources;
	    }
	    
		protected Content getContent(PageParameters parameters) {
			Content content = null;		
			StringValue id = parameters.get("content");
			if (!id.isNull()) { 
				content = getContentDao().findContentById(Long.valueOf(id.toString()));
			}	
			return content;
		}
		
		protected boolean isImage(Resource resource) {
			return resource instanceof KBFile && FSUtils.isImage(resource.getName());
		}
		
		protected JsonNode getJsonImage(Resource resource) {
			ObjectNode image = mapper.createObjectNode();
			String url = resource.getService(UrlService.class).getUrl();
			String thumb = resource.getService(UrlService.class).getThumbnailUrl(ThumbnailSize.MEDIUM);
			image.put("url", url);
			image.put("thumb", thumb);
			return image;
		}
		
		protected ContentDao getContentDao() {
			return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
		}
}
