package com.novamens.kbee.content.service.domain;

import java.util.Properties;
import java.io.Serializable;
import java.util.Map.Entry;

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
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.service.DomainPreferences;
import com.novamens.dom.Domain;
import com.novamens.kbee.domain.KbeeDomain;
import com.novamens.kbee.preferences.KbeePreferences;
import com.novamens.preferences.Preferences;


@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
@Table(name = "kb_preference_domain")
@DynamicInsert
public class KbeeDomainPreferences implements DomainPreferences {

	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePreferences.class.getName());
	
	@Id 
	@SequenceGenerator(name = "preference_sequencer", sequenceName = "objectid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "preference_sequencer")
	@Column(name = "ID")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeDomain.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "domain_id")
	private Domain domain;

	@Column(name = "name")
	private String name;
	
	@Column(name = "properties")
	private String properties;
	
	@Transient
	private Properties prop;
	
	
	public KbeeDomainPreferences() {
	}

	public KbeeDomainPreferences(Domain domain, String name) {
		this.domain=domain;
		this.name=name;
	}

	@Override
	public Serializable getId() {
		return (Serializable) id;
	}
	 
	@Override
	public void setId(Long id) {
		this.id=id;
	}
	 
	@Override
	public String getName() {
		return name;
	}

	@Override
	public Domain getDomain() {
		return domain;
	}

	@Override
	public void setName(String name) {
		this.name=name;
	}

	@Override
	public void setPreference(String keysrc, String valuesrc) {
		
		if (keysrc==null)
			return;
		
		if (this.prop==null)
			loadProperties();
		
		if (valuesrc==null)
			this.prop.remove(keysrc);
		else
			this.prop.put(keysrc, valuesrc);
		
		StringBuilder buil = new StringBuilder();
		for (Entry<Object, Object> entry: this.prop.entrySet()) {
			if (buil.length()>0)
				buil.append("|");
			buil.append( ((String)entry.getKey()) + ":" + ((String)entry.getValue()));
		}
		this.properties=buil.toString();
	}
	

	@Override
	public String getPreference(String key) {
		if (this.prop==null)
			loadProperties();
		return this.prop.getProperty(key);	
	}


	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		if (id!=null)
			str.append("id " + id.toString() + " | ");
		str.append(" name: " + name);
		if (domain!=null)
			str.append(" | domain: " + domain.getDisplayName());
		return str.toString();
	}
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof Preferences) 
			return getId().equals(((Preferences) object).getId()); 
		return false;
	}


	@Override
	public int hashCode() {
		int hash= super.hashCode(); 
		hash = this.id!=null?this.id.intValue():0;
		if (this.properties!=null)
			hash += this.properties.hashCode();
		if (this.name!=null)
			hash += this.name.hashCode();
		return hash;
 	}

	
	private void loadProperties() {
		prop=new Properties();
		if (this.properties!=null) {
			String val2 []= this.properties.split("\\|");
			for (String str: val2) {
				String el [] = str.split("\\:"); 
				if (el.length>1) {
					this.prop.put(el[0], el[1]);
				}
			}
  		}
	}

}
