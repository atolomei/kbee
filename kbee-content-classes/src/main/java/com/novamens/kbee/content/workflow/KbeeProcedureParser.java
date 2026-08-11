package com.novamens.kbee.content.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novamens.dom.Json;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Task;

public class KbeeProcedureParser extends ProcedureParser {
	
	//static Logger logger = LogManager.getLogger(KbeePhaseParser.class.getName());

	@SuppressWarnings({ "rawtypes" })
	public Json getJson(List<Procedure> procedures) {
		KbeeJson json = new KbeeJson();
		if (procedures.isEmpty())
			return null;
		List<Map> jsonprocedures  = new ArrayList<>();
		for (Procedure procedure : procedures) {
			jsonprocedures.add(getMap(procedure));
		}
		json.put("procedures", jsonprocedures);
		return json;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Procedure> getProcedures(Json json, Procedure parent) {
		List<Procedure> procedures = new ArrayList<>();
		if (json==null) return procedures;
		List<Map> proceduresmaps = (List<Map>)json.get("procedures");
		if (proceduresmaps==null) return procedures;
		for (Map map : proceduresmaps) {	
			Procedure procedure = getProcedure(map, parent);
			procedures.add(procedure);
		}
		return procedures;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map getMap(Procedure procedure) {
		Map map = new HashMap();
		if (procedure.getId()!=null)
		map.put("id", String.valueOf(procedure.getId()));
		map.put("name", procedure.getName());
		if (procedure.getTasks()!=null && !procedure.getTasks().isEmpty()) {
			Json jsontasks = TaskParser.Get().getJson(procedure.getTasks());
			map.put("tasks", ((KbeeJson)jsontasks).getData().get("tasks"));
		}
		return map;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Procedure getProcedure(Map map, Procedure parent) {
		Subprocedure procedure = new Subprocedure();
		if (map.get("id")!=null)
			procedure.setId(Long.valueOf((String)map.get("id")));
		procedure.setName((String)map.get("name"));
		procedure.setProcedure(parent);
		
		Map tasksmap = new HashMap();
		tasksmap.put("tasks",map.get("tasks"));
		if (tasksmap!=null) {
			Json jsontasks = new KbeeJson(tasksmap);
			List<Task> tasks = TaskParser.Get().getTasks(jsontasks, procedure);
			procedure.setTasks(tasks);
		}
		return procedure;
	}
}