package com.novamens.kbee.content.base;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import com.novamens.content.base.ResourceTag;
import com.novamens.content.base.ResourceGroupType;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.security.audit.AuditSet;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="resource")
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name = "kb_Resource_Tag")
public class KbeeResourceTag extends  AbstractObject  implements ResourceTag {

	@Id					
	@SequenceGenerator(name = "resourcetag_sequencer", sequenceName = "domainid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "resourcetag_sequencer")
	@Column(name = "id")
	private Long id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "multiple")
	private boolean multiple = true;
	
	@Column(name = "defaulttag")
	private boolean defaultTag = false;
	
	@Column(name = "newcontenttempplate")
	private boolean newcontenttempplate = true;
	
	
	@Column(name = "alias") 
	private String alias;
	
	@Column(name = "type")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.content.base.ResourceGroupUserType")
	private ResourceGroupType type;
	
	public KbeeResourceTag() {
	}
	
	public KbeeResourceTag(KbeeResourceTag source) {
		super(source);
		name=source.getName();
		type=source.getType();
		alias=name+"2";
	}
	
	public Long getId()	{
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long)id;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name=name;
	}
	
	@Override
	public String getDisplayName() {
		return name;
	}
	
	@Override
	public String getAlias() {
		return alias!=null?alias:getName();
	}
	
	public void setAlias(String name) {
		this.alias = name;
	}
	
	public ResourceGroupType getType() {
		return this.type;
	}
	
	public void setType(ResourceGroupType type) {
		this.type = type;
	}
	
	public void setMultiple(boolean value) {
		this.multiple = value;
	}
	
	public boolean isMultiple() {
		return this.multiple;
	}
	
	public void setDefault(boolean value) {
		this.defaultTag = value;
	}
	
	public boolean isDefault() {
		return this.defaultTag;
	}
	

	public void isInNewContentTekplates(boolean value) {
		this.newcontenttempplate=value;
	}
	
	
	public boolean isInNewContentTemplates() {
		return this.newcontenttempplate;
	}

	
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof KbeeResourceTag)) return false;
		return ((KbeeResourceTag)object).getId().equals(getId());
	}
	
	@Override
	public AuditSet getAuditSet() {
		return AuditSet.RESOURCE;
	}
}