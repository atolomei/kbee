package com.novamens.content.enoti;

import java.util.List;
import java.util.Locale;

import com.novamens.content.base.Rule;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.security.Role;
import com.novamens.dom.ObjectState;
import com.novamens.event.LogEvent;

import com.novamens.security.Principal;
import com.novamens.security.User;

/**
 * 
 * 
 * <p>There are 2 types of Rule based alerts

 * <b>Self Service Alerts</b>
 * Users can define their Alerts (email and/or internal alerts on the bell) in User Settings/alerts
 * 
 * <b>Domain Alerts</b>
 *  Same as self service alerts but they are managed by the Admin users, who can define the receivers (Users or Groups) of the Alert. 
 *
 * Events supported
 * ----------------
 * 
 * . When a File is Published
 * . When a File is sent to "Pending"
 * 
 * </p>
 * 
 * 
 *  see also: {@link ENotiRuleService}
 *
 */
public interface ENotiRule extends Rule {
	
	static final int EVENT_PUBLISH_CONTENT 	 = 0;
	static final int EVENT_PENDING_TASK		 = 1;
	static final String SOURCE_SELF_SERVICE = "self-service";
	static final String SOURCE_ALERT_MANUAL = "manual";
	static final String SOURCE_SYSTEM_ALERT_RULE = "system-alert-rule";
	static final String SOURCE_TIME_DEPENDENT_RULE = "time-dependent-rule";
	static public final String EMAIL_TEMPLATE_KEY = "notification-by-rule";
	static final String PUBLISH_EMAIL_TEMPLATE = EmailTemplate.PUBLISH_EMAIL_TEMPLATE ; // -user | -domain
	static final String PENDING_EMAIL_TEMPLATE = "pending-task";
	
	/**
	 * kind of "id" used by EmMailTemplates
	 * @return
	 * 
	 * 
	 *  
	 *  
	 */
	String getKey();
	
	public String getSubject();

//	public List<LogEvent> getEvents();
	public List<Principal> getReceivers();
	public List<Role> getRoleReceivers();
	public boolean evaluate(LogEvent event);
	
	public void setOwner(User user);
	public User getOwner();
	
	public int getEventType();
	public void setEventType(int type);
	public List<String> getEventTypes();
	public boolean includes(String type);
	
	String getEventTypeStr(Locale locale);
	
	public boolean isEmail();
	public boolean isAlert();
	public boolean isSystem();
	
	public void setRequireConfirm(boolean b);
	public boolean isRequireConfirm();
	public boolean getRequireConfirm();
	
	public String getEmailTemplate();
	
	ObjectState getState();

	public String getMetadataAsString();
	
	public String getRuleSource();
}
