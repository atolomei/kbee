package com.novamens.logging;


import java.io.File;
import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.novamens.content.base.DomainProxy;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.email.EmailData;
import com.novamens.event.LogEvent;

import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.Identifiable;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@Entity
@Table(name = "KB_SENDEMAILEVENT")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="log")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "event_type", discriminatorType=DiscriminatorType.STRING)
public class SendEmailEvent implements LogEvent, Identifiable,  DomainObject {
	
	@Transient
	private static transient kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SendEmailEvent.class.getName());
	
	@Transient
	public static final String Logger = "AUDITOR"; 
	
	static public String getClassEventType() {
		return "SendEmail";
	}
	
	@Id 
	@GenericGenerator(
		name = "sendemail_log_sequence",
		strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			@Parameter(name = "sequence_name", value = "log_sequence"),
			@Parameter(name = "increment_size", value = "50"),
			@Parameter(name = "optimizer", value = "pooled-lo")
		}
	)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sendemail_log_sequence")
	@Column(name = "event_id")
	private Long id;

	@Column(name = "event_time")
	private OffsetDateTime time;

	
	/** Who triggers the event
	 *  ---------------------- 
	 * 
	 * Sends the Content
	 * Downloads the export
	 * Completes a Task
	 * 
	 * */
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "event_user", updatable=false)
	private User user;

 
	@Column(name = "event_domain_id")
	private Long domainId;

	/**
	 *  ObjectID
	 *  -------- 
	 * Content Sent
	 * Content of a Task
	 * 
	 * Download Grid: null
	 * 
	 **/
	
	// Object Id
	@Column(name = "event_object_id")
	private String object_id;

	@Column(name = "event_result")
	private String result;

	/**
	 * Task, Send by Email, Download.
	 */
	@Column(name = "event_generator_action")
	private String generator_action;
	
	@Column(name = "email_from")
	private String email_from;

	@Column(name = "email_to")
	private String email_to;
	
	@Column(name = "email_subject")
	private String email_subject;

	@Column(name = "email_text")
	private String email_text;

	@Column(name = "email_attachments")
	private String email_attachments;
	
	@Column(name = "event_audit_resource_id")
	private Long event_audit_resource_id;
	
	// @Column(name = "related_content")
	// @Column(name = "related_rule")
	// @Column(name = "related_alert")
	
	// ------------------------------------------
	// User does the action 
	// User receives the action
	// ------------------------------------------
	//
	
	@Transient
	private transient Domain domain;
	
	@Transient
	private String eventType;
	
	@Transient
	final AuditSet audiset = AuditSet.EMAIL;
	

	public SendEmailEvent() {
		setTime(OffsetDateTime.now());
		setEventType(getClass().getSimpleName().replace("$(Event)", ""));
	}
	
	public SendEmailEvent(EmailData data, String result, Domain domain) {

		setTime(OffsetDateTime.now());
		setEventType(getClass().getSimpleName().replace("$(Event)", ""));
		setDomain(domain);
		
		if (data.audit_resource_id!=null) { 
			try {
				this.setAuditResourceKBFileId((Long) data.audit_resource_id);
			} catch (Exception e) {
				logger.error(e);
			}
		}

		try { 
			if (data.getResources()!=null) {
				StringBuilder str = new StringBuilder(); 
				for( String file: data.getResources()) { 
					if (file!=null) {
						String path[] = file.split("\\"+File.separator);			
						String fname=path[path.length-1];
						if (str.length()>0)
							str.append(" | ");
						str.append(fname);
					}
				}
				setAttachments(str.toString());
			}
		} catch (Exception e) {
			logger.error(e);
			setAttachments(e.getClass().getName());
		}
		
		setFrom(data.from);
		setTo(data.to);
		setSubject(data.subject);
		setText(data.msg);
		
		setResult(trunc(result, 64));
		
		if (data.getObjectId()!=null)
			setObjectId(trunc(data.getObjectId(), 32));
		
		setGeneratorAction(trunc(data.getContextInfo(), 128));
		
		
		if (data.getUserId()!=null) {
			User user = ServiceLocator.getService(SecurityService.class).findUserById(data.getUserId());
			if (user!=null)
				setEventUser(user);
		}
		else {
			User user = ServiceLocator.getService(UserService.class).findRootUser(domain);
			setEventUser(user);
		}
	}
	

	
	
	private String trunc(String s, int max) {
		if (s==null || s.length()<=max)
			return s;
		return s.substring(0, max);
	}
	
	@Override
	public AuditSet getAuditSet() {
		return AuditSet.EMAIL;
	}
	
	public String getTitle() {
		return getFrom() + " " + getTo() + " " + getSubject();
	}

	public String getResult() 						{return this.result;}
	public void setResult(String result) 			{this.result=result;}

	public String  getAttachments() 				{return this.email_attachments;}
	public void    setAttachments(String att) 		{this.email_attachments=att;}

	public String 	getFrom() 						{return email_from;}
	public void 	setFrom(String from) 			{this.email_from=from;}
	
	public String 	getTo() 						{return email_to;}
	public void 	setTo(String to)				{this.email_to=to;}
	
	public void 	setSubject(String subject)		{this.email_subject=subject;}
	public String 	getSubject()					{return this.email_subject;}
					
	public void setText(String msg) 				{this.email_text=msg;}
	public String getText() 						{return this.email_text;}
	
	public String getHTMLText() 					{
		if (this.email_text==null)
			return "";
		return this.email_text.replace("\r\n", "<br/>");
	}
	
				
	public void setGeneratorAction(String a) 		{this.generator_action=a;}
	public String getGeneratorAction() 				{return this.generator_action;}
	
	public Long getId()	 							{return id;}
	public void setId(Long id) 						{this.id = id;}
	
	public OffsetDateTime getTime() 				{return time;}
	public void setTime(OffsetDateTime time) 		{this.time = time;}

	public User getEventUser() 						{return user;}
	public void setEventUser(User user)	 			{this.user = user;}

	protected void setEventType(String eventType) 	{this.eventType=eventType;}

	@Override
	public String getEventType() {					return getClassEventType();	}
								
	protected void setObjectId(String oid) 			{this.object_id=oid;}
	public String getObjectId() 					{return  object_id;}

	
	public Long getDomainId() 						{return domainId;}
	public void setDomainId(Long domainId) 			{this.domainId = domainId;}
	
	public Domain getDomain() 						{
		  if (domain==null) 
			  domain = new DomainProxy(domainId);
		  return domain;
	}
	
	public void setDomain(Domain domain) {
		this.domain = domain;
		setDomainId((Long)domain.getId());
	}
	
	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String getType() {
		return "System";
	}

	
	public String getAction() {
		return  "Send Email"; //eventType;
	}
	
	public String getTarget() {
		return getFrom() + "-> " + getTo();
	}

	@Override
	public String getObjectClass() {
		return "EmailData";
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(getAction() + " | " + getTarget() + " | ");
		str.append(getTitle());
		if (getText()!=null) {
			str.append(" | ");
			if (getText().length() > 30)
				str.append(getText().substring(0, 30)+"...");
			else
				str.append(getText());
		}
		return str.toString();
	}

	@Override
	public String getDisplayName() {
		return this.getClass().getName();
	}

	@Override
	public String getParameters() {
		return null;
	}

	@Override
	public Long getAuditResourceKBFileId() {
		return event_audit_resource_id;
	}

	public void setAuditResourceKBFileId(Long kb_file_id) {		
		event_audit_resource_id = kb_file_id;
	}

	@Override
	public boolean isSilentMode() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isNotifiable() {
		return false;
	}
}

