package com.novamens.portal.service;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.portal6.model.PortalService;
import com.novamens.service.BusinessObjectService;

public interface ViewService extends BusinessObjectService, PortalService {
	
	public void save() throws ContentMgmtException;
	public void delete() throws ContentMgmtException;

	// for ViewDetailContent
	//
	// public void updateContent(Content content) throws ContentMgmtException;
	
	public String getSubtitle();

}
