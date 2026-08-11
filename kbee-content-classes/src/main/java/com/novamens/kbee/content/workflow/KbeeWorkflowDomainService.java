package com.novamens.kbee.content.workflow;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.LauncherGroup;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.workflow.ContentProcedure;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDao;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.content.model.KbeeContentTemplate;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.kbee.security.acl.KbeeAclEntry;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.logging.ObjectUpdateEvent;
import com.novamens.security.User;
import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.security.acl.Permission;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Process;

public class KbeeWorkflowDomainService implements WorkflowDomainService, EventListener {
																								
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeWorkflowDomainService.class.getName());
	
	private Domain domain = null;
	private WorkflowDao workflowDao;

	List<Procedure> list_p = null;
	
	static private Logger txlogger = LogManager.getLogger("TxLogger");

	public KbeeWorkflowDomainService() {
	}
	
	public KbeeWorkflowDomainService(Domain domain) {
		 this.domain = domain;
	}

	@Override
	public List<LauncherGroup> getLauncherGroups() {
		return getWorkflowDao().getLauncherGroups(getDomain(), ObjectState.ENABLED);
	}
	
	@Override
	public List<ProcessLauncher> getLaunchers() {
		return getWorkflowDao().getLaunchers(getDomain());
	}
	
	@Override
	public List<ProcessLauncher> getLaunchers(ObjectState state) {
		return getWorkflowDao().getLaunchers(getDomain(), state);
	}
	
	@Override
	@Transactional
	public Process startProcess(String launcherlabel) {
		for(ProcessLauncher launcher : getLaunchers()) {
			if (launcherlabel.equals(launcher.getLabel())) {
				Process process = startProcess(launcher);
				return process;
			}
		}
		return null;
	}
	
	@Override
	@Transactional
	public Process startProcess(ProcessLauncher launcher) {
		try {
			return launcher.startProcess();
		}
		catch(ContentCreationException | ContentMgmtException e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);
			
		} 
	}
	
	@Override
	@Transactional
	public Process startProcess(ProcessLauncher launcher, Content template) {
		try {
			return launcher.startProcess(template);
		}
		catch(ContentCreationException | ContentMgmtException e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);
			
		} 
	}
	
	@Override
	public List<Procedure> getProcedures() {
		return getWorkflowDao().getProcedures(getDomain());
	}
	
	@Override
	@Transactional
	public void update(Procedure procedure, List<String> parts) {
		getWorkflowDao().update(procedure);
		txlogger.info(new ObjectUpdateEvent<KbeeProcedure>((KbeeProcedure)procedure, parts));
	}
	
	
	@Override
	@Transactional
	public void update(Procedure procedure, String description) {
		getWorkflowDao().update(procedure);
		txlogger.info(new ObjectUpdateEvent<KbeeProcedure>((KbeeProcedure)procedure, description));
	}
	
	
	@Override
	public Procedure getProcedureBean(String key) {
		return (Procedure) ServiceLocator.getService(BeansService.class).getBean(key);
	}
	
	@Override
	public List<Procedure> getProceduresLibrary() {
		
		if (list_p!=null) 
			return list_p;
		
		list_p = new ArrayList<Procedure>();
		Map<String, Procedure> beans = ServiceLocator.getService(BeansService.class).getBeansOfType(Procedure.class);
		for (String bean : beans.keySet())
			list_p.add((Procedure) ServiceLocator.getService(BeansService.class).getBean(bean));

		list_p.sort(new Comparator<Procedure>() {

			@Override
			public int compare(Procedure a, Procedure b) {
				try {
					return a.getDisplayName().compareToIgnoreCase(b.getDisplayName());
				} catch (Exception e) {
					return 0;
				}
				
			}
			
		});
		return list_p;
	}
	
	@Override
	public List<ProcessLauncher> getContextLaunchers(Content content) {
		List<ProcessLauncher> launchers = new ArrayList<ProcessLauncher>();
		for (ProcessLauncher launcher : getContextLaunchers(content.getContentTemplate()))  {
			if (launcher.isEnabled(content)) {
				launchers.add(launcher);
			}
		}
		return launchers;
	}
	
	@Override
	public List<ProcessLauncher> getContextLaunchers(ContentTemplate template) {
		List<ProcessLauncher> launchers = new ArrayList<ProcessLauncher>();
		for (ProcessLauncher launcher : getLaunchers())  { 
			try { 
				if (launcher.isLibrary() && launcher.getContentTemplate().getName().equals(template.getName())) {
					launchers.add(launcher);
				}	
			}
			catch (Exception e) {
				logger.error(e);
			}
		}
		return launchers;
	}
	
	
	@Override
	@Transactional
	public Procedure createProcedure(ContentTemplate template, Procedure prototype) {
		KbeeContentProcedure procedure = new KbeeContentProcedure(prototype);
		procedure.setDomain(template.getDomain());
		getWorkflowDao().update(procedure);
		((KbeeContentTemplate)template).addProcedure(procedure);
		createLauncher(procedure);
		return procedure;
	}
	
	@Override
	@Transactional
	public ProcessLauncher createLauncher(Procedure procedure) {
		try {
			ContentTemplate template = ((ContentProcedure)procedure).getContentTemplate();
			
			KbeeProcessLauncher launcher = new KbeeProcessLauncher();
						
			launcher.setLabel(template.getName() + " - " + procedure.getDisplayName() + " " + String.valueOf(((ContentProcedure)procedure).getProcessLaunchers().size()+1));
			
			launcher.setCreationOffsetDateTime(OffsetDateTime.now());
			launcher.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			launcher.setLastModifiedUser(getSessionUser());
			
			launcher.setState(ObjectState.ENABLED);
			launcher.setDomain(template.getDomain());
			launcher.setContentTemplate(template);
			
			List<LauncherGroup> list= getLauncherGroups();
			if(list!=null && list.size()>0) {
				launcher.setLauncherGroup(list.get(0));
			}
			
			launcher.setLibrary(true);
			launcher.setEnabled(true);

			KbeeAcl acl = new KbeeAcl(); 
			acl.setLastModifiedUser(getSessionUser());
			acl.setLastModifiedOffsetDateTime(OffsetDateTime.now());

			List<Group> fgroups = null;
			fgroups = getSecurityDao().getCanonicalGroups(getDomain());
			Group fusers = null;
			for (Group group: fgroups) {
				if (group.getName().equals(KbeeGlobalRole.USER.getId())) {
					fusers = group;
					break;
				}
			}
			if (fusers!=null) {
				AclEntry docuentry = new KbeeAclEntry(acl, fusers, false);
				List<Permission> docupermissions= new ArrayList<Permission>();
				docupermissions.add(KbeePermission.CREATE);
				docuentry.setPermissions(docupermissions);
				acl.addEntry(getSessionUser(), docuentry);
				launcher.setAcl(acl);
			} 
			else {
				logger.error("can not find group USERS");
				launcher.setAcl(new KbeeAcl());
			}
			
			((KbeeContentProcedure)procedure).addLauncher(launcher);
			
			template.getService(DOMObjectService.class).update();
			
			return launcher;
		}
		catch (ContentMgmtException e) {
			logger.error(e);
			return null;
		}
	}
	
	@Transactional
	public void deleteLauncher(ProcessLauncher launcher) {
		//ContentTemplate template = ((KbeeProcessLauncher)launcher).getContentTemplate();
		ContentProcedure procedure = (ContentProcedure)((KbeeProcessLauncher)launcher).getProcedure();
		((KbeeContentProcedure)procedure).removeLauncher(launcher);
		//List<ProcessLauncher> launchers = ((KbeeContentTemplate)template).getProcessLaunchers();
		//((KbeeContentTemplate)template).removeLauncher(launcher);
		//launchers = ((KbeeContentTemplate)template).getProcessLaunchers();
		getWorkflowDao().delete(launcher);
		//template.getService(DOMObjectService.class).update();
	}
	
	public WorkflowDao getWorkflowDao() {
		return workflowDao;
	}
	
	public void setWorkflowDao(WorkflowDao dao) {
		workflowDao = dao;
	}
	
	public Domain getDomain() {
		return domain;
	}

	@Override
	public boolean listen(Event event) {
		if (event instanceof EvictCacheServiceEvent)
			return true;
		return false;

	}

	@Override
	public void onEvent(Event event) {
		if (event instanceof EvictCacheServiceEvent) {
			list_p=null;
			//map=null;
		}
		
	}
	
	private ContentSecurityDao getSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
