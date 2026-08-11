package com.novamens.kbee.content.webapi.handler;

import java.io.Serializable;
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
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentService;
import com.novamens.kbee.lock.LockTransactionSynchronization;
import com.novamens.content.workflow.EndCondition;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.IndexTask;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.content.model.KbeeRelation;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.content.workflow.KbeeWorkflowEvent;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.system.SystemParameters;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Procedure;

import kbee.api.model.ApiFile;
import kbee.api.model.ApiProxy;
import kbee.api.model.ApiResource;
import kbee.api.model.ITransaction;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;

public class ProcessLaunchHandler extends com.novamens.kbee.content.webapi.handler.FileUpdateAbstractHandler {
	
	static private Logger logger = LogManager.getLogger(FileUpdateAbstractHandler.class.getName());
	static private kbee.util.logging.Logger kblogger = new kbee.util.logging.Logger(logger);
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Transactional
	public ITransaction launch(String procedureName, ApiFile file) {
		
		try {
			Domain domain = getDomain(file);
			
			if (domain == null) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.DOMAIN_NOT_FOUND);
			}
			
			if (file.getApplication()==null) {
				throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.INVALID_APPLICATION);
			}
			
			Procedure procedure = getProcedure(procedureName, file);
			
			
			if (procedure==null) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.PROCEDURE_NOT_FOUND);
			}
			
			su(domain);
			
			Content content = getOrCreateContent(file);

			List<String> updates = new ArrayList<String>();
			
			updates.addAll(setState(content, file));
			
			updates.addAll(setTitle(content, file));
			
			updates.addAll(setAttributes((Classificable)content, file));
			
			updates.addAll(setCustomAttributes(content, file));
			
			updates.addAll(setRelations(content, file));
			
			updates.addAll(setResources(content, file));
			
			if (!isWriteable(content)) {
				throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
			}
			
			((KbeeContent)content).setExternalTime(file.getLastModifiedDate());
			
			content.getService(PropertyService.class).setProperty("file", toJson(file));
			
			content.getService(ContentService.class).update(updates);
			
			getContentDao().flush();
			
			WorkflowService workflowService = content.getService(WorkflowService.class);
			
			com.novamens.workflow.Process process = workflowService.startProcess(procedure);
			
			KbeeTask task = (KbeeTask)process.getContext().getTask();
			
			getContentDao().flush();
			
			content = ((KbeeContext)process.getContext()).getContent();
			
			workflowService = content.getService(WorkflowService.class);
			
			for (EndCondition action : task.getEndConditions()) {
				if (((ManualEndCondition)action).isBatch()) {
					workflowService.handle(new KbeeWorkflowEvent(action.getEvent(), action.getLabel()));
					break;
				}
			}
		
			ITransaction transaction  = getTransaction(getProxy(file));
			
			ServiceLocator.getService(SchedulerService.class).enqueue(new IndexTask(content, domain.getService(JavaIndexerService.class).getIndex(), true));
			
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
	@Transactional
	public ITransaction execute(String launcherId, ApiFile file) {
		
		try {
			Domain domain = getDomain(file);
			
			if (domain == null) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.DOMAIN_NOT_FOUND);
			}
			
//			if (file.getApplication()==null) {
//				throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.INVALID_APPLICATION);
//			}
			
			ProcessLauncher launcher = getLauncher(launcherId, file);
			
			if (launcher==null) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.PROCEDURE_NOT_FOUND);
			}
			
			su(domain);
			
			Content content = getOrCreateContent(file);

			List<String> updates = new ArrayList<String>();
			
//			updates.addAll(setState(content, file));
//			
//			updates.addAll(setTitle(content, file));
//			
//			updates.addAll(setAttributes((Classificable)content, file));
//			
//			updates.addAll(setCustomAttributes(content, file));
//			
//			updates.addAll(setRelations(content, file));
//			
//			updates.addAll(setResources(content, file));
			
			/*
			 * if (!isWriteable(content)) { throw new ApiException(HttpStatus.FORBIDDEN,
			 * ApiError.ACCESS_DENIED); }
			 */
			
			((KbeeContent)content).setExternalTime(file.getLastModifiedDate());
			
			content.getService(PropertyService.class).setProperty("file", toJson(file));
			
			content.getService(ContentService.class).update(updates);
			
			getContentDao().flush();
			
			WorkflowService workflowService = content.getService(WorkflowService.class);
			
			com.novamens.workflow.Process process = workflowService.startProcess(launcher);
			
			getContentDao().flush();
			
			content = ((KbeeContext)process.getContext()).getContent();
			
			Activity activity = !process.getActivities().isEmpty() ? process.getActivities().get(0) : null;
		
			ITransaction transaction  = getTransaction(activity==null ? getProxy(file) : getProxy(activity));
			
			ServiceLocator.getService(SchedulerService.class).enqueue(new IndexTask(content, domain.getService(JavaIndexerService.class).getIndex(), true));
			
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
	protected Procedure getProcedure(String procedurename, ApiFile file) {
		String templatename = getContentTemplate(file);
		Domain domain = getDomain(file);
		ContentTemplate template = getContentDao().findContentTemplateByName(templatename, domain.getId());
		for (ProcessLauncher launcher : template.getProcessLaunchers()) {
			if (procedurename.equals(launcher.getProcedure().getName().toLowerCase())) {
				return launcher.getProcedure();
			}
		}
		return null;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected ProcessLauncher getLauncher(String launcherId, ApiFile file) {
		String templatename = getContentTemplate(file);
		Domain domain = getDomain(file);
		ContentTemplate template = getContentDao().findContentTemplateByName(templatename, domain.getId());
		for (ProcessLauncher launcher : template.getProcessLaunchers()) {
			if (launcherId.equals(String.valueOf(launcher.getId()))) {
				return launcher;
			}
		}
		return null;
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
			Serializable resourceId = getId(resource.getHRef());
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
	
	
	/** ------------------------------------------------------------------------------------------------------------------------
	*/
	@Override
	protected String getDefaultTemplate() {
		return SystemParameters.get("com.novamens.content.webapi.realpage.contenttemplate", "File");
	}
}