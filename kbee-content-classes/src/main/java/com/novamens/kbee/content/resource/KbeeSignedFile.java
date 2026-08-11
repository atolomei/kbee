package com.novamens.kbee.content.resource;

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
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.resource.KBFile;
import com.novamens.content.resource.SignedFile;
import com.novamens.content.user.UserSignature;
import com.novamens.kbee.content.user.KbeeUserSignature;
import com.novamens.security.Identifiable;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
@Table(name = "kb_Signed_File")
public class KbeeSignedFile implements SignedFile, Identifiable {
	
	//private static Logger logger = Logger.getLogger(KbeeSignedFile.class.getName());
	
	@Id
	@SequenceGenerator(name = "signedfile_sequencer", sequenceName = "objectid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "signedfile_sequencer")
	@Column(name = "id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KBFileImpl.class)
	@JoinColumn(name="resource_id")
	private KBFile file;
	
	@Column(name = "date")
	private OffsetDateTime date;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUserSignature.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "signature_id")
	private UserSignature signature;
	
	public KbeeSignedFile() {
		
	}
	
	public KbeeSignedFile(UserSignature signature) {
		setSignature(signature);
		setDate(OffsetDateTime.now());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public UserSignature getSignature() {
		return signature;
	}

	public void setSignature(UserSignature signature) {
		this.signature = signature;
	}
	

	public KBFile getFile() {
		return file;
	}

	public void setFile(KBFile file) {
		this.file = file;
	}

	public OffsetDateTime getDate() {
		return date;
	}

	public void setDate(OffsetDateTime date) {
		this.date = date;
	}
	
	@Override
	public String getDisplayName() {
		return String.valueOf(id);
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof KbeeSignedFile)) return false;
		return ((KbeeSignedFile)object).getId().equals(getId());
	}
}