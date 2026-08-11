package kbee.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class IAction implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String event;
	private String label;
	private String description;
	private boolean collaboration;
	private boolean tokenValidation;
	private boolean enabled;
	private boolean priority;
	private boolean defa; 
	private String router;
	private String routerScript;
	private String nextTask;
	private ITrigger trigger;
	private long autoRunAfter;

	private List<IRule> rules;
	private List<IKeyValue> reasons;
	private List<IValidator> validators;
	private List<ApiProxy> collaborationGroups;

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isCollaboration() {
		return collaboration;
	}

	public void setCollaboration(boolean collaboration) {
		this.collaboration = collaboration;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isPriority() {
		return priority;
	}

	public void setPriority(boolean priority) {
		this.priority = priority;
	}
	

	public boolean isTokenValidation() {
		return tokenValidation;
	}

	public void setTokenValidation(boolean tokenValidation) {
		this.tokenValidation = tokenValidation;
	}

	public String getRouter() {
		return router;
	}

	public void setRouter(String router) {
		this.router = router;
	}

	public String getRouterScript() {
		return routerScript;
	}

	public void setRouterScript(String routerScript) {
		this.routerScript = routerScript;
	}

	public String getNextTask() {
		return nextTask;
	}

	public void setNextTask(String nextTask) {
		this.nextTask = nextTask;
	}
	
	public ITrigger getTrigger() {
		return trigger;
	}

	public void setTrigger(ITrigger trigger) {
		this.trigger = trigger;
	}

	public List<IRule> getRules() {
		return rules;
	}

	public void setRules(List<IRule> rules) {
		this.rules = rules;
	}

	public boolean isDefa() {
		return defa;
	}

	public void setDefa(boolean defa) {
		this.defa = defa;
	}
	
	public long getAutoRunAfter() {
		return autoRunAfter;
	}

	public void setAutoRunAfter(long autoRunAfter) {
		this.autoRunAfter = autoRunAfter;
	}

	public List<IKeyValue> getReasons() {
		return reasons;
	}

	public void setReasons(List<IKeyValue> reasons) {
		this.reasons = reasons;
	}
	
	public List<ApiProxy> getCollaborationGroups() {
		return collaborationGroups;
	}

	public void setCollaborationGroups(List<ApiProxy> collaborationGroups) {
		this.collaborationGroups = collaborationGroups;
	}
	
	public void addCollaborationGroup(ApiProxy group) {
		if (collaborationGroups == null) collaborationGroups = new ArrayList<ApiProxy>();			
		collaborationGroups.add(group);
	}

	public List<IValidator> getValidators() {
		return validators;
	}

	public void setValidators(List<IValidator> validators) {
		this.validators = validators;
	}
	
	public void addValidator(IValidator validator) {
		if (validators == null) validators = new ArrayList<IValidator>();			
		validators.add(validator);
	}
}
