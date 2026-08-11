package com.novamens.kbee.content.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.EntityMember;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.security.acl.Permission;
import com.novamens.security.audit.AuditSet;
import com.novamens.util.KbeeRuntimeException;

/**
 * <p>There are 2 classes of (Security) Roles:
 * 
 * {@link DomainRole}
 * They generate a Security Rule based on the static conditions defined in the Role  
 *  
 * {@link EntityRole}
 * They include a {@link DataSet} of type <i>Entity</i> that is used by the Security Rule created by the Role
 * <p>
 * 
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.INTEGER)
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "KB_ROLE")
public class KbeeAbstractRole extends AbstractObject implements Role  {
		
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeAbstractRole.class.getName());

	@Id 
	@SequenceGenerator(name = "role_sequencer", sequenceName = "entityid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_sequencer")
	@Column(name = "ID")
	private Long id;
	
	@Column(name = "NAME")
	private String name;
	
	@Column(name = "description")
	private String description;

	@Column(name = "TYPE", insertable=false, updatable=false)
	private int type;
	
	@Column(name = "condition")
	private String condition;
	
	@Column(name = "displaycondition")
	private String displayCondition;
	
	@Column(name = "permissions")
	private String permissionsvalue;
	
	@Column(name = "negative_permissions")
	private String negativepermissionsvalue;

	@Column(name = "api_enabled")
	private boolean api_enabled = false;
	
	@Column(name = "canonical")
	private boolean canonical;

	@Column(name = "alias")
	private String alias;
	
	@Column(name = "isdefault")
	private boolean isdefault;
	
	@Column(name = "principal_template")
	private String principalNameTemplate;
	

	@ManyToMany(fetch = FetchType.EAGER, targetEntity=KbeeGroup.class)
	@JoinTable(name = "KB_GROUP_ROLE", 
		joinColumns = {	@JoinColumn(name = "ROLE_ID", nullable = false, updatable = false) }, 
			inverseJoinColumns = { @JoinColumn(name = "GROUP_ID", nullable = false, updatable = false) })
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
	Set<Group> groups = new HashSet<Group>();
	
	@OneToOne(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeGroup.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="GROUP_ID")
	private Group group;
	

	@Transient
	private List<Permission> permissions;
	
	@Transient
	private List<Permission> negativepermissions;

	

	
	@Override
	public boolean isOnlyRootEditable() {
		if (getAlias()==null)
			return false;
		return getAlias().equals("super-user") || getAlias().equals("support") || getAlias().equals("superuser");
	}

	@Override
	public void setOnlyRootEditable(boolean onlyrooteditable) {
	}

	public void  setDefault(boolean b) {
		this.isdefault = b;
	}
	
	public void  setIsDefault(boolean b) {
		this.isdefault = b;
	}
	
	@Override
	public boolean isDefault() {
		return this.isdefault;
	}
	
	
	@Override
	public boolean getIsDefault() {
		return this.isdefault;
	}
	
	
	
	
	@Override
	public String getDisplayName() {
		if (this.name!=null)
			return this.name;
		return String.valueOf(this.id);
	}
	
	public Long getId()	{
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}
	
	public String getName()	{
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public String getCondition() {
		return condition;
	}
	
	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	public String getDisplayCondition() {
		return displayCondition;
	}
	
	public void setDisplayCondition(String condition) {
		this.displayCondition = condition;
	}
	
	public List<Permission> getPermissions() {
		if (this.permissions==null) 
			this.permissions = parsePermissions(this.permissionsvalue);
		return permissions;
	}
	
	public void setPermissions(List<Permission> permissions) {
		if (this.permissions==null) 
			this.permissions = new ArrayList<Permission>();
		this.permissions.clear();
		for(Permission permission : permissions) {
			this.permissions.add(permission);
		}
		this.permissionsvalue = serializePermissions(this.permissions);
	}
	
	public List<Permission> getNegativePermissions() {
		if (this.negativepermissions==null) 
			this.negativepermissions = parsePermissions(this.negativepermissionsvalue);
		return negativepermissions;
	}
	
	public void setNegativePermissions(List<Permission> permissions) {
		if (this.negativepermissions==null) 
			this.negativepermissions = new ArrayList<Permission>();
		this.negativepermissions.clear();
		for(Permission permission : permissions) {
			this.negativepermissions.add(permission);
		}
		this.negativepermissionsvalue = serializePermissions(this.negativepermissions);
	}
	
	public Set<Group> getGroups() {
		return this.groups;
	}
	
	public Set<Group> getGroups(EntityMember entity) {
		return getGroups();
	}
	
	@Override
	public boolean isCanonical() {
		return canonical;
	}

	@Override
	public void setCanonical(boolean canonical) {
		this.canonical = canonical;
	}
	
	public boolean isApiEnabled() {	
		return this.api_enabled;
	}
	
	public boolean getApiEnabled() {	
		return isApiEnabled();
	}
	
	public boolean getApienabled() { 
		return isApiEnabled();
	}
	
	public void setApiEnabled(boolean b) {	
		this.api_enabled=b;	
	}

	public boolean enableUserAdmin() {
		return false;
	}
	
	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	@Override
	public boolean isAdministrator() {
		for (Group group : getGroups()) {
			if (group.getName().equals(KbeeGlobalRole.DOMAIN_ADMIN.getId()) ||
				group.getName().equals(KbeeGlobalRole.SU.getId())) {
				return true;
			}
		}	
		return false;
	}
	
	public Group getGroup() {
		return group;
	}
	
	public void setGroup(Group group) {
		this.group = group;
	}
	
	public List<Group> getGroupsList() {
		List<Group> groupslist = new ArrayList<Group>();
		groupslist.addAll(groups);
		return groupslist;
	}
	
	public void setGroups(List<Group> groups) {
		Set<Group> groupsset = new HashSet<Group>();
		groupsset.addAll(groups);
		setGroups(groupsset);
	}
	
	public void addGroup(Group group) {
		getGroups().add(group);
	}
	
	public void removeGroup(Group group) {
		getGroups().remove(group);
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
	
	public void setRole(Person person) {
		UserProfile userprofile = person.getProfile(UserProfile.class);
		User user = userprofile.getUser();
		Set<Group> groups = new HashSet<Group>();
		groups.addAll(user.getGroups());
		boolean update = groups.addAll(getGroups());
		if (update)
		user.setGroups(groups);
	}
	
	public void setRole(Person person, EntityMember entity) {
		UserProfile userprofile = person.getProfile(UserProfile.class);
		User user = userprofile.getUser();
		Set<Group> groups = new HashSet<Group>();
		groups.addAll(user.getGroups());
		boolean update = groups.addAll(getGroups(entity));
		if (update)
		user.setGroups(groups);
	}
	
	public void removeRole(Person person) {
		UserProfile userprofile = person.getProfile(UserProfile.class);
		User user = userprofile.getUser();
		Set<Group> groups = new HashSet<Group>();
		groups.addAll(user.getGroups());
		boolean update = groups.removeAll(getGroups());
		for (UserRole userRole : userprofile.getRoles()) {
			if (groups.addAll(userRole.getGroups())) update = true;
		}
		if (update)
		user.setGroups(groups);
	}
	
	public void removeRole(Person person, EntityMember entity) {
		UserProfile userprofile = person.getProfile(UserProfile.class);
		User user = userprofile.getUser();
		Set<Group> groups = new HashSet<Group>();
		groups.addAll(user.getGroups());
		boolean update = groups.removeAll(getGroups(entity));
		for (UserRole userRole : userprofile.getRoles()) {
			if (!userRole.getRole().equals(this) || (entity!=null && !entity.equals(userRole.getEntity()))) {
				if (groups.addAll(userRole.getGroups())) 
					update = true;
			}
		}
		if (update)
		user.setGroups(groups);
	}
	
	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String str) {
		description=str;
	}

	public String getPrincipalNameTemplate() {
		return principalNameTemplate;
	}

	public void setPrincipalNameTemplate(String principalNameTemplate) {
		this.principalNameTemplate = principalNameTemplate;
	}

	@Override
	public String getRoleType() {
		return "General";
	} 
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof KbeeAbstractRole)) 
			return false;
		return ((KbeeAbstractRole)object).getId().equals(getId());
	}
	
	public boolean manage(DataSet dataset) {
		return false;
	}
	
	public AuditSet getAuditSet() {
		return AuditSet.SECURITY;
	}

	protected User getUser(Person person) {
		/**
		 * Scheduler is showing that sometimes (very rarely) the person is null
		 */
		if (person == null) {
			logger.error("person is null | " + this.getName() + " [ " + String.valueOf(this.getId()) + " ] ");
			throw new KbeeRuntimeException("person is null | " + this.getName() + " [ " + String.valueOf(this.getId()) + " ] ");
		}
		UserProfile userprofile = person.getProfile(UserProfile.class);
		
		if (userprofile==null) 
			return null;
		
		return userprofile.getUser();
	}
	
	private List<Permission> parsePermissions(String permissionsvalue) {
		List<Permission> permissions = new ArrayList<Permission>();
		if (permissionsvalue!=null) {
			StringTokenizer tokenizer = new StringTokenizer(permissionsvalue, ",");
			while (tokenizer.hasMoreTokens()) {
				permissions.add(KbeePermission.valueOf((tokenizer.nextToken().trim().toLowerCase())));
			}
		}
		return permissions;
	}
		
	private String serializePermissions(List<Permission> permissions) {
		boolean first = true;
		StringBuilder permissionsvalue = new StringBuilder();
		for (Permission permission : permissions) {
			if (!first) 
				permissionsvalue.append(", ");
			else
				first = false;
			permissionsvalue.append(permission.toString());
		}
		return permissionsvalue.toString();
	}

	

}
