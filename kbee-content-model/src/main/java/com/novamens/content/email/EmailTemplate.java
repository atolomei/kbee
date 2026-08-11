package com.novamens.content.email;

import com.novamens.dom.DomainObject;
import com.novamens.dom.Indexable;
import com.novamens.dom.Object;
import com.novamens.security.Identifiable;
import com.novamens.text.TemplateModelInfo;

public interface EmailTemplate extends Object, Identifiable, Indexable, DomainObject  {
	
				
	final String DB_EXPORT					= "db-export";
	final String SEND_EMAIL					= "send-email";
	final String FORGOT_PASSWORD			= "forgot-password";
	final String FORGOT_USERNAME			= "forgot-username";
	
	final String WELCOME 					= "welcome";
	final String TASK_REASSIGN_FORMER_OWNER = "reassign-task-former-owner";
	final String TASK_ASSIGN 				= "assign-task";
	final String TASK_PENDING  				= "pending-task";
	final String TASK_PROGRESS_NOTE 		= "task-progress-note";
	final String TASK_TIMEOUT 				= "timeout-task";
	
	final String TASK_DUE_DATE_NOTIFICATION = "due-date-notification-task";
	
	final String ADMIN_SEND_PASSWORD_RESET  = "admin-sends-reset-password";
	final String PUBLISH_EMAIL_TEMPLATE 	= "alert-rule-publish";
	
	final String REGISTER_DEVICE 			= "register-device";
	final String NEW_DOMAIN 				= "newdomain";
	final String SEND_TOKEN 				= "send-token";
			
	
	public String getTitle();
	public String getKey();
	public String getLanguage();
	
	public String getFrom();
	
	public void setFrom(String from);
	public void setSubject(String sub);
	public void setStringTemplate(String tem);
	public String getSubjectHTML();
	public String getTextHTML();
	
	public boolean isDefault();
	public String getDescription();
	public boolean isDeletable();
	public void setDeletable(boolean b);
	
	public String getDefaultStringTemplate();
	public String getDefaultStringTemplateHTML();
	
	public String getSubject();
	public String getStringTemplate();
	public String getPlainTextTemplate();
	
	public String getSubjectField();
	public String getTextField();
	
	public TemplateModelInfo getModel();
	public String getStrModel();
	public void setModel( String s);
	
}