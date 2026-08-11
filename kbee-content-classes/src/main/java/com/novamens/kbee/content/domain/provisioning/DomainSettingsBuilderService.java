package com.novamens.kbee.content.domain.provisioning;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.ResourceGroupType;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.base.Source;
import com.novamens.content.library.Library;
import com.novamens.content.model.LauncherGroup;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.Json;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.base.KbeeResourceTag;
import com.novamens.kbee.content.base.KbeeSource;
import com.novamens.kbee.content.library.KbeeLibrary;
import com.novamens.kbee.content.model.KbeeLauncherGroup;
import com.novamens.kbee.json.KbeeJson;

import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.logging.DomainUpdateEvent;
import com.novamens.portal.service.SiteFactoryService;
import com.novamens.security.acl.Group;

import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.LanguageService;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceLocator;

/**
 * com.novamens.kbee.content.domain.provisioning.DomainSettingsBuilderService
 * 
 * 
 */
public class DomainSettingsBuilderService extends BaseDomainBuilder implements ObjectService {

	/** Logger that works synchronously in the TRX thread */
	static private Logger txlogger = LogManager.getLogger("TxLogger");
								
	static private kbee.util.logging.Logger logger = new kbee.util.logging.Logger(LogManager.getLogger(DomainSettingsBuilderService.class.getName()));

	private String noReplyEmail;
	private List<Group> groups;
	
	public DomainSettingsBuilderService() {
	}
	
	public DomainSettingsBuilderService(Domain domain) {
		super(domain);
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void build(Map<String, Object> map) throws ContentMgmtException, ContentCreationException {
		
		addSettings();
		addCanonicalGroups();
		addSources();
		addLibraries();
		addResourceTags();
		addLauncherGroups();
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void buildEmpty(Map<String, Object> map) throws ContentMgmtException, ContentCreationException {
		
		addSettings();
		addCanonicalGroups();
		addSources();
		addLibraries();
		
		// 
		// addResourceTags();
		// addLauncherGroups();
	}
	
	
	private void addSettings() {
		Json json;
		json = new KbeeJson();
		json.put(DomainSettingsService.CONSOLES_PERSISTS_LABELS, "no");
		json.put(DomainSettingsService.TIP_OF_THE_DAY, "no");
		json.put(DomainSettingsService.EMAIL_SERVICE_STATUS, "enabled");
		json.put(DomainSettingsService.EMAIL_SERVICE_NO_REPLY, this.getNoReplyEmail());
		getBuildingDomain().getService(DomainSettingsService.class).SetValues(json);		
		getContentDao().save(getBuildingDomain());
		txlogger.info(new DomainUpdateEvent(getBuildingDomain(), "Settings"));
	}
	
	/**
	 * @throws ContentMgmtException
	 * @throws ContentCreationException
	 */
	private void addCanonicalGroups() throws ContentMgmtException, ContentCreationException {
		
		this.groups = new ArrayList<Group>();
		
 		addCanonicalGroup(KbeeGlobalRole.USER);
		addCanonicalGroup(KbeeGlobalRole.DOMAIN_ADMIN);
		addCanonicalGroup(KbeeGlobalRole.SUPPORT);
		addCanonicalGroup(KbeeGlobalRole.SECURITY);
		addCanonicalGroup(KbeeGlobalRole.INFORMATION_MODEL);
		addCanonicalGroup(KbeeGlobalRole.MODEL_READ);
		addCanonicalGroup(KbeeGlobalRole.SETTINGS);
		addCanonicalGroup(KbeeGlobalRole.DATASET_VALUES_WRITE);
		addCanonicalGroup(KbeeGlobalRole.DATASET_VALUES_READ);
		
		addCanonicalGroup(KbeeGlobalRole.WORKFLOW);
		addCanonicalGroup(KbeeGlobalRole.MONITOR_AUDIT);
		addCanonicalGroup(KbeeGlobalRole.ARCHIVE);
		addCanonicalGroup(KbeeGlobalRole.WORKSPACE);
		//addCanonicalGroup(KbeeGlobalRole.WORKSPACE_BULK_ACTIONS);
		addCanonicalGroup(KbeeGlobalRole.PENDING_TASKS);
		addCanonicalGroup(KbeeGlobalRole.PORTAL_ADMIN);
		addCanonicalGroup(KbeeGlobalRole.BILLBOARDS);
		addCanonicalGroup(KbeeGlobalRole.SU);
		
		addCanonicalGroup(KbeeGlobalRole.REPORTS);
		addCanonicalGroup(KbeeGlobalRole.FILE_SERVER);
		addCanonicalGroup(KbeeGlobalRole.AUDITOR);
		addCanonicalGroup(KbeeGlobalRole.FEDERATED_SECURITY);

		logger.debug("addCanonicalGroups() done");
		
	}
	
	
	public String getNoReplyEmail() {
		if (noReplyEmail==null)
			return getContentDao().findSystemParameterValueByKey("newdomain.root.noreply",  "noreply@" + getBuildingDomain().getName());
		return noReplyEmail;
	}

	public void setNoReplyEmail(String noReplyEmail) {
		this.noReplyEmail = noReplyEmail;
	}
	
	
	private void addSources() {
		StringTokenizer sourcesnames;
		if (getBuildingDomain().getDomainType()==DomainType.EXPRESS)
			sourcesnames = new StringTokenizer(getContentDao().findSystemParameterValueByKey("sources_free", ""), ":");
		else
			sourcesnames = new StringTokenizer(getContentDao().findSystemParameterValueByKey("sources_premium", ""), ":");
		while (sourcesnames.hasMoreTokens()) {
			String sourcename = sourcesnames.nextToken().trim();
			String displayname = getContentDao().findSystemParameterValueByKey("source_"+sourcename, sourcename);
			if (sourcename!=null && displayname!=null) {
				KbeeSource source = (KbeeSource)ServiceLocator.getService(ContentFactoryService.class).createSource(sourcename, displayname, getBuildingDomain());
				source.setDisplayName(displayname);
				source.setState(ObjectState.ENABLED);
				source.setDomain(getBuildingDomain());
				getContentDao().save((Source) source);
			}
			else {
				logger.debug("no data for library "+sourcename);
			}
		}
		getContentDao().flush();
		
		logger.debug("addSources() done");
	}

	private void addCanonicalGroup(KbeeGlobalRole canonical_group) {

		KbeeGroup group = (KbeeGroup)ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
		group.setDomain(getBuildingDomain());
		group.setName(canonical_group.getId());
		
		if (canonical_group.getAreaCode()==null) {
			logger.debug("error AreaCode is null");
		}
		
		group.setAreaCode(canonical_group.getAreaCode());
		group.setCanonical(true);
		group.setOnlyInternalUse(canonical_group.isInternalUseOnly());
		ServiceLocator.getService(SecurityContentMgmtService.class).update(group);
		this.groups.add(group);
}


	
	private void addResourceTags() {
	
		StringTokenizer resource_tags;
		LanguageService ls = ServiceLocator.getService(LanguageService.class);
		Locale locale  = getBuildingDomain().getLocale();
		resource_tags = new StringTokenizer(getContentDao().findSystemParameterValueByKey("resource_tags", "general:internal use"), ":");
		int n = 0;
		while (resource_tags.hasMoreTokens()) {
			String rt = resource_tags.nextToken().trim(); 
			String displayname =ls.getString(rt, locale);
			KbeeResourceTag k_rt = (KbeeResourceTag)ServiceLocator.getService(ObjectFactoryService.class).createResourceTag();
			k_rt.setAlias(displayname);
			k_rt.setCreationOffsetDateTime(OffsetDateTime.now());
			k_rt.setDefault( n++ == 0 ? true : false);
			k_rt.setDomain(getBuildingDomain());
			k_rt.setState(ObjectState.ENABLED);
			k_rt.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			k_rt.setLastModifiedUser(getSessionUser());
			k_rt.setMultiple(true);
			k_rt.setName(displayname);
			k_rt.setType(ResourceGroupType.DEFAULT);
			
			getRepository(ResourceTag.class).save(k_rt);
			
		}
		getContentDao().flush();
	}



	private void addLauncherGroups() {

		StringTokenizer l_groups;
		
		LanguageService ls = ServiceLocator.getService(LanguageService.class);
		Locale locale  = getBuildingDomain().getLocale();

		l_groups = new StringTokenizer(getContentDao().findSystemParameterValueByKey("launcher_groups", "general:admin:finance:compliance"), ":");
		
		while (l_groups.hasMoreTokens()) {
			
			String rt = l_groups.nextToken().trim(); 
			String displayname =ls.getString(rt, locale);
			
			KbeeLauncherGroup k_rt = (KbeeLauncherGroup) ServiceLocator.getService(ObjectFactoryService.class).createLauncherGroup(displayname);
			
			k_rt.setAlias(displayname);
			k_rt.setCreationOffsetDateTime(OffsetDateTime.now());
			k_rt.setDomain(getBuildingDomain());
			k_rt.setState(ObjectState.ENABLED);
			k_rt.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			k_rt.setLastModifiedUser(getSessionUser());
			k_rt.setName(displayname);
			
			getRepository(LauncherGroup.class).save(k_rt);
		}
		getContentDao().flush();
	}
	
	
	/**
	 * Creates parametrized libraries and 1 canonical library
	 * 
	 * 
	 * "corporate"
	 * "general"
	 * "all"
	 * "clients"
	 * 
	 * 
	 */
	private void addLibraries() {
		
		StringTokenizer librariesnames;
								
		LanguageService ls = ServiceLocator.getService(LanguageService.class);
		Locale locale  = getBuildingDomain().getLocale();
		
		// if 		(getBuildingDomain().getDomainType()==DomainType.EXPRESS)		librariesnames = new StringTokenizer(getContentDao().findSystemParameterValueByKey("libraries_free", "corporate:clients:general"), ":");
		
		
		if 		(getBuildingDomain().getDomainType()==DomainType.EXPRESS)		librariesnames = new StringTokenizer(getContentDao().findSystemParameterValueByKey("libraries_free", ""), ":");
		else if (getBuildingDomain().getDomainType()==DomainType.PREMIUM)		librariesnames = new StringTokenizer(getContentDao().findSystemParameterValueByKey("libraries_premium", "corporate:clients:general"), ":");
		else if (getBuildingDomain().getDomainType()==DomainType.COMPLIANCE) 	librariesnames = new StringTokenizer(getContentDao().findSystemParameterValueByKey("libraries_compliance",  "corporate:clients:general"), ":");
		else				
			librariesnames = new StringTokenizer(getContentDao().findSystemParameterValueByKey("libraries", "corporate"), ":");
		
		while (librariesnames.hasMoreTokens()) {
			
			String libraryname = librariesnames.nextToken().trim(); 
			String displayname = ls.getString(libraryname, locale);
			
			String criteria = getContentDao().findSystemParameterValueByKey("library_"+libraryname+".criteria", getBuildingDomain().getDomainType()==DomainType.EXPRESS?"ishead(true)":"ishead(true)");
			String readonly = getContentDao().findSystemParameterValueByKey("library_"+libraryname+".readonly", getBuildingDomain().getDomainType()==DomainType.EXPRESS ?"false":"false");

			if (displayname!=null && criteria!=null) {
				KbeeLibrary library = (KbeeLibrary)ServiceLocator.getService(ContentFactoryService.class).createLibrary(libraryname, getBuildingDomain());
				library.setDisplayName(displayname);
				library.setCriteria(criteria);
				library.setState(ObjectState.ENABLED);
				library.setReadOnly("true".equals(readonly));
				library.setDomain(getBuildingDomain());
				getContentDao().save((Library) library);
			}
			else {
				logger.debug("no data for library "+libraryname);
			}
		}

		

//		KbeeLibrary all_lib = (KbeeLibrary)ServiceLocator.getService(ContentFactoryService.class).createLibrary("all", getBuildingDomain());  
//		
//		//ls.getString("all", locale)
//		all_lib.setDisplayName( getBuildingDomain().getOrganization() );
//		all_lib.setReadOnly(false);
//		all_lib.setState(ObjectState.ENABLED);
//		all_lib.setCanonical(true);
//		all_lib.setOrder(99);
//		all_lib.setDomain(getBuildingDomain());
//		getContentDao().save((Library) all_lib);
//		getContentDao().flush();
//		
//		/**
//		 * We only create 1 portal for the ALL library
//		 */
//		ServiceLocator.getService(SiteFactoryService.class).createLibrarySite(all_lib);
//		

		
	}
}
