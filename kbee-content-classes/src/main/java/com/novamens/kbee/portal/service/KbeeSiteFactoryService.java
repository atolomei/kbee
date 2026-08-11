package com.novamens.kbee.portal.service;



import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.entity.Person;
import com.novamens.content.library.Library;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ExternalDao;
import com.novamens.content.model.ExternalMember;
import com.novamens.content.model.ExternalSet;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.query.SavedQuery;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.content.userlist.UserListService;
import com.novamens.dom.Domain;
import com.novamens.dom.Json;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.event.SiteCreationEvent;
import com.novamens.kbee.content.event.SiteUpdateEvent;
import com.novamens.kbee.content.library.KbeeLibrary;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeDataSet;
import com.novamens.kbee.content.model.KbeeEntitySet;
import com.novamens.kbee.content.model.KbeeExternalSet;
import com.novamens.kbee.content.model.KbeeLabelSet;
import com.novamens.kbee.json.KbeeJson;

import com.novamens.kbee.portal.model.KbeeArea;
import com.novamens.kbee.portal.model.KbeeBlock;
import com.novamens.kbee.portal.model.KbeePage;
import com.novamens.kbee.portal.model.KbeePageSection;
import com.novamens.kbee.portal.model.KbeePortalPersistentMenu;
import com.novamens.kbee.portal.model.KbeeSite;
import com.novamens.kbee.portal.model.KbeeViewBKIQL;
import com.novamens.kbee.portal.model.library.KbeeBlockGenericContentList;
import com.novamens.kbee.sql.SqlPlatform;
import com.novamens.kbee.sql.SqlPlatformFactory;
import com.novamens.logging.DataSetValueCreateEvent;
import com.novamens.logging.ModelCreateEvent;
import com.novamens.portal.factory.AreaFactory;
import com.novamens.portal.factory.BlockFactory;
import com.novamens.portal.service.PortalUrlService;
import com.novamens.portal.service.SiteFactoryService;
import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.AreaSection;
import com.novamens.portal6.model.AreaType;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.PageSection;
import com.novamens.portal6.model.PageSectionDisposition;
import com.novamens.portal6.model.PageSectionType;
import com.novamens.portal6.model.PortalLiteralsService;
import com.novamens.portal6.model.PortalPersistentMenu;
import com.novamens.portal6.model.Site;

import com.novamens.portal6.model.SiteType;
import com.novamens.portal6.model.ViewBKIQL;
import com.novamens.portal6.model.block.ListViewBlock;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

/**
 *
 */
public class KbeeSiteFactoryService implements SiteFactoryService {
																								
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeSiteFactoryService.class.getName());

	/**
	 * Logger Sync with the Trx Thread
	 */
	static private Logger txlogger = LogManager.getLogger("TXLogger");

	private PortalDao portalDao = null;
	private JdbcTemplate jdbcTemplate;
	private SqlPlatform sqlplatform;
	private String schema;
	private ContentDao contentDao;
	
	/**
	 * list of BlockFactory
	 * createBlock
	 * getClass -> (block)
	 */
	public KbeeSiteFactoryService() {
	}

	/** 
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Site createCorporateSite() throws ContentCreationException, ContentMgmtException {
		
		UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		Domain domain = userProfile.getDomain();
		
		Locale locale = domain.getLocale();
		KbeeSite site = (KbeeSite) createSite( domain.getDisplayName());
		
		site.setSiteType(SiteType.INSTITUTIONAL);	
		site.setName(site.getTitle());
		site.setUrl(site.getOId().toString()+"-"+domain.getName());
		site.setKey(Site.INSTITUTIONAL);
		
		getPortalDao().save(site);

		// main top-bar 

		// main top-bar -> logo
		// main top-bar ->  main menu
		
		// main top-bar -> bck
		// main top-bar -> home-banner
		
		// home-area Alert  
		// home-area Contenidos digitales
		// home-area optinice trabajo remoto
		// home-area
		// home-area
		// home-area
		
		Page page_home = addNewPage(site,  getLabel("home", getSessionUser().getLocale())); 	
		page_home.setBuildable(false);
		getPortalDao().save(page_home);
		getPortalDao().save(site);
		page_home.setOrder(0);
		KbeePageSection ps_home = (KbeePageSection) addNewPageSection(page_home, getLabel("main-section", locale), PageSectionType.ONE_SECTION, PageSectionDisposition.LEFT);
		ps_home.setCss("project-home-ps");
		getPortalDao().save(ps_home);
 
		// -----------
		// Site Info
		// Title / Description
		//
		Area area_home_1 = addNewArea(ps_home, getLabel("area_site_info", locale), AreaType.AREA_1S,  "area-site-info");
		getPortalDao().save(area_home_1);
		Block block_home_search  =	addNewBlock(area_home_1, new KbeeBlock("block site info",  getLabel("info", locale)), 	AreaSection.LEFT,  PortalLiteralsService.BLOCK_SITE_INFO);
		getPortalDao().save(block_home_search);

		// -----------
		// Widgets
		//
		// LEFT   -> My Tasks, Monitor, Library
		// RIGHT  -> Members, Text, References 
		
		Area area_home_2 = addNewArea(ps_home, getLabel("area_widgets", locale), AreaType.AREA_2S_60X40, "dashboard");
		getPortalDao().save(area_home_2);
		
		// LEFT  -----------------------
		//
		{
			Block block = 	addNewBlock(area_home_2, new KbeeBlock(getLabel("mytasks", locale), getLabel("mytasks", locale)), AreaSection.LEFT, PortalLiteralsService.BLOCK_MY_TASKS);
			getPortalDao().save(block);
		}
		
		{
			Block block = 	addNewBlock(area_home_2, new KbeeBlock(getLabel("monitor", locale), getLabel("monitor", locale)), AreaSection.LEFT, PortalLiteralsService.BLOCK_MONITOR);
			getPortalDao().save(block);
		}

		{
			Block block = 	addNewBlock(area_home_2, new KbeeBlock(getLabel("library", locale), getLabel("library", locale)), AreaSection.LEFT, PortalLiteralsService.BLOCK_LIBRARY);
			getPortalDao().save(block);
		}

		
		// RIGHT  -----------------------

		// Members
		{
			Block block = addNewBlock(area_home_2, new KbeeBlock(getLabel("info", locale)), 	AreaSection.CENTER, PortalLiteralsService.BLOCK_PORTAL_TEXT);
			block.setTitle( getLabel("info", locale));
			block.setPayloadEditor(true);
			Json json = new KbeeJson();
			String d="this is a text to edit";
			json.put("text", d);
			((KbeeBlock) block).setCustomValuesJson(json);
			block.setCss("dashboard-widget");
			getPortalDao().save(block);
		}

		
		// Text
		{
			Block block = addNewBlock(area_home_2, new KbeeBlock(getLabel("info", locale)), 	AreaSection.CENTER, PortalLiteralsService.BLOCK_PORTAL_TEXT);
			block.setTitle( getLabel("info", locale));
			block.setPayloadEditor(true);
			Json json = new KbeeJson();
			String d="this is a text to edit";
			json.put("text", d);
			((KbeeBlock) block).setCustomValuesJson(json);
			block.setCss("dashboard-widget");
			getPortalDao().save(block);
		}

		// References
		{
			Block block = addNewBlock(area_home_2, new KbeeBlock(getLabel("info", locale)), 	AreaSection.CENTER, PortalLiteralsService.BLOCK_PORTAL_TEXT);
			block.setTitle( getLabel("info", locale));
			block.setPayloadEditor(true);
			Json json = new KbeeJson();
			String d="this is a text to edit";
			json.put("text", d);
			((KbeeBlock) block).setCustomValuesJson(json);
			block.setCss("dashboard-widget");
			getPortalDao().save(block);
		}

		return site;
		
	}

	/** 
	 * 
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Site createProjectSite() throws ContentCreationException, ContentMgmtException {
		
		this.createDataSetSiteProjectsIfNotExists();
		
		UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		Domain domain = userProfile.getDomain();
		
		Locale locale = domain.getLocale();
		KbeeSite site = (KbeeSite) createSite(userProfile.getPerson().getFirstLastName()+ "  " + SiteType.PROJECT.getLabel(locale));
		
		site.setSiteType(SiteType.PROJECT);	// Locale locale = userProfile.getUser().getLocale();
		site.setName(site.getTitle());
		site.setUrl("project"+  site.getOId().toString());
		site.setKey(Site.PROJECT);
		
		getPortalDao().save(site);
		//txlogger.info(new SiteCreationEvent(site));
		
		DataSet portal_projects = getContentDao().findDataSetByAlias(DataSet.PORTAL, getDomain().getId());
		ObjectFactoryService os= ServiceLocator.getService(ObjectFactoryService.class);
		DataSetMember member=(DataSetMember) os.createMember(portal_projects);
		member.setStrValue(site.getTitle());
		
		Page page_home = addNewPage(site,  getLabel("home", getSessionUser().getLocale())); 	page_home.setBuildable(false);
		 
		getPortalDao().save(page_home);
		getPortalDao().save(site);
								
		KbeePageSection ps_home = (KbeePageSection) addNewPageSection(page_home, getLabel("main-section", locale), PageSectionType.ONE_SECTION, PageSectionDisposition.LEFT);
		ps_home.setCss("project-home-ps");
		getPortalDao().save(ps_home);
 
		// -----------
		//
		// Site Info
		// Title / Description
		//
		//
		Area area_home_1 = addNewArea(ps_home, getLabel("area_site_info", locale), AreaType.AREA_1S,  "area-site-info");
		getPortalDao().save(area_home_1);
		Block block_home_search  =	addNewBlock(area_home_1, new KbeeBlock("block site info",  getLabel("info", locale)), 	AreaSection.LEFT,  PortalLiteralsService.BLOCK_SITE_INFO);
		getPortalDao().save(block_home_search);

		
		// -----------
		// Widgets
		//
		
		// LEFT   -> My Tasks, Monitor, Library
		// RIGHT  -> Members, Text, References 
		
		Area area_home_2 = addNewArea(ps_home, getLabel("area_widgets", locale), AreaType.AREA_2S_60X40, "dashboard");
		getPortalDao().save(area_home_2);
		
		// LEFT  -----------------------
		//
		{
		Block block = 	addNewBlock(area_home_2, new KbeeBlock(getLabel("mytasks", locale), getLabel("mytasks", locale)), AreaSection.LEFT, PortalLiteralsService.BLOCK_MY_TASKS);
		getPortalDao().save(block);
		}
		
		{
		Block block = 	addNewBlock(area_home_2, new KbeeBlock(getLabel("monitor", locale), getLabel("monitor", locale)), AreaSection.LEFT, PortalLiteralsService.BLOCK_MONITOR);
		getPortalDao().save(block);
		}

		{
		Block block = 	addNewBlock(area_home_2, new KbeeBlock(getLabel("library", locale), getLabel("library", locale)), AreaSection.LEFT, PortalLiteralsService.BLOCK_LIBRARY);
		getPortalDao().save(block);
		}

		
		// RIGHT  -----------------------

		// Members
		{
			Block block = addNewBlock(area_home_2, new KbeeBlock(getLabel("info", locale)), 	AreaSection.CENTER, PortalLiteralsService.BLOCK_PORTAL_TEXT);
			block.setTitle( getLabel("info", locale));
			block.setPayloadEditor(true);
			Json json = new KbeeJson();
			String d="this is a text to edit";
			json.put("text", d);
			((KbeeBlock) block).setCustomValuesJson(json);
			block.setCss("dashboard-widget");
			getPortalDao().save(block);
		}

		
		// Text
		{
			Block block = addNewBlock(area_home_2, new KbeeBlock(getLabel("info", locale)), 	AreaSection.CENTER, PortalLiteralsService.BLOCK_PORTAL_TEXT);
			block.setTitle( getLabel("info", locale));
			block.setPayloadEditor(true);
			Json json = new KbeeJson();
			String d="this is a text to edit";
			json.put("text", d);
			((KbeeBlock) block).setCustomValuesJson(json);
			block.setCss("dashboard-widget");
			getPortalDao().save(block);
		}

		// References
		{
			Block block = addNewBlock(area_home_2, new KbeeBlock(getLabel("info", locale)), 	AreaSection.CENTER, PortalLiteralsService.BLOCK_PORTAL_TEXT);
			block.setTitle( getLabel("info", locale));
			block.setPayloadEditor(true);
			Json json = new KbeeJson();
			String d="this is a text to edit";
			json.put("text", d);
			((KbeeBlock) block).setCustomValuesJson(json);
			block.setCss("dashboard-widget");
			getPortalDao().save(block);
		}

		return site;
	}
	
	
	/** -----------------------------------------------
	 * 
	 * 
	 */
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Site createLibrarySite(Library library) throws ContentCreationException, ContentMgmtException {

		UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		Domain domain = userProfile.getDomain();
		
		KbeeSite site = (KbeeSite) createSite(library.getDisplayName());
		Locale locale = domain.getLocale();
		
		site.setSiteType(SiteType.LIBRARY);	
		site.setTitle(library.getDisplayName()); 
		site.setName(library.getName());
		
		String ul = (library.getKey()!=null? library.getKey().replace("/", "")  : library.getId().toString());

		site.setUrl(ul);
		
		Json json_site = site.getCustomValuesJson();
		json_site.put("iql", "ishead(true) and validon(today)");
		site.setCustomValuesJson(json_site);
		
		site.setKey(Site.LIBRARY);
		site.setLibrary(library);
		
		getPortalDao().save(site);
		//txlogger.info(new SiteCreationEvent(site));
		
		Page page_home	   = addNewPage(site,  getLabel("home", getSessionUser().getLocale())); 		page_home.setBuildable(true);  			page_home.setOrder(0);
		Page page_results  = addNewPage(site,  getLabel("results", getSessionUser().getLocale())); 		page_results.setBuildable(false); 		page_results.setOrder(1);
		Page page_explorer  = addNewPage(site,  getLabel("explorer", getSessionUser().getLocale())); 	page_explorer.setBuildable(false);		page_explorer.setOrder(2);
		Page page_detail   = addNewPage(site,  getLabel("detail", getSessionUser().getLocale()));  		page_detail.setBuildable(false); 		page_detail.setOrder(3);
		 
		getPortalDao().save(page_home);
		getPortalDao().save(page_results);
		getPortalDao().save(page_explorer);
		getPortalDao().save(page_detail);
		
		getPortalDao().save(site);
								
		
		
		// -----------------------------
		
		
		
		KbeePageSection ps_home = (KbeePageSection) addNewPageSection(page_home, getLabel("main-section", locale), PageSectionType.ONE_SECTION, PageSectionDisposition.LEFT);
		ps_home.setCss("searcher-home-ps");
		getPortalDao().save(ps_home);

		// -----------
		// Search
		//
		Area area_home_1 = addNewArea(ps_home, getLabel("area_search", locale), AreaType.AREA_1S,  "area-search");
		getPortalDao().save(area_home_1);
		Block block_home_search  =	addNewBlock(area_home_1, new KbeeBlock("block searcher",  getLabel("searcher", locale)), 	AreaSection.LEFT,  PortalLiteralsService.BLOCK_SEARCH);
		getPortalDao().save(block_home_search);

		
		// -----------
		// Widgets
		// 
		Area area_home_2 = addNewArea(ps_home, getLabel("area_widgets", locale), 	AreaType.AREA_3S_40x40x20,	"dashboard");
		getPortalDao().save(area_home_2);

		
		// LEFT Library -----------------------
		//
		Block block_home_bl_1 = 	addNewBlock(area_home_2, new KbeeBlock("block widget_L1", getLabel("activity", locale)), 	AreaSection.LEFT, PortalLiteralsService.BLOCK_PORTAL_LIBRARY);
		block_home_bl_1.setTitle(library.getDisplayName());
		getPortalDao().save(block_home_bl_1);

		
		// RIGHT  -----------------------
		// Text
		{
			
			
			Block block_home_br_2 = addNewBlock(area_home_2, new KbeeBlock(getLabel("info", locale)), 	AreaSection.RIGHT, PortalLiteralsService.BLOCK_PORTAL_TEXT );
			block_home_br_2.setTitle( getLabel("info", locale));
			block_home_br_2.setPayloadEditor(true);
			Json json1 = new KbeeJson();
			String d=library.getDescription();
			if (d==null)
				d=library.getDisplayName();
			json1.put("text", d);
			((KbeeBlock) block_home_br_2).setCustomValuesJson(json1);
			block_home_br_2.setCss("dashboard-widget");
			getPortalDao().save( block_home_br_2);
		}

		/**
		 * Aerolíneas Argentinas es la línea aérea de bandera de la República Argentina, dedicada al transporte comercial de pasajeros y carga. La flota se compone por aeronaves de la familia Airbus A330, Boeing 737-700, Boeing 737-800, Boeing 737 MAX 8 y Embraer E190.
		 */
		
		// Dashboard saved queries
		{
			Block block = addNewBlock(area_home_2, new KbeeBlock(getLabel("dashboard-queries", locale)), AreaSection.RIGHT, PortalLiteralsService.BLOCK_DASHBOARD_QUERIES );
			block.setTitle( getLabel("dashboard-queries", locale));
			getPortalDao().save(block);
		}

		
		// List of Categories -------------
		//
		/**
		KbeeBlockListView lis_c= (KbeeBlockListView) addNewBlock(area_home_2, new KbeeBlockListView(getLabel("categories", locale) ), 	AreaSection.CENTER, PortalLiteralsService.BLOCK_PORTAL_LISTVIEW);
		getPortalDao().save(lis_c);
		Json js=lis_c.getCustomValuesJson();
		js.put("show_meta", "no");
		lis_c.setCustomValuesJson(js);
		lis_c.setPayloadEditor(true);
		//if (library instanceof KbeeLibrary)
		//	lis_c.add(createViewQuery(lis_c, library.getDisplayName(), ((KbeeLibrary) library).getStatement() , ViewBKIQL.PARAMETERS_QUERY_TYPE, library.getDescription()));
		// lis_c.add(createViewQuery(lis_c, library.getDisplayName(), "head=true,domain=250,-isknowledgebase=true,member=pmcmember/50632,-isexternal=true,-istemplate=true,sort=modified,state=1,type=[text, idoc],ascending=false" , ViewBKIQL.PARAMETERS_QUERY_TYPE,library.getDescription()));
		
		List<SavedQuery> queries = ((KbeeUser) getSessionUser()).getService(UserListService.class).getSavedQueries(library.getId().toString());
		
		for (SavedQuery sq: queries) {
			if (sq.getParameters()!=null) {
				lis_c.add( createViewQuery(lis_c, sq.getDisplayName(), sq.getStatement(), ViewBKIQL.PARAMETERS_QUERY_TYPE, null));
				logger.debug(sq.toString());
			}
		}
		
		lis_c.setDefaults();
		getPortalDao().save(lis_c);
		**/
		

		
		// CENTER    ----------------------
		// News (iql)
		//
		{
			// show meta
			// max_items
			// query_type
			//  statement
			//																																									
			KbeeBlockGenericContentList block = (KbeeBlockGenericContentList) addNewBlock(area_home_2, new KbeeBlockGenericContentList(getLabel("news", locale)), AreaSection.CENTER, PortalLiteralsService.BLOCK_PORTAL_CONTENT_LIST );
			block.setPayloadEditor(true);
			Json json2 = new KbeeJson();
			json2.put("statement", "ishead(true) and portalbcv(Portada)");
			json2.put("query_type",  ViewBKIQL.IQL_TYPE);
			
			block.setPayloadEditor(true);
			((KbeeBlock) block).setCustomValuesJson(json2);
			block.setDefaults();
			getPortalDao().save(block);
		}
		

		{
			// show meta
			// max_items
			// query_type
			//  statement
			//																																									
			KbeeBlockGenericContentList block = (KbeeBlockGenericContentList) addNewBlock(area_home_2, new KbeeBlockGenericContentList(getLabel("updates", locale)), AreaSection.CENTER, PortalLiteralsService.BLOCK_PORTAL_CONTENT_LIST );
			block.setPayloadEditor(true);
			Json json = new KbeeJson();
			json.put("statement", "ishead (true) and novedad(true)");
			json.put("query_type",  ViewBKIQL.IQL_TYPE);
			block.setPayloadEditor(true);
			((KbeeBlock) block).setCustomValuesJson(json);
			block.setDefaults();
			getPortalDao().save(block);
		}

		
		
		
 
		
		//-----------------------------------------------------------------------------------------------------------------------------
		
 		getPortalDao().save(page_home);
		getPortalDao().save(page_results);
		getPortalDao().save(page_detail);
		getPortalDao().save(page_results);
		getPortalDao().save(ps_home);
		getPortalDao().save(site);
		
		addSiteSections(site);
			
		site.add(page_home);
		site.add(page_results);
		site.add(page_detail);
		site.setKey(library.getKey());
		site.setDescription(library.getDescription());
			
		try {
				Json json_v;
				json_v = site.getCustomValuesJson();
				json_v.put("library", library.getId().toString());
				KbeeSite ksite = (KbeeSite) site;
				ksite.setCustomValuesJson(json_v);
							
			} catch (Exception e) {
				logger.error(e);
				throw e;
			}

		addDataSetValues(domain, site);
		
		getPortalDao().save(site);
		txlogger.info(new SiteUpdateEvent(site));
		
		return site;
		
	}

	
	
	@Override
	public Site createMiniSite(Domain domain) throws ContentCreationException, ContentMgmtException {
		
		KbeeSite site = (KbeeSite) createSite(domain.getDisplayName());
		
		site.setSiteType(SiteType.MINISITE);	// Locale locale = userProfile.getUser().getLocale();
		site.setTitle("Minisite "  + " " + site.getOId().toString()); // resources.getString("searcher")
		site.setName("Minisite "  + " " + site.getOId().toString());
		site.setUrl("msite"+  site.getOId().toString());
		site.setKey(Site.MAIN_DASHBOARD);
		
		getPortalDao().save(site);
		//txlogger.info(new SiteCreationEvent(site));
		
		Page page_home	   = addNewPage(site,  getLabel("home", getSessionUser().getLocale())); 	page_home.setBuildable(false);
		getPortalDao().save(page_home);
		getPortalDao().save(site);
								

		KbeePageSection ps_home = (KbeePageSection) addNewPageSection(page_home, 	"Main Section", PageSectionType.ONE_SECTION, PageSectionDisposition.LEFT);
		ps_home.setCss("searcher-home-ps");
		getPortalDao().save(ps_home);
		
		Area area_banner 	= addNewArea(ps_home, "area_banner", 			AreaType.AREA_1S, 			"area-banner"		);
		Area area_how 		= addNewArea(ps_home, "area_how-it-works", 		AreaType.AREA_1S, 			"area-how-it-works"	);
		Area area_cases 	= addNewArea(ps_home, "area_cases", 			AreaType.AREA_2S_50X50, 		"area-cases"		);
		Area area_benefits 	= addNewArea(ps_home, "area_benefits", 			AreaType.AREA_1S, 			"area-benefits"		);
		Area area_platform 	= addNewArea(ps_home, "area_platform", 			AreaType.AREA_1S, 			"area-platform"		);
		Area area_contact 	= addNewArea(ps_home, "area_contact", 			AreaType.AREA_1S, 			"area-contact"		);
		
		getPortalDao().save(area_banner);
		getPortalDao().save(area_how);
		getPortalDao().save(area_cases);
		getPortalDao().save(area_benefits);
		getPortalDao().save(area_platform);
		getPortalDao().save(area_contact);
		
		Block block_banner  	= addNewBlock(area_banner, 		new KbeeBlock("block banner"), 		AreaSection.LEFT, "block-banner");		getPortalDao().save(block_banner);
		Block block_como		= addNewBlock(area_how, 		new KbeeBlock("block como"), 		AreaSection.LEFT, "block-como");		getPortalDao().save(block_como);
		Block block_caso_1		= addNewBlock(area_cases, 		new KbeeBlock("block case 1"), 		AreaSection.LEFT, "block-caso_1");		getPortalDao().save(block_caso_1);
		Block block_caso_2		= addNewBlock(area_cases, 		new KbeeBlock("block case 2"), 		AreaSection.LEFT, "block-caso_2");		getPortalDao().save(block_caso_2);
		Block block_benefits	= addNewBlock(area_benefits,	new KbeeBlock("block benefits"),	AreaSection.LEFT, "block-benefits");	getPortalDao().save(block_benefits);
		Block block_platform 	= addNewBlock(area_platform,	new KbeeBlock("block platform"),	AreaSection.LEFT, "block-platform");	getPortalDao().save(block_platform);
		Block block_contact 	= addNewBlock(area_contact,		new KbeeBlock("block contact"),		AreaSection.LEFT, "block-contact");		getPortalDao().save(block_contact);
		
		site.add(page_home);
		
		site.setDescription(domain.getDescription());
		addDataSetValues(domain, site);
		
		getPortalDao().save(site);
		txlogger.info(new SiteUpdateEvent(site));
		
		return site;
	}

	
	/**----------------------------------------------------------------------------------
	 * 
	 * @return
	 * @throws ContentCreationException
	 * 
	 *                                  Crea:
	 *                                  . Site -> Si es Intranet
	 *                                  . Grupo Admin, Write, Read . DataSetMember
	 *                                  del Sitio . DataSetMember del Repositorio
	 *                                  del Sitio
	 *                                  Url, Type, Access,
	 */

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Site createExternalSite() throws ContentCreationException, ContentMgmtException {
		UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		Domain domain = userProfile.getDomain();
		try {
			DataSet site_dataset 				= getExternalDao().createSiteDataSetIfNotExists(domain);
			DataSet site_repository_dataset 	= getExternalDao().createSiteRespositoryDataSetIfNotExists(domain);
			
			getExternalDao().createSiteClassifierIfNotExists(domain, site_dataset);
			getExternalDao().createSiteRepositoryClassifierIfNotExists(domain, site_repository_dataset);

		} catch (ContentMgmtException e) {
			logger.error(e);
			throw (e);
		}

		KbeeSite site = new KbeeSite();

		Long oid = getNewOId();
		site.setOId(oid);

		if (userProfile != null)
			site.setLastModifiedUser(userProfile.getUser());

		site.setCreationOffsetDateTime(OffsetDateTime.now());
		site.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		site.setIsExternal(true);
		Locale locale = userProfile.getUser().getLocale();
		String prefix = getLabel("site", locale);

		site.setName(prefix + oid.toString());
	
		site.setTitle(prefix + " " + getLabel("external", locale) + " - " + oid.toString());
		site.setState(ObjectState.ENABLED);

		// Mandatory: Type, Url, Access ---------------------------------------------
		//
		//String base = PortalUriHelper.getInstance().getPortalURL(domain.getName());
		//site.setURI(base);
			
		site.setPublicAccess(true);
		site.setSiteType(SiteType.GENERAL);
		site.setSiteType(null);
		
		getPortalDao().save(site);

		/**
		 * ------------------------------------------------ 
		 * Aca se debe loggear la
		 * creacion del Sitio logger.info(new CreateEvent(content));
		 * ------------------------------------------------
		 **/
		return site;
	}


	

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Site createSite(String title) throws ContentCreationException, ContentMgmtException {
	
		KbeeSite site = new KbeeSite();
		Long oid = getNewOId();
		site.setOId(oid);
		site.setLastModifiedUser(getSessionUser());
		site.setDomain(getDomain());
		site.setCreationOffsetDateTime(OffsetDateTime.now());
		site.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		site.setName(title);
		getPortalDao().save(site);
		site.setState(ObjectState.ENABLED);
		site.setKey(oid.toString());
		site.setOwner(getSessionUser());
		getPortalDao().save(site);
		return site;
	}
	
	/**
	 * 
	 * 
	 * 		/**
		 * 

 		KbeeSite site = (KbeeSite) createSite("Dashboards for " + person.getFirstLastName());
		
		site.setKey(Site.DASHBOARD);
		
		site.setDescription("Dashboards for " + person.getFirstLastName());
		getPortalDao().save(site);
		
		Page home = addNewPage(site, "Home");
		getPortalDao().save(home);
		getPortalDao().save(site);
		
		KbeePageSection ps_home = (KbeePageSection) addNewPageSection(home, "Main Page Section", PageSectionType.ONE_SECTION, PageSectionDisposition.LEFT);
		getPortalDao().save(ps_home);
							

		
		KbeePageSection ps_top_home = (KbeePageSection) addNewPageSection(home, "Top Page Section", PageSectionType.ONE_SECTION, PageSectionDisposition.TOP);
		getPortalDao().save(ps_top_home);
		Area area_top = addNewArea(ps_top_home, "area_top", 		AreaType.AREA_1S);
		getPortalDao().save(area_top);
		Block b_top =	addNewBlock(area_top, "top block", 	AreaSection.LEFT);
		getPortalDao().save(b_top);
		

		KbeePageSection ps_bottom_home = (KbeePageSection) addNewPageSection(home, "Bottom Page Section", PageSectionType.ONE_SECTION, PageSectionDisposition.BOTTOM);
		getPortalDao().save(ps_bottom_home);
		Area area_bottom = addNewArea(ps_bottom_home, "area_bottom", 		AreaType.AREA_1S);
		getPortalDao().save(area_bottom);
		Block b_bottom =	addNewBlock(area_bottom, "bottom block", 	AreaSection.LEFT);
		getPortalDao().save(b_bottom);
		
		
		Area area1 = addNewArea(ps_home, "area_kpi", 		AreaType.AREA_3S_3x33);
		Area area2 = addNewArea(ps_home, "area_charts", 	AreaType.AREA_2S50X50);
		Area area3 = addNewArea(ps_home, "area_reports", 	AreaType.AREA_1S);
		Area area4 = addNewArea(ps_home, "area_library", 	AreaType.AREA_1S);
		
		Area area5 = addNewArea(ps_home, "area_test_1", 	AreaType.AREA_2S_66X33);
		Area area6 = addNewArea(ps_home, "area_test_2", 	AreaType.AREA_2S_33X66);
		
		
		
		
		getPortalDao().save(area1);
		getPortalDao().save(area2);
		getPortalDao().save(area3);
		getPortalDao().save(area4);
		
		
		
		getPortalDao().save(area5);
		getPortalDao().save(area6);
		
		
		
		// FACTORY Area
		 		
		java.util.Map<String, AreaFactory> a_beans =  ServiceLocator.getService(BeansService.class).getBeansOfType(AreaFactory.class);
				for (Entry<String, AreaFactory> e: a_beans.entrySet()) {
					if (e.getValue().getKey().equals("area-billboard")) {
						Area area7 =this.addNewArea(e.getValue(), ps_home, "billboard wrapper area",AreaType.AREA_1S);
						getPortalDao().save(area7);
					}
					
					if (e.getValue().getKey().equals("area-dummy")) {
						Area area8 =this.addNewArea(e.getValue(), ps_home, "dummy wrapper area", AreaType.AREA_1S);
						getPortalDao().save(area8);
					}
				}
		
		
		
		Block b1 =	addNewBlock(area1, "block kpi_1", 	AreaSection.LEFT);
		Block b2 = 	addNewBlock(area1, "block kpi_2", 	AreaSection.CENTER);
		Block b3 = 	addNewBlock(area1, "block kpi_3", 	AreaSection.RIGHT);
		
		Block b4 = 	addNewBlock(area2, "block chart_1", 	AreaSection.LEFT);
		Block b5 =  addNewBlock(area2, "block chart_2", 	AreaSection.RIGHT);
		
		Block b6 =	addNewBlock(area3, "block reports_1", 	AreaSection.LEFT);
		
		Block b7 =	addNewBlock(area4, "block library_1", 	AreaSection.LEFT);
		Block b8 =	addNewBlock(area4, "block library_2", 	AreaSection.LEFT);
		


		// BLOCJ FACTORY
		java.util.Map<String, BlockFactory> beans =  ServiceLocator.getService(BeansService.class).getBeansOfType(BlockFactory.class);
		for (Entry<String, BlockFactory> e: beans.entrySet()) {
			if (e.getValue().getKey().equals("block-billboard")) {
				Block b8a =this.addNewBlock(e.getValue(), area4, "billboard block wrapper ", AreaSection.LEFT);
				getPortalDao().save(b8a);
			}
			
			if (e.getValue().getKey().equals("block-dummy")) {
				Block b8b =this.addNewBlock(e.getValue(), area4, "dummy block wrapper", AreaSection.LEFT);
				getPortalDao().save(b8b);
			}
		}

		
		Block b10 =	addNewBlock(area5, "block test1", 	AreaSection.LEFT);
		Block b11 =	addNewBlock(area5, "block tes2", 	AreaSection.RIGHT);
		Block b12 =	addNewBlock(area6, "block test1", 	AreaSection.LEFT);
		Block b13 =	addNewBlock(area6, "block tes2", 	AreaSection.RIGHT);
		
		getPortalDao().save(b1);
		getPortalDao().save(b2);
		getPortalDao().save(b3);
		getPortalDao().save(b4);
		getPortalDao().save(b5);
		getPortalDao().save(b6);
		getPortalDao().save(b7);
		
		getPortalDao().save(b8);

		
		
		getPortalDao().save(b10);
		getPortalDao().save(b11);
		getPortalDao().save(b12);
		getPortalDao().save(b13);
		
		getPortalDao().save(home);
		getPortalDao().save(ps_home);
		getPortalDao().save(site);

		/**
		 * ------------------------------------------------ 
		 * creacion del Sitio logger.info(new CreateEvent(content));
		 * ------------------------------------------------
		 * 		return site;
		 **/

	

	/** -----------------------------------------------
	 * 
	 * 
	 * 
	 */
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Site createKLibrarySite() throws ContentCreationException, ContentMgmtException {

		KbeeSite site = (KbeeSite) createSite("Library Site");
		site.setKey(Site.LIBRARY);
		
		site.setDescription("Library Search");
		
		getPortalDao().save(site);
		
		Page page_home	   = addNewPage(site, "Home");
		Page page_results  = addNewPage(site, "Results");
		Page page_detail   = addNewPage(site, "Detail");
		
		getPortalDao().save(page_home);
		getPortalDao().save(page_results);
		getPortalDao().save(page_detail);
		getPortalDao().save(site);
									
		KbeePageSection ps_home 		= (KbeePageSection) addNewPageSection(page_home, 	"Main Section", PageSectionType.ONE_SECTION, PageSectionDisposition.LEFT);
		KbeePageSection ps_results 		= (KbeePageSection) addNewPageSection(page_results, "Main Section", PageSectionType.ONE_SECTION, PageSectionDisposition.LEFT);
		KbeePageSection ps_detail 		= (KbeePageSection) addNewPageSection(page_detail,	"Main Section", PageSectionType.ONE_SECTION, PageSectionDisposition.LEFT);
		
		getPortalDao().save(ps_home);
		getPortalDao().save(ps_results);
		getPortalDao().save(ps_detail);


		// Home -----------
		//
		// Area area_home_1 = addNewArea(ps_home, "area_kpi", 		AreaType.AREA_3S_3x33);
		// Area area_home_2 = addNewArea(ps_home, "area_charts", 	AreaType.AREA_2S50X50);
		// Area area_home_3 = addNewArea(ps_home, "area_reports", 	AreaType.AREA_1S);
		//
		// getPortalDao().save(area_home_1);
		// getPortalDao().save(area_home_2);
		// getPortalDao().save(area_home_3);
		//
		// Block block_home_b1 =	addNewBlock(area_home_1, "block kpi_1", 	AreaSection.LEFT);
		// Block block_home_b2 = 	addNewBlock(area_home_1, "block kpi_2", 	AreaSection.CENTER);
		// Block block_home_b3 = 	addNewBlock(area_home_1, "block kpi_3", 	AreaSection.RIGHT);
		// Block block_home_b4 = 	addNewBlock(area_home_2, "block chart_1", 	AreaSection.LEFT);
		// Block block_home_b5 =   addNewBlock(area_home_2, "block chart_2", 	AreaSection.RIGHT);
		// Block block_home_b6 =	addNewBlock(area_home_3, "block reports_1", 	AreaSection.LEFT);
		//
		// getPortalDao().save(block_home_b1);
		// getPortalDao().save(block_home_b2);
		// getPortalDao().save(block_home_b3);
		// getPortalDao().save(block_home_b4);
		// getPortalDao().save(block_home_b5);
		// getPortalDao().save(block_home_b6);
		//

		// Library -----------
		
		
		//Area area_library_1 = addNewArea(ps_library, "My Favorites", 		AreaType.AREA_2S50X50);
		//Area area_library_2 = addNewArea(ps_library, "Recent Activity",		AreaType.AREA_2S50X50);
		//Area area_library_3 = addNewArea(ps_library, "Metrics",		 		AreaType.AREA_3S_3x33);
		//Area area_library_4 = addNewArea(ps_library, "News",		 		AreaType.AREA_2S_66X33);
		
		//getPortalDao().save(area_library_1);
		//getPortalDao().save(area_library_2);
		//getPortalDao().save(area_library_3);
		//getPortalDao().save(area_library_4);
		
		//Block block_library_b1 =	addNewBlock(area_library_1, "My Queries", 	AreaSection.LEFT);
		//Block block_library_b2 = 	addNewBlock(area_library_1, "My Favorites", AreaSection.RIGHT);
		
		//Block block_library_b3 = 	addNewBlock(area_library_2, "Published", 	AreaSection.LEFT);
		//Block block_library_b4 = 	addNewBlock(area_library_2, "Approved", 	AreaSection.RIGHT);
		
		
		//Block block_library_b5 = 	addNewBlock(area_library_3, "Approved", 		AreaSection.LEFT);
		//Block block_library_b6 = 	addNewBlock(area_library_3, "Perfect File",		AreaSection.RIGHT);
		//Block block_library_b7 = 	addNewBlock(area_library_3, "OneSite", 			AreaSection.RIGHT);
		
		
		//Block block_library_b8 = 	addNewBlock(area_library_4, "News", 				AreaSection.LEFT);
		//Block block_library_b9 = 	addNewBlock(area_library_4, "Tutorials", 			AreaSection.LEFT);
		//Block block_library_b10 = 	addNewBlock(area_library_4, "Quick Access", 		AreaSection.RIGHT);
		
		//getPortalDao().save(block_library_b1);
		//getPortalDao().save(block_library_b2);
		//getPortalDao().save(block_library_b3);
		//getPortalDao().save(block_library_b4);

		//getPortalDao().save(block_library_b5);
		//getPortalDao().save(block_library_b6);
		//getPortalDao().save(block_library_b7);
		
		//getPortalDao().save(block_library_b8);
		//getPortalDao().save(block_library_b9);
		//getPortalDao().save(block_library_b10);
		
		
		// Reports -----------
		
		//Area area_reports_1 = addNewArea(ps_reports, "Compliance KPI", 				AreaType.AREA_3S_3x33);
		//Area area_reports_2 = addNewArea(ps_reports, "Charts", 						AreaType.AREA_2S50X50);
		//Area area_reports_4 = addNewArea(ps_reports, "Property Submissions", 		AreaType.AREA_1S);
		//Area area_reports_3 = addNewArea(ps_reports, "Reports", 					AreaType.AREA_1S);
		
		
		//getPortalDao().save(area_reports_1);
		//getPortalDao().save(area_reports_2);
		//getPortalDao().save(area_reports_3);
		//getPortalDao().save(area_reports_4);
		
		//Block block_reports_b1 =	addNewBlock(area_reports_1, "Approved", 	AreaSection.LEFT);
		//Block block_reports_b2 = 	addNewBlock(area_reports_1, "Past Due", 	AreaSection.CENTER);
		//Block block_reports_b3 = 	addNewBlock(area_reports_1, "Forecasted", 	AreaSection.RIGHT);
		
		//Block block_reports_b4 = 	addNewBlock(area_reports_2, "File Status", 	AreaSection.LEFT);
		//Block block_reports_b5 = 	addNewBlock(area_reports_2, "Resubmission Reasons", 	AreaSection.RIGHT);
		
		//Block block_reports_b6 = 	addNewBlock(area_reports_3, "Property Submissions", 	AreaSection.LEFT);
		//Block block_reports_b7 = 	addNewBlock(area_reports_4, "Reports", 					AreaSection.LEFT);
		
		//getPortalDao().save(block_reports_b1);
		//getPortalDao().save(block_reports_b2);
		//getPortalDao().save(block_reports_b3);
		//getPortalDao().save(block_reports_b4);
		//getPortalDao().save(block_reports_b5);
		//getPortalDao().save(block_reports_b6);
		//getPortalDao().save(block_reports_b7);
		
		getPortalDao().save(page_home);
		getPortalDao().save(page_results);
		getPortalDao().save(page_detail);
		
		getPortalDao().save(ps_home);
		getPortalDao().save(site);

		
		addSiteSections(site);
		
		/**
		 * ------------------------------------------------ 
		 * creacion del Sitio logger.info(new CreateEvent(content));
		 * ------------------------------------------------
		 **/
		return site;

		
		
	}
	

		
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Site createMainDashboardSite(Person person) throws ContentCreationException, ContentMgmtException {

		KbeeSite site = (KbeeSite) createSite("Dashboards for " + person.getFirstLastName());
		
		site.setKey(Site.MAIN_DASHBOARD);
		
		site.setDescription("Dashboards for " + person.getFirstLastName());
		
		getPortalDao().save(site);
		
		Page page_home	   = addNewPage(site, "Home");
		Page page_library  = addNewPage(site, "Libray");
		Page page_reports  = addNewPage(site, "Reports");
		
		getPortalDao().save(page_home);
		getPortalDao().save(page_reports);
		getPortalDao().save(page_library);
		getPortalDao().save(site);
		
		KbeePageSection ps_home 		= (KbeePageSection) addNewPageSection(page_home, 	"Main Section", PageSectionType.ONE_SECTION, PageSectionDisposition.LEFT);
		KbeePageSection ps_reports 		= (KbeePageSection) addNewPageSection(page_reports, "Main Section", PageSectionType.ONE_SECTION, PageSectionDisposition.LEFT);
		KbeePageSection ps_library 		= (KbeePageSection) addNewPageSection(page_library,	"Main Section", PageSectionType.ONE_SECTION, PageSectionDisposition.LEFT);
		
		getPortalDao().save(ps_home);
		getPortalDao().save(ps_reports);
		getPortalDao().save(ps_library);
		
		// Home -----------
		
		Area area_home_1 = addNewArea(ps_home, "area_kpi", 		AreaType.AREA_3S_3x33);
		Area area_home_2 = addNewArea(ps_home, "area_charts", 	AreaType.AREA_2S_50X50);
		Area area_home_3 = addNewArea(ps_home, "area_reports", 	AreaType.AREA_1S);
		
		getPortalDao().save(area_home_1);
		getPortalDao().save(area_home_2);
		getPortalDao().save(area_home_3);
		
		Block block_home_b1 =	addNewBlock(area_home_1, new KbeeBlock("block kpi_1"), 	AreaSection.LEFT, "block-dummy");
		Block block_home_b2 = 	addNewBlock(area_home_1, new KbeeBlock("block kpi_2"), 	AreaSection.CENTER, "block-dummy");
		Block block_home_b3 = 	addNewBlock(area_home_1, new KbeeBlock("block kpi_3"), 	AreaSection.RIGHT, "block-dummy");
		Block block_home_b4 = 	addNewBlock(area_home_2, new KbeeBlock("block chart_1"), 	AreaSection.LEFT, "block-dummy");
		Block block_home_b5 =   addNewBlock(area_home_2, new KbeeBlock("block chart_2"), 	AreaSection.RIGHT, "block-dummy");
		Block block_home_b6 =	addNewBlock(area_home_3, new KbeeBlock("block reports_1"), 	AreaSection.LEFT, "block-dummy");
		
		getPortalDao().save(block_home_b1);
		getPortalDao().save(block_home_b2);
		getPortalDao().save(block_home_b3);
		getPortalDao().save(block_home_b4);
		getPortalDao().save(block_home_b5);
		getPortalDao().save(block_home_b6);


		// Library -----------
		
		
		Area area_library_1 = addNewArea(ps_library, "My Favorites", 		AreaType.AREA_2S_50X50);
		Area area_library_2 = addNewArea(ps_library, "Recent Activity",		AreaType.AREA_2S_50X50);
		Area area_library_3 = addNewArea(ps_library, "Metrics",		 		AreaType.AREA_3S_3x33);
		Area area_library_4 = addNewArea(ps_library, "News",		 		AreaType.AREA_2S_66X33);
		
		getPortalDao().save(area_library_1);
		getPortalDao().save(area_library_2);
		getPortalDao().save(area_library_3);
		getPortalDao().save(area_library_4);
		
		Block block_library_b1 =	addNewBlock(area_library_1, new KbeeBlock("My Queries"), 	AreaSection.LEFT, "block-dummy");
		Block block_library_b2 = 	addNewBlock(area_library_1, new KbeeBlock("My Favorites"), AreaSection.RIGHT, "block-dummy");
		
		Block block_library_b3 = 	addNewBlock(area_library_2, new KbeeBlock("Published"), 	AreaSection.LEFT, "block-dummy");
		Block block_library_b4 = 	addNewBlock(area_library_2, new KbeeBlock("Approved"), 	AreaSection.RIGHT ,"block-dummy");
		
		
		Block block_library_b5 = 	addNewBlock(area_library_3, new KbeeBlock("Approved"), 		AreaSection.LEFT, "block-dummy");
		Block block_library_b6 = 	addNewBlock(area_library_3, new KbeeBlock("Perfect File"),		AreaSection.RIGHT, "block-dummy");
		Block block_library_b7 = 	addNewBlock(area_library_3, new KbeeBlock("OneSite"), 			AreaSection.RIGHT, "block-dummy");
		
		
		Block block_library_b8 = 	addNewBlock(area_library_4, new KbeeBlock("News"), 				AreaSection.LEFT, "block-dummy");
		Block block_library_b9 = 	addNewBlock(area_library_4, new KbeeBlock("Tutorials"), 			AreaSection.LEFT, "block-dummy");
		Block block_library_b10 = 	addNewBlock(area_library_4, new KbeeBlock("Quick Access"), 		AreaSection.RIGHT, "block-dummy");
		
		getPortalDao().save(block_library_b1);
		getPortalDao().save(block_library_b2);
		getPortalDao().save(block_library_b3);
		getPortalDao().save(block_library_b4);

		getPortalDao().save(block_library_b5);
		getPortalDao().save(block_library_b6);
		getPortalDao().save(block_library_b7);
		
		getPortalDao().save(block_library_b8);
		getPortalDao().save(block_library_b9);
		getPortalDao().save(block_library_b10);
		
		
		// Reports -----------
		
		Area area_reports_1 = addNewArea(ps_reports, "Compliance KPI", 				AreaType.AREA_3S_3x33);
		Area area_reports_2 = addNewArea(ps_reports, "Charts", 						AreaType.AREA_2S_50X50);
		Area area_reports_4 = addNewArea(ps_reports, "Property Submissions", 		AreaType.AREA_1S);
		Area area_reports_3 = addNewArea(ps_reports, "Reports", 					AreaType.AREA_1S);
		
		
		getPortalDao().save(area_reports_1);
		getPortalDao().save(area_reports_2);
		getPortalDao().save(area_reports_3);
		getPortalDao().save(area_reports_4);
		
		Block block_reports_b1 =	addNewBlock(area_reports_1, new KbeeBlock("Approved"), 	AreaSection.LEFT, "block-dummy");
		Block block_reports_b2 = 	addNewBlock(area_reports_1, new KbeeBlock("Past Due"), 	AreaSection.CENTER, "block-dummy");
		Block block_reports_b3 = 	addNewBlock(area_reports_1, new KbeeBlock("Forecasted"), 	AreaSection.RIGHT, "block-dummy");
		
		Block block_reports_b4 = 	addNewBlock(area_reports_2, new KbeeBlock("File Status"), 	AreaSection.LEFT, "block-dummy");
		Block block_reports_b5 = 	addNewBlock(area_reports_2, new KbeeBlock("Resubmission Reasons"), 	AreaSection.RIGHT, "block-dummy");
		
		Block block_reports_b6 = 	addNewBlock(area_reports_3, new KbeeBlock("Property Submissions"), 	AreaSection.LEFT, "block-dummy");
		Block block_reports_b7 = 	addNewBlock(area_reports_4, new KbeeBlock("Reports"), 					AreaSection.LEFT, "block-dummy");
		
		getPortalDao().save(block_reports_b1);
		getPortalDao().save(block_reports_b2);
		getPortalDao().save(block_reports_b3);
		getPortalDao().save(block_reports_b4);
		getPortalDao().save(block_reports_b5);
		getPortalDao().save(block_reports_b6);
		getPortalDao().save(block_reports_b7);
		
		getPortalDao().save(page_home);
		getPortalDao().save(page_reports);
		getPortalDao().save(page_library);
		getPortalDao().save(ps_home);
		getPortalDao().save(site);

		
		addSiteSections(site);
		
		/**
		 * ------------------------------------------------ 
		 * creacion del Sitio logger.info(new CreateEvent(content));
		 * ------------------------------------------------
		 **/
		return site;
		
	}
	

	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Site createGeneralDashboardSite() throws ContentCreationException, ContentMgmtException {

		KbeeSite site = (KbeeSite) createSite("One Page Dashboard");
		
		site.setKey(Site.GENERAL_DASHBOARD);
		site.setDescription("General One Page portal.");
		site.setUrl(site.getOId().toString());
		site.setKey("op-"+site.getOId().toString());
		
		getPortalDao().save(site);
		
		Page page_home = addNewPage(site, "Home");
		
		getPortalDao().save(page_home);
		getPortalDao().save(site);
								
		KbeePageSection ps_home = (KbeePageSection) addNewPageSection(page_home, 	"Main Section", PageSectionType.ONE_SECTION, PageSectionDisposition.LEFT);
		getPortalDao().save(ps_home);
		
		// Home -----------
																		
		Area area_home_1 = addNewArea(ps_home, "area_main", AreaType.AREA_3S_3x33);
		getPortalDao().save(area_home_1);
		Block block_home_b11 =	addNewBlock(area_home_1, new KbeeBlock("block 1L"), 	AreaSection.LEFT, "block-dummy");
		Block block_home_b12 =	addNewBlock(area_home_1, new KbeeBlock("block 2L"), 	AreaSection.LEFT, "block-dummy");
		
		Block block_home_b21 = 	addNewBlock(area_home_1, new KbeeBlock("block 1C"), 	AreaSection.CENTER, "block-dummy");
		Block block_home_b22 = 	addNewBlock(area_home_1, new KbeeBlock("block 2C"), 	AreaSection.CENTER, "block-dummy");
		
		Block block_home_b31 = 	addNewBlock(area_home_1, new KbeeBlock("block 1R"), 	AreaSection.RIGHT, "block-dummy");
		Block block_home_b32 = 	addNewBlock(area_home_1, new KbeeBlock("block 2R"), 	AreaSection.RIGHT, "block-dummy");

		getPortalDao().save(block_home_b11);
		getPortalDao().save(block_home_b12);
		
		getPortalDao().save(block_home_b21);
		getPortalDao().save(block_home_b22);
		
		getPortalDao().save(block_home_b31);
		getPortalDao().save(block_home_b32);
		
		getPortalDao().save(page_home);
		getPortalDao().save(ps_home);
		getPortalDao().save(site);
		
		addSiteSections(site);
		
		/**
		 * ------------------------------------------------ 
		 * creacion del Sitio logger.info(new CreateEvent(content));
		 * ------------------------------------------------
		 **/
		return site;
		
	}

	
	
	

	
	@Transactional(propagation = Propagation.REQUIRED)
	private void addSiteSections(Site site) {
		
		Page ss = addNewPage(site, "Site Sections");
		
		ss.setState(ObjectState.ARCHIVED);
		
		ss.setIsRegularPage(false);
		ss.setSiteSection(true);
		getPortalDao().save(ss);
		getPortalDao().save(site);
		
		KbeePageSection ps_top_ss = (KbeePageSection) addNewPageSection(ss, "Site Top Section", PageSectionType.ONE_SECTION, PageSectionDisposition.TOP);
		getPortalDao().save(ps_top_ss);
		
		Area area_top_ss = addNewArea(ps_top_ss, "area_top", 		AreaType.AREA_1S);		getPortalDao().save(area_top_ss);
		Block b_top_ss =	addNewBlock(area_top_ss, new KbeeBlock("top block"), 	AreaSection.LEFT, "block-dummy");		getPortalDao().save(b_top_ss);
		KbeePageSection ps_bottom_ss = (KbeePageSection) addNewPageSection(ss, "Site Bottom Section", PageSectionType.ONE_SECTION, PageSectionDisposition.BOTTOM);
		getPortalDao().save(ps_bottom_ss);
		
		Area area_bottom_ss = addNewArea(ps_bottom_ss, "area_bottom", 		AreaType.AREA_1S);		getPortalDao().save(area_bottom_ss);
		Block b_bottom_ss =	addNewBlock(area_bottom_ss, new KbeeBlock("bottom block"), 	AreaSection.LEFT , "block-dummy");		getPortalDao().save(b_bottom_ss);
		
	}
	
	

	
	
	
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Site createTestSite(Person person) throws ContentCreationException, ContentMgmtException {
		KbeeSite site = (KbeeSite) createSite("Test portal for " + person.getFirstLastName());
		site.setKey("test");
		site.setDescription("Test portal for " + person.getFirstLastName());
		getPortalDao().save(site);
		
		
		Page home = addNewPage(site, "Home");
		getPortalDao().save(home);
		getPortalDao().save(site);
		
		KbeePageSection ps_home = (KbeePageSection) addNewPageSection(home, "Main Page Section", PageSectionType.ONE_SECTION, PageSectionDisposition.LEFT);
		getPortalDao().save(ps_home);
							

		
		KbeePageSection ps_top_home = (KbeePageSection) addNewPageSection(home, "Top Page Section", PageSectionType.ONE_SECTION, PageSectionDisposition.TOP);
		getPortalDao().save(ps_top_home);
		Area area_top = addNewArea(ps_top_home, "area_top", 		AreaType.AREA_1S);
		getPortalDao().save(area_top);
		Block b_top =	addNewBlock(area_top, new KbeeBlock("top block"), 	AreaSection.LEFT, "block-dummy");
		getPortalDao().save(b_top);
		

		KbeePageSection ps_bottom_home = (KbeePageSection) addNewPageSection(home, "Bottom Page Section", PageSectionType.ONE_SECTION, PageSectionDisposition.BOTTOM);
		getPortalDao().save(ps_bottom_home);
		Area area_bottom = addNewArea(ps_bottom_home, "area_bottom", 		AreaType.AREA_1S);
		getPortalDao().save(area_bottom);
		Block b_bottom =	addNewBlock(area_bottom, new KbeeBlock("bottom block"), 	AreaSection.LEFT, "block-dummy");
		getPortalDao().save(b_bottom);
		
		
		Area area1 = addNewArea(ps_home, "area_kpi", 		AreaType.AREA_3S_3x33);
		Area area2 = addNewArea(ps_home, "area_charts", 	AreaType.AREA_2S_50X50);
		Area area3 = addNewArea(ps_home, "area_reports", 	AreaType.AREA_1S);
		Area area4 = addNewArea(ps_home, "area_library", 	AreaType.AREA_1S);
		
		Area area5 = addNewArea(ps_home, "area_test_1", 	AreaType.AREA_2S_66X33);
		Area area6 = addNewArea(ps_home, "area_test_2", 	AreaType.AREA_2S_33X66);
		
		
		getPortalDao().save(area1);
		getPortalDao().save(area2);
		getPortalDao().save(area3);
		getPortalDao().save(area4);
		
		
		
		getPortalDao().save(area5);
		getPortalDao().save(area6);
		
		
		
		// FACTORY Area
		 		
		java.util.Map<String, AreaFactory> a_beans =  ServiceLocator.getService(BeansService.class).getBeansOfType(AreaFactory.class);
				for (Entry<String, AreaFactory> e: a_beans.entrySet()) {
					if (e.getValue().getKey().equals("area-billboard")) {
						Area area7 =this.addNewArea(e.getValue(), ps_home, "billboard wrapper area",AreaType.AREA_1S);
						getPortalDao().save(area7);
					}
					
					if (e.getValue().getKey().equals("area-dummy")) {
						Area area8 =this.addNewArea(e.getValue(), ps_home, "dummy wrapper area", AreaType.AREA_1S);
						getPortalDao().save(area8);
					}
				}
		
		
		
		Block b1 =	addNewBlock(area1, new KbeeBlock("block kpi_1"), 	AreaSection.LEFT,"block-dummy");
		Block b2 = 	addNewBlock(area1, new KbeeBlock("block kpi_2"), 	AreaSection.CENTER, "block-dummy");
		Block b3 = 	addNewBlock(area1, new KbeeBlock("block kpi_3"), 	AreaSection.RIGHT, "block-dummy");
		
		Block b4 = 	addNewBlock(area2, new KbeeBlock("block chart_1"), 	AreaSection.LEFT, "block-dummy");
		Block b5 =  addNewBlock(area2, new KbeeBlock("block chart_2"), 	AreaSection.RIGHT, "block-dummy");
		
		Block b6 =	addNewBlock(area3, new KbeeBlock("block reports_1"), 	AreaSection.LEFT, "block-dummy");
		
		Block b7 =	addNewBlock(area4, new KbeeBlock("block library_1"), 	AreaSection.LEFT, "block-dummy");
		Block b8 =	addNewBlock(area4, new KbeeBlock("block library_2"), 	AreaSection.LEFT,"block-dummy");
		


		// BLOCJ FACTORY
		java.util.Map<String, BlockFactory> beans =  ServiceLocator.getService(BeansService.class).getBeansOfType(BlockFactory.class);
		for (Entry<String, BlockFactory> e: beans.entrySet()) {
			if (e.getValue().getKey().equals("block-billboard")) {
				Block b8a =this.addNewBlock(e.getValue(), area4, "billboard block wrapper ", AreaSection.LEFT);
				getPortalDao().save(b8a);
			}
			
			if (e.getValue().getKey().equals("block-dummy")) {
				Block b8b =this.addNewBlock(e.getValue(), area4, "dummy block wrapper", AreaSection.LEFT);
				getPortalDao().save(b8b);
			}
		}

		
		Block b10 =	addNewBlock(area5, new KbeeBlock("block test1"), 	AreaSection.LEFT, "block-dummy");
		Block b11 =	addNewBlock(area5, new KbeeBlock("block tes2"), 	AreaSection.RIGHT, "block-dummy");
		Block b12 =	addNewBlock(area6, new KbeeBlock("block test1"), 	AreaSection.LEFT, "block-dummy");
		Block b13 =	addNewBlock(area6, new KbeeBlock("block tes2"), 	AreaSection.RIGHT, "block-dummy");
		
		getPortalDao().save(b1);
		getPortalDao().save(b2);
		getPortalDao().save(b3);
		getPortalDao().save(b4);
		getPortalDao().save(b5);
		getPortalDao().save(b6);
		getPortalDao().save(b7);
		
		getPortalDao().save(b8);

		
		
		getPortalDao().save(b10);
		getPortalDao().save(b11);
		getPortalDao().save(b12);
		getPortalDao().save(b13);
		
		getPortalDao().save(home);
		getPortalDao().save(ps_home);
		getPortalDao().save(site);

		addSiteSections(site);
		
		 // ------------------------------------------------ 
		 // creacion del Sitio logger.info(new CreateEvent(content));
		 // ------------------------------------------------
		
		return site;
		
	}

	
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public PortalPersistentMenu addNewMenu(Site site, String name) throws ContentCreationException, ContentMgmtException {
		
		if (site==null)
			throw new IllegalArgumentException("site can not be null");
		
		KbeePortalPersistentMenu menu = new KbeePortalPersistentMenu(name);
		Long oid = getNewOId();
		menu.setState(ObjectState.ENABLED);
		menu.setOId(oid);
		menu.setCreationOffsetDateTime(OffsetDateTime.now());
		menu.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		menu.setLastModifiedUser(getSessionUser());
		menu.setTitle(name);
		menu.setDomain(site.getDomain());
		menu.setOrder(site.getMenus().size());
		// menu.setDefaults();
		site.add(menu);
		return menu;
	}
	
	
	 

	

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public Page addNewPage(Site site, String name) throws ContentCreationException, ContentMgmtException {
		
		if (site==null)
			throw new IllegalArgumentException("site can not be null");
		
		KbeePage page = new KbeePage(name);
		Long oid = getNewOId();
		page.setState(ObjectState.ENABLED);
		page.setOId(oid);
		page.setTitle(name);
		page.setIsRegularPage(true);
		page.setSiteSection(false);
		page.setDomain(site.getDomain());
		page.setOrder(site.getPages().size());
		
		if (site.getPages().isEmpty()) {
			page.setIsHome(true);
			page.setKey("home");
		}

		page.setDefaults();
		site.add(page);
		
		 return page;
	}
	
	
	

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public PageSection addNewPageSection(Page page, String name, PageSectionType pst, PageSectionDisposition psd)  throws ContentCreationException, ContentMgmtException {
		if (page==null)
			throw new IllegalArgumentException("page can not be null");
		KbeePageSection ps = new KbeePageSection(name);
		
		ps.setKey("pagesection");
		ps.setDomain(page.getDomain());
		ps.setTitle(name);
		ps.setState(ObjectState.ENABLED);
		ps.setPageSectionType(pst);
		ps.setPageSectionDisposition(psd);
		ps.setOrder(page.getPageSections().size());
		
		Long oid = getNewOId();
		ps.setOId(oid);
		ps.setDefaults();
		
		page.add(ps);
		
		
		ps.setDefaults();
		return ps;
	}
	
	
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public  Area addNewArea(PageSection ps, String title, AreaType areatype)  throws ContentCreationException, ContentMgmtException {
				return addNewArea( ps,  title, areatype, null);
	}
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public  Area addNewArea(PageSection ps, String title, AreaType areatype, String area_class)  throws ContentCreationException, ContentMgmtException {
		if (ps==null)
			throw new IllegalArgumentException("page section can not be null");
		
		KbeeArea area  = new KbeeArea(title, areatype);
		area.setDomain(ps.getDomain());
		
		if (area_class!=null)
			area.setCss(area_class);
		Long oid = getNewOId();
		area.setState(ObjectState.ENABLED);
		area.setOId(oid);
		area.setOrder(ps.getAreas().size());
		area.setDefaults();
		ps.add(area);
		return area;
	}
	

	@Transactional(propagation = Propagation.REQUIRED)
	public  Area addNewArea(AreaFactory factory, PageSection ps, String title, AreaType areatype)  throws ContentCreationException, ContentMgmtException {
		
		if (ps==null)
			throw new IllegalArgumentException("pagesection can not be null");
		
		if (factory==null)
			throw new IllegalArgumentException("factory can not be null");

		Area b=(Area) factory.create();
		b.setAreaType(areatype);
		b.setDomain(ps.getDomain());
		b.setState(ObjectState.ENABLED);
		b.setTitle(title);
		b.setKey(factory.getKey());
		b.setOrder(ps.getAreas().size());
		
		
		b.setOId(getNewOId());
		b.setDefaults();
		
		ps.add(b);
		return b;
	}



	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public Block addNewBlock(Area area, Block b, AreaSection as, String key)  throws ContentCreationException, ContentMgmtException {

		if (area==null)
			throw new IllegalArgumentException("area can not be null");
		
		//KbeeBlock b= new KbeeBlock(title);	
		b.setDomain(area.getDomain());
		b.setState(ObjectState.ENABLED);
		b.setOId(getNewOId());
		b.setKey(key);
		area.add(b, as);
		b.setDefaults();
		return b;
	}


	
	@Transactional(propagation = Propagation.REQUIRED)
	public  Block addNewBlock(BlockFactory factory, Area area, String title, AreaSection as)  throws ContentCreationException, ContentMgmtException {
		
		if (area==null)
			throw new IllegalArgumentException("area can not be null");
		
		if (factory==null)
			throw new IllegalArgumentException("factory can not be null");

		Block b=(Block) factory.create();
		b.setDomain(area.getDomain());
		b.setTitle(title);
		b.setKey(factory.getKey());
		b.setOId(getNewOId());
		b.setOrder(area.getBlocks().size());
		b.setDefaults();
		area.add(b, as);
		return b;
	}


	
	
	
	@Transactional(propagation = Propagation.REQUIRED)
	public ViewBKIQL createViewQuery(ListViewBlock parent, String title, String iql, String query_type, String description) throws ContentCreationException, 	ContentMgmtException {

		KbeeViewBKIQL view = new KbeeViewBKIQL(title, iql, description, query_type);
		parent.add(view);
		view.setDomain(getDomain());
		view.setDefaults();
		
		getPortalDao().save(view);
		
		// ------------------------------------------------ 
		// creacion del Sitio logger.info(new CreateEvent(content));
		// ------------------------------------------------
		return view;
		
	}
	

		
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Site createPublicWebSite(Person person) throws ContentCreationException, ContentMgmtException {
												
		KbeeSite site = (KbeeSite) createSite("Public Portal "+ person.getDomain().getOrganization());
		
		site.setDescription("Public Portal "+ person.getDomain().getOrganization());
		getPortalDao().save(site);
		
		Page home = addNewPage(site, "home - public web");
		getPortalDao().save(home);
		getPortalDao().save(site);
		
		KbeePageSection ps_home = (KbeePageSection) addNewPageSection(home, "Main Page Section", PageSectionType.ONE_SECTION, PageSectionDisposition.LEFT);
		getPortalDao().save(ps_home);

							
		KbeePageSection ps_top_home = (KbeePageSection) addNewPageSection(home, "Top Page Section", PageSectionType.ONE_SECTION, PageSectionDisposition.TOP);
		getPortalDao().save(ps_top_home);

		KbeePageSection ps_bottom_home = (KbeePageSection) addNewPageSection(home, "Bottom Page Section", PageSectionType.ONE_SECTION, PageSectionDisposition.BOTTOM);
		getPortalDao().save(ps_bottom_home);

		Area area1 = addNewArea(ps_home, "area banner home-page", 	AreaType.AREA_1S);
		Area area2 = addNewArea(ps_home, "area servicios home-page",  AreaType.AREA_1S);
		Area area3 = addNewArea(ps_home, "area proyectos home-page ", AreaType.AREA_1S);
		Area area4 = addNewArea(ps_home, "area_quienes somos home-page", AreaType.AREA_1S);
			
		getPortalDao().save(area1);
		getPortalDao().save(area2);
		getPortalDao().save(area3);
		getPortalDao().save(area4);
		
		
		Block b1 =	addNewBlock(area1, new KbeeBlock("block banner - area banner"), 	AreaSection.LEFT, "block-dummy");
		Block b2 = 	addNewBlock(area1, new KbeeBlock("block servicios - area servicios- home dasboards"), 	AreaSection.CENTER, "block-dummy");
		
		getPortalDao().save(b1);
		getPortalDao().save(b2);
		
		getPortalDao().save(home);
		getPortalDao().save(ps_home);
		getPortalDao().save(site);

		
		// ------------------------------------------------ 
		// creacion del Sitio logger.info(new CreateEvent(content));
		// ------------------------------------------------

		return site;
		
		 
		
		
	}
		 
	public void setPortalDao(PortalDao dao) {
		this.portalDao = dao;
	}


	public PortalDao getPortalDao() {
		return portalDao;
	}

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getSchema() {
		if (schema != null && schema.length() > 0)
			return schema + ".";
		return "";
	}

	/**
	 * @return
	 */
	public Long getNewOId() {
		SqlPlatform sqlplatform = getSqlPlatform();
		Long value = (Long) this.jdbcTemplate.query(sqlplatform.nextSequenceQuery(getSchema() + "portalid_sequence"),
				new ResultSetExtractor<Long>() {
					public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
						if (rs.next())
							return rs.getLong(1);
						return null;
					}
				});
		return value;
	}

	/**
	 * @param key
	 * @param locale
	 * @return
	 */
	protected String getLabel(String key, Locale locale) {
		try {
			ResourceBundle res = ResourceBundle.getBundle(KbeeSiteFactoryService.this.getClass().getName(), locale);
			return res.getString(key);
		} catch (Exception e) {
			logger.error(e);
			return key;
		}

	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}


	private SqlPlatform getSqlPlatform() {

		if (sqlplatform != null)
			return sqlplatform;

		Connection connection = null;
		try {
			connection = this.jdbcTemplate.getDataSource().getConnection();
			sqlplatform = SqlPlatformFactory.getPlatformFor(connection.getMetaData());
		} catch (SQLException e) {
			logger.error(e);

			throw new KbeeRuntimeException(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					logger.error(e);
					throw new KbeeRuntimeException(e);
				}
			}
		}
		return this.sqlplatform;
	}

	private ExternalDao getExternalDao() {
		return (ExternalDao) ServiceLocator.getService(BeansService.class).getBean("externalDao");
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	private DataSet createSiteDataSetIfNotExists(Domain domain) throws ContentMgmtException {
		DataSet site_dataset = getExternalDao().createSiteDataSetIfNotExists(domain);
		getExternalDao().createSiteClassifierIfNotExists(domain, site_dataset);
		return site_dataset;
	}


	private DataSet createSiteRepositoryDataSetIfNotExists(Domain domain) throws ContentMgmtException {
		DataSet site_repository_dataset 	= getExternalDao().createSiteRespositoryDataSetIfNotExists(domain);
		getExternalDao().createSiteRepositoryClassifierIfNotExists(domain, site_repository_dataset);
		return site_repository_dataset;
	}


	private void addDataSetValues(Domain domain, Site site) {
		
		DataSet site_dataset = createSiteDataSetIfNotExists(domain);
		DataSet site_repository_dataset = createSiteRepositoryDataSetIfNotExists(domain);

		String site_url = ServiceLocator.getService(PortalUrlService.class).getSiteUrl(site);

		
		/**
		 * Add Site External Member
		 */
		ExternalMember member_site = getExternalDao().create(site.getOId(), site.getTitle(), site_url,	ObjectState.ENABLED, site_dataset);
		txlogger.info(new DataSetValueCreateEvent(member_site, "Create"));
		
		/**
		 * Add Site Repository Member
		 */
		ExternalMember member_repo = getExternalDao().create(site.getOId(), site.getTitle(), site_url, ObjectState.ENABLED, site_repository_dataset);
		txlogger.info(new DataSetValueCreateEvent(member_repo, "Create"));

		/**
		 *  PortalSecurityService. Crea 3 Grupos y 1 SecurityRule y loguea
		 *	ServiceLocator.getService(PortalSecurityService.class).addSiteGroupsRulesIfNotExist(site, member_site, member_repo, true);
		 */
	}


	protected Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}
	

	
	public ContentDao getContentDao() {
		return contentDao;
	}

	public void setContentDao(ContentDao contentDao) {
		this.contentDao = contentDao;
	}



	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Site createKBaseSite() throws ContentCreationException,  ContentMgmtException {
									
		ResourceBundle resources = ResourceBundle.getBundle(getClass().getName());
		
		UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		Domain domain = userProfile.getDomain();
		KbeeSite site = new KbeeSite();
		Long oid = getNewOId();
		site.setOId(oid);
		if (userProfile != null)
			site.setLastModifiedUser(userProfile.getUser());
		site.setCreationOffsetDateTime(OffsetDateTime.now());
		site.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		site.setIsExternal(false);
		site.setSiteType(SiteType.KNOWLEDGE_BASE);
		site.setName(resources.getString("kbase") + "_" + oid.toString());
		site.setTitle(resources.getString("kbase") + "_" + oid.toString());
		site.setUrl("kbase"+ oid.toString());
		site.setState(ObjectState.ENABLED);
		site.setPublicAccess(true);
		site.setLastModifiedUser(getSessionUser());
		
		

		// Site ---------------------------------------------------
		KbeeJson json_site = new KbeeJson();
		json_site.put("iql", "");
		json_site.put("about-title", "About us");
		json_site.put("about-abstract", "abstract");
		json_site.put("about-text", "About us text");
		json_site.put("search-form", "general");
		site.setCustomValuesJson(json_site);

		
		getPortalDao().save(site);
		txlogger.info(new SiteCreationEvent(site));

		addHomeCCC(site);
		addDataSetValues(domain, site);
		
		getPortalDao().save(site);
		txlogger.info(new SiteUpdateEvent(site));
		return site;
	}
	

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Site createDealRoomSite() throws ContentCreationException, ContentMgmtException {
		ResourceBundle resources = ResourceBundle.getBundle(getClass().getName());

		
		UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		Domain domain = userProfile.getDomain();
		KbeeSite site = new KbeeSite();
		Long oid = getNewOId();
		site.setOId(oid);
		if (userProfile != null)
			site.setLastModifiedUser(userProfile.getUser());
		site.setCreationOffsetDateTime(OffsetDateTime.now());
		site.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		site.setIsExternal(false);

		site.setSiteType(SiteType.DEAL_ROOM);
		site.setTitle(resources.getString("dealroom") + "_" + oid.toString());
		site.setName(resources.getString("dealroom") + "_" + oid.toString());
		site.setUrl("dealroom"+ oid.toString());
		site.setState(ObjectState.ENABLED);
		site.setPublicAccess(true);
		site.setLastModifiedUser(getSessionUser());
		
		// Site ---------------------------------------------------
		KbeeJson json_site = new KbeeJson();
		json_site.put("iql", "");
		json_site.put("about-title", "About us");
		json_site.put("about-abstract", "abstract");
		json_site.put("about-text", "About us text");
		json_site.put("search-form", "general");
		site.setCustomValuesJson(json_site);

		getPortalDao().save(site);
		txlogger.info(new SiteCreationEvent(site));

		addHomeCCC(site);
		addDataSetValues(domain, site);
		
		getPortalDao().save(site);
		txlogger.info(new SiteUpdateEvent(site));
		return site;
	}

	private void addHomeCCC(Site site) {
		// HomePage ------------------------------------------------
		// 
		KbeePage home     = new KbeePage();
		KbeeArea area 	  = new KbeeArea();
		
		area.setAreaType(AreaType.AREA_1S);
		area.setDomain(getDomain());
		area.setLastModifiedUser(getSessionUser());
		area.setTitle("News");
		KbeeBlock block_1 = new KbeeBlock(); block_1.setTitle("Property Data");
		KbeeBlock block_2 = new KbeeBlock(); block_2.setTitle("Reports");
		KbeeBlock block_3 = new KbeeBlock(); block_3.setTitle("Leases");
		KbeeJson json1 = new KbeeJson();
		/**
		 * "blocks"
		 */
		Map<String, String> map = new HashMap<String, String>();
		/**
		map.put("rule_1", "group_id;iql;sort_criteria;order");
		map.put("rule_2", "group_id;iql;sort_criteria;order");
		map.put("rule_3", "group_id;iql;sort_criteria;order");
		map.put("rule_4", "group_id;iql;sort_criteria;order");
		json.put("rules", map);
		*/
		
		json1.put("iql", "isknowledgebase(true)");
		json1.put("include-metadata", "yes");
		json1.put("sort", "modified");
		//block_1.setMaxElements(10);
		block_1.setCustomValuesJson(json1);
		area.add(block_1);
		
		
		KbeeJson json2 = new KbeeJson();
		Map<String, String> map2 = new HashMap<String, String>();
		
		/**
		map2.put("rule_1", "group_id;iql;sort_criteria;order");
		map2.put("rule_2", "group_id;iql;sort_criteria;order");
		map2.put("rule_3", "group_id;iql;sort_criteria;order");
		map2.put("rule_4", "group_id;iql;sort_criteria;order");
		json2.put("rules", map2);
		*/
		
		json2.put("iql", "isknowledgebase(true)");
		json2.put("include-metadata", "no");
		json2.put("sort", "title");
		//block_2.setMaxElements(15);
		block_2.setCustomValuesJson(json2);
		area.add(block_2);
		
		area.add(block_3);
		KbeeJson json3 = new KbeeJson();
		/**
		Map<String, String> map3 = new HashMap<String, String>();
		map3.put("rule_1", "group_id;iql;sort_criteria;order");
		map3.put("rule_2", "group_id;iql;sort_criteria;order");
		map3.put("rule_3", "group_id;iql;sort_criteria;order");
		map3.put("rule_4", "group_id;iql;sort_criteria;order");
		json3.put("rules", map3);
		*/
		json3.put("iql", "isknowledgebase(true)");
		json3.put("include-metadata", "no");
		json3.put("sort", "title");
		//block_3.setMaxElements(15);
		block_3.setCustomValuesJson(json3);
		area.add(block_3);
		
		// TODO VER AT SITE
		// home.add(area);
		//
		site.add(home);

	}





	/**
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void createDataSetSiteProjectsIfNotExists() {
		
		DataSet dp =getContentDao().findDataSetByAlias(DataSet.PORTAL_PROJECTS,  getDomain().getId());
		
		if (dp!=null)
			return;
		
			logger.debug("Creating DataSet Site Project labels for Domain " + getDomain().getDisplayName());
			
			KbeeExternalSet d_project = new KbeeExternalSet();
			d_project.setDomain(getDomain());
			d_project.setCanonical(true);
			d_project.setReadonly(true);
			d_project.setAlias(DataSet.PORTAL_PROJECTS);
			d_project.setExternalSubtype( KbeeExternalSet .EXTERNAL_SITE_PROJECT);
			d_project.setName(getContentDao().findSystemParameterValueByKey("dataset_site_project.name", "Project"));
			d_project.setLastModifiedUser(getSessionUser());
			
			getContentDao().save(d_project);
			
			txlogger.info(new ModelCreateEvent(d_project, "create"));

			KbeeClassifier c_project = new KbeeClassifier();
			c_project.setDomain(getDomain());
			c_project.setName(d_project.getName());
			c_project.setAPIClassifier(false);
			c_project.setUniqueName(Classifier.PORTAL_PROJECTS_SOLR); // tiene que ser consistente con el esquema solr fijo en schema.xml
			c_project.setPredicate(Classifier.PORTAL_HOME_PREDICATE);
			c_project.setMultiplicity(Multiplicity.M01);
			c_project.setContentType(false);
			c_project.setMetadataSubtitle(false);
			c_project.setRuleCondition(false);
			c_project.addDataSet(d_project);
			c_project.setLastModifiedUser(getSessionUser());
			getContentDao().save(c_project);
			txlogger.info(new ModelCreateEvent(c_project, "create"));
			
			DataSet d_portal=getContentDao().findDataSetByAlias(DataSet.PORTAL, getDomain().getId());
			List<Classifier> l=d_portal.getClassifiers();
			l.add(c_project);
			d_portal.setClassifiers(l);
			getContentDao().save(d_portal);
			
			logger.debug("done");
			
			
			
	}
	

			
		
	
	
	

}
