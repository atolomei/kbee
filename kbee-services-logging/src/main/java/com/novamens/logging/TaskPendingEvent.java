package com.novamens.logging;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.base.Content;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Activity;

@DiscriminatorValue("TaskPendingEvent")
@Entity
public class TaskPendingEvent extends WorkflowEvent {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TaskPendingEvent.class.getName());
	
	static public String getClassEventType() {
		return "Task Pending";
	}

	public TaskPendingEvent() {
	
	}
	
	/**
	 * @param content Content that is part of the workflow
	 * @param activity Task that ended, after which the content went to Pending
	 * @param condition The Action executed that caused the activity to end
	 */
	public TaskPendingEvent(Content content, Activity activity, String condition) {
		super(content);
		
		KbeeJson json = new KbeeJson();
		
		if (activity.getId()!=null)
			setActivityId(activity.getId());

		
		String task = activity.getTask().getName(); 									// task that terminated
		String procedure = activity.getProcess().getProcedure().getCode();
		
		User user = ServiceLocator.getService(SecurityService.class).getSessionUser();  // User that terminated the task

		String sender_note = activity.getNote();				// Note from the user that terminated the task
		String resolution = activity.getResolution();	    	//  
		
		json.put("task", "n/a");  							// pending task 
		json.put("previousTask", task);		 				// previous task

		json.put("procedure", procedure!=null?procedure.trim():null);
		json.put("condition", condition);					// how the previous task ended
		
		if (resolution!=null)
			json.put("resolution", escape(resolution));
		
		// inpunote is the note from the user that completed the previous task 
		if (sender_note!=null) {
			json.put("inputNote", escape(sender_note));
		}	

		setJson(json);
		setEventUser(user);
		setTask(null);
		setTask(task);
		setProcedure(procedure);
	}
	
	@Override
	public String getEventType() {
   		return getClassEventType(); 
	}
	
	@Override
	public String getCondition() {
		KbeeJson json = getJson();
		String c = unescape((String)json.get("condition"));
		return c;
	}
	
	@Override
	public String getDescription() {
		KbeeJson json = getJson();
		String description = unescape((String)json.get("note"));
		return description;
	}
	
	public Activity getActivity() {
		Activity activity = getWorkflowDao().findActivityById((Long)getActivityId());
		return activity;
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
	
	/**
	 * @return usuario que realizo la tarea anterior 
	 */
	public User getTriggerUser() {
		try {
			KbeeJson json = getJson();
			String userid = (String)json.get("triggerUser");
			if (userid!=null) {
				User user = (ServiceLocator.getService(SecurityService.class).findUserById(Long.valueOf(userid)));
				return user;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}
	
	/** -------------------------------------------------------------------------------------------
	 */
	public String getInputNote() {
		KbeeJson json = getJson();
		String note = (String)json.get("inputNote");
		return note;
	}

	/** -------------------------------------------------------------------------------------------
	 */
	public String getPreviousTask() {
		KbeeJson json = getJson();
		String note = (String)json.get("previousTask");
		return note;
	}

	/** -------------------------------------------------------------------------------------------
	 */
	@Override
	public String getAction() {
		return "[Pending] "+  (String)getJson().get("procedure")  +" " + (String)getJson().get("task");
	}
}
