package com.novamens.portal6.model;


import java.io.Serializable;
import java.util.Map;

import com.novamens.dom.DomainObject;
import com.novamens.dom.Json;
import com.novamens.dom.Versionable;
import com.novamens.security.audit.AuditSet;


/**
 * This is equivalent to {@link Content} Interface in kbee-content
 */
public interface PortalObject  extends com.novamens.dom.Object, DomainObject, Versionable<PortalObject> {

	
	public String getTitle();
	public void setTitle(String string);

	public String getSubtitle();
	public void setSubtitle(String string);
	
	public String getDescription();
	public void setDescription(String string);
	
	public Serializable getOId();
	public String getName();

	public String getKey();
	public void setKey(String key);
	
	public Site getSite();
	public PortalObject getParent();
	
	public String getMetadataAsString();
	public String getLanguage();
	
	public boolean isPublished();
	public String getDisplayName();
	public Json getCustomValuesJson();
	
	@Override
	public default AuditSet getAuditSet() {	return AuditSet.PORTAL;	}
	
	public Map<String, String> getGeneralInfo();
	public Map<String, String> getSpecificInfo();
	
	public String getUsageInfoKey();
	public void setUsageInfoKey(String i);
	
	public String getClassKey();
	public String getDataProviderInfo();
	
	
	
}


