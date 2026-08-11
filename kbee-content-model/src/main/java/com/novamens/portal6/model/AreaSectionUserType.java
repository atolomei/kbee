package com.novamens.portal6.model;

import com.novamens.dom.DomPersistentEnumUserType;

public class AreaSectionUserType extends DomPersistentEnumUserType<AreaSection> {

	@Override
	public Class<AreaSection> returnedClass() {
		return AreaSection.class;
	}
}

