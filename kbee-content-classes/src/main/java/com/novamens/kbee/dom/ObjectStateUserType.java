package com.novamens.kbee.dom;

import java.io.Serializable;

import com.novamens.content.base.PersistentEnumUserType;
import com.novamens.dom.ObjectState;

public class ObjectStateUserType extends PersistentEnumUserType<ObjectState> implements Serializable {
	private static final long serialVersionUID = 1L;

	@Override
	public Class<ObjectState> returnedClass() {
		return ObjectState.class;
	}
}