package com.novamens.email;

import java.util.List;
import java.util.Map;

import com.novamens.dom.Domain;
import com.novamens.security.User;


/**
 *  Send Content by Email
 *
 *  Rule (self service) -> Event Publish
 *  Rule (self service) -> Event Pending
 *  
 *  Task Assigned
 *  Task Pending
 *  Task Due date expired
 *  
 *  Workflow Alert -> Post Termination Send to
 *  Workflow Alert -> Task running time out
 *  Workflow Alert -> Task pending time out
 *
 *
  
 KEY:
 
 admin-sends-reset-password
 alert-rule-publish
 alert-rule-publish-domain
 alert-rule-publish-requires-accept
 alert-rule-publish-user
 assign-task
 content-home-searcher-portal-url
 db-export
 forgot-password
 forgot-username
 newdomain
 notification-by-action-rule
 pending-task
 reassign-task-former-owner
 reassign-task-receiver
 report_subscription
 send-email
 timeout-task
 welcome
 welcome_basic
 welcome_compliance_monitoring
 welcome_premium
 welcome_standard
 workflow-notification
 workflow-notification-timeout
 
 * 
 * support-ticket-submitter
 * support-ticket-receiver
 *
 *
 *
 *
 * ------------------
 * register-device
 * ------------------ 
 *
 *
 *IMPORTANT
 *ALL CLASSES MUST HAVE A CONSTRUCTOR WITH NO PARAMETERS (DEFAULT CONSTRUCTOR)
 *
 *
 */
public interface EmailBuilder {
	
	
	public final String MAX_TIME_PENDING = "max-time-pending";
	public final String MAX_TIME_RUNNING  = "max-time-running";
	
	public final String DOMAIN="domain";
	public final String REPORT="report";
	public final String GRID="grid";
	public final String GENERAL="general";
	public final String CONTENT="content";
	public final String WORKFLOW="workflow";
	public final String RULE="rule";
	public final String CONTEXT="context";
	public final String SUPPORT="support";
	
	public String AREAS[] = {GENERAL,CONTENT, WORKFLOW, RULE, CONTEXT, DOMAIN, REPORT, GRID, SUPPORT };  
	
	
	public Map<String, Object> getBuilderObjects();
	
	public String getKey();
	public Map<String, Object> getParameters();
	public void setParameters(Map<String, Object> map);

	public EmailData build();
	public Domain getDomain();
	
	public String buildPlain();
	
	public User getSender();
	public boolean isSendEnabled(); 
	
	public String getArea();
	
	public String getLanguage();
	public void setLanguage(String lang);
	
	public List<String> getMacrosAreas();
	
	
	
}
