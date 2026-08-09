package kbee.web.help;

import java.util.Locale;
import java.util.Map;

import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.service.BusinessSystemService;

public interface InlineHelpWebService extends BusinessSystemService {

	
	static final public String PORTAL_LIBRARY = "portal-library";
	static final public String PORTAL_SIMPLE_TEXT = "portal-simple-text";
	static final public String PORTAL_FAV_CATEGORIES = "portal-categories";
	
	
	
	static final public String HOME_FACTORY= "home-factory";
	
										
	static final public String HOME_ACCOUNT = "home-account";
	static final public String HOME_PROGRESS_NOTES = "home-progress-notes";
	
	static final public String HOME_LIBRARY = "home-library";
	static final public String HOME_MONITOR =  "home-monitor";
	static final public String HOME_MYTASKS = "home-mytasks";
	
	
	
	static final public String HOME_DATASETMEMBERS = "home-datasetmembers";
	static final public String HOME_DATASETMEMBERS_ENTITIES = "home-datasetmembers-entities";
	
	
	
	static final public String HOME_NEW = "home-new";
	static final public String HOME_NOTES = "home-notes";
	static final public String HOME_NOTIFICATIONS = "home-notifications";
	
	static final public String HOME_PENDING = "home-pending";
	static final public String HOME_PORTALS = "home-portals";
	

	static final public String HOME_MODEL = "home-model";
	
	static final public String HOME_MODEL_ELEMENTS 		= "home-model-elements";
	
	static final public String HOME_MODEL_TEMPLATES 	= "home-model-templates";
	static final public String HOME_MODEL_PROCESS 		= "home-model-process";
	public static final String HOME_MODEL_EFORMS 		= "home-model-eforms";
	
	public static final String FACTORY_HOME_SERVER_INFO	= "factory-home-servers";
	
	
	public Panel getPanel(String id, Locale locale, String key);
	public Panel getPanel(String id, Locale locale, String key, Map<String, String> helpContext);
	
}
