package com.novamens.kbee.system;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;

import com.novamens.system.SystemParameter;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "kb_system_properties")
@DynamicInsert
public class KbeeSystemParameter implements SystemParameter {

	@Id 
	@Column(name = "key")
	private String 	key;
	
	@Column(name = "value")
	private String 	value;
	
	@Column(name = "area")
	private String 	area;
	
	
	public KbeeSystemParameter() {
	}
			
	public KbeeSystemParameter(String key, String value) {
		this.key=key;
		this.value=value;
	}

	public String getKey() {
		return key;
	}
	
	public String getValue() {
		return value;
	}

	@Override
	public void setKey(String key) {
		this.key=key;
		
	}

	@Override
	public void setValue(String value) {
		this.value=value;
		
	}
}
