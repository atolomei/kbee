package com.novamens.logging;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.base.Content;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Activity;

@Entity
@DiscriminatorValue("TaskEndEvent")
public class TaskEndEvent extends WorkflowEvent {
	
	static public String getClassEventType() {
		return "Task End";
	}

	public TaskEndEvent() {
	}
	
	public TaskEndEvent(Content content, Activity activity, String condition) {
		this(content, activity, condition, false);
	}
	
	public TaskEndEvent(Content content, Activity activity, String condition, boolean forced) {
		super(content);
		
		KbeeJson json = new KbeeJson();
		
		if (activity.getId()!=null)
			setActivityId(activity.getId());

		
		String task = activity.getTask().getName();
		String procedure = activity.getProcess().getProcedure().getCode();
		User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
		String note = activity.getNote();
		String resolution = activity.getResolution();
		String resolutiontitle = activity.getResolutionTitle();
		
		json.put("task", task);
		json.put("procedure", procedure);
		json.put("condition", condition);
		
		if (forced)
			json.put("forced", "true");
			
		if (note!=null)
			if (!forced)
				json.put("note", "Note: " + escape(note));
			else
				json.put("note", "Forced Termination by "+user.getDisplayName()+". Note: " + escape(note));
		
		if (resolution!=null)
			json.put("resolution", escape(resolution));
		
		if (resolutiontitle!=null)
			json.put("resolution-title", escape(resolutiontitle));
		
		setJson(json);
		setEventUser(user);
		setTask(null);
		setTask(task);
	}
	
	@Override
	public String getEventType() {
		return getClassEventType(); 
	}
	
	@Override
	public String getDescription() {
		KbeeJson json = getJson();
		String description = unescape((String)json.get("note"));
		return description;
	}
	
	@Override
	public String getCondition() {
		KbeeJson json = getJson();
		String c = unescape((String)json.get("condition"));
		return c;
	}

	public String getResolutionTitle() {
		KbeeJson json = getJson();
		String description = unescape((String)json.get("resolution-title"));
		return description!=null?description:"Task Resolution Letter";
	}
	
	public String getResolution() {
		String parameters = super.getParameters();
		String resolution = null;     
		if (parameters.startsWith("[")) {
			KbeeJson json = getJson();
			resolution = unescape((String)json.get("resolution"));
		}
		return resolution;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(getAction());
		if (str.length()>0) str.append(" | "); 
		str.append(getTarget());
		 if (getEventUser()!=null) {
			 if (str.length()>0) str.append(" | ");
			 str.append(getEventUser().getDisplayName());
		 }
		 else
			 str.append("Event User is null");
		return str.toString();
	}
	
	@Override
	public String getAction() {
		String label;
		if (!"true".equals((String)getJson().get("forced"))) 
			label = "[End] ";
		else
			label = "[End. Forced Termination] ";
		label += (String)getJson().get("procedure") + " " +(String)getJson().get("task") + " > " + getJson().get("condition");
		return label;
	}
	
	@Override
	public boolean isNotifiable() {
		return false;
	}
}
