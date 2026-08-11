package com.novamens.content.base;

public class ResourceGroupUserType extends PersistentEnumUserType<ResourceGroupType> {
	@Override
	public Class<ResourceGroupType> returnedClass() {
		return  ResourceGroupType.class;
	}
}