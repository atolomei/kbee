package com.novamens.kbee.content.command;


import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.ObjectID;
import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.service.ServiceLocator;

public class PdfConverterServiceRequest extends AbstractServiceRequest {
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PdfConverterServiceRequest.class.getName());
	
	private static final long serialVersionUID = -1L;

	private Long contentId;
	private transient Content content = null;
	
	public PdfConverterServiceRequest(Content content) {
		contentId = (Long)content.getId();
		
		try {
			setObjectID(new ObjectID(content).toString());
		} catch (Exception e) {
			logger.error(e);
		}

		
		super.setDescription(PdfConverterServiceRequest.this.getClass().getSimpleName() + " [ " + (contentId!=null? String.valueOf(contentId):"null"));
	}
	
	public void execute() {
		logger.debug(getContent().getTitle());
	}
	
	public Content getContent() {
		if (content==null) {
			content = (Content)getContentDao().findContentById(contentId);
		}
		return content;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
