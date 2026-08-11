package com.novamens.kbee.content.user;

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

import com.novamens.content.model.LabelScope;
import com.novamens.content.user.UserLabel;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.Identifiable;
import com.novamens.security.User;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "UserLabel")
public class KbeeUserLabel implements UserLabel, Identifiable {
	
	@Id 
	@SequenceGenerator(name = "label_sequencer", sequenceName = "entityid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "label_sequencer")
	@Column(name = "ID")
	private Long id;
	
	@ManyToOne(fetch = FetchType.EAGER, targetEntity=KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name ="USER_ID")
	private User user;
	
	@Column(name = "SCOPE", nullable=false, insertable = false, updatable = false)
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.content.model.LabelScopeType")
	private LabelScope scope;
	
	@Column(name = "LABEL")
	private String label;
	
	@Column(name = "CSS")
	private String css;
	
	@Column(name = "context")
	private String context;
	
	
	public Long getId() {
		return id;
	}
	
	@Override
	public String getLabel() {
		return label;
	}
	
	@Override
	public String getCss() {
		return (css!=null?css.trim():null);
	}

	@Override
	public void setCss(String css) {
		this.css = css;
	}

	@Override
	public String getContext() {
		return this.context;
	}

	@Override
	public void setContext(String ct) {
		this.context=ct;
	}
	
	@Override
	public void setLabel(String label) {
		this.label = label;
	}
	
	@Override
	public void setScope(LabelScope scope) {
		this.scope = scope;
	}
	
	public LabelScope getScope() {
		return scope;
	}
	
	
	@Override
	public void setUser(User user) {
		this.user = user;
	}
	
	@Override
	public User getUser() {
		return user;
	}

	@Override
	public String getClassName() {
		return "User Label";
	}

	@Override
	public String getDisplayName() {
		return label;
	}
}
