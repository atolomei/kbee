package kbee.web.workflow.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.form.EForm;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.PersonSet;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.Role;
import com.novamens.content.workflow.ClassificationRule;
import com.novamens.content.workflow.EndCondition;
import com.novamens.content.workflow.WorkflowRule;
import com.novamens.content.workflow.Validator;
import com.novamens.dao.SecurityDao;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.dom.Json;
import com.novamens.kbee.content.form.KbeeEForm;
import com.novamens.kbee.content.security.GroupProxy;
import com.novamens.kbee.content.util.ContentList;
import com.novamens.kbee.content.workflow.KbeeAutomaticTrigger;
import com.novamens.kbee.content.workflow.KbeeCollaboratorTrigger;
import com.novamens.kbee.content.workflow.KbeeForkJoinTask;
import com.novamens.kbee.content.workflow.KbeeLastUserAutomaticTrigger;
import com.novamens.kbee.content.workflow.KbeeLastUserManualTrigger;
import com.novamens.kbee.content.workflow.KbeeManualTrigger;
import com.novamens.kbee.content.workflow.KbeeProcedure;
import com.novamens.kbee.content.workflow.KbeeReason;
import com.novamens.kbee.content.workflow.KbeeRoleAutomaticTrigger;
import com.novamens.kbee.content.workflow.KbeeWRoleTrigger;
import com.novamens.kbee.content.workflow.KbeeWorkflowThread;
import com.novamens.kbee.content.workflow.KbeeRoundRobin;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.kbee.content.workflow.KbeeTrigger;
import com.novamens.kbee.content.workflow.KbeeUserAutomaticTrigger;
import com.novamens.kbee.content.workflow.KbeeUserTrigger;
import com.novamens.kbee.content.workflow.KbeeValidator;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.content.workflow.MultipleRule;
import com.novamens.kbee.content.workflow.RuleParser;
import com.novamens.kbee.content.workflow.TaskParser;
import com.novamens.kbee.content.workflow.TimeOutEndCondition;
import com.novamens.kbee.content.workflow.UserTask;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.repository.DomRepositoryService;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.Permission;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.FormLayout;
import com.novamens.workflow.DueDateAction;
import com.novamens.workflow.DueDateExpressionType;
import com.novamens.workflow.ForkJoinTask;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.ProcedurePhase;
import com.novamens.workflow.Reason;
import com.novamens.workflow.ResolutionAction;
import com.novamens.workflow.RoleInProcess;
import com.novamens.workflow.RouterType;
import com.novamens.workflow.Task;
import com.novamens.workflow.Trigger;
import com.novamens.workflow.TriggerType;
import com.novamens.workflow.UserAutomaticTrigger;
import com.novamens.workflow.WorkflowThread;

import kbee.web.eform.KbeeDefaultFormProxy;

public class WebTaskParser extends TaskParser {
												
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(WebTaskParser.class.getName());
	
	@SuppressWarnings("unchecked")
	public Json getJson(List<Task> tasks) {
		KbeeJson json = new KbeeJson();
		List<Map<String, String>> jsontasks = new ArrayList<Map<String, String>>();
		for (Task task : tasks) {
			jsontasks.add(getMap(task));
		}
		if (!jsontasks.isEmpty())
			json.put("tasks", jsontasks);
		else
			return null;
		return json;
	};
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Task> getTasks(Json json, Procedure procedure) {
		List<Task> tasks = new ArrayList<Task>();
		if (json==null || json.get("tasks")==null) return tasks;
		List<Map> tasksmaps = (List<Map>)json.get("tasks");
		if (tasksmaps!=null)
		for (Map taskmap : tasksmaps) {	
			if (taskmap!=null) {
			Task task = getTask(taskmap, procedure);
			((KbeeTask)task).setProcedure(procedure);
			tasks.add(task);
			}
		}
		for (Task task : tasks) {
			if (task instanceof UserTask) {
				for (EndCondition condition : ((UserTask)task).getEndConditions()) {
					if (condition instanceof ManualEndCondition) {
						Task nextTask = null;
						if (((ManualEndCondition)condition).getNextTaskId()!=null) {
							for (Task t : tasks) {
								if (t.getId().equals(((ManualEndCondition)condition).getNextTaskId())) {
									nextTask = t;
									((ManualEndCondition)condition).setNextTask(t);
									break;
								}
							}
						}
						if (((ManualEndCondition)condition).getTrigger()!=null) {
							((KbeeTrigger)((ManualEndCondition)condition).getTrigger()).setTask(nextTask);
						}
					}
				}
			}
			if (task instanceof KbeeTask) {
				if (((KbeeTask)task).getTaskIdOnPreconditionFail()!=null) {
					for (Task nextTask : tasks) {
						if (nextTask.getId().equals(((KbeeTask)task).getTaskIdOnPreconditionFail())) {
							((KbeeTask)task).setTaskOnPreconditionFail(nextTask);
							break;
						}
					}
				}
			}
			if (task instanceof ForkJoinTask) {
				for (WorkflowThread thread : ((ForkJoinTask)task).getThreads()) {
//					for (Task threadtask : tasks) {
//						if (threadtask.getId().equals(((KbeeWorkflowThread)thread).getTaskId())) {
//							((KbeeWorkflowThread)thread).setTask(threadtask);
//							break;
//						}
//					}
					for (Procedure subprocedure : procedure.getSubprocedures()) {
						if (subprocedure.getId().equals(((KbeeWorkflowThread)thread).getProcedureId())) {
							((KbeeWorkflowThread)thread).setProcedure(subprocedure);
							break;
						}
					}	
				}
			}
		}
		return tasks;
	};
	
	@SuppressWarnings({ "rawtypes" })
	private Map getMap(Task task) {
		if (task instanceof WebTask) {
			return getWebTaskMap(task);
		}
		else {
			if (task instanceof ForkJoinTask) {
				return getForkJoinTaskMap(task);
			}
			else {
				return null; 
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map getWebTaskMap(Task task) {

		Map map = new HashMap();
		
		map.put("id", task.getId());
		map.put("name", task.getName());
		map.put("type", "web");
		
		if (task.isInitial()) {
			map.put("initial", "true");
		}
		
		if (((WebTask)task).getVersion()!=null) {
			map.put("version", ((WebTask)task).getVersion());
		}
		
		if (task.getDescription()!=null) {
			map.put("description", task.getDescription());
		}	
		
		map.put("trigger", getMap(task.getTrigger(), task));
		
		if (task instanceof UserTask) {
			List<Map<String, String>> conditions = getEndConditionsMap(((UserTask)task).getEndConditions(), task);
			if (!conditions.isEmpty())
			map.put("conditions", conditions);
		}
		
		map.put("cancel", ((WebTask)task).isCancelEnabled());
		map.put("resources", ((WebTask)task).getEnableEditingAllResources());
		map.put("title", ((WebTask)task).isEditableTitle());
		map.put("progressnotes", ((WebTask)task).isEnableProgressNotes());
		map.put("publiclink", ((WebTask)task).isEnablePublicLink());
		map.put("labels", ((WebTask)task).isEnableLabels());
		
		if (((WebTask)task).getKnowledgeCriteria()!=null && !"".equals(((WebTask)task).getKnowledgeCriteria())) {
			String criteria = ((WebTask)task).getKnowledgeCriteria();
			criteria = criteria.replace("\"", "'");
			map.put("knowledge", criteria);
		}
						
		if (((WebTask)task).getRelatedCriteria()!=null && !"".equals(((WebTask)task).getRelatedCriteria())) {
			String criteria = ((WebTask)task).getRelatedCriteria();
			criteria = criteria.replace("\"", "'");
			map.put("related", criteria);
		}
		
		if (((WebTask)task).getPrecondition()!=null && !"".equals(((WebTask)task).getPrecondition())) {
			String criteria = ((WebTask)task).getPrecondition();
			criteria = criteria.replace("\"", "'");
			map.put("precondition", criteria);
		}
		
		if (task.getRole()!=null) {
			map.put("role", task.getRole().getName());
		}
		
		if (task.getPhase()!=null) {
			map.put("phase", task.getPhase().getName());
		}

		if (!((KbeeTask)task).getEnabledGroups().isEmpty()) {
			List<Map<String, String>> groups = new ArrayList<Map<String, String>>();
			for (Group group : ((KbeeTask)task).getEnabledGroups()) {
				Map groupmap = new HashMap();
				groupmap.put("group", String.valueOf(group.getId()));
				groups.add(groupmap);
			}
			if (!groups.isEmpty())
			map.put("enabled-groups", groups);
		}
		
		if (!((KbeeTask)task).getEnabledRoles().isEmpty()) {
			List<Map<String, String>> roles = new ArrayList<Map<String, String>>();
			for (Role role : ((KbeeTask)task).getEnabledRoles()) {
				Map rolemap = new HashMap();
				rolemap.put("role", String.valueOf(role.getId()));
				roles.add(rolemap);
			}
			if (!roles.isEmpty())
			map.put("enabled-roles", roles);
		}
		
		if (task.getResolutionAction()!=null) {
			map.put("resolutionAction", task.getResolutionAction().name());
		}
		
		if (((KbeeTask)task).getDuedateAction()!=null) {
			map.put("duedateAction", ((KbeeTask)task).getDuedateAction().name());
		}
		
		if (((KbeeTask)task).getDuedateExpression()!=null) {
			map.put("duedateExpression", ((KbeeTask)task).getDuedateExpression());
		}
		
		if (((KbeeTask)task).getDueDateAlerts()!=null) {
			map.put("dueDateAlerts", ((KbeeTask)task).getDueDateAlerts());
		}
		
		if (((KbeeTask)task).getMaxTimePending()>0) {
			map.put("maxTimePending", ((KbeeTask)task).getMaxTimePending());
		}
		
		if (((KbeeTask)task).getMaxTimeRunning()>0) {
			map.put("maxTimeRunnig", ((KbeeTask)task).getMaxTimeRunning());
		}
		
		if (((KbeeTask)task).getTaskOnPreconditionFail()!=null) {
			map.put("taskOnConditionFail", ((KbeeTask)task).getTaskOnPreconditionFail().getId());
		}
		
		if (((WebTask)task).getIncludeCallerForms()) {
			map.put("includeCallerForms", "true");
		}
		
		if (!((WebTask)task).getForms().isEmpty()) {
			List<Map<String, String>> eforms = new ArrayList<Map<String, String>>();
			for (EForm form : ((WebTask)task).getForms()) {
				Map formmap = getMap(form);
				eforms.add(formmap);
			}
			if (!eforms.isEmpty()) {
				map.put("forms", eforms);
			}
		}
		
		if (((KbeeTask)task).getOnStart()!=null && !"".equals(((KbeeTask)task).getOnStart().trim())) {
			map.put("onStart", ((KbeeTask)task).getOnStart());
		}
		
		return map;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map getForkJoinTaskMap(Task task) {

		Map map = new HashMap();
		
		map.put("id", task.getId());
		map.put("name", task.getName());
		map.put("type", "forkjoin");
		
		if (task.getDescription()!=null) {
			map.put("description", task.getDescription());
		}	
		
		if (((KbeeForkJoinTask)task).getRouterScript()!=null) {
			map.put("router", ((KbeeForkJoinTask)task).getRouterScript());
		}
		
		if (task.getPhase()!=null) {
			map.put("phase", task.getPhase().getName());
		}
		
		List<Map<String, String>> jsonthreads = new ArrayList<>();

		for (WorkflowThread thread : ((ForkJoinTask)task).getThreads()) {
			jsonthreads.add(getMap(thread));
		}
		
		if (!jsonthreads.isEmpty())
			map.put("threads", jsonthreads);
	
		return map;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map getMap(Trigger trigger, Task task) {

		Permission permission;
		
		Map map = new HashMap();
		
		if (trigger==null) 
			trigger = new KbeeManualTrigger();
		
		map.put("type", trigger.getType().getId());
		
		if (trigger instanceof KbeeUserTrigger) {
			permission = ((KbeeUserTrigger)trigger).getManualPermission();
			if (permission!=null)
				map.put("manual-permission", getMap(permission, (KbeeTask)task));
		}
		
		if (trigger!=null && trigger instanceof UserAutomaticTrigger) {
			if (((UserAutomaticTrigger)trigger).getUserAssignationStrategy()!=null) {
				Assert.isInstanceOf(KbeeRoundRobin.class, ((UserAutomaticTrigger)trigger).getUserAssignationStrategy());
				map.put("assignation", "roundrobin");
				permission = ((KbeeRoundRobin)((UserAutomaticTrigger)trigger).getUserAssignationStrategy()).getPermission();
				if (permission!=null)
					map.put("permission", getMap(permission, (KbeeTask)task));
				permission = ((KbeeRoundRobin)((UserAutomaticTrigger)trigger).getUserAssignationStrategy()).getBackupPermission();
				if (permission!=null)
					map.put("backup-permission", getMap(permission, (KbeeTask)task));
			}
		}
		
		return map;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map getMap(WorkflowThread thread) {
		Map map = new HashMap();
		if (thread.getProcedure()!=null)
		//map.put("task", thread.getTask().getId());
		map.put("procedure", thread.getProcedure().getId());
		map.put("name", thread.getName());
		return map;
	}

	@SuppressWarnings("rawtypes")
	private Task getTask(Map map, Procedure procedure) {
		String type = (String)map.get("type"); 
		if (type!=null && "forkjoin".equals(type)) {
			return getForkJoinTask(map, procedure);
		}
		else {
			return getWebTask(map, procedure);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Task getWebTask(Map map, Procedure procedure) {
		WebTask task = new WebTask();
		task.setId((String)map.get("id"));
		task.setName((String)map.get("name"));
		if (map.get("page")!=null)
			task.setPage((String)map.get("page"));
		if (map.get("description")!=null)
			task.setDescription((String)map.get("description"));

		if ("true".equals(map.get("initial")) || Boolean.TRUE.equals(map.get("initial"))) {
			task.setInitial(true);
		}
		
		if (map.get("version")!=null) {
			task.setVersion((String)map.get("version"));
		}	

		task.setTrigger(getTrigger((Map)map.get("trigger"), procedure, task));
		task.setTriggerType(task.getTrigger().getType());
		task.setEndConditions(getEndConditions((List<Map>)map.get("conditions"), procedure, task));

		task.setCancelEnabled("true".equals(map.get("cancel")) || Boolean.TRUE.equals(map.get("cancel")));
		task.setEnableEditingAllResources("true".equals(map.get("resources")) || Boolean.TRUE.equals(map.get("resources")));
		task.setEditableTitle(!("false".equals(map.get("title")) || Boolean.FALSE.equals(map.get("title"))));
		task.setEnableProgressNotes("true".equals(map.get("progressnotes")) || Boolean.TRUE.equals(map.get("progressnotes")));
		task.setEnablePublicLink("true".equals(map.get("publiclink")) || Boolean.TRUE.equals(map.get("publiclink")));
		task.setEnableLabels(!("false".equals(map.get("labels")) || Boolean.FALSE.equals(map.get("labels"))));
		
		if (map.get("knowledge")!=null) {
			String criteria = (String)map.get("knowledge");
			criteria = criteria.replace("'", "\"");
			task.setKnowledgeCriteria(criteria);
		}
					
		if (map.get("related")!=null) {
			String criteria = (String)map.get("related");
			criteria = criteria.replace("'", "\"");
			task.setRelatedCriteria(criteria);
		}
		
		if (map.get("precondition")!=null) {
			String criteria = (String)map.get("precondition");
			criteria = criteria.replace("'", "\"");
			task.setPrecondition(criteria);
		}
		
		if (map.get("role")!=null) {
			task.setRole(getRole((String)map.get("role"), procedure));
		}
		
		if (map.get("phase")!=null) {
			task.setPhase(getPhase((String)map.get("phase"), procedure));
		}
		
		if (map.get("enabled-groups")!=null) {
			List<Group> groups = new ArrayList<Group>();
			for (Map groupmap : (List<Map>)map.get("enabled-groups")) {
				try {
					Group group = getSecurityDao().findGroupById(Long.valueOf((String)groupmap.get("group")));
					if (group!=null )
						groups.add(new GroupProxy(group));
				}
				catch(Exception e) {
					logger.error(e);
				}
				if (!groups.isEmpty()) {
					task.setEnabledGroups(groups);
				}
			}
		}
		if (map.get("enabled-roles")!=null) {
			List<Role> roles = new ArrayList<Role>();
			for (Map rolemap : (List<Map>)map.get("enabled-roles")) {
				try {
					Role role = getContentSecurityDao().findRoleById(Long.valueOf((String)rolemap.get("role")));
					role = (Role)getContentDao().reload(role);
					if (role!=null )
						roles.add(role);
				}
				catch(Exception e) {
					logger.error(e);
				}
			}
			if (!roles.isEmpty()) {
				task.setEnabledRoles(roles);
			}
		}
		if (map.get("resolutionAction")!=null) {
			try {
				task.setResolutionAction(ResolutionAction.valueOf((String)map.get("resolutionAction")));
			}
			catch (Exception e) {
			}
		}
		
		if (map.get("duedateAction")!=null) {
			task.setDuedateAction(DueDateAction.valueOf((String)map.get("duedateAction")));
		}
		
		if (map.get("duedateExpression")!=null) {
			task.setDuedateExpression((String)map.get("duedateExpression"));
		}
		
		if (map.get("dueDateAlerts")!=null) {
			task.setDueDateAlerts(map.get("dueDateAlerts").toString());
		}
		
		if (map.get("maxTimePending")!=null) {
			try {
				int value = Integer.valueOf((String)map.get("maxTimePending").toString());
				task.setMaxTimePending(value);
			}
			catch (NumberFormatException e) {
			}
		}
		
		if (map.get("maxTimeRunning")!=null) {
			try {
				int value = Integer.valueOf((String)map.get("maxTimeRunning").toString());
				task.setMaxTimeRunning(value);
			}
			catch (NumberFormatException e) {
			}
		}
		
		if (map.get("taskOnConditionFail")!=null) {
			task.setTaskIdOnPreconditionFail((String)map.get("taskOnConditionFail"));
		}
		
		if (map.get("forms")!=null) {
			List<EForm> forms = new ArrayList<EForm>();
			for (Map formmap : (List<Map>)map.get("forms")) {
				EForm  form = getForm(formmap);
				if (form!=null) {
					forms.add(form);
				}
			}
			if (!forms.isEmpty()) {
				task.setForms(forms);
			}
		}
		
		if ("true".equals(map.get("includeCallerForms")) || Boolean.TRUE.equals(map.get("includeCallerForms"))) {
			task.setIncludeCallerForms(true);
		}
		
		if (map.get("onStart")!=null) {
			task.setOnStart(map.get("onStart").toString());
		}
		
		return task;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Task getForkJoinTask(Map map, Procedure procedure) {
		KbeeForkJoinTask task = new KbeeForkJoinTask();
		task.setId((String)map.get("id"));
		task.setName((String)map.get("name"));
		task.setTrigger(new KbeeAutomaticTrigger());
		if (map.get("description")!=null) {
			task.setDescription((String)map.get("description"));
		}	
		if (map.get("phase")!=null) {
			task.setPhase(getPhase((String)map.get("phase"), procedure));
		}
		if (map.get("router")!=null) {
			task.setRouterScript((String)map.get("router"));
		}
		if (map.get("threads")!=null) {
			List<WorkflowThread> threads = new ArrayList<>();
			for (Map threadmap : (List<Map>)map.get("threads")) {
				WorkflowThread thread = getThread(threadmap);
				if (thread!=null) {
					threads.add(thread);
				}
			}
			if (!threads.isEmpty()) {
				task.setThreads(threads);
			}
		}		
		return task;
	}
	
	@SuppressWarnings("rawtypes")
	private WorkflowThread getThread(Map map) {
		if (map.get("procedure")==null)
			return null;
		KbeeWorkflowThread thread = new KbeeWorkflowThread();
		thread.setTaskId((String)map.get("task"));
		thread.setProcedureId(Long.valueOf((Integer)map.get("procedure")));
		thread.setName((String)map.get("name"));
		return thread;
	}
	
	@SuppressWarnings("rawtypes")
	private Trigger getTrigger(Map map, Procedure procedure, Task task) {
		Trigger trigger = null;
		
		trigger = getTrigger((String)map.get("type"));
		
		if (trigger instanceof KbeeUserTrigger) {
			((KbeeUserTrigger)trigger).setManualPermission(getPermission((Map)map.get("manual-permission"), "take", procedure, task));
		}
		
		if (trigger instanceof KbeeUserAutomaticTrigger) {
			KbeeRoundRobin strategy = new KbeeRoundRobin();
			strategy.setPermission(getPermission((Map)map.get("permission"), ".", procedure, task));
			strategy.setBackupPermission(getPermission((Map)map.get("backup-permission"), "backup", procedure, task));
			((KbeeUserAutomaticTrigger)trigger).setUserAssignationStrategy(strategy);
		}
		
		((KbeeTrigger)trigger).setTask(task);
		
		return trigger;
	}
	
	private Trigger getTrigger(String type) {
		Trigger trigger = null;
		if (type == null) {
			trigger = new KbeeManualTrigger();
		}
		else
		if (type.equals(TriggerType.AUTOMATIC.getLabel())) {
			trigger = new KbeeUserAutomaticTrigger();
		}
		else
		if (type.equals(TriggerType.USERAUTOMATIC.getId()))  {
			trigger = new KbeeUserAutomaticTrigger();
		}
		else
		if (type.equals(TriggerType.OLDUSERAUTOMATIC.getLabel()) || 
			type.equals(TriggerType.USERAUTOMATIC_LASTUSER.getId())) {
			trigger = new KbeeLastUserAutomaticTrigger();
		}
		else
		if (type.equals(TriggerType.USERAUTOMATIC_ROLE.getId())) { 
			trigger = new KbeeRoleAutomaticTrigger();
		}
		else
		if (type.equals(TriggerType.MANUAL.getId()))  {
			trigger = new KbeeManualTrigger();
		}
		else
		if (type.equals(TriggerType.MANUAL_LASTUSER.getId()))  {
			trigger = new KbeeLastUserManualTrigger();
		}
		else
		if (type.equals(TriggerType.ROLE.getId()))  {
			trigger = new KbeeWRoleTrigger();
		}
		else
		if (type.equals(TriggerType.COLLABORATOR.getId()))  {
			trigger = new KbeeCollaboratorTrigger();
		}
		else {
			trigger = new KbeeManualTrigger();
		}
		return trigger;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map getMap(Permission permission, KbeeTask task) {
		Map map = new HashMap();
		String name = ((KbeePermission)permission).getAction();
		if (name==null || "".equals(name)) name= ".";
		map.put("name", name);
		if (task!=null) {
			Map taskmap = new HashMap();
			taskmap.put("id", task.getId());
			taskmap.put("display-name", task.getDisplayName());
			map.put("task", taskmap);
		}
		return map;
	}
	
	@SuppressWarnings("rawtypes")
	private Permission getPermission(Map map, String defaultname, Procedure procedure, Task task) {
		
		String name = map==null || map.get("name")==null ? defaultname : (String)map.get("name");
		String action = name!=null && name.contains("-") ? name.substring(0, name.indexOf("-")) : name;
		
		name = String.valueOf(procedure.getId()) + (".".equals(action) ? "": "-" + action);
		
		String taskid = null, taskdisplayname = null;
		if (map!=null && map.get("task")!=null && map.get("task") instanceof Map) {
			Map taskmap = (Map)map.get("task");
			taskid =  (String)taskmap.get("id");
			taskdisplayname = (String)taskmap.get("display-name");
		}
		else {
			taskid =  task.getId();
			taskdisplayname =  task.getDisplayName();
		}
		
		name += "-" + taskid;  
				
		KbeePermission permission = KbeePermission.valueOf(name.toLowerCase());
		
		Domain domain = ((DomainObject)procedure).getDomain();
		domain = domain!=null ? (Domain)getContentDao().reload(domain) : null;
		
		Locale locale = domain!=null ? domain.getLocale() : Locale.getDefault();
		String label = ".".equals(action) ? taskdisplayname : getLabel(action, locale) + " " + taskdisplayname; 
		permission.setAction(action);
		permission.setLabel(label);
		
		return permission;
	}
	
	@SuppressWarnings("rawtypes")
	private List<EndCondition> getEndConditions(List<Map> maps, Procedure procedure, Task task) {
		List<EndCondition> conditions = new ArrayList<EndCondition>();
		if (maps!=null)
		for (Map map : maps) {
			conditions.add(getEndCondition(map, procedure, task));
		}
		return conditions;
	}
	
	@SuppressWarnings("unchecked")
	private List<Map<String, String>> getEndConditionsMap(List<EndCondition> conditions, Task task) {
		List<Map<String, String>> map = new ArrayList<Map<String, String>>();
		if (conditions!=null)
		for (EndCondition condition : conditions) {
			if (condition instanceof ManualEndCondition) {
				map.add(getMap((ManualEndCondition)condition, task));
			}
			else {
				if (condition instanceof TimeOutEndCondition) {
					map.add(getMap((TimeOutEndCondition)condition));
				}
			}
		}
		return map;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map getMap(ManualEndCondition condition, Task task) {
		Map map = new HashMap();
		map.put("event", condition.getEvent());
		map.put("label", condition.getLabel());
		map.put("description", condition.getDescription());
		if (condition.getCss()!=null && !"".equals(condition.getCss().trim()))
			map.put("css", condition.getCss());
		map.put("priority", condition.isEnablePriority());
		//map.put("resources", condition.getRequiredResources());
		if (!condition.isEnabled()) {
			map.put("enabled", "false");
		}
		map.put("collaboration", condition.getCollaboration());
		if (condition.getCollaborationGroups()!=null) {
			List<Map<String, String>> groups = new ArrayList<Map<String, String>>();
			for (Group group : condition.getCollaborationGroups()) {
				Map groupmap = new HashMap();
				groupmap.put("group", String.valueOf(group.getId()));
				groups.add(groupmap);
			}
			if (!groups.isEmpty())
			map.put("collaboration-groups", groups);
		}
		if (condition.getCollaborationSet()!=null) {
			map.put("collaboration-set", String.valueOf(condition.getCollaborationSet().getId()));
		}
		map.put("batch", condition.getBatch());
		if (condition.getPerms()!=null && !"".equals(condition.getPerms()))  {
			map.put("perm", condition.getPerms());
		}
		if (condition.getCondition()!=null && !"".equals(condition.getCondition()))  {
			map.put("condition", condition.getCondition());
		}
		if (condition.getRule()!=null) {
			Json jsonrule = getRuleMap(condition.getRule());
			if (jsonrule!=null && jsonrule.get("rule")!=null) {
				map.put("rule", jsonrule.get("rule"));
			}
		}
		if (condition.getLetterTemplates()!=null) {
			if (condition.getLetterTemplates() instanceof ContentList) {
				List<WorkflowRule> rules = new ArrayList<WorkflowRule>();
				rules.addAll(((ContentList)condition.getLetterTemplates()).getCriteria());
				if (!rules.isEmpty()) {
					MultipleRule rule = new MultipleRule(rules);
					Json jsonrule = getRuleMap(rule);
					map.put("templates", jsonrule.get("rule"));
				}
			}
		}
		if (condition.isMandatoryLetter()) {
			map.put("mandatoryLetter", "true");
		}
		if (condition.isDefault()) {
			map.put("default", "true");
		}
		if (condition.getReasons()!=null && !condition.getReasons().isEmpty()) {
			List<Map<String, String>> reasons = new ArrayList<Map<String, String>>();
			for (Reason reason : condition.getReasons()) {
				Map reasonmap = new HashMap();
				reasonmap.put("code", reason.getCode());
				reasonmap.put("label", reason.getLabel());
				reasons.add(reasonmap);
			}
			if (!reasons.isEmpty())
			map.put("reasons", reasons);
		}
		if (condition.getPrecondition()!=null && !condition.getPrecondition().isEmpty()) {
			map.put("precondition", getPreconditionMap(condition));
		}
		if (condition.isInfrequent()) {
			map.put("infrequent", "true");
		}
		if (condition.isTokenValidation()) {
			map.put("tokenValidation", "true");
		}
		if (condition.getDuedateAction()!=null) {
			map.put("duedateAction", condition.getDuedateAction().name());
		}
		if (condition.getDuedateExpression()!=null) {
			map.put("duedateExpression", condition.getDuedateExpression());
		}
		if (condition.getDuedateExpressionType()!=null) {
			map.put("duedateExpressionType", condition.getDuedateExpressionType().name());
		}
		if (condition.getNextTaskId()!=null) {
			map.put("nextTask", condition.getNextTaskId());
		}
		if (condition.getRouter()!=null) {
			map.put("router", condition.getRouter().name());
		}
		if (condition.getRouterScript()!=null) {
			map.put("routerScript", condition.getRouterScript());
		}
		
		if (condition.getTrigger()!=null) {
			map.put("trigger", getMap(condition.getTrigger(), condition.getNextTask()));
		}
		
		if (condition.getAutoRunAfter()>0) {
			map.put("autoRunAfter", condition.getAutoRunAfter());
		}
		
		return map;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map getMap(EForm form) {
		Map map = new HashMap();
		if (form instanceof KbeeTaskForm) {
			if (((KbeeTaskForm) form).getFormId()!=null) 
			map.put("id", ((KbeeTaskForm)form).getFormId());
			map.put("name", ((KbeeTaskForm)form).getName());
			if (((KbeeTaskForm)form).getFormLayout()!=null)
			map.put("layout", ((KbeeTaskForm)form).getFormLayout().name());
			map.put("readonly", ((KbeeTaskForm)form).isReadOnly() ? "true" : "false");
			map.put("signature", ((KbeeTaskForm)form).isSignatureRequired() ? "true" : "false");
		}
		return map;
	}
	
	@SuppressWarnings("rawtypes")
	private EForm getForm(Map map) {
		EForm eform = null;
		try {
			KbeeTaskForm taskform = new KbeeTaskForm();
			String eformid = (String)map.get("id");
			if (eformid!=null) {
				eform = (KbeeEForm)ServiceLocator.getService(DomRepositoryService.class).getRepository(EForm.class).findById(Long.valueOf(eformid));
			}
			else {
				eform = new KbeeDefaultFormProxy((String)map.get("name"));
			}
			taskform.setForm(eform);
			taskform.setReadOnly("true".equals(map.get("readonly")) || Boolean.TRUE.equals(map.get("readonly")));
			taskform.setSignatureRequired("true".equals(map.get("signature")) || Boolean.TRUE.equals(map.get("signature")));
			if (map.get("layout")!=null)
			taskform.setFormLayout(FormLayout.valueOf((String)map.get("layout")));
			eform = taskform;
		}
		catch (Exception e) {
			logger.error(e);
		}
		return eform;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map getMap(TimeOutEndCondition condition) {
		Map map = new HashMap();
		map.put("event", condition.getEvent());
		map.put("label", condition.getLabel());
		map.put("type", "timeout");
		map.put("note", condition.getNote());
		map.put("duration", String.valueOf(condition.getDuration()));
		if (!condition.isEnabled()) {
			map.put("enabled", "false");
		}
		if (condition.getRule()!=null) {
			Json jsonrule = getRuleMap(condition.getRule());
			if (jsonrule!=null && jsonrule.get("rule")!=null) {
				map.put("rule", jsonrule.get("rule"));
			}
		}
		return map;
	}
	
	private Json getRuleMap(WorkflowRule rule) {
		return RuleParser.Get().getJson(rule);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<Map<String, String>> getPreconditionMap(ManualEndCondition condition) {
		List<Map<String, String>> map = new ArrayList<Map<String, String>>();
		for (Validator validator : condition.getPrecondition()) {
			Map validatormap = getMap(validator);
			if (validatormap!=null)
			map.add(validatormap);
		}
		return map;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map getMap(Validator validator) {
		Map map = new HashMap();
		KbeeValidator kbeevalidator =  (KbeeValidator)validator;
		if ("".equals(kbeevalidator.getCondition()) || kbeevalidator.getCondition()==null) return null;
		map.put("condition", kbeevalidator.getCondition());
		if (!"".equals(kbeevalidator.getMessage()) && kbeevalidator.getMessage()!=null)
		map.put("message", kbeevalidator.getMessage());
		return map;
	}
	
	@SuppressWarnings("rawtypes")
	private EndCondition getEndCondition(Map map, Procedure procedure, Task task) {
		String type = (String)map.get("type");
		if (type==null || "manual".equals(type)) {
			return getManualEndCondition(map, procedure, task);
		}
		else {
			if ("timeout".equals(type)) {
				return getTimeoutEndCondition(map);
			}
		}
		return null;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private EndCondition getManualEndCondition(Map map, Procedure procedure, Task task) {
		String label = (String)map.get("label");
		String event = (String)map.get("event");
		ManualEndCondition condition = new ManualEndCondition(label, event);
		condition.setDescription((String)map.get("description"));
		condition.setCollaboration("true".equals(map.get("collaboration")) || Boolean.TRUE.equals(map.get("collaboration")));
		condition.setEnablePriority("true".equals(map.get("priority")) || Boolean.TRUE.equals(map.get("priority")));
		condition.setDefault("true".equals(map.get("default")) || Boolean.TRUE.equals(map.get("default")));
		//condition.setRequiredResources("true".equals(map.get("resources")) || Boolean.TRUE.equals(map.get("resources")));
		condition.setBatch("true".equals(map.get("batch")) || Boolean.TRUE.equals(map.get("batch")));
		if (map.get("collaboration-groups")!=null) {
			List<Group> groups = new ArrayList<Group>();
			for (Map groupmap : (List<Map>)map.get("collaboration-groups")) {
				try {
					Group group = getSecurityDao().findGroupById(Long.valueOf((String)groupmap.get("group")));
					if (group!=null )
						groups.add(new GroupProxy(group));
				}
				catch(Exception e) {
					logger.error(e);
				}
				if (!groups.isEmpty()) {
					condition.setCollaborationGroups(groups);
				}
			}
		}
		if (map.get("collaboration-set")!=null) {
			try {
				DataSet dataset = getContentDao().findDataSetById(Long.valueOf((String)map.get("collaboration-set")));
				if (dataset!=null && dataset instanceof PersonSet) {
					condition.setCollaborationSet((PersonSet)dataset);
				}	
			}
			catch(Exception e) {
				logger.error(e);
			}
		}
		if (map.get("css")!=null)
			condition.setCss((String)map.get("css"));
		if (Boolean.FALSE.equals(map.get("enabled")) || "false".equals(map.get("enabled")))
			condition.setEnabled(false);
		if (map.get("perm")!=null && !"".equals(map.get("perm")))
			condition.setPerms((String)map.get("perm"));
		if (map.get("condition")!=null && !"".equals(map.get("condition")))
			condition.setCondition((String)map.get("condition"));
		if (map.get("autoRunAfter")!=null && !"".equals(map.get("autoRunAfter"))) {
			condition.setAutoRunAfter((Integer)map.get("autoRunAfter"));
		}	

		if (map.get("rule")!=null) {
			WorkflowRule rule = getRule((Map)map.get("rule"));
			if (rule!=null) {
				condition.setRule(rule);
			}
		}
		if (map.get("templates")!=null) {
			WorkflowRule rule = getRule((Map)map.get("templates"));
			if (rule instanceof MultipleRule) {
				List<ClassificationRule> criteria = new ArrayList<ClassificationRule>();
				for (WorkflowRule singlerule : ((MultipleRule)rule).getRules()) {
					if (singlerule instanceof ClassificationRule) {
						criteria.add((ClassificationRule)singlerule);
					}
				}
				if (!criteria.isEmpty()) {
					ContentList templates = new ContentList(criteria);
					condition.setLetterTemplates(templates);
				}
			}
		}
		
		condition.setMandatoryLetter(Boolean.TRUE.equals(map.get("mandatoryLetter")) || "true".equals(map.get("mandatoryLetter")));
		condition.setInfrequent(Boolean.TRUE.equals(map.get("infrequent")) || "true".equals(map.get("infrequent")));
		if (map.get("precondition")!=null) {
			condition.setPrecondition(getPrecondition((List<Map>)map.get("precondition")));
		}
		if (map.get("duedateAction")!=null) {
			condition.setDuedateAction(DueDateAction.valueOf((String)map.get("duedateAction")));
		}
		if (map.get("duedateExpression")!=null) {
			condition.setDuedateExpression((String)map.get("duedateExpression"));
		}
		if (map.get("duedateExpressionType")!=null) {
			condition.setDuedateExpressionType(DueDateExpressionType.valueOf((String)map.get("duedateExpressionType")));
		}
		if (map.get("tokenValidation")!=null) {
			condition.setTokenValidation(true);
		}
		if (map.get("reasons")!=null) {
			List<Reason> reasons = new ArrayList<Reason>();
			for (Map reasonmap : (List<Map>)map.get("reasons")) {
				KbeeReason reason = new KbeeReason();
				reason.setCode((String)reasonmap.get("code"));
				reason.setLabel((String)reasonmap.get("label"));
				reasons.add(reason);
			}
			if (!reasons.isEmpty()) {
				condition.setReasons(reasons);
			}
		}
		if (map.get("resolutionAction")!=null) {
			condition.setResolutionAction(ResolutionAction.valueOf((String)map.get("resolutionAction")));
		}
		if (map.get("nextTask")!=null) {
			condition.setNextTaskId((String)map.get("nextTask"));
		}
		if (map.get("router")!=null) {
			condition.setRouter(RouterType.valueOf((String)map.get("router")));
		}
		if (map.get("routerScript")!=null) {
			condition.setRouterScript(((String)map.get("routerScript")));
		}
		if (map.get("trigger")!=null) {
			condition.setTrigger(getTrigger((Map)map.get("trigger"), procedure, task));
		}

		
		return condition;
	}
	
	@SuppressWarnings({ "rawtypes" })
	private EndCondition getTimeoutEndCondition(Map map) {
		String label = (String)map.get("label");
		String event = (String)map.get("event");
		TimeOutEndCondition condition = new TimeOutEndCondition(label, event);
		if (map.get("enabled")!=null && "false".equals(map.get("enabled")))
			condition.setEnabled(false);
		if (map.get("rule")!=null) {
			WorkflowRule rule = getRule((Map)map.get("rule"));
			if (rule!=null) {
				condition.setRule(rule);
			}
		}
		if (map.get("note")!=null) {
			condition.setNote((String)map.get("note"));
		}
		if (map.get("duration")!=null) {
			try {
				condition.setDuration(Integer.valueOf((String)map.get("duration")));
			}	
			catch (NumberFormatException e) {
			}
		}
		return condition;
	}
	
	@SuppressWarnings("rawtypes")
	private List<Validator> getPrecondition(List<Map> maps) {
		List<Validator> conditions = new ArrayList<Validator>();
		for (Map map : maps) {
			conditions.add(getValidator(map));
		}
		return conditions;
	}
	
	@SuppressWarnings({ "rawtypes" })
	private Validator getValidator(Map map) {
		KbeeValidator validator = new KbeeValidator();
		validator.setCondition((String)map.get("condition"));
		validator.setMessage((String)map.get("message"));
		return validator;
	}
	
//	@SuppressWarnings("rawtypes")
//	private List<ClassifierTemplate> getClassifierTemplates(List<Map> maps) {
//		List<ClassifierTemplate> templates = new ArrayList<ClassifierTemplate>();
//		if (maps!=null) {
//			for (Map map : maps) {
//				ClassifierTemplate template = getClassifierTemplate(map);
//				if (template!=null)
//					templates.add(template);
//			}
//		}
//		return templates;
//	}
	
//	@SuppressWarnings("rawtypes")
//	private List<AttributeTemplate> getAttributeTemplates(List<Map> maps) {
//		List<AttributeTemplate> templates = new ArrayList<AttributeTemplate>();
//		if (maps!=null) {
//			for (Map map : maps) {
//				AttributeTemplate template = getAttributeTemplate(map);
//				if (template!=null)
//					templates.add(template);
//			}
//		}
//		return templates;
//	}
	
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	private List<ModelElementTemplate> getStructure(Map map) {
//		List<ModelElementTemplate> structure = new ArrayList<ModelElementTemplate>();
//		if (map.get("classifiers")!=null)
//			structure.addAll(getClassifierTemplates((List<Map>)map.get("classifiers")));
//		if (map.get("attributes")!=null)
//			structure.addAll(getAttributeTemplates((List<Map>)map.get("attributes")));
//		Collections.sort(structure, new Comparator<ModelElementTemplate>() {
//			@Override
//			public int compare(ModelElementTemplate a, ModelElementTemplate b) {
//				return a.getOrder() < b.getOrder() ? -1 : 1;
//			}
//		});
//		return structure;
//	}
	
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	private List<ModelSection> getSections(Map map) {
//		List<ModelSection> sections = new ArrayList<ModelSection>();
//		if (map.get("sections")!=null) {
//			for (Map sectionmap : (List<Map>)map.get("sections")) {
//				ModelSection section  = getSection(sectionmap);
//				if (section!=null) {
//					sections.add(section);
//				}	
//			}
//		}
//		else {
//			TaskModelSection section  = new TaskModelSection();
//			section.setStructure(getStructure(map));
//			if (!section.getStructure().isEmpty())
//			sections.add(section);
//		}
//		return sections;
//	}
	
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	public ModelSection getSection(Map map) {
//		TaskModelSection section = new TaskModelSection();
//		String name = (String)map.get("name");
//		if (!"NULL".equals(name))
//		section.setName(name);
//		List<ModelElementTemplate> structure = new ArrayList<ModelElementTemplate>();
//		if (map.get("structure")!=null && map.get("structure") instanceof List<?>) {
//			for (Map templatemap : (List<Map>)map.get("structure")) {
//				if (templatemap.get("attribute")!=null) {
//					AttributeTemplate template = getAttributeTemplate(templatemap);
//					structure.add(template);
//				}
//				else
//				if (templatemap.get("classifier")!=null) {
//					ClassifierTemplate template = getClassifierTemplate(templatemap);
//					structure.add(template);
//				}
//				else
//				if (templatemap.get("name")!=null) {
//					SubsectionTemplate template = getSubsectionTemplate(templatemap);
//					structure.add(template);
//				}
//			}
//		}
//		section.setStructure(structure);
//		return section;
//	}
	
//	@SuppressWarnings("unchecked")
//	private List<Map<String, String>> getSectionsMap(WebTask task) {
//		List<Map<String, String>> map = new ArrayList<Map<String, String>>();
//		for (ModelSection section : task.getSections()) {
//			map.add(getMap(section));
//		}
//		return map;
//	}
//	
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	public Map getMap(ModelSection section) {
//		Map map = new HashMap();
//		if (section.getName()!=null)
//		map.put("name", "".equals(section.getName().trim()) ? "no name" : section.getName());
//		if (!section.getStructure().isEmpty()) {
//			map.put("structure", getStructureMap(section.getStructure()));
//		}
//		return map;
//	}
	
//	@SuppressWarnings("unchecked")
//	private List<Map<String, String>> getStructureMap(List<ModelElementTemplate> structure) {
//		List<Map<String, String>> map = new ArrayList<Map<String, String>>();
//		int order = 0;
//		for (ModelElementTemplate template : structure) {
//			if (template instanceof ClassifierTemplate) {
//				((TaskClassifierTemplate)template).setOrder(order++);
//				map.add(getMap((ClassifierTemplate)template));
//			}	
//			if (template instanceof AttributeTemplate) {
//				((TaskAttributeTemplate)template).setOrder(order++);
//				map.add(getMap((AttributeTemplate)template));
//			}	
//			if (template instanceof SubsectionTemplate) {
//				((TaskSubsectionTemplate)template).setOrder(order++);
//				map.add(getMap((SubsectionTemplate)template));
//			}	
//			if (template instanceof KbeeModelElementTemplate) {
//				if (template.getElement() instanceof Attribute) {
//					TaskAttributeTemplate attributetemplate = new TaskAttributeTemplate();
//					attributetemplate.setAttribute((Attribute)template.getElement());
//					attributetemplate.setMultiplicity(((KbeeModelElementTemplate)template).getMultiplicity());
//					attributetemplate.setOrder(order++);
//					attributetemplate.setVisible(((KbeeModelElementTemplate)template).isVisible());
//					attributetemplate.setParent(((KbeeModelElementTemplate)template).getParent());
//					attributetemplate.setReadOnly(((KbeeModelElementTemplate)template).isReadOnly());
//					attributetemplate.setSource(((KbeeModelElementTemplate)template).getSource());
//					attributetemplate.setCalculationScript(((KbeeModelElementTemplate)template).getCalculationScript());
//					map.add(getMap(attributetemplate));
//				}
//				if (template.getElement() instanceof Classifier) {
//					TaskClassifierTemplate classifiertemplate = new TaskClassifierTemplate();
//					classifiertemplate.setClassifier((Classifier)template.getElement());
//					classifiertemplate.setOrder(order++);
//					classifiertemplate.setMultiplicity(((KbeeModelElementTemplate)template).getMultiplicity());
//					classifiertemplate.setMetadataSubtitle(((KbeeModelElementTemplate)template).isMetadataSubtitle());
//					classifiertemplate.setVisible(((KbeeModelElementTemplate)template).isVisible());
//					classifiertemplate.setAccessibility(((KbeeModelElementTemplate)template).getAccessibility());
//					classifiertemplate.setParent(((KbeeModelElementTemplate)template).getParent());
//					classifiertemplate.setReverse(((KbeeModelElementTemplate)template).isReverse());
//					classifiertemplate.setSelectionScript(((KbeeModelElementTemplate)template).getSelectionScript());
//					map.add(getMap(classifiertemplate));
//				}
//				if (template.getElement() instanceof Subsection) {
//					TaskSubsectionTemplate subsectiontemplate = new TaskSubsectionTemplate();
//					subsectiontemplate.setOrder(order++);
//					subsectiontemplate.setName(template.getName());
//					map.add(getMap(subsectiontemplate));
//				}
//			}
//		}
//		return map;
//	}
	
//	@SuppressWarnings({ "rawtypes", "deprecation" })
//	private ClassifierTemplate getClassifierTemplate(Map map) {
//		TaskClassifierTemplate template = null;
//		try {
//			String classifierid = (String)map.get("classifier");
//			KbeeClassifier classifier = (KbeeClassifier) getContentDao().findModelObjectById(Classifier.class, Long.valueOf(classifierid));
//			if (classifier!=null && classifier.getState()==ObjectState.ENABLED) {
//				template = new TaskClassifierTemplate();
//				template.setClassifier(classifier);
//				if (map.get("multiplicity")!=null) {
//					template.setMultiplicity(Multiplicity.valueOf((String)map.get("multiplicity")));
//				}
//				if (map.get("order")!=null) {
//					try {
//						template.setOrder(Integer.valueOf((String)map.get("order")));
//					}
//					catch (Exception e) {
//					}
//				}
//				if (map.get("accessibility")!=null) {
//					template.setAccessibility(AccessStrategy.valueOf((String)map.get("accessibility")));
//				}
//				if (map.get("calculation")!=null) {
//					template.setSelectionScript((String)map.get("calculation"));
//				}
//				if (map.get("criteria")!=null) {
//					template.setValuesCriteria((String)map.get("criteria"));
//				}
//				template.setReadOnly(Boolean.TRUE.equals(map.get("readonly")) || "true".equals(map.get("readonly")));
//				if (map.get("reverse")!=null) {
//					template.setReverse(Boolean.TRUE.equals(map.get("reverse")) || "true".equals(map.get("reverse")));
//				}
//			} 
//			String parentid = (String)map.get("parent");
//			if (parentid!=null) {
//				classifier = (KbeeClassifier) getContentDao().findModelObjectById(Classifier.class, Long.valueOf(parentid));
//				if (classifier!=null && classifier.getState()==ObjectState.ENABLED) {
//					template.setParent(classifier);
//				}
//			}
//		}
//		catch (Exception e) {
//			template = null;
//		}
//		return template;
//	}
	
//	@SuppressWarnings("rawtypes")
//	private AttributeTemplate getAttributeTemplate(Map map) {
//		TaskAttributeTemplate template = null;
//		try {
//			String attributeid = (String)map.get("attribute");
//			KbeeAttribute attribute = (KbeeAttribute) getContentDao().findModelObjectById(Attribute.class, Long.valueOf(attributeid));
//			if (attribute!=null && attribute.getState()==ObjectState.ENABLED) {
//				template = new TaskAttributeTemplate();
//				template.setAttribute(attribute);
//				template.setReadOnly(Boolean.TRUE.equals(map.get("readonly")) || "true".equals(map.get("readonly")));
//				if (map.get("subsection")!=null) {
//					template.setSubsection((String)map.get("subsection"));
//				}
//				if (map.get("multiplicity")!=null) { 
//					template.setMultiplicity(Multiplicity.valueOf((String)map.get("multiplicity")));
//				}
//				if (map.get("order")!=null) {
//					try {
//						template.setOrder(Integer.valueOf((String)map.get("order")));
//					}
//					catch (Exception e) {
//					}
//				}
//				String parentid = (String)map.get("parent");
//				if (parentid!=null) {
//					KbeeClassifier classifier = (KbeeClassifier) getContentDao().findModelObjectById(Classifier.class, Long.valueOf(parentid));
//					if (classifier!=null && classifier.getState()==ObjectState.ENABLED) {
//						template.setParent(classifier);
//					}
//				}
//				if (map.get("source")!=null) {
//					template.setSource(AttributeSource.valueOf((String)map.get("source")));
//				}
//				if (map.get("calculation")!=null) {
//					template.setCalculationScript(((String)map.get("calculation")));
//				}
//
//			}
//		}
//		catch (Exception e) {
//			template = null;
//		}
//		return template;
//	}
	
//	@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
//	private Map getMap(ClassifierTemplate template) {
//		Map map = new HashMap();
//		map.put("classifier", String.valueOf(template.getClassifier().getId()));
//		map.put("readonly", template.isReadOnly());
//		map.put("order", String.valueOf(template.getOrder()));
//		if (template.getMultiplicity()!=null) {
//			map.put("multiplicity", template.getMultiplicity().name());
//		}
//		if (template.getParent()!=null) {
//			map.put("parent", String.valueOf(template.getParent().getId()));
//		}
//		if (template.getAccessibility()!=null) {
//			map.put("accessibility", template.getAccessibility().name());
//		}
//		if (template.getValuesCriteria()!=null) {
//			map.put("criteria", template.getValuesCriteria());
//		}
//		if (template.getSelectionScript()!=null) {
//			map.put("calculation", template.getSelectionScript());
//		}
//		if (template.isReverse()) {
//			map.put("reverse", "true");
//		}
//		return map;
//	}
//	
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	private Map getMap(AttributeTemplate template) {
//		Map map = new HashMap();
//		map.put("attribute", String.valueOf(template.getAttribute().getId()));
//		map.put("readonly", template.isReadOnly());
//		map.put("order", String.valueOf(template.getOrder()));
//		if (template.getMultiplicity()!=null) {
//			map.put("multiplicity", template.getMultiplicity().name());
//		}
//		if (template.getParent()!=null) {
//			map.put("parent", String.valueOf(template.getParent().getId()));
//		}
//		if (template.getSource()!=null) {
//			map.put("source", template.getSource().name());
//		}
//		if (template.getCalculationScript()!=null) {
//			map.put("calculation", template.getCalculationScript());
//		}
//		return map;
//	}
//	
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	private Map getMap(SubsectionTemplate template) {
//		Map map = new HashMap();
//		map.put("name", template.getName());
//		map.put("order", String.valueOf(template.getOrder()));
//		return map;
//	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private WorkflowRule getRule(Map map) {
		Map rulemap = new HashMap();
		rulemap.put("rule", map);
		WorkflowRule rule = RuleParser.Get().getRule(new KbeeJson(rulemap));
		return rule;
	}
	
	private RoleInProcess getRole(String name, Procedure procedure) {
		for (RoleInProcess role : procedure.getRoles()) {
			if (name.equals(role.getName()))
				return role;
		}
		return null;
	}
	
	private ProcedurePhase getPhase(String name, Procedure procedure) {
		if (name==null) return null;
		name = name.toLowerCase();
		for (ProcedurePhase phase : procedure.getPhases()) {
			if (phase.getName()!=null && name.equals(phase.getName().toLowerCase())) {
				return phase;
			}	
		}
		return null;
	}
	
	private String getLabel(String key, Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(getClass().getName(), locale);
		return  res.getString(key);
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private SecurityDao  getSecurityDao() {
		return	(SecurityDao)ServiceLocator.getService(BeansService.class).getBean("securityDao");
	}
	
	private ContentSecurityDao  getContentSecurityDao() {
		return	(ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
}
