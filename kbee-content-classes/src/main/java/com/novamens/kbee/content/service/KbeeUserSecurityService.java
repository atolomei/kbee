package com.novamens.kbee.content.service;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classification;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.content.security.Role;
import com.novamens.content.service.UserSecurityService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.security.User;

public class KbeeUserSecurityService implements UserSecurityService {
	
	private User user;
	private ContentDao contentDao;
	
	public KbeeUserSecurityService() {
	}
	
	public KbeeUserSecurityService(User user) {
		 this.user = user;
	}
	
	public User getUser() {
		return user;
	}
	
	public boolean isMember(String roleName) {
		return getRoles().stream().anyMatch(r -> r.getRole().getName().equalsIgnoreCase(roleName));
	}
	
	public boolean hasRole(String rolename, EntityMember entity) {
		return getRoles().stream().anyMatch(r -> r.getRole().getName().equalsIgnoreCase(rolename) &&
			r.getEntity().getId().equals(entity.getId()));
	}
	
	public List<UserRole> getRoles() {
		List<UserRole> roles;
		UserProfile profile = getContentDao().findUserProfileByUser(getUser());
		if (profile!=null) {
			roles = profile.getRoles();
		}
		else {
			roles = new ArrayList<UserRole>();
		}
		return roles;
	}
	
	public ContentDao getContentDao() {	
		return contentDao;
	} 
	
	public void setContentDao(ContentDao dao) {
		contentDao=dao;
	}
	
	protected boolean userBelongs(EntityMember entity) {
		UserProfile profile = getContentDao().findUserProfileByUser(getUser());
		if (profile!=null) {
			for (DataSetMember member : getContentDao().findMembersByEntity(profile.getPerson())) {
				for (Classification classification : member.getClassification()) {
					if (classification.getDataSetMember().equals(entity)) {
						return true;
					}
				}
			}
		}		
		return false;
	}
	
	protected Role reload(Role role) {
		return (Role)getContentDao().reload(role);
	}

	 
}
