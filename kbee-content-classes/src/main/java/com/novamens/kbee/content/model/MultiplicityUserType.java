package com.novamens.kbee.content.model;

import com.novamens.content.base.PersistentEnumUserType;
import com.novamens.content.model.Multiplicity;

public class MultiplicityUserType extends PersistentEnumUserType<Multiplicity> {

	@Override
	public Class<Multiplicity> returnedClass() {
		return Multiplicity.class;
	}
}
