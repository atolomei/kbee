package com.novamens.kbee.content.user;


import java.io.Serializable;
import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
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

import com.novamens.content.user.UserDevice;
import com.novamens.content.user.UserProfile;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "KB_Device")
public class KbeeUserDevice extends AbstractObject implements UserDevice {
	
	@Id 
	@SequenceGenerator(name = "device_sequencer", sequenceName = "entityid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "device_sequencer")
	@Column(name = "ID")
	private Long id;
	
	@Column(name = "device_id")
	private String deviceId;

	@Column(name = "description")
	private String description;
	
	@Column(name = "number")
	private String number;
	
	@Column(name = "registration_time")
	private OffsetDateTime registrationTime;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeUserProfile.class)
	@JoinColumn(name="userprofile_id", insertable=false, updatable=false, nullable=false)
	private UserProfile userProfile;
	

	/**
	@Column(name = "category")
	private String deviceCategory;
	
	
	@Override
	public String getDeviceCategory() {
		return deviceCategory;
	}

	public void setDeviceCategory(String description) {
		this.deviceCategory = description;
	}
	**/
	
	@Override
	public boolean isAndroid() {
		return getDescription()!=null && getDescription().toLowerCase().contains("android");
	}
	
	
	@Override
	public boolean isIOS() {
		return getDescription()!=null && getDescription().toLowerCase().contains("ios");
	}
	
	@Override
	public boolean isWebRegistered() {
		return getNumber()!=null;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	@Override
	public String getDisplayName() {
		return description;
	}
	
	@Override
	public String getName() {
		return description;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public User getOwner() {
		return getUserProfile()!=null ? getUserProfile().getUser() : null; 
	}

	public OffsetDateTime getRegistrationTime() {
		return registrationTime;
	}

	public void setRegistrationTime(OffsetDateTime registrationTime) {
		this.registrationTime = registrationTime;
	}

	public UserProfile getUserProfile() {
		return userProfile;
	}

	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}
	
	@Override
	public boolean equals(Object object) {
		
		if (!(object instanceof KbeeUserDevice)) 
			return false;
		
		return ((KbeeUserDevice)object).getId().equals(getId());
	}
	
	@Override
	public AuditSet getAuditSet() {
		return AuditSet.SYSTEM;
	}
}