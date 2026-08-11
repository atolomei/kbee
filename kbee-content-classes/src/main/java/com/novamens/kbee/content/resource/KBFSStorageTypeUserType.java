package com.novamens.kbee.content.resource;

import java.io.Serializable;

import com.novamens.dom.KBFSStorageType;



public class KBFSStorageTypeUserType  extends com.novamens.content.base.PersistentEnumUserType<KBFSStorageType> implements Serializable  {

	private static final long serialVersionUID = 1L;

	@Override 
	public Class<KBFSStorageType> returnedClass() {
		return KBFSStorageType.class;
	}

	

}
