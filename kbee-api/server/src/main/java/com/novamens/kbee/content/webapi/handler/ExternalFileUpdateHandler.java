package com.novamens.kbee.content.webapi.handler;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.Relation;
import com.novamens.content.base.Resource;
import com.novamens.content.base.Source;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentService;
import com.novamens.kbee.lock.LockTransactionSynchronization;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.content.model.KbeeRelation;
import com.novamens.kbee.system.SystemParameters;

import kbee.api.model.ApiFile;
import kbee.api.model.ApiProxy;
import kbee.api.model.ApiResource;
import kbee.api.model.ITransaction;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;

public class ExternalFileUpdateHandler extends FileUpdateAbstractHandler {
	
	static private Logger logger = LogManager.getLogger(FileUpdateAbstractHandler.class.getName());
	static private kbee.util.logging.Logger kblogger = new kbee.util.logging.Logger(logger);
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Override
	@Transactional
	public ITransaction update(ApiFile file) {
		
		String externalId = null;
		
		try {
			Domain domain = getDomain(file);
			
			if (domain == null) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.DOMAIN_NOT_FOUND);
			}
			
			if (file.getApplication()==null) {
				throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.INVALID_APPLICATION);
			}

			su(domain);
			
			if (file.getExternalId()==null || "".equals(file.getExternalId())) {
				throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.ATTRIBUTE_IS_REQUIRED, "externalid");
			}
			
			externalId = file.getExternalId();
			
			new LockTransactionSynchronization(externalId);
			
			Content content = getOrCreateExternalContent(file);
			
			if (!file.getResources().isEmpty()) {
				ApiResource resource = file.getResources().get(0);
				String lastmodifiedvalue = resource.getControlAttributeValue("lastModifedDate");
				if (lastmodifiedvalue!=null) {
					try {
						OffsetDateTime lastmodifieddate = OffsetDateTime.parse(lastmodifiedvalue, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
						file.setLastModifiedDate(lastmodifieddate);
					}
					catch (DateTimeParseException e) {
						logger.error("date time error for "+file.getExternalId() + " "+lastmodifiedvalue);
					}
				}
			}
			
			if (content.getExternalTime()!=null && file.getLastModifiedDate()!=null && content.getExternalTime().isAfter(file.getLastModifiedDate())) {
				throw new ApiException(HttpStatus.ALREADY_REPORTED, ApiError.INVALID_VERSION);
			}
			
			List<String> updates = new ArrayList<String>();
			
			updates.addAll(setState(content, file));
			
			updates.addAll(setTitle(content, file));
			
			updates.addAll(setAttributes((Classificable)content, file));
			
			updates.addAll(setCustomAttributes(content, file));
			
			updates.addAll(setRelations(content, file));
			
			updates.addAll(setResources(content, file));
			
			if (updates.isEmpty()) {
				throw new ApiException(HttpStatus.NOT_MODIFIED, ApiError.NOT_MODIFIED);
			}
			
			if (!isWriteable(content)) {
				throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
			}
			
			((KbeeContent)content).setExternalTime(file.getLastModifiedDate());
			
			content.getService(PropertyService.class).setProperty("file", toJson(file));
			
			content.getService(ContentService.class).update(updates);
			
			getContentDao().flush();
			
			if (!content.isHeadVersion()) {
				content.getService(ContentService.class).checkin();
			}
		
			ITransaction transaction  = getTransaction(getProxy(file));
			
			return transaction;
		}
		catch (ApiException e) {
			throw e;
		}
		catch (ContentMgmtException e) {
			kblogger.error(e);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
		catch (Exception e) {
			kblogger.error(e);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}	
	}
	


	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected List<String> setRelations(Content content, ApiFile file) {
		List<String> updates = new ArrayList<String>();
		
		List<Relation> relations = new ArrayList<Relation>();
		for (ApiProxy relationproxy : file.getRelationships()) {
			if (relationproxy.getRel()!=null) {
				RelationTemplate template = getRelation(content, relationproxy.getRel());
				
				if (template==null) {
					throw new ApiException(HttpStatus.PRECONDITION_FAILED, ApiError.INVALID_RELATION, relationproxy.getRel());
				}
				
				String targetId = getId(relationproxy.getHRef());
				
				if (targetId==null) {
					throw new ApiException(HttpStatus.NOT_FOUND, ApiError.INVALID_RELATION);
				}	
				
				new LockTransactionSynchronization(targetId);

				Content target = getFile(content.getSource(), targetId);
				
				if (target==null || !target.getDomain().equals(content.getDomain())) {
					throw new ApiException(HttpStatus.NOT_FOUND, ApiError.INVALID_RELATION, relationproxy.getHRef());
				}
				
				KbeeRelation relation = new KbeeRelation();
				relation.setTemplate(template);
				relation.setTarget(target);
				relations.add(relation);
			}
		}

		boolean update = false;
		if (content.getRelations().size()==relations.size()) {
			for (Relation relation1 : content.getRelations()) {
				boolean found = false;
				for (Relation relation2 : relations) {
					if (relation2.equals(relation1)) {
						found = true;
						break;
					}
				}
				if (!found) {
					update = true;
					break;
				}
			}
		}
		else {
			update = true;
		}
		
		if (update) {
			updates.add("relations");
			content.setRelations(relations);
		}
		
		return updates;
	}
	
	private List<String> setResources(Content content, ApiFile file) throws ContentMgmtException {
		List<String> updates = new ArrayList<String>();
		
		List<KBFile> files = new ArrayList<KBFile>();
		
		if (file.getResources().isEmpty()) return updates;

		for (ApiResource resource : file.getResources()) {
			Serializable resourceId = getId(resource);
			Resource kbfile = getContentDao().findResourceById(KBFile.class, resourceId);
			if (kbfile==null || !(kbfile instanceof KBFile)) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.FILE_NOT_FOUND);
			}	
			else {
				files.add((KBFile)kbfile);
			}	
		};
	
		updates = setResources(content, files);
		
		return updates;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	private RelationTemplate getRelation(Content content, String relationname) {
		for (RelationTemplate template : content.getContentTemplate().getRelations()) {
			if (template.getName().toLowerCase().equals(relationname.toLowerCase())) {
				return template;
			}
		}
		return null;
	}
	
//	/** ------------------------------------------------------------------------------------------------------------------------
//	 */
//	private Attribute getAttribute(String name) {
//		name = name.toLowerCase();
//		for (Attribute attribute : getContentDao().getAttributes(getDomain())) {
//			if (name.equals(attribute.getName().toLowerCase())) {
//				return attribute;
//			}
//		}
//		return null;
//	}
//	
//	/** ------------------------------------------------------------------------------------------------------------------------
//	 */
//	private Classifier getClassifier(String name) {
//		name = name.toLowerCase();
//		for (Classifier classifier : getContentDao().getClassifiers(getDomain())) {
//			if (name.equals(classifier.getName().toLowerCase()) || name.equals(classifier.getAlias())) {
//				return classifier;
//			}
//		}
//		return null;
//	}
	
	private String getId(ApiResource resource) {
		if (resource.getId()!=null) {
			return resource.getId();
		}
		else {
			return getId(resource.getHRef());
		}
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	private Content getFile(Source source, String id) {
		Content content = getContentDao().findContentByExternalId(source, id);
		return content;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	private String getId(String href) {
		if (href==null) return null;
		int s = href.lastIndexOf("/");
		if (s<=0 || s==href.length()-1) return null;
		String id = href.substring(s+1);
		id = id.toLowerCase();
		return id;
	}
	
	
//	/** ------------------------------------------------------------------------------------------------------------------------
//	 */
//	private String getTitle(String filename) {
//		int i = filename.lastIndexOf(".");
//		String title = "";
//		String extension = i>0 && i<filename.length() ? filename.substring(i+1).toLowerCase() : "";
// 		if (extension.equals("pdf") || extension.equals("jpg") || extension.equals("xls") || extension.equals("xlsx") || extension.equals("webp"))
// 			title = filename.substring(0, i);
//		return title;
//	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	*/
	@Override
	protected String getDefaultTemplate() {
		return SystemParameters.get("com.novamens.content.webapi.realpage.contenttemplate", "File");
	}
}