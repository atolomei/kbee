package com.novamens.kbee.security.acl;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.util.Assert;

import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.Identifiable;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.Permission;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@Entity
@Table(name = "kb_acl")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
public class KbeeAcl implements Acl, Identifiable {
	
	@Id 
	@SequenceGenerator(name = "security_sequencer", sequenceName = "security_sequence", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "security_sequencer")
	@Column(name = "id")
	private Long id;
	
	@Column(name = "lastModifiedDate")
	private OffsetDateTime lastModifiedDate;
	
	@Column(name = "creationDate")
	private OffsetDateTime creationDate;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "lastModifiedUser")
	private User lastModifiedUser;
	
	@Column(name = "name")
	private String name;
	
	@OneToMany(orphanRemoval=true, cascade=CascadeType.ALL, targetEntity = KbeeAclEntry.class)
	@JoinColumn(name = "acl", nullable=false) 
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
	List<AclEntry> entries = new ArrayList<AclEntry>();
	

	public KbeeAcl() {
		
	}
	public Serializable getId() {
		return id;
	}
	
	public OffsetDateTime getLastModifiedOffsetDateTime() {
		return lastModifiedDate;
	}

	@Override
	public void setLastModifiedOffsetDateTime(OffsetDateTime lastModifiedOffsetDateTime) {
		this.lastModifiedDate = lastModifiedOffsetDateTime;
	}

	public User getLastModifiedUser() {
		return lastModifiedUser;
	}

	@Override
	public void setLastModifiedUser(User lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getDisplayName() {
		return name;
	}
	
	@Override
	public void setName(Principal caller, String name) throws SecurityException {
		this.name = name;
	}
	
	@Override
	public List<AclEntry> getEntries() {
		return entries;
	}
	
	public void clearEntries() {
		entries.clear();
	}
	
	@Override
	public Enumeration<AclEntry> entries() {
		return (new Enumeration<AclEntry>() {
			int size = entries.size();
			int cursor;
			public boolean hasMoreElements() {
				return (cursor < size);
			}
			public AclEntry nextElement() {
				return entries.get(cursor++);
			}
		});
	}
	
	public void merge(Principal caller, Acl acl) {
		Assert.isInstanceOf(KbeeAcl.class, acl);
		((KbeeAcl)acl).getId();
	    List<AclEntry> entriesToMerge =
            new ArrayList<>(((KbeeAcl) acl).getEntries());
		for (AclEntry entry1 : entriesToMerge) {
			entry1 = (AclEntry)entry1.clone();
			if ((com.novamens.security.Principal)entry1.getPrincipal()!=null) {
				AclEntry entry2 = null;
				AclEntry negative = null;
				for (AclEntry entry : getEntries()) {
					if (((com.novamens.security.Principal)entry1.getPrincipal()).getId().equals(((com.novamens.security.Principal)entry.getPrincipal()).getId())) {
						if (!entry1.isNegative() && entry.isNegative()) {
							negative = entry;
						}
						if (entry1.isNegative() && !entry.isNegative()) {
							entry2 = entry;
						}
						if (entry1.isNegative()==entry.isNegative()) {
							entry2 = entry;
						}
					}
				}
				if (entry2!=null) {
					((KbeeAclEntry)entry2).merge(entry1);
					if (entry1.isNegative() && !entry2.isNegative()) 
						addEntry(caller, entry1);
					if (((KbeeAclEntry)entry2).getPermissions().isEmpty())
						getEntries().remove(entry2);
				}
				else {
					if (negative!=null) {
						((KbeeAclEntry)entry1).merge(negative);
					}
					if (!((KbeeAclEntry)entry1).getPermissions().isEmpty())
						addEntry(caller, entry1);
				}
			}
		}
	}
	
	public boolean isOwner(Principal principal) {
		return false;
	}
	
	public boolean deleteOwner(Principal principal, Principal p2) {
		return false;
	}
	
	@Override
	public boolean removeEntry(Principal caller, AclEntry entry) {
		entries.remove(entry);
		return true;
	}
	
	@Override
	public boolean addEntry(Principal caller, AclEntry entry) {
		((KbeeAclEntry)entry).setAcl(this);
		this.entries.add(entry);
		return true;
	}
	
	//@Override
	public boolean addOwner(Principal caller, Principal principal) {
		return false;
	}
	
	public Enumeration<Permission> getPermissions(Principal arg0) {
		return null;
	}
	
	public boolean checkPermission(Principal principal, Permission permission) {
		
		getId();
	
		List<Principal> positives = new ArrayList<Principal>();
		List<Principal> negatives = new ArrayList<Principal>();
		
		for (AclEntry entry : getEntries()) {
			if ((entry.getPrincipal() instanceof Group && ((Group)entry.getPrincipal()).isMember(principal)) || (entry.getPrincipal() instanceof User && entry.getPrincipal().equals(principal))) {
				if (entry.checkPermission(permission)) {
					boolean found = true;
					while (found) {
						found = false;
						for (Principal negative : negatives) {
							if (negative instanceof Group && ((Group)negative).isMember(entry.getPrincipal())) {
								negatives.remove(negative);
								found = true;
								break;
							}
						}
					}
					found = true;
					while (found) {
						found = false;
						for (Principal positive : positives) {
							if (positive instanceof Group && ((Group)positive).isMember(entry.getPrincipal())) {
								positives.remove(positive);
								found = true;
								break;
							}
						}
					}
					if (entry.isNegative()) {
						found = false;
						for (Principal negative : negatives) {
							if (entry.getPrincipal() instanceof Group && ((Group)entry.getPrincipal()).isMember(negative)) {
								found = true;
								break;
							}
						}
						if (!found)
							negatives.add(entry.getPrincipal());
					}
					else {
						found = false;
						for (Principal positive : positives) {
							if (entry.getPrincipal() instanceof Group && ((Group)entry.getPrincipal()).isMember(positive)) {
								found = true;
								break;
							}
						}
						if (!found)
							positives.add(entry.getPrincipal());
					}
				};
			}
		 }
		 return !negatives.isEmpty() ? false : (!positives.isEmpty() ? true : false);
	 }
	
	@Override
	public OffsetDateTime getCreationOffsetDateTime() {
		return this.creationDate;
	}
	
	@Override
	public void setDefaultAudit() {
		this.creationDate=OffsetDateTime.now();
		this.lastModifiedDate=OffsetDateTime.now();
	}

	@Override
	public void setCreationOffsetDateTime(OffsetDateTime date) {
		this.creationDate=date;
		
	}
	
	@Override
	public String getLastModifiedOffsetDateTimeColloquial() {
		return getLastModifiedOffsetDateTimeColloquial("ago");
	}
	
	@Override
	public String getCreationOffsetDateTimeColloquial() {
		return getLastModifiedOffsetDateTimeColloquial(getCreationOffsetDateTime(), "ago");
	}
	
	public String getLastModifiedOffsetDateTimeColloquial(String classago) {
		return getLastModifiedOffsetDateTimeColloquial(getLastModifiedOffsetDateTime(), classago);
	}
	
	@Override
	public AuditSet getAuditSet() {
		return AuditSet.SECURITY;
	}
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private String getLastModifiedOffsetDateTimeColloquial(OffsetDateTime date, String classago) {

		if (date==null) 
			return "";
		
		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		User user = getSessionUser();
		
		String zid = null;
		if (user!=null)
			zid=service.getMapZoneIds().get(user.getTimeZone());
		if (zid==null)
			zid=ZoneId.systemDefault().getId();
		Locale locale = null;
		if (user!=null)
				locale=user.getLocale();
		else
			locale=Locale.getDefault();
		
		return service.timeElapsed(date, ZoneId.of(zid), locale, DateTimeService.DATE_COLlOQUIAL_AGO, classago);
		
	}

}
