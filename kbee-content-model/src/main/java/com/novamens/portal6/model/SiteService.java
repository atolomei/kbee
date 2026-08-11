package com.novamens.portal6.model;

import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.service.BusinessObjectService;

public interface SiteService extends PortalService, BusinessObjectService {

	public Site getSite();

	public void delete() 										throws ContentMgmtException;
	public void markAsDeleted() 								throws ContentMgmtException;
	
	public void save() 											throws ContentMgmtException;
	public void recycle() 										throws ContentMgmtException;

	public void update(List<String> updatedParts) 				throws ContentMgmtException;
	public void update(String description) 						throws ContentMgmtException;
	
	public String getUrl(Content content);
}