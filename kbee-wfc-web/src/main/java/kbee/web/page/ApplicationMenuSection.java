package kbee.web.page;

import java.util.Locale;
import java.util.ResourceBundle;


/**
 * 
 * 1 to 100 are reserved for these Section
 * 100+ are SiteIds (SiteStatInEvent)
 * 
 *
 */
public enum ApplicationMenuSection {
								
	HOME 			(1, "home"),
	
	MYWORK 			(2, "mywork"),
	
	TASK 			(3, "task"),
	
	DRAFTRESOURCES	(50, "draftresources"),
	
	CONTENT 		(4, "content"), 
	SETTINGS 		(5, "settings"),
	SECURITY 		(6, "security"	),
	SITES	 		(7, "sites"	),
	LOGS 			(8, "audit"),
	REPORTS 		(9, "reports"),
	
	MINI_SITE 		(42, "mini-site"),
	

	DOMAINS			(10, "domains"),
	INFO 			(11, "info"),
	DATA_MANAGEMENT (12, "datamanagement"),
	API 			(13, "api"),
	GENERAL 		(14, "general"),
	INTEGRATION		(15, "integration"),

	SEARCHER		(16, "searcher"),
	PAYMENTS 		(17, "payments"),
	
	ALERT_SETTINGS 	(20, "alertsettings"),
	USER_MESSAGES 	(30, "messages"),
	SUPPORT		 	(40, "support");
	
		
	private String key;
	private int id;
	
	
	private ApplicationMenuSection(int code, String key) {
		this.key = key;
		this.id = code; 
	}
	
	public String toString() {
		return ("id: " + getId() + ". key: "+ getKey());
	}

	public String getKey() {
		return key;
	}
	
	public String getDisplayName() {
		return getLabel();
	}
	
	public String getLabel() {
		return getLabel(Locale.getDefault());
	}
	
	
	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(ApplicationMenuSection.this.getClass().getName(), locale);
		return res.getString(this.key);
	}
	
	public int getId() {
		return id;
	}
	
}
