package kbee.web.portal6;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.dao.ContentDao;
import com.novamens.content.searcher.SearcherHomeBlock;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.event.Event;
import com.novamens.event.EventListener;

import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.kbee.portal.model.KbeeSite;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteTemplate;
import com.novamens.service.BrandingService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KeyValue;

import kbee.util.PropertiesFactory;


public class KbeeDomainSearcherPortalService implements DomainSearcherPortalService, EventListener {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeDomainSearcherPortalService.class.getName());
	
	static final Properties properties = PropertiesFactory.getInstance("kbee").getProperties();
	
	private Domain domain = null;

	/**
	 * Data structures shared by all Domains   
	 */
								
	private static Map<String, String> portal_name;
	private static Map<String, String> iql_query;
	
	private static Map<String, HomeBlock> home_block;
	private static Map<String, String> searcher_query;
	private static Map<String, Map<String, String>> about_lang;
	private static Map<String, List<KeyValue<String>>> footerMenuOptions_lang;

	/**
	 * home_block_lang
	 * 
	 * 
	 * "key" -> "news", "useful links"
	 * 
	 * "title"
	 * "iql"
	 * "total"
	 * 
	 **/
	
	private ContentDao contentDao;
	
	public KbeeDomainSearcherPortalService() {
	}
	
	
	
	@Override
	@Transactional
	public void save (SearcherHomeBlock block) {
		 getContentDao().save(block);
		 // logger event.
	}
	
	
	@Override
	public List<SearcherHomeBlock> getSearcherHomeBlock() {
		return getContentDao().getSearcherHomeBlock(getDomain());
	}
	
	
	public KbeeDomainSearcherPortalService(Domain domain) {
		this.domain=domain;
	}

	@Override
	public boolean listen(Event event) {
		if (event instanceof EvictCacheServiceEvent)
			return true;
		return false;
	}

	@Override
	public void onEvent(Event event) {
			if (event instanceof EvictCacheServiceEvent)
				evict();
	}
	
	public void evict() {
		footerMenuOptions_lang=null;
		home_block =null;
		searcher_query=null;
		about_lang=null;
		footerMenuOptions_lang=null;
	}

	@Override
	public  List<KeyValue<String>> getFooterMenuOptions() {
		return getFooterMenuOptions(getDomain().getLocale());
	}
	
	public String getAboutTitle() {return getAboutTitle(getDomain().getLocale()); } 
	public String getAboutAbstract() {return getAboutAbstract(getDomain().getLocale()); } 
	public String getAboutText() {return getAboutText(getDomain().getLocale()); }
	
	

	
	
	
	/**
	 */
	public String getAboutTitle(Locale locale) {
		
		 if (about_lang==null)
			 about_lang=new  ConcurrentHashMap<String, Map<String,String>>();
		 
		if (!about_lang.containsKey(locale.getLanguage())) 
				about_lang.put(locale.getLanguage(), new HashMap<String, String>());
			
		String key="searcher.portal.domain_"+getDomain().getName().trim()+".about_title."+locale.getLanguage();	logger.debug(key);
		
		if (about_lang.get(locale.getLanguage()).containsKey(key))
			about_lang.get(locale.getLanguage()).get(key);
		
		synchronized (about_lang) {
			String value = getContentDao().findSystemParameterValueByKey(key, DEFAULT_ABOUT_TITLE);
			about_lang.get(locale.getLanguage()).put(key, value);
		}
		return about_lang.get(locale.getLanguage()).get(key);
	}
	
	/**
	 * 
	 */
	public String getAboutAbstract(Locale locale) {
		
		 if (about_lang==null)
			 about_lang = new  ConcurrentHashMap<String, Map<String,String>>();
		 
		if (!about_lang.containsKey(locale.getLanguage())) 
				about_lang.put(locale.getLanguage(), new HashMap<String, String>());
		
		String key="searcher.portal.domain_"+getDomain().getName().trim()+".about_abstract."+locale.getLanguage(); logger.debug(key);
		
		if (about_lang.get(locale.getLanguage()).containsKey(key))
			about_lang.get(locale.getLanguage()).get(key);
		
		synchronized (about_lang) {
			String value = getContentDao().findSystemParameterValueByKey(key, DEFAULT_ABOUT_ABSTRACT);
			about_lang.get(locale.getLanguage()).put(key, value);
		}
		return about_lang.get(locale.getLanguage()).get(key);
	}
	
	
	public String getAboutText(Locale locale) {
		 if (about_lang==null)
			 about_lang = new  ConcurrentHashMap<String, Map<String,String>>();
		 
		if (!about_lang.containsKey(locale.getLanguage())) 
				about_lang.put(locale.getLanguage(), new HashMap<String, String>());
		
		String key="searcher.portal.domain_"+getDomain().getName().trim()+".about_text."+locale.getLanguage(); logger.debug(key);
		
		if (about_lang.get(locale.getLanguage()).containsKey(key))
			about_lang.get(locale.getLanguage()).get(key);
		
		synchronized (about_lang) {
			String value = getContentDao().findSystemParameterValueByKey(key, DEFAULT_ABOUT_TEXT);
			about_lang.get(locale.getLanguage()).put(key, value);
		}
		return about_lang.get(locale.getLanguage()).get(key);
	}
	

	
	/**
	 * 
	 */
	@Override
	public List<KeyValue<String>> getFooterMenuOptions(Locale locale) {

		 if (footerMenuOptions_lang==null)
			footerMenuOptions_lang = new  ConcurrentHashMap<String, List<KeyValue<String>>>();
		
		if (footerMenuOptions_lang.containsKey(locale.getLanguage())) 
				return footerMenuOptions_lang.get(locale.getLanguage());
		
		
		synchronized (footerMenuOptions_lang) {
		
			if (footerMenuOptions_lang.containsKey(locale.getLanguage())) 
				return footerMenuOptions_lang.get(locale.getLanguage());
			
			List<KeyValue<String>> list = new ArrayList<KeyValue<String>>();
				
			for (int n=0; n<8; n++) {
				
				String key_en    = "searcher.portal.menu_"+String.valueOf(n)+".label.eng";		logger.debug(key_en);
				String key_spa   = "searcher.portal.menu_"+String.valueOf(n)+".label.spa";		logger.debug(key_spa);
				
				String value_en  = getContentDao().findSystemParameterValueByKey(key_en, null);
				String value_spa  = getContentDao().findSystemParameterValueByKey(key_spa, null);
	
				if (n==0 && value_en==null)	value_en= "About";
				if (n==1 && value_en==null)	value_en= "Contact";
				if (n==2 && value_en==null)	value_en= "Privacy Shield";

				if (n==0 && value_spa==null)	value_spa= "Acerca";
				if (n==1 && value_spa==null)	value_spa= "Contacto";
				if (n==2 && value_spa==null)	value_spa= "Confidencialidad";
				
				String lkey_en     = "searcher.portal.menu_"+String.valueOf(n)+".link.eng";		logger.debug(lkey_en);
				String lkey_spa    = "searcher.portal.menu_"+String.valueOf(n)+".link.spa";		logger.debug(lkey_spa);
				
				String lvalue_en   = getContentDao().findSystemParameterValueByKey(lkey_en, null);
				String lvalue_spa  = getContentDao().findSystemParameterValueByKey(lkey_spa, null);
						
				if (n==0)	lvalue_en= "/about"; 
				if (n==1)	lvalue_en= "/contact"; 
				if (n==2)	lvalue_en= "/privacy-shield";

				if (lvalue_en!=null)			list.add(new KeyValue<String>(value_en,  lvalue_en));
				// if (lvalue_spa!=null)			list.add(new Pair(value_spa, lvalue_spa));
				
			}
			
			footerMenuOptions_lang.put(locale.getLanguage(), list);
		}
		
		return footerMenuOptions_lang.get(locale.getLanguage());
	}

	
	
	@Override
	public HomeBlock getHomeBlock(String key) {
	
		if (home_block==null)
			home_block = new  ConcurrentHashMap<String, HomeBlock>();
		
		String xkey = getDomain().getId().toString()+"-"+key;

		if (home_block.containsKey(xkey))
			return home_block.get(xkey);

		
		synchronized (home_block) {
			
			if (home_block.containsKey(xkey))
				return home_block.get(xkey);
		
			HomeBlock hb = new HomeBlock();
			
			String hb_k = "searcher.portal.domain_"+getDomain().getId().toString()+".home_block_"+key;
			
	 		hb.title_en 	  = getContentDao().findSystemParameterValueByKey(hb_k+".title.eng", null);
	 		hb.title_spa   	  = getContentDao().findSystemParameterValueByKey(hb_k+".title.spa", null);
	 		hb.iql 	    	  = getContentDao().findSystemParameterValueByKey(hb_k+".iql", null);
	 		
	 		if      (key.equals("news")       && hb.title_en==null)   hb.title_en="Property Data";
	 		if 		(key.equals("links")      && hb.title_en==null)   hb.title_en="Reports"; // "Tutorials and Support";
	 		if 		(key.equals("queries")    && hb.title_en==null)   hb.title_en="Leases"; //  "Toolbox";
	 		
	 		if      (key.equals("news")       && hb.title_spa==null)   hb.title_spa="Novedades";
	 		if 		(key.equals("links")      && hb.title_spa==null)   hb.title_spa="Tutoriales";
	 		if 		(key.equals("queries")    && hb.title_spa==null)   hb.title_spa="Enlaces útiles";
	 		
	 		if      (key.equals("news")       && hb.iql==null)   hb.iql="isknowledgebase(true)";
	 		if 		(key.equals("links")      && hb.iql==null)   hb.iql="isknowledgebase(true)";
	 		if 		(key.equals("queries")    && hb.iql==null)   hb.iql="isknowledgebase(true)";
	 	
	 		try {		
	 			String t=getContentDao().findSystemParameterValueByKey(hb_k+".total", null);
		 		if      (key.equals("news")       && t==null)   t="10";
		 		if 		(key.equals("links")      && t==null)   t="15";
		 		if 		(key.equals("queries")    && t==null)   t="15";
	 			hb.total      = Long.valueOf(t);
	 			
	 		} catch (Exception e) {
	 			
	 			logger.error(e);
	 			
		 		if      (key.equals("news"))   hb.total=new Long(10);
		 		if 		(key.equals("links"))  hb.total=new Long(15);
		 		if 		(key.equals("queries"))  hb.total=new Long(15);
	 		}
	 		
			hb.key = key;
			
			hb.isEnabled= (hb.iql!=null);
			home_block.put(xkey, hb);
		}
	
		return home_block.get(xkey);
	}
	
	OffsetDateTime last_check = OffsetDateTime.now();
			
	
	
	/**
	 * Default iql 
	 *  
	 * @param site
	 * @return
	 */
	@Override
	public String getDefaultSearcherPortalIql(Site site) {

		if (iql_query ==null)
			iql_query = new  ConcurrentHashMap<String, String>();
		
		String xkey = "searcher.portal.domain_"+getDomain().getId().toString()+".iql"; logger.debug(xkey);
			
		
		synchronized (iql_query) {
			String xn= (getDomain().getDomainType()==DomainType.COMPLIANCE) ? "isknowledgebase(true)" : "";
			 String name = getContentDao().findSystemParameterValueByKey("searcher.portal.domain_"+getDomain().getId().toString()+".iql", xn);
 			iql_query.put(xkey, name);
			 
		}
		
		logger.debug(xkey);
		return iql_query.get(xkey);
	}


	
	
	@Override
	public String getSearchPortalName(Locale locale) {
		
		if (portal_name==null)
			portal_name = new  ConcurrentHashMap<String, String>();
		
		String xkey = "searcher.portal.domain_"+getDomain().getId().toString()+".portal_name."+locale.getLanguage(); logger.debug(xkey);
		
		if (portal_name.containsKey(xkey))
			return portal_name.get(xkey);

		synchronized (portal_name) {
			String name = getContentDao().findSystemParameterValueByKey("searcher.portal.domain_"+getDomain().getId().toString()+".portal_name."+locale.getLanguage(), null);
			
			if (name==null && locale==Locale.ENGLISH) 										name = "Knowledge Base";
			if (name==null && (locale.getLanguage()=="spa" || locale.getLanguage()=="es")) 	name = "Base de Conocimientos";
			if (name==null) name = "Knowledge Base";
			
			portal_name.put(xkey, name);
		}
		
		return portal_name.get(xkey);
	}
	
	/**
	 * for public portals
	 */
	@Override
	public boolean isPublic() {
		return false;
	}

	
	public void setContentDao(ContentDao dao) 						{contentDao=dao;}
	public ContentDao getContentDao()							 	{return contentDao;} // return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");

	private Domain getDomain() {
		return this.domain;
	}

	
	static String DEFAULT_ABOUT_TITLE = "Windsor Knowledge Base Portal";
	static String DEFAULT_ABOUT_ABSTRACT = "Compliance monitoring and asset management for all property types, taking the burden off of your staff, mitigating risk and keeping your properties in line with changing regulations.";
	static String DEFAULT_ABOUT_TEXT ="<h2 class=\"inst rp-heading overline-double-left\" style=\"clear: left; float: left; width: 100%;\">Stay Ahead of the Compliance Curve</h2>"+
			"<p>Changing Affordable Housing regulations can throw you a curve. Penalties for noncompliance can mean huge financial losses for owners and fee managers. That’s a lot to keep up with. Stay ahead of it all with RealPage Compliance Services."+
			"Our dedicated teams of compliance experts are at your service to help mitigate risk, maximize occupancy and ensure property compliance with federal, state and local government regulations.</p> <p>Scalable and platform agnostic, RealPage Document Director is suitable for any sized property management company, regardless of the property management system they use. In " +
				"addition, it’s web-based, accessible from anywhere and offersproperty owners and managers the ability to electronically store, organize, browse, search and securely publish all types "+ 
				"of corporate, property and resident documents, including:	compliance documents, due diligence documents, resident	documents, financial statements, employee handbooks and	training materials."+
			"</p><p>Knowing whether a property is in compliance with these	regulations often requires expertise that few property managers	have, in spite of the fact that millions of dollars are at " +
				"stake. RealPage Compliance Services provides services to ensure	compliance with local, state and federal Affordable Housing	regulations. Our products and services work seamlessly within " +
				"your company’s business processes to meet all of your asset	management and compliance needs. It’s the expertise you need to	be sure.</p>" +
			"<h2 class=\"inst mt1em\">A complete document management system that’s secure, flexible and friendly</h2><p>In addition to our products’ success, our growth is	supported by acquisitions that enhance our product development " +
				" and sales and marketing efforts and expand the types of rental housing properties we serve.</p>";

}
