package com.novamens.content.workflow;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import com.novamens.content.entity.Person;
import com.novamens.security.Principal;
import com.novamens.security.TokenSubmission;
import com.novamens.security.User;
import com.novamens.service.ObjectService;
import com.novamens.workflow.Activity;
import com.novamens.workflow.ActivityProgressNote;
import com.novamens.workflow.Priority;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.WorkflowEvent;
import com.novamens.workflow.WorkflowException;
import com.novamens.workflow.WorkflowThreadStatus;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Process;
import com.novamens.workflow.RoleInProcess;
import com.novamens.workflow.Task;

public interface WorkflowService extends ObjectService {
	
	public Process startProcess(ProcessLauncher launcher);
	public Process startProcess(ProcessLauncher launcher, boolean ispai);
	public Process startProcess(ProcessLauncher launcher, String note, boolean ispai);
	public Process startProcess(ProcessLauncher launcher, Object initialData, String note, User collaborator);
	
	public Process startProcess(Procedure procedure);
	public Process startProcess(Procedure procedure, boolean ispai);
	public Process startProcess(Procedure procedure, String note, boolean ispai);
	
	public Process getLastProcess();
	public Activity getActivity();
	public Task getTask();
	public Task reloadTask();
	public String getTaskComment();
	
	public WorkflowContext getContext();

	public boolean active();
	public boolean updatedResources();
	public boolean isPending();
	public boolean isBatchEnabled();
	
	public void update();
	public void cancel();
	public void startTask() throws WorkflowException;
	public void setPriority(Priority priority);
	
	public void reassign(User user, String note);
	public void assign(User user, String note);
	public void assign(User user, String note, String resolution);
	public void setAsPending(Task task, WorkflowContext context);
	public void handle(WorkflowEvent event, WorkflowContext context);
	public void handle(WorkflowEvent event);                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  
	public Map<RoleInProcess, List<Principal>> getRoles(WorkflowContext context);
	public void setParameter(String name, String value);
	public void setParameters(Map<String, String> parameters);
	public void setResolution(String response, String title);
	public void setNote(String note);
	
	public void setDueDate(OffsetDateTime duedate);
	public void updateDueDate();
	public boolean hasDueDateAlert();
	public boolean fireDueDateAlert();
	
	public TokenSubmission sendToken(WorkflowContext context, Person person);
	public TokenSubmission sendToken(Person person);
	public TokenSubmission resendToken();
	
	public void restartPrevious();
	
	public ActivityProgressNote createProgressNote();
	public void deleteProgressNote(ActivityProgressNote note);
	public void publish(ActivityProgressNote note);
	
	public List<WorkflowThreadStatus> getThreads();
	
	public boolean isBroken();
	public void fix();
}