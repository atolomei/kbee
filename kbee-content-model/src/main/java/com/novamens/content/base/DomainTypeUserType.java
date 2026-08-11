package com.novamens.content.base;


import java.io.Serializable;
import com.novamens.dom.DomPersistentEnumUserType;
import com.novamens.dom.DomainType;

public class DomainTypeUserType extends DomPersistentEnumUserType<DomainType> implements Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public Class<DomainType> returnedClass() {
		return DomainType.class;
	}

}
