package com.novamens.content.model;

import com.novamens.dom.DomainObject;
import com.novamens.security.Auditable;
import com.novamens.security.audit.AuditSet;

public interface LauncherGroup extends com.novamens.dom.Object, DomainObject, Auditable  {

	public String getName();
	public String getDisplayName();
	public String getAlias();
	public int getOrder();
	boolean isVisible();
	
	public default AuditSet getAuditSet() {
		return AuditSet.MODEL;
	}
}