package kbee.web.help;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import com.novamens.service.ApplicationServerService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeFileUtils;

import kbee.util.FSUtils;

import kbee.web.error.ErrorPanel;



/**
 * 
 * 
 *
 */
public class KbeeInlineHelpWebService implements InlineHelpWebService {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeInlineHelpWebService.class.getName());

	static final int BUFFER_SIZE = 16384;
	
	private Map<String, String> help_en;
	private Map<String, String> help_es;

	
	@Override
	public Panel getPanel(String id,  Locale locale, String key) {
		return getPanel(id, locale, key, null);
	}
	
	/**
	 *  home/mytasks
	 */
	@Override
	public Panel getPanel(String id,  Locale locale, String key,  Map<String, String> helpContext) {
		
		String value;
		
		if (getHelp(locale).containsKey(key))
			value=getHelp(locale).get(key);
		
		else if (getHelp(Locale.ENGLISH).containsKey(key)) 
			value=getHelp(Locale.ENGLISH).get(key);
		else
			value = "no value for key > " + key;

		return new InlineHelpPanel(id, new Model<String>(value));
		
		 
	}

	// InlineHelpContent c = new InlineHelpContent(); 

	
	
	private Map<String, String> getHelp(Locale locale) {
		
		
		
		if (help_es==null || help_en==null) 
				load();
			
		if (locale.getLanguage().equals("es")) 
			return help_es;
		return help_en;
	}
	
	
	
	
	private synchronized void load() {
		
		help_en = new HashMap<String, String>();	
		help_es = new HashMap<String, String>();
		
		
		{
			File base = new File(ServiceLocator.getService(ApplicationServerService.class).getInlineHelpDir() + File.separator + "eng");
			if (base.exists() && base.isDirectory()) {
					File arrfiles [] = base.listFiles();
					for (File file: arrfiles) {
						if (file.isFile()) {
					        
					        try {
					        	StringBuilder str  = new StringBuilder();
					        	BufferedReader br = new BufferedReader(new FileReader(file));
						        	 String st;
						        	 try {
										while ((st = br.readLine()) != null) {
											str.append(st);
										 }
									} catch (IOException e) {
										logger.error(e);
									}
						        	
						        	String na=FileNameUtils.getBaseName(file.getName()).toLowerCase();
						        	help_en.put(na, str.toString());
					        
					        } catch (FileNotFoundException e) {
								logger.error(e);
							}
						}
				}
			} else {
				logger.error(" no help directory -> " + base!=null?base.getAbsolutePath() : "null" );
			}
		}
		
		
		
		{
			File base = new File(ServiceLocator.getService(ApplicationServerService.class).getInlineHelpDir() + File.separator + "es");
			
			if (base.exists() && base.isDirectory()) {
				File arrfiles [] = base.listFiles();
				for (File file: arrfiles) {
						if (file.isFile()) {
					        
					        try {
					        	StringBuilder str  = new StringBuilder();
					        	BufferedReader br = new BufferedReader(new FileReader(file));
						        	 String st;
						        	 try {
										while ((st = br.readLine()) != null) {
											str.append(st);
										 }
									} catch (IOException e) {
										logger.error(e);
									}
						        	
						        	String na=FileNameUtils.getBaseName(file.getName()).toLowerCase();
						        	help_es.put(na, str.toString());
						        	// logger.debug(na + " -> " + str.toString());
					        
					        } catch (FileNotFoundException e) {
								logger.error(e);
							}
						}
				}
		}
			else {
				logger.error(" no help directory -> " + base!=null?base.getAbsolutePath() : "null" );
			}
	}
		
	logger.debug(help_en.toString());
	logger.debug(help_es.toString());
		
		
	}
	
	
	private String getHelpText(Locale locale, String key) {
		
		if (locale.getLanguage().equals("es")) {
			// getHelp("es")
		}
		
		return "not found for key -> " + key;
	}
	
	
	
}
