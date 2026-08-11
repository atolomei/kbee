package com.novamens.kbee.content.model;

import com.novamens.content.base.PersistentEnumUserType;
import com.novamens.content.model.AccessStrategy;

public class AccessStrategyUserType extends PersistentEnumUserType<AccessStrategy> {

	@Override
	public Class<AccessStrategy> returnedClass() {
		return AccessStrategy.class;
	}
}
