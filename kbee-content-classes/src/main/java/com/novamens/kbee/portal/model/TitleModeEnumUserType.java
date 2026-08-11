package com.novamens.kbee.portal.model;

import java.io.Serializable;

import com.novamens.content.base.PersistentEnumUserType;
import com.novamens.portal6.model.TitleMode;

public class TitleModeEnumUserType extends PersistentEnumUserType<TitleMode> implements Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public Class<TitleMode> returnedClass() {
		return TitleMode.class;
	}

}
