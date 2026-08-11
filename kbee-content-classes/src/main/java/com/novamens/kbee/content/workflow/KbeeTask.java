package com.novamens.kbee.content.workflow;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.form.EForm;
import com.novamens.content.security.Role;
import com.novamens.content.workflow.EndCondition;
import com.novamens.indexer.iql.Expression;
import com.novamens.indexer.iql.IqlService;
import com.novamens.kbee.content.security.JavaIqlEvaluator;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.workflow.Activity;
import com.novamens.workflow.DueDateAction;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.ProcedurePhase;
import com.novamens.workflow.ResolutionAction;
import com.novamens.workflow.RoleInProcess;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.WorkflowListener;
import com.novamens.workflow.Trigger;
import com.novamens.workflow.TriggerType;

public class KbeeTask implements Task {
	private String id;
	private String name;
	private String alias;
	private String description;
	
	private Procedure procedure;
	
	private boolean isInitial = false;
	
	private Trigger trigger;
	private TriggerType triggerType = TriggerType.AUTOMATIC;
	
	private List<EndCondition> endConditions;
	
	private String listenername;
	
	private RoleInProcess role;
	private ProcedurePhase phase;
	private String phaseName;
	private String roleName;
	
	private List<EForm> forms = new ArrayList<EForm>();
	
	private List<Role> enabledRoles = new ArrayList<Role>();
	private List<Group> enabledGroups = new ArrayList<Group>();
	private boolean enableProgressNotes = true;
	private boolean editableTitle = true;
	private boolean enableCancel = false;
	private boolean enablePublicLink;
	
	private ResolutionAction resolutionAction;
	
	private String precondition;
	private Task taskOnPreconditionFail;
	private String taskIdOnPreconditionFail;
	
	private DueDateAction duedateAction;
	private String duedateExpression;
	
	private String dueDateAlerts;
	private int maxTimePending;
	private int maxTimeRunning;
	
	private String onStart;
	
	private String subprocedureName;

	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setAlias(String name) {
		this.alias = name;
	}
	
	public String getAlias() {
		return alias;
	}
	
	public boolean isInitial() {
		return isInitial;
	}
	
	public void setInitial(boolean value) {
		isInitial = value;
	}
	
	public void setListenerName(String name) {
		this.listenername = name;
	}
	
	public String getListenerName() {
		return listenername;
	}
	
	public void setDescription(String name) {
		this.description = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setRole(RoleInProcess role) {
		this.role = role;
	}
	
	public RoleInProcess getRole() {
		if (role==null && roleName!=null && getProcedure().getRoles()!=null) {
			for (RoleInProcess role : getProcedure().getRoles()) {
				if (role.getName().equals(roleName)) {
					this.role = role; 
				}
			}
		}
		return role;
	}
	
	public void setRoleName(String role) {
		this.roleName = role;
	}
	
	public String getRoleName() {
		return roleName;
	}
	
	public void setPhase(ProcedurePhase phase) {
		this.phase = phase;
	}
	
	public ProcedurePhase getPhase() {
		if (phase==null && phaseName!=null) {
			for (ProcedurePhase phase : getProcedure().getPhases()) {
				if (phase.getName().equals(phaseName)) {
					this.phase = phase; 
				}
			}
		}
		return phase;
	}
	
	public void setPhaseName(String phase) {
		this.phaseName = phase;
	}
	
	public String getPhaseName() {
		return phaseName;
	}
	
	public boolean isReadOnly() {
		return false;
	}
	
	public void setEditableTitle(boolean value) {
		this.editableTitle = value;
	}
	
	public boolean isEditableTitle() {
		return this.editableTitle;
	}
	
	public void setCancelEnabled(boolean value) {
		this.enableCancel = value; 
	}
	
	public boolean isCancelEnabled() {
		return enableCancel;
	}
	
	public void setProcedure(Procedure procedure) {
		this.procedure = procedure;
	}
	
	public Procedure getProcedure() {
		return this.procedure;
	}
	
	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
	}
	
	public Trigger getTrigger() {
		return trigger;
	}
	
	public void setTriggerType(String value) {
		this.triggerType = TriggerType.fromString(value);
	}
	
	public void setTriggerType(TriggerType value) {
		this.triggerType = value;
	}
	
	public TriggerType getTriggerType() {
		return triggerType;
	}
	
	public void setEndConditions(List<EndCondition> conditions) {
		this.endConditions = conditions;
	}
	
	public List<EndCondition> getEndConditions() {
		return endConditions;
	}
	
	public Activity start(WorkflowContext context, User user) {
		return context.getFactory().createActivity(this, context, user);
	}
	
	public void setPrecondition(String  condition) {
		this.precondition = condition;
	}
	
	@Override
	public String getPrecondition() {
		return precondition;
	}
	
	public void setTaskOnPreconditionFail(Task task) {
		taskOnPreconditionFail = task;
		taskIdOnPreconditionFail = task!=null ? task.getId() : null;
	}
	
	public Task getTaskOnPreconditionFail() {
		return taskOnPreconditionFail;
	}
	
	public String getTaskIdOnPreconditionFail() {
		return taskIdOnPreconditionFail;
	}
	
	public void setTaskIdOnPreconditionFail(String id) {
		taskIdOnPreconditionFail = id;
	}
	
	public boolean precondition(WorkflowContext context) {
		if (getPrecondition()==null)
			return true;
		try {
			Content content = ((KbeeContext)context).getContent();
			Expression iqlexpression = content.getDomain().getService(IqlService.class).getExpression(getPrecondition());
			if (iqlexpression!=null) {
				JavaIqlEvaluator evaluator = new JavaIqlEvaluator(iqlexpression);
				boolean evaluation = evaluator.evaluate(content);
				return evaluation;
			}
		} 
		catch (Exception e) {
			throw new KbeeRuntimeException(e);
		}
		return true;
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof KbeeTask)) return false;
		if (!((KbeeTask)object).getId().equals(getId())) {
			return false;
		};
		return true;
	}

	@Override
	public String getDisplayName() {
		return getName();
	}
	
	@Override
	public WorkflowListener getListener() {
		return null;
	}
	
	public List<Group> getEnabledGroups() {
		return enabledGroups;
	}
	
	public void setEnabledGroups(List<Group> groups) {
		this.enabledGroups = groups;
	}
	
	public List<Role> getEnabledRoles() {
		return enabledRoles;
	}
	
	public void setEnabledRoles(List<Role> roles) {
		this.enabledRoles = roles;
	}
	
	public ResolutionAction getResolutionAction() {
		return resolutionAction==null ? ResolutionAction.SETNULL : resolutionAction;
	}
	
	public void setResolutionAction(ResolutionAction action) {
		this.resolutionAction = action;
	}
	
	public String getDueDateAlerts() {
		return dueDateAlerts;
	}

	public void setDueDateAlerts(String dueDateAlerts) {
		this.dueDateAlerts = dueDateAlerts;
	}

	public int getMaxTimePending() {
		return maxTimePending;
	}
	
	public void setMaxTimePending(int days) {
		this.maxTimePending = days;
	}
	
	public int getMaxTimeRunning() {
		return maxTimeRunning;
	}
	
	public void setMaxTimeRunning(int days) {
		this.maxTimeRunning = days;
	}
	
	public void setForms(List<EForm> forms) {
		this.forms = forms;
	}
	
	public List<EForm> getForms() {
		return forms;
	}

	public boolean isProgressNotesEnabled() {
		return enableProgressNotes;
	}
	
	public boolean isEnableProgressNotes() {
		return enableProgressNotes;
	}

	public void setEnableProgressNotes(boolean value) {
		this.enableProgressNotes = value;
	}

	public boolean isEnablePublicLink() {
		return enablePublicLink;
	}

	public void setEnablePublicLink(boolean enablePublicLink) {
		this.enablePublicLink = enablePublicLink;
	}
	
	public DueDateAction getDuedateAction() {
		return duedateAction;
	}
	
	public void setDuedateAction(DueDateAction action) {
		this.duedateAction = action;
	}
	
	public String getDuedateExpression() {
		return duedateExpression;
	}
	
	public void setDuedateExpression(String expression) {
		this.duedateExpression = expression;
	}

	public String getOnStart() {
		return onStart;
	}

	public void setOnStart(String onStart) {
		this.onStart = onStart;
	}

	public String getSubprocedureName() {
		return subprocedureName;
	}

	public void setSubprocedureName(String subprocedureName) {
		this.subprocedureName = subprocedureName;
	}
}