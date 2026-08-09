package kbee.web.resource;

import java.io.File;
import java.io.IOException;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.security.User;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.util.logging.Logger;

public class WebPdfFileResourceResponse extends WebFileResourceResponse {

	private static Logger logger = Logger.getLogger(WebPdfFileResourceResponse.class.getName());
	
	static boolean isWindows = false;
	static {
		String strOSName = System.getProperty("os.name");
		isWindows = (strOSName.toLowerCase().contains("windows"));
	}
	public WebPdfFileResourceResponse(URI uri) {
		super(uri);
	}	
	
	public File getFile() throws IOException {
		
		if (isReadable()) {
			return super.getFile();
		}
		else {
			logger.error("File is not readable");
			throw new IOException("File is not readable");
		}
	}
	
	public boolean isReadable() {
		Long resourceid = getResourceId();
		
		if (resourceid==null) 
			return false;
		
		Resource resource = getContentDao().findResourceById(KBFileImpl.class, resourceid);

		if (resource==null) 
			return false;
		
		Content content = getContentDao().findContentByResource(resource);
		
		User user = getReader();
		
		if(user==null) 
			return false;
		
		if (content.getWorkspace()!=null && content.getWorkspace().equals(user.getId()))
			return true;
		
		boolean readable = ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(content, user);
		return readable;
		
	}
	
	public User getReader() {
		return getSessionUser();
	}
	
	public Long getResourceId() {
		String path = getURI().getEscapedPath();
		String tokens[] = path.split("/");
		String id = tokens[0];
		try {
			return Long.valueOf(id);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}
	
	@Override
	public String getFilePath() {
		String rootDir = ServiceLocator.getService(ApplicationServerService.class).getWorkDirAbsolutePath() + File.separator + "pdfserver" + File.separator + "db";
		String path =  rootDir + File.separator + getURI().getName().replace("/", File.separator);
		logger.debug("\n-----------------\nPATH 1. -> " +path+"\n-----------------");
		return path;
	}
	
	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}