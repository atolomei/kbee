package com.novamens.kbee.content.webapi.type;

import java.util.ArrayList;
import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EIdentifiableForm;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.LauncherGroup;
import com.novamens.content.workflow.AttributeRule;
import com.novamens.content.workflow.ClassificationRule;
import com.novamens.content.workflow.ContentProcedure;
import com.novamens.content.workflow.EndCondition;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.Validator;
import com.novamens.content.workflow.WorkflowRule;
import com.novamens.kbee.content.workflow.KbeeProcedure;
import com.novamens.kbee.content.workflow.KbeeRoundRobin;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.kbee.content.workflow.KbeeUserTrigger;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.content.workflow.MultipleRule;
import com.novamens.kbee.content.workflow.UserTask;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.Permission;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.ProcedurePhase;
import com.novamens.workflow.RoleInProcess;
import com.novamens.workflow.Task;
import com.novamens.workflow.Trigger;
import com.novamens.workflow.UserAssignationStrategy;
import com.novamens.workflow.UserAutomaticTrigger;
import com.novamens.workflow.UserTrigger;

import kbee.api.model.ApiProcedure;
import kbee.api.model.ApiProxy;
import kbee.api.model.ApiViewMode;
import kbee.api.model.IAction;
import kbee.api.model.IKeyValue;
import kbee.api.model.ILauncher;
import kbee.api.model.IRule;
import kbee.api.model.ITask;
import kbee.api.model.ITaskForm;
import kbee.api.model.ITrigger;
import kbee.api.model.IValidator;

public class IProcedureAdapter implements Adapter<Procedure, ApiProcedure> {
	
	private ApiViewMode viewMode = ApiViewMode.All;

	public IProcedureAdapter() {
	}
	
	public IProcedureAdapter(ApiViewMode viewMode) {
		this.viewMode = viewMode;
	}
	
	public ApiProcedure adapt(Procedure procedure) {
		
		procedure = (Procedure)getContentDao().unproxy(procedure);
		procedure = (ContentProcedure)getContentDao().reload(procedure);
		
		ContentTemplate template = ((ContentProcedure)procedure).getContentTemplate();
		
		ApiProcedure iprocedure = new ApiProcedure();
		iprocedure.setId(String.valueOf(((KbeeProcedure)procedure).getId()));
		iprocedure.setAlias(procedure.getAlias());
		iprocedure.setDisplayName(procedure.getDisplayName());
		iprocedure.setDomain(((KbeeProcedure)procedure).getDomain().getName());
		iprocedure.setState(((KbeeProcedure)procedure).getState().name());
		iprocedure.setTemplate(new ApiProxy(String.valueOf(template.getId()), template.getName(), UriHelper.getUri(template), "template"));
		iprocedure.setName(procedure.getName());
		
		for (Task task : procedure.getTasks()) {
			ITask itask = new ITask();
			itask.setId(task.getId());
			itask.setInitial(task.isInitial());
			itask.setName(task.getName());
			itask.setRole(task.getRole()!=null?task.getRole().getName():null);
			itask.setPhase(task.getPhase()!=null?task.getPhase().getName():null);
			itask.setEditableTitle(((KbeeTask)task).isEditableTitle());
			itask.setCancelEnabled(((KbeeTask)task).isCancelEnabled());
			itask.setDisplayName(task.getDisplayName());
			if (ApiViewMode.All.equals(viewMode) && 
				task instanceof UserTask) {
				for (EndCondition actionbase : ((UserTask)task).getEndConditions()) {
					ManualEndCondition action = (ManualEndCondition)actionbase;
					IAction iaction = new IAction();
					iaction.setEvent(action.getEvent());
					iaction.setLabel(action.getLabel());
					iaction.setDescription(action.getDescription());
					iaction.setCollaboration(action.getCollaboration());
					iaction.setEnabled(action.isEnabled());
					iaction.setPriority(action.isEnablePriority());
					iaction.setTokenValidation(action.isTokenValidation());
					iaction.setRouter(action.getRouter()!=null?action.getRouter().name():null);
					iaction.setRouterScript(action.getRouterScript());
					iaction.setNextTask(action.getNextTaskId());
					iaction.setDefa(action.isDefault());
					iaction.setAutoRunAfter(action.getAutoRunAfter());
					iaction.setTrigger(getTrigger(action.getTrigger()));
					iaction.setRules(getRules(action.getRule()));
					for (Group group : action.getCollaborationGroups()) {
						iaction.addCollaborationGroup(new IGroupProxy(group));
					}
					for (Validator validator : action.getPrecondition()) {
						iaction.addValidator(new IValidator(validator.toString(), validator.getMessage()));
					}
					itask.addAction(iaction);
				}
			}
			itask.setTrigger(getTrigger(task.getTrigger()));
			if (ApiViewMode.All.equals(viewMode) && 
				task instanceof UserTask) {
				for (EForm taskform : ((UserTask)task).getForms()) {
					EForm eform = ((KbeeTaskForm)taskform).getForm();
					ITaskForm itaskform = new ITaskForm(String.valueOf(((EIdentifiableForm)eform).getId()), eform.getName(), UriHelper.getUri(eform));
					itaskform.setSignatureRequired(((KbeeTaskForm)taskform).isSignatureRequired());
					itaskform.setReadonly(((KbeeTaskForm)taskform).isReadOnly());
					itaskform.setLayout(((KbeeTaskForm)taskform).getFormLayout().name());
					itask.addForm(itaskform);
				}
			}	
			iprocedure.addTask(itask);
		}
		
		for (RoleInProcess role : procedure.getRoles()) {
			iprocedure.addRole(new IKeyValue(role.getName(), role.getLabel()));
		}
		
		for (ProcedurePhase phase : procedure.getPhases()) {
			iprocedure.addPhase(new IKeyValue(phase.getName(), phase.getLabel()));
		}
		
		for (ProcessLauncher launcher : ((ContentProcedure)procedure).getProcessLaunchers()) {
			ILauncher ilauncher = new ILauncher();
			ilauncher.setId(String.valueOf(launcher.getId()));
			ilauncher.setDomain(launcher.getDomain().getName());
			ilauncher.setDisplayName(launcher.getDisplayName());
			ilauncher.setNewDocumentEnabled(launcher.isEnabled());
			ilauncher.setLibraryEnabled(launcher.isLibrary());
			ilauncher.setApiEnabled(launcher.isApiEnabled());
			ilauncher.setMobile(launcher.isMobile());
			ilauncher.setGroup(launcher.getLauncherGroup()!=null?getProxy(launcher.getLauncherGroup()):null);
			ilauncher.setAcl((new IAclAdapter()).adapt(launcher.getAcl()));
			ilauncher.setDescription(launcher.getDescription());
			ilauncher.setProcedure(new ApiProxy(String.valueOf(launcher.getProcedure().getId()), launcher.getProcedure().getName(), UriHelper.getUri(launcher.getProcedure()), "procedure"));
			iprocedure.addLauncher(ilauncher);
		}
		
		iprocedure.setRules(getRules(((KbeeProcedure)procedure).getInitialRule()));

		return iprocedure;	
	}
	
	private List<IRule> getRules(WorkflowRule rule) {
		if (rule==null) return null;
		List<IRule> irules = new ArrayList<IRule>();
		List<WorkflowRule> rules = new ArrayList<WorkflowRule>();
		if (rule instanceof MultipleRule) {
			rules.addAll(((MultipleRule)rule).getRules());
		}
		else {
			rules.add(rule);
		}
		for (WorkflowRule wrule : rules) {
			if (wrule instanceof ClassificationRule) {
				IRule irule = new IRule();
				irule.setType("classification");
				Classifier classifier = ((ClassificationRule)wrule).getClassifier();
				if (classifier!=null) {
					irule.setClassifier(new ApiProxy(String.valueOf(classifier.getId()), classifier.getAlias(), UriHelper.getUri(classifier), "classifier"));
				}
				DataSetMember value = ((ClassificationRule)wrule).getValue();
				if (value!=null) {
					irule.setValue(new ApiProxy(String.valueOf(value.getId()), value.getDisplayName(), UriHelper.getUri(value), "value"));
				}
				irules.add(irule);
			}
			if (wrule instanceof AttributeRule) {
				IRule irule = new IRule();
				irule.setType("attribute");
				Attribute attribute = ((AttributeRule)wrule).getAttribute();
				if (attribute!=null) {
					irule.setAttribute(new ApiProxy(String.valueOf(attribute.getId()), attribute.getAlias(), UriHelper.getUri(attribute), "attribute"));
				}
				irule.setStringValue(((AttributeRule)wrule).getValue());
				irules.add(irule);
			}
		}
		return irules;
	}
	
	private ITrigger getTrigger(Trigger trigger) {
		if (trigger==null) return null;
		ITrigger itrigger = new ITrigger();
		itrigger.setType(trigger.getType().name());
		if (trigger instanceof UserTrigger)
		itrigger.setManualPermission(getPermission(((KbeeUserTrigger)trigger).getManualPermission()));
		if (trigger instanceof UserAutomaticTrigger) {
			UserAssignationStrategy strategy = ((UserAutomaticTrigger)trigger).getUserAssignationStrategy();
			if (strategy instanceof KbeeRoundRobin) {
				itrigger.setPermission(getPermission(((KbeeRoundRobin)strategy).getPermission()));
				itrigger.setBackupPermission(getPermission(((KbeeRoundRobin)strategy).getBackupPermission()));
			}
		}
		return itrigger;
	
	}
	
	private ApiProxy getProxy(LauncherGroup group) {
		ApiProxy proxy = new ApiProxy(UriHelper.getUri(group));
		proxy.setId(String.valueOf(group.getId()));
		proxy.setRel("launchergroup");
		proxy.setName(group.getAlias());
		return proxy;
	}
	
	private IKeyValue getPermission(Permission permission) {
		if (permission instanceof KbeePermission) {
			IKeyValue ipermission = new IKeyValue(((KbeePermission)permission).getAction(), ((KbeePermission)permission).getLabel());
			return ipermission;
		}
		return null;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
} 
