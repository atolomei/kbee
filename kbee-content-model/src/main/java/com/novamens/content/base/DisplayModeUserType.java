package com.novamens.content.base;

public class DisplayModeUserType extends PersistentEnumUserType< DisplayMode> {

	@Override
	public Class< DisplayMode> returnedClass() {
		return  DisplayMode.class;
	}
}
