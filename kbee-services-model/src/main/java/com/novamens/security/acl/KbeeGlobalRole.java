package com.novamens.security.acl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 *  @see ContentSecurtityDao
 *  
 *  getCanonicalGroups
 *  List<Group> canonical_groups = getContentSecurityDao().getCanonicalGroups(getBuildingDomain());
 *  
 */

public enum KbeeGlobalRole {
	
	USER 					(0, "user", 				"internal",			"All users belong to this group", false, false, true),
	WORKFLOW 				(7, "workflow", 			"internal",			"Used internally by the Workflow engine", false, false, true),
	PORTAL_ADMIN 			(17,"portal-admin", 		"admin",			"Admin Sites", false, true, false), 									// 19
	DOMAIN_ADMIN 			(1, "domain-admin", 		"admin", 			"System Administrator (can do everything except edit user root)"), 		// 2
	SUPPORT 				(2, "support",				"admin",			"RP Support, can not open binary attachments", false, false, true), 						// 3
	SECURITY 				(3, "security", 			"admin", 			"Manage Users, Groups, Rules (can not grant Domain Admin permission)"), // 4
	EXTERNAL_USER			(90, "external-user", 		"admin", 			"External User"), // 4
	INFORMATION_MODEL		(4, "information-model", 	"settings",			"Manage DataSets, Attributes, Classifiers, Content Classses"), 			// 5
	MODEL_READ				(15,"model-read",			"settings",			"Read access to DataSets, Attributes, Classifiers, Content Classses"), 	// 15
	DATASET_VALUES_WRITE	(5, "dataset-values", 		"settings", 		"Manage DataSet Values"), 												// 6
	DATASET_VALUES_READ 	(6, "dataset-values-read",	"settings",			"DataSet Values Section (Read)"), 										// 7
	SETTINGS				(16,"settings",				"settings",			"Access to general settings, templates, etc"), 							// 16
	MONITOR_AUDIT			(8, "monitor", 				"workflow", 		"Access to Monitor to Read files that the user has Read Permission"),
	DASHBOARD				(40, "dashboard",			"dashboard", 		"Access to Dashboard"),
	ARCHIVE 				(9, "archive", 				"content",			"Access to Archive"),
	WORKSPACE_BULK_ACTIONS	(10, "mytasks-bulk-actions","workflow",			"Access to My Tasks - Bulk Actions"),
	
	//WORKSPACE_MY_RESOURCES	(27, "mybox",				"workflow",			"Access to My Resources"),
	
	
	PENDING_TASKS 			(12, "pending-tasks", 		"workflow",			"Access to Pending Tasks"), 					// 14
	WORKSPACE 				(13, "mytasks", 			"workflow",			"Access to My Tasks"), 							// 15
	CORPORATE_ADMIN			(18, "corporate-admin",		"admin",			"Corporate Admin"), 							// no se si se usa 20
	BILLBOARDS				(19, "work-notes" ,			"content",			"Work Notes"),  								// temp 21
	NOTIFICATIONS			(28, "notifications" ,		"workflow",			"Notifications"),  								// 
	SU						(20, "su" ,					"admin",			"Super User"),  								// 22
	REPORTS 				(25, "reports", 			"content",			"Access to Reports"), 							// 25
	FILE_SERVER				(26, "file-server", 		"admin",			"File Server"), 								//
	AUDITOR					(27, "auditor", 			"admin",			"Access to Audit"), 								//
	
	
	
	
	// Factory GlobalRoles
	//
	DOMAIN_FACTORY_MANAGER 	(21, "domain-factory-manager",	"kbee-admin",  "Domain Factory Manager", 	true),    			// 21. CRUD Domains, API Logs, SI (Stefanie)
	API_DEVELOPER			(22, "api-developer" ,		  	"kbee-admin",  "API Developer", 			true),  			// 22. Check API Logs, use API, SI (Anand, Ganesan)
	SERVICE_ADMIN			(23, "service-management" ,		"kbee-admin",  "Service Management", 		true), 	 			// 23. Data Management [SQL and Critical Commands], CRUD Domains, SI (AT)
	OPERATIONS_ENGINEER		(24, "operations-engineer" ,  	"kbee-admin",  "Operations Engineer", 		true),  			// 24. Operations Engineer (Cuong)
	
	
	SUPPORT_AGENT			(25, "support-agent" ,		  	"kbee-admin",  "Support Agent", 			true),  			// 22. Check API Logs, use API, SI (Anand, Ganesan)
	
	RECYCLE_BIN 			(50, "recycle-bin", 			"content",		"Access to Recycle Bin"),
								
	FEDERATED_SECURITY		(60, "federated-security", 		"admin",		"Federated Security"),
	
	FEDERATED_VALUES		(70, "federated-values", 		"admin",		"Federated Values");


	static private final List<KbeeGlobalRole> roles = new ArrayList<KbeeGlobalRole>();
	static private final Map<String, KbeeGlobalRole> map = new HashMap<String, KbeeGlobalRole>();
						
	static {
					
		roles.add(USER);					
		roles.add(WORKFLOW);
		roles.add(PORTAL_ADMIN);
		roles.add(DOMAIN_ADMIN);
		roles.add(SUPPORT);
		roles.add(SECURITY);
		roles.add(EXTERNAL_USER);
		roles.add(INFORMATION_MODEL);		
		roles.add(MODEL_READ);		
		roles.add(SETTINGS);		
		roles.add(DATASET_VALUES_WRITE);
		roles.add(DATASET_VALUES_READ);
		 							
		roles.add(MONITOR_AUDIT); 	
		roles.add(DASHBOARD);
		
		roles.add(ARCHIVE); 													
		//roles.add(WORKSPACE_BULK_ACTIONS);
		// roles.add(WORKSPACE_MY_RESOURCES);
		roles.add(PENDING_TASKS);
		roles.add(WORKSPACE);
		roles.add(CORPORATE_ADMIN);
		roles.add(BILLBOARDS);
		roles.add(NOTIFICATIONS);
		roles.add(SU);
		roles.add(REPORTS);
		roles.add(FILE_SERVER);
		roles.add(AUDITOR);
		
		roles.add(DOMAIN_FACTORY_MANAGER);
		roles.add(API_DEVELOPER);
		roles.add(SERVICE_ADMIN);				
		roles.add(OPERATIONS_ENGINEER);
		
		roles.add(RECYCLE_BIN);
		roles.add(SUPPORT_AGENT);
		
		
		map.put(USER.getId()								, KbeeGlobalRole.USER);
		map.put(WORKFLOW.getId()							, KbeeGlobalRole.WORKFLOW);
		
		map.put(PORTAL_ADMIN.getId()						, KbeeGlobalRole.PORTAL_ADMIN);
		map.put(DOMAIN_ADMIN.getId()						, KbeeGlobalRole.DOMAIN_ADMIN);
		
		map.put(SUPPORT.getId()								, KbeeGlobalRole.SUPPORT);
		map.put(SECURITY.getId()							, KbeeGlobalRole.SECURITY);
		map.put(EXTERNAL_USER.getId()						, KbeeGlobalRole.EXTERNAL_USER);
		map.put(INFORMATION_MODEL.getId()					, KbeeGlobalRole.INFORMATION_MODEL);
		map.put(MODEL_READ.getId()							, KbeeGlobalRole.MODEL_READ);
		map.put(SETTINGS.getId()							, KbeeGlobalRole.SETTINGS);
		map.put(DATASET_VALUES_WRITE.getId()				, KbeeGlobalRole.DATASET_VALUES_WRITE);
		map.put(DATASET_VALUES_READ.getId()					, KbeeGlobalRole.DATASET_VALUES_READ);
		
		map.put(FEDERATED_SECURITY.getId()					, KbeeGlobalRole.FEDERATED_SECURITY);
		map.put(FEDERATED_VALUES.getId()					, KbeeGlobalRole.FEDERATED_VALUES);
		 							
		map.put(MONITOR_AUDIT.getId()						, KbeeGlobalRole.MONITOR_AUDIT); 	
		map.put(DASHBOARD.getId()							, KbeeGlobalRole.DASHBOARD);
		
		map.put(ARCHIVE.getId()								, KbeeGlobalRole.ARCHIVE);
		//map.put(WORKSPACE_BULK_ACTIONS.getId()				, KbeeGlobalRole.WORKSPACE_BULK_ACTIONS);
		// map.put(WORKSPACE_MY_RESOURCES.getId()   					, KbeeGlobalRole.WORKSPACE_MY_RESOURCES);
		map.put(PENDING_TASKS.getId()						, KbeeGlobalRole.PENDING_TASKS);
		map.put(WORKSPACE.getId()							, KbeeGlobalRole.WORKSPACE);
		
		map.put(CORPORATE_ADMIN.getId()						, KbeeGlobalRole.CORPORATE_ADMIN);
		map.put(BILLBOARDS.getId()							, KbeeGlobalRole.BILLBOARDS);
		map.put(NOTIFICATIONS.getId()						, KbeeGlobalRole.NOTIFICATIONS);
		map.put(SU.getId()									, KbeeGlobalRole.SU);
		map.put(REPORTS.getId()								, KbeeGlobalRole.REPORTS);
		map.put(FILE_SERVER.getId()							, KbeeGlobalRole.FILE_SERVER);
		map.put(AUDITOR.getId()								, KbeeGlobalRole.AUDITOR);

		
		map.put(DOMAIN_FACTORY_MANAGER.getId()				, KbeeGlobalRole.DOMAIN_FACTORY_MANAGER);
		map.put(API_DEVELOPER.getId()						, KbeeGlobalRole.API_DEVELOPER);
		map.put(SERVICE_ADMIN.getId()						, KbeeGlobalRole.SERVICE_ADMIN);
		map.put(OPERATIONS_ENGINEER.getId()					, KbeeGlobalRole.OPERATIONS_ENGINEER);
		map.put(RECYCLE_BIN.getId()							, KbeeGlobalRole.RECYCLE_BIN);
		
		map.put(SUPPORT_AGENT.getId()						, KbeeGlobalRole.SUPPORT_AGENT);
		
	}
	
	private final String key;
	private final String description;
 	private boolean factory = false; 			//  only for Domain kbee
	private boolean is_portal = false; 			// only Domain with portal enabled
	private boolean is_internal_use = false; 	// only for internal use
	private final int id;
	private final String area;

	static public final List<KbeeGlobalRole> getRoles() {
		return roles;
	}
	
	static public final KbeeGlobalRole getGlobalRoleByKey(String key) {
		return map.get(key);
	}
	
	private KbeeGlobalRole(int id, String label,  String area, String description) {
			this(id, label, area, description, false);
	}
												
	private KbeeGlobalRole(int id, String label, String area, String description, boolean factory) {
		this.id=id;
		this.key = label;
		this.description = description;
		this.factory=factory;
		this.area=area;
	}
	
 	/**
 	 *  
 	 */																								
 	private KbeeGlobalRole(int id, String label, String area, String description, boolean isfactoryonly, boolean isportalonly, boolean isalluse) {
 		this.id=id;
 		this.key = label;
		this.description = description;
		this.factory=isfactoryonly;
		this.is_portal=isportalonly;
		this.is_internal_use=isalluse;
		this.area=area;
	}
 	
	public String toString() {
		return ("id: " + getId() + "  label: "+ getLabel() + " | area: " + getArea() + " | description: " + getDescription());
	}

	
	public int getInternalId() {
		return this.id;
	}
	
	/**
	 * If this GlobalRole is used only internally (should not be included in selectors) 
	 */
	public boolean isInternalUseOnly() {
		return this.is_internal_use;
	}
	
	/**
	 * If this GlobalRole is used only be Kbee Domain 
	 */
	public boolean isFactory() {
		return this.factory;
	}
	
	/**
	 * Whether this GlobalRole is used only be Domains with Portal enabled 
	 * 
	 */
	public boolean isPortal() {
		return this.is_portal;
	}


	
	public String getAreaCode() {
		return this.area;
	}
	
	public String getArea() {
		ResourceBundle res = ResourceBundle.getBundle(KbeeGlobalRole.class.getName(), Locale.getDefault());
		if (res.getString(this.area)!=null)
			return res.getString(this.area);
		return this.area;
	}

	
	public String getArea(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(KbeeGlobalRole.class.getName(), locale);
		if (res.getString(this.area)!=null)
			return res.getString(this.area);
		return this.area;
	}
	
	
	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(KbeeGlobalRole.class.getName(), locale);
		if (res.getString(this.key)!=null)
			return res.getString(this.key);

		return this.key;
	}
	
	public String getLabel() {
		ResourceBundle res = ResourceBundle.getBundle(KbeeGlobalRole.class.getName(), Locale.getDefault());
		if (res.getString(this.key)!=null)
			return res.getString(this.key);
		return this.key;
	}
	
	public String getSelectorLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(KbeeGlobalRole.class.getName(), locale);
		if (res.getString("selector-"+this.key)!=null)
			return res.getString("selector-"+this.key);
		return "selector-"+this.key;
	}
	
	public String getSelectorLabel() {
		ResourceBundle res = ResourceBundle.getBundle(KbeeGlobalRole.class.getName(), Locale.getDefault());
		if (res.getString("selector-"+this.key)!=null)
			return res.getString("selector-"+this.key);
		return "selector-"+this.key;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getId() {
		return key;
	}
}