package com.novamens.kbee.content.workflow;

import java.time.OffsetDateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novamens.content.base.Content;
import com.novamens.content.form.EForm;
import com.novamens.content.service.DomainService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.security.User;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.WorkflowThreadStatus;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Factory;
import com.novamens.workflow.Priority;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.ProcedurePhase;
import com.novamens.workflow.Process;
import com.novamens.workflow.Reason;
import com.novamens.workflow.ResolutionAction;
import com.novamens.workflow.RoleInProcess;
import com.novamens.workflow.Task;

public class KbeeContext implements WorkflowContext  {
	private Factory factory;
	private String state;
	private Process process;
	private Activity activity;
	private Activity parentActivity;
	private String thread;
	private Task task;
	private Task callerTask;
	private OffsetDateTime time;
	private OffsetDateTime dueDate;
	private User user;
	private User initiator;
	private User requester;
	private User collaborator;
	private ProcedurePhase phase;
	private Map<RoleInProcess, User> roles;
	private Content content;
	private Priority priority;
	private Reason reason;
	private String note;
	private String resolution;
	private String resolution_title;
	private boolean isApi;
	private List<WorkflowThreadStatus> threads;
	private Map<EForm, String> formscaptures = new HashMap<EForm, String>();
	private Map<String, String> parameters = new HashMap<String, String>();
	
	private transient Object initialData;
	
	public static String Initial_State = "Initial";
	
	public KbeeContext(Factory factory) {
		setFactory(factory);
		setState(Initial_State);
		setPriority(Priority.Standard);
	}
	
	public String getState() {
		return state;
	}
	
	public void setState(String state) {
		this.state = state;
	}
	
	public Factory getFactory() {
		return factory;
	}

	public void setFactory(Factory factory) {
		this.factory = factory;
	}

	@Override
	public Procedure getProcedure() {
		if (getTask()!=null) return getTask().getProcedure();
		return getProcess() == null ? null : getProcess().getProcedure();
	}
	
	@Override
	public Process getProcess() {
		return process;
	}
	
	public void setProcess(Process process) {
		this.process = process;
	}
	
	@Override
	public Task getTask() {
		return task;
	}
	
	public void setTask(Task task) {
		this.task = task;
	}
	
	public Task getCallerTask() {
		return callerTask;
	}
	
	public void setCallerTask(Task task) {
		this.callerTask = task;
	}
	
	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public Activity getParentActivity() {
		return parentActivity;
	}

	public void setParentActivity(Activity parentActivity) {
		this.parentActivity = parentActivity;
	}
	
	public WorkflowContext getParentContext() {
		Content content = ((KbeeWorkflowActivity)getParentActivity()).getContent();
		KbeeContext context = (KbeeContext)content.getService(WorkflowService.class).getContext();
		return context;
	}

	public String getThread() {
		return thread;
	}

	public void setThread(String thread) {
		this.thread = thread;
	}

	public Task getPreviousTask() {
		Activity previous = getPreviousTerminatedActivity();
		if (previous!=null) {
			return previous.getTask();
		}
		return null;
	}
	
	@Override
	public Activity getCurrentActivity() {
		if (thread!=null) {
			for (Activity activity : getProcess().getActivities()) {
				if (thread.equals(activity.getThread()) && activity.isRunning()) {
					return activity;
				}
			}
			return null;
		}
		else {
			List<Activity> activities = getProcess().getActivities();
			if (activities.isEmpty()) return null;
			return activities.get(0);
		}
	}
	
	public Activity getPreviousActivity() {
		if (getProcess()!=null)
			for (Activity activity : getProcess().getActivities()) {
				if (activity.getStatus().equals(Activity.Status.TERMINATED) || activity.getStatus().equals(Activity.Status.REASSIGNED))
					return activity;
			}
		return null;		
	}
	
	public Activity getPreviousTaskResolution() {
		Activity previous = null;
		if (getProcess()!=null)
		for (Activity activity : getProcess().getActivities()) {
			if (activity.getStatus().equals(Activity.Status.TERMINATED)) {
				if (!ResolutionAction.TRANSFER.equals(activity.getTask().getResolutionAction())) {
					if (activity.getResolution()!=null) {
						previous = activity;
					}
					break;
				}
			}
		}
		return previous;	
	}
	
	public Activity getPreviousTerminatedActivity() {
		Activity previous = null;
		if (getProcess()==null) return null;
		for (Activity activity : getProcess().getActivities()) {
			if (activity.getStatus().equals(Activity.Status.TERMINATED)) {
				previous = activity;
				break;
			}
		}
		return previous;
	}

	@Override
	public ProcedurePhase getCurrentPhase() {
		return phase;
	}
	
	public void setCurrentPhase(ProcedurePhase phase) {
		this.phase = phase;
	}
	
	public Map<RoleInProcess, User> getRoles() {
		return roles;
	}
	
	
	public void setRoles(Map<RoleInProcess, User> map) {
		this.roles = map;
	}
	
	public void setRole(RoleInProcess role, User user) {
		if (roles == null) roles = new HashMap<RoleInProcess, User>();
		roles.put(role, user);
	}
	
	public Content getContent() {
		return content;
	};
	
	public void setContent(Content content) {
		this.content = content;
	};
	
	public Priority getPriority() {
		return priority;
	}
	
	public void setPriority(Priority priority) {
		this.priority = priority;
	}
	
	public Reason getReason() {
		return reason;
	}
	
	public void setReason(Reason reason) {
		this.reason = reason;
	}
	
	public void setReason(String code, String label) {
		this.reason = new KbeeReason(code, label);
	}
	
	public OffsetDateTime getTime() {
		return time;
	};
	
	public void setTime(OffsetDateTime date) {
		this.time = date;
	};
	
	public OffsetDateTime getDueDate() {
		return dueDate;
	};
	
	public void setDueDate(OffsetDateTime date) {
		this.dueDate = date;
	};
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public User getInitiator() {
		return initiator;
	}
	
	public void setInitiator(User user) {
		this.initiator = user;
	}
	
	public User getRequester() {
		return requester;
	}
	
	public void setRequester(User user) {
		this.requester = user;
	}
	
	public User getCollaborator() {
		return collaborator;
	}
	
	public void setCollaborator(User user) {
		this.collaborator = user;
	}
	
	public String getNote() {
		return note;
	}
	
	public void setNote(String note) {
		this.note = note;
	}
	
	public String getResolution() {
		return resolution;
	}
	
	public void setResolution(String note) {
		this.resolution = note;
	}
	
	public boolean isApi() {
		return isApi;
	}

	public void setApi(boolean isApi) {
		this.isApi = isApi;
	}

	public void setFormCapture(EForm form, String capture) {
		this.formscaptures.put(form, capture);
	}
	
	public String getFormCapture(EForm form) {
		return this.formscaptures.get(form);
	}
	
	public void setParameter(String name, String value) {
		parameters.put(name, value);
	}
	
	public String getParameter(String name) {
		return parameters.get(name);
	}
	
	public Map<String, String> getParameters() {
		return parameters;
	}
	
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
	
	public List<WorkflowThreadStatus> getThreads() {
		return this.threads;
	}
	
	public void setThreads(List<WorkflowThreadStatus> threads) {
		this.threads = threads;
	}
	
	public WorkflowThreadStatus getThread(String name) {
		for (WorkflowThreadStatus thread : getThreads()) {
			if (thread.getThread().getName().equals(name)) {
				return thread;
			}
		}
		return null;
	}
	
	public boolean isRunningThreads() {
		if (getThreads()!=null)
		for (WorkflowThreadStatus thread : getThreads()) {
			if (thread.getStatus().equals(WorkflowThreadStatus.Status.INITIAL) ||
				thread.getStatus().equals(WorkflowThreadStatus.Status.RUNNING)) {
				return true;
			}
		}
		return false;
	}
	
	public KbeeContext clone() {
		KbeeContext clone = new KbeeContext(getFactory());
		clone.setState(getState());
		clone.setNote(getNote());
		clone.setCollaborator(getCollaborator());
		clone.setUser(getUser());
		clone.setContent(getContent());
		clone.setParameters(getParameters());
		clone.setRoles(getRoles());
		clone.setProcess(getProcess());
		if (getDueDate()!=null)
			clone.setDueDate(getDueDate());
		return clone;
	}
	
	public String getResolutionTitle() {
		return this.resolution_title;
	}
	
	public void setResolutionTitle(String title) {
		this.resolution_title=title;
	}
	
	public boolean isPending() {
		Content content = getContent(); 
		User user = content.getDomain().getService(DomainService.class).getWorkflowUser();
		String wuid = user!=null?String.valueOf(user.getId()):null;
		if (wuid!=null && content.getWorkspace()!=null) {
			return content.getWorkspace().toString().equals(wuid);
		}	
		return false;
	}

	public Object getInitialData() {
		return initialData;
	}

	public void setInitialData(Object initialData) {
		this.initialData = initialData;
	}
}
