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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Type;

import com.novamens.content.base.Content;
import com.novamens.content.properties.Property;
import com.novamens.content.properties.PropertyType;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.base.KbeeContent;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
@Table(name = "Property")
@DynamicInsert
public class KbeeProperty implements Property {
	
	@Id @GeneratedValue
	@Column(name = "ID")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeContent.class)
	@JoinColumn(name="content_id")
	private Content content;
	
	@Column(name = "type")
	@Enumerated(EnumType.ORDINAL)
	@Type(type="com.novamens.kbee.content.properties.KbeePropertyType")
	private PropertyType type;
	
	@Column(name = "name")
	private String name;
	
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
	
	public Content	getContent() {
		return content;
	}
	
	public void setContent(Content content) {
		this.content=content;
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

	@Override
	public Domain getDomain() {
		return getContent()!=null?getContent().getDomain():null;
	}
}
