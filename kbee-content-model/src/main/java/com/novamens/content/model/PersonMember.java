package com.novamens.content.model;

import com.novamens.content.entity.Person;
import com.novamens.security.audit.AuditSet;

public interface PersonMember extends DataSetMember, Person, EntityMember {
	public Person getPerson();
	public default AuditSet getAuditSet() {
		return AuditSet.ENTITY;
	}
}
