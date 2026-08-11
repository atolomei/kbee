package com.novamens.kbee.content.webapi.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.resource.KBFile;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiFile;
import kbee.api.model.ApiObject;
import kbee.api.model.ApiProcedure;
import kbee.api.model.ApiProxy;
import kbee.api.model.ApiValue;
import kbee.api.model.ApiClassifier;
import kbee.api.model.ApiDataSet;
import kbee.api.model.IEmailTemplate;
import kbee.api.model.IFacet;
import kbee.api.model.IForm;
import kbee.api.model.IGroup;
import kbee.api.model.ILauncher;
import kbee.api.model.ILauncherGroup;
import kbee.api.model.ILibrary;
import kbee.api.model.IModelAttribute;
import kbee.api.model.IPageRequest;
import kbee.api.model.IPerson;
import kbee.api.model.ApiResource;
import kbee.api.model.IResourceTag;
import kbee.api.model.IResponse;
import kbee.api.model.IResultSet;
import kbee.api.model.IRole;
import kbee.api.model.ISettings;
import kbee.api.model.ITemplate;
import kbee.api.model.IToken;
import kbee.api.model.ITransaction;
import kbee.api.model.ApiUser;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;
import kbee.api.service.ApiService;

@SuppressWarnings("serial")
public class LocalApiServiceWrapper implements ApiService {

	protected Logger logger = LogManager.getLogger(LocalApiServiceWrapper.this.getClass());

	ApiController controller;
	Domain domain;
	
	public LocalApiServiceWrapper(Domain domain) {
		this.domain = domain; 
		this.controller = new ApiController() {
			@Override
			protected void su(String domainname) {
			}
			@Override
			protected Domain getDomain() {
				return LocalApiServiceWrapper.this.getDomain();
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> iclass, String url) {
		try {
			String path[] = url.split("/");
			if (path.length==4 && "groups".equals(path[2])) {
				IGroup igroup = getController().getGroup(path[1], Long.valueOf(path[3])).getBody();
				return (T)igroup;
			}
			if (path.length==4 && "roles".equals(path[2])) {
				IRole irole = getController().getRole(path[1], Long.valueOf(path[3])).getBody();
				return (T)irole;
			}
			if (path.length==6 && "datasets".equals(path[2])) {
				ApiValue value = getController().getValue(path[1], path[3], Long.valueOf(path[5])).getBody();
				return (T)value;
				
			}
		}
		catch(Exception e) {
			logger.error(e);
		}
		return null;
	}
	
	public ApiFile getFile(String uri) {
		return null;
	}
	
	public List<ApiDataSet> getDataSets() {
		return getController().getDataSets(getDomain().getName()).getBody();
	}
	
	public ApiDataSet getDataSet(String id) {
		return getController().getDataSet(getDomain().getName(), id).getBody();
	}
	
	public ISettings getSettings() {
		return getController().getSettings(getDomain().getName()).getBody();
	}
	
	public IResultSet<ApiValue> getValues(ApiDataSet dataSet) {
		IResultSet<ApiValue> resultSet = new IResultSet<ApiValue>(new IPageRequest<ApiValue>() {
			public IResponse<ApiValue> execute(long offset) {
				IResponse<ApiValue> page = getController().getValues(dataSet.getDomain(), dataSet.getId(), 
						Optional.empty(), 
						Optional.empty(), 
						Optional.empty(), 
						Optional.empty(), 
						Optional.of(offset), Optional.empty()).getBody();
				return page;
			}
		});
		return resultSet;
	}
	
	public IResultSet<ApiProxy> getRoles() {
		IResultSet<ApiProxy> resultSet = new IResultSet<ApiProxy>(new IPageRequest<ApiProxy>() {
			public IResponse<ApiProxy> execute(long offset) {
				IResponse<ApiProxy> page = getController().getRoles(getDomain().getName(), Optional.of(offset), Optional.empty(), Optional.empty()).getBody();
				return page;
			}
		});
		return resultSet;
	}
	
	public IRole getRole(String id) {
		return getController().getRole(getDomain().getName(), Long.valueOf(id)).getBody();
	}
	
	public IGroup getGroup(String id) {
		return getController().getGroup(getDomain().getName(), Long.valueOf(id)).getBody();
	}
	
//	public IValue getValue(String id) {
//		//return getController().getVGroup(getDomain().getName(), Long.valueOf(id)).getBody();
//		return null;
//	}
	
	public IResultSet<ApiProxy> getUsers() {
		return null;
	}
	
	public ApiUser getUser(String id) {
		return getController().getUser(Long.valueOf(id), Optional.empty()).getBody();
	}
	
	public ApiUser getUser(String id, boolean all) {
		return getController().getUser(Long.valueOf(id), Optional.of(all?"true":"false")).getBody();
	}
	
	public IPerson getPerson(String id) {
		return getController().getPerson(getDomain().getName(), id).getBody();
	}
	
	public IToken getToken() {
		return null;
	}
	
	public List<ApiClassifier> getClassifiers() {
		return getController().getClassifiers(getDomain().getName()).getBody();
	}
	
	public ApiClassifier getClassifier(String id) {
		return getController().getClassifier(getDomain().getName(), Long.valueOf(id)).getBody();
	}
	
	public List<IModelAttribute> getAttributes() {
		return getController().getAttributes(getDomain().getName()).getBody();
	}
	
	public IModelAttribute getAttribute(String id) {
		return getController().getAttribute(getDomain().getName(), Long.valueOf(id)).getBody();
	}
	
	public List<IResourceTag> getResourceTags() {
		return getController().getResourceTags(getDomain().getName()).getBody();
	}
	
	public IResourceTag getResourceTag(String id) {
		return getController().getResourceTag(getDomain().getName(), Long.valueOf(id)).getBody();
	}
	
	public List<ILauncher> getLaunchers() {
		return getController().getLaunchers(getDomain().getName(), Optional.empty()).getBody();
	}
	
	public List<ILauncherGroup> getLauncherGroups() {
		return getController().getLauncherGroups(getDomain().getName()).getBody();
	}
	
	public ILauncherGroup getLauncherGroup(String id) {
		return getController().getLauncherGroup(getDomain().getName(), Long.valueOf(id)).getBody();
	}
	
	public List<ILibrary> getLibraries() {
		return getController().getLibraries(getDomain().getName()).getBody();
	}
	
	public List<IFacet> getFacets() {
		return getController().getFacets(getDomain().getName()).getBody();
	}
	
	public List<ITemplate> getTemplates() {
		return getController().getTemplates(getDomain().getName()).getBody();
	}
	
	public ITemplate getTemplate(String id) {
		return getController().getTemplate(getDomain().getName(), Long.valueOf(id)).getBody();
	}
	
	public IForm getForm(String id) {
		return getController().getForm(Long.valueOf(id)).getBody();
	}
	
	public ApiProcedure getProcedure(String id) {
		return getController().getProcedure(Long.valueOf(id)).getBody();
	}
	
	public ITransaction replicate(ApiObject object, String replicaId) {
		return null;
	}
	
	public IResultSet<IEmailTemplate> getEmailTemplates() {
		IResultSet<IEmailTemplate> resultSet = new IResultSet<IEmailTemplate>(new IPageRequest<IEmailTemplate>() {
			public IResponse<IEmailTemplate> execute(long offset) {
				IResponse<IEmailTemplate> page = getController().getEmailTemplates(getDomain().getName(), Optional.of(offset), Optional.empty()).getBody();
				return page;
			}
		});
		return resultSet;
	}
	
	public IResultSet<ApiProxy> getGroups() {
		IResultSet<ApiProxy> resultSet = new IResultSet<ApiProxy>(new IPageRequest<ApiProxy>() {
			public IResponse<ApiProxy> execute(long offset) {
				IResponse<ApiProxy> page = getController().getGroups(getDomain().getName(), Optional.empty(), Optional.of(offset), Optional.empty()).getBody();
				return page;
			}
		});
		return resultSet;
	}
	
	public ApiResource upload(File file) throws IOException {
		return null;
	}
	
	public InputStream getResource(String resourceurl)  {
		//"/resource/content/CId-kbeeidoc-98482951/ANEXO I.pdf"
		try {
			String path[] = resourceurl.split("/");
			String resourcename = path[3];
			String resourceid = resourcename.split("-")[2];
			ResourceContainer content = (ResourceContainer)getContentDao().findContentById(Long.valueOf(resourceid));
			
			for (KBFile file : content.getFiles()) {
				if (file.getName().toLowerCase().equals(path[4].toLowerCase())) {
					return file.getInputStream();
				}
			}
		}
		catch (IOException e) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
		
		throw new ApiException(HttpStatus.NOT_FOUND, ApiError.FILE_NOT_FOUND);
	}

	public String getUrl() {
		return "localhost";
	}
	
	public ApiController getController() {
		return controller;
	}
	
	public Domain getDomain() {
		return domain;
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
