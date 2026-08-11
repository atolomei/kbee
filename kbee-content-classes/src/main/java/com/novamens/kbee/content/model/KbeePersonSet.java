package com.novamens.kbee.content.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.PersonSet;
import com.novamens.kbee.content.entity.KbeePerson;

@Entity
@DiscriminatorValue(value="3")
public class KbeePersonSet extends KbeeDataSet implements PersonSet {
	
	static final public String defaultTitleRule = "${firstname} ${lastname}";
	static final public String defaultAlternateTitleRule = "${lastName}, ${firstName}";
	
	
	public KbeePersonSet() {
		super();
		setDataSetType(DataSetType.PEOPLE);
	}
	
	public DataSetMember createMember() {
		KbeePersonMember member = new KbeePersonMember(this);
		member.setPerson(new KbeePerson());
		return member;
	}
	
	public boolean hasEntityGroup() {
		return false;
	}
	
	@Override
	public DataSet clone() {
		KbeePersonSet clone = new KbeePersonSet();
		super.clone(clone);
		return clone;
	}
}
