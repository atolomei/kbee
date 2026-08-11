package com.novamens.portal.model.diagrammablesite;

import com.novamens.security.User;

@Deprecated
public class SiteUserRightsDEPRECATED {

	private User user;
	private int permissions;

	
	public SiteUserRightsDEPRECATED(User user, int permissions) {
		this.user=user;
		this.permissions=permissions;
	}
	
	public void setPermissions(int permissions) {
		this.permissions=permissions;
	}
	
	public void setUser(User user) {
		this.user=user;
	}
	
	public User getUser() {
		return user;
	}
	
	public int getPermissions() {
		return permissions;
	}
	
	public boolean hasRights(SitePermission permission) {
		return ((permissions & permission.getId())!=0);
	}
	
}
