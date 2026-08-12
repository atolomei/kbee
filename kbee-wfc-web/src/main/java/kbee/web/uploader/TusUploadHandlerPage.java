package kbee.web.uploader;

import kbee.util.logging.Logger;
import kbee.web.resource.ResourcesPanel;
import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.exception.TusException;

import org.apache.wicket.Application;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.component.IRequestablePage;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.resource.KBFile;
import com.novamens.service.ServiceLocator;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TusUploadHandlerPage extends WebPage {
	private static final long serialVersionUID = 1L;
	
	private Logger logger = Logger.getLogger(TusUploadHandlerPage.class.getName());
	
	public TusUploadHandlerPage() {
		
		HttpServletRequest request = 
				(HttpServletRequest)getRequest().getContainerRequest();
		HttpServletResponse response = 
			(HttpServletResponse)getResponse().getContainerResponse();
		ServletContext servletContext = request.getServletContext();
		
		try {

	        String body = request.getReader()
                .lines()
                .collect(Collectors.joining("\n"));

	        String uploadId = extractValue(body, "uploadId");
	        String path = extractFolder(extractValue(body, "relativePath"));
	        String name = extractValue(body, "originalFileName");
	        String destination = extractValue(body, "destinationId");

	        if (uploadId == null || uploadId.trim().isEmpty()) {
	            response.sendError
	            	(HttpServletResponse.SC_BAD_REQUEST, 
	            	"Missing uploadId");
	            return;
	        }

	        String uploadUrl = "/api/upload/" + uploadId;
	   
	        TusFileUploadService tusService = (TusFileUploadService)
	        	servletContext.getAttribute(TusServlet.TUS_SERVICE_ATTRIBUTE);

	    	try (InputStream uploadedStream =
	    		tusService.getUploadedBytes(uploadUrl)) {

	    		KBFile file = ServiceLocator
	         		.getService(UploaderService.class)
	         		.upload(
	         			name, 
	         			path, 
	         			uploadedStream, 
	         			0);
	    		
	    		ResourcesPanel panel = getDestinationPanel(destination);
	    		
	    		if (panel==null) {
		            response.sendError
	            	(HttpServletResponse.SC_BAD_REQUEST, 
	            	"Missing Destination");
		            return;
	    		}
	    		
	    		if (isVersion(destination)) {
		    		panel.addVersion(getResource(destination), file);
	    		}
	    		else {
		    		panel.add(file);
	    		}
	    		
	    		detach(panel);
	        		
	    		tusService.deleteUpload(uploadUrl);
	      
	    		response.setStatus(HttpServletResponse.SC_OK);
	    		response.setContentType("application/json");
	    		response.getWriter().write("{\"status\":\"ok\"}");
	    	}
	    	catch (TusException e) {
	    		response.setStatus(e.getStatus());
	    		response.setContentType("application/json");
	    		response.getWriter().write(	"{\"status\":\"error\",\"message\":\"" +
	    			escapeJson(e.getMessage()) + "\"}");
	        }
	    	catch (IOException e) {
	    		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	    		response.setContentType("application/json");
	    		response.getWriter().write(	"{\"status\":\"error\",\"message\":\"" +
	    			escapeJson(e.getMessage()) + "\"}");
	        }
		}
        catch (Exception e) {
        	logger.error(e);
        	try {
        	response.sendError(
        		HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        		"Error processing uploaded file");
        	}
        	catch (IOException ioe) {
            	logger.error(ioe);
        	}
        }
	}
		
	private String extractValue(String json, String field) {
		String key = "\"" + field + "\"";
		int idx = json.indexOf(key);
		if (idx < 0) return null;

		int colon = json.indexOf(":", idx);
		int firstQuote = json.indexOf("\"", colon + 1);
		int secondQuote = json.indexOf("\"", firstQuote + 1);

		return json.substring(firstQuote + 1, secondQuote);
	}
		
	private String extractFolder(String pathvalue) {
	    String folder = "";
	    if (pathvalue==null) return null;
	    String path[] = pathvalue.split("/");
	    for (int f=0; f<=path.length-2; f++) {
	    	if (!"".equals(folder)) {
	    		folder = folder + "/";
	    	}
	    	folder += normalize(path[f]); 
	    }
	    return folder;
	}
	
	private String normalize(String str) {
		 
		String p=str.replaceAll("[\\t|\\s|(|)]", "")
				 .replace("'", "-")
				 .replace("á", "a")
				 .replace("é", "e")
				 .replace("í", "i")
				 .replace("ó", "o")
				 .replace("ú", "u")
				 .replace("ñ", "n")
				 .replace(";", "")
				 .replace(":", "")
		 		 .replace("°", "")
				 .replace("|", "")
				 .replace("#", "")
				 .replace("$", "")
				 .replace("%", "")
				 .replace("&", "")
				 .replace("/", "")
				 .replace("¡", "")
				 .replace("?", "")
				 .replace("=", "")
				 .replace("}", "")
				 .replace("{", "")
				 .replace(":", "")
				 .replace("Á,", "A")
				 .replace("É,", "E")
				 .replace("Í,", "I")
				 .replace("Ó,", "O")
				 .replace("Ú,", "U")
				 .replace("Ñ",  "N");
	
		 return p;
	}
	
    private String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
	
	private ResourcesPanel getDestinationPanel(String destination) {
		
		int i = destination.indexOf(":");
		String pageId = destination.substring(0, i);
		String path = destination.substring(i+1);
		int s = path.indexOf("/");
		if (s>0) path = path.substring(0,s);
		
		IRequestablePage page = Application.get().getMapperContext().getPageInstance(Integer.valueOf(pageId));
		IRequestableComponent component = page.get(path);
		if (component instanceof ResourcesPanel) {
			return ((ResourcesPanel)component);
		}
		return null;	
	}
	
	private KBFile getResource(String destination) throws TusException {
		
		String resourceId = destination.substring(destination.indexOf("/")+1);
		KBFile resource = (KBFile)getContentDao().findResourceById(KBFile.class, Long.valueOf(resourceId));
		
		if (resource==null) {
			throw new TusException(HttpServletResponse.SC_BAD_REQUEST, "missing resource");
		}
		
		return resource;
	}
	
	private boolean isVersion(String destination) {
		return destination.contains("/");
	}
	
	private void detach(ResourcesPanel panel) {
		((MarkupContainer)panel).detach();
		MarkupContainer parent = ((MarkupContainer)panel).getParent();
		while (parent!=null) {
			parent.detach();
			parent = parent.getParent();
		}
		((MarkupContainer)panel).getPage().detach();
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
