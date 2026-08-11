package com.novamens.kbee.content.subscription;

import java.io.Serializable;

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
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;
import com.novamens.content.subscription.ContentSubscription;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.content.entity.KbeePerson;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.security.audit.AuditSet;

@Entity
@Table(name = "Kb_Content_Subscription")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
public class KbeeContentSubscription extends AbstractObject implements ContentSubscription {

	@Id 
	@SequenceGenerator(name = "subscription_sequencer", sequenceName = "objectid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subscription_sequencer")
	@Column(name = "ID")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeePerson.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "PERSON_ID")
	private Person person;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeContent.class)
	@JoinColumn(name="CONTENT_ID", nullable=false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Content content;

	
	
	public KbeeContentSubscription() {
		
	}
	
	
	public Long getId() {
		return id;
	}

	public void setId(Serializable id) {
		this.id = (Long)id;
	}

	@Override
	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	@Override
	public Content getContent() {
		return content;
	}

	public void setContent(Content content) {
		this.content = content;
	}
	
	@Override
	public String getName() {
		return content!=null ? content.getName() : String.valueOf(getId());
	}

	@Override
	public AuditSet getAuditSet() {
		return AuditSet.CONTENT;
	}
}
