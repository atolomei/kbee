package com.novamens.content.form;

import java.io.Serializable;

import com.novamens.content.base.PersistentEnumUserType;

public class EDispositionUserType extends PersistentEnumUserType<EDisposition> implements Serializable {
	private static final long serialVersionUID = 1L;

	@Override
	public Class<EDisposition> returnedClass() {
		return EDisposition.class;
	}
}