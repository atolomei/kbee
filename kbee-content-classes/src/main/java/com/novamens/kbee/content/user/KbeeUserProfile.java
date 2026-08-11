package com.novamens.kbee.content.user;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.novamens.content.user.externalLogin.UserExternalLoginPlatform;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserDevice;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserProfileType;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserSignature;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.kbee.content.entity.KbeeProfile;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;

@Entity
@Table(name = "UserProfile")
@PrimaryKeyJoinColumn(name="id")
public class KbeeUserProfile extends KbeeProfile implements UserProfile, DomainObject {

	public static final String DEFAULT_ICON_SET = "far";
	
	private static String PROFILE_NAME = "user";
	
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity=KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="USER_ID")
	private User user;
	
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeUserRole.class)
	@JoinColumn(name = "USERPROFILE_ID", nullable=false) 
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="entity")
	List<UserRole> roles = new ArrayList<UserRole>();
	
	@Column(name = "type")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.content.user.UserProfileTypeUserType")
	private UserProfileType type;

	@Column(name = "CONFIDENCELEVEL")
	private double confidencelevel;

	// new task notification by email
	@Column(name = "email_notifications")
	private boolean email_notifications  = true;

	// notification rule by email
	@Column(name = "email_rule_notifications")
	private boolean email_rule_notifications  = true;
	
	// notification rule by alerts
	@Column(name = "alert_rule_notification")
	private boolean alert_rule_notification  = true;

	// new pending task notification by email
	@Column(name = "email_notifications_pending")
	private boolean email_notifications_pending  = true;
	
	// progress note notification by email
	@Column(name = "email_progress_notification")
	private boolean email_progress_notification = true;
	
	// progress note notification by alert
	@Column(name = "alert_progress_notification")
	private boolean alert_progress_notification = true;
	
	@Column(name = "whatsapp_enabled")
	private boolean whatsAppEnabled = false;

	
	@Column(name = "tipoftheday")
	private boolean tipoftheday;

	@Column(name = "editperson")
	private boolean edit_person  = true;
	
	@Column(name = "changepassword")
	private boolean change_password  = true;

	@Column(name = "sendfilesemail")
	private boolean send_files_email = true;

	@Column(name = "uitheme")
	private String uitheme;

	@Column(name = "iconset")
	private String iconset;
	
	@Column(name = "startpage")
	private String startpage;
	
	@Column(name = "lastlogindate")
	private OffsetDateTime lastlogindate;
	 
	@Column(name = "isclient")
	private boolean isclient = true;

	@OneToMany(orphanRemoval=true, mappedBy = "userProfile", fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeUserExternalLoginPlatform.class)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="entity")
	List<UserExternalLoginPlatform> userExternalLoginPlatforms = new ArrayList<UserExternalLoginPlatform>();;
	
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeUserDevice.class)
	@JoinColumn(name = "USERPROFILE_ID", nullable=false) 
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="entity")
	List<UserDevice> devices = new ArrayList<UserDevice>();
	
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeUserSignature.class)
	@JoinColumn(name = "USERPROFILE_ID", nullable=false) 
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="entity")
	List<UserSignature> signatures = new ArrayList<UserSignature>();
	
	@Column(name = "signature_security_level")
	private int signatureSecurityLevel;
	
	
	@Transient
	private String person_first_lastname;
	
	@Override
	public boolean isClientProfile() {
		return this.isclient;
	}
	
	public void setClientProfile( boolean b) {
		this.isclient=b;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	@Override
	public User getUser() {
		return user;
	}
	
	@Override
	public String getDisplayName() {
		return user!=null?user.getDisplayName():null;
	}
	
	@Override
	public String getName() {
		return PROFILE_NAME;
	}
	
	@Override
	public UserProfileType getType() {
		return type;
	}

	public void setType(UserProfileType type) {
		this.type = type;
	}

	public List<UserRole> getRoles() {
		return roles;
	}
	
	public void setRoles(List<UserRole> roles) {

		boolean remove = true;
		
		if (this.roles==null)
			this.roles = new ArrayList<UserRole>();
		// bajas
		while (remove) {
			remove = false;
			for (UserRole userrole : this.roles) {
				boolean found = false;
				for (UserRole newuserrole : roles) {
					if (newuserrole.equals(userrole)) {
						found = true;
						break;
					}
				}
				if (!found) {
					userrole.getRole().removeRole(getPerson(), userrole.getEntity());
					this.roles.remove(userrole);
					remove = true;
					break;
				}
			}
		}
		// altas
		for (UserRole newuserrole : roles) {
			boolean found = false;
			for (UserRole userrole : this.roles) {
				if (newuserrole.equals(userrole)) {
					found = true;
					break;
				}
			}
			if (!found) {
				((KbeeUserRole)newuserrole).setUser(getUser());
				newuserrole.getRole().setRole(getPerson(), newuserrole.getEntity());
				this.roles.add(newuserrole);
			}
		}
	}

	@Override
	public List<UserExternalLoginPlatform> getUserExternalLoginPlatforms() {
		return userExternalLoginPlatforms;
	}

	public void setUserExternalLoginPlatforms(List<UserExternalLoginPlatform> userExternalLoginPlatforms) {
		this.userExternalLoginPlatforms.removeAll(this.userExternalLoginPlatforms);
		this.userExternalLoginPlatforms.addAll(userExternalLoginPlatforms);
	}

	public void setSendFilesEmail(boolean b) {
		this.send_files_email=b;
	}
	
	public boolean isSendFilesEmail() {
		return this.send_files_email;
	}

	@Override
	public void setConfidenceLevel(double level) {
		this.confidencelevel=level;
	}

	@Override
	public double getConfidenceLevel() {
		return confidencelevel;
	}

	@Override
	public void incrementConfidenceLevel(double inc) {
		this.confidencelevel += inc;
	}
	
	@Override
	public void setLastLoginDate(OffsetDateTime time) {
		this.lastlogindate = time;
	}

	//@Override
	public OffsetDateTime getLastLoginDate() {
		return lastlogindate;
	}
	
	@Override
	public String getStartPage() {
		return this.startpage !=null ? this.startpage : "library";
	}

	@Override
	public void decrementConfidenceLevel(double dec) {
		this.confidencelevel -= dec;
	}

	@Override
	public String getPersonDisplayName() {
		return getPerson()!=null ? getPerson().getDisplayName() : "";
	}
	
	@Override
	public String getPersonFirstLastName() {
		if (this.person_first_lastname == null)
			this.person_first_lastname = getPerson()!=null ? getPerson().getFirstLastName() : "";
		 return this.person_first_lastname;		 
	}

	@Override
	public Person getPerson() {
		return (Person) getEntity();
	}
	
	@Override
	public boolean isTipOfTheDay() {
		return this.tipoftheday;
	}

	@Override
	public void setTipOfTheDay(boolean tips) {
		this.tipoftheday = tips;
	}
	
	@Override
	public boolean isEmailNotifications() {
		return email_notifications;
	}

	@Override
	public void setEmailNotifications(boolean isenabled) {
		this.email_notifications = isenabled;
	}

	@Override
	public boolean isEmailRuleNotifications() {
		return email_rule_notifications;
	}

	@Override
	public void setEmailRuleNotifications(boolean isenabled) {
		this.email_rule_notifications = isenabled;
	}
	
	@Override
	public boolean isAlertRuleNotifications() {
		return alert_rule_notification;
	}

	@Override
	public void setAlertRuleNotifications(boolean b) {
		alert_rule_notification=b;
	}
	
	
	
	@Override
	public void setEmailPendingNotifications(boolean b) {
		this.email_notifications_pending=b;
	}

	@Override
	public boolean isEmailPendingNotifications() {
		return this.email_notifications_pending;
	}
	
	@Override
	public void setEmailProgressNoteNotifications(boolean b) {
		this.email_progress_notification=b;
	}

	@Override
	public boolean isEmailProgressNoteNotifications() {
		return this.email_progress_notification;
	}
	
	@Override
	public void setAlertProgressNoteNotifications(boolean b) {
		this.alert_progress_notification=b;
	}

	@Override
	public boolean isAlertProgressNoteNotifications() {
		return this.alert_progress_notification;
	}
	
	@Override
	public boolean isEditPersonEnabled() {
		return this.edit_person;
	}

	@Override
	public void setEditPersonEnabled(boolean enabled) {
		this.edit_person=enabled;
	}
	
	@Override
	public boolean isChangePasswordEnabled() {
		return this.change_password;
	}

	@Override
	public void setChangePasswordEnabled(boolean enabled) {
		this.change_password=enabled;
	}
	

	@Override
	public boolean isWhatsAppEnabled() {
		return whatsAppEnabled;
	}

	public void setWhatsAppEnabled(boolean whatsAppEnabled) {
		this.whatsAppEnabled = whatsAppEnabled;
	}

	@Override
	public String getUitheme() {
		return this.uitheme;
	}

	@Override
	public void setUitheme(String theme) {
		this.uitheme=theme;
	}

	@Override
	public void setStartPage(String a) {
		this.startpage=a;
	}
	
	@Override
	public Locale getLocale() {
		return getUser()!=null ? getUser().getLocale() : Locale.getDefault();
	}
	
	@Override
	public String getTimeZone() {
		return getUser()!=null ? getUser().getTimeZone() : ZoneId.systemDefault().getId();
	}
	
	@Override
	public String getIconSet() {
		return this.iconset !=null ? this.iconset : DEFAULT_ICON_SET;
	}

	@Override
	public void setIconSet(String a) {
		this.iconset=a;
	}
	
	public List<UserDevice> getDevices() {
		return devices;
	}
	
	public void setDevices(List<UserDevice> devices) {
		this.devices.clear();
		this.devices.addAll(devices);
	}
	
	@Override
	public void addDevice(UserDevice device) {
		this.devices.add(device);
	}
	
	@Override
	public void delete(UserDevice device) {
		for (UserDevice d : devices) {
			if (d.getDeviceId().equals(device.getDeviceId())) {
				devices.remove(d);
				break;
			}
		}
	}

	public List<UserSignature> getSignatures() {
		return signatures;
	}

	public void setSignatures(List<UserSignature> signatures) {
		this.signatures.clear();
		this.signatures.addAll(signatures);
	}
	
	public void addSignature(UserSignature signature) {
		this.signatures.add(signature);
	}
	
	public void delete(UserSignature signature) {
		if (!(signature instanceof KbeeUserSignature)) return;
		this.signatures.remove(signature);
	}
	
	
	@Override
	public int getSignatureSecurityLevel() {
		return signatureSecurityLevel;
	}
	
	public void setSignatureSecurityLevel(int level) {
		signatureSecurityLevel = level;
	}
	
	@Override
	public void setDomain(Domain domain) {
		super.setDomain(domain);
		if (getUser()!=null)
			((KbeeUser)getUser()).setDomain(domain);
	}
	
	@Override
	public void setLastModifiedUser(User user)	{
		super.setLastModifiedUser(user);
		if (getUser()!=null)
			((KbeeUser)getUser()).setLastModifiedUser(user);
	}
	
	@Override
	public void setLastModifiedOffsetDateTime(OffsetDateTime date) {
		super.setLastModifiedOffsetDateTime(date);
		if (getUser()!=null)
			((KbeeUser)getUser()).setLastModifiedOffsetDateTime(date);
	}


 }
