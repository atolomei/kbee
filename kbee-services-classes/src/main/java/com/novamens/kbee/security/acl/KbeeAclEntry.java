package com.novamens.kbee.security.acl;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.novamens.kbee.security.KbeePrincipal;
import com.novamens.security.Principal;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.AclEntry;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.Permission;

@Entity
@Table(name = "kb_aclentry")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
public class KbeeAclEntry implements AclEntry {
	
	@Id
	@GenericGenerator(
		name = "aclentry_sequencer",
		strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			@Parameter(name = "sequence_name", value = "aclentry_sequence"),
			@Parameter(name = "increment_size", value = "50"),
			@Parameter(name = "optimizer", value = "hilo")
		}
	)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "aclentry_sequence")
	@Column(name = "id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeAcl.class, cascade=CascadeType.DETACH)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
	@JoinColumn(name="acl", insertable=false, updatable=false, nullable=false)
	private Acl acl;
	
	@ManyToOne(fetch = FetchType.EAGER, cascade=CascadeType.DETACH, targetEntity=KbeePrincipal.class)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
	@JoinColumn(name="principal")
	private Principal principal;
	
	@Column(name = "negative")
	private boolean isnegative = false;

	
	@Column(name = "permissions")
	private String permissionsvalue;
	
	@Transient
	private List<Permission> permissions;
	
	
	public KbeeAclEntry(KbeeAclEntry src) {
		principal=src.principal;
		isnegative=src.isnegative;
		this.permissionsvalue=src.permissionsvalue;
	}
	
	
	
	public KbeeAclEntry() {
	}
	
	public KbeeAclEntry(Acl acl, Principal principal, boolean isnegative) {
		setPrincipal(principal);
		
		if (isnegative) 
			setNegativePermissions();
	
	}
	
	public Acl getAcl() {
		return acl;
	}
	
	public void setAcl(Acl acl) {
		this.acl = acl;
	}
	
	@Override
	public Principal getPrincipal() {
		return principal;
	}
	
	@Override
	public boolean setPrincipal(Principal principal) {
		this.principal = principal;
		return true;
	}
	
	@Override
	public void setNegativePermissions() {
		isnegative = true;
	}
	
	@Override
	public boolean isNegative() {
		return isnegative;
	}
	
	public Long getId() {
		return id;
	}
	
	@Override
	public Enumeration<Permission> permissions() {
		if (permissions==null) parsePermissions();
		return (new Enumeration<Permission>() {
			int size = permissions.size();
			int cursor;
			public boolean hasMoreElements() {
				return (cursor < size);
			}
			public Permission nextElement() {
				return permissions.get(cursor++);
			}
		});
	}
	
	public List<Permission> getPermissions() {
		if (permissions==null) parsePermissions();
		return permissions;
	}
	
	@Override
	public boolean addPermission(Permission permission) {
		if (permissions==null) parsePermissions();
		boolean rc = permissions.add(permission);
		serializePermissions();
		return rc;
	}
	
	@Override
	public boolean removePermission(Permission permission) {
		if (permissions==null) parsePermissions();
		boolean rc = permissions.remove(permission);
		serializePermissions();
		return rc;
	}
	
	
	@Override
	public void setPermissions(List<com.novamens.security.acl.Permission> permissions) {
		if (this.permissions==null) parsePermissions();
		this.permissions.clear();
		for(Permission permission : permissions) {
			this.permissions.add(permission);
		}
		serializePermissions();
	}
	
	@Override
	public boolean checkPermission(Permission permission) {
		if (permissions==null) parsePermissions();
		return permissions.contains(permission);
	}
	
	public void merge(AclEntry entry ) {
		if (!((com.novamens.security.Principal)entry.getPrincipal()).getId().equals(((com.novamens.security.Principal)getPrincipal()).getId())) 
			return;
		for (Permission permission1 : ((KbeeAclEntry)entry).getPermissions()) {
			boolean found = false;
			for (Permission permission2 : getPermissions()) {
				if (permission1.equals(permission2)) {
					found = true;
					if (!isNegative() && entry.isNegative()) {
						removePermission(permission2);
						break;
					}
				}
			}
			if (!found) {
				if (entry.isNegative() == isNegative())
					addPermission(permission1);
			}
		}
	}
	
	@Override
	public Object clone() { 
		KbeeAclEntry clone;
		if (getPrincipal() instanceof Group && !(getPrincipal() instanceof KbeeGroupProxy)) 
			clone = new KbeeAclEntry(getAcl(), new KbeeGroupProxy((KbeeGroup)getPrincipal()), isNegative());
		else
			clone = new KbeeAclEntry(getAcl(), getPrincipal(), isNegative());
		clone.permissionsvalue = permissionsvalue;
		return clone;
	}
	
	private void parsePermissions() {
		this.permissions = new ArrayList<Permission>();
		if (permissionsvalue!=null) {
			StringTokenizer tokenizer = new StringTokenizer(permissionsvalue, ",");
			while (tokenizer.hasMoreTokens()) {
				permissions.add(KbeePermission.valueOf((tokenizer.nextToken().trim().toLowerCase())));
			}
		}
	}
	
			
	public void setPermissionsSerialized(String permissionsvalue) {
		this.permissionsvalue=permissionsvalue;
		parsePermissions();
	}
	
	public String getPermissionsSerialized() {
		return permissionsvalue;
	}
	
	private void serializePermissions() {
		boolean first = true;
		permissionsvalue = "";
		for (Permission permission : permissions) {
			if (!first) 
				permissionsvalue += ", ";
			else
				first = false;
			permissionsvalue += permission.toString();
		}
	} 
}
