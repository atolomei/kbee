package com.novamens.content.form;

import java.io.Serializable;

import com.novamens.content.base.PersistentEnumUserType;

public class EFormAccessLevelUserType extends PersistentEnumUserType<EFormAccessLevel> implements Serializable {
	private static final long serialVersionUID = 1L;

	@Override
	public Class<EFormAccessLevel> returnedClass() {
		return EFormAccessLevel.class;
	}
}