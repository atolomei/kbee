package com.novamens.portal6.model;

import com.novamens.dom.DomPersistentEnumUserType;

public class AreaTypeUserType extends DomPersistentEnumUserType<AreaType> {
			
	@Override
	public Class<AreaType> returnedClass() {
		return AreaType.class;
	}
	
}
