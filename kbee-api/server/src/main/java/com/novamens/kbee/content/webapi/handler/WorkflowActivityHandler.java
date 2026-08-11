package com.novamens.kbee.content.webapi.handler;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.Resource;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.DOMObjectService;
import com.novamens.kbee.lock.LockTransactionSynchronization;
import com.novamens.workflow.ActivityProgressNote;

import kbee.api.model.INote;
import kbee.api.model.ApiResource;
import kbee.api.model.ITransaction;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;

import com.novamens.content.workflow.WorkflowService;
import com.novamens.kbee.content.workflow.KbeeActivityProgressNote;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.content.workflow.KbeeWorkflowActivity;

public class WorkflowActivityHandler extends AbstractRequestHandler {
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(WorkflowActivityHandler.class.getName());
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Transactional
	public ITransaction cancel(String activityId) {
		
		try {
			
			KbeeWorkflowActivity activity = null;
			try {
				activity = (KbeeWorkflowActivity)getWorkflowDao().findActivityById(Long.valueOf(activityId));
			}
			catch (Exception e) {
				logger.error(e);
			}
			
			if (activity==null) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.ACTIVITY_NOT_FOUND);
			}
			
			if (!activity.getStatus().equals(com.novamens.workflow.Activity.Status.RUNNING) || !activity.getUser().equals(getUser())) {
				throw new ApiException(HttpStatus.GONE, ApiError.ACTIVITY_ILLEGAL_STATE);
			}
			
			
			Content content = activity.getContent();
			
			new LockTransactionSynchronization(String.valueOf(activity.getContent().getId()));
			
			KbeeTask task = (KbeeTask)content.getService(WorkflowService.class).getTask();
			
			if (!task.isCancelEnabled()) {
	            throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
			}

			content.getService(WorkflowService.class).cancel();

			ITransaction transaction  = getTransaction(getProxy(content));
			
			return transaction;
		}
		catch (ApiException e) {
			throw e;
		}
		catch (Exception e) {
			logger.error(e);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}	
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Transactional
	public ITransaction add(String activityId, INote inote) {
		
		try {
			
			KbeeWorkflowActivity activity = null;
			try {
				activity = (KbeeWorkflowActivity)getWorkflowDao().findActivityById(Long.valueOf(activityId));
			}
			catch (Exception e) {
				logger.error(e);
			}
			
			if (activity==null) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.ACTIVITY_NOT_FOUND);
			}
			
			if (!activity.getStatus().equals(com.novamens.workflow.Activity.Status.RUNNING)) {
				throw new ApiException(HttpStatus.GONE, ApiError.ACTIVITY_ILLEGAL_STATE);
			}
			
			
			Content content = activity.getContent();
			
			new LockTransactionSynchronization(String.valueOf(activity.getContent().getId()));

			KbeeActivityProgressNote note = (KbeeActivityProgressNote)content.getService(WorkflowService.class).createProgressNote();
			
			note.setText(inote.getText());
			note.setLastModifiedUser(getUser());
			note.setActivity(activity);
			for (ApiResource iresource : inote.getResources()) {
				Resource resource = getResource(iresource);
				note.addResource(resource);
			}
			note.getService(DOMObjectService.class).update();
			
			content.getService(WorkflowService.class).publish(note);

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
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Transactional
	public ITransaction update(String activityId, INote inote) {
		
		try {
			
			KbeeWorkflowActivity activity = null;
			try {
				activity = (KbeeWorkflowActivity)getWorkflowDao().findActivityById(Long.valueOf(activityId));
			}
			catch (Exception e) {
				logger.error(e);
			}
			
			if (activity==null) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.ACTIVITY_NOT_FOUND);
			}
			
			if (!activity.getStatus().equals(com.novamens.workflow.Activity.Status.RUNNING)) {
				throw new ApiException(HttpStatus.GONE, ApiError.ACTIVITY_ILLEGAL_STATE);
			}
			
			
			Content content = activity.getContent();
			
			new LockTransactionSynchronization(String.valueOf(activity.getContent().getId()));

			KbeeActivityProgressNote note = null;
			for (ActivityProgressNote activitynote : activity.getProgressNotes()) {
				if (inote.getId().equals(String.valueOf(activitynote.getId()))) {
					note = (KbeeActivityProgressNote)activitynote;
					break;
				}
			}
			note.setText(inote.getText());
			note.setLastModifiedUser(getUser());
			note.setActivity(activity);
			note.getResources().clear();
			for (ApiResource iresource : inote.getResources()) {
				Resource resource = getResource(iresource);
				note.addResource(resource);
			}
			note.getService(DOMObjectService.class).update();
			
			content.getService(WorkflowService.class).publish(note);

			ITransaction transaction  = getTransaction(getProxy(content));
			
			return transaction;
		}
		catch (ApiException e) {
			throw e;
		}
		catch (Exception e) {
			logger.error(e);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}	
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Transactional
	public ITransaction delete(String activityId, INote inote) {
		
		try {
			
			KbeeWorkflowActivity activity = null;
			try {
				activity = (KbeeWorkflowActivity)getWorkflowDao().findActivityById(Long.valueOf(activityId));
			}
			catch (Exception e) {
				logger.error(e);
			}
			
			if (activity==null) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.ACTIVITY_NOT_FOUND);
			}
			
			if (!activity.getStatus().equals(com.novamens.workflow.Activity.Status.RUNNING)) {
				throw new ApiException(HttpStatus.GONE, ApiError.ACTIVITY_ILLEGAL_STATE);
			}
			
			Content content = activity.getContent();
			
			new LockTransactionSynchronization(String.valueOf(activity.getContent().getId()));

			KbeeActivityProgressNote note = null;
			for (ActivityProgressNote activitynote : activity.getProgressNotes()) {
				if (inote.getId().equals(String.valueOf(activitynote.getId()))) {
					note = (KbeeActivityProgressNote)activitynote;
					break;
				}
			}
			
			content.getService(WorkflowService.class).deleteProgressNote(note);

			ITransaction transaction  = getTransaction(getProxy(content));
			
			return transaction;
		}
		catch (ApiException e) {
			throw e;
		}
		catch (Exception e) {
			logger.error(e);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}	
	}

	
	protected Resource getResource(ApiResource value) {
		try {
			Resource resource = getContentDao().findResourceById(KBFile.class, Long.valueOf(value.getId()));
			return resource;
		} 
		catch (Exception e) {
			throw new ApiException(HttpStatus.NOT_FOUND, ApiError.NO_DATA, null, "resource " +  value.getId());
		}
	}

}