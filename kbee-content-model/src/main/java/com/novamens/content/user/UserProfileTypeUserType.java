package com.novamens.content.user;

import com.novamens.content.base.PersistentEnumUserType;

public class UserProfileTypeUserType extends PersistentEnumUserType<UserProfileType> {

	@Override
	public Class<UserProfileType> returnedClass() {
		return  UserProfileType.class;
	}
}
