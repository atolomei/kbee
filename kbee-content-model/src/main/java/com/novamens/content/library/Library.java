package com.novamens.content.library;

import java.io.Serializable;

import com.novamens.content.base.Content;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.dom.Indexable;
import com.novamens.indexer.query.Criteria;
import com.novamens.security.Auditable;
import com.novamens.security.Identifiable;
import com.novamens.security.audit.AuditSet;

public interface Library extends com.novamens.dom.Object, DomainObject, Indexable, Identifiable, Auditable  {

	String STANDARD		= "standard";
	String TEMPLATES	= "templates";
	String EXTERNAL		= "external";
	String KBASE		= "kbase";
	String ALL			= "all";
	
	public Serializable getId();
	public String getDisplayName();
	public Criteria getCriteria();
	public Domain getDomain();
	
	public boolean includes(Content content);
	
	public boolean isReadOnly();
	public boolean isReadable();
	public boolean isEnabled();
	
	public boolean isCanonical();
	
	public int getOrder();
	
	public String getKey();
	public String getDisplayCriteria();
	public String getDescription();
	public String getPage();
	
	public default AuditSet getAuditSet() {
		return AuditSet.SYSTEM;
	}
}