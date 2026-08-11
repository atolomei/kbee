package com.novamens.kbee.content.base;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.novamens.content.base.Source;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.security.audit.AuditSet;

/**
 * 
 * 
 *
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "KB_SOURCE")
public class KbeeSource extends AbstractObject implements Source   {
	
	@Id
	@SequenceGenerator(name = "source_sequencer", sequenceName = "domainid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "source_sequencer")
	@Column(name = "id")
	private Long id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "display_name")
	private String displayName;
 
	@Override
	public Long getId()	{
		return id;
	}
	
	@Override
	public void setId(Serializable id) {
		this.id = (Long)id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDisplayName() {
		return displayName!=null?displayName:getName();
	}
	
	public void setDisplayName(String name) {
		this.displayName = name;
	}
	
	@Override
	public AuditSet getAuditSet() {
		return AuditSet.CONTENT;
	}
}
