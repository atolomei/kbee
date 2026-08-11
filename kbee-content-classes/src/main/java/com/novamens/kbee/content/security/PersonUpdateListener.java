package com.novamens.kbee.content.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.EntityMember;
import com.novamens.content.model.PersonMember;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.security.RolesService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.event.AppUpdateEvent;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.content.dao.Proxy;
import com.novamens.kbee.content.user.KbeeUserRole;
import com.novamens.service.ServiceLocator;

public class PersonUpdateListener implements EventListener {
	
	public boolean listen(Event event) {
		return ((event instanceof AppUpdateEvent) && 
				event.getObject() instanceof PersonMember &&
				DataSetType.USER.equals(
					((PersonMember)event.getObject()).getDataSet().getDataSetType()));
	}
	
	public void onEvent(Event event) {
		
		
		PersonMember person = (PersonMember)event.getObject();
		
		UserProfile userprofile = person.getProfile(UserProfile.class);
		
		boolean update = false;
		
		List<UserRole> userRoles = new ArrayList<>(); 
		userRoles.addAll(userprofile.getRoles());
		
		List<UserRole> defaultAssignedRoles = new ArrayList<>();
		for (UserRole userRole : userRoles) {
			if (userRole.getRole().isDefault()) {
				defaultAssignedRoles.add(userRole);
			}
		}
		
		List<UserRole> defaultEfectiveRoles = new ArrayList<>();
		for (Role role : getSecurityDao().getRoles(person.getDomain())) {
			if (role.isDefault() && role.isEntity()) {
				EntityRole entityRole = (EntityRole)Proxy.Unproxy(role);
				Classifier classifier = entityRole.getClassifier();
				EntityMember entity = getEntity(person, classifier);
				if (entity!=null) {
					KbeeUserRole userRole = new KbeeUserRole(role, userprofile.getUser(), entity);
					defaultEfectiveRoles.add(userRole);
				}
			}
		}
		
		for (UserRole efectiveRole : defaultEfectiveRoles) {
			boolean found = false;
			for (UserRole defaultAssignedRole : defaultAssignedRoles) {
				if (defaultAssignedRole.getRole().equals(efectiveRole.getRole())) {
					if (defaultAssignedRole.getEntity().equals(efectiveRole.getEntity())) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				update = true;
				userRoles.add(efectiveRole);
			}
		}
		
		for (UserRole defaultAssignedRole : defaultAssignedRoles) {
			for (UserRole efectiveRole : defaultEfectiveRoles) {
				boolean found = false;
				if (defaultAssignedRole.getRole().equals(efectiveRole.getRole())) {
					if (defaultAssignedRole.getEntity().equals(efectiveRole.getEntity())) {
						found = true;
					}
				}
				if (!found) {
					for (UserRole userRole : userRoles) {
						if (defaultAssignedRole.getRole().equals(userRole.getRole())) {
							if (defaultAssignedRole.getEntity().equals(userRole.getEntity())) {
								userRoles.remove(userRole);
								update = true;
								break;
							}
						}
					}	
				}
			}
		}
		
		if (update) {
			person.getService(RolesService.class).update(userRoles);
		}
	}
	
	private EntityMember getEntity(PersonMember person, Classifier classifier) {
		return getEntity(person, classifier, new HashSet<DataSetMember>());
	}
	
	private EntityMember getEntity(DataSetMember member, Classifier classifier, Set<DataSetMember> recursive) {
		EntityMember entity = null;
		if (recursive.contains(member)) {
			return null;
		}
		for (Classification classification : member.getClassification()) {
			if (classification!=null && classifier.equals(classification.getClassifier())) {
				if (entity==null) {
					entity = (EntityMember)classification.getDataSetMember();
				}
				else {
					return null;
				}
			}
		}
		if (entity==null) {
			recursive.add(member);
			for (Classification classification : member.getClassification()) {
				if (classification!=null) {
					entity = getEntity(classification.getDataSetMember(), classifier, recursive);
				}
			}	
		}
		return entity;
	}
	
	private SessionFactory getSessionFactory() {
		return (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
	}
	
	private ContentSecurityDao getSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
}
