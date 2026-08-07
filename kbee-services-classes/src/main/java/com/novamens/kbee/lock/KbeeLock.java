package com.novamens.kbee.lock;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.kbee.security.KbeeUser;
import com.novamens.lock.Lock;
import com.novamens.lock.LockScope;
import com.novamens.security.User;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "KLOCK")
public class KbeeLock implements Lock {
	
	@Id 
	@SequenceGenerator(name = "lock_sequencer", sequenceName = "lock_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lock_sequencer")
	@Column(name = "LOCK_ID")
	private long lockId;
	
	@Column(name = "LOCK_OBJECT_ID")
	private String objectId;
	
	@Column(name = "LOCK_DATE")
	private Date date = new Date();
	
	@Column(name = "LOCK_TIMEOUT")
	private Date timeout;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "LOCK_USER_ID", updatable=false)
	private User user;
	
	@Column(name="LOCK_SCOPE") 
	@Enumerated(EnumType.STRING) 
	private LockScope scope;
	
	public Long getId() {
		return lockId;
	}
	
	public void setId(long id) {
		this.lockId = id;
	}
	
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	
	public String getObjectId() {
		return objectId;
	}
	
	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}
	
	public Date getDate() {
		return date;
	}
	
	public Date getTimeout() {
		return timeout;
	}
	
	public void setScope(LockScope scope) {
		this.scope = scope;
	}
	
	public LockScope getScope() {
		return scope;
	}
}
