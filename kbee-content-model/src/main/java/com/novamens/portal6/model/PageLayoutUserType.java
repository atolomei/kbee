package com.novamens.portal6.model;

import com.novamens.dom.DomPersistentEnumUserType;

public class PageLayoutUserType extends DomPersistentEnumUserType<PageLayoutType> {
			
	@Override
	public Class<PageLayoutType> returnedClass() {
		return PageLayoutType.class;
	}
}
