package com.novamens.kbee.content.security;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.model.ObjectId;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.Proxy;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.ServiceLocator;

public class PrincipalProxy implements Principal, Proxy<Principal> {
	private ObjectId id;
	private String displayName;
	private String type;
	
	static private Logger logger = LogManager.getLogger(PrincipalProxy.class.getName());
	
	public PrincipalProxy(ObjectId id) {
		this.id = id;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public void setDisplayName(String value) {
		this.displayName = value;
	}
	
	public Serializable getId() {
		return null;
	}
	
	public String getName() {
		return null;
	}
	
	public Set<Group> getGroups() {
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
	
	public Principal getObject() {
		Principal principal = null;
		try {
			if (type.equals("group")) {
				principal = (Principal)getContentDao().findObjectById(id);
			}
			else {
				Person person = (Person)getContentDao().findObjectById(id);
				principal = person.getProfile(UserProfile.class).getUser();
				((User)principal).getFirstLastName();
			}
		}
		catch (ContentMgmtException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return principal;
	}
	
	public ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	@Override
	public void setDefaultAudit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getLastModifiedOffsetDateTimeColloquial() {
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
	public String getLastModifiedOffsetDateTimeColloquial(String css) {
		// TODO Auto-generated method stub
		return null;
	}
}
