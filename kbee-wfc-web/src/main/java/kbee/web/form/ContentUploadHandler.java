package kbee.web.form;

import kbee.web.util.MultipartUtil;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.wicket.markup.html.WebPage;
import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.content.user.UserService;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.service.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Deprecated
public class ContentUploadHandler extends WebPage {
			
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ContentUploadHandler.class.getName());

	
	private static final long serialVersionUID = 1L;
	private ContentDao contentDao;

	static final private int THRESHOLD = 1024*1024*10; //10MB.
	
	public ContentUploadHandler() {
		
		HttpServletRequest request = getHttpRequest();
		HttpServletResponse response = getHttpResponse();
		
		if (!MultipartUtil.isMultipartFormData(request)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		try {
			DiskFileItemFactory factory = new DiskFileItemFactory();
			factory.setSizeThreshold(THRESHOLD); 
			
			kbee.web.form.ServletFileUpload servletFileUpload = new kbee.web.form.ServletFileUpload(factory);
			KBFileImpl file = servletFileUpload.parseMultipart(request);
			
			if (file!=null) {
				ContentTemplate defaultTemplate = getDefaultTemplate();
				
				Assert.isTrue(defaultTemplate!=null);
				
				if (!inWorkflow()) {
					ServiceLocator.getService(ContentFactoryService.class).create(defaultTemplate.getName(), file);
				} 
				else {
					List<ProcessLauncher> launchers = getLaunchers(defaultTemplate);
				
					Assert.isTrue(!launchers.isEmpty());
					
					ProcessLauncher launcher = launchers.get(0);
 					Content content = ServiceLocator.getService(ContentFactoryService.class).create(defaultTemplate.getName(), file);
					content.getService(WorkflowService.class).startProcess(launcher.getProcedure());
				}
			}
		}
		catch (ContentCreationException e) { 
			logger.error(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch (FileUploadException e) {
			logger.error(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch (RuntimeException e) {
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
	
	protected List<ProcessLauncher> getLaunchers(ContentTemplate template) {
		return getDomain().getService(WorkflowDomainService.class)==null ? new ArrayList<ProcessLauncher>() :
			getDomain().getService(WorkflowDomainService.class).getContextLaunchers(template);
	}
	
	protected ContentTemplate getDefaultTemplate() {
		String defaultTemplate = getDomain().getService(DomainSettingsService.class).get("defaultTemplate");
		for (ContentTemplate template : getContentDao().getTemplates()) {
			if (template.getName().equals(defaultTemplate))
				return template;
		}
		return null;
	}
	
	protected boolean inWorkflow() {
		return getDomain().getService(WorkflowDomainService.class)!=null;
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
