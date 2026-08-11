package kbee.api.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
import kbee.api.model.IPerson;
import kbee.api.model.ApiResource;
import kbee.api.model.IResourceTag;
import kbee.api.model.IResultSet;
import kbee.api.model.IRole;
import kbee.api.model.ISettings;
import kbee.api.model.ITemplate;
import kbee.api.model.IToken;
import kbee.api.model.ITransaction;
import kbee.api.model.ApiUser;

public interface ApiService {
	public <T> T get(Class<T> iclass, String url);
	public ApiFile getFile(String uri);
	
	public List<ApiClassifier> getClassifiers();
	public ApiClassifier getClassifier(String id);
	
	public List<IModelAttribute> getAttributes();
	public IModelAttribute getAttribute(String id);
	
	public List<ApiDataSet> getDataSets();
	public ApiDataSet getDataSet(String id);
	
	public IResultSet<ApiValue> getValues(ApiDataSet dataSet);
	
	public IPerson getPerson(String id);
	
	public List<IResourceTag> getResourceTags();
	public IResourceTag getResourceTag(String id);
	
	public List<ILauncher> getLaunchers();
	
	public List<ILauncherGroup> getLauncherGroups();
	public ILauncherGroup getLauncherGroup(String id);
	
	public List<ILibrary> getLibraries();
	
	public List<IFacet> getFacets();
	
	public IToken getToken();
	
	public List<ITemplate> getTemplates();
	public ITemplate getTemplate(String id);
	
	public IForm getForm(String id);
	
	public ApiProcedure getProcedure(String id);
	
	public IResultSet<ApiProxy> getGroups();
	public IGroup getGroup(String id);
	
	public IResultSet<ApiProxy> getUsers();
	public ApiUser getUser(String id);
	
	public IResultSet<ApiProxy> getRoles();
	public IRole getRole(String id);
	
	public IResultSet<IEmailTemplate> getEmailTemplates();
	
	public String getUrl();
	
	public ISettings getSettings();
	
	public ApiResource upload(File file) throws IOException;
	
	public InputStream getResource(String resourceurl) throws IOException;
	
	public ITransaction replicate(ApiObject object, String replicaId);
}