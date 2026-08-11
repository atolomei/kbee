package com.novamens.kbee.content.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.service.ContentService;
import com.novamens.content.workflow.EndCondition;
import com.novamens.content.workflow.WorkflowDao;
import com.novamens.content.workflow.WorkflowRule;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.RoleInProcess;
import com.novamens.workflow.RouterType;
import com.novamens.workflow.WorkflowContext;

import kbee.util.logging.Logger;

import com.novamens.workflow.Task;

public abstract class KbeeStatesMachine implements StatesMachine {
	
	private static Logger logger = Logger.getLogger(StatesMachine.class.getName());
	
	public Map<RoleInProcess, List<Principal>> getRoles(WorkflowContext context) {
		Map<RoleInProcess, List<Principal>> roles = new HashMap<RoleInProcess, List<Principal>>();
		
		KbeeContext kbeecontext = (KbeeContext)context;
		KbeeProcedure procedure = (KbeeProcedure)kbeecontext.getProcedure();
		
		for (RoleInProcess role : procedure.getRoles()) {
			List<Principal> principals = new ArrayList<Principal>();
			for (Task task : procedure.getTasks()) {
				if (task.getRole()!=null && task.getRole().getName().equals(role.getName())) {
					for (Principal principal : task.getTrigger().getEnabledPrincipals(context)) {
						if (!principals.contains(principal)) {
							principals.add(principal);
						}	
					}
				}
			}
			roles.put(role, principals);
		}

		return roles;
	}

	protected void checkin(Content content) {
		content.getService(ContentService.class).checkin();
	}
	
	protected void dropcheckout(Content content) {
		content.getService(ContentService.class).dropCheckout();
	}

	protected WorkflowRule getRule(String event, WorkflowContext context) {
		KbeeContext kbeecontext = (KbeeContext)context;
		
		Task actualtask  = kbeecontext.getTask();
		if (actualtask!=null) {
			for (com.novamens.content.workflow.EndCondition condition : ((KbeeTask)actualtask).getEndConditions()) {
				if (event.equals(condition.getEvent())) {
					return condition.getRule();
				}
			}
		}
		
		return null;
	}
	
	protected RouterType getRouter(KbeeProcedure procedure, String event) {
		for (Task task : procedure.getTasks()) {
			if (task instanceof KbeeTask) {
				for (EndCondition endcondition : ((KbeeTask)task).getEndConditions()) {
					if (event.equals(endcondition.getEvent()) && endcondition instanceof ManualEndCondition) {
						return ((ManualEndCondition)endcondition).getRouter();
					}
				}
			}
		}	
		return null;
	}
	
	protected ManualEndCondition getTransition(Procedure procedure, String event) {
		for (Task task : procedure.getTasks()) {
			if (task instanceof KbeeTask) {
				if (((KbeeTask)task).getEndConditions()!=null)
				for (EndCondition endcondition : ((KbeeTask)task).getEndConditions()) {
					if (event.equals(endcondition.getEvent()) && endcondition instanceof ManualEndCondition) {
						return (ManualEndCondition)endcondition;
					}
				}
			}
		}	
		return null;
	}
	
	protected boolean precondition(Task task, KbeeContext context) {
		try {
			return task.precondition(context);
		}
		catch (KbeeRuntimeException e) {
			logger.error(e);
			return false;
		}
	}
	
	protected WorkflowDao getWorkflowDao() {
		return (WorkflowDao)ServiceLocator.getService(BeansService.class).getBean("WorkflowDao");
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected User getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
