package com.novamens.kbee.content.webapi.handler;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.CustomAttribute;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.base.Source;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.ContentService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.content.base.KbeeCustomAttribute;
import com.novamens.kbee.content.webapi.type.UriHelper;
import com.novamens.kbee.lock.LockTransactionSynchronization;
import com.novamens.kbee.system.SystemParameters;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiFile;
import kbee.api.model.ApiProxy;
import kbee.api.model.ICustomAttributeValue;
import kbee.api.model.ApiResource;
import kbee.api.model.ITransaction;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;

public abstract class FileUpdateAbstractHandler extends ClassificableUpdateHandler {
	
	static private Logger logger = LogManager.getLogger(FileUpdateAbstractHandler.class.getName());

	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	public ITransaction update(ApiFile file) {
		return null;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	public ITransaction update(ApiFile file, InputStream stream) {
		return null;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	public ITransaction zipupdate(ApiFile file, InputStream stream) {
		return null;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected Content getOrCreateExternalContent(ApiFile file) {
		Content content = getContentDao().findContentByExternalId(file.getApplication(), file.getExternalId());
		if (file.getExternalId()==null || "".equals(file.getExternalId())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.ATTRIBUTE_IS_REQUIRED, "externalid");
		}
		if (content == null ) {
			content = createContent(file);
		}
		else {
			if (!content.getDomain().getName().equals(file.getDomain())) {
				throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.INVALID_DOMAIN, content.getDomain().getName(), file.getDomain());
			}
			else {
				String classname = getContentTemplate(file);
				if (!content.getContentTemplate().getName().toLowerCase().equals(classname.toLowerCase())) {
					throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.INVALID_CLASS, classname);
				}
				else
				if (content.getSource()!=null && !content.getSource().getName().toLowerCase().equals(file.getApplication().toLowerCase())) {
					throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.INVALID_SOURCE, classname);
				}
				else
				if (content.isHeadVersion()) {
					if (content.isRecycled()) {
						content.getService(ContentService.class).restore();
					}
					content = content.getService(ContentService.class).checkout();
				}	
				else {
					if (!content.getWorkspace().equals(getUser().getId())) {
						throw new ApiException(HttpStatus.LOCKED, ApiError.LOCKED, content.getDomain().getName(), file.getDomain());
					}
				}
				getContentDao().flush();
			}
		}
		return content;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected Content getOrCreateContent(ApiFile file) {
		Content content = null;
		
		if (file.getId()==null) {
			content = createContent(file);
			return content;
		}
		
		if (!isDigits(file.getId())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.NO_DATA, null, file.getDomain());
		}
		
		content = getContentDao().findContentById(Long.valueOf(file.getId()));
		
		if (content==null) {
			throw new ApiException(HttpStatus.NOT_FOUND, ApiError.NO_DATA, null, file.getDomain());
		}
		
		if (!content.getDomain().getName().equals(file.getDomain())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.INVALID_DOMAIN, content.getDomain().getName(), file.getDomain());
		}
		
		String classname = getContentTemplate(file);
		if (!content.getContentTemplate().getName().toLowerCase().equals(classname.toLowerCase())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.INVALID_CLASS, classname);
		}
		
		if (content.getSource()!=null && !content.getSource().getName().toLowerCase().equals(file.getApplication().toLowerCase())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.INVALID_SOURCE, classname);
		}
		
		if (content.isHeadVersion()) {
			if (content.isRecycled()) {
				content.getService(ContentService.class).restore();
			}
			content = content.getService(ContentService.class).checkout();
		}	
		
		if (!content.getWorkspace().equals(getUser().getId())) {
			throw new ApiException(HttpStatus.LOCKED, ApiError.LOCKED, content.getDomain().getName(), file.getDomain());
		}
		
		getContentDao().flush();
		
		return content;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected Content createContent(ApiFile file) {
		try {
			String classname = getContentTemplate(file);
			if (classname==null) {
				throw new ApiException(HttpStatus.PRECONDITION_FAILED, ApiError.INTERNAL_ERROR, file.getClassName());
			}
			Content content = ServiceLocator.getService(ContentFactoryService.class).create(classname, false, true);
			content.setCheckinOffsetDateTime(OffsetDateTime.now());
			getContentDao().save(content);
			
			String externalId = file.getExternalId();
			if (externalId!=null) externalId = externalId.toLowerCase();
			((KbeeContent)content).setExternalId(externalId);
			
			String sourcename = file.getApplication();
			if (sourcename!=null) {
				Source source = getContentDao().findSourceByName(sourcename);
				if (source==null) {
					new LockTransactionSynchronization(sourcename);
					source = getContentDao().findSourceByName(sourcename);
					if (source == null) {
						source = ServiceLocator.getService(ContentFactoryService.class).createSource(sourcename, sourcename, content.getDomain());
					}
				}
				((KbeeContent)content).setSource(source);
			}
			
			return content;
		}
		catch (ContentCreationException | ContentMgmtException e) {
			logger.error(e);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected String getContentTemplate(ApiFile file) {
		if (file.getClassName()==null || "".equals(file.getClassName().trim())) {
			String template = SystemParameters.get("com.novamens.content.webapi.contenttemplate", "File");
			return template;
		}
		else {
			String name = file.getClassName().toLowerCase();
			for (ContentTemplate template : getContentDao().getTemplates(getDomain(file))) {
				if (template.getName().toLowerCase().equals(name) || (template.getAlias()!=null && template.getAlias().toLowerCase().equals(name))) {
					return template.getName();
				}
			}
			
			return null;
		}
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	*/
	protected String getDefaultTemplate() {
		return SystemParameters.get("com.novamens.content.webapi.contenttemplate", "File");
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected List<String> setResources(Content content, List<KBFile> files) throws ContentMgmtException {
		List<String> updates = new ArrayList<String>();

		for (KBFile contentfile : ((ResourceContainer)content).getFiles()) {
			boolean infiles = false;
			for (KBFile kbfile : files) {
				if (kbfile.getUrl().equals(contentfile.getUrl())) {
					infiles = true;
				}
			}
			if (!infiles) {
				((ResourceContainer)content).removeFile(contentfile);
				updates.add("remove "+contentfile.getName());
			}
		}
		for (KBFile kbfile : files) {
			boolean incontent = false;
			for (KBFile contentfile : ((ResourceContainer)content).getFiles()) {
				String url1 = kbfile.getUrl();
				String url2 = contentfile.getUrl();
				if (url1.equals(url2)) {
					incontent = true;
				}
			}
			if (!incontent) {
				getContentDao().save(kbfile);
				((ResourceContainer)content).addFile(kbfile, getTag(content, kbfile));
				updates.add("add "+kbfile.getName());
			}
		}
		
		return updates;
	}
	
	protected ResourceTag getTag(Content content, KBFile file) {
		ResourceTag selectedtag = null;
		for (ResourceTag tag : content.getContentTemplate().getResourceTags()) {
			if (selectedtag==null) {
				selectedtag = tag;
			}
			if (tag.isDefault()) {
				selectedtag = tag;
				break;
			}
		}
		return selectedtag;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected List<String> setCustomAttributes(Content content, ApiFile file) {
		List<String> updates = new ArrayList<String>();
		List<CustomAttribute> attributes = content.getUserDefinedAttributes();
		List<CustomAttribute> newattributes = new ArrayList<CustomAttribute>();
		boolean updated = false;
		if (file.getCustomAttributes()==null) return updates;
		for (ICustomAttributeValue attributevalue : file.getCustomAttributes()) {
			String attribute = attributevalue.getAttribute();
			String value = attributevalue.getValue();
			if (attribute!=null && value!=null && !"".equals(value.trim())) {
				newattributes.add(new KbeeCustomAttribute(attribute, value));
			}
		}
		if ((attributes!=null && attributes.size()!=newattributes.size()) || (attributes==null && !newattributes.isEmpty())) {
			updated = true;
		}
		if (!updated && attributes!=null) {
			for (int i=0; i<attributes.size(); i++) {
				CustomAttribute attribute = attributes.get(i);
				CustomAttribute newattribute = newattributes.get(i);
				if (!attribute.equals(newattribute)) {
					updated=true;
					break;
				}
			}
		}
		if (updated) {
			content.setUserDefinedAttributes(newattributes);
			updates.add("User Attributes");
		}
		return updates;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected List<String> setTitle(Content content, ApiFile file) {
		List<String> updates = new ArrayList<String>();
		if (!equals(content.getTitle(), file.getTitle())) {
			content.setTitle(file.getTitle());
			updates.add("Title");
		}
		return updates;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected List<String> setState(Content content, ApiFile file) {
		List<String> updates = new ArrayList<String>();
		if (file.getState()!=null) {
			try {
				ObjectState state = ObjectState.valueOf(file.getState());
				//if (content.getState().equals(state)) {
					content.setState(state);
					updates.add("State");
				//}
			}
			catch (Exception e) {
			}
		}
		return updates;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected List<String> setWorkspace(Content content, ApiFile file) {
		List<String> updates = new ArrayList<String>();
		content.setWorkspace((long)getUser().getId());
		updates.add("Workspace");
		return updates;
	}	
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected boolean changeResources(Content content, ApiFile file){
		List<String> fileresources = new ArrayList<String>();
		List<String> contentresources = new ArrayList<String>();
		for (ApiResource resource : file.getResources()) {
			fileresources.add(resource.getControlAttributeValue("name"));
		}
		for (Resource resource : ((ResourceContainer)content).getResources()) {
			contentresources.add(resource.getName());
		}
		if (fileresources.size()!=contentresources.size())
			return true;
		for (String resource : fileresources) {
			if (!contentresources.contains(resource)) {
				return true;
			}
		}
		return false;
	}

	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Override
	protected List<Classifier> getClassifiers(Classificable classificable){
		List<Classifier> classifiers = new ArrayList<Classifier>();
		for (ClassifierTemplate template : ((Content)classificable).getContentTemplate().getClassifiers()) {
			classifiers.add(template.getClassifier());
		};
		return classifiers;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Override
	protected List<AttributeTemplate> getAttributes(Classificable classificable) {
		return ((Content)classificable).getContentTemplate().getAttributes();
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected ApiProxy getProxy(ApiFile file) {
		ApiProxy proxy = new ApiProxy(file.getTitle(), UriHelper.getUri(file));
		proxy.setId(file.getId());
		return proxy;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	private boolean isDigits(String argument) {
		for (int c = 0; c < argument.length(); c++) {
			if (!Character.isDigit(argument.charAt(c))) {
				return false;
			}
		}
		return true;
	}
}