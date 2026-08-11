package kbee.replica;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.novamens.content.form.EForm;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.workflow.EndCondition;
import com.novamens.content.workflow.WorkflowRule;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.form.KbeeEForm;
import com.novamens.kbee.content.model.KbeeAttribute;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeContentTemplate;
import com.novamens.kbee.content.model.KbeeDataSetMember;
import com.novamens.kbee.content.workflow.DynamicStates;
import com.novamens.kbee.content.workflow.KbeeAttributeRule;
import com.novamens.kbee.content.workflow.KbeeClassificationRule;
import com.novamens.kbee.content.workflow.KbeeCollaboratorTrigger;
import com.novamens.kbee.content.workflow.KbeeContentProcedure;
import com.novamens.kbee.content.workflow.KbeeLastUserAutomaticTrigger;
import com.novamens.kbee.content.workflow.KbeeLastUserManualTrigger;
import com.novamens.kbee.content.workflow.KbeeManualTrigger;
import com.novamens.kbee.content.workflow.KbeeProcedurePhase;
import com.novamens.kbee.content.workflow.KbeeProcessLauncher;
import com.novamens.kbee.content.workflow.KbeeRoundRobin;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.kbee.content.workflow.KbeeUserAutomaticTrigger;
import com.novamens.kbee.content.workflow.KbeeUserTrigger;
import com.novamens.kbee.content.workflow.KbeeWRole;
import com.novamens.kbee.content.workflow.KbeeWRoleTrigger;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.content.workflow.MultipleRule;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.Permission;
import com.novamens.wicket.markup.html.form.FormLayout;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.ProcedurePhase;
import com.novamens.workflow.RoleInProcess;
import com.novamens.workflow.RouterType;
import com.novamens.workflow.Task;
import com.novamens.workflow.Trigger;
import com.novamens.workflow.TriggerType;

import kbee.api.model.ApiProcedure;
import kbee.api.model.ApiProxy;
import kbee.api.model.ApiValue;
import kbee.api.model.IAction;
import kbee.api.model.ApiClassifier;
import kbee.api.model.IForm;
import kbee.api.model.IGroup;
import kbee.api.model.IKeyValue;
import kbee.api.model.ILauncher;
import kbee.api.model.IModelAttribute;
import kbee.api.model.IRule;
import kbee.api.model.ITask;
import kbee.api.model.ITaskForm;
import kbee.api.model.ITemplate;
import kbee.api.model.ITrigger;


public class ProcedureReplicaHandler extends AbstractReplicaHandler<ApiProcedure, KbeeContentProcedure> {

	public ProcedureReplicaHandler(Replica replica, ApiProcedure idataset) {
		super(replica, idataset);
	}
	
	@Override
	protected void replicateIn(KbeeContentProcedure local) throws ReplicaException {
		ApiProcedure remote = getObject();

		local.setName(remote.getName());
		local.setAlias(remote.getAlias());
		local.setVersion(2);
		local.setDisplayName(remote.getDisplayName());
		local.setLastModifiedUser(getSessionUser());
		
		ITemplate itemplate = getReplicaApi().getTemplate(remote.getTemplate().getId());
		ContentTemplate template = replicated(KbeeContentTemplate.class, itemplate);
		local.setContentTemplate(template);
			
		WorkflowRule initialrule = getRule(remote);
		if (initialrule!=null) local.setInitialRule(initialrule);

		List<RoleInProcess> roles = new ArrayList<RoleInProcess>();
		if (remote.getRoles()!=null)
		for (IKeyValue irole : remote.getRoles()) {
			KbeeWRole role = new KbeeWRole();
			role.setName(irole.getKey());
			role.setLabel(irole.getValue());
			roles.add(role);
		}
		local.setRoles(roles);
			
		if (remote.getPhases()!=null) {
			List<ProcedurePhase> phases = new ArrayList<ProcedurePhase>();
			for (IKeyValue iphase : remote.getPhases()) {
				KbeeProcedurePhase phase = new KbeeProcedurePhase();
				phase.setName(iphase.getKey());
				phase.setLabel(iphase.getValue());
				phases.add(phase);
			}
			local.setPhases(phases);
		}
			
		List<Task> tasks = new ArrayList<Task>();
		for (ITask itask :remote.getTasks()) {
			tasks.add(importTask(itask, local));
		}
		local.setTasks(tasks);
			
		update(local);
		getContentDao().flush();
			
		if (remote.getLaunchers()!=null) {
			//List<ProcessLauncher> launchers = new ArrayList<ProcessLauncher>();
			for (ILauncher ilauncher : remote.getLaunchers()) {
				KbeeProcessLauncher localLauncher = replicated(KbeeProcessLauncher.class, ilauncher);
				//launchers.add(localLauncher);
				local.addLauncher(localLauncher);
			}
		}
	}
	
	@Override
	protected KbeeContentProcedure createLocal() {
		KbeeContentProcedure procedure = new KbeeContentProcedure();
		procedure.setDomain(getSessionDomain());
		procedure.setState(ObjectState.ENABLED);
		procedure.setStates(new DynamicStates());
		procedure.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		procedure.setLastModifiedUser(getSessionUser());
		
//		ITemplate itemplate = getReplicaApi().getTemplate(getObject().getTemplate().getId());
//
//		ContentTemplate localtemplate = getLocal(KbeeContentTemplate.class, itemplate);
//		procedure.setContentTemplate(localtemplate);
		
		update(procedure);
		return procedure;
	}
	
	protected Task createTask() {
		return new WebTask();
	}
		
	private KbeeTask importTask(ITask itask, Procedure procedure) {
		KbeeTask task = (KbeeTask)createTask();
		task.setProcedure(procedure);
		task.setId(itask.getId());
		task.setName(itask.getDisplayName());
		task.setInitial(itask.isInitial());
		task.setRoleName(itask.getRole());
		task.setPhaseName(itask.getPhase());
		task.setEditableTitle(itask.isEditableTitle());
			
		task.setTrigger(getTrigger(itask.getTrigger(), procedure));
			
		List<EndCondition> actions = new ArrayList<EndCondition>();
		for (IAction iaction : itask.getActions()) {
			ManualEndCondition action = new ManualEndCondition(iaction.getLabel(), iaction.getEvent());
			action.setCollaboration(iaction.isCollaboration());
			action.setEnabled(iaction.isEnabled());
			action.setDescription(iaction.getDescription());
			action.setEnablePriority(iaction.isPriority());
			if (iaction.getRouter()!=null)
			action.setRouter(RouterType.valueOf(iaction.getRouter()));
			action.setNextTaskId(iaction.getNextTask());
			action.setRouterScript(iaction.getRouterScript());
			action.setRule(getRule(iaction));
			action.setDefault(iaction.isDefa());
			action.setAutoRunAfter(iaction.getAutoRunAfter());
			if (iaction.getTrigger()!=null) {
				action.setTrigger(getTrigger(iaction.getTrigger(), procedure));
			}
			if (iaction.getCollaborationGroups()!=null) {
				List<Group> collaborators = new ArrayList<Group>();
				for (ApiProxy groupproxy : iaction.getCollaborationGroups()) {
					IGroup igroup = getReplicaApi().getGroup(groupproxy.getId());
					Group group = getLocalGroup(igroup);
					if (group!=null)
					collaborators.add(group);
				}
				action.setCollaborationGroups(collaborators);
			}
			actions.add(action);
		}
		task.setEndConditions(actions);
		List<EForm> forms = new ArrayList<EForm>();
		if (itask.getForms()!=null)
		for (ITaskForm formproxy : itask.getForms()) {
			IForm iform = getReplicaApi().getForm(formproxy.getId());
			if (iform!=null) {
				EForm form = getLocal(KbeeEForm.class, iform);
				if (form!=null) {
					KbeeTaskForm taskform = new KbeeTaskForm(form);
					taskform.setSignatureRequired(formproxy.isSignatureRequired());
					taskform.setReadOnly(formproxy.isReadonly());
					taskform.setFormLayout(FormLayout.valueOf(formproxy.getLayout()));
					forms.add(taskform);
				}
			}
		}
		task.setForms(forms);
		return task;
	}
		
	private Trigger getTrigger(ITrigger itrigger, Procedure procedure) {
			
		Trigger trigger = getTrigger(itrigger.getType());
			
		if (itrigger.getManualPermission()!=null) {
			((KbeeUserTrigger)trigger).setManualPermission(getPermission(itrigger.getManualPermission(), procedure));
		}
			
		if (trigger instanceof KbeeUserAutomaticTrigger) {
			KbeeRoundRobin roundRobin = new KbeeRoundRobin();
			roundRobin.setPermission(getPermission(itrigger.getPermission(), procedure));
			roundRobin.setBackupPermission(getPermission(itrigger.getBackupPermission(), procedure));
			((KbeeUserAutomaticTrigger)trigger).setUserAssignationStrategy(roundRobin);
		}
		return trigger;
	}
		
	private Trigger getTrigger(String type) {
		Trigger trigger = null;
		if (type == null) {
			trigger = new KbeeManualTrigger();
		}
		else
		if (type.equals(TriggerType.AUTOMATIC.name())) {
			trigger = new KbeeUserAutomaticTrigger();
		}
		else
		if (type.equals(TriggerType.USERAUTOMATIC.name()))  {
			trigger = new KbeeUserAutomaticTrigger();
		}
		else
		if (type.equals(TriggerType.OLDUSERAUTOMATIC.name()) || 
			type.equals(TriggerType.USERAUTOMATIC_LASTUSER.name())) {
			trigger = new KbeeLastUserAutomaticTrigger();
		}
		else
		if (type.equals(TriggerType.MANUAL.name()))  {
			trigger = new KbeeManualTrigger();
		}
		else
		if (type.equals(TriggerType.MANUAL_LASTUSER.name()))  {
			trigger = new KbeeLastUserManualTrigger();
		}
		else
		if (type.equals(TriggerType.ROLE.name()))  {
			trigger = new KbeeWRoleTrigger();
		}
		else
		if (type.equals(TriggerType.COLLABORATOR.name()))  {
			trigger = new KbeeCollaboratorTrigger();
		}
		else {
			trigger = new KbeeManualTrigger();
		}
		return trigger;
	}
		
	private Permission getPermission(IKeyValue ipermission, Procedure procedure) {
		if (ipermission==null) return null;
		String action = ipermission.getKey();
		String label = ipermission.getValue();
		KbeePermission permission = new KbeePermission(action, label);
		permission.setAction(action);
		return permission;
	}
		
	private WorkflowRule getRule(ApiProcedure iprocedure) {
		List<WorkflowRule> rules = new ArrayList<WorkflowRule>();
		if (iprocedure.getRules()!=null) {
			for (IRule irule : iprocedure.getRules()) {
				WorkflowRule rule = getRule(irule);
				if (rule!=null) {
					rules.add(rule);
				}
			}
		}
		if (!rules.isEmpty()) {
			MultipleRule rule = new MultipleRule(rules);
			return rule;
		}
		return null;
	}
		
	private WorkflowRule getRule(IAction action) {
		List<WorkflowRule> rules = new ArrayList<WorkflowRule>();
		if (action.getRules()!=null) {
			for (IRule irule : action.getRules()) {
				WorkflowRule rule = getRule(irule);
				if (rule!=null) {
					rules.add(rule);
				}
			}
		}
		if (!rules.isEmpty()) {
			MultipleRule rule = new MultipleRule(rules);
			return rule;
		}
		return null;
	}
		
	private WorkflowRule getRule(IRule irule) {
		WorkflowRule rule = null;
		if ("classification".equals(irule.getType())) {
			Classifier classifier = null;
			DataSetMember value = null;
			ApiClassifier iclassifier = null;
			ApiValue ivalue = null;
			if (irule.getClassifier()!=null) {
				iclassifier = getReplicaApi().getClassifier(irule.getClassifier().getId());
			}
			if (iclassifier!=null) {
				classifier = getLocal(KbeeClassifier.class, iclassifier);
			}
			if (classifier!=null && irule.getValue()!=null) {
				ivalue = new ApiValue();
				ivalue.setId(irule.getValue().getId());
				ivalue.setDomain(iclassifier.getDomain());
				value = getLocal(KbeeDataSetMember.class, ivalue);
			}
			if (value!=null && classifier!=null) {
				rule = new KbeeClassificationRule(classifier, value);
			}
		}
		else
		if ("attribute".equals(irule.getType())) {
			Attribute attribute = null;
			IModelAttribute iattribute = null;
			if (irule.getAttribute()!=null) {
				iattribute = getReplicaApi().getAttribute(irule.getAttribute().getId());
			}
			if (iattribute!=null) {
				attribute = getLocal(KbeeAttribute.class, iattribute);
			}
			if (attribute!=null && irule.getStringValue()!=null) {
				rule = new KbeeAttributeRule(attribute, irule.getStringValue());
			}
		}
		return rule;
	}
		
//	private ProcessLauncher createLauncher(ContentTemplate template) {
//		return (ProcessLauncher)ServiceLocator.getService(ObjectFactoryService.class).createLauncher(template);
//	}
	

}
