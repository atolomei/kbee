package com.novamens.kbee.security.acl;


import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Enumeration;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.JoinColumn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Indexable;
import com.novamens.kbee.security.KbeePrincipal;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeArea;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;


/**
 * select * from kgroup G, principal P where P.id=G.id and P.domain_id in (select id from domain where name like 'winn%') order by name 
 * 
 * 

update kgroup set areacode = 'workflow' where 
(name = 'mytasks' or
name= 'monitor' or
name='mytasks-bulk-actions' or
name='pending-tasks' )
and  
id in (select G.id from kgroup G, principal P where G.id=P.id and P.domain_id in (select id from domain where name like 'winn%'))
 * 
 *
 */
@Entity
@Table(name = "KGROUP")
public class KbeeGroup extends KbeePrincipal implements Group, Indexable {
		
	@SuppressWarnings("unused")
	static private Logger logger = LogManager.getLogger(KbeeGroup.class.getName());
		
	@ManyToMany(fetch = FetchType.LAZY, targetEntity=KbeePrincipal.class)
	@JoinTable(name = "KGROUPMEMBER", 
		joinColumns = {	@JoinColumn(name = "KGROUP", nullable = false, updatable = false) }, 
			inverseJoinColumns = { @JoinColumn(name = "PRINCIPAL", nullable = false, updatable=false) })
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
	Set<Principal> members = new HashSet<Principal>();
	
	
	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;
	
	@Column(name = "derived")
	private boolean derived;
	
	@Column(name = "canonical")
	private boolean canonical = false;

	@Column(name = "enabled")
	private boolean enabled = true;
	
	@Column(name = "onlyportal")
	private boolean  onlyportal = false;
	
	@Column(name = "onlydomainkbee")
	private boolean  onlydomainkbee = false;
	
	@Column(name = "onlyinternaluse")
	private boolean  onlyinternaluse = false;
	
	@Column(name = "areacode")
	private String areacode;

	@ManyToMany(fetch = FetchType.LAZY, targetEntity=KbeeGroup.class)
	@JoinTable(name = "KGROUPMEMBER", 
		joinColumns = {	@JoinColumn(name = "PRINCIPAL", nullable = false, updatable = false) }, 
			inverseJoinColumns = { @JoinColumn(name = "KGROUP", nullable = false, updatable = false) })
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
	Set<Group> groups = new HashSet<Group>();
	
	@Transient
	private String _areacode;
	
	@Transient
	private String _area=null;
	
	
	public KbeeGroup() {
		setAreaCode("security");
	}
	
	public String getName(){ 
		return name;
	}
	
	public void setName(String name) { 
		this.name = name;
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled=enabled;
	}
	
	public KbeeArea getArea() {
		return KbeeArea.getAreaByCode(getAreaCode());
	}
	
	public void  setArea(KbeeArea area) {
		setAreaCode(area.getCode());
	}	
	
	/**
	 * 
	 */
	@Override
	public String getAreaCode() {
	if (_areacode!=null)
			return _areacode;
		return areacode;
	}
	
	public void  setAreaCode(String code) {
		this.areacode = code;
	}
	
	@Override
	public String getDisplayName() { 
		return getDisplayName(Locale.getDefault());
	}
	
	@Override
	public String getDisplayName(Locale locale) { 
		
		if (!isCanonical())
			return name;
		
		if (name==null)
			return null;
		
		// Canonical Groups are the KbeeRole enum, so the resources are taken
		ResourceBundle res = ResourceBundle.getBundle(KbeeGlobalRole.class.getName(), locale);
		
		try {
			if ( res.getString(name) !=null)
				return res.getString(name);
			else
				return name;
		} 
		catch (MissingResourceException e) {
			return name;
		}
	}
	
	@Override
	public String getDescription(){ 
		return description;
	}
	
	@Override
	public void setDescription(String description) { 
		this.description = description;
	}
	
	@Override
	public void setDerived( boolean derived) {
		this.derived=derived;
	}
	
	@Override
	public boolean isDerived() {
		return this.derived;
	}
	
	public Set<Principal> getMembers() {
		return members;
	}
	
	@Override
	public int numMembers() {
		return getMembers().size();
	}

	public boolean isCanonical() { 
		return this.canonical;
	}

	public void setCanonical(boolean canonical) { 
		this.canonical=canonical;
	}
	
	public boolean addMember(Principal principal) {
		return members.add(principal);
	}
	
	public boolean removeMember(Principal principal) {
		return members.remove(principal);
	}
	
	public boolean isMember(Principal principal) {
		return members.contains(principal);
	}

	public Enumeration<? extends Principal> members() {
		return (new Enumeration<Principal>() {
			Iterator<Principal> members = getMembers().iterator();
			public boolean hasMoreElements() {
				return members.hasNext();
			}
			public Principal nextElement() {
				return members.next();
			}
		});
	}
	
	public Set<Group> getGroups() {
		return this.groups;
	}
	
	public void setGroups(Set<Group> groups) {
		boolean groupexist = false;
		while (!groupexist) {
			groupexist = true;
			for (Group g1 : getGroups()) {
				groupexist = false;
				for (Group g2 : groups) {
					if (g1.equals(g2)) {
						groupexist = true;
						break;
					}
				}
				if (!groupexist) {
					getGroups().remove(g1);
					break;
				}
			}
		}
		
		for (Group g1 : groups) {
			boolean e = false;
			for (Group g2 : getGroups()) {
				if (g1.equals(g2)) {
					e = true;
					break;
				}
			}
			if (!e) {
				getGroups().add(g1);
			}
		}
	}
	
	public <T extends ObjectService> T getService(Class<T> service) throws ServiceNotFoundException {
		return ServiceLocator.getService(this, service);
	}
	
	@Override
	public boolean isEmpty() {
		return this.members==null || this.members.isEmpty();
	}

	public boolean isOnlyPortal() {
		return this.onlyportal;
	}
	
	public boolean isOnlyDomainKbee() {
		return this.onlydomainkbee;
	}
	
	public boolean isOnlyInternalUse() {
		return this.onlyinternaluse;
	}
	
	public void setOnlyPortal(boolean b) {
		this.onlyportal=b;
	}
	
	public void setOnlyDomainKbee(boolean b) {
		this.onlydomainkbee  =b;
	}
	
	public void setOnlyInternalUse(boolean b) {
		this.onlyinternaluse = b;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object==null || !(object instanceof KbeeGroup) )
			return false;
		if (this==object) return true;
		return getId()!=null && getId().equals(((KbeeGroup)object).getId());
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
		return service.timeElapsed(date, ZoneId.of(zid), locale, DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
		
	}
}
