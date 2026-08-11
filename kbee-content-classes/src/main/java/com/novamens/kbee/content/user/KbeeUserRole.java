package com.novamens.kbee.content.user;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.novamens.content.entity.Person;
import com.novamens.content.model.EntityMember;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.kbee.content.model.KbeeEntityMember;
import com.novamens.kbee.content.security.KbeeAbstractRole;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.Identifiable;
import com.novamens.security.User;
import com.novamens.security.acl.Group;


/**
 
	Para que user_id si ya tiene el userprofile ?
	alter table kb_user_role add column user_id bigint not null;
	alter table kb_user_role add constraint user_role_user_id_fk foreign key (user_id) references users(id) on delete cascade;

 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "KB_USER_ROLE")
public class KbeeUserRole implements UserRole,  Identifiable {
	
	
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
	
	// Cascade.DELETE must not be used here
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeUserProfile.class)
	@JoinColumn(name="userprofile_id", insertable=false, updatable=false, nullable=false)
	private UserProfile userProfile;

	// Cascade.DELETE must not be used here
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeAbstractRole.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="role_id")
	private Role role;

	// Cascade.DELETE must not be used here
	@ManyToOne(fetch = FetchType.EAGER, targetEntity=KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="user_id")
	private User user;
	
	// Cascade.DELETE must not be used here
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeEntityMember.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="entity_id")
	private EntityMember entity;
	
	
	public KbeeUserRole() {
	}
	
	public KbeeUserRole(Role role, User user, EntityMember entity) {
		setRole(role);
		setUser(user);
		setEntity(entity);
	}
	
	@Override
	public Long getId() {
		return id;
	}
	
	@Override
	public String getDisplayName() {
		String dn = getRole()!=null?getRole().getName():"-";
		dn += getEntity()!=null ? " ("+getEntity().getDisplayName()+")" : "";
		return dn;
	}
	
	public Role getRole() {
		return role;
	}
	
	public void setRole(Role role) {
		this.role = role;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public EntityMember getEntity() {
		return entity;
	}
	
	public void setEntity(EntityMember member) {
		this.entity = member;
	}
	
	public UserProfile getUserProfile() {
		return this.userProfile;
	}
	
	public void setUserProfile(UserProfile profile) {
		this.userProfile = profile;
	}
	
	public Person getPerson() {
		return getUserProfile()!=null ? getUserProfile().getPerson() : null;
	}
	
	public Set<Group> getGroups() {
		return ((KbeeAbstractRole)getRole()).getGroups(getEntity());
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof KbeeUserRole)) return false;
		KbeeUserRole role = (KbeeUserRole)object;
		if ((role.getUser()==null && getUser()!=null) ||
			(role.getUser()!=null && getUser()==null)) {
			return false;
		}
		if (!((role.getUser()==null && getUser()==null) ||
			(role.getUser().equals(getUser())))) {
			return false;
		}
		if ((role.getRole()==null && getRole()!=null) ||
			(role.getRole()!=null && getRole()==null)) {
			return false;
		}
		if (!((role.getRole()==null && getRole()==null) ||
			(role.getRole().equals(getRole())))) {
			return false;
		}
		if ((role.getEntity()==null && getEntity()!=null) ||
			(role.getEntity()!=null && getEntity()==null)) {
			return false;
		}
		if (!((role.getEntity()==null && getEntity()==null) ||
			(role.getEntity().equals(getEntity())))) {
			return false;
		}
		return true;
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("User -> ");
		str.append(user!=null?user.getDisplayName(): "null");
		
		str.append(" | UserProfile -> ");
		str.append(userProfile!=null?userProfile.getPersonDisplayName(): "null");
		
		str.append("| Role -> ");
		str.append(role!=null?role.getDisplayName(): "null");
		
		if (entity!=null) {
			str.append(" | Entity -> ");
			str.append(entity.getDisplayName());
		}
		
		return str.toString();
	}
	

}
