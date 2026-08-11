package com.novamens.kbee.content.social;
 
import java.time.OffsetDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.base.Content;
import com.novamens.content.social.Report;

import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "KB_REPORT")
@DynamicInsert
public class KbeeReport implements Report {
 
	@Id 
	@SequenceGenerator(name = "portal_sequencer", sequenceName = "portalid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "portal_sequencer")
	@Column(name = "ID")
	private Long id;
	
	@Column(name = "reportdate")
	private OffsetDateTime date;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "user_id")
	private User user;
	
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity = KbeeContent.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "content_id")
	private Content content;

	
	public KbeeReport() {
	}
	
	public KbeeReport(Content content, User user, int report) {
		setUser(user);
		setOffsetDateTime(OffsetDateTime.now());
		setContent(content);
		setReport(report);
	}

	@Column(name = "report")
	private int report;
 	
	@Override
 	public int getReport() {
		return report;
	}
	
	@Override
	public void setReport(int report) {
		this.report=report;
	}

	@Override
	public OffsetDateTime getOffsetDateTime() {
		return date;
	}
	
	@Override
	public User getUser() {
		return user;
	}
	
	@Override
	public Content getContent() {
		return content;
	}

	@Override
	public void setOffsetDateTime(OffsetDateTime date) {
		this.date=date;
	}
	
	@Override
	public void setUser(User user) {
		this.user=user;
	}

	@Override
	public void setContent(Content content) {
		this.content=content;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof Report) {
			User ouser = ((Report) object).getUser();
			Content ocontent =  ((Report) object).getContent();
			if (ouser!=null && ocontent!=null && getUser()!=null && getContent()!=null) {
				return ouser.getId().equals(getUser().getId()) && ocontent.getId().equals(getContent().getId());	
			}
		}
		return false;
	}
}
