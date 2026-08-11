package com.novamens.kbee.content.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.novamens.content.base.SecurityRule;
import com.novamens.content.model.EntityMember;
import com.novamens.content.model.MemberRole;
import com.novamens.content.security.Role;
import com.novamens.kbee.content.security.KbeeAbstractRole;
import com.novamens.kbee.content.security.KbeeSecurityRule;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.security.Identifiable;
import com.novamens.security.acl.Group;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "KB_MEMBER_ROLE")
public class KbeeMemberRole implements MemberRole, Identifiable {
	
	@Id
	@GenericGenerator(
		name = "userrole_sequencer",
		strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			@Parameter(name = "sequence_name", value = "classificationid_sequence"),
			@Parameter(name = "increment_size", value = "50"),
			@Parameter(name = "optimizer", value = "hilo")
		}
	)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userrole_sequencer")
	@Column(name = "id")
	private Long id;
		
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeAbstractRole.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="role_id")
	private Role role;
	
	@OneToOne(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeSecurityRule.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="SECURITYRULE_ID")
	private SecurityRule securityRule;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeEntityMember.class)
	@JoinColumn(name="entity_id", insertable=false, updatable=false, nullable=false)
	private EntityMember entity;
	
	@OneToOne(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeGroup.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="GROUP_ID")
	private Group group;
	
	@Override
	public Long getId() {
		return id;
	}
	
	public String getDisplayName() {
		return getRole()!=null ? getRole().getName() : null;
	}
	
	public Role getRole() {
		return role;
	}
	
	public void setRole(Role role) {
		this.role = role;
	}
	
	public SecurityRule getSecurityRule() {
		return securityRule;
	}
	
	public void setSecurityRule(SecurityRule rule) {
		this.securityRule = rule;
	}
	
	public EntityMember getEntity() {
		return entity;
	}
	
	public void setEntity(EntityMember entity) {
		this.entity = entity;
	}
	
	public Group getGroup() {
		return group;
	}
	
	public void setGroup(Group group) {
		this.group = group;
	}

}
