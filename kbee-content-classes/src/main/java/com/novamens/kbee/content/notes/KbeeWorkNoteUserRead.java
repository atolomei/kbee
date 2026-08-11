package com.novamens.kbee.content.notes;

import java.time.OffsetDateTime;

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

import com.novamens.content.notes.Billboard;
import com.novamens.content.notes.WorkNoteUserRead;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="notification")
@Table(name = "kb_work_note_user_read")
@DynamicInsert
public class KbeeWorkNoteUserRead implements WorkNoteUserRead {

	@Id 
	@SequenceGenerator(name = "entity_sequencer", sequenceName = "entityid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_sequencer")
	@Column(name = "ID")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "user_id", updatable=false)
	private User user;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeBillboard.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "work_note_id", updatable=false)
	private Billboard worknote;
	
	@Column(name = "readdate")
	private OffsetDateTime readdate;
	
	
	public KbeeWorkNoteUserRead() {
	}
	
	public KbeeWorkNoteUserRead(User user, Billboard note) {
		this.user=user;
		this.worknote=note;
		this.readdate=OffsetDateTime.now();
	}

	@Override
	public User getUser() {
		return user;
	}

	@Override
	public Billboard getWorkNote() {
		return this.worknote;
	}

	@Override
	public OffsetDateTime getOffsetDateTimeRead() {
		return this.readdate;
	}

	@Override
	public void setOffsetDateTimeRead(OffsetDateTime date) {
		this.readdate=date;
	}
	
}
