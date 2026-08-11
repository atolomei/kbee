package com.novamens.content.user;

import com.novamens.content.base.PersistentEnumUserType;

public class SignatureTypeUserType extends PersistentEnumUserType<SignatureType> {

	@Override
	public Class<SignatureType> returnedClass() {
		return  SignatureType.class;
	}
}
