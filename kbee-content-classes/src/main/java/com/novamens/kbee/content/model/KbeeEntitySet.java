package com.novamens.kbee.content.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.EntitySet;

@Entity
@DiscriminatorValue(value="11")
public class KbeeEntitySet extends KbeeDataSet implements EntitySet {
	
	
	@Column(name = "entity_group")
	private boolean entityGroup;
	
	public KbeeEntitySet() {
		super();
		setDataSetType(DataSetType.ENTITY);
	}
	
	public DataSetMember createMember() {
		KbeeEntityMember member = new KbeeEntityMember(this);
		return member;
	}
	
	public boolean hasEntityGroup() {
		return entityGroup;
	}
	
	public void setEntityGroup(boolean value) {
		this.entityGroup = value;
	}
	
	@Override
	public DataSet clone() {
		KbeeEntitySet clone = new KbeeEntitySet();
		super.clone(clone);
		return clone;
	}
}
