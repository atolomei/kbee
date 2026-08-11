package com.novamens.kbee.content.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.novamens.content.entity.Entity;
import com.novamens.content.entity.Profile;
import com.novamens.dom.Domain;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.security.Identifiable;
import com.novamens.security.User;



/**
 * 
 * 
 * 
 *
 */
@javax.persistence.Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "ENTITY")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class KbeeEntity extends AbstractObject implements Entity, Identifiable {
	
	@Id 
	@SequenceGenerator(name = "entity_sequencer", sequenceName = "entityid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_sequencer")
	@Column(name = "id")
	private Long id;
	
	@OneToMany(orphanRemoval=false, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeProfile.class)
	//@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeProfile.class)
	@JoinColumn(name = "entity", nullable=false)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="entity")
	List<Profile> profiles = new ArrayList<Profile>();
	
	public Long getId()	{
		return this.id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}
	
	/** 
	 * The relationship is completed in both directions by setting the entity in profile 
	 **/
	public void addProfile(Profile profile) {
		((KbeeProfile)profile).setEntity(this);
		this.profiles.add(profile);
	}
	
	public void removeProfile(Profile profile) {
		this.profiles.remove(profile);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Profile> T getProfile(Class<T> profileclass) {
		for (Profile profile : this.profiles) {
			if (profileclass.isInstance(profile))
				return (T)profile;
		}
		return null;
	}
	
	public List<Profile> getProfiles() {
		return this.profiles;
	}
	
	@Override
	public void setLastModifiedUser(User user)	{
		super.setLastModifiedUser(user);
		for (Profile profile : this.profiles) {
			if (profile instanceof KbeeProfile)
				((KbeeProfile)profile).setLastModifiedUser(user);
		}
	}
	
	@Override
	public void setLastModifiedOffsetDateTime(OffsetDateTime date) {
		super.setLastModifiedOffsetDateTime(date);
		for (Profile profile : this.profiles) {
			if (profile instanceof KbeeProfile)
				((KbeeProfile)profile).setLastModifiedOffsetDateTime(date);
		}
	}
	
	@Override
	public void setDomain(Domain domain) {
		super.setDomain(domain);
		for (Profile profile : this.profiles) {
			if (profile instanceof KbeeProfile)
				((KbeeProfile)profile).setDomain(domain);
		}
	}
	

}
