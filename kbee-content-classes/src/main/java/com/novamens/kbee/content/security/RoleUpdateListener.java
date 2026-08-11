package com.novamens.kbee.content.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import com.novamens.content.properties.ObjectPropertyService;
import com.novamens.content.security.Role;
import com.novamens.event.AppUpdateEvent;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.hibernate.event.HibernateUpdateEvent;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;

public class RoleUpdateListener implements EventListener {
	
	public boolean listen(Event event) {
		return ((event instanceof AppUpdateEvent) && event.getObject() instanceof Role);
	}
	
	public void onEvent(Event event) {
		try {
			if (groupsUpdated((KbeeAbstractRole)event.getObject())) {
				ServiceLocator.getService(SchedulerService.class).enqueue(new UpdateRoleGroupsCommandServiceRequest((KbeeAbstractRole)event.getObject()));
				ServiceLocator.getService(SchedulerService.class).enqueue(new UpdateRoleCommandServiceRequest((KbeeAbstractRole)event.getObject()));	
			}
			else
			if (permissionsUpdated((HibernateUpdateEvent)event) || conditionUpdated((HibernateUpdateEvent)event) || nameUpdated((HibernateUpdateEvent)event)) {
				ServiceLocator.getService(SchedulerService.class).enqueue(new UpdateRoleCommandServiceRequest((KbeeAbstractRole)event.getObject()));	
			}
		}
		catch(SchedulerException e) {
			throw new RuntimeException(e);
		}
	}
	
	private boolean groupsUpdated(KbeeAbstractRole role) {
		List<Long> previuosgroups = getPreviuosGroups(role);
		Set<Group> actualgroups = role.getGroups();
		if (actualgroups.size()!=previuosgroups.size())
			return true;
		for (Group group : actualgroups) {
			boolean found = false;
			for (Long groupid : previuosgroups) {
				if (groupid.equals(group.getId())) {
					found = true;
					break;
				}
			}
			if (!found)
				return true;
		}
		return false;
	}
	
	private boolean permissionsUpdated(HibernateUpdateEvent event) {
		int i = 0;
		for (String propertyName : event.getPropertyNames()) {
			if ("permissionsvalue".equals(propertyName) || "negativepermissionsvalue".equals(propertyName)) {
				if (event.getCurrentState()[i]!=null && !event.getCurrentState()[i].equals(event.getPreviousState()[i])) {
					return true;
				}
				else {
					i++;
				}
			}
			else {
				i++;
			}
		}	
		return false;
	}
	
	private boolean conditionUpdated(HibernateUpdateEvent event) {
		int i = 0;
		for (String propertyName : event.getPropertyNames()) {
			if ("condition".equals(propertyName)) {
				if (event.getCurrentState()[i]!=null && !event.getCurrentState()[i].equals(event.getPreviousState()[i])) {
					return true;
				}
				else {
					i++;
				}
			}
			else {
				i++;
			}
		}	
		return false;
	}
	
	private boolean nameUpdated(HibernateUpdateEvent event) {
		int i = 0;
		for (String propertyName : event.getPropertyNames()) {
			if ("name".equals(propertyName)) {
				if (event.getCurrentState()[i]!=null && !event.getCurrentState()[i].equals(event.getPreviousState()[i])) {
					return true;
				}
				else {
					i++;
				}
			}
			else {
				i++;
			}
		}	
		return false;
	}

	private List<Long> getPreviuosGroups(Role role) {
		List<Long> groups = new ArrayList<Long>();
		String rolesstring = (String)((KbeeAbstractRole)role).getService(ObjectPropertyService.class).getProperty("groups");
		if (rolesstring==null) return groups;
		StringTokenizer tokenizer = new StringTokenizer(rolesstring, ",");
		while (tokenizer.hasMoreTokens()) {
			String idstr = tokenizer.nextToken();
			try {
				groups.add(Long.valueOf(idstr.trim()));
			}
			catch (NumberFormatException e) {
			}
		}
		
		return groups;
	}
}
