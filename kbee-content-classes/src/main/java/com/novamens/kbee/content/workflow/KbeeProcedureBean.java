package com.novamens.kbee.content.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.Transient;

import org.springframework.beans.factory.BeanNameAware;

import com.novamens.security.Principal;
import com.novamens.workflow.Activity;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.ProcedurePhase;
import com.novamens.workflow.Process;
import com.novamens.workflow.RoleInProcess;
import com.novamens.workflow.Task;

public class KbeeProcedureBean implements Procedure, BeanNameAware {
	private String id;
	private String name;
	private String code;
	private String bean;
	private String alias;
	private String locale_str;
	private Integer version = 2;
	private List<Task> tasks;
	private List<RoleInProcess> roles;
	private List<ProcedurePhase> phases;
	private StatesMachine states;
	private int plannedTime; // hours
	private String description;
	
	@Transient 
	private Locale locale = null;
	
	public Process start(WorkflowContext context) {
		return getNewProcess(context).start();
	}
	
	public boolean isBean() {
		return true;
	}
	
	public void setTasks(List<Task> tasks) {
		tasks.forEach(task -> ((KbeeTask)task).setProcedure(this));
		this.tasks = tasks;
	}
	
	public List<Task> getTasks() {
		return tasks==null ? new ArrayList<Task>() : tasks;
	}
	
	public Task getTask(String name) {
		for (Task task : tasks) {
			if (task.getId()!=null && task.getId().equals(name)) {
				return task;
			}
		}
		for (Task task : tasks) {
			if (task.getName().equals(name) || (((KbeeTask)task).getAlias()!=null && ((KbeeTask)task).getAlias().equals(name))) {
				return task;
			}
		}
		return null;
	}
	
	public Task getInitial() {
		for (Task task : getTasks()) {
			if (task.isInitial()) {
				return task;
			}
		}
		return null;
	}

	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}

	public void setBeanName(String bean) {
		this.bean = bean;
	}
	
	public String getBeanName() {
		return this.bean;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Integer getVersion() {
		return version;
	}
	
	public void setVersion(Integer version) {
		this.version = version;
	}
	
	public void setDisplayName(String name) {
		this.name = name;
	}
	
	public String getDisplayName() {
		return name;
	}
	
	public void setAlias(String name) {
		this.alias = name;
	}
	
	public String getAlias() {
		return alias;
	}
	
	public void setRoles(List<RoleInProcess> roles) {
		this.roles = roles;
	}
	
	public List<RoleInProcess> getRoles() {
		return this.roles;
	}
	
	public void setPhases(List<ProcedurePhase> phases) {
		this.phases = phases;
	}
	
	public List<ProcedurePhase> getPhases() {
		return this.phases;
	}
	
	public void setPlannedTime(int time) {
		this.plannedTime = time;
	}
	
	public int getPlannedTime() {
		return plannedTime;
	}
	
	public void setStates(StatesMachine states) {
		this.states = states;
	}
	
	public StatesMachine getStates() {
		return this.states;
	}
	
	public List<Procedure> getSubprocedures() {
		return new ArrayList<Procedure>();
	}
	
	public Activity initiate(WorkflowContext context) {
		return this.handle(StatesMachine.Initialization_Event, context);
	}
	
	public void cancel(WorkflowContext context) {
		this.handle(StatesMachine.Cancelation_Event, context);
	}
	
	public Activity handle(String event, WorkflowContext context) {
		return getStates().handle(event, context);
	}
	
	public Map<RoleInProcess, List<Principal>> getRoles(WorkflowContext context) {
		return getStates().getRoles(context);
	}
	
	public Procedure clone() {
		KbeeProcedureBean clone = new KbeeProcedureBean();
		
		clone.setName(getName());
		clone.setAlias(getAlias());
		clone.setStates(getStates());
		clone.setTasks(getTasks());
		clone.setCode(getCode());
		clone.setVersion(getVersion());
		clone.setDescription(getDescription());
		
		return clone;
	}
	
	protected Process getNewProcess(WorkflowContext context) {
		return context.getFactory().createProcess(this, context);
	}
	
	public void setDescription(String d) {
		this.description=d;
	}

	@Override
	public String getDescription() {
		return description;
	}
	
	public void setLocale(String locale_str) {
		this.locale_str=locale_str;
		this.locale=null;
	}

	@Override
	public Locale getLocale() {
		if (this.locale==null) {
			if (this.locale_str==null)
				 this.locale=Locale.getDefault();
			 
			else if (this.locale_str.trim().toLowerCase().equals("en"))
				this.locale=Locale.ENGLISH;
			 
		    else if (locale_str.trim().toLowerCase().equals("es"))
				locale=new Locale("es");
			
		    else
				this.locale=Locale.getDefault();
		}
		return this.locale;
	}
	
	public void setSubprocedures(List<Procedure> procedures) {
		
	}
	
	public Procedure getMaster() {
		return this;
	}
}
