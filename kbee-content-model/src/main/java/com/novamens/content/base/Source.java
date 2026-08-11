package com.novamens.content.base;

import com.novamens.dom.DomainObject;
import com.novamens.security.Auditable;

import java.io.Serializable;

/**
 * <p>A Source is an external application that submits Files via the API (examples: onesite, accounting</p>
 * 
 *
 */
public interface Source extends com.novamens.dom.Object, DomainObject, Auditable {

	public Serializable getId();
	public String getName();
	public String getDisplayName();
}
