package com.novamens.logging;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.base.Content;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Activity;

@Entity
@DiscriminatorValue("TaskStartEvent")
public class TaskStartEvent extends WorkflowEvent {

	//@PersistenceContext
	//EntityManager entityManager;
 
	static public String getClassEventType() {
		return "Task Start";
	}
	
	public TaskStartEvent() {
	}
	
	public TaskStartEvent(Content content, Activity activity) {
		this (content, activity, null, null);
	}
	 
	
	public TaskStartEvent(Content content, Activity activity, String inputnote, User reassignedby) {
		this(content, activity, inputnote, reassignedby, false);
	}
	
	public TaskStartEvent(Content content, Activity activity, String inputnote, User reassignedby, boolean restart) {
		super(content);
		
		KbeeJson json = new KbeeJson();
		

		if (activity.getId()!=null)
			setActivityId(activity.getId());
		else 
			setActivityId(null);
		
		String task = activity.getTask().getName();
		String procedure = activity.getProcess().getProcedure().getCode();
		User activityUser = activity.getUser();
		User triggerUser = (ServiceLocator.getService(SecurityService.class).getSessionUser());
		
		json.put("task", task);
		json.put("procedure", procedure);
		json.put("process", String.valueOf(activity.getProcess().getId()));
		json.put("startTime", getTimeString(activity.getStartTime()));
		
		// note is the note from the user that reassigned the task
		//		
		if (activity.getAssignedBy()!=null) { 
			json.put("note", escape(activity.getNote()));
		}
		
		if (reassignedby!=null) {
			json.put("reassignedBy", reassignedby.getId().toString());
		}

		// inpunote is the note from the user that completed the previous task 
		//
		if (inputnote!=null) {
			json.put("inputNote", escape(inputnote));
		}	
		
		json.put("triggerUser", String.valueOf(triggerUser.getId()));
		
		if (restart)
		json.put("restart", "true");
		
	
		setJson(json);
		
		setEventUser(activityUser);
		setTask(task);
		setProcedure(procedure);
		
//
//		ServiceLocator.getService(EventService.class).addListener(new EventListener() {
//			@Override
//			public void onEvent(Event event) {
//				if (event.getObject().equals(activity)) {
//					setActivityId(activity.getId());
//				}
//			}
//			@Override
//			public boolean listen(Event event) {
//				return event instanceof AppCreateEvent &&
//				((AppCreateEvent)event).getObject() instanceof Activity &&
//				event.getObject().equals(activity);
//			}
//		});
//		
	}

	
	
	@Override
	public String toString() {
	
		StringBuilder str = new StringBuilder();
		
		str.append(getAction());
		
		if (getTriggerUser()!=null) 
			str.append(getTriggerUser().getDisplayName());
		
		if (getTask()!=null) {
			if (str.length()>0) str.append(" | ");
			str.append(getTask());
		}
		
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
	public String getEventType() {
		return getClassEventType(); 
	}
	
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
		
		String parameters = super.getParameters();
		System.out.println("PARAMETERS " + parameters);
		try {
			System.out.println("pausa");
			Thread.sleep(5000);
		}
		catch (Exception e) {
			
		}
		Activity activity = null;
		KbeeJson json = getJson();
		System.out.println("json " + json.toString());
		Object processId = json.get("process");
		System.out.println("process " + processId);
		String startTime = (String)json.get("startTime");
		System.out.println("starttime " + startTime);
		ActivitiesQuery query = new ActivitiesQuery(Long.valueOf((String)processId));
		query.getSessionFactory().getCurrentSession().clear();
		ResultSet activities = query.execute();

			//Process process = getWorkflowDao().findProcessById(Long.valueOf((String)processId));
			//getContentDao().refresh(process);
			//process = (Process)getContentDao().reload(process);
		while (activities.hasNext()) {
			Activity a = (Activity)activities.next().getObject();
			String time = getTimeString(a.getStartTime());
			System.out.println("time " + time);
			if (startTime.equals(getTimeString(a.getStartTime()))) {
				activity = a;
				break;
			}
		}	
		System.out.println("a " + (activity==null?"null":activity.getId().toString()));
		activities.close();
		return activity;
	}
	
	@Override
	public String getAction() {
		KbeeJson json = getJson();
		boolean restart = "true".equals(json.get("restart"));
		return (restart ? "[Restart] " : "[Start] ") +  (String)getJson().get("procedure")  +" " + (String)getJson().get("task");
	}
	
	private String getTimeString(OffsetDateTime time) {
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");
		String timeString = fmt.format(time);
		return timeString;
	}
}