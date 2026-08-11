package com.novamens.kbee.content.model;

import com.novamens.content.base.PersistentEnumUserType;
import com.novamens.content.model.DataSetType;

public class DataSetTypeUserType extends PersistentEnumUserType<DataSetType> {
	@Override
	public Class<DataSetType> returnedClass() {
		return DataSetType.class;
	}
}
