package kbee.web.uploader;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.novamens.beans.BeansService;
import com.novamens.service.ServiceLocator;

import me.desair.tus.server.TusFileUploadService;

public class TusServlet extends javax.servlet.http.HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TusFileUploadService tusService;
    public static final String TUS_SERVICE_ATTRIBUTE = "tusService";

    @Override
    public void init() throws ServletException {


        File storage = new File(System.getProperty("java.io.tmpdir"), "tus-uploads");
        storage.mkdirs();

        tusService = new TusFileUploadService()
                .withUploadURI("/api/upload")
                .withStoragePath(storage.getAbsolutePath())
                .withDownloadFeature();
        
        getServletContext().setAttribute(TUS_SERVICE_ATTRIBUTE, tusService);
        
        ServiceLocator.getService(BeansService.class);
        
    }

    @Override
    protected void service(HttpServletRequest req,
                           HttpServletResponse resp)
            throws ServletException, IOException {

        tusService.process(req, resp);
    }
}