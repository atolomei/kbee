package com.novamens.logging;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.workflow.WorkflowDao;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Activity;

@Entity
@DiscriminatorValue("DueDateAlertEvent")
public class DueDateAlertEvent extends WorkflowEvent {
	
	static public String getClassEventType() {
		return "Due Date Alert";
	}

	public DueDateAlertEvent() {
	}
	
	public DueDateAlertEvent(Content content, Activity activity, Integer days) {
		super(content);
		
		KbeeJson json = new KbeeJson();
		
		if (activity.getId()!=null)
			setActivityId(activity.getId());
		
		String task = activity.getTask().getName();
		String procedure = activity.getProcess().getProcedure().getCode();
		User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
		
		json.put("task", task);
		json.put("procedure", procedure);
		json.put("days", String.valueOf(days));
		if (activity.getDueDate()!=null)
		json.put("duedate", activity.getDueDate().toString());
		
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
		label = "[Due Date Alert] ";
		label += (String)getJson().get("procedure") + " " +(String)getJson().get("task") + " > " + getJson().get("days");
		return label;
	}
	
	@Override
	public boolean isNotifiable() {
		return true;
	}
	
	public Activity getActivity() {
		
		Activity activity = getWorkflowDao().findActivityById((long)getActivityId());
		return activity;

	}
	
//	private String getTimeString(OffsetDateTime time) {
//		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");
//		String timeString = fmt.format(time);
//		return timeString;
//	}
}