package com.novamens.kbee.content.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.UserSet;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.entity.KbeePerson;
import com.novamens.kbee.content.user.KbeeUserProfile;


/**
 *  Canonical DataSet asociated to the Users
 */
@Entity
@DiscriminatorValue(value="4")
public class KbeeUserSet extends KbeeDataSet implements UserSet {

	public KbeeUserSet() {
		super();
	}
	
	public KbeeUserSet(String nombre, DataSetType type) {
		super(nombre, type);
	}

	public DataSetMember createMember() {

		KbeePersonMember member = new KbeePersonMember(this);
		KbeePerson person = new KbeePerson(); 
		KbeeUserProfile userProfile = new KbeeUserProfile();
		person.addProfile(userProfile);
		member.setPerson(person);
		member.setState(ObjectState.ENABLED);
		return member;
	}
	

	@Override
	public DataSet clone() {
		KbeeUserSet clone = new KbeeUserSet();
		super.clone(clone);
		return clone;
	}
}
