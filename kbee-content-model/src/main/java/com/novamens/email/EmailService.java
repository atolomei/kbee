package com.novamens.email;


import java.util.List;
import java.util.Map;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.entity.Person;

import com.novamens.dom.Domain;
import com.novamens.service.SystemService;

/** 
 * <p>Service for sending email in Notifications</p>
 * 
 * @see {@link EmailBuilder}
 */
public interface EmailService extends SystemService {

	// Template
	public void save(EmailTemplate template, List<String> parts) throws ContentMgmtException;

	// Send generic email 
	public void send(EmailData emaildata, Domain domain);
	public void sendEmail(Person person, EmailData data);

	// EmailBuilder
	void send(EmailBuilder builder) throws ContentMgmtException;

	public boolean hasEmailTemplate(Domain domain);
	public void setUpTemplates(Domain domain);

	public EmailTemplate getEmailTemplate(Domain domain, String language, String key);

	public Map<String, EmailTemplate> getDefaultTemplates(String language);
	
	
	
	
	// public Map<String, Object> getDefaultMacros();

	public List<Class<? extends EmailBuilder>> getAllEmailBuilderClasses();
	public Map<String, Class<? extends EmailBuilder>> getEmailBuilderKeyClassMap();

	public Map<String, String> getTemplateMacros(Domain domain);

	String getNoReplyEmailAddress();
	
	 
	
	

}









// -----------------------------------------------------------------------------------------------------
// public void sendConditionNotification(ENotiRule rule, Content content, Person publisher, Person subscriber);
//public void addTemplateAttributesToMap(Map<String, Object> map, Content content);

//
// public void sendTestEmailRuleNotification(ENotiRule rule, String key, Person receiver);
//  public void sendSubscriptionReport(Person person, String to, String reportScheduleName, String reportScheduleDescription, String[] attachment, String displayname, long audit_kbfile_id);
// public void sendPasswordReset(Person person, Person sender);
// public void sendAdminPasswordReset(Person person, String to, String displayname);
// public void sendGetMyUsername(Domain domain, String language, String to, Map<String, Object> map);
// public void sendNewAdminDomainMessage(Person person, String service_monitor_email, String string);
//  public void sendNewDomainMessage(Domain domain, Map<String, Object> map, String service_monitor_email, String string);
//	public void sendWelcomeMessage(Person person, String to, String displayname); 	// EmailBuilderWelcomeMessage
//  public void sendWorkflowNotification(WorkflowContext context, Content content, Person task_executer, Person receiver, String text);
//  public void sendWorkflowTimeoutNotification(WorkflowContext context, Content content, Person receiver, String alertkey);
//  public void sendContentByEmail(Content content, Person from_person, String to, String text, String[] attachment); // 7
//  public void sendPublishContentRuleNotification(ENotiRule rule, Content content, Person publisher, Person subscriber);  // 6
//  public void sendKbeeUserPasswordReset(Person person);
//  public void sendTaskAssigned(LogEvent event);  // 3
//  public void sendTaskPending(LogEvent event, User user);
//	public void sendTaskTimeout(WorkflowContext context, Content content, User user_that_had_the_task);

	// Export 
// void sendGridExport(Person person, File file);
// void sendDBExportLink(Person person, String filename);
//
// public Map<String, String> TestEmail(Person person, EmailTemplate object, Map<String, String> x_map);
//
//--------------------------------
//public void addContentMacros(Content content, Map<String, Object> map);
//public void addGeneralMacros(Domain domain, Map<String, Object> map);
//public void addAppContextMacros(Person sender, Person receiver, String key, Map<String, Object> map);
//public void addRuleMacros(ENotiRule rule, Person subscriber, Map<String, Object> map);
//public void addWorkflowMacros(WorkflowContext context, Map<String, Object> map);
//--------------------------------



