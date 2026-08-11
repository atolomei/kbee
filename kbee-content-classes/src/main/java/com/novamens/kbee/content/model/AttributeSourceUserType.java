package com.novamens.kbee.content.model;

import com.novamens.content.base.PersistentEnumUserType;
import com.novamens.content.model.AttributeSource;

public class AttributeSourceUserType extends PersistentEnumUserType<AttributeSource> {

	@Override
	public Class<AttributeSource> returnedClass() {
		return AttributeSource.class;
	}
}
