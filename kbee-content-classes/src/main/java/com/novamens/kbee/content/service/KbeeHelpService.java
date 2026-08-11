package com.novamens.kbee.content.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.content.service.HelpService;

import kbee.util.PropertiesFactory;


public class KbeeHelpService implements HelpService {
				
	static String HELP_PROD_URL			 = PropertiesFactory.getInstance("kbee").getProperties().getProperty("help.server.production", "https://kbee.io/");			 
	static String HELP_TEST_URL  		 = PropertiesFactory.getInstance("kbee").getProperties().getProperty("help.server.testing", "https://kbee.io/" );
	static String HELP_IS_TESTING_SERVER = PropertiesFactory.getInstance("kbee").getProperties().getProperty("help.server.testing", "false");
	static String HELP_ENV 				 = PropertiesFactory.getInstance("kbee").getProperties().getProperty("help.env", "on");
	static String HELP_VER 				 = PropertiesFactory.getInstance("kbee").getProperties().getProperty("help.ver", "30");
	static String HELP_SERVER 			 = PropertiesFactory.getInstance("kbee").getProperties().getProperty("help.ver", "350");
	static String HELP_CENTER_STRING	 = PropertiesFactory.getInstance("kbee").getProperties().getProperty("help.center.string", "000000000000000000000000000000000000000010000000000000000000000000000000000000000000");
			
	static String test_url = HELP_TEST_URL;
	static String prod_url  = HELP_PROD_URL;	
	
	@SuppressWarnings("unused")
	static private Logger logger = LogManager.getLogger(KbeeHelpService.class.getName());

	
	@Override
	public String getHelpUrl(String page_key) {
		
		String url;
		
		if(HELP_IS_TESTING_SERVER.equals("false"))
				url=prod_url;
		else
			url=test_url;
		return url + "?env=" + HELP_ENV + "&pg=" + page_key + "&vr=" + HELP_VER +"&Scrver=" + HELP_SERVER +"&cs=" + HELP_CENTER_STRING;    
	
	}

}
