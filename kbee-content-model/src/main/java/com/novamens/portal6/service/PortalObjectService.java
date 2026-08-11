package com.novamens.portal6.service;

import java.util.List;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.portal6.model.PortalService;
import com.novamens.service.BusinessObjectService;

public interface PortalObjectService extends BusinessObjectService, PortalService {

	
	public void delete() 										throws ContentMgmtException;
	public void save() 										throws ContentMgmtException;
	
	// public void updateNoTrx() 									throws ContentMgmtException;
	//public void update(List<String> updatedParts) 				throws ContentMgmtException;
	//public void update(String part) 							throws ContentMgmtException;
	
	public void archive()								throws ContentMgmtException;
	public void unArchive() 							throws ContentMgmtException;
	public void recycle() 								throws ContentMgmtException;
	public void restore() 								throws ContentMgmtException;
	
}
