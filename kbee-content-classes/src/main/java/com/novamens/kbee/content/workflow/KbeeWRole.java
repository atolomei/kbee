package com.novamens.kbee.content.workflow;

import java.io.Serializable;

import com.novamens.dom.Indexable;
import com.novamens.workflow.RoleInProcess;

/**
 *<p> A Workflow Role is a role specific for the Business Process. 
 * Every Tasks has one and only one Workflow-Profile. A Workflow-Profile can be seen as a tag applied to the user that executes the Task. 
 * When the Task starts, the user that executes the Task is assigned the Task's Workflow-Profile. This information is used by the workflow engine for routing, 
 * next time it needs to assign a task to the workflow role it will look for the user that has the 'tag attached'.</p>
 */
public class KbeeWRole implements Indexable, RoleInProcess, Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String label;
	
	public KbeeWRole() {
		
	}
	
	public KbeeWRole(String name) {
		this.name = name;
	}
	
	public KbeeWRole(String name, String label) {
		this.name = name;
		this.label = label;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getLabel() {
		return label != null ? label: getName();
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof KbeeWRole)) return false;
		return ((KbeeWRole)object).getName().equals(getName());
	}
	
	@Override
	public int hashCode(){
		return getName()!=null ? getName().hashCode() : super.hashCode();
	}

	@Override
	public Serializable getId() {
		return this.name;
	}

	@Override
	public String getDisplayName() {
		return getLabel();
	}
}