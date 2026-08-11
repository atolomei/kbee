package kbee.email;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.enoti.ENotiRuleDao;
import com.novamens.content.entity.Person;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.UrlService;
import com.novamens.content.user.UserProfile;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.email.EmailData;
import com.novamens.email.EmailService;
import com.novamens.kbee.content.enoti.KbeeENotiRule;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.service.BrandingService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.WorkflowContext;

import kbee.content.support.SupportTicket;
import kbee.util.logging.Logger;


/**
*
* <p>ALL SUBCLASSESMUST HAVE A CONSTRUCTOR WITH NO PARAMETERS (DEFAULT CONSTRUCTOR)</p>
*
*@see {@link KbeeEmailService}
*
**/
public abstract class EmailBuilderBase {
					
	private static Logger logger = Logger.getLogger(EmailBuilderBase.class.getName());
	private static Logger emaillogger = Logger.getLogger("email");

	
	private static String _default_noreply = null;
	private static Map<String, Object> default_macros_map = null;	
	
	private Domain KBEE = null;	
	private User sender;
	private List<User> receivers  = new ArrayList<User>();
	private String lang;

	private Map<String, Object> parameters;
	private List<String> macro_areas = new ArrayList<String>();
	
	public EmailBuilderBase() {
	}

	public EmailBuilderBase (Map<String, Object> parameters) {
		this.parameters=parameters;
	}
	
	public EmailBuilderBase(User sender) {
		this.sender=sender;
	}
	
	public void setMacroAreas(String areas[]) {
		if (areas!=null && areas.length>0) {
			for (String c:areas) {
				macro_areas.add(c);
			}
		}
	}
	
	public List<String> getMacrosAreas() {
		return macro_areas;
	}
 
	public String getLanguage() {
		
		if (lang!=null)
			return lang;
		
		if (getSender()!=null)
			return getSender().getLocale().getLanguage();
			
		if (getSessionUser()!=null)
			return getSessionUser().getLocale().getLanguage();
			
		return Locale.getDefault().getLanguage(); 
	}
	
	public void setLanguage(String lang) {
		this.lang=lang;
	}
	
	public Map<String, Object> getParameters() {
		return parameters;
	}
	
	public void setParameters(Map<String, Object> map) {
		this.parameters = map;
	}
	
	public void setParameter(String name, Object value) {
		if (parameters==null)
		parameters = new HashMap<String, Object>();
		parameters.put(name, value);
	}
	
	public User getSender() {
		return sender;
	}
	
	public void setSender(User user) {
		this.sender=user;
	}
	
	public void setReceiver(User user) {
		this.receivers.add(user);
	}
	
	public List<User> getReceivers() {
		return receivers;
	}
	
	public String getEmailTo() {
		String to = "";
		for (User receiver : receivers) {
			if (!"".equals(to)) to += ", ";
			to += getPerson(receiver).getEmail();
		}
		return to;
	}
	
	public boolean isSendEnabled() {
		return true;
	}
	
	public abstract String getArea();
	
	public abstract String getKey();
	
	public String getNoReplyEmailAddress() {
		if (_default_noreply!=null)
			return _default_noreply;
		synchronized (this) {		
			_default_noreply = ServiceLocator.getService(BrandingService.class).getNoReplyEmailAddress();
			emaillogger.debug(_default_noreply);
		}
		return _default_noreply;
	}
	
	public String buildPlain() {
		return null;
	}
	
	public Map<String, Object> getDefaultMacros() {
		if (default_macros_map!=null)
			return default_macros_map;
		default_macros_map = new HashMap<String, Object> ();
		default_macros_map.put("${domain-noreply}", getNoReplyEmailAddress());
		default_macros_map.put("${from}", getNoReplyEmailAddress());
		default_macros_map.put("${service-noreply}", getNoReplyEmailAddress());
		default_macros_map.put("${application}", ServiceLocator.getService(com.novamens.service.BrandingService.class).getApplicationShortName());
		default_macros_map.put("${application-name}", ServiceLocator.getService(com.novamens.service.BrandingService.class).getApplicationShortName());
		default_macros_map.put("${application-fullname}", ServiceLocator.getService(com.novamens.service.BrandingService.class).getApplicationName());
		default_macros_map.put("${training-url}", ServiceLocator.getService(com.novamens.service.BrandingService.class).getTrainingUrl()); 		
		
		if (emaillogger.isDebugEnabled()) {
			for (Entry<String, Object> entry: default_macros_map.entrySet()) {
				if (entry.getKey()!=null && entry.getValue()!=null) {
					emaillogger.debug( entry.getKey() + " ->" + entry.getValue());
				} 
			}
		}
		return default_macros_map;
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Area -> " + getArea());
		str.append(" | Key -> " + getKey());
		return str.toString();
	}
	
	protected EmailData parse(EmailTemplate template,  String to, Map<String, Object> macros, String attachments [], String content_info) {
		return parse(template, to, macros, attachments, null, content_info);
	}
	
	protected EmailData parse(EmailTemplate template,  String to, Map<String, Object> macros, String attachments [], String local_file, String context_info) {

		String from 	= template.getFrom();
		String subject 	= template.getSubject();
		String str 		= template.getStringTemplate();
		
		if (from==null || subject==null || str==null) {
			logger.error("from, subject or str: is null");
			return null;
		}
		
		Map<String, Object> dm=getDefaultMacros();
		
		/** ----------------------------------------------------------------
		 * 				 							
		 * Default Macros
		 */
		for (Entry<String, Object> entry: dm.entrySet()) {
			if (entry.getKey()!=null && entry.getValue()!=null) {

				if (entry.getKey().equals( "${service-noreply}")) {
					logger.debug(entry.getKey()+ " -> " + entry.getValue());
				}
				
				from=from.replace(entry.getKey(), (String) entry.getValue());
				subject=subject.replace(entry.getKey(), (String) entry.getValue());
				str=str.replace(entry.getKey(), (String) entry.getValue());
			} else {
				str=str.replace(entry.getKey(), "");
			}
		}
		
		/**											
		 * Template specific macros
		 */
		for (Entry<String, Object> entry: macros.entrySet()) {
			if (entry.getKey()!=null && entry.getValue()!=null) {
				from=from.replace(entry.getKey(), String.valueOf(entry.getValue()));
				subject=subject.replace(entry.getKey(), String.valueOf(entry.getValue()));
				str=str.replace(entry.getKey(), String.valueOf(entry.getValue()));
			} 
			else {
				str=str.replace(entry.getKey(), "");
			}
		}
		

		if (context_info==null)
			context_info="email-service";

		if (emaillogger.isDebugEnabled()) {
			emaillogger.debug("Map");
			emaillogger.debug("---");
			for (Entry<String, Object> entry: macros.entrySet()) {
				if (entry.getKey()!=null && entry.getValue()!=null) {
					emaillogger.debug( entry.getKey() + " ->" + entry.getValue());
				} 
			}
			emaillogger.debug("----------------------------------------------------");
			emaillogger.debug("Text");
			emaillogger.debug("---");
			emaillogger.debug(str);
			emaillogger.debug("context -> " + context_info);
		}
		
		if (attachments!=null) {
			if (local_file!=null)
				return new EmailData(from, to, subject, str, attachments, context_info, local_file);
			else
				return new EmailData(from, to, subject, str, attachments, context_info, null);
		}
		else { 
			if (local_file!=null)
				return new EmailData(from, to, subject, str, null, context_info, local_file);
			else
				return new EmailData(from, to, subject, str, null, context_info, null);
		}
	}
	
	protected String getServerUrl(Domain domain) {
		
		
		return domain.getService(UrlService.class).getServerUrl();
		
		//if (domain.getName().equals("kbee"))
		//	return servername + (vanity_port.length()==0 || vanity_port.equals("80") ? "": (":"+vanity_port));
		//return vanity_server.trim().replace("${domain}", domain.getName()) + (vanity_port.length()==0 || vanity_port.equals("80") ? "": (":"+vanity_port));
	}
	
	protected void addContentMacros(Content content, Map<String, Object> map) {
		try {
			if (content==null) {
				map.put("${error}", "addContentMacros() -> content is null" );
				logger.debug("content is null");
				return;
			}

			map.put("${file-title}", (content.getDisplayName()!=null?content.getDisplayName():""));
			map.put("${title}", (content.getDisplayName()!=null?content.getDisplayName():""));
			map.put("${domain-name}", (content.getDomain().getOrganization()!=null?content.getDomain().getOrganization():content.getDomain().getName()));
			map.put("${file-library-url}", getServerUrl(content.getDomain())  + "/" +  content.getClassCode()	+ "/" +  String.valueOf(content.getOId()));
			
			StringBuilder str = new StringBuilder();
			for (String s: content.getMetadataAsList()) {
				if (str.length()>0)
					str.append("<br/>");
				str.append(s);
			}
			map.put("${file-attributes}", str.toString());
			map.put("${file-metadata}", str.toString());
			map.put("${file-content-classifier}", content.getContentTypeClassificationAsString());

			
			for (Site site: getPortalDao().getSitesPublic(content.getDomain())) {
				if (site.getState()==ObjectState.ENABLED && !site.isExternal()) {
					String siteurl = getServerUrl(content.getDomain()) + "/portal/"+ site.getUrl() + "/doc/" +  String.valueOf(content.getOId());
					map.put("${portal-"+site.getKey()+"-url}", siteurl);
				}	
			}
			addTemplateAttributesToMap(map, content);
		}
		catch (Exception e) {
			map.put("${error}", "addContentMacros() -> e.getClass().getName()" );
			logger.error(e);
		}
	}
	
	protected void addGeneralMacros(Domain domain, Map<String, Object> map) {
		try {
		
			String mytasksurl 		= getServerUrl(domain) + "/mytasks";
			String library_link 	 = getServerUrl(domain) + "/content";
			String pending_url 		 = getServerUrl(domain) + "/pendingtasks";
			
			if (map==null)
				map=new HashMap<String, Object>();
			
			map.put("${domain-name}", domain.getOrganization()!=null?domain.getOrganization():domain.getName());
			
			String durl = getServerUrl(domain);
			map.put("${domain-url}", durl);
			
			map.put("${library-url}", library_link);
			map.put("${library-link}", library_link);
			map.put("${my-tasks-link}", mytasksurl);
			map.put("${my-tasks-url}", mytasksurl);
			map.put("${pending-tasks-url}", pending_url);
		} 
		catch (Exception e) {
			map.put("${error}", "addGeneralMacros -> e.getClass().getName()" );
			logger.error(e);
		}
	}
	
	protected void addWorkflowMacros( WorkflowContext context, Map<String, Object> map) {
		
		if (context==null || map==null) {
			logger.debug("addWorkflowMacros: content or map is null");
			return;
		}
		
		try {
			map.put("${procedure}", context.getProcedure().getDisplayName());
			map.put("${task}", context.getTask().getDisplayName());
			map.put("${task-displayname}", context.getTask().getName());

			if (context instanceof KbeeContext) {
				
				String taskcode = context.getTask().getName().replaceAll("\\s", "-").toLowerCase();
				Content content=((KbeeContext) context).getContent();
				
				com.novamens.workflow.Activity cu=((KbeeContext) context).getCurrentActivity();
				
				if (cu!=null) {
					
					map.put("${task-person-name}",  (cu.getUser()!=null?cu.getUser().getFirstLastName():"[null]"));
					
					map.put("${task-start-date}", 
							ServiceLocator.getService(DateTimeService.class).getDateDisplayString(
									context.getTime(), 
							        (cu.getUser()!=null?cu.getUser().getLocale() :  content.getDomain().getLocale())
						   )
					);
				}
				
				String task_url = getServerUrl(content.getDomain()) + "/task/" + content.getClassCode()+ "/"	+ taskcode + "/" + String.valueOf(content.getId());
				
				com.novamens.workflow.Activity a=((KbeeContext) context).getPreviousActivity();
				
				String note;
				if (a!=null) {
					note=a.getNote()!=null?a.getNote():"";
					map.put("${previous-task}", a.getTask().getDisplayName());
					map.put("${previous-task-displayname}",  a.getTask().getDisplayName());
					map.put("${previous-task-person-name}",  a.getUser().getFirstLastName());
					//map.put("${previous-task-action}", a.getC);
				}
				else {
					note="";
				}
				
				map.put("${task-url}", task_url);
				map.put("${task-note}", note);
				map.put("${comment}", note);
				
			}
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}

	protected void addRuleMacros(ENotiRule rule, Person subscriber, Map<String, Object> map) {
		
		try {
			map.put("${rule-modified-by}", rule.getLastModifiedUser()!=null?rule.getLastModifiedUser().getFirstLastName():"");
			map.put("${rule-lastmodified}", rule.getLastModifiedOffsetDateTimeColloquial());
	
			map.put("${rule-event}", rule.getEventTypeStr(subscriber.getProfile(UserProfile.class).getUser().getLocale()));
			map.put("${event-name}", rule.getEventTypeStr(subscriber.getProfile(UserProfile.class).getUser().getLocale()));
			
			map.put("${rule-name}", rule.getDisplayName());
			map.put("${rule-owner}", rule.getOwner().getFirstLastName());
			map.put("${rule-title}", rule.getName());
			map.put("${rule-id}", rule.getId().toString());
			map.put("${rule-description}", rule.getDescription()!=null? rule.getDescription():"");
			map.put("${rule-subject}", rule.getSubject()!=null?rule.getSubject():"");
			map.put("${rule-condition}", rule.getDisplayCondition()!=null?rule.getDisplayCondition():"");
			map.put("${rule-metadata}", rule.getMetadataAsString());
			map.put("${rule-source}", rule.getRuleSource());
			
			if (rule instanceof KbeeENotiRule) {
				map.put("${action-rule-ruleid}", ((KbeeENotiRule) rule).getActionRuleId());
				map.put("${action-rule-rule-name}", ((KbeeENotiRule) rule).getActionRuleName());
				map.put("${action-rule-subtitle}", ((KbeeENotiRule) rule).getSubject());
				map.put("${action-rule-text}", ((KbeeENotiRule) rule).getText());
			}
		} 
		catch (Exception e) {
			logger.error(e);
			map.put("${rule-title}", e.getClass().getName());
			map.put("${rule-id}", e.getClass().getName());
			map.put("${rule-error}", e.getClass().getName()+ " " + e.getMessage());
		
		}
	}
	
	/**
	 * @param ticket
	 * @param map
	 */
	protected void addSupportMacros(SupportTicket ticket, Map<String, Object> map) {
		try {
				map.put("${action}", "support-ticket");
				StringBuilder str = new StringBuilder();
				if (ticket.getContext()!=null) {
					for (Entry<String, String> s: ticket.getContext().entrySet()) {
						if (str.length()>0)
							str.append("<br/>");
						str.append(s.getKey() + " -> " + s.getValue());
					}
				}
				map.put("${support-ticket-subject}", ticket.getSubject());
				map.put("${support-ticket-text}", ticket.getText());
				map.put("${support-ticket-context}", str.toString());
				map.put("${support-ticket-person}", ticket.getUser().getFirstLastName());
				map.put("${support-ticket-person-username}", ticket.getUser().getUserName());
				map.put("${support-ticket-person-email}", getContentDao().findUserProfileByUser(ticket.getUser()).getPerson().getEmail());
		} 
		catch (Exception e) {
			logger.error(e);
			map.put("${support-error}", e.getClass().getName()+ " " + e.getMessage());
		}
	}

	protected void addAppContextMacros(Person sender, Person receiver, String action, Map<String, Object> map) {
		try {
			map.put("${action}", action);
			
			if (sender!=null)	{
				map.put("${from-displayname}", sender.getFirstLastName());
				map.put("${person-displayname}", sender.getFirstLastName());
				map.put("${person-username}", sender.getProfile(UserProfile.class).getUser().getUserName());
				map.put("${person-email-address}", sender.getEmail());
				
				map.put("${publisher}", sender.getFirstLastName());
				map.put("${sender}", sender.getFirstLastName());
				
			}
			if (receiver!=null)		{
				map.put("${receiver}", receiver.getFirstLastName());
				map.put("${subscriber}", receiver.getFirstLastName());
				map.put("${username}", receiver.getProfile(UserProfile.class).getUser().getUserName());
			}
		} 
		catch (Exception e) {
			logger.error(e);
			map.put("${context-error}", e.getClass().getName()+ " " + e.getMessage());
		}
	}

	protected void addTemplateAttributesToMap( Map<String, Object> map, Content content) {
		if (content==null || map==null) {
			logger.debug("addTemplateAttributesToMap: content or map is null");
			return;
		}
		try {
			Map<String, List<String>> map_clasi = content.getClassificationAsMapString();
			
			String prefix ="${file-attribute.";
			for (Entry<String, List<String>> entry: map_clasi.entrySet()) {
				StringBuilder str = new StringBuilder();
				for (String s: entry.getValue()) {
					if (str.length()>0)
						str.append(", ");
					str.append(s);
				}
				String key_lc= prefix+ entry.getKey().toLowerCase().replace("$", "").replace("{", "").replace("}","") +"}";
				map.put(key_lc, str.toString());
				
				
				String key= prefix+ entry.getKey().replace("$", "").replace("{", "").replace("}","") +"}";
				map.put(key, str.toString());
			}
							
			map.put("${file-console-subtitle}", content.getService(ContentService.class).getConsoleSubtitle());
			map.put("${file-portal-subtitle}", content.getService(ContentService.class).getPortalSubtitle());
			
		} 
		catch (Exception e) {
			logger.error(e);
			map.put("${content-template-error}", e.getClass().getName()+ " " + e.getMessage());
		}
	}
	
	protected boolean isEnabled(Person person) {
		if (person==null) {
			logger.error("Person is null");
			return false;
		}
		if (!person.getProfile(UserProfile.class).getUser().isActive()) {
			logger.error("User is not Active -> " + person.getDisplayName() + " | Person id:" + person.getId() +  " | User id:" + person.getProfile(UserProfile.class).getUser().getUserName());
			emaillogger.debug("User is not Active -> " + person.getDisplayName() + " Person | id:" + person.getId() +  " | User id:" + person.getProfile(UserProfile.class).getUser().getUserName());
			return false;
		}
		return true;
	}

	protected boolean isEmailRuleNotifications(Person receiver) {
		if (!receiver.getProfile(UserProfile.class).isEmailRuleNotifications()) {
			logger.debug("User -> " + receiver.getFirstLastName()+ " has disabled Email Rule/Workflow Notifications.");
			emaillogger.debug("User -> " + receiver.getFirstLastName()+ " has disabled Email Rule/Workflow Notifications.");
			return false;
		}
		return true;
	}
	
	protected EmailTemplate getEmailTemplate(Domain domain, String language, String string) {
		return ServiceLocator.getService(EmailService.class).getEmailTemplate(domain, language, string);
	}
	
	protected EmailData parse(EmailTemplate template, String to, Map<String, Object> macros, String content_info) {
		return parse(template, to, macros, null, content_info);
	}
	
	protected Person getPerson(User user) {
		UserProfile profile = getContentDao().findUserProfileByUser(user);
		return profile.getPerson();
	}
	
	protected User getUser(Person person) {
		UserProfile profile = person.getProfile(UserProfile.class);
		return profile.getUser();
	}
	
	protected Domain getDomainKbee() {
		if (KBEE==null) 
			KBEE = ((ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao")).findDomainByName("kbee");
		return KBEE;
	}

	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected PortalDao getPortalDao() {
		return (PortalDao)ServiceLocator.getService(BeansService.class).getBean("portalDao");
	}
	
	protected ENotiRuleDao getENotiRuleDao() {
		return (ENotiRuleDao)ServiceLocator.getService(BeansService.class).getBean("enotiRuleDao");
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
