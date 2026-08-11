package com.novamens.kbee.content.webapi.handler;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ITransaction;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;

import com.novamens.content.workflow.WorkflowService;

public class TakeTaskHandler extends AbstractRequestHandler {
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TakeTaskHandler.class.getName());
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Transactional
	public ITransaction handle(String contentId) {
		
		try {
			
	        Content content = getContentDao().findContentById(Long.valueOf(contentId));
	        
	        if (content==null) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.FILE_NOT_FOUND);
	        }
	        
	        if (!isTakeable(content)) {
	            throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
	        }
	        
			WorkflowService ws = content.getService(WorkflowService.class);
			
			if (ws==null || ws.getContext().getTime()!=null) {
				// Task Started
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.ACTIVITY_NOT_FOUND);
			}
	        
			content.getService(WorkflowService.class).startTask();
			
			ITransaction transaction  = getTransaction(getProxy(content));
			
			return transaction;
		}
		catch (ApiException e) {
			throw e;
		}
		catch (ContentMgmtException e) {
			logger.error(e);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
		catch (Exception e) {
			logger.error(e);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}	
	}
	
    private boolean isTakeable(Content content) {
        return ServiceLocator.getService(ContentSystemSecurityService.class).isTakeable(content);
    }
}