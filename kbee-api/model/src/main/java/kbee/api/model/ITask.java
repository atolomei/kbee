package kbee.api.model;

import java.util.ArrayList;
import java.util.List;

public class ITask extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private boolean initial;
	private boolean editableTitle;
	private boolean cancelEnabled;
	private String role;
 	private String phase;
	private List<IAction> actions;
	private List<ITaskForm> forms;
	private ITrigger trigger;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<IAction> getActions() {
		return actions;
	}
	public void setActions(List<IAction> actions) {
		this.actions = actions;
	}
	public void addAction(IAction action) {
		if (actions == null) actions = new ArrayList<IAction>();			
		actions.add(action);
	}
	public List<ITaskForm> getForms() {
		return forms;
	}
	public void setForms(List<ITaskForm> forms) {
		this.forms = forms;
	}
	public void addForm(ITaskForm form) {
		if (forms == null) forms = new ArrayList<ITaskForm>();			
		forms.add(form);
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getPhase() {
		return phase;
	}
	public void setPhase(String phase) {
		this.phase = phase;
	}
	public boolean isInitial() {
		return initial;
	}
	public void setInitial(boolean initial) {
		this.initial = initial;
	}
	public ITrigger getTrigger() {
		return trigger;
	}
	public void setTrigger(ITrigger trigger) {
		this.trigger = trigger;
	}
	public boolean isEditableTitle() {
		return editableTitle;
	}
	public void setEditableTitle(boolean editableTitle) {
		this.editableTitle = editableTitle;
	}
	public boolean isCancelEnabled() {
		return cancelEnabled;
	}
	public void setCancelEnabled(boolean cancelEnabled) {
		this.cancelEnabled = cancelEnabled;
	}
}