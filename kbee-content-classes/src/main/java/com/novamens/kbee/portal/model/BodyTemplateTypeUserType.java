package com.novamens.kbee.portal.model;

import java.io.Serializable;

import com.novamens.content.base.PersistentEnumUserType;
import com.novamens.portal6.model.BodyTemplateType;

public class BodyTemplateTypeUserType extends PersistentEnumUserType<BodyTemplateType> implements Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public Class<BodyTemplateType> returnedClass() {
		return BodyTemplateType.class;
	}

}
