package kbee.web.uploader;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.novamens.content.resource.KBFile;
import com.novamens.event.EventService;
import com.novamens.service.ServiceLocator;

import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.exception.TusException;

public class TusUploadCompleteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

	private TusFileUploadService tusService;
    public static final String TUS_SERVICE_ATTRIBUTE = "tusService";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String body = req.getReader()
                .lines()
                .collect(Collectors.joining("\n"));

        String uploadId = extractValue(body, "uploadId");
        String path = extractFolder(extractValue(body, "relativePath"));
        String name = extractValue(body, "originalFileName");
        String destination = extractValue(body, "destinationId");

        if (uploadId == null || uploadId.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing uploadId");
            return;
        }

        String uploadUrl = "/api/upload/" + uploadId;
   
        tusService = (TusFileUploadService)
                getServletContext().getAttribute(TusServlet.TUS_SERVICE_ATTRIBUTE);

    	try (InputStream uploadedStream =
    		tusService.getUploadedBytes(uploadUrl)) {

     		KBFile file = ServiceLocator
     			.getService(UploaderService.class)
     			.upload(
     				name, 
     				path, 
     				uploadedStream, 
     				0);
     		
     		FileUploadedEvent event = FileUploadedEvent.builder()
     			.time(Instant.now())
     			.destination(destination)
     			.name(name)
     			.file(file)
     			.build();
     		
     		ServiceLocator
     			.getService(EventService.class)
     			.fire(event);
    		
    		tusService.deleteUpload(uploadUrl);
  
    	    resp.setStatus(HttpServletResponse.SC_OK);
    	    resp.setContentType("application/json");
    	    resp.getWriter().write("{\"status\":\"ok\"}");
    	}
    	catch (TusException|IOException e) {
    	    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    	    resp.setContentType("application/json");
    	    resp.getWriter().write(
    	        "{\"status\":\"error\",\"message\":\"" +
    	        escapeJson(e.getMessage()) +
    	        "\"}"
    	    );
    	}
    	catch (Exception e) {
    		resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
    			"Error processing uploaded file");
    	}
    }
    
    private String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"");
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

    private String extractValue(String json, String field) {
        String key = "\"" + field + "\"";
        int idx = json.indexOf(key);
        if (idx < 0) return null;

        int colon = json.indexOf(":", idx);
        int firstQuote = json.indexOf("\"", colon + 1);
        int secondQuote = json.indexOf("\"", firstQuote + 1);

        return json.substring(firstQuote + 1, secondQuote);
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
}