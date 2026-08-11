package com.novamens.kbee.content.properties;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

import com.novamens.content.model.ObjectId;
import com.novamens.content.properties.Property;
import com.novamens.content.properties.PropertyType;
import com.novamens.dom.Domain;
import com.novamens.kbee.domain.KbeeDomain;

@Entity
@Table(name = "kb_Object_Property")
@DynamicInsert
public class KbeeObjectProperty implements Property {
	
	@Id @GeneratedValue
	@Column(name = "ID")
	private Long id;
	
	@Column(name = "object_id")
	private String objectId;

	@Column(name = "type")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.content.properties.KbeePropertyType")
	private PropertyType type;
	
	@Column(name = "name")
	private String name;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeDomain.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "domain_id", updatable=false)
	private Domain domain;

	
	@Override
	public Domain getDomain() {
		return domain;
	}

	
	
	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	@Column(name = "value")
	private String stringvalue;
	
	@Column(name = "uset")
	private String set;
	
	@Column(name = "lastModifiedDate")
	private OffsetDateTime lastModifiedDate;

	@Override
	public OffsetDateTime getLastModifiedOffsetDateTime() {
		return lastModifiedDate;
	}
	
	@Override
	public void setLastModifiedOffsetDateTime(OffsetDateTime date) {
		this.lastModifiedDate = date;
	}
	
	@Override
	public Long getId() {
		return id;
	}
	
	public PropertyType getType() {
		return type;
	}
	
	public void setType(PropertyType type) {
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name=name;
	}
	
	public String getObjectId() {
		return objectId;
	}
	
	public void setObject(com.novamens.dom.Object object) {
		this.objectId=(new ObjectId(object)).toString();
	}
	
	public String getStringValue() {
		return stringvalue;
	}
	
	public void setStringValue(String value) {
		this.stringvalue =  value;
	}
	
	@Override
	public String getSet() {
		return set;
	}
		
	@Override
	public void setSet(String set) {
		this.set=set;
	}
	
	public Object getValue() {
		if (getType()!=null && getType().equals(PropertyType.LONG)) {
			return Long.valueOf(getStringValue());
		}
		else
			return getStringValue();
	}
	
	public void setValue(Object value) {
		if (value instanceof Long || value instanceof Integer) {
			setStringValue(String.valueOf(value));
			setType(PropertyType.LONG);
		}
		if (value instanceof String) {
			setStringValue((String)value);
			setType(PropertyType.STRING);
		}
	}
}
