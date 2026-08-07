package com.novamens.kbee.domain;

import java.io.Serializable;
import java.time.OffsetDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import org.hibernate.annotations.Type;

import com.novamens.content.webapi.service.ApiService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.event.Event;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;

import kbee.replica.Replica;
import kbee.replica.ReplicaType;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.INTEGER)
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "kb_replica")
public class KbeeReplica implements Replica {

	@Id 
	@SequenceGenerator(name = "replica_sequencer", sequenceName = "entityid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "replica_sequencer")
	
	@Column(name = "ID")
	private Long id;
	
	@Column(name = "server")
	private String server;
	
	@Column(name = "username")
	private String user;
	
	@Column(name = "password")
	private String password;
	
	@Column(name = "TYPE", nullable=false, insertable = false, updatable = false)
	@Enumerated(EnumType.ORDINAL)
	@Type(type="kbee.replica.ReplicaUserType")
	private ReplicaType type;
	
	@Column(name = "creationDate")
	private OffsetDateTime creationDate;
	
	@Column(name = "lastModifiedDate")
	private OffsetDateTime lastModifiedDate;

	@ManyToOne(fetch = FetchType.LAZY, cascade=CascadeType.DETACH, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "lastModifiedUser")
	private User lastModifiedUser;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeDomain.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "domain_id", updatable=false)
	private Domain domain;
	
	@Column(name = "state")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.dom.ObjectStateUserType")
	private ObjectState state;
	
	public KbeeReplica() {
		setState(ObjectState.ENABLED);
		setCreationDate(OffsetDateTime.now());
	}
	
	public Long getId()	{
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}

	public ReplicaType getType() {
		return type;
	}

	public void setType(ReplicaType type) {
		this.type = type;
	}

	@Override
	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	@Override
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	@Override
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public ApiService getApi() {
		return null;
	}
	
	public void handle(Event event) {
	}

	public ObjectState getState() {
		return state;
	}

	public void setState(ObjectState state) {
		this.state = state;
	}

	public OffsetDateTime getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(OffsetDateTime lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public User getLastModifiedUser() {
		return lastModifiedUser;
	}

	public void setLastModifiedUser(User lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}

	public OffsetDateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(OffsetDateTime creationDate) {
		this.creationDate = creationDate;
	}
}