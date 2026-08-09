package kbee.web.form;


import kbee.web.util.MultipartUtil;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.wicket.markup.html.WebPage;
import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ContentId;
import com.novamens.content.service.ContentService;

import com.novamens.dom.ObjectState;

import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.lock.ValueLockerService;
import com.novamens.service.ServiceLocator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 *  id=id del Contenido
 *  class=Clase de Contenido
 *   
 *   id = id del contenido / class = clase de contenido
 *   resoid = oid del resource (sin contenido) 
 *  
 */
public class UploadHandler extends WebPage {
			
	private static final long serialVersionUID = 1L;
	private ContentDao contentDao;

	static private kbee.util.logging.Logger logger = new kbee.util.logging.Logger(LogManager.getLogger(UploadHandler.class.getName()));
	
	static final private int THRESHOLD = 1024*1024*20; //20MB.
	
	public UploadHandler() {
		
		HttpServletRequest request = getHttpRequest();
		HttpServletResponse response = getHttpResponse();
		
		if (!MultipartUtil.isMultipartFormData(request)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		String contentId = request.getParameter("id");
		String contentClass = request.getParameter("class");
		String ispublic = request.getParameter("public");
		String groupId = request.getParameter("group");
			
		if (contentId !=null && contentClass!=null) {
			upload(contentId, contentClass, groupId, ispublic, request,  response);
			return;
		}
		else {
			String resoid = request.getParameter("resoid");
			if (resoid!=null) 
				upload(resoid, request,  response);
		}
	}


	public ContentDao getContentDao() {
		if (contentDao==null) {
			BeansService beans = ServiceLocator.getService(BeansService.class);
			contentDao = (ContentDao) beans.getBean("contentDao");
		}
		return contentDao;
	}

	public HttpServletRequest getHttpRequest() {
		return(HttpServletRequest)getRequest().getContainerRequest();
	}
	
	
	public HttpServletResponse getHttpResponse() {
		return (HttpServletResponse)getResponse().getContainerResponse();
	}
	
	/** 
	 * 
	 * Add Resource to an existing Content
	 * 
	 */
	private void upload(String contentId, String contentClass, String groupId, String ispublic, HttpServletRequest request, HttpServletResponse response) {
		
		try {
			
			
			// if content is null ?
			
			Content content = getContentDao().findContentById(new ContentId(contentClass, contentId));
			long ws = content.getWorkspace();
			
			DiskFileItemFactory factory = new DiskFileItemFactory();
			factory.setSizeThreshold(THRESHOLD); 
			
			kbee.web.form.ServletFileUpload servletFileUpload = new kbee.web.form.ServletFileUpload(factory);
			
			KBFileImpl file = servletFileUpload.parseMultipart(request);
	
					
			file.setState(ObjectState.ENABLED);
			
			ResourceTag group = groupId!=null && !"".equals(groupId) ? getContentDao().findResourceGroupById(Long.valueOf(groupId)) : null;
			
			if (file!=null) {
				try {
					ServiceLocator.getService(ValueLockerService.class).lock(Long.valueOf(contentId));
					getContentDao().refresh(content);
					if (content.getWorkspace()!=null && content.getWorkspace()==ws) {
						content.getService(ContentService.class).addFile(file, group, "true".equals(ispublic));
					}
				} 
				catch (RuntimeException e) {
					logger.error(e);
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
				finally {
					ServiceLocator.getService(ValueLockerService.class).unlock(Long.valueOf(contentId));
				}
			}
		}
		catch (FileUploadException e) {
			logger.error(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		finally {
			
			
		}
	}

	/** 
	 * Add Resource to the File Server
	 */
	private void upload(String esource_id, HttpServletRequest request, HttpServletResponse response) {
		try {
			
			DiskFileItemFactory factory = new DiskFileItemFactory();
			factory.setSizeThreshold(THRESHOLD); 
			kbee.web.form.ServletFileUpload servletFileUpload = new kbee.web.form.ServletFileUpload(factory);
			KBFileImpl file = servletFileUpload.parseMultipart(request);
			file.setState(ObjectState.ENABLED);
			getContentDao().saveTX(file);
		}
		catch (FileUploadException |  RuntimeException e) {
			logger.error(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
