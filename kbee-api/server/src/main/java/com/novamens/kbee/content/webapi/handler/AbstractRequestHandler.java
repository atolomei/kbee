package com.novamens.kbee.content.webapi.handler;

import java.time.OffsetDateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.content.user.UserService;
import com.novamens.content.workflow.WorkflowDao;
import com.novamens.dao.SecurityDao;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.webapi.transaction.ApiTransactionService;
import com.novamens.kbee.content.webapi.type.UriHelper;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Activity;

import kbee.api.model.ApiFile;
import kbee.api.model.ApiObject;
import kbee.api.model.ApiProxy;
import kbee.api.model.ITransaction;

public class AbstractRequestHandler implements RequestHandler {
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected Domain getDomain(ApiObject file) {
		return getDomain(file.getDomain());
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected Domain getDomain(String name) {
		if (name==null)
			return getDomain();
		for (Domain domain : getContentDao().getDomains()) {
			if (name.toLowerCase().equals(domain.getName().toLowerCase()))
				return domain;
		}
		return null;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected void su(Domain domain) {
		if (!getDomain().equals(domain)) {
			String username = getUser().getName();
			int i = username.indexOf("@");
			if (i<0) i = username.length();
			username = username.substring(0, i);
			String suusername = username + "@" + domain.getName();
			ServiceLocator.getService(SecurityService.class).authenticate(suusername);
		}
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected boolean isWriteable(Content content) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(content);
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected boolean isDeletable(Content content) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isDeleteable(content);
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected ApiProxy getProxy(ApiFile file) {
		return new ApiProxy(file.getTitle(), UriHelper.getUri(file));
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected ApiProxy getProxy(Content content) {
		return new ApiProxy(String.valueOf(content.getId()), content.getTitle(), UriHelper.getUri(content), "file");
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected ApiProxy getProxy(Resource resource) {
		return new ApiProxy(String.valueOf(resource.getId()), resource.getName(), UriHelper.getUri(resource), "resource");
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected ApiProxy getProxy(Activity activity) {
		return new ApiProxy(String.valueOf(activity.getId()), activity.getDisplayName(), UriHelper.getUri(activity), "activity");
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected boolean equals(String s1, String s2) {
		if (s1!=null && !s1.equals(s2))
			return false;
		if (s2!=null && !s2.equals(s1))
			return false;
		return true;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected boolean equals(OffsetDateTime s1, OffsetDateTime s2) {
		if (s1!=null && !s1.equals(s2))
			return false;
		if (s2!=null && !s2.equals(s1))
			return false;
		return true;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected String toJson(Object object) {
		GsonBuilder b = new GsonBuilder();
		Gson gson = b.create();
		String json = gson.toJson(object);
		return json;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected String getApplication(Domain domain) {
		return domain.getService(DomainSettingsService.class).get("name", "application");
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected ITransaction getTransaction(ApiProxy proxy) {
		return ServiceLocator.getService(ApiTransactionService.class).getTransaction(proxy);
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected User getUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected SecurityDao getSecurityDao() {
		return (SecurityDao)ServiceLocator.getService(BeansService.class).getBean("securityDao");
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected WorkflowDao getWorkflowDao() {
		return (WorkflowDao)ServiceLocator.getService(BeansService.class).getBean("WorkflowDao");
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected <R> DomRepository<R> getRepository(Class<R> objectclass) {
		DomRepository<R> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
	}
}
