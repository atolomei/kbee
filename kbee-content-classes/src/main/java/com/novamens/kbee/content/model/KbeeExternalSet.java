package com.novamens.kbee.content.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.springframework.util.Assert;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.ExternalSet;

@Entity
@DiscriminatorValue(value="6")
public class KbeeExternalSet extends KbeeDataSet implements ExternalSet {

	static public int EXTERNAL_GENERAL  		= 1; 
	static public int EXTERNAL_SITE 			= 2; // Portals (kbee-portal, dr bit, etc.)
	static public int EXTERNAL_SITE_REPOSITORY 	= 3; 
	static public int EXTERNAL_SITE_PROJECT 	= 4;
	
	
	@Column(name = "external_subtype")
	private int external_subtype = EXTERNAL_GENERAL ;
	
	
	public KbeeExternalSet() {
		super();
		setDataSetType(DataSetType.EXTERNAL);
	}
	
	public KbeeExternalSet(String name, DataSetType type) {
		super(name, type);
	}

	public DataSetMember createMember() {
		Assert.isTrue(true, "not allowed");
		return null;
	}
	
	@Override
	public boolean isExternal() { 
		return true;
	}
	
	@Override
	public DataSet clone() {
		KbeeExternalSet clone = new KbeeExternalSet();
		super.clone(clone);
		clone.setExternalSubtype(this.external_subtype);
		return clone;
	}
	
	public int getExternalSubtype() {
		return this.external_subtype;
	}

	public void setExternalSubtype(int n){
		external_subtype=n;
	}
	
}
