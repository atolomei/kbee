package com.novamens.portal6.model;



import com.novamens.dom.DomPersistentEnumUserType;

/**
 * Used to store {@link SiteType} in the Database via Hibernate
 * 
  
 *
 */
public class SiteTypeUserType extends DomPersistentEnumUserType<SiteType> {
	
	@Override
	public Class<SiteType> returnedClass() {
		return SiteType.class;
	}
	
}

