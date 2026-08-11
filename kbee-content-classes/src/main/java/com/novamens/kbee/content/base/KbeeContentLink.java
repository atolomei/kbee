package com.novamens.kbee.content.base;


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
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentLink;
import com.novamens.content.base.Resource;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.Identifiable;
import com.novamens.security.User;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="resource")
@Table(name = "kb_Content_Link")
public class KbeeContentLink implements ContentLink, Identifiable {

	@Id
	@GenericGenerator(
		name = "link_sequencer",
		strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
		parameters = {
			@Parameter(name = "sequence_name", value = "classificationid_sequence"),
			@Parameter(name = "increment_size", value = "50"),
			@Parameter(name = "optimizer", value = "hilo")
		}
	)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "link_sequencer")
	@Column(name = "id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeContent.class)
	@JoinColumn(name="source_id", insertable=false, updatable=false, nullable=false)
	private Content source;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeContent.class)
	@JoinColumn(name="target_id", nullable=false)
	private Content target;
	
	private String anchor;
	
	@Column(name = "lastModifiedDate")
	private OffsetDateTime lastModifiedDate;

	@ManyToOne(fetch = FetchType.LAZY, cascade=CascadeType.DETACH, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "lastModifiedUser")
	private User lastModifiedUser;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KBFileImpl.class)
	@JoinColumn(name="resource_id", insertable=false, updatable=false, nullable=false)
	private Resource resource;
	
	@Column(name = "targetUpdated")
	private boolean targetUpdated;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Content getSource() {
		return source;
	}

	public void setSource(Content source) {
		this.source = source;
	}
	
	public String getDisplayName() {
		return getTarget().getTitle();
	}

	public Content getTarget() {
		return target;
	}

	public void setTarget(Content target) {
		this.target = target;
	}

	public String getAnchor() {
		if (this.anchor!=null && anchor.startsWith("#"))
				anchor = anchor.substring(1);
		return anchor;
	}

	public void setAnchor(String anchor) {
		this.anchor = anchor;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public boolean isTargetUpdated() {
		return targetUpdated;
	}

	public void setTargetUpdated(boolean targetUpdated) {
		this.targetUpdated = targetUpdated;
	}

	public OffsetDateTime getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(OffsetDateTime lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public User getLastModifiedUser() {
		return lastModifiedUser;
	}

	public void setLastModifiedUser(User lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}
	
	@Override
	public ContentLink clone() {
		KbeeContentLink clone = new KbeeContentLink();
		clone.setAnchor(getAnchor());
		clone.setSource(getSource());
		clone.setTarget(getTarget());
		return clone;
	}
}
