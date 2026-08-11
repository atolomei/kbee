package com.novamens.kbee.content.webapi.handler;

import org.apache.logging.log4j.LogManager;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.entity.Person;
import com.novamens.content.model.PersonMember;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.webapi.type.UriHelper;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiProxy;
import kbee.api.model.ITransaction;
import kbee.api.model.ApiUser;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;

public class UserDeleteHandler extends AbstractRequestHandler {
	
	static private kbee.util.logging.Logger  logger = new kbee.util.logging.Logger(LogManager.getLogger(UserDeleteHandler.class.getName()));
	
	@Transactional
	public ITransaction delete(ApiUser user) {
		try {
			Domain domain = getDomain(user);
			
			if (domain == null) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.DOMAIN_NOT_FOUND);
			}
			
			su(domain);
			
			
			Person person = getUser(user);
			
			if (isRootUser(person)) {
				throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
			}
			
			ServiceLocator.getService(SecurityContentMgmtService.class).delete(person);
			
			getContentDao().flush();
			
			ITransaction transaction  = getTransaction(getProxy(user));
			
			return transaction;
		}
		catch (ApiException e) {
			throw e;
		}
		catch (ContentMgmtException e) {
			logger.error(e);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
		catch (AuthenticationException e) {
			logger.error(e);
			throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED, e.getMessage());
		}
		catch (Exception e) {
			logger.error(e);
			if (e.getCause() instanceof ConstraintViolationException) {
				throw new ApiException(HttpStatus.FAILED_DEPENDENCY, ApiError.USER_CONSTRAINT);
			}
			else {
				throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
			}	
		}
	}
	
	protected Person getUser(ApiUser user) throws ContentMgmtException {
		Person person = null;
		if (user.getId()!=null) {
			try {
				person = (PersonMember)getContentDao().findMemberById(Long.valueOf(user.getId()));
				if (person==null) {
					throw new ApiException(HttpStatus.NOT_FOUND, ApiError.USER_NOT_FOUND);
				}
			}
			catch (NumberFormatException e) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.USER_NOT_FOUND, e.getMessage());
			}
		}
		else {
			throw new ApiException(HttpStatus.NOT_FOUND, ApiError.USER_NOT_FOUND, "no id");
		}
		return person;
	}
	
	protected ApiProxy getProxy(ApiUser user) {
		return new ApiProxy(user.getDisplayName(), UriHelper.getUri(user));
	}
	
	protected boolean isRootUser(Person person) {
		UserProfile profile = person.getProfile(UserProfile.class);
		if (profile!=null && 
				profile.getUser()!=null && 
				profile.getUser().getName()!=null && 
				profile.getUser().getName().startsWith("root@")) {
			return true;
		}
		return false;
	}

}