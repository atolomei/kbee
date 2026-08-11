package com.novamens.portal.factory;

import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.PortalObject;

public interface PortalObjectFactory<T extends PortalObject> {

	public String 	getId();
	public String 	getKey();
	
	public String 	getTitle();
	public String 	getDisplayName();
	public String 	getUsageInfoKey();
	
	public T create();
	
	

}
