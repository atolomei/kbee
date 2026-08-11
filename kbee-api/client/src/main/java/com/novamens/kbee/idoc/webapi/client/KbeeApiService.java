package com.novamens.kbee.idoc.webapi.client;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import com.caucho.hessian.client.HessianProxyFactory;

import kbee.api.model.ApiFile;
import kbee.api.model.ApiObject;
import kbee.api.model.ApiProcedure;
import kbee.api.model.ApiProxy;
import kbee.api.model.ApiValue;
import kbee.api.model.IBinaryResource;
import kbee.api.model.ApiClassifier;
import kbee.api.model.ApiDataSet;
import kbee.api.model.IEmailTemplate;
import kbee.api.model.IError;
import kbee.api.model.IFacet;
import kbee.api.model.IForm;
import kbee.api.model.IGroup;
import kbee.api.model.ILauncher;
import kbee.api.model.ILauncherGroup;
import kbee.api.model.ILibrary;
import kbee.api.model.ILogEvent;
import kbee.api.model.IModelAttribute;
import kbee.api.model.IMultipartResource;
import kbee.api.model.IObjectReplica;
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
import kbee.api.service.ApiBinaryService;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;
import kbee.api.service.ApiService;

@SuppressWarnings("serial")
public class KbeeApiService implements ApiService {
																
	protected static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(KbeeApiService.class.getName());

	private String url;
	private String user;
	private String password;
	private int chunk = 0;
	
	static final int READ_TIMEOUT = 45000;
 
	private RestTemplate restTemplate = new RestTemplate();
	final RestObjectMapper restObjectMapper = new RestObjectMapper();

	public KbeeApiService(String url) {
		setUrl(url);
		List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
		converters.add(new JacksonHttpMessageConverter ());
		converters.add(new FormHttpMessageConverter());
		
		restTemplate.setMessageConverters(converters);
	}
	
	public KbeeApiService(String url, String user, String password) {
		setUrl(url);
		setUser(user);
		setPassword(password);
		
		List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
		converters.add(new JacksonHttpMessageConverter());
		converters.add(new FormHttpMessageConverter());
		
		restTemplate.setMessageConverters(converters);
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUrl() {
		return this.url;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public String getUser() {
		return this.user;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	
	public int getChunk() {
		return chunk;
	}

	public void setChunk(int chunk) {
		this.chunk = chunk;
	}

	public <T> T get(Class<T> iclass, String url) {
		return exchange(getUrl()+"/"+url, HttpMethod.GET, new HttpEntity<String>(getHeaders()), iclass);
	}
	
	public ApiFile getFile(String uri) {
		return exchange(getUrl()+"/"+uri, HttpMethod.GET, new HttpEntity<String>(getHeaders()), ApiFile.class);
	}
	
	public ApiFile getFileById(String id) {
		return getFileById(id, false);
	}
	
	public ApiFile getFileById(String id, boolean tokens) {
		return exchange(getUrl()+"/file/0/"+id+(tokens?"?tokens=true":""), HttpMethod.GET, new HttpEntity<String>(getHeaders()), ApiFile.class);
	}
	
	public ApiFile getFileByExternalId(String application, String externalId) {
		return getFileByExternalId(application, externalId, false);
	}
	
	public ApiFile getFileByExternalId(String application, String externalId, boolean tokens) {
		return exchange(getUrl()+"/file/"+application+"/"+getDomain()+"/"+externalId+(tokens?"?tokens=true":""), HttpMethod.GET, new HttpEntity<String>(getHeaders()), ApiFile.class);
	}
	
	public List<ApiDataSet> getDataSets() {
		return exchange(getUrl()+"/"+getDomain()+"/datasets", HttpMethod.GET, new HttpEntity<String>(getHeaders()),  new ParameterizedTypeReference<List<ApiDataSet>>() {} );
	}
	
	public ApiDataSet getDataSet(String id) {
		return exchange(getUrl()+"/"+getDomain()+"/datasets/" + id, HttpMethod.GET, new HttpEntity<String>(getHeaders()), ApiDataSet.class);
	}
	
	public List<ApiClassifier> getClassifiers() {
		return exchange(getUrl()+"/"+getDomain()+"/classifiers", HttpMethod.GET, new HttpEntity<String>(getHeaders()),  new ParameterizedTypeReference<List<ApiClassifier>>() {} );
	}
	
	public ApiClassifier getClassifier(String id) {
		return exchange(getUrl()+"/"+getDomain()+"/classifiers/" + id, HttpMethod.GET, new HttpEntity<String>(getHeaders()), ApiClassifier.class);
	}
	
	public List<IModelAttribute> getAttributes() {
		return exchange(getUrl()+"/"+getDomain()+"/attributes", HttpMethod.GET, new HttpEntity<String>(getHeaders()),  new ParameterizedTypeReference<List<IModelAttribute>>() {} );
	}
	
	public IModelAttribute getAttribute(String id) {
		return exchange(getUrl()+"/"+getDomain()+"/attributes/" + id, HttpMethod.GET, new HttpEntity<String>(getHeaders()), IModelAttribute.class);
	}
	
	public List<IResourceTag> getResourceTags() {
		return exchange(getUrl()+"/"+getDomain()+"/resourcetags", HttpMethod.GET, new HttpEntity<String>(getHeaders()),  new ParameterizedTypeReference<List<IResourceTag>>() {} );
	}
	
	public List<ILauncher> getLaunchers() {
		return exchange(getUrl()+"/"+getDomain()+"/launchers", HttpMethod.GET, new HttpEntity<String>(getHeaders()),  new ParameterizedTypeReference<List<ILauncher>>() {} );
	}
	
	public List<ILauncherGroup> getLauncherGroups() {
		return exchange(getUrl()+"/"+getDomain()+"/launchergroups", HttpMethod.GET, new HttpEntity<String>(getHeaders()),  new ParameterizedTypeReference<List<ILauncherGroup>>() {} );
	}
	
	public IResourceTag getResourceTag(String id) {
		return exchange(getUrl()+"/"+getDomain()+"/resourcetags/" + id, HttpMethod.GET, new HttpEntity<String>(getHeaders()), IResourceTag.class);
	}
	
	public ILauncherGroup getLauncherGroup(String id) {
		return exchange(getUrl()+"/"+getDomain()+"/launchergroups/" + id, HttpMethod.GET, new HttpEntity<String>(getHeaders()), ILauncherGroup.class);
	}
	
	public ISettings getSettings() {
		return exchange(getUrl()+"/"+getDomain()+"/settings", HttpMethod.GET, new HttpEntity<String>(getHeaders()),  new ParameterizedTypeReference<ISettings>() {} );
	}
	
	public List<ILibrary> getLibraries() {
		return exchange(getUrl()+"/"+getDomain()+"/libraries", HttpMethod.GET, new HttpEntity<String>(getHeaders()),  new ParameterizedTypeReference<List<ILibrary>>() {} );
	}
	
	public List<IFacet> getFacets() {
		return exchange(getUrl()+"/"+getDomain()+"/facets", HttpMethod.GET, new HttpEntity<String>(getHeaders()),  new ParameterizedTypeReference<List<IFacet>>() {} );
	}	
	
	public List<ITemplate> getTemplates() {
		return exchange(getUrl()+"/"+getDomain()+"/templates", HttpMethod.GET, new HttpEntity<String>(getHeaders()),  new ParameterizedTypeReference<List<ITemplate>>() {} );
	}
	
	public ITemplate getTemplate(String id) {
		return exchange(getUrl()+"/"+getDomain()+"/template/" + id, HttpMethod.GET, new HttpEntity<String>(getHeaders()), ITemplate.class);
	}
	
	public IForm getForm(String id) {
		return exchange(getUrl()+"/"+getDomain()+"/forms/" + id, HttpMethod.GET, new HttpEntity<String>(getHeaders()), IForm.class);
	}
	
	public ApiProcedure getProcedure(String id) {
		return exchange(getUrl()+"/"+getDomain()+"/procedures/" + id, HttpMethod.GET, new HttpEntity<String>(getHeaders()), ApiProcedure.class);
	}

	public IResultSet<ApiValue> getValues(ApiDataSet dataSet) {
		final String datasetId = dataSet.getId();
		final String url = getUrl()+"/"+getDomain()+"/datasets/"+datasetId+"/values";
		IResultSet<ApiValue> resultSet = new IResultSet<ApiValue>(new IPageRequest<ApiValue>() {
			public IResponse<ApiValue> execute(long offset) {
				String pageurl = url + "?offset=" + String.valueOf(offset); 
				IResponse<ApiValue> page = exchange(pageurl, HttpMethod.GET, new HttpEntity<String>(getHeaders()),  new ParameterizedTypeReference<IResponse<ApiValue>>() {} );
				return page;
			}
		});
		return resultSet;
	}
	
//	public IValue getValue(String id) {
//		return exchange(getUrl()+"/"+getDomain()+"/procedures/" + id, HttpMethod.GET, new HttpEntity<String>(getHeaders()), IValue.class);
//	}
	
	public IResultSet<ApiProxy> getUsers() {
		final String url = getUrl()+"/"+getDomain()+"/users";
		IResultSet<ApiProxy> resultSet = new IResultSet<ApiProxy>(new IPageRequest<ApiProxy>() {
			public IResponse<ApiProxy> execute(long offset) {
				String pageurl = url + "?offset=" + String.valueOf(offset); 
				IResponse<ApiProxy> page = exchange(pageurl, HttpMethod.GET, new HttpEntity<String>(getHeaders()),  new ParameterizedTypeReference<IResponse<ApiProxy>>() {} );
				return page;
			}
		});
		return resultSet;
	}
	
	public ApiUser getUser(String id) {
		return exchange(getUrl()+"/"+getDomain()+"/users/" + id, HttpMethod.GET, new HttpEntity<String>(getHeaders()), ApiUser.class);
	}
	
	public ApiUser getUser(String id, boolean all) {
		return exchange(getUrl()+"/"+getDomain()+"/users/" + id + "?all=true", HttpMethod.GET, new HttpEntity<String>(getHeaders()), ApiUser.class);
	}
	
	public IPerson getPerson(String id) {
		return exchange(getUrl()+"/"+getDomain()+"/persons/" + id, HttpMethod.GET, new HttpEntity<String>(getHeaders()), IPerson.class);
	}
	
	public IResultSet<ApiProxy> getRoles() {
		final String url = getUrl()+"/"+getDomain()+"/roles";
		IResultSet<ApiProxy> resultSet = new IResultSet<ApiProxy>(new IPageRequest<ApiProxy>() { 
			
			public IResponse<ApiProxy> execute(long offset) {
				String pageurl = url + "?offset=" + String. valueOf(offset); 
				IResponse<ApiProxy> page = exchange(pageurl, HttpMethod.GET, new HttpEntity<String>(getHeaders()),  new ParameterizedTypeReference<IResponse<ApiProxy>>() {} );
				return page;
			}
		});
		return resultSet;
	}
	
	public IResultSet<IEmailTemplate> getEmailTemplates() {
		final String url = getUrl()+"/"+getDomain()+"/emailtemplates";
		IResultSet<IEmailTemplate> resultSet = new IResultSet<IEmailTemplate>(new IPageRequest<IEmailTemplate>() {
			public IResponse<IEmailTemplate> execute(long offset) {
				String pageurl = url + "?offset=" + String. valueOf(offset); 
				IResponse<IEmailTemplate> page = exchange(pageurl, HttpMethod.GET, new HttpEntity<String>(getHeaders()),  new ParameterizedTypeReference<IResponse<IEmailTemplate>>() {} );
				return page;
			}
		});
		return resultSet;
	}
	
	public IRole getRole(String id) {
		return exchange(getUrl()+"/"+getDomain()+"/roles/" + id, HttpMethod.GET, new HttpEntity<String>(getHeaders()), IRole.class);
	}
	
	public IToken getToken() {
		return exchange(getUrl()+"/"+getDomain()+"/security/token", HttpMethod.POST, new HttpEntity<String>(getHeaders()), IToken.class);
	}
	
	public ITransaction create(ApiUser user) {
		return exchange(getCreateUrl(user), HttpMethod.POST, new HttpEntity<ApiUser>(user, getHeaders()), ITransaction.class);
	}
	
	public ITransaction create(ApiValue value) {
		return exchange(getCreateUrl(value), HttpMethod.POST, new HttpEntity<ApiValue>(value, getHeaders()), ITransaction.class);
	}
	
	public ITransaction update(ApiValue value) {
		return exchange(getUpdateUrl(value), HttpMethod.POST, new HttpEntity<ApiValue>(value, getHeaders()), ITransaction.class);
	}
	
	public IResultSet<ApiProxy> getGroups() {
		final String url = getUrl()+"/"+getDomain()+"/groups";
		IResultSet<ApiProxy> resultSet = new IResultSet<ApiProxy>(new IPageRequest<ApiProxy>() {
			public IResponse<ApiProxy> execute(long offset) {
				String pageurl = url + "?offset=" + String.valueOf(offset); 
				IResponse<ApiProxy> page = exchange(pageurl, HttpMethod.GET, new HttpEntity<String>(getHeaders()),  new ParameterizedTypeReference<IResponse<ApiProxy>>() {} );
				return page;
			}
		});
		return resultSet;
	}
	
	public IGroup getGroup(String id) {
		return exchange(getUrl()+"/"+getDomain()+"/groups/" + id, HttpMethod.GET, new HttpEntity<String>(getHeaders()), IGroup.class);
	}
	
	public IResultSet<ApiProxy> getRules() {
		final String url = getUrl()+"/"+getDomain()+"/security/rules";
		IResultSet<ApiProxy> resultSet = new IResultSet<ApiProxy>(new IPageRequest<ApiProxy>() {
			public IResponse<ApiProxy> execute(long offset) {
				String pageurl = url + "?offset=" + String.valueOf(offset); 
				IResponse<ApiProxy> page = exchange(pageurl, HttpMethod.GET, new HttpEntity<String>(getHeaders()),  new ParameterizedTypeReference<IResponse<ApiProxy>>() {} );
				return page;
			}
		});
		return resultSet;
	}
	
	public List<ILogEvent> getAudit(ApiFile file) {
		String uri = "/file/"+ file.getOId() + "/" + file.getId() + "/audit";
		return exchange(getUrl()+uri, HttpMethod.GET, new HttpEntity<String>(getHeaders()),  new ParameterizedTypeReference<List<ILogEvent>>() {} );
	}
	
	public List<ILogEvent> getAudit(ApiUser user) {
		String uri = "/users/"+ user.getId() + "/audit";
		return exchange(getUrl()+uri, HttpMethod.GET, new HttpEntity<String>(getHeaders()),  new ParameterizedTypeReference<List<ILogEvent>>() {} );
	}
	
	public List<ApiFile> getHistory(ApiFile file) {
		String uri = "/file/"+ file.getOId() + "/" + file.getId() + "/history";
		return exchange(getUrl()+uri, HttpMethod.GET, new HttpEntity<String>(getHeaders()),  new ParameterizedTypeReference<List<ApiFile>>() {} );
	}
	
	public ITransaction update(ApiFile file) {
		if (isMultipart(file))
			return updateMultipart(file);
		else 
		if (isBinary(file))
			return updateBinary(file);
		else	
			return exchange(getUrl()+"/file/new", HttpMethod.POST, new HttpEntity<ApiFile>(file, getHeaders()), ITransaction.class);
	}
	
	public ITransaction delete(ApiFile file) {
		return exchange(getUpdateUrl(file), HttpMethod.DELETE, new HttpEntity<ApiFile>(file, getHeaders()), ITransaction.class);
	}
	
	public ITransaction update(ApiUser user) {
		return exchange(getUpdateUrl(user), HttpMethod.POST, new HttpEntity<ApiUser>(user, getHeaders()), ITransaction.class);
	}
	
	public ITransaction delete(ApiUser user) {
		return exchange(getUpdateUrl(user), HttpMethod.DELETE, new HttpEntity<ApiUser>(user, getHeaders()), ITransaction.class);
	}
	
	public IResultSet<ApiFile> select(String criteria) {
		return select(criteria, false);
	}
	
	public ITransaction delete(String url) {
		return exchange(getUrl(url), HttpMethod.DELETE, new HttpEntity<String>(getHeaders()), ITransaction.class);
	}
	
	public ITransaction replicate(ApiObject object, String replicaId) {
		try {
			String value = restObjectMapper.writeValueAsString(object);
			String iclass = object.getClass().getName();
			IObjectReplica replica = new IObjectReplica();
			replica.setData(value);
			replica.setIclass(iclass);
			return exchange(getUrl()+"/"+getDomain()+"/replicate/"+replicaId, HttpMethod.POST, new HttpEntity<IObjectReplica>(replica, getHeaders()), ITransaction.class);
		}
		catch (Exception e) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.INTERNAL_ERROR, e.getMessage());
		}
	}
	
	public ITransaction freeze(ApiFile file) {
		String url = getUrl()+"/file/freeze/";
		url += String.valueOf(file.getOId()) + "/";
		url += String.valueOf(file.getId());
		return exchange(url, HttpMethod.POST, new HttpEntity<ApiFile>(file, getHeaders()), ITransaction.class);
	}
	
	public IResultSet<ApiFile> select(String criteria, boolean crc) {
		return select(criteria, getDomain(), 256, false, crc);
	}
	
	public IResultSet<ApiFile> select(String criteria, String domain, int pageSize) {
		return select(criteria, domain, pageSize, false, false);
	}
	
	public IResultSet<ApiFile> select(String criteria, String domain, int pageSize, boolean allstates, boolean crc) {
		final String url = getUrl()+"/file/"+domain+"/select?s="+criteria+(allstates?"&allstates=true":"")+(crc?"&crc=true":"");
		IResultSet<ApiFile> resultSet = new IResultSet<ApiFile>(new IPageRequest<ApiFile>() {
			public IResponse<ApiFile> execute(long offset) {
				if (offset==0) offset = 1;
				String pageurl = url + "&offset=" + String.valueOf(offset); 
				pageurl += "&pageSize=" + String.valueOf(pageSize); 
				IResponse<ApiFile> page = exchange(pageurl, HttpMethod.GET, new HttpEntity<String>(getHeaders()),  new ParameterizedTypeReference<IResponse<ApiFile>>() {} );
				return page;
			}
		});
		return resultSet;
	}
	
	@Deprecated
	public IResultSet<ApiFile> select2(String criteria, String domain, int pageSize, boolean allstates, boolean crc) {
		final String url = getUrl()+"/select?s="+criteria+(allstates?"&allstates=true":"")+(crc?"&crc=true":"");
		IResultSet<ApiFile> resultSet = new IResultSet<ApiFile>(new IPageRequest<ApiFile>() {
			public IResponse<ApiFile> execute(long offset) {
				if (offset==0) offset = 1;
				String pageurl = url + "&offset=" + String.valueOf(offset); 
				pageurl += "&pageSize=" + String.valueOf(pageSize); 
				IResponse<ApiFile> page = exchange(pageurl, HttpMethod.GET, new HttpEntity<String>(getHeaders()),  new ParameterizedTypeReference<IResponse<ApiFile>>() {} );
				return page;
			}
		});
		return resultSet;
	}

	
	public IResultSet<ApiFile> selectTask(String criteria) {
		final String url = getUrl()+"/selecttask?s="+criteria;
		IResultSet<ApiFile> resultSet = new IResultSet<ApiFile>(new IPageRequest<ApiFile>() {
			public IResponse<ApiFile> execute(long offset) {
				String pageurl = url + "&offset=" + String.valueOf(offset); 
				IResponse<ApiFile> page = exchange(pageurl, HttpMethod.GET, new HttpEntity<String>(getHeaders()),  new ParameterizedTypeReference<IResponse<ApiFile>>() {} );
				return page;
			}
		});
		return resultSet;
	}
	
	public InputStream getResource(String resourceurl) {
		resourceurl = UriUtils.encode(resourceurl, "UTF-8");
		resourceurl = resourceurl.replace("%2F", "/");
		String urlstr = getUrl()+resourceurl;
		urlstr = urlstr.replace(" ", "%20");
		try {
			String credential = getUser()+":"+getPassword();
			String encoded = new String(Base64.encodeBase64(credential.getBytes()));
			URL url = new URL(urlstr);
			
			URLConnection connection =  url.openConnection();
			connection.setRequestProperty ("Authorization", "Basic " + encoded);
			InputStream stream = connection.getInputStream();
			return stream;
		} 
		catch (IOException e) {
			logger.error(e);
		}
		return null;
	}
	
	public String getDomain() {
		String domain = null;
		int i = user.indexOf("@");
		if (i>0) domain = user.substring(i+1);
		return domain;
	}
	
	public ITransaction updateBinary(ApiFile file) {
		try {
			HessianProxyFactory factory = new HessianProxyFactory();
			
			
			factory.setUser(getUser());
			factory.setPassword(getPassword());
			factory.setReadTimeout(READ_TIMEOUT);
				
			String url = (getUrl()+"/fileService");
	
			ApiBinaryService fileService = (ApiBinaryService) factory.create(ApiBinaryService.class, url);
			
			List<IBinaryResource> binaryresources = new ArrayList<IBinaryResource>();
			
			for (ApiResource resource : file.getResources()) {
				if (resource instanceof IBinaryResource) {
					binaryresources.add((IBinaryResource)resource);
				}
			}

			ITransaction transaction = null;
			
			if (!binaryresources.isEmpty()) {
				if (binaryresources.size()==1) {
					IBinaryResource resource = binaryresources.get(0);
					InputStream stream = resource.getStream();
					resource.setStream(null);
					transaction = fileService.update1(file, stream);
				}
				else {
					IBinaryResource resource = getZip(binaryresources);
					InputStream stream = resource.getStream();
					resource.setStream(null);
					List<ApiResource> resources = new ArrayList<ApiResource>();
					resources.add(resource);
					file.setResources(resources);
					transaction = fileService.zipupdate(file, stream);
				}
			}
			else {
				transaction = fileService.update(file);
			}
	
			return transaction;
		}
		catch (IOException e) {
			logger.error(e);
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.FILE_NOT_FOUND, e.getMessage());
		}
	}
	
	public ITransaction updateMultipart(ApiFile file) {
		try {
			List<ApiResource> resources = new ArrayList<ApiResource>();
			for (ApiResource multipart : file.getResources()) {
				if (multipart instanceof IMultipartResource) {
					ApiResource resource = upload(((IMultipartResource)multipart).getFile());
					resources.add(resource);
				}
				else {
					if (multipart.getId()!=null) {
						resources.add(multipart);
					}
				}
			}
			file.setResources(resources);
			ITransaction response = update(file);
			return response;
		}
		catch (IOException e) {
			logger.error(e);
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.FILE_NOT_FOUND, e.getMessage());
		}
		
	}

	
	public ApiResource upload(File file) throws IOException {
		
		
		HttpMultipart request = getMultipart("/binfile/upload", null);
		
		if (getChunk()>0) request.setChunk(getChunk());
		
        ITransaction upload = request.exchange(new HttpFileEntity(file), new com.fasterxml.jackson.core.type.TypeReference<ITransaction>() {});

		ApiResource iresource = new ApiResource();
		
		iresource.setHRef(upload.getTarget().getHRef());
		iresource.setId(upload.getTarget().getId());
		
		return iresource;
//		MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
//		FileSystemResource resource = new FileSystemResource(file);
//		map.add("file", resource);
//
//		HttpHeaders headers = getHeaders();
//		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//		
//		HttpEntity<MultiValueMap<String, Object>> httpentity = new HttpEntity<MultiValueMap<String, Object>>(map, headers);
//		
//		ITransaction transaction = exchange(getUrl()+"/binfile/upload", HttpMethod.POST, httpentity, ITransaction.class);
//		
//		IResource iresource = new IResource();
//		
//		iresource.setHRef(transaction.getTarget().getHRef());
//		iresource.setId(transaction.getTarget().getId());
//		
//		return iresource;
	}
	
	protected <T> T exchange(String url, HttpMethod method,	HttpEntity<?> requestEntity, Class<T> responseType) throws RestClientException {
		try {
			ResponseEntity<T> response = restTemplate.exchange(url, method, requestEntity, responseType);
			return response.getBody();
		}
		catch (HttpClientErrorException e) {
			logger.error(e);
			throw new RuntimeException(e);
			
			//throw new ApiException(e.getStatusCode(), e.getMessage());
		}
		catch (HttpServerErrorException e) {
			IError error = getError(e);
			logger.error(e);
			if (error!=null) {
				throw new ApiException(e.getStatusCode(), error.getCode(), error.getMessage());
			}
			else {
				throw new ApiException(e.getStatusCode(), e.getMessage());
			}
		}
	}
	
	protected <T> T exchange(String url, HttpMethod method,	HttpEntity<?> requestEntity, ParameterizedTypeReference<T> responseType) throws RestClientException {
		try {
			ResponseEntity<T> response = restTemplate.exchange(url, method, requestEntity, responseType);
			return response.getBody();
		}
		catch (HttpClientErrorException e) {
			logger.error(e);
			IError error = getError(e);
			if (error!=null) {
				throw new ApiException(e.getStatusCode(), error.getCode(), error.getMessage());
			}
		}
		catch (HttpServerErrorException e) {
			logger.error(e);
			IError error = getError(e);
			if (error!=null) {
				throw new ApiException(e.getStatusCode(),error.getCode(), error.getMessage());
			}
		}
		return null;
	}
	
	protected String getUrl(String url) {
		return getUrl() + url;
	}
	
	protected String getUpdateUrl(ApiFile file) {
		String url = getUrl();
		
		url += "/file/";
		
		if (file.getApplication()==null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.INVALID_APPLICATION);
		}
		
		url += file.getApplication() + "/";
		
		if (file.getDomain()==null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.DOMAIN_NOT_FOUND);
		}
		
		url += file.getDomain() + "/";
		
		if (file.getExternalId()==null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.FILE_NOT_FOUND);
		}
		
		url += file.getExternalId();
		
		return url;
	}
	
	protected String getUpdateUrl(ApiUser user) {
		String url = getUrl();
		
		if (user.getDomain()==null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.DOMAIN_NOT_FOUND);
		}
		
		url += "/" + user.getDomain() + "/users/" + user.getId();
		
		return url;
	}
	
	protected String getCreateUrl(ApiUser user) {
		String url = getUrl();
		
		if (user.getDomain()==null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.DOMAIN_NOT_FOUND);
		}
		
		url += "/" + user.getDomain() + "/users/newuser";
		
		return url;
	}
	
	protected String getCreateUrl(ApiValue value) {
		String url = getUrl();
		
		if (value.getDomain()==null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.DOMAIN_NOT_FOUND);
		}
		
		if (value.getDataSet()==null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.DATASET_NOT_FOUND);
		}
		
		ApiDataSet dataset = get(ApiDataSet.class, value.getDataSet().getHRef());
		
		if (dataset==null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.DATASET_NOT_FOUND);
		}
		
		url += "/" + value.getDomain() + "/datasets/"+dataset.getDisplayName().toLowerCase()+"/values/newvalue";
		
		return url;
	}
	
	protected String getUpdateUrl(ApiValue value) {
		String url = getUrl();
		
		if (value.getDomain()==null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.DOMAIN_NOT_FOUND);
		}
		
		if (value.getDataSet()==null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.DATASET_NOT_FOUND);
		}
		
		ApiDataSet dataset = get(ApiDataSet.class, value.getDataSet().getHRef());
		
		if (dataset==null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.DATASET_NOT_FOUND);
		}
		
		String valueId = value.getId()==null ? "newvalue" : value.getId();
		
		url += "/" + value.getDomain() + "/datasets/"+dataset.getAlias().toLowerCase()+"/values/"+valueId;
		
		return url;
	}

	protected boolean isBinary(ApiFile file) {
		for (ApiResource resource : file.getResources()) {
			if (resource instanceof IBinaryResource)
				return true;
		}
		return false;
	}
	
	protected boolean isMultipart(ApiFile file) {
		for (ApiResource resource : file.getResources()) {
			if (resource instanceof IMultipartResource)
				return true;
		}
		return false;
	}
	
    protected HttpMultipart getMultipart(String uri, ProgressListener listener) {
		String plainCredentials=getUser()+":"+getPassword();
        return new HttpMultipart(getUrl()+uri, plainCredentials, listener);
    }
	
	protected List<IBinaryResource> getBinaryResources(ApiFile file) {
		List<IBinaryResource> resources = new ArrayList<IBinaryResource>();
		for (ApiResource resource : file.getResources()) {
			if (resource instanceof IBinaryResource) {
				resources.add((IBinaryResource)resource);
			}	
		}
		return resources;
	}
	
	protected HttpHeaders getHeaders(){
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + getCredentials());
		headers.add("Device", "XXXXX");
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		return headers;
	}
	
	protected String getCredentials(){
		String plainCredentials=getUser()+":"+getPassword();
		String base64Credentials = new String(Base64.encodeBase64(plainCredentials.getBytes()));
		return base64Credentials;
	}
	
	protected IError getError(HttpStatusCodeException e) {
		try {
			return restObjectMapper.readValue(e.getResponseBodyAsString(), IError.class);
		}
		catch (Exception e1) {
			logger.error(e1);
			return null;
		}
	}
	
	private IBinaryResource getZip(List<IBinaryResource> resources) {
		ZipOutputStream zipout = null;
		try {
			File zip = File.createTempFile("tmp", ".zip");
			zip.deleteOnExit();
			
			zipout = new ZipOutputStream(new FileOutputStream(zip));
			
			for (IBinaryResource resource : resources) {
				ZipEntry entry = new ZipEntry(resource.getName());
				zipout.putNextEntry(entry);
					
				int len;
				byte[] buffer = new byte[2048];
				InputStream stream = resource.getStream();
				while ((len = stream.read(buffer)) > 0) {
					zipout.write(buffer, 0, len);
				}
				stream.close();
			}
			
			return new IBinaryResource(zip);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			try {
				if (zipout!=null) zipout.close();
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
