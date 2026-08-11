package com.novamens.kbee.content.security;

import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Transaction;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.beans.BeansService;
import com.novamens.content.base.SecurityRule;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.model.EntityMember;
import com.novamens.content.security.DomainRole;
import com.novamens.dao.SecurityDao;
import com.novamens.hibernate.session.Session;
import com.novamens.kbee.security.acl.KbeeAcl;
import com.novamens.kbee.security.acl.KbeeAclEntry;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.logging.SecurityUpdateEvent;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.security.acl.Permission;
import com.novamens.service.ServiceLocator;

@Entity
@DiscriminatorValue(value="1") 	// DomainRole.TYPE;
public class KbeeDomainRole extends KbeeAbstractRole implements DomainRole {
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeDomainRole.class.getName());

	static Logger txLogger = LogManager.getLogger("TxLogger");	
	
	private static String Rule_Description = "Rule for %s role";
	
	// Cascade ALL means that the Security Rule is deleted when this relationship is removed
	//
	@OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeSecurityRule.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="SECURITYRULE_ID")
	private SecurityRule securityRule;
	

	
	
	@Override
	public void setRole(Person person) {
		Transaction transaction = Session.get().getTransaction();
		if (transaction!=null && transaction.isActive()) {
			if (getSecurityRule()==null) {
				createSecurityRule();
			}
			else {
				if (!getRuleCondition().equals(getSecurityRule().getCondition())) {
					getSecurityRule().setCondition(getRuleCondition());
				}
			}
			
			KbeeAcl acl = (KbeeAcl)getSecurityRule().getAcl();
			
			User user = getUser(person);
			if (user==null) return;
			
			Group group = getRoleGroup();
			
			boolean updated1 = updatePermissions(acl, group, user, getPermissions(), false);
			boolean updated2 = updatePermissions(acl, group, user, getNegativePermissions(), true);
	
			if (updated1 || updated2) {
				acl.setLastModifiedOffsetDateTime(OffsetDateTime.now());
				acl.setLastModifiedUser(getSessionUser());
				getSecurityRule().setLastModifiedOffsetDateTime(OffsetDateTime.now());
				getSecurityRule().setLastModifiedUser(getSessionUser());
				setLastModifiedOffsetDateTime(OffsetDateTime.now());
				txLogger.info(new SecurityUpdateEvent(getSecurityRule(), "Update permissions to "+user.getDisplayName()));
			}	
			
			try {
				ContentDao dao = (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
				dao.flush();
			}
			catch (Exception e) {
				logger.error(e);
				throw e;
			}
		}
		
		super.setRole(person, null);
	}
	
	@Override
	public void removeRole(Person person) {
		if (getSecurityRule()==null) {
			createSecurityRule();
		}	
		
		if (!getName().equals(getSecurityRule().getName())) {
			getSecurityRule().setName(getName());
		}
		
		User user = getUser(person);
		if (user==null) return;
		
		KbeeAcl acl = (KbeeAcl)getSecurityRule().getAcl();
		
		for (AclEntry entry : acl.getEntries()) {
			if (entry.getPrincipal().equals(user)) {
				acl.removeEntry(getSessionUser(), entry);
				txLogger.info(new SecurityUpdateEvent(getSecurityRule(), "Remove permissions to "+person.getDisplayName()));
				break;
			}
		}
		
		super.removeRole(person, null);
	}
	
	@Override
	public void setRole(Person person, EntityMember entity) {
		Assert.isNull(entity, "is domanin role");
		setRole(person);
	}
	
	@Override
	public void setCondition(String condition) {
		if (getSecurityRule()!=null) {
			getSecurityRule().setCondition(condition);
		}
		super.setCondition(condition);
	}
	
	public SecurityRule getSecurityRule() {
		return securityRule;
	}
	
	public void setSecurityRule(SecurityRule rule) {
		this.securityRule = rule; 
	}
	
	protected String getDomainCondition() {
		String condition = "domain("+getDomain().getId()+")";
		return condition;
	}
	
	protected boolean enable(KbeeGlobalRole globalrole) {
		for (Group group : getGroups()) {
			if (group.getName().equals(globalrole.getId())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Set<Group> getGroups(EntityMember entity) {
		Set<Group> groups = new HashSet<Group>(); 
		groups.addAll(getGroups());
		Group rolegroup = getGroup();
		if (rolegroup!=null) groups.add(rolegroup);
		return groups;
	}
	
	@Override
	public String getRoleType() {
		Locale locale = getSessionUser().getLocale();
		ResourceBundle res = ResourceBundle.getBundle(this.getClass().getName(), locale);
		return res.getString("type");
	}
	
	protected boolean updatePermissions(Acl acl, Group group, User user, List<Permission> permissions, boolean negative) {
		KbeeAcl kbeeacl = (KbeeAcl)acl;
		
		boolean updated = false;
		
		KbeeAclEntry userEntry = null;
		for (AclEntry entry : acl.getEntries()) {
			if (entry.getPrincipal().equals(user) && entry.isNegative()==negative) {
				userEntry = (KbeeAclEntry)entry;
				break;
	 		}
		}
		
		if (userEntry != null) {
			kbeeacl.removeEntry(getSessionUser(), userEntry);
			updated = true;
		}
		
		KbeeAclEntry groupEntry = null;
		for (AclEntry entry : acl.getEntries()) {
			if (entry.getPrincipal().equals(group) && entry.isNegative()==negative) {
				groupEntry = (KbeeAclEntry)entry;
				break;
			}
		}
		
		if (groupEntry == null) {
			if (!permissions.isEmpty()) {
				groupEntry = new KbeeAclEntry(acl, group, negative);
				kbeeacl.addEntry(getSessionUser(), groupEntry);
			}
		}
		else {
			if (permissions.isEmpty()) {
				kbeeacl.removeEntry(getSessionUser(), groupEntry);
				groupEntry = null;
				updated = true;
			}
		}
		
		if (groupEntry!=null) {
			if (!equals(groupEntry.getPermissions(), permissions)) {
				groupEntry.setPermissions(permissions);
				groupEntry.setPrincipal(group);
				updated = true;
			}
		}
		
		
		return updated;
	}
	
	protected void createSecurityRule() {
		
		KbeeSecurityRule rule = new KbeeSecurityRule();
		
		rule.setName(getName());
		
		rule.setDerived(true);
		rule.setDomain(getDomain());
		rule.setDescription(String.format(Rule_Description, getDisplayName()));
		rule.setCreationOffsetDateTime(OffsetDateTime.now());
		rule.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		rule.setLastModifiedUser(getSessionUser());
		rule.setType(SecurityRule.RULE_COLLOQUIAL_IQL);
		
		rule.setCondition(getRuleCondition());
		
		KbeeAcl acl = new KbeeAcl();
		acl.setCreationOffsetDateTime(OffsetDateTime.now());
		acl.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		acl.setLastModifiedUser(getSessionUser());
		rule.setAcl(acl);
		
		setSecurityRule(rule);
	}
	
	protected String getRuleCondition() {
		String condition = getCondition();
		
		if (condition==null || "".equals(condition)) {
			condition = getDomainCondition();
		}
		
		if (!enable(KbeeGlobalRole.MONITOR_AUDIT)) {
			condition += " and isHead(true)";
		}
		
		return condition;
	}
	
	protected Group getRoleGroup() {
		Group group;
		if (getGroup()==null) {
			group = createGroup();
			setGroup(group);
		}
		else {
			group = getGroup();
			if (!group.getName().equals(getGroupName())) {
				group.setName(getGroupName());
			}
			if (group.getDescription()==null || !group.getDescription().equals(getGroupDescription())) {
				group.setDescription(getGroupDescription());
			}
		}
		return group;
	}
	
	protected Group createGroup() {
		KbeeGroup group = new KbeeGroup();
		
		group.setDerived(true);
		group.setDescription(getGroupDescription());
		group.setDomain(getDomain());
		group.setCreationOffsetDateTime(OffsetDateTime.now());
		group.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		group.setLastModifiedUser(getSessionUser());
		
		group.setName(getGroupName());
		
		try {
			SecurityDao dao = (SecurityDao)ServiceLocator.getService(BeansService.class).getBean("securityDao");
			dao.save(group);
		}
		catch (Exception e) {
			logger.error(e);
			throw e;
		}
		
		return group;
	}
	
	protected String getGroupName() {
		String name = "";
		name += getName();
		return name;
	}
	
	protected String getGroupDescription() {
		Locale locale;
		if (getSessionUser()!=null)
			locale = getSessionUser().getLocale();
		else
			locale = Locale.getDefault();
		ResourceBundle res = ResourceBundle.getBundle(KbeeDomainRole.this.getClass().getName(), locale);
		String description = "";
		description = String.format(res.getString("rolegroup-description"),  getDisplayName());
		return description;
	}
	
	protected boolean equals(List<Permission> permissions1, List<Permission> permissions2) {
		if (permissions1.size()!=permissions2.size()) return false;
		for (Permission permission1 : permissions1) {
			boolean found = false;
			for (Permission permission2 : permissions2) {
				if (permission1.equals(permission2)) {
					found = true;
					break;
				}
			}	
			if (!found) {
				return false;
			}
		}
		return true;
	}
}
