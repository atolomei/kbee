package com.novamens.workflow;

import java.util.List;

public interface ForkJoinTask extends Task {
	public List<WorkflowThread> getThreads();
}