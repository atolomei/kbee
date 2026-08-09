package kbee.web.report;

import java.io.Serializable;

import org.apache.commons.text.WordUtils;

import com.novamens.beans.BeansService;

import com.novamens.security.User;
import com.novamens.service.LanguageService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


public class ReportFactory implements Serializable {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReportFactory.class.getName());
	
	private static final long serialVersionUID = 1L;

	private String key;
	private String displayname; // default display name, can be overriden by LanguageService
	private String abstract_str; // default abstract, can be overriden by LanguageService
	
	private String bean;
	private String group;
	
	public ReportFactory (String key, String bean, String displayName, String abstract_str) {
		this.key = key;
		this.displayname = displayName;
		this.abstract_str=abstract_str.replace("\\n", "<br />");
		this.bean = bean;
	}
	
	
	public ReportFactory (String key, String bean, String displayName) {
		this.key = key;
		this.displayname = displayName;
		this.bean = bean;
		this.abstract_str=key;
	}
	
	public ReportFactory (String key, String bean) {
		this.key = key;
		this.displayname = parse(key);
		this.bean = bean;
		this.abstract_str=key;
	}

	private String parse(String s) {
		return WordUtils.capitalize(s.replace("-", " "));
	}
	
	
	public String getReportGroup() {
		if (group==null)
			group=getReport().getReportGroup();
		return group;
	}
	public String getKey() {
		return key;
	}
	
	public ReportConsole getReport() {
		try {
			return (ReportConsole) ServiceLocator.getService(BeansService.class).getBean(bean, getKey());
		} catch (Exception e) {
			logger.error(e);
			throw(e);
		}
	}

	/**
	 * @return
	 */
	public String getDisplayName() {
		return ServiceLocator.getService(LanguageService.class).getString(getKey(), getSessionUser().getLocale(), displayname);
	}
	
	
	public String getReportAbstract() {
		return ServiceLocator.getService(LanguageService.class).getString(getKey()+"-abstract", getSessionUser().getLocale(), abstract_str);
	}
		
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
