package com.novamens.kbee.content.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Permission;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Process;
import com.novamens.workflow.Task;
import com.novamens.workflow.UserTrigger;
import com.novamens.workflow.WorkflowContext;

public abstract class KbeeUserTrigger extends KbeeTrigger implements UserTrigger {
	
	private Permission permission;

	public Permission getManualPermission() {
		return permission;
	}
	
	public void setManualPermission(Permission permission) {
		this.permission = permission;
	}
	
	public void pull(WorkflowContext context) {
		((KbeeContext)context).setTask(getTask());
		setAsPending(getTask(), ((KbeeContext)context).getContent(), context);
	}
	
	public void pull(WorkflowContext context, User user) {
		if (context.isApi() && ((KbeeContext)context).getPreviousActivity()==null) {
			pull(context);
		}
		else {
			pullToUser(context, user);
		}
		((KbeeContext)context).setApi(false);
	}
	
	public List<Principal> getEnabledPrincipals(WorkflowContext context) {
		return new ArrayList<Principal>();
	}
	
	protected void pullToUser(WorkflowContext context, User user) {
		KbeeContext kbeecontext = (KbeeContext)context;
		kbeecontext.setTask(getTask());
		Content content = kbeecontext.getContent();
		Process process = kbeecontext.getProcess();
		Activity activity = process.start(getTask(), context, user);
		assign(content, activity.getUser(), context.getNote());
	}
		
	protected User getLastUser(WorkflowContext context) {
		KbeeContext kbeecontext = (KbeeContext)context;
		Process process = kbeecontext.getProcess();
		return getLastUser(process);
	}
	
	protected User getLastUser(Process process) {
		User user = null;
		for (Activity activity : process.getActivities()) {
			if (activity.getTask().equals(getTask()) && activity.getStatus().equals(Activity.Status.TERMINATED)) {
				user = activity.getUser();
				break;
			}
		}
		return user;
	}
	
	protected List<Principal> getEnabledPrincipals(Content content, Permission permission) {
		List<Principal> principals = ServiceLocator.getService(ContentSystemSecurityService.class).getEnabledPrincipals(content, permission);
		Collections.sort(principals, new Comparator<Principal>() {
			@Override
			public int compare(Principal a, Principal b) {
				try {
					return a.getName().compareTo(b.getName());
				} 
				catch (Exception e) {
					return 0;
				}
			}
		});
		return principals;
	}
	
	protected void assign(Content content, User user, String note) {
		content.getService(WorkflowService.class).assign(user, note);
	}
	
	protected void setAsPending(Task task, Content content, WorkflowContext context) {
		content.getService(WorkflowService.class).setAsPending(task, context);
	}
}
