package com.novamens.workflow;

import com.novamens.workflow.Process;

public interface WorkflowDao {
	public Process findProcessById(Long id);
}
