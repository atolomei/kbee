package com.novamens.kbee.content.webapi.type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentResource;
import com.novamens.content.base.CustomAttribute;
import com.novamens.content.base.Relation;
import com.novamens.content.base.Resource;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormAccessLevel;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EIdentifiableForm;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.TokenService;
import com.novamens.content.service.UrlService;
import com.novamens.kbee.content.base.KbeeResourceTag;
import com.novamens.kbee.content.document.KbeeIDoc;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.content.resource.KBeeFileProxy;
import com.novamens.kbee.content.resource.KbeeExternalResource;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiFile;
import kbee.api.model.ApiProxy;
import kbee.api.model.ApiValue;
import kbee.api.model.ApiViewMode;
import kbee.api.model.ApiAttributeProxy;
import kbee.api.model.IAttributeValues;
import kbee.api.model.IFieldData;
import kbee.api.model.IFormData;
import kbee.api.model.ApiResource;
import kbee.util.logging.Logger;

public class IDocAdapter implements Adapter<KbeeIDoc, ApiFile> {
	
	private static Logger logger = Logger.getLogger(IDocAdapter.class.getName());
	
	private boolean includecrc;
	private boolean includesecuritytokens;
	private boolean includeurl;
	private boolean includetags;
	private ApiViewMode viewMode;
	private String version;
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	public IDocAdapter(boolean includecrc) {
		this("0", ApiViewMode.All, includecrc, false, false);
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	public IDocAdapter(String version,
			ApiViewMode viewMode,
			boolean includecrc, 
			boolean includesecuritytokens, 
			boolean includeurl) {
		this.version = version;
		this.includecrc = includecrc;
		this.includesecuritytokens = includesecuritytokens;
		this.includeurl = includeurl;
		this.viewMode = viewMode;
		this.includetags = !"0".equals(version);
	}
	
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	public ApiFile adapt(KbeeIDoc kbeeidoc) {
		ApiFile idoc = new ApiFile();
		idoc.setId(String.valueOf(kbeeidoc.getId()));
		idoc.setOId(String.valueOf(kbeeidoc.getOId()));
		idoc.setVersion(kbeeidoc.getVersion());
		idoc.setExternalId(kbeeidoc.getExternalId());
		
		idoc.setTitle(kbeeidoc.getTitle());
		String subtitle = kbeeidoc.getService(ContentService.class).getPortalSubtitle();
		idoc.setSubtitle(subtitle);
		
		idoc.setDomain(kbeeidoc.getDomain().getName());
		idoc.setDomainRef(new ApiProxy(String.valueOf(kbeeidoc.getDomain().getId()), 
				kbeeidoc.getDomain().getName(), 
				UriHelper.getUri(kbeeidoc.getDomain()), 
				"domain"));
		idoc.setApplication(kbeeidoc.getSource()!=null ? kbeeidoc.getSource().getName() : null);
		idoc.setClassName(kbeeidoc.getContentTemplate().getName());
		idoc.setContentClass(new ApiProxy(kbeeidoc.getContentTemplate().getDisplayName(), UriHelper.getUri(kbeeidoc.getContentTemplate()), "template"));
		idoc.setState(kbeeidoc.getState().name());
		idoc.setLastModifiedDate(kbeeidoc.getLastModifiedOffsetDateTime());
		idoc.setLastModifiedUser(new ApiProxy(kbeeidoc.getLastModifiedUser().getDisplayName(), UriHelper.getUri(kbeeidoc.getLastModifiedUser()), "user"));
		if (kbeeidoc.getPreviousVersion()!=null)
		idoc.setPreviousVersion(new ApiProxy(kbeeidoc.getPreviousVersion().getTitle(), UriHelper.getUri(kbeeidoc.getPreviousVersion()), "file"));

		if (ApiViewMode.All.equals(viewMode) ||
			ApiViewMode.Grid.equals(viewMode)) {
			List<IAttributeValues> values = new ArrayList<>();
			values.addAll(getClassification(kbeeidoc));
			for (Relation relation : kbeeidoc.getRelations()) {
				ApiProxy relationproxy = new ApiProxy(
					relation.getTarget().getTitle(), 
					UriHelper.getUri(relation.getTarget()), relation.getTemplate().getName());
				idoc.addRelation(relationproxy);
			}
			values.addAll(getAttributes(kbeeidoc));
			idoc.setAttributes(values);
			for (CustomAttribute attribute : kbeeidoc.getUserDefinedAttributes()) {
				idoc.setCustomAttribute(attribute.getName(), attribute.getValue());
			}
		}	
			
		if (ApiViewMode.All.equals(viewMode)) {
			idoc.setResources(getResources(kbeeidoc));
		}
		
		if (includeurl) {
			idoc.setCustomAttribute("publicurl", 
				kbeeidoc.getService(UrlService.class).getPublicUrl());
			idoc.setControlAttribute("publicurl", 
				kbeeidoc.getService(UrlService.class).getPublicUrl());
		}
			
		if (ApiViewMode.All.equals(viewMode) && 
			getVersion()!=null && 
			!"0".equals(getVersion())) {
			idoc.setForms(getForms(kbeeidoc));
		}

		return idoc;	
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected String getCRC(Resource resource) {
		try {
			if (resource instanceof KBFile) {
				KBFile file = (KBFile)resource;
				long crc32 = org.apache.commons.io.FileUtils.checksumCRC32(file.getFile());
				return String.valueOf(crc32);
			}
			else {
				return null;
			}
		}
		catch (IOException e) {
			return null;
		}
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	private List<IAttributeValues> getClassification(KbeeIDoc kbeeidoc) {
		List<IAttributeValues> values = new ArrayList<>();
		for (Classification classification : kbeeidoc.getClassification()) {
			if (classification!=null) {
				Classifier classifier = classification.getClassifier();
				String classifieruri = UriHelper.getUri(classifier);
				IAttributeValues attributevalues = null;
				for (IAttributeValues previousattributevalues : values) {
					if (previousattributevalues.getAttribute().getHRef().equals(classifieruri)) {
						attributevalues = previousattributevalues;
						break;
					}
				}
				if (attributevalues==null) {
					ApiAttributeProxy attribute = new ApiAttributeProxy();
					attribute.setId(String.valueOf(classifier.getId()));		
					attribute.setHRef(UriHelper.getUri(classifier));		
					attribute.setRel("classifier");
					attribute.setName(classifier.getName());
					attributevalues = new IAttributeValues(attribute);
					values.add(attributevalues);
				}
				DataSetMember member = classification.getDataSetMember();
				ApiValue value = new ApiValue();
				value = (new IValueAdapter()).adapt(member);
				attributevalues.addValue(value);
			}
		}		
		return values;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	private List<IAttributeValues> getAttributes(KbeeIDoc kbeeidoc) {
		List<IAttributeValues> values = new ArrayList<>();
		for (AttributeTemplate template : kbeeidoc.getContentTemplate().getAttributes()) {
			for (String value : kbeeidoc.getAttributeValues(template.getAttribute())) {
				ApiAttributeProxy iattribute = new ApiAttributeProxy();
				iattribute.setId(String.valueOf(template.getAttribute().getId()));		
				iattribute.setHRef(UriHelper.getUri(template.getAttribute()));		
				iattribute.setRel("attribute");
				iattribute.setName(template.getAttribute().getName());
				
				if (template.getAttribute().isDate()) {
					int t = value.indexOf("T");
					if (t>0) {
						value = value.substring(0, t);
					}				
				}
				
				ApiValue ivalue = new ApiValue();
				ivalue.setValue(value);
				
				values.add(new IAttributeValues(iattribute, ivalue));
			}
		}	
		return values;
	}
	
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	private List<ApiResource> getResources(KbeeIDoc kbeeidoc) {
		List<ApiResource> files = new ArrayList<>();
		for (ContentResource contentresource : kbeeidoc.getContentResources()) {
			Resource resource = contentresource.getResource();
			ApiResource file = new ApiResource();
			if (includecrc) {
				String crc = getCRC(resource);
				file.setCRC(crc);
			}
			if (includesecuritytokens) {
				file.setHRef(UriHelper.getUri(resource));
			}
			else {
				file.setHRef(UriHelper.getUri(kbeeidoc, resource));
			}
			file.setId(String.valueOf(resource.getId()));
			file.setDomain(kbeeidoc.getDomain().getName());
			file.setName(resource.getName());
			
			KbeeResourceTag tag = (KbeeResourceTag)contentresource.getTag();
			if (tag!=null && includetags) {
				file.setTag(new ApiProxy(String.valueOf(tag.getId()), tag.getName(), UriHelper.getUri(tag), "resourcetag"));
			}

			if (resource instanceof KBFileImpl) {
				User user = ((KBFileImpl)resource).getLastModifiedUser(); 
				file.setLastModifiedUser(new ApiProxy(user.getDisplayName(), UriHelper.getUri(user), "user" ));
				file.setLastModifiedDate(((KBFileImpl)resource).getLastModifiedOffsetDateTime());
				file.setRel("file");
				if (resource instanceof KBeeFileProxy) {
					file.setControlAttribute("proxy-url", ((KBeeFileProxy)resource).getUrl());
				}
				else {
					String proxyurl = getProxyUrl(kbeeidoc);
					if (proxyurl!=null) {
						file.setControlAttribute("proxy-url", proxyurl);
					}
				}
				file.setDomainRef(new ApiProxy(String.valueOf(kbeeidoc.getDomain().getId()), 
						kbeeidoc.getDomain().getName(), 
						UriHelper.getUri(kbeeidoc.getDomain()), 
						"domain"));
				file.setControlAttribute("storageType", ((KBFileImpl)resource).getStorageType().getKey());
				file.setControlAttribute("bucketName", ((KBFileImpl)resource).getBucketName());		
				file.setControlAttribute("objectName", ((KBFileImpl)resource).getObjectName());
				file.setControlAttribute("encrypted", String.valueOf(((KBFileImpl)resource).getIsEncrypted()));
			}
			else {
				if (resource instanceof KbeeExternalResource) {
					User user = ((KbeeExternalResource)resource).getLastModifiedUser(); 
					file.setLastModifiedUser(new ApiProxy(user.getDisplayName(), UriHelper.getUri(user), "user" ));
					file.setLastModifiedDate(((KbeeExternalResource)resource).getLastModifiedOffsetDateTime());
					file.setRel("link");
				}
			}
			
			files.add(file);
		}
		
		if (kbeeidoc.getResources().isEmpty()) {
			ApiFile sourcefile = getSourceFile(kbeeidoc);
			if (sourcefile!=null) {
				files = sourcefile.getResources();
			}
		}
		
		return files;
	}

	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	private List<IFormData> getForms(Content content) {
		List<IFormData> forms = new ArrayList<IFormData>();
		try {
			for (EForm form : content.getContentTemplate().getForms()) {
				if (!viewMode.equals(ApiViewMode.Site) || 
						form.getFormAccessLevel().equals(EFormAccessLevel.GENERAL) || 
						form.getFormAccessLevel().equals(EFormAccessLevel.GENERAL_PORTAL) ||
						form.getFormAccessLevel().equals(EFormAccessLevel.GENERAL_LIBRARY)) {
					KbeeTaskForm taskform = new KbeeTaskForm(form);
					IFormData idata = new IFormData();
					EFormData data = content.getFormData(taskform);
					if (form.getViewer()!=null)
					idata.setUrl(getUrl(content, form));
					ApiProxy iform = new ApiProxy();
					iform.setId(String.valueOf(((EIdentifiableForm)form).getId()));
					iform.setName(form.getDisplayName());
					idata.setForm(iform);
					idata.setSigned(data.isSigned());
					idata.setFile(new ApiProxy(String.valueOf(content.getId()), content.getTitle(), UriHelper.getUri(content), "content"));
					idata.setData(getData(data));
					forms.add(idata);
				}
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
		return forms;
	}
	
	private List<IFieldData> getData(EFormData formdata) {
		List<IFieldData> data = (new IFormDataAdapter()).adapt(formdata);
		return data;
	}
	
	private String getUrl(Content content, EForm form) {
		KbeeJson data = new KbeeJson();
		data.put("content", String.valueOf(content.getId()));
		data.put("form", String.valueOf(((EIdentifiableForm)form).getId()));
		data.put("user", getSessionUser().getName());
		return "/sharedform/" + ServiceLocator.getService(TokenService.class).getToken(data);
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	private String getProxyUrl(KbeeIDoc idoc) {
		String json = (String)idoc.getService(PropertyService.class).getProperty("file");
		if (json==null) return null;
		GsonBuilder b = new GsonBuilder();
		Gson gson = b.create();
		ApiFile file = gson.fromJson(json, ApiFile.class);
		for (ApiResource resource : file.getResources()) {
			return resource.getHRef();
		}
		return null;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	private ApiFile getSourceFile(KbeeIDoc idoc) {
		String json = (String)idoc.getService(PropertyService.class).getProperty("file");
		if (json==null) return null;
		GsonBuilder b = new GsonBuilder();
		Gson gson = b.create();
		ApiFile file = gson.fromJson(json, ApiFile.class);
		return file;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
