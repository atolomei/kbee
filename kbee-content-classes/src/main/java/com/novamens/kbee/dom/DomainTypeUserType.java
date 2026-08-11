package com.novamens.kbee.dom;

import com.novamens.content.base.PersistentEnumUserType;
import com.novamens.dom.DomainType;

public class DomainTypeUserType extends PersistentEnumUserType<DomainType> {

	@Override
	public Class<DomainType> returnedClass() {
		return DomainType.class;
	}
}
