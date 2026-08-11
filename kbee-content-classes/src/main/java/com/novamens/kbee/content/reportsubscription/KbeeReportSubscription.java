package com.novamens.kbee.content.reportsubscription;

import com.novamens.content.reportsubscription.ReportSubscription;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "KB_REPORT_SUBSCRIPTION")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entity")
public class KbeeReportSubscription extends AbstractObject implements ReportSubscription {
				
	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeReportSubscription.class.getName());

	@Id
	@SequenceGenerator(name = "rule_sequencer", sequenceName = "security_sequence", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rule_sequencer")
	@Column(name = "id")
	private Long id;


	@Column(name = "report_export_sched_id")
	private String reportExportScheduleId;

	@Column(name = "enabled")
	private boolean enabled;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "usr")
	private User usr;

	@Column(name = "last_export_sent")
	private OffsetDateTime lastExportSent;

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public void setId(Serializable id) {
		this.id = (Long) id;
	}

	@Override
	public String getName() {
		return this.id.toString();
	}

	@Override
	public String getReportExportScheduleId() {
		return reportExportScheduleId;
	}

	@Override
	public void setReportExportScheduleId(String reportExportScheduleId) {
		this.reportExportScheduleId = reportExportScheduleId;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public User getUsr() {
		return usr;
	}

	@Override
	public void setUsr(User usr) {
		this.usr = usr;
	}

	@Override
	public OffsetDateTime getLastExportSent() {
		return lastExportSent;
	}

	@Override
	public void setLastExportSent(OffsetDateTime lastExportSent) {
		this.lastExportSent = lastExportSent;
	}

	@Override
	public void setDefaultAudit() {

		if (this.getCreationOffsetDateTime() == null)
			this.setCreationOffsetDateTime(OffsetDateTime.now());

		if (this.getLastModifiedOffsetDateTime() == null)
			this.setLastModifiedOffsetDateTime(OffsetDateTime.now());

		if (this.getLastModifiedUser() == null)
			this.setLastModifiedUser(getSessionUser());
	}
	
	public AuditSet getAuditSet() {
		return AuditSet.SYSTEM;
	}
	protected KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	@Override
	public String getDisplayName() {
		return (getReportExportScheduleId()!=null && getUsr() != null)? "User " + getUsr().getUserName() + " subscription to " + getReportExportScheduleId():getId().toString();
	}
}
