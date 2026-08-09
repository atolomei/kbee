package kbee.web.form;

import kbee.web.resource.ResourcesPanel;
import kbee.web.util.MultipartUtil;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.wicket.Application;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.component.IRequestablePage;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.SimpleImageInfo;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FormUploadHandler extends WebPage {
				
	private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(FormUploadHandler.class.getName());
	
	private static final long serialVersionUID = 1L;
	private ContentDao contentDao;

	static final private int THRESHOLD = 1024*1024*10; //10MB.
	final boolean is_root			= ServiceLocator.getService(SecurityService.class).isRoot(); 
	final boolean is_domain_admin	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	public FormUploadHandler() {
		
		HttpServletRequest request = getHttpRequest();
		HttpServletResponse response = getHttpResponse();
		
		if (!MultipartUtil.isMultipartFormData(request)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		try {
			
			String path	= request.getParameter("path");
			
			DiskFileItemFactory factory = new DiskFileItemFactory();
			factory.setSizeThreshold(THRESHOLD); 
			
			ServletFileUpload servletFileUpload = new ServletFileUpload(factory);
			KBFileImpl file = servletFileUpload.parseMultipart(request);
			
			if (file!=null) {
				int i = path.indexOf(":");
				String pageId = path.substring(0, i);
				IRequestablePage page = Application.get().getMapperContext().getPageInstance(Integer.valueOf(pageId));
				IRequestableComponent component = page.get(path.substring(i+1));
				if (component instanceof FileUploadField) {
					onUpload(file);
					((FileUploadField)component).setValue(file);
					(getContentDao()).saveTX(file);
					((FileUploadField)component).detach();
				}
				if (component instanceof ResourcesPanel) {
					(getContentDao()).saveTX(file);
					((ResourcesPanel)component).add(file);
					((MarkupContainer)component).detach();
					MarkupContainer parent = ((MarkupContainer)component).getParent();
					while (parent!=null) {
						parent.detach();
						parent = parent.getParent();
					}
				}
			}
		}
		catch (FileUploadException e) {
			logger.error(e);
			try {
				response.getWriter().write(getMessage(e));
				//response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
			catch (Exception e1) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
		catch (IOException e) {
			logger.error(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch (ContentMgmtException e) {
			logger.error(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			
		}
		catch (Exception e) {
			logger.error(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
	
	protected String getMessage(Exception e) {
		 String message = e.getMessage();
		 if (is_root || is_domain_admin) {
			 Throwable t = e;
			 while (t.getCause()!=null) {
				 t = t.getCause();
				 message += "\r\n" + t.getMessage(); 
			 }
		 }
		 return message;
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected void onUpload(KBFileImpl file) throws IOException {
		if (kbee.util.FSUtils.isImage(file.getFile())) {
			SimpleImageInfo imageInfo;
			int nw, nh;
			try {
				imageInfo = new SimpleImageInfo(file.getFile());
				nw  = imageInfo.getWidth();
				nh = imageInfo.getHeight();
			}
			catch (IOException e) {
				nw = 0;
				nh = 0;
			}
			file.setWidth(nw);
			file.setHeight(nh);
		}
	}
}
