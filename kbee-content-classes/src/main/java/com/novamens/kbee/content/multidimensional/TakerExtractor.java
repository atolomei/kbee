package com.novamens.kbee.content.multidimensional;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.Permission;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Task;
import com.novamens.workflow.UserTrigger;
import com.novamens.workflow.WorkflowContext;

public class TakerExtractor implements Extractor {
	private long workflowuser = 0;
	
	private static final String Workflow_User = "workflow";

	public Object extract(Object object) throws IndexerException  {
		Assert.isInstanceOf(Content.class, object);
		
		Content content = (Content)object;
		
		if (content.getWorkspace()==null || !content.getWorkspace().equals(getWorkflowUser(content.getDomain()))) {
			return null;
		}
		
		List<String> takersids = new ArrayList<String>();
		List<Principal> takers = new ArrayList<Principal>();
		
		WorkflowContext workflowcontext = content.getService(WorkflowService.class).getContext();
		
		if (workflowcontext.getTime()!=null)
			return null;
		
		Task task = workflowcontext.getTask();
		
		if (task==null || !(task.getTrigger() instanceof UserTrigger))
			return null;
		
		Permission takepermission = ((UserTrigger)task.getTrigger()).getManualPermission();
		
		if (takepermission==null)
			return null;

		Acl acl = ServiceLocator.getService(ContentSystemSecurityService.class).getAcl((Content)object);
		
		for (AclEntry entry : ((KbeeAcl)acl).getEntries()) {
			if (entry.checkPermission(takepermission) && !entry.isNegative()) {
				takers.add((Principal)entry.getPrincipal());
			}
		}
		
		for (AclEntry entry : ((KbeeAcl)acl).getEntries()) {
			if (entry.checkPermission(takepermission) && entry.isNegative()) {
				Principal principal = (Principal)entry.getPrincipal();
				if (principal instanceof Group) {
					Group group = (Group)principal;
					for (Principal writer : takers) {
						if (group.isMember(writer)) {
							takers.remove(writer);
							break;
						}
					}
				}
			}
		}
		
		for (Principal taker : takers) {
			takersids.add(String.valueOf((Long)taker.getId()));
		}
		
		return takersids;
	}
	
	private long getWorkflowUser(Domain domain) {
		if (workflowuser == 0) {
			User user = ServiceLocator.getService(SecurityService.class).findUserByUsername(Workflow_User+"@"+domain.getName());
			workflowuser = user!=null ? (long)user.getId() : -1;
		}
		return workflowuser;
	}
}
