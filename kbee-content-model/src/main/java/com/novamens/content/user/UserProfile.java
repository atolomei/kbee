package com.novamens.content.user;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

import com.novamens.content.entity.Person;
import com.novamens.content.entity.Profile;
import com.novamens.content.user.externalLogin.UserExternalLoginPlatform;
import com.novamens.dom.DomainObject;
import com.novamens.security.User;

public interface UserProfile extends Profile, DomainObject {
	
	public Long getId();
	public User getUser();
	public Person getPerson();
	
	public UserProfileType getType();
	
	public void setLastLoginDate(OffsetDateTime date);
	public OffsetDateTime getLastLoginDate();
	
	public List<UserRole> getRoles();
	public void setRoles(List<UserRole> roles);

	public List<UserExternalLoginPlatform> getUserExternalLoginPlatforms();

	public void setUserExternalLoginPlatforms(List<UserExternalLoginPlatform> userExternalLoginPlatforms);
	
	public void setConfidenceLevel(double level);
	public double getConfidenceLevel();
	
	public void incrementConfidenceLevel(double inc);
	public void decrementConfidenceLevel(double dec);
	
	public boolean isEditPersonEnabled();
	public void setEditPersonEnabled(boolean enabled);
	
	public boolean isChangePasswordEnabled();
	public void setChangePasswordEnabled(boolean enabled);
	
	public boolean isTipOfTheDay();
	public void setTipOfTheDay(boolean tips);

	// New task notification
	public boolean isEmailNotifications();
	public void setEmailNotifications(boolean enabled);

	// Notification rule
	public boolean isEmailRuleNotifications();
	public void setEmailRuleNotifications(boolean enabled);
	
	public void setAlertRuleNotifications(boolean enabled);
	public boolean isAlertRuleNotifications();
	
	// Pendig task notification
	public void setEmailPendingNotifications(boolean b);
	public boolean isEmailPendingNotifications();
	
	// Progress note notification
	public void setEmailProgressNoteNotifications(boolean b);
	public void setAlertProgressNoteNotifications(boolean b);
	
	public boolean isEmailProgressNoteNotifications();
	public boolean isAlertProgressNoteNotifications();
	
	public void setSendFilesEmail(boolean b);
	public boolean isSendFilesEmail();
	
	public boolean isWhatsAppEnabled();
	
	/**
	 * This is normally Lastname, Firstname due to indexing reasons
	 * user {@code getPersonFirstLastName} for colloquial name.
	 */
	public String getPersonDisplayName();
	public String getPersonFirstLastName();
	
	public String getUitheme();
	public void setUitheme(String theme);

	public String getStartPage();
	public void setStartPage(String a);

	public String getIconSet();
	public void setIconSet(String a);
	
	public Locale getLocale();
	public String getTimeZone();
	
	public List<UserDevice> getDevices(); 
	public void addDevice(UserDevice device);
	public void delete(UserDevice device);
	
	public List<UserSignature> getSignatures(); 
	public void addSignature(UserSignature signature);
	public void delete(UserSignature signature);
	public int getSignatureSecurityLevel();
	
	/** client vs root or vendor user */
	public boolean isClientProfile();
	
	public void setLastModifiedOffsetDateTime(OffsetDateTime now);
	public void setLastModifiedUser(User sessionUser);
}