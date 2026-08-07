package com.novamens.service;


import java.util.List;
import java.util.Map;

/**
 * {@link BrandingService} must be binded by the Spring configuration xml.
 * see {@link KbeeDefaultBrandingService}

 **/
public interface BrandingService extends BusinessSystemService {

	 public final String VERSION = "8.5";
	 
	
	 public String getApplicationIconCss();
	 public String getFactoryIconCss();
	
	 // iDOC kbee
	 public String getApplicationName();
	 
	 //  kbee.io
	 public String getApplicationURL();
	 
	//  kbee.io
	public String getApplicationVersion();
		 
	// is external help service available ?
	public boolean isHelpEnabled();
	
	// is portal module enabled
	public boolean isPortalEnabled();

	// idoc, kbee
	public  String getProductKey();
	public  boolean isNotificationEnabled();

	public  String getLoginCss();
	public  String getApplicationCss();
	
	public  String getApplicationShortName();
	public  String getProductTabTitle();

				
	public String getSearchLibraryApplicationCss();
	public String getDefaultUITheme();
	
	public String getFavIconStr();
	String getCompanyURL();
	public List<String> getUIThemes();
	
	public boolean isKbee();
	public Map<String, Map<String, String>> getDefaultEmailTemplates();
	public String getNoReplyEmailAddress();
	
	public String getSupportTicketEmailAddress();
	
	
			
	public String getExternalSourceName();
	public String getTrainingUrl();
	public boolean isHelpVisible();

	String[] getProductCursesName();
	String getSubProductKey();
	
	public String getDefaultLoginLearMoreLink();
	public String getDefaultLoginMessage();
	public String getDefaultLoginLearMoreText();
	String getDefaultContactText();
	String getDefaultContactLink();
}