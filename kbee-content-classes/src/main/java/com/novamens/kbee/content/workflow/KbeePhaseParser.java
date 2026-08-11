package com.novamens.kbee.content.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novamens.dom.Json;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.workflow.ProcedurePhase;

public class KbeePhaseParser extends PhaseParser {
	
	//static Logger logger = LogManager.getLogger(KbeePhaseParser.class.getName());

	@SuppressWarnings("unchecked")
	public Json getJson(List<ProcedurePhase> phases) {
		KbeeJson json = new KbeeJson();
		if (phases.isEmpty())
			return null;
		List<Map<String, String>> jsonphases  = new ArrayList<Map<String, String>>();
		for (ProcedurePhase phase : phases) {
			jsonphases.add(getMap(phase));
		}
		json.put("phases", jsonphases);
		return json;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<ProcedurePhase> getPhases(Json json) {
		List<ProcedurePhase> phases = new ArrayList<ProcedurePhase>();
		if (json==null) return phases;
		List<Map> phasesmaps = (List<Map>)json.get("phases");
		if (phasesmaps==null) return phases;
		for (Map phasemap : phasesmaps) {	
			ProcedurePhase phase = getPhase(phasemap);
			phases.add(phase);
		}
		return phases;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map getMap(ProcedurePhase phase) {
		Map map = new HashMap();
		
		map.put("name", phase.getName()!=null?phase.getName():"null");
		map.put("icon", phase.getIcon()!=null?phase.getIcon():"");
		map.put("label", phase.getLabel()!=null?phase.getLabel():"null");
		
		return map;
	}
	
	@SuppressWarnings("rawtypes")
	private ProcedurePhase getPhase(Map map) {
		KbeeProcedurePhase phase = new KbeeProcedurePhase();
		phase.setName((String)map.get("name"));
		phase.setIcon((String)map.get("icon"));
		phase.setLabel((String)map.get("label"));
		return phase;
	}
}