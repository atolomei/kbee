package com.novamens.kbee.content.model;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;


@Deprecated
@Entity
@DiscriminatorValue(value="2")
public class KbeeDateSet extends KbeeDataSet {
	
	public KbeeDateSet() {
		super();
		setDataSetType(DataSetType.DATE);
		setSuggester(false);
	}
	
	public KbeeDateSet(String name) {
		super(name, DataSetType.DATE);
	}
	 
	public DataSetMember createMember() {
		DataSetMember member = new KbeeValueMember(this);
		return member;
	}
	
	@Override
	public DataSet clone() {
		KbeeDateSet clone = new KbeeDateSet();
		super.clone(clone);
		return clone;
	}


}



