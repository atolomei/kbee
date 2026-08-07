package com.novamens.kbee.security;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.audit.AuditSet;

public class ProxyUserPrincipal implements User, Serializable {
	private static final long serialVersionUID = 1L;
	Serializable id;
	String username;
	public ProxyUserPrincipal(Serializable id, String username) {
		this.id=id;
		this.username=username;
	}
	public String getUserName() {
		return this.username;
	}
	public String getName() {
		return username;
	}
	@Override
	public OffsetDateTime getCreationOffsetDateTime() {
		return null;
	}
	@Override
	public void setCreationOffsetDateTime(OffsetDateTime lastModifiedOffsetDate) {
	}
	@Override
	public OffsetDateTime getLastModifiedOffsetDateTime() {
		return null;
	}
	@Override
	public void setLastModifiedOffsetDateTime(OffsetDateTime lastModifiedOffsetDate) {
	}
	@Override
	public User getLastModifiedUser() {
		return null;
	}
	@Override
	public void setLastModifiedUser(User lastModifiedUser) {
	}
	@Override
	public void setDefaultAudit() {
	}
	@Override
	public Serializable getId() {
		return null;
	}
	@Override
	public String getDisplayName() {
		return null;
	}
	@Override
	public String getLastFirstName() {
		return null;
	}
	@Override
	public String getFirstLastName() {
		return null;
	}
	@Override
	public String getFirstName() {
		return null;
	}
	@Override
	public String getLasName() {
		return null;
	}
	@Override
	public String getPassword() {
		return null;
	}
	@Override
	public boolean isEnabled() {
		return false;
	}
	
	@Override
	public boolean isActive() {
		return false;
	}
	@Override
	public void setActive(boolean enabled) {
	}
	@Override
	public void setGroups(Set<Group> groups) {
	}
	@Override
	public Set<Group> getGroups() {
		return null;
	}
	@Override
	public void addGroup(Group group) {
	}
	@Override
	public void removeGroup(Group group) {
	}
	@Override
	public boolean isMember(Group group) {
		return false;
	}
	@Override
	public void setLocale(String locale_str) {
	}
	@Override
	public void setLocale(Locale locale) {
	}
	@Override
	public Locale getLocale() {
		return null;
	}
	@Override
	public String getTimeZone() {
		return null;
	}
	@Override
	public void setTimeZone(String tz) {
	}
	@Override
	public boolean isCanonical() {
		return false;
	}
	@Override
	public List<Group> getStandardGroups() {
		return null;
	}
	@Override
	public ZoneId getZoneId() {
		return null;
	}
	@Override
	public String getLastModifiedOffsetDateTimeColloquial() {
		return null;
	}
	@Override
	public String getCreationOffsetDateTimeColloquial() {
		return null;
	}
	@Override
	public AuditSet getAuditSet() {
		return AuditSet.SECURITY;
	}
	@Override
	public void setStateEnabled() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean isArchived() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void setStateArchived() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setStateDeleted() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean isDeleted() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public String getLastModifiedOffsetDateTimeColloquial(String css) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public OffsetDateTime getValidityAccessDate() {
		// TODO Auto-generated method stub
		return null;
	}
}