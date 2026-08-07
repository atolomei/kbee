package com.novamens.kbee.security.acl;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.kbee.security.KbeePrincipal;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.Identifiable;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.ServiceLocator;

public class KbeeGroupProxy implements Group, Identifiable, DomainObject {
	 
	private Long id;
	private String name;
	private String description;
	
	private transient Set<Long> membersset = null;
	
	private SessionFactory sessionFactory;
	
	public KbeeGroupProxy(Group group) {
		this.id = ((KbeeGroup)group).getId();
		this.name = group.getName();
		this.description=group.getDescription();
	}
	
	public Serializable getId() {
		return id;
	}
	
	public String getName(){ 
		return name;
	}
	
	public void setName(String name) {
		getGroup().setName(name);
	}
	
	public boolean addMember(Principal principal) {
		return getGroup().addMember(principal);
	}
	
	public boolean removeMember(Principal principal) {
		return getGroup().removeMember(principal);
	}
	
	public Set<Principal> getMembers() {
		return ((KbeeGroup)getGroup()).getMembers();
	}
	
	public boolean isMember(Principal principal) {
		if (membersset == null) {
			synchronized (this) {
				if (membersset == null) {
					membersset = new HashSet<Long>();
					for (Principal member : getMembers()) {
						membersset.add(((KbeePrincipal)member).getId());
					}	
				}
			}
		}	
		boolean member = membersset.contains(((com.novamens.security.Principal)principal).getId());
		if (!member) {
			if (principal instanceof KbeeUser) {
				principal = (KbeeUser)getSessionFactory().getCurrentSession().load(KbeeUser.class, ((KbeeUser)principal).getId());
			}
			Set<Group> principalgroups = ((com.novamens.security.Principal)principal).getGroups();
			for (Group group : principalgroups)  {
				if (group.getName().equals(getName()) || isMember(group)) {
					member = true;
					break;
				}
			}
		}
		return member;
	}
	
	public Enumeration<? extends Principal> members() {
		return ((KbeeGroup)getGroup()).members();
	}
	
	public Set<Group> getGroups() {
		return ((KbeeGroup)getGroup()).getGroups();
	}
	
	public void setGroups(Set<Group> groups) {
		((KbeeGroup)getGroup()).setGroups(groups);
	}
	
	public Domain getDomain() {
		return ((KbeeGroup)getGroup()).getDomain();
	}
	
	public void setDomain(Domain domain) {
		((KbeeGroup)getGroup()).setDomain(domain);
	}

	private SessionFactory getSessionFactory() {
		if (sessionFactory == null)
			sessionFactory = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
		return sessionFactory;
	}
	
	public Group getGroup() {
		return (KbeeGroup)getSessionFactory().getCurrentSession().load(KbeeGroup.class, id);
	}

	@Override
	public boolean isCanonical() {
		return getGroup().isCanonical();
	}

	public boolean isEmpty() {
		return getMembers()==null || getMembers().isEmpty();
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description=description;
	}

	@Override
	public User getLastModifiedUser() {
		return ((KbeeGroup)getGroup()).getLastModifiedUser();
	}

	@Override
	public void setLastModifiedUser(User lastModifiedUser) {
		((KbeeGroup)getGroup()).setLastModifiedUser(lastModifiedUser);
	}

	
	@Override
	public OffsetDateTime getLastModifiedOffsetDateTime() {
		return ((KbeeGroup)getGroup()).getLastModifiedOffsetDateTime();
	}

	@Override
	public void setLastModifiedOffsetDateTime(OffsetDateTime lastModifiedDate) {
		((KbeeGroup)getGroup()).setLastModifiedOffsetDateTime(lastModifiedDate);
	}
	
	@Override
	public OffsetDateTime getCreationOffsetDateTime() {
		return ((KbeeGroup)getGroup()).getCreationOffsetDateTime();
	}

	@Override
	public void setCreationOffsetDateTime(OffsetDateTime date) {
		((KbeeGroup)getGroup()).setCreationOffsetDateTime(date);
	}

	@Override
	public String getDisplayName() {
		return getName();
	}

	@Override
	public int numMembers() {
		return 	((KbeeGroup)getGroup()).numMembers();
	}

	@Override
	public void setDerived(boolean derived) {
		((KbeeGroup)getGroup()).setDerived(derived);
	}

	@Override
	public boolean isDerived() {
		return ((KbeeGroup)getGroup()).isDerived();
	}

	@Override
	public void setDefaultAudit() {
	}

	@Override
	public boolean isEnabled() {
		return getGroup()!=null?getGroup().isEnabled():false;
	}

	@Override
	public void setEnabled(boolean enabled) {
		((KbeeGroup)getGroup()).setEnabled(enabled);
	}

	@Override
	public boolean isOnlyPortal() {
		return false;
	}

	@Override
	public boolean isOnlyDomainKbee() {
		return false;
	}

	@Override
	public boolean isOnlyInternalUse() {
		return false;
	}

	@Override
	public void setOnlyPortal(boolean b) {
	}

	@Override
	public void setOnlyDomainKbee(boolean b) {
	}

	@Override
	public void setOnlyInternalUse(boolean b) {
	}

	@Override
	public String getAreaCode() {
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
	public String getLastModifiedOffsetDateTimeColloquial(String css) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDisplayName(Locale locale) {
		return getDisplayName();
	}
}
