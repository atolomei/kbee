package com.novamens.portal6.model;

import com.novamens.dom.DomPersistentEnumUserType;

public class PageSectionDispositionUserType extends DomPersistentEnumUserType<PageSectionDisposition> {
	@Override
	public Class<PageSectionDisposition> returnedClass() {
		return PageSectionDisposition.class;
	}
}
