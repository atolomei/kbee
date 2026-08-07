package com.novamens.kbee.security;


import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.novamens.dom.DomainObject;
import com.novamens.dom.Indexable;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.security.Auditable;
import com.novamens.security.Identifiable;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;

@Entity
@Table(name = "users")
public class KbeeUser extends KbeePrincipal implements User, Identifiable, DomainObject, Indexable, Auditable {

	@Column(name = "username")
	private String name;
	
	@Column(name = "password")
	private String password;
	
	@Column(name = "passwordLastModifiedDate")
	private OffsetDateTime passwordLastModifiedDate;

	@Column(name = "validityaccessdate")
	private OffsetDateTime validityaccessdate;

	
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "password_md5")
	private byte[] password_md5;
	
	@Column(name = "seed")
	private byte seed[];// = new byte[16];
	
	@Column(name = "enabled")
	private boolean x_enabled;
	
	@Column(name = "active")
	private boolean active;
	
	@Column(name = "firstname")
	private String firstname;
	
	@Column(name = "lastname")
	private String lastname;

	@Column(name = "uitheme")
	private String uitheme;
	
	@Column(name = "locale_str")
	private String locale_str;

	@Column(name = "canonical")
	private boolean canonical = false;

	@Column(name = "timezone")
	private String timezone;

	@Column(name = "STATE")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.dom.ObjectStateUserType")
	private ObjectState state;
	
	@ManyToMany(fetch = FetchType.LAZY, targetEntity=KbeeGroup.class, mappedBy="members")
	Set<Group> groups = new HashSet<Group>();
	
	@Transient
	private boolean gsorted=false;

	@Transient
	private String passwordClear;
	
	@Transient
	List<Group> sorted_groups;

	@Transient 
	Locale locale = null;

	@Transient
	List<Group> standard_groups = null;

	@Column(name = "is_billable")
	private boolean isBillable;
	
	public KbeeUser() {
		this(null);
	}

	
	public KbeeUser(String username) {
		this(username, null);
	}

	
	public KbeeUser(String username, String password) {
		this.name = username;
		
		if (this.password==null)
			setPassword(username);
		else
			setPassword(password);
		setStateEnabled();
	}
	
	
	public void setLocale(String locale_str) {
		this.locale_str=locale_str;
	}

	
	public void setLocale(Locale locale) {
		this.locale_str=locale.getLanguage();
	}

	
	public Locale getLocale() {
		
		if (this.locale==null) {

			if (this.locale_str==null)
				 this.locale=Locale.getDefault();
			 
			else if (this.locale_str.trim().toLowerCase().equals("en"))
				this.locale=Locale.ENGLISH;
			 
		    else if (locale_str.trim().toLowerCase().equals("es"))
				locale=new Locale("es");
			
		    else
				this.locale=Locale.getDefault();
		
		}
		return this.locale;
	}

	
	public String getUserName() {
		return name;
	}

	public String getName() {
		return name;
	}
	
	public String getPassword() {
		return password;
	}

	public String getDisplayName() {
		
		if (getLastName()!=null) {
			return getLastName() +  ((getFirstName()!=null && getFirstName().length()>0) ? (", "+getFirstName()):"");
		}	
		else {
			if (getFirstName()!=null)
				return getFirstName();
			else {
				if (getName()!=null)
					return getName();
				else
					return getId().toString();
			}
		}
	}

	
	public void setState(ObjectState state) {
		this.state = state;
		
		if (state==ObjectState.ENABLED) {
			this.x_enabled=true;
		}
		else {
			this.x_enabled=false;
		}
	}

	@Override
	public boolean isArchived() {return getState()==ObjectState.ARCHIVED;}

	@Override
	public void setStateArchived() {this.state=ObjectState.ARCHIVED;}

	@Override
	public void setStateDeleted() {	this.state=ObjectState.DELETED;	}

	@Override
	public boolean isDeleted() {return getState()==ObjectState.DELETED;}
	
	@Override
	public boolean isEnabled() {return getState()==ObjectState.ENABLED;}
	
	
	 
	/** 
	 * Canonical users are root@ , workflow@, and others
	 * They can't be deleted.
	 */
	public boolean isCanonical() {
		return canonical;
	}

	
	public void setCanonical(boolean value) {
		this.canonical = value;
	}

	
	public void setEmail(String email) {
		this.email = email;
	}

	
	public ObjectState getState() {
		return state;
	}

	// tanto las bajas como en la altas hay que hacerlas en el set local (groups) como
	// en el set del grupo (members) para que se actualicen correctamente la cache de 
	// ambas colecciones
	
	public void setGroups(Set<Group> groups) {
		boolean removed;
		do {
			removed = false;
			for (Group group : this.groups) {
				if (!groups.contains(group)) {
					group.removeMember(this);
					this.groups.remove(group);
					removed = true;
					break;
				}
			}
		} while (removed);
		
		for (Group group : groups) {
			if (!this.groups.contains(group)) {
				group.addMember(this);
				this.groups.add(group);
			}
		}
		
		standard_groups = null;
	}
	
	/**
	 * This method is needed by the RelationEditor.
	 * we should fix it and remove this method asap.
	 * 
	 * @param groups
	 */
	public void setStandardGroups(List<Group> groups) {
		Set<Group> groupsset = new HashSet<Group>();
		groupsset.addAll(groups);
		setStandardGroups(groupsset);
	}
	
		// tanto las bajas como en la altas hay que hacerlas en el set local (groups) como
		// en el set del grupo (members) para que se actualicen correctamente la cache de 
		// ambas colecciones
		
		public void setStandardGroups(Set<Group> groups) {
			boolean removed;
			do {
				removed = false;
				for (Group group : this.groups) {
					if (!group.isCanonical() && !groups.contains(group)) {
						group.removeMember(this);
						this.groups.remove(group);
						removed = true;
						break;
					}
				}
			} while (removed);
			
			for (Group group : groups) {
				if (!this.groups.contains(group)) {
					group.addMember(this);
					this.groups.add(group);
				}
			}
			
			standard_groups = null;
		}
	
	
	public void addGroup(Group group) {
		if (group==null) return;
		group.addMember(this);
		this.groups.add(group);
		standard_groups = null;
	}
	
	public void removeGroup(Group group) {
		group.removeMember(this);
		this.groups.remove(group);
		standard_groups = null;
	}
	
	public boolean isMember(Group group) {
		return getGroups().contains(group);
	}
	
	public Set<Group> getGroups() {
		return groups;
	}
	
	
	@Override
	public List<Group> getStandardGroups() {
		
		if (standard_groups==null) {
			
			synchronized (this) {
				
				if (standard_groups!=null)
					return standard_groups; 
				
				standard_groups = new ArrayList<Group>();
				
				for (Group group: getGroups()) {
					if (!group.isCanonical())
						standard_groups.add(group);
				}
				
				Collections.sort(standard_groups, new Comparator<Group>() {

					@Override
					public int compare(Group a, Group b) {
						try {
						if (a.getName()==null)
							return (b.getName()!=null?1:0);
						else if(b.getName()==null)
							return -1;
						return a.getName().compareToIgnoreCase(b.getName());
						} catch (Exception e) {
							//logger.error(e.getClass().getName(), e);
							return 0;
						}
					}
				});
			}
		}
		return standard_groups;
	}
	
	public byte[] getSeed() {
		return null;
	}

	public String getEmail() {
		return email;
	}

	public void setUserName(String username) {
		this.name=username;
	}

	public void setPassword(String password) {
		this.passwordClear=password;
		this.password = password==null ? null : encode(password);
	}
	
	public void setEncodedPassword(String password) {
		this.password = password;
	}
	
	public void setSeed(byte[] seed) {
		this.seed=seed;
	}
	
	public String getFirstName() {
		return firstname;
	}
	
	public void setFirstName(String name) {
		this.firstname=name;
	}
	
	public String getLastName() {
		return lastname; 
	}
	
	public void setLastName(String name) {
		this.lastname=name;
	}
	
	public <T extends ObjectService> T getService(Class<T> service) throws ServiceNotFoundException {
		return ServiceLocator.getService(this, service);
	}
		
	/** --------------------------------------------------------------
	 * year, month, day, hour

	public String getUserHash(Date date) {

		final StringBuilder st = new StringBuilder();
		
		st.append(this.getDisplayName());
		st.append(this.getId().toString());
		st.append("kbee");
		
		Calendar aCalendar = Calendar.getInstance();
		
		if (date!=null) {
			aCalendar.setTime(date);
			aCalendar.set(Calendar.MINUTE, 0);
			aCalendar.set(Calendar.SECOND, 0);
			aCalendar.set(Calendar.MILLISECOND, 0);
		}
		else {
			aCalendar.set(Calendar.MINUTE, 0);
			aCalendar.set(Calendar.SECOND, 0);
			aCalendar.set(Calendar.MILLISECOND, 0);
		}
		
		st.append(formatDateTime(aCalendar.getTime()));
		
		final byte[] defaultBytes = st.toString().getBytes();
		
		try {
			
			final MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.update(defaultBytes);
			final byte[] digest = algorithm.digest();
									
			String str = URLDecoder.decode(new String((new Base64()).encode(digest)), "UTF-8").replace(' ', '-');

			return str;
			
		} 
		catch (final NoSuchAlgorithmException e) {
			logger.error(e.getStackTrace());
			return null;
		} 
		catch (final UnsupportedEncodingException e) {
			logger.error(e.getStackTrace());
			return null;
		}
	}
	 */	

	public String encode(String value) {
		KbeeDelegatingPasswordEncoder enconder = new KbeeDelegatingPasswordEncoder();
		String encoded = enconder.encode(value);
		return encoded;
	}


	@Override
	public int compareTo(com.novamens.security.Principal o) {
		if (o instanceof KbeeUser)
			return getLastFirstName().compareToIgnoreCase(((KbeeUser) o).getLastFirstName());
		else
			return super.compareTo(o);
	}


	@Override
	public String getLastFirstName() {
		StringBuilder title = new StringBuilder();
		if (getLastName()!=null) 
				title.append(getLastName());
		if (getFirstName()!=null && getFirstName().length()>0) {
			if (title.length()>0)
				title.append(", ");
			title.append(getFirstName());
		}		
		return title.toString();
	}


	@Override
	public String getFirstLastName() {
		StringBuilder title = new StringBuilder();
		if (getFirstName()!=null) 
			title.append(getFirstName());
		if (getLastName()!=null) {
			if (title.length()>0)
				title.append(" ");
			title.append(getLastName());
		}		
		return title.toString();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof KbeeUser)) return false;
		return ((KbeeUser)object).getId().equals(getId());
	}
	
	public ZoneId getZoneId() {
		return ( getTimeZone()!=null?ZoneId.of(getTimeZone()) : ZoneId.systemDefault());
	}

	/**
	 * ZoneId id
	 */
	@Override
	public String getTimeZone() {
		return this.timezone;
	}

	@Override
	public void setTimeZone(String tz) {
		this.timezone=tz;
	}

	@Override
	public String getLasName() {
		return this.lastname;
	}

	@Override
	public void setDefaultAudit() {
		
	}

	public void setUitheme(String u) {
		this.uitheme=u;
	}
	
	public String getUitheme() {
		return this.uitheme;
	}

	public String getPasswordClear(){
		return this.passwordClear;
	}
	
	@Override
	public OffsetDateTime getPasswordLastModifiedDate() {
		return passwordLastModifiedDate;
	}

	public void setPasswordLastModifiedDate(OffsetDateTime passwordLastModifiedDate) {
		this.passwordLastModifiedDate = passwordLastModifiedDate;
	}
	
	public boolean isBillable() {
		return isBillable;
	}

	public OffsetDateTime getValidityAccessDate() {
		return validityaccessdate;
	}


	public void setValidityAccessDate(OffsetDateTime validityaccessdate) {
		this.validityaccessdate = validityaccessdate;
	}


	public void setBillable(boolean billable) {
		isBillable = billable;
	}
	
	@Override
	public AuditSet getAuditSet() {
		return AuditSet.SECURITY;
	}

	@Override
	public void setStateEnabled() {
			setState(ObjectState.ENABLED);
	}

	// Workflow active
	@Override
	public boolean isActive() {
		return this.active;
	}

	@Override
	public void setActive(boolean a) {
		this.active=a;
	}
}
