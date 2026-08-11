package com.novamens.kbee.content.workflow;

import com.novamens.workflow.Procedure;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowThread;

public class KbeeWorkflowThread implements WorkflowThread {

	String name;
	Task task;
	Procedure procedure;
	String taskId;
	Long procedureId;

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Procedure getProcedure() {
		return procedure;
	}

	public void setProcedure(Procedure procedure) {
		this.procedure = procedure;
	}

	public Long getProcedureId() {
		return procedureId;
	}

	public void setProcedureId(Long procedureId) {
		this.procedureId = procedureId;
	}

}