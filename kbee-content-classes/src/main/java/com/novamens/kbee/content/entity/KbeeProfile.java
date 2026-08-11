package com.novamens.kbee.content.entity;


import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.entity.Profile;
import com.novamens.dom.Domain;
import com.novamens.kbee.domain.KbeeDomain;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.Identifiable;
import com.novamens.security.User;

@Entity
@Table(name = "PROFILE")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class KbeeProfile implements Profile, Identifiable {
	
	
	@Id 
	@SequenceGenerator(name = "profile_sequencer", sequenceName = "entityid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "profile_sequencer")
	@Column(name = "ID")
	private Long id;
	
	@Column(name = "lastModifiedDate")
	private OffsetDateTime lastModifiedDate;
	
	@Column(name = "creationDate")
	private OffsetDateTime creationDate;

	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "lastModifiedUser")
	private User lastModifiedUser;
		
	@ManyToOne(fetch = FetchType.EAGER, targetEntity=KbeeDomain.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="DOMAIN_ID")
	private Domain domain;
	
	@ManyToOne(fetch = FetchType.EAGER, targetEntity=KbeeEntity.class)
	@JoinColumn(name="entity", insertable=false, updatable=false, nullable=false)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="entity")
	private com.novamens.content.entity.Entity entity;
	
	public Long getId() { 
		return id;		
	}
	
	public void setEntity(com.novamens.content.entity.Entity entity) {
		this.entity = entity;
	}
	
	public com.novamens.content.entity.Entity getEntity() {
		return this.entity;
	}

	public Domain getDomain() {
		return domain;
	}
	
	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	@Override
	public OffsetDateTime getLastModifiedOffsetDateTime() {
		return lastModifiedDate;
	}
	
	public void setLastModifiedOffsetDateTime(OffsetDateTime lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	@Override
	public OffsetDateTime getCreationOffsetDateTime() {
		return this.creationDate;
	}

	public void setCreationDate(OffsetDateTime date) {
		this.creationDate = date;
	}
	
	public User getLastModifiedUser() {
		return lastModifiedUser;
	}

	public void setLastModifiedUser(User lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}

	
	public void setCreationOffsetDateTime(OffsetDateTime date) {
		this.creationDate=date;
		
	}
}
