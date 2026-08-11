package com.novamens.portal6.service;

import java.util.List;

import com.novamens.event.EventListener;
import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.PortalService;

import com.novamens.service.BusinessObjectService;

public interface SiteModelService extends BusinessObjectService, PortalService, EventListener {

	public List<Page> getSimplePages();
	public List<Area> getSimplePages(Page page);
	public List<Block> getSimplePages(Area area);
	
	public Page getPage(String key);
	
}
