package com.novamens.portal.service;


import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.entity.Person;
import com.novamens.content.library.Library;
import com.novamens.dom.Domain;
import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.AreaSection;
import com.novamens.portal6.model.AreaType;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.PageSection;
import com.novamens.portal6.model.PageSectionDisposition;
import com.novamens.portal6.model.PageSectionType;
import com.novamens.portal6.model.PortalPersistentMenu;
import com.novamens.portal6.model.PortalService;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.ViewBKIQL;
import com.novamens.portal6.model.block.ListViewBlock;
import com.novamens.service.BusinessSystemService;
import com.novamens.service.FactoryService;

/**
 * 
 * - Biblioteca -> buscador sobre contenidos de la Biblioteca
 * - Sitio de Proyecto
 * 
 *
 */
public interface SiteFactoryService extends BusinessSystemService, FactoryService, PortalService {
					
	
	
	
	/**
	 *  Plantillas de Portal
	 *  -------------------- 
	 *  
	 *  Library 
	 *  HelpCenter
	 *  Manual
	 *  Project 
	 *  
	 *  -------------
	 *  
	 *  LIBRARY
	 *  -------
	 *  
	 *  1. Background photo
	 *  2. Advanced Search_Form
	 *  3. Home Widgets ->  
	 *  
	 *  
	 *  - My Tasks 
	 *  - Pending Tasks 
	 *  - Library
	 *  - New File
	 *  - My Notes 
	 *  
	 *  Contenidos publicados explicitamente (listblock), 
	 *  (textblock)
	 *  
	 *  ---
	 *  Results
	 *  -------
	 *  HitPanel_1
	 *  HitPanel_2
	 *  
	 *  Detail
	 *  ------
	 *  LibPanel
	 *  
	 *  Include Related
	 *  
	 *  
	 *  Include Social Tools 
	 *  --------------------  
	 *  Vote
	 *  Comment 
	 *  
	 *  --------------------
	 * 
	 * 
	 * 
	 * @return
	 * @throws ContentCreationException
	 * @throws ContentMgmtException
	 */
	
	
	
	
	
	
	
	public ViewBKIQL createViewQuery(ListViewBlock parent, String title, String iql, String query_type, String description) throws ContentCreationException, 	ContentMgmtException;
	
	public Site createKBaseSite() throws ContentCreationException, 	ContentMgmtException;
	public Site createExternalSite() throws ContentCreationException, ContentMgmtException;
	public Site createDealRoomSite() throws ContentCreationException, ContentMgmtException;
	public Site createMainDashboardSite(Person person) throws ContentCreationException, ContentMgmtException;
	

	
	public Site createKLibrarySite() throws ContentCreationException, ContentMgmtException;
	public Site createGeneralDashboardSite() throws ContentCreationException, ContentMgmtException;
	
	
	
	
	public Site createSite(String name) throws ContentCreationException, ContentMgmtException;
	public Site createPublicWebSite(Person person) throws ContentCreationException, ContentMgmtException;
	public Site createTestSite(Person person) throws ContentCreationException, ContentMgmtException;
	
	public Page 					addNewPage(Site site, String name) 																	throws ContentCreationException, ContentMgmtException;
	public PageSection 				addNewPageSection(Page page, String name, PageSectionType pst, PageSectionDisposition psd)  		throws ContentCreationException, ContentMgmtException;
	public Area 					addNewArea(PageSection ps, String title, AreaType areatype)  										throws ContentCreationException, ContentMgmtException;
	public Area 					addNewArea(PageSection ps, String title, AreaType areatype, String area_class)											throws ContentCreationException, ContentMgmtException;
	
	public Block 				 	addNewBlock(Area area, Block block, AreaSection as, String key) 									throws ContentCreationException, ContentMgmtException;
	public PortalPersistentMenu  	addNewMenu(Site site, String name) throws ContentCreationException, ContentMgmtException;
	
	/** -----------------------------------------------
	 */
	public Site createLibrarySite(Library library) throws ContentCreationException, ContentMgmtException;
	public Site createMiniSite(Domain domain) throws ContentCreationException, ContentMgmtException;

	/** -----------------------------------------------
	 * 
	 * 
	 */
	Site createProjectSite() throws ContentCreationException, ContentMgmtException;

	/**
	 * 
	 */
	void createDataSetSiteProjectsIfNotExists();

	/** 
	 * 
	 * 
	 */
	Site createCorporateSite() throws ContentCreationException, ContentMgmtException;
		
		

}
