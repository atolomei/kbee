package com.novamens.portal6.model;


import com.novamens.dom.DomPersistentEnumUserType;

public class SiteTemplateUserType extends DomPersistentEnumUserType<SiteTemplate> {

	@Override
	public Class<SiteTemplate> returnedClass() {
		return SiteTemplate.class;
	}
}
