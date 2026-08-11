package com.novamens.portal.service;

import java.util.List;

import com.novamens.content.base.Content;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.event.EventListener;
import com.novamens.portal6.model.PortalService;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.BusinessObjectService;

public interface SiteServiceDONOTUSE extends BusinessObjectService, PortalService, EventListener {


	public Site getSite();
	
	public List<User> getAdminUsers();
	public List<User> getWriteUsers();
	public List<User> getReadUsers();

	public void delete() 							throws ContentMgmtException;
	public void save() 								throws ContentMgmtException;
	public void recycle() 							throws ContentMgmtException;
	
	public void update(List<String> updatedParts) 	throws ContentMgmtException;
	public void update(String description) 			throws ContentMgmtException;

	
	public Group getAdminGroup();
	public Group getReadGroup();
	public Group getWriteGroup();

	public String getAdminGroupStr();
	public String getReadGroupStr();
	public String getWriteGroupStr();

	
	public void saveAndAddDetailView(Content content) throws ContentMgmtException;

	//public void update(Block  block) 				throws ContentMgmtException;
	//public void delete(Block block) 				throws ContentMgmtException;
	//public void delete(ViewBK c) 					throws ContentMgmtException;
	//public ViewDetailContent addViewDetailContent(Content content) throws ContentMgmtException,  ContentCreationException;
	// public List<BlockFactory> getBlockFactories();


	

	
}
