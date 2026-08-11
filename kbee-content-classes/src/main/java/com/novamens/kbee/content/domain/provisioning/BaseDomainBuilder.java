package com.novamens.kbee.content.domain.provisioning;


import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.user.UserService;
import com.novamens.dao.SecurityDao;
import com.novamens.dom.Domain;
import com.novamens.kbee.security.KbeeSecurityDao;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.security.User;
import com.novamens.service.LanguageService;
import com.novamens.service.ServiceLocator;

import kbee.util.PropertiesFactory;

public class BaseDomainBuilder  {
										
	static public String EXTERNAL_CABINET 	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("cabinet.external",  "External");
	static public String EMAIL_NOREPLY 		= PropertiesFactory.getInstance("kbee").getProperties().getProperty("noreply.email", 	 "noreply@kbee.io");
	
	static public String DEFAULT_LABELS 	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("labels.default","Draft;Delete;Follow up;Duplicate;Review");
	static public String DEFAULT_TYPES 		= PropertiesFactory.getInstance("kbee").getProperties().getProperty("dataset_type.default","Training;Tenant Selection Plan;Contract;EOM Financials;Mortgage Statement;Lease Agreement;Rent Schedule;Management Agreements;Non-Disclosure Agreement;Shareholder Meetings;Lawsuits;Acquisitions;Due Diligence;Territory Assignments;Sales Incentives;Compensation Plan;Hardware;Software;System Logs;Benefits;Organizational Chart;Annual Reviews;Offer Letters;Signage;Brochures;Flyers");
	static public String DEFAULT_STATUS 	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("dataset_status.default","Draft;Under Review;Approved;Final;Cancelled");
						
	static public String DEFAULT_SECURED_ACCESS = PropertiesFactory.getInstance("kbee").getProperties().getProperty("dataset_secured_access.default","Public;Secured");
	static public String DEFAULT_DEPARTMENT		= PropertiesFactory.getInstance("kbee").getProperties().getProperty("dataset_department.default","Marketing;HR;IT;Sales;Legal;Finance;Compliance;Property Management;Facilities;Training");
				
	static public String EMAIL_SUPPORT_1 	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("support1.email", 	 "support1@kbee.io");
	static public String EMAIL_SUPPORT_2 	= PropertiesFactory.getInstance("kbee").getProperties().getProperty("support2.email", 	 "support2@kbee.io");
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(BaseDomainBuilder.class.getName());
	
	private Domain domain;
	
	private String default_time_zone;
	LanguageService ls;
	
	
	public BaseDomainBuilder() {
	}
	
	public BaseDomainBuilder(Domain domain) {
		this.domain=domain;
	}
	
	public Domain getBuildingDomain() {
		return domain;
	}

	public void setBuildingDomain(Domain domain) {
		this.domain = domain;
	}

	protected String getStringValue( String key, Map<String, Object> map) {
		if (map.containsKey(key) && map.get(key) != null)
			return map.get(key).toString();
		return "key";
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	protected User getSessionUser() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}
	
	protected SecurityDao  getSecurityDao() {
		return	(SecurityDao) ServiceLocator.getService(BeansService.class).getBean("securityDao");
	}
	
	protected <T> DomRepository<T> getRepository(Class<T> objectclass) {
		DomRepository<T> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
	}
	
	protected User getRootUser(Domain domain) {
		return ((KbeeSecurityDao) getSecurityDao()).findUserByName("root@"+ domain.getName());
	}
	
	protected ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}

	
	
	protected LanguageService getLanguageService() {
		if (ls==null)
			ls = ServiceLocator.getService(LanguageService.class);
		
		return this.ls;
	}
	
	
	protected Locale DomaingetLocale() {
		return getBuildingDomain().getLocale();
	}
	

	protected String getDefaultTimeZone() {
		if (default_time_zone==null) {
			synchronized(this) {
				logger.debug(TimeZone.getDefault().getID());
				default_time_zone = getContentDao().findSystemParameterValueByKey("timezone.default", "US/Central");
			}
		}
		return default_time_zone;
	}

}
