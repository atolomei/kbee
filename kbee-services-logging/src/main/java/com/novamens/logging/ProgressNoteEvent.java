package com.novamens.logging;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.base.Content;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.security.Identifiable;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Activity;
import com.novamens.workflow.ActivityProgressNote;


@Entity
@DiscriminatorValue("ProgressNoteEvent")
public class ProgressNoteEvent extends WorkflowEvent {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ProgressNoteEvent.class.getName());

	static public String getClassEventType() {
		return "ProgressNote";
	}
	
	public ProgressNoteEvent() {
	}
	
	public ProgressNoteEvent(Content content, ActivityProgressNote note) {
		super(content);
		
		KbeeJson json = new KbeeJson();
		
		Activity activity = note.getActivity();
		
		String task = activity.getTask().getName();
		String procedure = activity.getProcess().getProcedure().getCode();
		
		json.put("task", task);
		json.put("procedure", procedure);
		json.put("note", escape(note.getText()));
		json.put("noteid", String.valueOf(((Identifiable)note).getId()));
		
		setJson(json);
		
		setEventUser(activity.getUser());
		setTask(task);
		setProcedure(procedure);
	}
	
	@Override
	public String toString() {
	
		StringBuilder str = new StringBuilder();
		
		str.append(getAction());
		
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
	
	public ActivityProgressNote getNote() {
		try {
			
			KbeeJson json = getJson();
			String id = unescape((String)json.get("noteid"));
			
		ActivityProgressNote note = getRepository(ActivityProgressNote.class).findById(Long.valueOf(id));
		return note;
		} catch (Exception e) {
			logger.error(e);
			throw(e);
		}
		
	}
	
	@Override
	public String getDescription() {
		KbeeJson json = getJson();
		String description = unescape((String)json.get("note"));
		return description;
	}
	
	@Override
	public String getAction() {
		return "[Progress Note] " + (String)getJson().get("procedure")  +" " + (String)getJson().get("task");
	}
	
	private <R> DomRepository<R> getRepository(Class<R> objectclass) {
		DomRepository<R> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
	}
}