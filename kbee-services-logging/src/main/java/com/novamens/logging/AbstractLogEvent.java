package com.novamens.logging;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import org.hibernate.annotations.Type;

import com.novamens.event.LogEvent;
import com.novamens.kbee.security.KbeeUser;

import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;

@Entity
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="log")
@Table(name = "LOGEVENT")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "event_type", discriminatorType=DiscriminatorType.STRING)
public class AbstractLogEvent implements LogEvent  {

	@Transient
	public static transient final String Logger = "AUDITOR"; 

	@Id 
	@GenericGenerator(
		name = "log_sequencer",
		strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			@Parameter(name = "sequence_name", value = "log_sequence"),
			@Parameter(name = "increment_size", value = "50"),
			@Parameter(name = "optimizer", value = "pooled-lo")
		}
	)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "log_sequencer")
	@Column(name = "EVENT_ID")
	private Long id;

	@Column(name = "EVENT_TIME")
	private OffsetDateTime time; 

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "EVENT_USER", updatable=false)
	private User user;   // user that executed or triggered the event

	@Column(name = "EVENT_PARAMETERS")
	private String parameters;
	
	@Column(name = "auditset")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.dom.AuditSetUserType")
	private AuditSet auditSet;

	@Column(name = "event_audit_resource_id")
	private Long event_audit_resource_id;
	
	@Override
	public Long getAuditResourceKBFileId() {
		return event_audit_resource_id;
	}

	public void setAuditResourceKBFileId(Long kb_file_id) {		
		event_audit_resource_id = kb_file_id;
	}
	
	@Transient
	private String eventType;   

	
	@Transient
	boolean silentMode = false;

	public boolean isSilentMode() {
		return silentMode;
	}

	public void setSilentMode(boolean silentMode) {
		this.silentMode = silentMode;
	}
	
	public AbstractLogEvent() {
		setTime(OffsetDateTime.now());
		setEventType(getClass().getSimpleName().replace("$(Event)", ""));
	}
	
	public AuditSet getAuditSet() {
		return auditSet;
	}

	public void setAuditSet(AuditSet auditSet) {
		this.auditSet = auditSet;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public OffsetDateTime getTime() {
		return time;
	}

	public void setTime(OffsetDateTime time) {
		this.time = time;
	}

	public User getEventUser() {
		return user;
	}

	public void setEventUser(User user) {
		this.user = user;
	}
	
	@Override
	public String getTitle() {
		return getType();
	}
	
	@Override
	public String getDisplayName() {
		return getType() + " - " + getObjectClass();
	}

	public String getDescription() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public String getParameters() {
		return parameters;
	}

	protected void setEventType(String eventType) {
		this.eventType=eventType;
	}
	
	public String getEventType() {
		return  eventType==null?"LogEvent":eventType;
	}
	
	public String getAction() {
		return getEventType();
	}
	
	public String getTarget() {
		return "";
	}
	
	@Override
	public String getType() {
		return "Log";
	}

	@Override
	public String getObjectClass() {
		return "Object";
	}
	
	@Override
	public String toString() {
		return this.getClass().getName() + " - " + (parameters!=null?parameters.toString():"null");
	}
	
	@Override
	public boolean isNotifiable() {
		return false;
	}
}
