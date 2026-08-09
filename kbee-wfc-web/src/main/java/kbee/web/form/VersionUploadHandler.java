package kbee.web.form;

import kbee.web.resource.ResourcesPanel;
import kbee.web.util.MultipartUtil;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.wicket.Application;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.component.IRequestablePage;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ContentId;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentService;

import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.lock.ValueLockerService;
import com.novamens.service.ServiceLocator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class VersionUploadHandler extends WebPage {
			
	private static final long serialVersionUID = 1L;
	private ContentDao contentDao;

	static private kbee.util.logging.Logger logger = new kbee.util.logging.Logger(LogManager.getLogger(UploadHandler.class.getName()));
	
	static final String MULTIPART_ERROR = "Not found or Not Multipart Request";
	static final String VERSION_ERROR = "Version not Found";
	
	static final private int THRESHOLD = 1024*1024*20; //20MB.
	
	public VersionUploadHandler() {
		
		HttpServletRequest request = getHttpRequest();
		HttpServletResponse response = getHttpResponse();
		
		try {
			
			if (!MultipartUtil.isMultipartFormData(request)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
	
			String contentId = request.getParameter("id");
			String contentClass = request.getParameter("class");
			String resourceId = request.getParameter("resource");
			String path = request.getParameter("path");
				
			if (contentId !=null && contentClass!=null) {
				upload(contentId, contentClass, resourceId, path, request,  response);
			}
			else {
				if (resourceId!=null) 
					upload(resourceId, path, request,  response);
			}
		}
		catch (Exception e) {
			logger.error(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	public HttpServletRequest getHttpRequest() {
		return(HttpServletRequest)getRequest().getContainerRequest();
	}
	
	public HttpServletResponse getHttpResponse() {
		return (HttpServletResponse)getResponse().getContainerResponse();
	}
	
	private void upload(String contentId, 
		String contentClass, 
		String resourceId, 
		String path, 
		HttpServletRequest request, 
		HttpServletResponse response) throws FileUploadException {
		
		Content content = getContentDao().findContentById(new ContentId(contentClass, contentId));
		long ws = content.getWorkspace();
			
		KBFile previousversion = (KBFile)getContentDao().findResourceById(KBFile.class, Long.valueOf(resourceId));
			
		if (previousversion==null) { 
			logger.error(VERSION_ERROR);
			return;
		}
			
		KBFileImpl version = getVersion(request);
		
		if (version==null) {
			logger.error(MULTIPART_ERROR);
			return;
		}
		
		// la logica va al servicio:
//		int fileversion = previousversion.getVersion();
//		
//		if (fileversion==0) {
//			((KBFileImpl)previousversion).setVersion(1);
//			fileversion = 1;
//		}
//			
//		version.setPreviousVersion(previousversion);
//		version.setVersion(fileversion+1);
//		version.setOId(previousversion.getOId());
			
		try {
			ServiceLocator.getService(ValueLockerService.class).lock(Long.valueOf(contentId));
			getContentDao().refresh(content);
			if (content.getWorkspace()!=null && content.getWorkspace()==ws) {
				content.getService(ContentService.class).replaceFile(previousversion, version);
			}
			notifyPanel(previousversion, version, path);
			response.setStatus(HttpServletResponse.SC_OK);
		} 
		finally {
			ServiceLocator.getService(ValueLockerService.class).unlock(Long.valueOf(contentId));
		}
	}

	/** 
	 * Add Resource to the File Server
	 */
	private void upload(String resourceId, 
		String path, 
		HttpServletRequest request, 
		HttpServletResponse response) throws FileUploadException {
		
		KBFile previousversion = (KBFile)getContentDao().findResourceById(KBFile.class, Long.valueOf(resourceId));
			
		if (previousversion==null) {
			logger.error(VERSION_ERROR);
			return;
		}
			
		KBFileImpl version = getVersion(request);
			
		if (version==null) {
			logger.error(MULTIPART_ERROR);
			return;
		}
			
		int fileversion = previousversion.getVersion();
			
		if (fileversion==0) {
			((KBFileImpl)previousversion).setVersion(1);
			fileversion = 1;
		}
			
		version.setPreviousVersion(previousversion);
		version.setVersion(fileversion+1);
		version.setOId(previousversion.getOId());
			
		getContentDao().saveTX(version);

		notifyPanel(previousversion, version, path);
			
		response.setStatus(HttpServletResponse.SC_OK);
	}
	
	private void notifyPanel(KBFile file, KBFile version, String path) {
		if (path!=null) {
			IRequestableComponent component = getComponent(path);
			if (component instanceof ResourcesPanel) {
				((ResourcesPanel)component).addVersion(file, version);
				((Panel)component).detach();
				MarkupContainer parent = ((Panel)component).getParent();
				while (parent!=null) {
					parent.detach();
					parent = parent.getParent();
				}
			}
		}
	}
	
	private KBFileImpl getVersion(HttpServletRequest request) throws FileUploadException {
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(THRESHOLD); 
		kbee.web.form.ServletFileUpload servletFileUpload = new kbee.web.form.ServletFileUpload(factory);
		KBFileImpl version = servletFileUpload.parseMultipart(request);
		return version;
	}
	
	private IRequestableComponent getComponent(String path) {
		int i = path.indexOf(":");
		String pageId = path.substring(0, i);
		IRequestablePage page = Application.get().getMapperContext().getPageInstance(Integer.valueOf(pageId));
		IRequestableComponent component = page.get(path.substring(i+1));
		return component;
	}
	
	private ContentDao getContentDao() {
		if (contentDao==null) {
			BeansService beans = ServiceLocator.getService(BeansService.class);
			contentDao = (ContentDao) beans.getBean("contentDao");
		}
		return contentDao;
	}
}
