package com.novamens.kbee.content.service.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.codesnippets4all.json.exceptions.JSONParsingException;
import com.novamens.content.service.domain.DomainSettings;
import com.novamens.dom.Domain;
import com.novamens.dom.Json;

import com.novamens.kbee.domain.KbeeDomain;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.security.Identifiable;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name = "kb_domain_settings")
@DynamicInsert
public class KbeeDomainSettings implements DomainSettings, Identifiable  {
	
	static Logger logger = LogManager.getLogger( KbeeDomainSettings.class.getName());
	
	@Id 
	@SequenceGenerator(name = "entity_sequencer", sequenceName = "entityid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_sequencer")
	@Column(name = "ID")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeDomain.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "domain_id", updatable=false)
	private Domain domain;
	
	@Column(name = "category")
	private String category;
	
	@Column(name = "values_json")
	private String values_json;

	@Transient
	private Json values;

	
	public KbeeDomainSettings() {
	}

	public KbeeDomainSettings(Domain domain) {
		this.domain=domain;
		this.category=domain.getName();
	}

	public KbeeDomainSettings(Domain domain, String category) {
		this.domain=domain;
		this.category=category;
	}

	
	public void setId(Long id) {
		this.id=id;
	}

	public Long getId() {
		return this.id;
	}
	
	@Override
	public void setDomain(Domain domain) {
		this.domain=domain;
	}

	@Override
	public void setCategory(String category)  {
		this.category=category;
	}
	
	@Override
	public String getCategory() {
		return category;
	}
	
	@Override
	public void setValues(Json values) {
		this.values=values;
		this.values_json = values.toString();
	}

	@Override
	public Json getValues() {
		try {
		if(values==null && values_json!=null)
			values = new KbeeJson(values_json);
			return values;
		
		} catch (JSONParsingException e) {
			logger.error(e.getClass().getName(), e);
			return new KbeeJson();
		}
	}
	
	@Override
	public int hashCode() {
		        int hash = 1;
		        hash = hash * 17 + (this.getDomain()!=null   ? this.getDomain().hashCode()   : 0);
		        hash = hash * 31 + (this.getCategory()!=null ? this.getCategory().hashCode() : 0);
		        hash = hash * 13 + (this.getValues_json()!=null ? this.getValues_json().hashCode() : 0);
		        return hash;
	}
	

	@Override
	public boolean equals(Object s) {
		
		if (! (s instanceof KbeeDomainSettings))
			return false;
		
		KbeeDomainSettings set = (KbeeDomainSettings) s;
		
		if  (getDomain()==null || ((KbeeDomainSettings) set).getDomain()==null || getCategory()==null || set.getCategory()==null)
			return false;
		
		return this.domain.getId().toString().equals( ((KbeeDomainSettings) set).getDomain().getId().toString()) &&
			 this.category.equals(set.getCategory()); 
	}

	
	public Domain getDomain() {
		return domain;
	}

	public String getValues_json() {
		return values_json;
	}

	public void setValues_json(String values_json) {
		this.values_json = values_json;
	}
	
	@Override
	public String getDisplayName() {
		return (getDomain()!=null?getDomain().getName():"-");
	}

}

