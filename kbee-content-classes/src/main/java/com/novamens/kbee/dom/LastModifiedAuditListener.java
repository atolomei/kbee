package com.novamens.kbee.dom;

import javax.persistence.PrePersist;

import com.novamens.security.Auditable;

public class LastModifiedAuditListener {

	@PrePersist
	public void setCreatedAt(final Auditable object) {
		object.setDefaultAudit();				
	}
}
