package com.novamens.logging;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.base.Content;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Activity;


/**
 * <p>The @link Activity} is the one done by the former user before the {@link Task} was reassigned.</p>
 */
@Entity
@DiscriminatorValue("TaskReassignedFormerOwnerEvent")
public class TaskReassignedFormerOwnerEvent extends WorkflowEvent {
				
	static public String getClassEventType() {
		return "Task Reassigned";
	}
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TaskReassignedFormerOwnerEvent.class.getName());
	
	
	public TaskReassignedFormerOwnerEvent() {
	}
	
	public TaskReassignedFormerOwnerEvent(Content content, Activity activity) {
		this (content, activity, null, null);
	}
	
	
	public TaskReassignedFormerOwnerEvent(	Content content, 
											Activity activity_terminated_by_reassign, 
											String inputnote, 
											User reassignedby) {
		super(content);
		
		KbeeJson json = new KbeeJson();

		if (activity_terminated_by_reassign.getId()!=null)
			setActivityId(activity_terminated_by_reassign.getId());
		
		String task = activity_terminated_by_reassign.getTask().getName();
		String procedure = activity_terminated_by_reassign.getProcess().getProcedure().getCode();
		
		User activityUser = activity_terminated_by_reassign.getUser();
		User triggerUser = (ServiceLocator.getService(SecurityService.class).getSessionUser());
		
		
		json.put("task", task);
		json.put("procedure", procedure);
		
		// note is the note from the user that reassigned the task
		//		
		if (activity_terminated_by_reassign.getAssignedBy()!=null && activity_terminated_by_reassign.getNote()!=null) { 
			json.put("note", escape(activity_terminated_by_reassign.getNote()));
		}
		
		if (reassignedby!=null) {
			json.put("reassignedBy", reassignedby.getId().toString());
		}

		// input_note is the note from the user that completed the previous task 
		//
		if (inputnote!=null) {
			json.put("inputNote", escape(inputnote));
		}	
		
		json.put("triggerUser", String.valueOf(triggerUser.getId()));
		
		
		setJson(json);
		
		
		// EventUser -> "former owner"
		setEventUser(activityUser);
		setTask(task);
		setProcedure(procedure);
		
		
		logger.debug( this.toString());
	}

	
	public User getFormerOwner() {
		return getEventUser();
	}
	
	@Override
	public String toString() {
	
		StringBuilder str = new StringBuilder();
		
		str.append(getAction()+" | ");
		
		if (getTriggerUser()!=null) 
			str.append(getTriggerUser().getDisplayName()+ " | ");
		
		if (getTask()!=null)
				str.append(getTask());
		
		if (str.length()>0) 
			str.append(" | "); 
			
		str.append(getTarget());
		
		if (getEventUser()!=null) {
			
			if (str.length()>0) 
				str.append(" | ");
			
			str.append(getEventUser().getDisplayName());
			
		 }
		 else
			 str.append("Event User is null");
		
		return str.toString();
	}
	
	@Override
	public String getEventType() {
		return getClassEventType(); 
	}
	
	/**
	 * @return usuario que dispara el trigger
	 */
	public User getTriggerUser() {
		KbeeJson json = getJson();
		String userid = (String)json.get("triggerUser");
		User user = (ServiceLocator.getService(SecurityService.class).findUserById(Long.valueOf(userid)));
		return user;
	}
	
	public String getNote() {
		KbeeJson json = getJson();
		String note = (String)json.get("note");
		return note;
	}
	
	public User getReassignedBy() {
		KbeeJson json = getJson();
		String userid = (String)json.get("reassignedBy");
		if (userid!=null && userid.length()>0)
			return (ServiceLocator.getService(SecurityService.class).findUserById(Long.valueOf(userid)));
		return null;
	}
	
	@Override
	public String getDescription() {
	
		StringBuilder str = new StringBuilder();
		
		if (getReassignedBy()!=null) {
			str.append("Reassigned by " + getReassignedBy().getFirstLastName());
		}
		
		if (getNote()!=null) {
			str.append(".\n"+ getNote());
		}
		return str.toString();
	}
	
	public String getInputNote() {
		KbeeJson json = getJson();
		String note = (String)json.get("inputNote");
		return note;
	}
	
	public Activity getActivity() {
		Activity activity = getWorkflowDao().findActivityById((Long)getActivityId());
		return activity;
	}
	
	@Override
	public String getAction() {
		KbeeJson json = getJson();
		boolean restart = "true".equals(json.get("restart"));
		return (restart ? "[Restart] " : "[Start] ") +  (String)getJson().get("procedure")  +" " + (String)getJson().get("task");
	}


}
