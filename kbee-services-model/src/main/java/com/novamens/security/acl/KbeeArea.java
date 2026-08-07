package com.novamens.security.acl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum KbeeArea {
	
	ADMIN			("admin", "Administration"),
	CONTENT			("content", "Content"),
	SETTINGS		("settings", "Settings"),
	SECURITY		("security", "Security"),
	WORKFLOW		("workflow", "Workflow"),
	FACTORY			("kbee-admin", "Factory"),
	REPORTS			("kbee-reports", "Reports");
	


	
	static private final List<KbeeArea> areas = new ArrayList<KbeeArea>();
	static private final Map<String, KbeeArea> map = new HashMap<String, KbeeArea>();
						
	static {
		areas.add(CONTENT);					
		areas.add(WORKFLOW);
		areas.add(ADMIN);
		areas.add(SETTINGS);
		areas.add(FACTORY);
		
		map.put(CONTENT.getCode(), CONTENT);
		map.put(WORKFLOW.getCode(), WORKFLOW);
		map.put(ADMIN.getCode(), ADMIN);
		map.put(SECURITY.getCode(), SECURITY);
		map.put(SETTINGS.getCode(), SETTINGS);
		map.put(FACTORY.getCode(), FACTORY);
		map.put(REPORTS.getCode(), REPORTS);
	}
	
	private final String code;
	private final String name;

	
	static public final KbeeArea getAreaByCode(String code) {
		return map.get(code);
	}
	
	private KbeeArea(String code, String name) {
		this.code = code;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getCode() {
		return code;
	}
}