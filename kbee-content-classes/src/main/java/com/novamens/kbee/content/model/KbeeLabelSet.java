package com.novamens.kbee.content.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.LabelSet;


/**
 * Labels are like String DataSets, but displayed with a graphic icon
 */
@Entity
@DiscriminatorValue(value="10")
public class KbeeLabelSet extends KbeeDataSet implements LabelSet {
			
	public KbeeLabelSet() {
		super();
		setDataSetType(DataSetType.LABEL);
		setSuggester(false);
	}
	
	public KbeeLabelSet(String name) {
		super(name, DataSetType.LABEL);
	}
	
	@Override
	public DataSetMember createMember() {
		return new KbeeLabelMember(this);
	}

	public DataSet clone() {
		KbeeLabelSet clone = new KbeeLabelSet();
		super.clone(clone);
		return clone;
	}
	
}
