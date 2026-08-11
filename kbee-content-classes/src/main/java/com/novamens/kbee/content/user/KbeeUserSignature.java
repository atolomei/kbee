package com.novamens.kbee.content.user;


import java.io.IOException;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import org.hibernate.annotations.Type;

import com.novamens.content.resource.KBFile;
import com.novamens.content.user.SignatureType;
import com.novamens.content.user.UserDevice;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserSignature;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;
import com.novamens.signature.CertificateParser;

import kbee.util.PropertiesFactory;
import kbee.util.logging.Logger;

@Entity
@Table(name = "Kb_Signature")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
public class KbeeUserSignature extends AbstractObject implements UserSignature {

	
	static Boolean SIMULATE_HANDWRITTEN_SIGNATURE = "yes".equals(PropertiesFactory.getInstance("kbee").getProperties().getProperty("simulate-hand-written-signature", "no").trim());
	
	private static Logger logger = Logger.getLogger(UserSignature.class.getName());

	@Id 
	@SequenceGenerator(name = "signature_sequencer", sequenceName = "entityid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "signature_sequencer")
	@Column(name = "ID")
	private Long id;

	@Column(name = "type")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.content.user.SignatureTypeUserType")
	private SignatureType type;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeUserDevice.class)
	@JoinColumn(name="device_id")
	private UserDevice device;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KBFileImpl.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "handwrite_image")
	private KBFile handWriteImage;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeUserProfile.class)
	@JoinColumn(name="userprofile_id", insertable=false, updatable=false, nullable=false)
	private UserProfile userProfile;
	
	@Column(name = "certificate")
	private String certificate;
	
	@Column(name = "private_key")
	private String privateKey;
	
	
	@Override
	public Long getId()	{
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}

	@Override
	public SignatureType getType() {
		return type;
	}

	public void setType(SignatureType type) {
		this.type = type;
	}

	public UserDevice getDevice() {
		return device;
	}

	public void setDevice(UserDevice device) {
		this.device = device;
	}
																	

	public KBFile getHandWriteImage() {
		return handWriteImage;
	}

	public void setHandWriteImage(KBFile handWriteImage) {
		this.handWriteImage = handWriteImage;
	}

	@Override
	public UserProfile getUserProfile() {
		return userProfile;
	}

	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}
	
	@Override
	public User getUser() {
		return userProfile!=null ? userProfile.getUser() : null;
	}
	
	@Override
	public String getName() {
		return String.valueOf(getId());
	}
	
	@Override
	public Certificate getCertificate() {
		try {
			if (this.certificate == null) return null;
			Certificate certificate = CertificateParser.Get().read(this.certificate);
			return certificate;
		}
		catch (CertificateException | IOException e) {
			logger.error(e);
			return null;
		}
	}
	
	public void setCertificate(Certificate certificate) throws IOException {
		this.certificate = certificate!=null ? CertificateParser.Get().write(certificate) : null;
	}
	
	
	
	@Override
	public PrivateKey getPrivateKey() throws IOException {
		try {
			PrivateKey key  = CertificateParser.Get().readPrivateKey(this.privateKey);
			return key;
		}
		catch (CertificateException e) {
			logger.error(e);
			return null;
		}
	}
	
	public void setPrivateKey(PrivateKey key) throws IOException {
		this.privateKey = key!=null ? CertificateParser.Get().writePrivateKey(key) : null;
	}
	
	@Override
	public AuditSet getAuditSet() {
		return AuditSet.ENTITY;
	}
}