package com.novamens.kbee.content.properties;

import com.novamens.content.base.PersistentEnumUserType;
import com.novamens.content.properties.PropertyType;

public class KbeePropertyType extends PersistentEnumUserType<PropertyType> {
	@Override
	public Class<PropertyType> returnedClass() {
		return PropertyType.class;
	}
}
