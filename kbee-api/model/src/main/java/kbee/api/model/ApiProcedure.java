package kbee.api.model;

import java.util.ArrayList;
import java.util.List;

public class ApiProcedure extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String alias;
	private List<ITask> tasks;
	private List<IRule> rules;
	private List<IKeyValue> phases;
	private List<IKeyValue> roles;
	private List<ILauncher> launchers;
	private ApiProxy template;

	public List<IKeyValue> getPhases() {
		return phases;
	}

	public void setPhases(List<IKeyValue> phases) {
		this.phases = phases;
	}
	
	public void addPhase(IKeyValue phase) {
		if (phases == null) phases = new ArrayList<IKeyValue>();			
		phases.add(phase);
	}

	public List<IKeyValue> getRoles() {
		return roles;
	}

	public void setRoles(List<IKeyValue> roles) {
		this.roles = roles;
	}
	
	public void addRole(IKeyValue role) {
		if (roles == null) roles = new ArrayList<IKeyValue>();			
		roles.add(role);
	}
	
	public List<IRule> getRules() {
		return rules;
	}

	public void setRules(List<IRule> rules) {
		this.rules = rules;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	public ApiProxy getTemplate() {
		return template;
	}

	public void setTemplate(ApiProxy template) {
		this.template = template;
	}

	public List<ITask> getTasks() {
		return tasks;
	}
	
	public void setTasks(List<ITask> tasks) {
		this.tasks = tasks;
	}
	
	public void addTask(ITask task) {
		if (tasks == null) tasks = new ArrayList<ITask>();			
		tasks.add(task);
	}

	public List<ILauncher> getLaunchers() {
		return launchers;
	}

	public void setLaunchers(List<ILauncher> launchers) {
		this.launchers = launchers;
	}
	
	public void addLauncher(ILauncher launcher) {
		if (launchers == null) launchers = new ArrayList<>();			
		launchers.add(launcher);
	}
}
