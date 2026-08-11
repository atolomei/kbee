package com.novamens.kbee.content.domain.provisioning;


import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.service.domain.DomainBuilderService;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.KBFSStorageType;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.domain.KbeeDomain;

import com.novamens.logging.DomainCreateEvent;

import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.system.properties.SystemPropertiesService;


/**
 * 
 * view {@link BaseDomainBuilder} 
 *
 */
public class KbeeDomainBuilderService implements DomainBuilderService {
			
	/** Logger that works synchronously in the TRX thread */
	
	static private Logger txlogger = LogManager.getLogger("TxLogger");
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeDomainBuilderService.class.getName());
	

	static private final int UNLIMITED_HD 		= -1;
	static private final int UNLIMITED_USERS 	=  0;
	public static final  int DEFAULT_HARD_DISK 	= 100;
	public static final  int DEFAULT_USERS 		= -1;

	private ContentDao dao=null; 	// Spring assigned
	private String default_time_zone;
	
	/**
	 * Settings
	 * Canonical Groups
	 * Cabinets
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Domain createDomain(String name, Map<String, Object> map) throws ContentMgmtException, ContentCreationException {
		
		if (!(	map.containsKey("type") 			&&
				map.containsKey("root_password") 	&&
				map.containsKey("root_email") 		&&
				map.containsKey("admin_username") 	&&
				map.containsKey("admin_lastname") 	&&
				map.containsKey("timezone") 		&&
				map.containsKey("locale") 			&&
				map.containsKey("api"))) {
			
			if (logger.isDebugEnabled())	
				map.entrySet().forEach(item -> logger.debug(item.getKey() + " " + item.getValue()));
				
			throw new ContentCreationException("Mandatory parameters missing");
		}
			
		KbeeDomain n_domain = new KbeeDomain();
		n_domain.setEncryptFiles(true);
		n_domain.setName(name);
		n_domain.setDomainType(map.get("type") == null ? DomainType.PREMIUM : (DomainType)map.get("type"));
		
		ZoneId zid = getZoneId(map);
		n_domain.setTimeZone(zid.getId());
		
		
		Locale locale = getLocale(map);
		n_domain.setLanguage(locale.getLanguage());
		
		/**
		// where to store binary objects 
		// the main purpose of this field was
		// to allow beta testing during the transition to KBFS2 and distributed KBFS2
		 * 
		 */

		KBFSStorageType defaultStorageType = KBFSStorageType.getByKey(getContentDao().findSystemParameterValueByKey("kbfs.storage.default", ServiceLocator.getService(SystemPropertiesService.class).getDefaultKBFSService()));
		n_domain.setStorageType(defaultStorageType);

		n_domain.setCabinetExternal(true);	
		n_domain.setAPIEnabled(isApi(getStringValue("api", map)));
		
		n_domain.setCreationOffsetDateTime(OffsetDateTime.now());
		n_domain.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		n_domain.setOrganization(getStringValue("organization", map));
		n_domain.setPasswordRenewMonths(0);
		n_domain.setState(ObjectState.ENABLED);
		n_domain.setExternalId(getStringValue("externalid", map));
		n_domain.setPortalLibrary((getContentDao().findSystemParameterValueByKey("portals", "yes")).equals("yes"));

		n_domain.setQuota(UNLIMITED_HD);
		n_domain.setMaxUsers(UNLIMITED_USERS);
		n_domain.setEncryptFiles(true);

		getContentDao().save(n_domain);
		txlogger.info(new DomainCreateEvent(n_domain));
		
		// -----------------
		//
		// Settings 
		// Canonical Groups
		// Cabinets
		//
		//  -----------------
		
		((DomainSettingsBuilderService) n_domain.getService(DomainSettingsBuilderService.class)).build(map);
    	
		if (logger.isDebugEnabled()) 
			listDomain(n_domain).entrySet().forEach(item-> logger.debug(item.getKey(),  item.getValue()));
		
		return n_domain;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Domain createEmptyDomain(String name, Map<String, Object> map) throws ContentMgmtException, ContentCreationException {
		
		if (!(	map.containsKey("type") 			&&
				map.containsKey("root_password") 	&&
				map.containsKey("root_email") 		&&
				map.containsKey("admin_username") 	&&
				map.containsKey("admin_lastname") 	&&
				map.containsKey("timezone") 		&&
				map.containsKey("locale") 			&&
				map.containsKey("api"))) {
			
			if (logger.isDebugEnabled())	
				map.entrySet().forEach(item -> logger.debug(item.getKey() + " " + item.getValue()));
				
			throw new ContentCreationException("Mandatory parameters missing");
		}
			
		KbeeDomain n_domain = new KbeeDomain();

		n_domain.setEncryptFiles(true);
		n_domain.setName(name);
		n_domain.setDomainType(map.get("type") == null ? DomainType.PREMIUM : (DomainType)map.get("type"));
		
		ZoneId zid = getZoneId(map);
		n_domain.setTimeZone(zid.getId());
		
		
		Locale locale = getLocale(map);
		n_domain.setLanguage(locale.getLanguage());
		n_domain.setLocale(locale);
		
		
		/**
		// where to store binary objects 
		// the main purpose of this field was
		// to allow beta testing during the transition to distributed 
		 * 
		 */

		KBFSStorageType defaultStorageType = KBFSStorageType.getByKey(getContentDao().findSystemParameterValueByKey("kbfs.storage.default", ServiceLocator.getService(SystemPropertiesService.class).getDefaultKBFSService()));
		n_domain.setStorageType(defaultStorageType);

		n_domain.setCabinetExternal(true);	
		n_domain.setAPIEnabled(isApi(getStringValue("api", map)));
		
		n_domain.setCreationOffsetDateTime(OffsetDateTime.now());
		n_domain.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		n_domain.setOrganization(getStringValue("organization", map));
		n_domain.setPasswordRenewMonths(0);
		n_domain.setState(ObjectState.ENABLED);
		n_domain.setExternalId(getStringValue("externalid", map));
		n_domain.setPortalLibrary((getContentDao().findSystemParameterValueByKey("portals", "yes")).equals("yes"));

		n_domain.setQuota(UNLIMITED_HD);
		n_domain.setMaxUsers(UNLIMITED_USERS);
		n_domain.setEncryptFiles(true);

		getContentDao().save(n_domain);
		txlogger.info(new DomainCreateEvent(n_domain));
		
		// -----------------
		// Settings 
		// Canonical Groups
		// Cabinets
		//  -----------------
		
		((DomainSettingsBuilderService) n_domain.getService(DomainSettingsBuilderService.class)).buildEmpty(map);
    	
		if (logger.isDebugEnabled()) 
			listDomain(n_domain).entrySet().forEach(item-> logger.debug(item.getKey(),  item.getValue()));
		
		return n_domain;
	}

	/**
	 *  DataSets, Classifiers, Atttibutes, Content Classes
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void setUpModelBasic(Domain domain) throws ContentMgmtException, ContentCreationException {
		domain.getService(DomainModelBuilderService.class).build();
	}

	/**
	 * Information Model
	 * 
	 * @param domain
	 * @throws ContentMgmtException
	 * @throws ContentCreationException
	 * 
	 * Requires Model Basic to exists
	 * 
	 * Roles Canonical
	 * Roles Entity
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void setUpRolesBasic(Domain domain) throws ContentMgmtException, ContentCreationException {
		domain.getService(DomainRolesCanonicalBuilderService.class).build();
		domain.getService(DomainRolesEntityBuilderService.class).build();

	}
	
	/**
	 * Requires model basic and Roles
	 * Users
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void setUpUsersBasic(Domain domain, Map<String, Object> map) throws ContentMgmtException, ContentCreationException {
		DomainUsersBuilderService service = domain.getService(DomainUsersBuilderService.class);
		service.setParameters(map);
		service.build();
	}

	/**
	 * basic 
	 * Premium-api
	 * Premium-noapi  (assign) 
	 * Premium-empty  (assign)
	 * -----------------------------------
	 * @param domain
	 * @param imodeltype
	 * @throws ContentMgmtException
	 * @throws ContentCreationException
	 */
													
	public void setUpModelPremium(Domain domain, String imodeltype) throws ContentMgmtException, ContentCreationException {
		domain.getService(DomainModelBuilderService.class).build(imodeltype);
	}
												
	public void setUpRolesPremium(Domain domain, String imodeltype) throws ContentMgmtException, ContentCreationException {
		domain.getService(DomainRolesCanonicalBuilderService.class).build(imodeltype);
	}
																					
	public void setUpUsersPremium(Domain domain, Map<String, Object> map, String imodeltype) throws ContentMgmtException, ContentCreationException {
		DomainUsersBuilderService service = domain.getService(DomainUsersBuilderService.class);
		service.setParameters(map);
		service.build(imodeltype);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void setUpExpress(Domain domain) throws ContentMgmtException, ContentCreationException {
		DomainUsersBuilderService service = domain.getService(DomainUsersBuilderService.class);
		service.setExpressUsers();
	}
	
	public ContentDao getContentDao() {	
		return dao;	
	}
	
	public void setContentDao(ContentDao dao) {	
		this.dao=dao;	
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}

	private String getStringValue( String key, Map<String, Object> map) {
		if (map.containsKey(key)) {
			if (map.get(key)!=null)
				return  map.get(key).toString();
			else
				return "";
		}
		return "key";
	}

	private boolean isApi(String key) {
		if (key.equals("yes"))
			return true;
		return false;
	}
 
	private Map<String, String> listDomain(Domain n_domain) {
		Map<String, String> list = new HashMap<String, String>();
		list.put("name", n_domain.getName());
		list.put("address", n_domain.getAddress());
		list.put("organization", n_domain.getOrganization());
		list.put("description", n_domain.getDescription());
		list.put("displayname", n_domain.getDisplayName());
		list.put("type", n_domain.getDomainType().getLabel());
		list.put("external_id", n_domain.getExternalId());
		list.put("id", n_domain.getId().toString());
		list.put("max_users", String.valueOf(n_domain.getMaxUsers()));
		list.put("quota", String.valueOf(n_domain.getQuota()));
		return list;
	}
			
	private Locale getLocale(Map<String, Object> map) {
		if (!map.containsKey("locale"))
				return Locale.getDefault();
		Locale locale = (Locale) map.get("locale");
		return locale;
	}
	
	private ZoneId getZoneId(Map<String, Object> map) {
		if (!map.containsKey("timezone"))
			return ZoneId.of(getDefaultTimeZone());
		ZoneId zid = (ZoneId) map.get("timezone");
		if (zid==null)
			zid=ZoneId.systemDefault();
		return  zid;
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
