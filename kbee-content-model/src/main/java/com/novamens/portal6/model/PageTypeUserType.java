package com.novamens.portal6.model;

import com.novamens.dom.DomPersistentEnumUserType;

public class PageTypeUserType extends DomPersistentEnumUserType<PageType> {
	
	@Override
	public Class<PageType> returnedClass() {
		return PageType.class;
	}
	
}
