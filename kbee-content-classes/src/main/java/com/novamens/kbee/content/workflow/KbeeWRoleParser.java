package com.novamens.kbee.content.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novamens.dom.Json;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.workflow.RoleInProcess;

public class KbeeWRoleParser extends RoleParser {
	
	//static Logger logger = LogManager.getLogger(KbeeRoleParser.class.getName());

	@SuppressWarnings("unchecked")
	public Json getJson(List<RoleInProcess> roles) {
		KbeeJson json = new KbeeJson();
		if (roles.isEmpty())
			return null;
		List<Map<String, String>> jsonroles  = new ArrayList<Map<String, String>>();
		for (RoleInProcess role : roles) {
			jsonroles.add(getMap(role));
		}
		json.put("roles", jsonroles);
		return json;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<RoleInProcess> getRoles(Json json) {
		List<RoleInProcess> roles = new ArrayList<RoleInProcess>(); 
		List<Map> rolesmaps = (List<Map>)json.get("roles");
		for (Map rolemap : rolesmaps) {	
			RoleInProcess role = getRole(rolemap);
			roles.add(role);
		}
		return roles;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map getMap(RoleInProcess role) {
		Map map = new HashMap();
		map.put("name", role.getName());
		map.put("label", role.getLabel());
		return map;
	}
	
	@SuppressWarnings("rawtypes")
	private RoleInProcess getRole(Map map) {
		KbeeWRole role = new KbeeWRole();
		role.setName((String)map.get("name"));
		role.setLabel((String)map.get("label"));
		return role;
	}
}
