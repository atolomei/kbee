package com.novamens.kbee.content.multidimensional;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.Group;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class WriterExtractor implements Extractor {
	private long workflowuser = 0;
	
	private static final String Workflow_User = "workflow";

	public Object extract(Object object) throws IndexerException  {
		Assert.isInstanceOf(Content.class, object);
		
		Content content = (Content)object;
		
//		if (content.getWorkspace()==null || !content.getWorkspace().equals(getWorkflowUser(content.getDomain()))) {
//			return null;
//		}
		
		List<String> writersids = new ArrayList<String>();
		List<Principal> writers = new ArrayList<Principal>();

		Acl acl = ServiceLocator.getService(ContentSystemSecurityService.class).getAcl((Content)object);
		
		for (AclEntry entry : ((KbeeAcl)acl).getEntries()) {
			if (entry.checkPermission(KbeePermission.WRITE) && !entry.isNegative()) {
				writers.add((Principal)entry.getPrincipal());
			}
		}
		
		for (AclEntry entry : ((KbeeAcl)acl).getEntries()) {
			if (entry.checkPermission(KbeePermission.WRITE) && entry.isNegative()) {
				Principal principal = (Principal)entry.getPrincipal();
				if (principal instanceof Group) {
					Group group = (Group)principal;
					for (Principal writer : writers) {
						if (group.isMember(writer)) {
							writers.remove(writer);
							break;
						}
					}
				}
			}
		}
		
		for (Principal writer : writers) {
			writersids.add(String.valueOf((Long)writer.getId()));
		}
		
		return writersids;
	}
	
	private long getWorkflowUser(Domain domain) {
		if (workflowuser == 0) {
			User user = ServiceLocator.getService(SecurityService.class).findUserByUsername(Workflow_User+"@"+domain.getName());
			workflowuser = user!=null ? (long)user.getId() : -1;
		}
		return workflowuser;
	}
}
