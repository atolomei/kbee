package com.novamens.kbee.content.workflow;

import java.util.ArrayList;
import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.workflow.WorkflowDao;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Activity;
import com.novamens.workflow.ForkJoinTask;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.WorkflowThread;
import com.novamens.workflow.WorkflowThreadStatus;

public class KbeeForkJoinTask extends KbeeTask implements ForkJoinTask {
	
	private String routerScript;
	private List<WorkflowThread> threads = new ArrayList<>();
	
	public List<WorkflowThread> getThreads() {
		return threads;
	}
	
	public void setThreads(List<WorkflowThread> threads) {
		this.threads = threads;
	}
	
	public void setTasks(List<Task> tasks) {
		threads = new ArrayList<>();
		for (Task task : tasks) {
			KbeeWorkflowThread thread = new KbeeWorkflowThread();
			thread.setTask(task);
			threads.add(thread);
		}
	}
	
	public List<Task> getTasks() {
		List<Task> tasks = new ArrayList<>();
		for (WorkflowThread thread : threads) {
			tasks.add(thread.getTask());
		}
		return tasks;
	}
	
	@Override
	public Activity start(WorkflowContext context, User user) {
		Activity parent = context.getFactory().createActivity(this, context, user);
		((ProcessProxy)context.getProcess()).getProcess().getActivities().add(0, parent);
		getWorkflowDao().update(parent);
		List<WorkflowThreadStatus> threads = new ArrayList<>();
		for (WorkflowThread thread : getThreads()) {
			KbeeContext threadcontext = ((KbeeContext)context).clone();
			Content proxy = ServiceLocator.getService(ContentFactoryService.class).createProxy(((KbeeContext)context).getContent());
			threadcontext.setContent(proxy);
			threadcontext.setTask(thread.getProcedure().getTasks().get(0));
			threadcontext.setThread(thread.getName());
			threadcontext.setParentActivity(parent);
			//threadcontext.setApi(true);
			threads.add(new KbeeWorkflowThreadStatus(thread, proxy));
			update(threadcontext);
			//thread.getProcedure().initiate(threadcontext);
			thread.getProcedure().getInitial().getTrigger().pull(threadcontext);
			//thread.getTask().getTrigger().pull(threadcontext);
		}
		((KbeeContext)context).setThreads(threads);
		return parent;
	}
	
	public String getRouterScript() {
		return routerScript;
	}

	public void setRouterScript(String routerScript) {
		this.routerScript = routerScript;
	}

	public boolean isCancelEnabled() {
		return true;
	}

	private WorkflowDao getWorkflowDao() {
		return (WorkflowDao)ServiceLocator.getService(BeansService.class).getBean("WorkflowDao");
	}

	private void update(WorkflowContext context) {
		getWorkflowDao().update(context);
	}
}