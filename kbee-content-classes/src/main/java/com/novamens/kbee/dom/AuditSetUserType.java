package com.novamens.kbee.dom;

import java.io.Serializable;

import com.novamens.content.base.PersistentEnumUserType;
import com.novamens.security.audit.AuditSet;

public class AuditSetUserType extends PersistentEnumUserType<AuditSet>  implements Serializable {
											
	private static final long serialVersionUID = 1L;

	@Override
	public Class<AuditSet> returnedClass() {
		return AuditSet.class;
	} 
}
