package com.novamens.kbee.content.webapi.handler;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.entity.Person;
import com.novamens.content.model.PersonMember;
import com.novamens.content.service.PersonService;
import com.novamens.content.user.UserProfile;
import com.novamens.kbee.lock.LockTransactionSynchronization;
import com.novamens.security.User;

import kbee.api.model.ITransaction;
import kbee.api.model.IWorkflowEvent;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;

import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.KbeeWorkflowActivity;
import com.novamens.kbee.content.workflow.KbeeWorkflowEvent;

public class WorkflowEventHandler extends AbstractRequestHandler {
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(WorkflowEventHandler.class.getName());
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Transactional
	public ITransaction handle(IWorkflowEvent event) {
		
		try {
			Domain domain = getDomain(event.getDomain());
			
			if (domain == null || !domain.equals(getDomain()))  {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.DOMAIN_NOT_FOUND);
			}
			
			KbeeWorkflowActivity activity = null;
			try {
				activity = (KbeeWorkflowActivity)getWorkflowDao().findActivityById(Long.valueOf(event.getActivity()));
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
			
			KbeeContext context = (KbeeContext)content.getService(WorkflowService.class).getContext();
			
			if (event.getNote()!=null && !"".equals(event.getNote().trim())) {
				context.setNote(event.getNote());
			}
			
			for (String parameter: event.getParameters().keySet()) {
				context.setParameter(parameter, event.getParameters().get(parameter));
			}
			
			if (!"".equals(event.getCollaborator()) && event.getCollaborator()!=null) {
				User collaborator = null;
				try {
					Person person = (PersonMember)getContentDao().findMemberById(Long.valueOf(event.getCollaborator()));
					if (person!=null) {
						if (person.getProfile(UserProfile.class)!=null) {
							collaborator = person.getProfile(UserProfile.class).getUser();
						}
						if (collaborator==null) {
							collaborator = person.getService(PersonService.class).createUser();
						}
					}
				}
				catch (Exception e) {
					logger.error(e);
				}
				if (collaborator!=null) {
					context.setCollaborator(collaborator);
				}
			}
			
			content.getService(WorkflowService.class).handle(new KbeeWorkflowEvent(event.getName()), context);

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
}