package com.novamens.portal6.model;

import com.novamens.dom.DomainObject;
import com.novamens.security.Identifiable;

/**
 * 
 * Block
 * Area
 * PageSection
 * Page
 * Site
 * 
 * 
 * @author atolo
 *
 */
public interface PortalModel extends DomainObject, Identifiable {
	
	public boolean isPayloadEditor();
	public void setPayloadEditor(boolean b);
	
	
}
