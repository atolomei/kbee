package com.novamens.kbee.content.security;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import com.novamens.beans.BeansService;
import com.novamens.dao.SecurityDao;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.ServiceLocator;

public class GroupProxy implements Group, Serializable {
	private static final long serialVersionUID = 1L;
	
	private long id;
	private String displayName;
	
	public GroupProxy(Group group) {
		id = (long)group.getId();
		displayName = group.getDisplayName();
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public Serializable getId() {
		return id;
	}
	
	public boolean isEnabled() {
		return getGroup()!=null?getGroup().isEnabled():false;
	}
	
	public String getName() {
		return null;
	}
	
	public Date getLastModifiedDate() {
		return null;
	}
	
	public void setLastModifiedDate(Date lastModifiedDate) {
		
	}
	
	public Date getCreationDate() {
		return null;
	}
	
	public void setCreationDate(Date lastModifiedDate) {
		
	}
	
	public OffsetDateTime getCreationOffsetDateTime() {
		return null;
	}
	
	public void setCreationOffsetDateTime(OffsetDateTime lastModifiedOffsetDate) {
		
	}
	
	public OffsetDateTime getLastModifiedOffsetDateTime() {
		return null;
	}
	
	public void setLastModifiedOffsetDateTime(OffsetDateTime lastModifiedOffsetDate) {
		
	}
	
	public User getLastModifiedUser() {
		return null;
	}
	
	public void setLastModifiedUser(User lastModifiedUser) {
		
	}
	
	public void setName(String name) {
	}
	
	public Set<Group> getGroups() {
		return null;
	}
	
	public void setGroups(Set<Group> groups) {
		
	}
	
	public boolean isCanonical() {
		return false;
	}
	
	public boolean isEmpty() {
		return false;
	}
	
	public  String getDescription() {
		return null;
	}
	
	public void setDescription(String description) {
	}
	
	public int numMembers() {
		return 0;
	}
	
	public void setDerived(boolean derived) {
	}
	
	public boolean isDerived() {
		return false;
	}
	
	public boolean addMember(Principal arg0) {
		return false;
	}
	
	public boolean removeMember(Principal arg0) {
		return false;
	}
	
	public boolean isMember(Principal arg0) {
		Group group = getSecurityDao().findGroupById(getId());
		if (group!=null) return group.isMember(arg0);
		return false;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public java.util.Enumeration members() {
		return null;
	}

	@Override
	public void setDefaultAudit() {
		// TODO Auto-generated method stub
	}
	
	public Group getGroup() {
		return getSecurityDao().findGroupById(getId());
	}
	
	private SecurityDao  getSecurityDao() {
		return	(SecurityDao)ServiceLocator.getService(BeansService.class).getBean("securityDao");
	}

	@Override
	public void setEnabled(boolean enabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isOnlyPortal() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOnlyDomainKbee() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOnlyInternalUse() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setOnlyPortal(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOnlyDomainKbee(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOnlyInternalUse(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getAreaCode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLastModifiedOffsetDateTimeColloquial(String css) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCreationOffsetDateTimeColloquial() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public AuditSet getAuditSet() {
		return AuditSet.SECURITY;
	}

	@Override
	public String getDisplayName(Locale locale) {
		return getDisplayName();
	}
}
