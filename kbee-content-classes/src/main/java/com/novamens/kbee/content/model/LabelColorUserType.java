package com.novamens.kbee.content.model;

import com.novamens.content.base.PersistentEnumUserType;
import com.novamens.content.model.LabelColor;

public class LabelColorUserType extends PersistentEnumUserType<LabelColor> {

	@Override
	public Class<LabelColor> returnedClass() {
		return LabelColor.class;
	}
}
