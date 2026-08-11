package com.novamens.kbee.content.searcher;

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
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Type;

import com.novamens.content.searcher.SearcherHomeBlock;
import com.novamens.dom.Json;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.security.audit.AuditSet;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name = "KB_SEARCHER_HOMEBLOCK")
@DynamicInsert
public class KbeeSearcherHomeBlock extends AbstractObject implements SearcherHomeBlock {
			
	//private static com.novamens.logging.Logger logger = com.novamens.logging.Logger.getLogger(KbeeSearcherHomeBlock.class.getName());

	@Id 
	@SequenceGenerator(name = "member_sequencer", sequenceName = "entityid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_sequencer")
	@Column(name = "ID")
	private Long id;
	
	@Column(name = "lang")
	private String 	language;  // ISO 639‑3 (3 letters code)

	@Column(name = "title")
	private String 	title;
	
	@Column(name = "abstract")
	private String 	des;

	@Column(name = "name")
	private String name;
	
	@Column(name = "iql")
	private String iql;
	
	@Column(name = "sortstr")
	private String sortstr;
	
	@Column(name = "formatstr")
	private String formatstr;
	
	@Column(name = "custom_values")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.json.JsonType")
	private Json custom_values;
	
	@Override
	public Json getCustomValuesJson() {
		return custom_values;
	}

	@Override
	public String getAbstract() {
		return this.des;
	}
	
	public void setAbstract(String d) {
		this.des=d;
	}
	
	@Override
	public String getSortStr() {
		return sortstr;
	}

	@Override
	public String getIQL() {
		return iql;
	}
	
	@Override
	public String getTitle() {
		return title;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setId(Serializable id) {
		this.id = (Long) id;
	}

	@Override
	public Serializable getId() {
		return this.id;
	}
	
	@Override
	public AuditSet getAuditSet() {
		return AuditSet.SYSTEM;
	}
}
