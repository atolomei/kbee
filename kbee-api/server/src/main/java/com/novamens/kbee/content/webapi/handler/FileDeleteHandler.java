package com.novamens.kbee.content.webapi.handler;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.service.ContentService;
import com.novamens.dom.Domain;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.webapi.command.DeleteFilesCommand;
import com.novamens.kbee.content.webapi.type.UriHelper;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiFile;
import kbee.api.model.ApiProxy;
import kbee.api.model.ITransaction;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;

public class FileDeleteHandler extends AbstractRequestHandler {
			

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(FileDeleteHandler.class.getName());

	
	@Transactional
	public ITransaction delete(ApiFile file) {
		return delete(file, false);
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Transactional
	public ITransaction delete(String source, String externalId) {
		return delete(source, externalId, false);
	}	
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Transactional
	public ITransaction delete(String criteria, boolean recycle) {
		CommandService service = ServiceLocator.getService(CommandService.class);
		DeleteFilesCommand command = new DeleteFilesCommand(criteria, recycle);
		service.add(command);
		ITransaction transaction  = getTransaction(new ApiProxy(command.getName(), UriHelper.getUri(command)));
		return transaction;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Transactional
	public ITransaction recycle(ApiFile file) {
		return delete(file, true);
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Transactional
	public ITransaction recycle(String source, String externalId) {
		return delete(source, externalId, true);
	}	
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	public ITransaction delete(String source, String externalId, boolean recycle) {
		Content content = getContent(source, externalId);
		
		if (!content.isHeadVersion()) {
			throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
		}
		
		if (!isDeletable(content)) {
			throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
		}
		
		if (recycle) {
			content.getService(ContentService.class).recycle();
		}
		else {
			content.getService(ContentService.class).deleteAllVersions();
		}
		
		ITransaction transaction  = getTransaction(getProxy(content));
		
		return transaction;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected ITransaction delete(ApiFile file, boolean recycle) {
		try {
			Domain domain = getDomain(file);
			
			if (domain == null) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.DOMAIN_NOT_FOUND);
			}
			
//			if (getApplication(domain) == null || !getApplication(domain).equals(file.getApplication())) {
//				throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.INVALID_APPLICATION);
//			}
			
			su(domain);
			
			Content content = getContent(file);

			if (!content.isHeadVersion()) {
				throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
			}
			
			if (!isDeletable(content)) {
				throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
			}
			
			if (recycle) {
				content.getService(ContentService.class).recycle();
			}
			else {
				content.getService(ContentService.class).deleteAllVersions();
			}
			
			ITransaction transaction  = getTransaction(getProxy(file));
			
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
			e.printStackTrace();
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	private Content getContent(ApiFile file) {
		if (file.getExternalId()==null || "".equals(file.getExternalId())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.ATTRIBUTE_IS_REQUIRED, "externalid");
		}
		Content content = getContentDao().findContentByExternalId(file.getApplication(), file.getExternalId());
		if (content == null) {
			throw new ApiException(HttpStatus.NOT_FOUND, ApiError.FILE_NOT_FOUND);
		}
		return content;
	}	
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	private Content getContent(String source, String externalId) {
		Content content = getContentDao().findContentByExternalId(source, externalId);
		if (content == null) {
			throw new ApiException(HttpStatus.NOT_FOUND, ApiError.FILE_NOT_FOUND);
		}
		return content;
	}
}