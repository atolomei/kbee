package com.novamens.portal6.model;

import com.novamens.dom.DomPersistentEnumUserType;

public class PageSectionTypeUserType extends DomPersistentEnumUserType<PageSectionType> {
				
	@Override
	public Class<PageSectionType> returnedClass() {
		return PageSectionType.class;
	}

}

 