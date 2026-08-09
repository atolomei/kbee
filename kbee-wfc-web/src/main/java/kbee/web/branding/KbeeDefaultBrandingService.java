package kbee.web.branding;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;

import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.service.BrandingService;
import com.novamens.service.ServiceLocator;

import kbee.util.PropertiesFactory;

public class KbeeDefaultBrandingService implements BrandingService, EventListener {


	static private final Properties properties = PropertiesFactory.getInstance("kbee").getProperties();
	static private final String SUPPORT = PropertiesFactory.getInstance("kbee").getProperties().getProperty("support.email", "support@kbee.io").trim();
	
	private String application_url;
	private String application_key;
	private String application_name;
	private String application_name_shrot;
	private String application_tab_label;	// tab name
	private String defaultLoginMessage = null;

	private String esource, training_url;
	
	private String application_search_url;
	private String application_search_css;
	
	private String icon_css;
	private String factory_css;
	
	private Boolean help_enabled;
	private Boolean notifications_enabled;
	
	private Boolean portal_enabled;

	private String company_url;
	
	
	private String sub_application_key;  		
	

	
	@Override
	public void onEvent(Event event) {
 
			if (event instanceof EvictCacheServiceEvent) {
				application_url = null;
				application_key = null;
								
				sub_application_key=null;
				
				application_name =null;
				application_name_shrot =null;
				application_tab_label = null;
				application_search_url= null;
				application_search_css = null;
				
				icon_css = null;
				factory_css = null;
				
				help_enabled = null;
				notifications_enabled = null;
				esource = null;
				training_url = null;
				
				company_url = null;
				th = null;
				noreply = null;
				portal_enabled = null;

			}
				
	}

	
	
	@Override
	public boolean isKbee() {
		return true;
	}
	
	@Override		
	public String getCompanyURL() {
		if (company_url!=null)
			return company_url;
		company_url = ((ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao")).findSystemParameterValueByKey("company_url", "https://www.novamens.com");
		return company_url;

	}

	
	@Override
	public boolean isPortalEnabled() {
		if (portal_enabled!=null)
			return portal_enabled.booleanValue();
		portal_enabled = ((ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao")).findSystemParameterValueByKey( "portal", "yes").toLowerCase().equals("yes");
		return portal_enabled.booleanValue(); 
			
	}
	
	@Override
	public boolean isHelpEnabled() {
		if (help_enabled!=null)
			return help_enabled.booleanValue();
		help_enabled = ((ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao")).findSystemParameterValueByKey( "help", "yes").toLowerCase().equals("yes");
		return help_enabled.booleanValue(); 
			
	}
	
	@Override
	public boolean isNotificationEnabled() {
		if (notifications_enabled!=null)
			return notifications_enabled.booleanValue();
		notifications_enabled = ((ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao")).findSystemParameterValueByKey( "notifications", "yes").toLowerCase().equals("yes");
		return notifications_enabled.booleanValue(); 
			
	}

	
	/**
	 * IDOC Factory css
	 */
	@Override
	public String getFactoryIconCss() {
		if (factory_css!=null)
			return factory_css;
		factory_css = ((ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao")).findSystemParameterValueByKey( "product.factory.icon.css", "rpicon");
		return factory_css;

	}

	/**
	 * RealPage logo css
	 */
	@Override
	public String getApplicationIconCss() {
		if (icon_css!=null)
			return icon_css;
		icon_css = ((ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao")).findSystemParameterValueByKey( "product.application.icon.css", "rp");
		return icon_css;
	
	}

	@Override
	public String getProductKey() {
		if (application_key!=null)
			return application_key;
		application_key = ((ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao")).findSystemParameterValueByKey("product.brand", "kbee");
		return application_key;
	}
	
	@Override
	public String getApplicationName() {
		if (application_name!=null)
			return application_name;
		application_name = ((ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao")).findSystemParameterValueByKey( "product.name", "KBEE");
		return application_name;
	}

	@Override
	public String getApplicationURL() {
		if (application_url!=null)
			return application_url;
		application_url = ((ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao")).findSystemParameterValueByKey("product.url", "http://novamens.com");
		return application_url;
	}
	
	
	@Override
	public boolean listen(Event event) {
		if (event instanceof EvictCacheServiceEvent)
			return true;
		return false;
	}

	@Override
	public String getApplicationShortName() {
		if (application_name_shrot!=null)
			return application_name_shrot;
		application_name_shrot = ((ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao")).findSystemParameterValueByKey( "product.name.short", "kbee");
		return application_name_shrot;
	}


 
	@Override
	public String getLoginCss() {
		return "kbeelogin";
	}
	
	@Override
	public String getApplicationVersion() {
		return VERSION;
	}

	@Override
	public String getProductTabTitle() {
		if (this.application_tab_label!=null)
			return this.application_tab_label;
		this.application_tab_label = ((ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao")).findSystemParameterValueByKey( "product.tab.label", "KBEE");
		return this.application_tab_label;
	}

	@Override
	public String getSearchLibraryApplicationCss() {
		if (application_search_css!=null)
			return application_search_css;
		application_search_css = ((ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao")).findSystemParameterValueByKey("search.library.css", "aa");
		return application_search_css;

	}

	@Override
	public String getDefaultUITheme() {
		return "kbee";
	}

	@Override
	public String getFavIconStr() {
		return "/images/kbee-favicon.gif";
	}
	

	private List<String> th;
	
	@Override
	public List<String> getUIThemes() {
		if (th==null) {
			th = new ArrayList<String>();
			th.add("kbee");
			//th.add("rpdddm");
		}
		return th;
	}
	
	private Map<String, String> getMap(String ...pa) {
		Map<String, String> m=new HashMap<String, String>();
		m.put( "from", pa[0]);
		m.put( "title",pa[1]);
		m.put( "lang",pa[2]);
		m.put( "subject",pa[3]);
		m.put( "text",pa[4]);
		return m;
	}
	
	Map<String, Map<String, String>>  default_email; 
			
	@Override
	public Map<String, Map<String, String>> getDefaultEmailTemplates() {
		
		if (default_email!=null)
			return default_email;
		
		synchronized (default_email) {
			default_email = new HashMap<String, Map<String, String>>();
			
				
			
			default_email.put("welcome", getMap(
								"${domain-noreply}",  	
								"Welcome to ${application}",                 
								"en",	
								"Welcome to kbee – Login Information", 
								"<p>${person-displayname},</p>"
								+ "<p>Welcome to <b>kbee</b>!.</p>"
						)
			);
		
			
		}
		
		return default_email;
	}

	
	
	String noreply = null;
	
	@Override
	public String getNoReplyEmailAddress() {
		if (noreply!=null)
			return noreply;
		synchronized (this) {
			noreply = properties.getProperty("noreply", getProductKey() + " <donotreply@kbee.io>");
		}
		return noreply;
	}

	@Override
	public String getApplicationCss() {
		return "kbee";
	}
	
	
	
	public String getExternalSourceName() {
		if (esource!=null)
			return esource;
		synchronized (this) {
			esource = properties.getProperty("esource",  "KBEE Management");
		}
		return noreply;
	}
	
	
	public String getTrainingUrl() {
		if (training_url!=null)
			return training_url;
		synchronized (this) {
			training_url = properties.getProperty("trainingurl", "https://resources.kbee.io");
		}
		return training_url;
	}

	@Override
	public boolean isHelpVisible() {
		return false;
	}


	@Override
	public String[] getProductCursesName() {
		final String kbee[] =
				{" _   __  _ _ _   _ _ _   _ _ _  ",
						"| | / / |  _  \\ |  _ _| |  _ _| ",
						"| |/ /  | |_| | | |_    | |_    ",
						"|   \\   |  _  | |  _|   |  _|   ",
						"| |\\ \\  | |_| | | |_ _  | |_ _  ",
						"|_| \\_\\ |_ _ _/ |_ _ _| |_ _ _| "
				};
		return kbee;
	}
	
	@Override
	public String getSubProductKey() {
		if (sub_application_key!=null)
			return sub_application_key;
		sub_application_key = ((ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao")).findSystemParameterValueByKey( "subproduct.brand", "KBEE");
		return sub_application_key;
	}
	
	
	
	

	// return  "Todo documento impreso o descargado desde la BCV puede quedar obsoleto al momento de su utilización, por lo que te recordamos la necesidad que verifiques su vigencia (Ver si el número de revisión impreso coincide con el digital al momento de uso).";
	public String getDefaultLoginMessage() {
		if (defaultLoginMessage!=null)
			return defaultLoginMessage;
		defaultLoginMessage = ((ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao")).findSystemParameterValueByKey( "login.default.message", "");
		return defaultLoginMessage;
	}
	
	String defaultContactText = null;
	String defaultLoginLearMoreLink = null;
	String defaultLoginLearMoreText =  null;
	String defaultContactLink = null;
	
	public String getDefaultLoginLearMoreText() {
		if (defaultLoginLearMoreText!=null)
			return defaultLoginLearMoreText;
		defaultLoginLearMoreText = ((ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao")).findSystemParameterValueByKey( "login.default.learn-more-text", "Learn more about kbee");
		return defaultLoginLearMoreText;
	}
	
	
	
	public String getDefaultLoginLearMoreLink() {
		if (defaultLoginLearMoreLink!=null)
			return defaultLoginLearMoreLink;
		defaultLoginLearMoreLink = ((ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao")).findSystemParameterValueByKey( "login.default.learn-more-link", "https://kbee.io");
		return defaultLoginLearMoreLink;
	}
	
	
	@Override
	public String getDefaultContactText() {
		if (defaultContactText!=null)
			return defaultContactText;
		defaultContactText = ((ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao")).findSystemParameterValueByKey( "login.default.contact.text", "");
		return defaultContactText;
		
	}

	@Override
	public String getDefaultContactLink() {
		if (defaultContactLink!=null)
			return defaultContactLink;
		defaultContactLink = ((ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao")).findSystemParameterValueByKey( "login.default.contact.link", "");
		return defaultContactLink;
	}



	private String supportEmail;
	
	@Override
	public String getSupportTicketEmailAddress() {
		if (supportEmail!=null)
			return supportEmail;
		supportEmail = ((ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao")).findSystemParameterValueByKey( "support.email",  SUPPORT);
		return supportEmail;

	}


	
}
